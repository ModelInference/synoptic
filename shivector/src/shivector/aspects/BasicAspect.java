package shivector.aspects;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import shivector.options.ShiVectorOptions;

/**
 * Aspect to wrap socket streams and intercept logging methods.
 * 
 * @author jennyabrahamson
 */
@Aspect
public class BasicAspect {

    private VectorClock clock;
    private ShiVectorOptions options;

    private static final String processId = ManagementFactory
            .getRuntimeMXBean().getName();
    private static final String macAddress = getMacAddress();

    public BasicAspect() {
        this.options = ShiVectorOptions.getOptions();
        this.clock = new VectorClock(processId + macAddress, options);
    }

    private static String getMacAddress() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i],
                        (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /******************************* Network Aspects *******************************/

    // Adopted from http://www.javaspecialists.eu/archive/Issue169.html
    @Around("call(* java.net.Socket.getInputStream()) && target(s) && !within(shivector..*)")
    public Object wrapInputStream(ProceedingJoinPoint joinPoint, Socket s)
            throws Throwable {
        if (options.useSocketsAPI) {
            InputStream in = (InputStream) joinPoint.proceed();
            return new ShivSocketInputStream(in, clock);
        }
        return joinPoint.proceed();
    }

    @Around("call(* java.net.Socket.getOutputStream()) && target(s) && !within(shivector..*)")
    public Object wrapOutputStream(ProceedingJoinPoint joinPoint, Socket s)
            throws Throwable {
        if (options.useSocketsAPI) {
            OutputStream out = (OutputStream) joinPoint.proceed();
            return new ShivSocketOutputStream(out, clock);
        }
        return joinPoint.proceed();
    }

    @Around("call(* java.nio.channels.SocketChannel.write(..)) && args(buf) && target(s) && !within(shivector.aspects..*)")
    public Object interceptNioWrite(ProceedingJoinPoint joinPoint,
            ByteBuffer buf, SocketChannel s) throws Throwable {
        if (options.useNioAPI) {
            clock.writeClock(s);
        }
        return joinPoint.proceed();
    }

    @Around("call(* java.nio.channels.SocketChannel.read(..)) && args(buf) && target(s) && !within(shivector.aspects..*)")
    public Object interceptNioRead(ProceedingJoinPoint joinPoint,
            ByteBuffer buf, SocketChannel s) throws Throwable {
        if (options.useNioAPI) {
            // NOTE(jennya): for now, we're going to assume that reads mirror
            // writes
            // so that we don't need to keep track of available bytes. Later
            // we'll
            // want to update this implementation to handle non-mirror behavior
            // similar to how the ShivInputStream does by tracking available
            // bytes.
            clock.parseClock(s);
        }
        return joinPoint.proceed();
    }

    @Around("call(* org.apache.mina.core.session.IoSession.write(Object)) && args(msg) && !within(shivector.aspects..*)")
    public Object interceptMinaWrite(ProceedingJoinPoint joinPoint, Object msg)
            throws Throwable {
        if (options.useMinaAPI) {
            byte[] message = clock.getMessageArray(msg);
            return joinPoint.proceed(new Object[] { message });
        }
        return joinPoint.proceed();
    }

    @Around("call(* org.apache.mina.core.session.IoSession.write(Object, SocketAddress)) && args(msg, dest) && !within(shivector.aspects..*)")
    public Object interceptMinaWriteDest(ProceedingJoinPoint joinPoint,
            Object msg, SocketAddress dest) throws Throwable {
        if (options.useMinaAPI) {
            byte[] message = clock.getMessageArray(msg);
            return joinPoint.proceed(new Object[] { message, dest });
        }
        return joinPoint.proceed();
    }

    @Around("execution(void org.apache.mina.core.service.IoHandlerAdapter.messageReceived(.., Object)) && args(session, message) && !within(shivector.aspects..*)")
    public Object interceptMinaRead(ProceedingJoinPoint joinPoint,
            Object session, Object message) throws Throwable {
        if (options.useMinaAPI) {
            Object msg = clock.parseMessageArray((byte[]) message);
            return joinPoint.proceed(new Object[] { session, msg });
        }
        return joinPoint.proceed();
    }

    /******************************* Logging Aspects *******************************/

    private Object print(ProceedingJoinPoint joinPoint, Object obj, boolean flag)
            throws Throwable {
        if (flag) {
            clock.incrementClock();
            return joinPoint.proceed(new Object[] { obj.toString() + "\n"
                    + clock });
        }
        return joinPoint.proceed();
    }

    @Around("call(void *.println(String)) && args(str) && !within(shivector..*)")
    public Object interceptPrintlnLogging(ProceedingJoinPoint joinPoint,
            String str) throws Throwable {
        return print(joinPoint, str, options.usePrintln);
    }

    @Around("call(void org.apache.log4j.Logger.info(*))&& args(obj) && !within(shivector..*)")
    public Object interceptLog4JInfo(ProceedingJoinPoint joinPoint, Object obj)
            throws Throwable {
        return print(joinPoint, obj, options.useLog4J);
    }

    @Around("call(void org.apache.log4j.Logger.warn(*))&& args(obj) && !within(shivector..*)")
    public Object interceptLog4JWarn(ProceedingJoinPoint joinPoint, Object obj)
            throws Throwable {
        return print(joinPoint, obj, options.useLog4J);
    }
}
