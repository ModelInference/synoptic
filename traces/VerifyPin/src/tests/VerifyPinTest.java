package tests;

import static org.junit.Assert.*;

import main.ResponseCode;
import main.VerifyPin;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VerifyPinTest {
    
    private VerifyPin verifyPin;
    
    @Before
    public void setUp() {
        verifyPin = new VerifyPin();
    }
    
    @After
    public void tearDown() {
        VerifyPin.logger.log(VerifyPin.logLevel, "--");
    }
    
    @Test
    public void succeedInOneTryTest() {
        ResponseCode code = verifyPin.enterPin("1234");
        assertTrue(ResponseCode.SUCCESSFUL == code);
    }
    
    @Test
    public void succeedInTwoTriesTest() {
        ResponseCode code = verifyPin.enterPin("1233");
        assertTrue(ResponseCode.UNSUCCESSFUL == code);
        code = verifyPin.enterPin("1234");
        assertTrue(ResponseCode.SUCCESSFUL == code);
    }
    
    @Test
    public void succeedInThreeTriesTest() {
        ResponseCode code = verifyPin.enterPin("1232");
        assertTrue(ResponseCode.UNSUCCESSFUL == code);
        code = verifyPin.enterPin("1233");
        assertTrue(ResponseCode.UNSUCCESSFUL == code);
        code = verifyPin.enterPin("1234");
        assertTrue(ResponseCode.SUCCESSFUL == code);
    }
    
    @Test
    public void failAllThreeTriesTest() {
        ResponseCode code = verifyPin.enterPin("1232");
        assertTrue(ResponseCode.UNSUCCESSFUL == code);
        code = verifyPin.enterPin("1233");
        assertTrue(ResponseCode.UNSUCCESSFUL == code);
        code = verifyPin.enterPin("1235");
        assertTrue(ResponseCode.UNSUCCESSFUL == code);
    }
    
    @Test
    public void correctPinOnForthTryTest() {
        ResponseCode code = verifyPin.enterPin("1232");
        assertTrue(ResponseCode.UNSUCCESSFUL == code);
        code = verifyPin.enterPin("1233");
        assertTrue(ResponseCode.UNSUCCESSFUL == code);
        code = verifyPin.enterPin("1235");
        assertTrue(ResponseCode.UNSUCCESSFUL == code);
        code = verifyPin.enterPin("1234");
        assertTrue(ResponseCode.BLOCKED == code);
    }
    
    @Test
    public void twoTriesAfterBlockedTest() {
        ResponseCode code = verifyPin.enterPin("1232");
        assertTrue(ResponseCode.UNSUCCESSFUL == code);
        code = verifyPin.enterPin("1233");
        assertTrue(ResponseCode.UNSUCCESSFUL == code);
        code = verifyPin.enterPin("1235");
        assertTrue(ResponseCode.UNSUCCESSFUL == code);
        code = verifyPin.enterPin("0000");
        assertTrue(ResponseCode.BLOCKED == code);
        code = verifyPin.enterPin("9999");
        assertTrue(ResponseCode.BLOCKED == code);
    }
    
    @Test
    public void succeedTwiceTest() {
        ResponseCode code = verifyPin.enterPin("1234");
        assertTrue(ResponseCode.SUCCESSFUL == code);
        code = verifyPin.enterPin("1234");
        assertTrue(ResponseCode.SUCCESSFUL == code);
    }
}
