
package tennisscheduler;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Iterator;
/**
 * The Team class contains the Team Specific Data. This class in instantiated
 * from the TeamMgr Class
 * 
 * @author timj
 */
public class Team {
    
    public enum LOCATION {Home,Away};
    
    private String name;
    private int    id;
    private Club   club;
    private Logger log  = TennisScheduler.log;
    private ColorDate.COLOR    color          = ColorDate.COLOR.Either;
    private Map<Date,Integer>  schedMap       = new HashMap<Date,Integer>();
    private Map<Date,Integer>  awayBlackouts  = new HashMap<Date,Integer>();
    private Map<Date,Integer>  homeBlackouts  = new HashMap<Date,Integer>();
    private SimpleDateFormat   dateFormat     = new SimpleDateFormat("EEE MMM dd yyyy");
    private TeamProperties     teamProperties;
    /**
     * Initialize a Team Class
     * 
     * @param id The unique ID for the team
     * @param n The name for the team
     * @param division The division for the team
     * @param c The Club this team is associated with
     */
    Team(int i, String n, Division.NAME division, Club c, TeamProperties tP) {
        this.id    = i;
        this.name  = n;
        this.club  = c;
        this.teamProperties = tP;
        this.color = this.club.getColor(division);
        getTeamBlackouts();
        log.info(String.format("Team() %s %s %s %s",name,club.getName(),division,color.toString()));
    }
    /**
     * Return the Team name as a String.
     * 
     * @return teamName The name of the team as a string
     */
    public String  getName() {return name;}
    /**
     * Returns true if Date is a Team Blackout Date. The TeamBlackout Date has 
     * specific settings for home & away. The Team Blackout Date is the sum of 
     * the blackout dates from the team and the club.
     * @param date 
     * @param location
     * @return The boolean value of the blackout
     */
    public boolean blackout(Date date, LOCATION location ) {
        boolean retVal = false;
        if (location == LOCATION.Away) {
            if (awayBlackouts.containsKey(date)) retVal = true;
        } else {
            if (homeBlackouts.containsKey(date)) retVal = true;
        }
        if (retVal)
            log.debug(String.format("Team.blackout() %s %s %b",name,dateFormat.format(date),retVal));
        return retVal;
        }
    /**
     * Add the specified list of dates to the teams Away Blackout List. It is OK
     * if the date is not there (or already removed).
     * @param dateList List of dates to add to the Away Blackout List
     */
    public void    addAwayBlackoutDates (List<Date> dateList) {
        // Given a list of dates add them to the awayBlackoutDates List
        if (dateList == null) return;
        Iterator it = dateList.iterator();
        while (it.hasNext()) {
            Date date = (Date)it.next();
            awayBlackouts.put(date, 1);
            log.info(String.format("addAwayBlackoutDates() %s (%s) %s", id, name, dateFormat.format(date)));
        }
        return;
        }
    /**
     * Add the specified list of dates to the teams Home Blackout List. It is OK
     * if the date is not there (or already removed).
     * @param dateList List of dates to add to the Home Blackout List
     */
    public void    addHomeBlackoutDates (List<Date> dateList) {
       // Given a list of dates add them to the homeBlackoutDates List
       if (dateList == null) return;
       Iterator it = dateList.iterator();
       while (it.hasNext()) {
           Date date = (Date)it.next();
           homeBlackouts.put(date,1);
           log.info(String.format("addHomeBlackoutDates() %s (%s) %s", id, name, dateFormat.format(date)));
       }
       return;
    }
    /**
     * Return the Club that is home for this team
     * 
     * @return Club The Class for the Club for this team
     */
    public Club    getClub()      {return club;}
    /**
     * Accept this date. Add this Date to the schedule for this team.
     * 
     * @param d The date that was accepted by the scheduler
     */
    public void    accept(Date d) {schedMap.put(d,null);}
    /**
     * Calculate the Weight for the specified date based upon the following:
     * <ul><li>there is a direct conflict</ul>
     * <ul><li>this team played last week</ul>
     * <ul><li>this team has a 'color' preference</ul>
     * 
     * @param date The proposed date to be scheduled
     * @return weight The team weight for the specified Date
     */
    public Weight getWeight(ColorDate date) {
        // Validate a Date. Return a int (weight) The weight is based upon the
        // color of scheduling conflicts. See 'Weight' Class.
        Weight weight = new Weight();
        if (schedMap.containsKey(date)) {
            weight.setDirect();
        } else {
            Calendar cal = Calendar.getInstance();
            // Add weight if this team has been scheduled 1 week before/after
            // or 2 weeks before/after. Special case for +-1 day for LO
            // teams that are moved from Wed -> Tue
            int [] offset = {-1,1,-7,7,-14,14};
            for (int i=0; i<offset.length; i++) {
                cal.setTime(date);
                cal.add(Calendar.DATE, offset[i]); // Set Date forward/backward
                if (schedMap.containsKey(cal.getTime())) {
                    switch (i) {
                    case 0: case 1: weight.setDirect();    break;
                    case 2:         weight.setOneBefore(); break;
                    case 3:         weight.setOneAfter();  break;
                    case 4:         weight.setTwoBefore(); break;
                    case 5:         weight.setTwoAfter();  break;
                    }
                }
            }
            // If the team has a Color preference, add to the weight
            if (color != ColorDate.COLOR.Either)
                if (date.getColor() != color)
                    weight.setColor();
        }
        log.debug(String.format("Team.getWeight() %-6s Date: %s returning: '%s'",name,
                    dateFormat.format(date),weight));
        return weight;
    }
    /**
     * Return the unique Team ID
     * 
     * @return id The unique Team ID
     */
    public int     getID() {return id;}
    @Override public String toString() {
        String str = String.format("%s", name);
        return str;
    }
    /**
     * Get the list of blackout from ClubProperties and TeamProperties.
     */
    private void getTeamBlackouts() {
        addAwayBlackoutDates(club.getTeamBlackoutList(this.id, LOCATION.Away));
        addHomeBlackoutDates(club.getTeamBlackoutList(this.id, LOCATION.Home));
        addAwayBlackoutDates(teamProperties.getAwayBlackoutList(this.id));
        addHomeBlackoutDates(teamProperties.getHomeBlackoutList(this.id));
    }
    
}

