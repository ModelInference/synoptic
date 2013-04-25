package shivector.aspects;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;

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
        this.clock = new VectorClock(processId + macAddress);
        this.options = ShiVectorOptions.getOptions();
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

    // Adopted from http://www.javaspecialists.eu/archive/Issue169.html

    @Around("call(* java.net.Socket.getInputStream()) && target(s) && !within(shivector..*)")
    public Object wrapInputStream(ProceedingJoinPoint joinPoint, Socket s)
            throws Throwable {
        InputStream in = (InputStream) joinPoint.proceed();
        return new ShivSocketInputStream(in, clock);
    }

    @Around("call(* java.net.Socket.getOutputStream()) && target(s) && !within(shivector..*)")
    public Object wrapOutputStream(ProceedingJoinPoint joinPoint, Socket s)
            throws Throwable {
        OutputStream out = (OutputStream) joinPoint.proceed();
        return new ShivSocketOutputStream(out, clock);
    }

    @Around("call(void *.println(..)) && args(str) && !within(shivector..*)")
    public Object interceptPrintlnLogging(ProceedingJoinPoint joinPoint,
            String str) throws Throwable {
        if (options.usePrintln) {
            String modifiedString = "Timestamp: " + clock.toString() + "\n"
                    + str;
            return joinPoint.proceed(new Object[] { modifiedString });
        }
        return joinPoint.proceed(new Object[] { str });
    }

    @Around("call(* org.apache.log4j.Logger.info(..)) && args(msg) && !within(shivector..*)")
    public Object interceptLog4JLogging(ProceedingJoinPoint joinPoint,
            Object msg) throws Throwable {
        if (options.useLog4J) {
            String modifiedString = "Timestamp: " + clock.toString() + "\n"
                    + msg;
            return joinPoint.proceed(new Object[] { modifiedString });
        }
        return joinPoint.proceed(new Object[] { msg });
    }
}
