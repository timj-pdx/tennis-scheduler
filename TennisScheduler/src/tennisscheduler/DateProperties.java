
package tennisscheduler;

import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.io.FileInputStream;
import java.util.StringTokenizer;
/**
 * The class controls acces to the Date Properties. All access to these
 * properties is through a set of 'getters' provided by this class. When the
 * class is instantiated it it open the file (dates.properties) and read all
 * the values into private data structures.
 * 
 * @author timj
 */
public class DateProperties extends PropMgr {
    
     private Date        springStart;
     private int         year;
     private String      propertyFilename = "date.properties";
     private String      springStartKey   = "SpringStart";
     private Logger      log              = TennisScheduler.log;
     private List<Date>  fallDateList     = new ArrayList<Date>();
     private List<Date>  springDateList   = new ArrayList<Date>();
     
    DateProperties() {
        year = TennisScheduler.schedProperties.getYear();
        try {
            this.load(new FileInputStream(propertyFilename));
        } catch (Exception e) {
            System.err.format("Error: %s%n", e.getMessage());
            System.err.println("PropMgr.PropMgr() Exiting....");
            System.exit(1);
        }
        setSpringStart();
        Enumeration e = this.propertyNames();
        while (e.hasMoreElements()) {
            String key = e.nextElement().toString();
            if (key.contentEquals(springStartKey)) continue;
            log.debug(String.format("Club.getMasterDates() %s",key));
            Month  mon = new Month();
            try {
                mon.set(key);
            } catch (Exception ex) {
                log.err(String.format("Club.getMasterDates() : '%s' : Unknown Month : '%s'",
                        propertyFilename, key));
                System.exit(1);
            }
            int yr;
            if (mon.newYear()) yr = year+1;
            else               yr = year;
            String pList = this.getProperty(key);
            StringTokenizer stringTokenizer = new StringTokenizer(pList,"    ");
            while (stringTokenizer.hasMoreElements()) {
                Integer day  = Integer.parseInt(stringTokenizer.nextToken());
                Date date    = strToDate(mon.toInt(), day, yr);
                if (date.before(springStart)) fallDateList.add(date);
                else                          springDateList.add(date);
            }
        }
    }
    /**
     * 
     * @param season The season covered by the the of Dates to be returned
     * @return The list of Dates
     */
    public List<Date> getDateList(Match.SEASON season) {
        if (season == Match.SEASON.Fall)
            return fallDateList;
        return springDateList;
    }
    /**
     * Set the 'SpringStart' Date. Called from the constructor. Since this
     * class is a sub class of the Property Class 'this' is a Property and it
     * is assumed that the  Properties have already been loaded. The following
     * <ul><li>SpringStart 'mon' 'day'</ul>
     * If any errors are encountered we issue an error message and exit.
     */
    private void setSpringStart() {
        String value = this.getProperty(springStartKey);
        if (value == null) {
            log.err(String.format("setSpringStart(): '%s' property missing from '%s'",
                    springStartKey, propertyFilename));
            System.exit(1);
        }
        StringTokenizer stringTokenizer = new StringTokenizer(value,"    ");
        if (stringTokenizer.countTokens() != 2) {
            log.err(String.format("setSpringStart() in '%s'", propertyFilename));
            log.err(String.format("'%s' Property expects two arguments, received %",
                    springStartKey, stringTokenizer.countTokens()));
            log.err(String.format("%s 'mon' 'day'", springStartKey));
            System.exit(1);
        }
        Month  mon = new Month();
        String arg = stringTokenizer.nextToken();
        try {
            mon.set(arg);
        } catch (Exception ex) {
            log.err(String.format("setSpringStart() : '%s' : Unknown Month : '%s'",
                    propertyFilename, arg));
        }
        Integer day = Integer.parseInt(stringTokenizer.nextToken());
        springStart = TennisScheduler.schedProperties.strToDate(mon.toInt(), day, year+1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(springStart);
        if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            log.err(String.format("Club.getMasterDates() '%s' must be a Sunday", springStartKey));
            System.exit(1);
        }
        log.debug(String.format("Club.setSpringStart() '%s' = %s", springStartKey, springStart.toString()));
    }
}
