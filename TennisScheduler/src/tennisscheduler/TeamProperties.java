
package tennisscheduler;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
/**
 * This Class provides access to all the Team specific properties. The properties
 * are read in from the 'team.properties' file. The class provides access to the
 * properties value with a set of 'getter' methods.
 * 
 * @author timj
 */
public class TeamProperties extends PropMgr {
    
    private String  fileName = "team.properties";
    private Logger  log      = TennisScheduler.log;
    private Map<Integer,List<Date>> awayBlackoutMap = new HashMap<Integer,List<Date>>();
    private Map<Integer,List<Date>> homeBlackoutMap = new HashMap<Integer,List<Date>>();
    
    TeamProperties() {
        getBlackoutDates(Team.LOCATION.Away);
        getBlackoutDates(Team.LOCATION.Home);
    }
    /**
     * Given a Team ID return the list of Team Blackout Dates when it is Away
     * 
     * @param team The Team id
     * @return A list of blackout dates
     */
    List<Date> getAwayBlackoutList(int team) {return awayBlackoutMap.get(team);}
    /**
     * Given a Team ID return the list of Team Blackout Dates when it is Home
     * 
     * @param team The Team id
     * @return A list of blackout dates
     */
    List<Date> getHomeBlackoutList(int team) {return homeBlackoutMap.get(team);}
    /**
     * Given a list of team ID, valid that each date in the awayBlackoutMap &
     * homeBlackoutMap in a valid team ID
     * 
     * @param teamList The list of Team ID's
     * 
     */
    public void validateTeamList(Map<Integer,Integer> teamList) {
        Integer key = 0;
        Set words = awayBlackoutMap.keySet();
        for(Iterator i = words.iterator();i.hasNext();) {
            key = (Integer)i.next();
            if ( !teamList.containsKey(key) ) {
                log.err(String.format("Invalid Team ID in team.property file (AWAY): %d", key));
                System.exit(1);
            }
        }
        words = homeBlackoutMap.keySet();
        for(Iterator i = words.iterator();i.hasNext();) {
            key = (Integer)i.next();
            if ( !teamList.containsKey(key) ) {
                log.err(String.format("Invalid Team ID in team.property file (HOME): %d", key));
                System.exit(1);
            }
        }
    }
    /**
     * Given a Match Location restrieve the Group Blackout Information from the
     * properties file. Save the results in awayBlackoutMap or homeBlackoutMap.
     * 
     * @param location The
     */
    private void getBlackoutDates(Team.LOCATION location) {
        String      name = String.format("BLACKOUT-%s",location.toString().toUpperCase());
        int         year = TennisScheduler.schedProperties.getYear();
        Section     sect = new Section(name,fileName);
        Enumeration e    = sect.getProp().propertyNames();
        while (e.hasMoreElements()) {
            String strKey   = e.nextElement().toString();
            int intKey      = 0;
            try {
                intKey = Integer.parseInt(strKey);
            } catch (Exception ex) {
                log.err(String.format("TeamProperties() Error: %s", ex.getMessage()));
                log.err("Expected an 'int'");
                System.exit(1);
            }
            String value     = sect.getProp().getProperty(strKey);
            List<Date> bList = parseDatesString(value, year, fileName);
            if (location == Team.LOCATION.Away) awayBlackoutMap.put(intKey,bList);
            else                                homeBlackoutMap.put(intKey,bList);
        }
    }
}
