/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tennisscheduler;

import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

public class PropMgr extends Properties {
    
    public enum DIV_NAME {A, B, C, D, E, F, G, H, I, J, K}
    
    protected DateFormat       readDateformat   = new SimpleDateFormat("MM-dd-yyyy");
    protected SimpleDateFormat dateFormat       = new SimpleDateFormat("EEE MMM dd yyyy");
    
    protected List<Date>  parseDatesString(String value, int year, String fileName) {
        Month mon = new Month();
        int   day = 0;
        int   yr  = 0;
        List<Date> dateList = new ArrayList<Date>();
        StringTokenizer stringTokenizer = new StringTokenizer(value,"    ");
        while (stringTokenizer.hasMoreElements()) {
            String token = stringTokenizer.nextToken();
            try {
                mon.set(token);
                if (mon.toInt() > 5) yr = year;
                else                 yr = year + 1;
            } catch (Exception ex) {
                if (mon.isEmpty()) {
                    System.err.format("Bad Month '%s' in '%s'%n", token,fileName);
                    System.exit(1);
                }
                try {
                    day = Integer.parseInt( token );
                } catch( Exception ex1 ) {
                    System.err.format("Invalid day %s in '%s'%n",token,fileName);
                    System.exit(1);
                }
                Date date = strToDate(mon.toInt(),day,yr);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY &&
                            cal.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
                    SimpleDateFormat dateFormat  = new SimpleDateFormat("EEE MMM dd");
                    System.err.format("Bad Day of the week: %s. Only Tue & Wed allowed in '%s'",
                                 dateFormat.format(date), fileName);
                    System.exit(1);
                }
                dateList.add(date);
            }
        }
        return dateList;
    }
    /**
     * Convert the mon/day/yr to a Date. The time is assumed to be 00:00
     * 
     * @param mon The Month
     * @param day The Day
     * @param yr  The Year
     * @return The Date
     */
    protected Date        strToDate(int mon, int day, int yr) {
        Date   date    = new Date();
        String dateStr = String.format("%02d-%02d-%4d", mon,day,yr);
        try {
            date = readDateformat.parse(dateStr);
        } catch (Exception e) {
            System.err.println(e + e.getMessage());
            System.err.println("Club.strToDate() Exiting....");
            System.exit(1);
        }
        return date;
    }
     /**
     * Return an enum (Division.NAME) based on the specified String. If the
     * String does not map to a valid Enum, return 'null'
     * 
     * @param str String to map to the enum
     * @return The NAME enum
     */
    public    Division.NAME str2Name(String str) {
        try {
            return Enum.valueOf(Division.NAME.class, str);
        } catch(IllegalArgumentException e) {
            return null;
        }
    }
}
