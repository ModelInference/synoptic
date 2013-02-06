import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Aspect to wrap socket streams and intercept logging methods.
 * 
 * @author jennyabrahamson
 */
@Aspect
public class BasicAspect {

    private VectorClock clock;

    public BasicAspect() {
        this.clock = new VectorClock();
    }

    // Adopted from http://www.javaspecialists.eu/archive/Issue169.html

    @Around("call(* java.net.Socket.getInputStream()) && target(s)")
    public Object wrapInputStream(ProceedingJoinPoint joinPoint, Socket s)
            throws Throwable {
        InputStream in = (InputStream) joinPoint.proceed();
        return new ShivSocketInputStream(in, s, clock);
    }

    @Around("call(* java.net.Socket.getOutputStream()) && target(s)")
    public Object wrapOutputStream(ProceedingJoinPoint joinPoint, Socket s)
            throws Throwable {
        OutputStream out = (OutputStream) joinPoint.proceed();
        return new ShivSocketOutputStream(out, s, clock);
    }

    @Around("call(void *.println(..)) && args(str)")
    public Object interceptLogging(ProceedingJoinPoint joinPoint, String str)
            throws Throwable {
        String modifiedString = "Timestamp: " + clock.toString() + "\n" + str;
        return joinPoint.proceed(new Object[] { modifiedString });
    }
}
