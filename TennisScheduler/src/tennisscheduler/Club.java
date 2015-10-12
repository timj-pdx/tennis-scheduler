/*
 * Club Class
 * 
 * As a container it contains the 'club specific' data and methods to
 * access/use this data
 * 
 * The class contructor extract the 'club specific' properties from the the
 * 'clubPropertyFile'. It will generate the time slots by getting the master
 * cDate list from 'datePropertyFile' and removes any club blackout dates.
 * 
 */
package tennisscheduler;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.io.PrintStream;
/**
 * A class for a Club. It contains all the club specific data. It will retrieve
 * Properties' using the 'ClubProperties' Class.
 * 
 * I will be asked the scheduler to provide a list of available time slots. If
 * any of the time slots are accepted the scheduler will 'accept' the cDate and
 * the time is removed from from the list of available time slots.
 *
 * The club properties can be used to specify a scheduling affinity for zero
 * or more divisions. Each available cDate has an affinity assigned to it
 * 
 * @author timj
 */
public class Club {
    
    private enum BOOK {Single,Double};
    
    private ClubName          name;
    private String            strName;
    private String            tueMatchTime;
    private String            wedMatchTime;
    private int               queueSize;
    private SimpleDateFormat  dateFormat     = new SimpleDateFormat("EEE MMM dd yyyy");
    private List<ColorDate>   tueFallSlots   = new ArrayList<ColorDate>();
    private List<ColorDate>   wedFallSlots   = new ArrayList<ColorDate>();
    private List<ColorDate>   tueSpringSlots = new ArrayList<ColorDate>();
    private List<ColorDate>   wedSpringSlots = new ArrayList<ColorDate>();
    private Map<Date,Integer> acceptedDates  = new HashMap<Date,Integer>();
    private List<String>      goldList       = new ArrayList<String>(); // List of 'gold' divisions
    private List<String>      blueList       = new ArrayList<String>(); // List of 'blue' divisions
    private boolean           doubleBookTue  = false;
    private boolean           doubleBookWed  = false;
    private Logger            log            = TennisScheduler.log;
    private Map<Date,BOOK>    fallDateMap    = new HashMap<Date,BOOK>();
    private Map<Date,BOOK>    springDateMap  = new HashMap<Date,BOOK>();
    private Stack<Date>       blackout       = new Stack<Date>();
    private ClubProperties    clubProperties = TennisScheduler.clubProperties;
    private DateProperties    dateProperties = new DateProperties();
    private static ColorDate.COLOR curColor  = ColorDate.COLOR.Blue;
    
    Club(ClubName n) {
        name      = n;
        queueSize = TennisScheduler.schedProperties.getQueueSize();
        getProperties();
        getMasterDates(Match.SEASON.Fall);
        getMasterDates(Match.SEASON.Spring);
        applyBlackoutDates();
        setDateList(Match.SEASON.Fall);
        setDateList(Match.SEASON.Spring);
    }
    /**
     * Return the ClubName
     *
     * @return clubName
     */
    public ClubName getName()    {return name;}
    /**
     * Return the Club ID
     * 
     * @return clubID
     */
    // FIX delete this method
    public int      getID()      {return name.getValue();}
    /**
     * Return the Club Name as a String
     * @return The Club Name as a String
     */
    public String   getStrName() {return strName;}
    /**
     * Get the Weight for the specified Date.
     * We know that exactly three things are possible
     * <ul>
     *     <li>Single Booking allowed:
     * <ul>        1. This date has not been assigned: return 0         </ul>
     *      <li>Double Booking allowed:
     * <ul>        2. This date has not been assigned: return 0          </ul>
     * <ul>        3. This date has been assigned once: return Team.Club </ul>
     * </ul>
     * @param cDate The cDate we want to get the weight for
     * @return The weight for the specified Date.LONG
     */
    public boolean isWeighted(Date d) {
        boolean retVal = (acceptedDates.containsKey(d));
        log.debug(String.format("Club.isWeighted() %s %s returning: %b",name,d,retVal));
        return retVal;
    }
    /**
     * Accept the specified Date. There are four available slot (Date) lists:
     * <UL> tueFallSlots, wedFallSlots, tueSpringSlots, wedSpringSlots</UL>
     * Move the time slot from the list of available
     * slots to the 'acceptedDates' list.
     * 
     * @param season The season we are currently scheduling
     * @param wd The Day (Tue or Wed) we are currently scheduling
     * @param cDate The Date we are currently scheduling
     */
    public void     acceptDate(Match.SEASON season, Match.WEEKDAY wd, ColorDate date) {
        List<ColorDate> slots;
        if (season.equals(Match.SEASON.Fall)) {
            if (wd.equals(Match.WEEKDAY.Tue)) slots = tueFallSlots;
            else                              slots = wedFallSlots;
        } else {
            if (wd.equals(Match.WEEKDAY.Tue)) slots = tueSpringSlots;
            else                              slots = wedSpringSlots;
        }
        slots.remove(date);
        acceptedDates.put(date,null);
        log.debug(String.format("Club.acceptDate() %s %s %d %d", name,wd.name(),acceptedDates.size(),slots.size()));
    }
    /**
     * Print the crrently available time slots for the specified season to the
     * specified Stream. This method is usually called at the before and after
     * the scheduling has commenced.
     * @param season The season we are currently scheduling
     * @param ps     The stream we will print to. This must be a valid, open stream
     */
    public void     printAvailable(Match.SEASON season, PrintStream ps) {
        List<Date> tueSlots;
        List<Date> wedSlots;
        if (season.equals(Match.SEASON.Fall)) {
            tueSlots = new ArrayList<Date>(tueFallSlots);
            wedSlots = new ArrayList<Date>(wedFallSlots);
        } else {
            tueSlots = new ArrayList<Date>(tueSpringSlots);
            wedSlots = new ArrayList<Date>(wedSpringSlots);
        } 
        ps.format("#### Club: %4s Tues: %2d Wed: %2d ####%n", name,tueSlots.size(),wedSlots.size());
        Collections.sort(tueSlots);
        Collections.sort(wedSlots);
        Iterator iter = tueSlots.iterator();
        while (iter.hasNext())
            ps.format("    %s%n", dateFormat.format(iter.next()));
        iter = wedSlots.iterator();
        while (iter.hasNext())
            ps.format("    %s%n",dateFormat.format(iter.next()));
    }
    /**
     * Get the Match Time for the specified day
     * @param day The day we want the Match Time for
     * @return Match time
     */
    public String   getMatchTime(Match.WEEKDAY day)  {
        if (day == Match.WEEKDAY.Tue) return tueMatchTime;
        else                                    return wedMatchTime;
    }
    /**
     * Get the Color for the specified division. The curColor is the affinity
     * assigned to team within a Division. Possible colors are: Gold, Blue &
     * Either. The 'Either' value means the Club does not honor affinity.
     * @param division The division we want the 'curColor' for
     * @return The Color for the specified Division
     */
    public ColorDate.COLOR  getColor(Division.NAME division) {
        ColorDate.COLOR retVal = ColorDate.COLOR.Either;
        if (goldList.contains(division.toString()))
            retVal = ColorDate.COLOR.Gold;
        else if (blueList.contains(division.toString()))
            retVal = ColorDate.COLOR.Blue;
        log.debug(String.format("Club.getDayWeight() %s %s %s",name,division.toString(),retVal.toString()));
        return retVal;
    }
    /**
     * Return a list (Queue) for the specified season, division & day. The
     * queueSize can be specified in the Scheduler Properties File
     * 
     * @param season  The season we are currently scheduling
     * @param divName The division we are currently scheduling
     * @param wd      The day we are currently scheduling
     * @return A queue of available dates
     */
    public Queue<ColorDate> getDateQueue(Match.SEASON season, Division.NAME divName, Match.WEEKDAY wd) {
        List<ColorDate> slots;
        if (season.equals(Match.SEASON.Fall)) {
            if (wd.equals(Match.WEEKDAY.Tue)) slots = tueFallSlots;
            else                              slots = wedFallSlots;
        } else {
            if (wd.equals(Match.WEEKDAY.Tue)) slots = tueSpringSlots;
            else                              slots = wedSpringSlots;
        }
        Queue<ColorDate> dateQueue = new LinkedList<ColorDate>();
        int i = 0;
        while (dateQueue.size() < queueSize) {
            if (i >= slots.size()) break;
            ColorDate cDate = slots.get(i);
            if (!dateQueue.contains(cDate))
                dateQueue.add(cDate);
            i++;
        }
        log.debug(String.format("Club.getDateQueue() %s %s %d %d",name,wd.toString(),slots.size(),dateQueue.size()));
        return(dateQueue);
    }
    @Override public String toString() {
        String str;
        str  = String.format("Full Name:         %s%n", strName);
        str += String.format("Name:              %s%n", name);
        str += String.format("%n Dates available: Tue %2d Wed %2d%n", tueFallSlots.size(), wedFallSlots.size());
        Iterator iter = tueFallSlots.iterator();
        while (iter.hasNext())
            str += String.format("    %s%n", dateFormat.format(iter.next()));
        iter = wedFallSlots.iterator();
        while (iter.hasNext())
            str += String.format("    %s%n",dateFormat.format(iter.next()));
        return str;
    }
    /**
     * Retrieve the Master Date list from the Date Properties Class. If this
     * club supports Double Booking add the extra dates.
     * 
     * @param season The season we are currently scheduling
     */
    private void getMasterDates(Match.SEASON season) {
        List<Date> dateList = dateProperties.getDateList(season);
        Map<Date,BOOK> dateMap;
        if (season == Match.SEASON.Fall) dateMap = fallDateMap;
        else                                       dateMap = springDateMap;
        Iterator it = dateList.iterator();
        while (it.hasNext()) {
            Date date    = (Date)it.next();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if      (cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY && doubleBookTue)
                dateMap.put(date,BOOK.Double);
            else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY && doubleBookWed)
                dateMap.put(date,BOOK.Double);
            else
                dateMap.put(date,BOOK.Single);
        }
    }
    /**
     * Apply the Club Blackout Dates to the existing available cDate list. Do it
     * for both Fall & Spring
     */
    private void applyBlackoutDates() {
        log.info(String.format("Club.applyBlackoutDates() %s",name));
        while (!blackout.empty()) {
            Date date = blackout.pop();
            // If this cDate allows double booking just remove one
            if (fallDateMap.containsKey(date)) {
                if (fallDateMap.get(date) == BOOK.Double) 
                    fallDateMap.put(date, BOOK.Single);
                else
                    fallDateMap.remove(date);
            }
            if (springDateMap.containsKey(date)) {
                if (springDateMap.get(date) == BOOK.Double)
                    springDateMap.put(date, BOOK.Single);
                else
                    springDateMap.remove(date);
            }
        }
        blackout.clear();
    }
    /**
     * 
     * @param season The season we are currently scheduling
     */
    private void setDateList(Match.SEASON season) {
        List<ColorDate>     tueSlots;
        List<ColorDate>     wedSlots;
        Map<Date,BOOK> dateMap;
        if (season.equals(Match.SEASON.Fall)) {
            tueSlots = tueFallSlots;
            wedSlots = wedFallSlots;
            dateMap  = fallDateMap;
        } else {
            tueSlots = tueSpringSlots;
            wedSlots = wedSpringSlots;
            dateMap  = springDateMap;
        }
        Iterator it = dateMap.keySet().iterator(); 
        while (it.hasNext()) {
            Date date = (Date)it.next();
            Match.WEEKDAY wd = getDayOfWeek(date);
            ColorDate cDate  = getColorDate(date);
            if (wd.equals(Match.WEEKDAY.Tue)) {
                tueSlots.add(cDate);
                if (doubleBookTue && dateMap.get(date) == BOOK.Double)
                    tueSlots.add(cDate);
            } else {
                wedSlots.add(cDate);
                if (doubleBookWed && dateMap.get(date) == BOOK.Double)
                    wedSlots.add(cDate);
            }
        }
        dateMap.clear();
        Collections.shuffle(tueSlots);
        Collections.shuffle(wedSlots);
        //System.out.println(tueSlots);
        //System.out.println(wedSlots);
    }
    private ColorDate getColorDate(Date date) {
       // ColorDate.COLOR curColor = ColorDate.COLOR.Blue;
        ColorDate cDate = new ColorDate(date, curColor);
        if (curColor == ColorDate.COLOR.Blue) curColor = ColorDate.COLOR.Gold;
        else                               curColor = ColorDate.COLOR.Blue;
        return cDate;
    }
    private void getProperties() {
        this.doubleBookTue = clubProperties.isDoubleBookTue(name);
        this.doubleBookWed = clubProperties.isDoubleBookWed(name);
        this.goldList      = clubProperties.getGoldList(name);
        this.blueList      = clubProperties.getBlueList(name);
        this.tueMatchTime  = clubProperties.getTueMatchTime(name);
        this.wedMatchTime  = clubProperties.getWedMatchTime(name);
        this.blackout      = clubProperties.getBlackouts(name);
    }
    public  List<Date>              getTeamBlackoutList(int id,Team.LOCATION stat) {
        String sectName = String.format("BLACKOUT-%s-%d",stat.toString().toUpperCase(),id);
        log.info(String.format("Club.getProperties() %s", sectName));
        Section sect  = new Section(name.toString(),TennisScheduler.clubPropertyFileName);
        Enumeration e = sect.getProp().propertyNames();
        //while (e.hasMoreElements()) {
        Date d = new Date();
        List<Date> dateList = new ArrayList<Date>();
        return dateList;
    }
    private Match.WEEKDAY getDayOfWeek(Date myDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(myDate);
        switch ( cal.get(Calendar.DAY_OF_WEEK) ) {
            case 3: return Match.WEEKDAY.Tue ;
            case 4: return Match.WEEKDAY.Wed ;  
            default:
                log.err(String.format("Club.getDayOfWeek() : Date '%s' is invalid", dateFormat.format(myDate)));
                System.exit(1);
                return Match.WEEKDAY.Tue; // Can't get here
        }
    }
    
}
