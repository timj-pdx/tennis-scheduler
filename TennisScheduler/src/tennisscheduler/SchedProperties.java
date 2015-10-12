
package tennisscheduler;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Date;
import java.util.Enumeration;
import java.util.TreeMap;
import java.io.FileInputStream;
/**
 * Manage the properties located in Scheduler Properties file. The constructor
 * will read in all the properties when the class is instanciated. The property
 * values are then accessed through the provided 'get' methods.
 * 
 * @author timj
 */

public class SchedProperties extends PropMgr {
    
    private Logger.LEVEL logLevel;
    private int          year           = 0;
    private int          queueSize      = 0;
    private int          numMatches     = 0;
    private String       tueMatchTime   = null;
    private String       wedMatchTime   = null;
    private String                 propertyFileName = "scheduler.properties";
    private List<Division.NAME>    oList            = new ArrayList<Division.NAME>();
    private Map<String,List<Date>> groupBlackoutMap = new HashMap<String,List<Date>>();
    private Map<String,Integer>    clubNameMap      = new HashMap<String,Integer>();
    
    SchedProperties() {
        try {
            this.load(new FileInputStream(propertyFileName));
        } catch (Exception e) {
            System.err.format("Error: %s%n", e.getMessage());
            System.err.println("PropMgr.PropMgr() Exiting....");
            System.exit(1);
        }
        // We cannot control/predict the order properties in the loop below.
        // The year is required to evaluate any property with a date so
        // we need to get the year property first.
        initYear();
        Enumeration e = this.propertyNames();
        while (e.hasMoreElements()) {
            String key = e.nextElement().toString();
            String value = this.getProperty(key);
            if (value == null) {
                System.err.format("The '%s' property must be specified in: '%s'%n",
                        key, propertyFileName);
                System.exit(1);
            }
            if (key.matches("Year")) {
                // Already have the 'year' property
                continue;
            } else if (key.matches("LogLevel")) {
                String str = value.toLowerCase();
                if      (str.equals("debug")) logLevel = Logger.LEVEL.Debug;
                else if (str.equals("info"))  logLevel = Logger.LEVEL.Info;
                else if (str.equals("error")) logLevel = Logger.LEVEL.Error;
                else if (str.equals("none"))  logLevel = Logger.LEVEL.None;
                else {
                    System.err.format("SchedProperties.initLogLevel() Unknown level: %s%n",str);
                    System.err.println("Defaulting to: 'Error'");
                }
                System.out.println("LogLevel      = "+logLevel.toString());
                continue;
            } else if (key.matches("NumMatches")) {
                numMatches = Integer.parseInt(value);
                System.out.println("MatchNum      = "+numMatches);
                continue;
            } else if (key.matches("ClubQueueSize")) {
                queueSize = Integer.parseInt(value);
                System.out.println("ClubQueueSize = "+queueSize);
                continue;
            } else if (key.matches("DivisionOrder")) {
                StringTokenizer stringTokenizer = new StringTokenizer(value,"    ");
                while (stringTokenizer.hasMoreElements()) {
                    String divStr = stringTokenizer.nextToken();
                    Division.NAME divName = str2Name(divStr);
                    if (divName == null) {
                        System.err.format("Invalid Division Name: '%s' for %s in %s",
                            key,key,TennisScheduler.clubPropertyFileName);
                        System.exit(1);
                    }
                oList.add(divName);
                }
                System.out.println("DivisionOrder = "+oList);
                continue;
            } else if (key.matches("Break-OR")) {
                List<Date> dateList = parseDatesString(value, year, propertyFileName);
                groupBlackoutMap.put("OR", dateList);
                continue;
            } else if (key.matches("Break-WA")) {
                List<Date> dateList = parseDatesString(value, year, propertyFileName);
                groupBlackoutMap.put("WA", dateList);
                continue;
            } else if (key.matches("TueMatchTime")) {
                tueMatchTime = value;
                continue;
            } else if (key.matches("WedMatchTime")) {
                wedMatchTime = value;
                continue;
            } else {
                int id = Integer.parseInt(value);
                clubNameMap.put(key, id);
            }
        }
        // Print the clubNameMap entries, sorted by Key
        Map<String, Integer> treeMap = new TreeMap<String, Integer>(clubNameMap);
        System.out.format("ClubMap       = %s%n%n",treeMap.toString());
        
    }
    /**
     * Get the size of the Queue
     * 
     * @return The size of the Queue
     */
    public int                    getQueueSize()      {return queueSize;}
    /**
     * Get the Year
     * 
     * @return The Year for the Fall Session
     */
    public int                    getYear()           {return year;}
    /**
     * Get the Number of Matches to schedule in each division before the
     * scheduler moves on the next division.
     * 
     * @return The number of Matches
     */
    public int                    getNumMatches()     {return numMatches;}
    /**
     * Get the order that the divisions will be scheduled in
     * 
     * @return The Division schedule order
     */
    public List<Division.NAME>    getDivisionOrder()  {return oList;}
    /**
     * Get the Map of Group Blackout Dates. The key is the name of the group
     * I.E. "OR", "WA"
     * 
     * @return The Map of Group Blackout Dates
     */
    public Map<String,List<Date>> getGroupBlackouts() {return groupBlackoutMap;}
    /**
     * Get the Log Level for log messages being sent to the log file.
     * 
     * @return The log Level
     */
    public Logger.LEVEL           getLogLevel()       {return logLevel;}
    /**
     * Get the Default Tue Match Time. This time can be overridded by a Club
     * Property
     * 
     * @return The Default Tue Match Time.
     */
    public String                 getTueMatchTime()   {return tueMatchTime;}
    /**
     * Get the Default Wed Match Time. This time can be overridded by a Club
     * Property
     * 
     * @return The Default Wed Match Time.
     */
    public String                 getWedMatchTime()   {return wedMatchTime;}
    /**
     * Get the Map containing all the Club Names (and ID's).
     * 
     * @return The map of Club Names.
     */
    public Map<String,Integer>    getClubNameMap()    {return clubNameMap;}
    private void initYear() {
        String key      = "Year";
        String yearStr  = this.getProperty(key);
        if (yearStr == null) {
            System.err.format("The '%s' property must be specified in: '%s'%n",
                    key, propertyFileName);
            System.exit(1);
        }
        year = Integer.parseInt(yearStr);
        System.out.println("Year          = "+year);
    }
}
