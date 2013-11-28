package main;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * VerifyPin is a simple authentication program that verifies pin.
 * It allows pin entering for at most 3 times.
 * 
 *
 */
public class VerifyPin {
    public static Logger logger = Logger.getLogger(VerifyPin.class.getName());
    public static Level logLevel = Level.FINE;
    static {
        logger.setLevel(logLevel);
        Handler handler = new ConsoleHandler();
        handler.setLevel(logLevel);
        logger.addHandler(handler);
    }
    
    private static final String KEY = "1234";
    private static final int MAX_COUNT_PIN = 3;
    
    private int countPin;
    
    public VerifyPin() {
        countPin = 0;
    }
    
    public ResponseCode enterPin(String pin) {
        logger.log(logLevel, "enter-pin");
        logger.log(logLevel, "{" + "pin=\"" + pin + "\",KEY=\"" + KEY + "\"}");
        
        if (countPin >= MAX_COUNT_PIN) {
            logger.log(logLevel, "pin-blocked");
            return ResponseCode.BLOCKED;
        }
        
        if (pin.equals(KEY)) {
            logger.log(logLevel, "access-granted");
            countPin = 0;
            return ResponseCode.SUCCESSFUL;
        }
        
        logger.log(logLevel, "access-denied");
        countPin++;
        return ResponseCode.UNSUCCESSFUL;
    }
}
