package synopticgwt.client.util;

/**
 * Why does this exist?
 * 
 * The goal is to be able to have one setter for both mouseover and mouseout
 * on raphael text labels that is in the raphael text label wrapper itself.
 * 
 * The setter method takes an object implementing MouseHover and registers
 * the appropriate function to the text label
 * 
 * So now, mouseover/mouseout interactions can be written in java and
 * reasonably registered into their JS objects.
 * 
 * Written for raphael text elements, but probably easily reusable for other 
 * raphael element wrappers
 * 
 * @author timjv
 *
 */
public interface MouseHover {
    void mouseover();
    void mouseout();
}
