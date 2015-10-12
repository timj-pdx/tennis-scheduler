
package tennisscheduler;

import java.util.Date;
/**
 * A wrapper for the Date Class that adds getColor 'color' to a Date. Each team
 * in a club is  assigned an 'color' based on the 'club.properties' file. The
 * affinity of a Club can be Blue or Either (Don't Care). A Date can only
 * have Gold or Blue affinity.
 * 
 * @author timj
 */
public class ColorDate extends Date {
    
    public enum COLOR {Gold,Blue,Either};
    
    private COLOR  color;
    private Logger log   = TennisScheduler.log;
    
    ColorDate(Date d, COLOR c) {
        super(d.getTime());
        if (c == COLOR.Either)
            log.err("ColorDate() Date cannot be 'Either'");
        color = c;
    }
    /**
     * Return the color attribute for this Date
     * 
     * @return TJe Color for this Date
     */
    public COLOR getColor() {return color;}
}
