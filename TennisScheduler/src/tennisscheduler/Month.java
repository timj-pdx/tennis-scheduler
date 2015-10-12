
package tennisscheduler;

import java.util.EmptyStackException;

/**
 * Primarily a 'wrapper' around a 'enum' with specified values for each of the
 * months. This wrapper is still 'type safe' but has a 'set' function for
 * converting a 'string' to a 'Month'.
 */
public class Month {
    enum ORD {
        Jan(1),Feb(2),Mar(3),Apr(4),May(5),Sept(9),Oct(10),Nov(11),Dec(12);
        private int id;
        private ORD(int value) {this.id = value;}
        public int toInt() {return id;}
    }
    
    static ORD JAN = ORD.Jan;
    static ORD FEB = ORD.Feb;
    static ORD MAR = ORD.Mar;
    static ORD APR = ORD.Apr;
    static ORD MAY = ORD.May;
    static ORD SEP = ORD.Sept;
    static ORD OCT = ORD.Oct;
    static ORD NOV = ORD.Nov;
    static ORD DEC = ORD.Dec;
    
    private ORD  month = null;
    /**
     * The Month constructor just creates and 'empty' month with a null value.
     * 
     */
    Month()      {}
    /**
     * Set the ORD value to the specified month. If the String does not
     * match any values of the ORD Enum throw an exception.
     * 
     * @param s The string to
     */
    public void    set(String s) {
        String str = s.substring(0,3).toLowerCase();
        if (str.contentEquals("jan")) {
            month = ORD.Jan;
        } else if (str.contentEquals("feb")) {
            month = ORD.Feb;
        } else if (str.contentEquals("mar")) {
            month = ORD.Mar;
        } else if (str.contentEquals("apr")) {
            month = ORD.Apr;
        } else if (str.contentEquals("may")) {
            month = ORD.May;
        } else if (str.contentEquals("sep")) {
            month = ORD.Sept;
        } else if (str.contentEquals("oct")) {
            month = ORD.Oct;
        } else if (str.contentEquals("nov")) {
            month = ORD.Nov;
        } else if (str.contentEquals("dec")) {
            month = ORD.Dec;
        } else {
            throw new EmptyStackException();
        }
    }
    public boolean newYear() {
        switch(month) {
            case Jan:
            case Feb:
            case Mar:
            case Apr:
            case May:
                return true;
        }
        return false;
    }
    /**
     * Get the ordinal (int) representation for the Month. It will cause a
     * a NullPointerException.
     * 
     * @return The ordinal value
     */
    public int     toInt()   {return month.toInt();}
    /**
     * Return true if the Month is Empty (set() has not been called), else
     * return false.
     * 
     * @return True if Month is empty
     */
    public boolean isEmpty() {return month==null;}
    @Override public String toString() {return month.name();}
    
}
