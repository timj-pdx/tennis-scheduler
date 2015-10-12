
package tennisscheduler;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
/**
 * Extract the properties from the Club Properties file and store them. Provide
 * Provide a set of 'getters' to retrieve the properties. The properties are
 * stored in a map keyed on 'ClubNameStr' Provide 'get' Methods to grab the
 * properties from the maps keyed on the 'ClubNameStr' string
 */
public class ClubProperties extends PropMgr {
    
    private Logger  log              = TennisScheduler.log;
    private String  propertyFilename = "club.properties";
    private Map<ClubName, String>       clubNameStrMap   = new HashMap<ClubName, String>();
    private Map<ClubName, Boolean>      doubleBookTueMap = new HashMap<ClubName, Boolean>();
    private Map<ClubName, Boolean>      doubleBookWedMap = new HashMap<ClubName, Boolean>();
    private Map<ClubName, List<String>> goldListMap      = new HashMap<ClubName, List<String>>();
    private Map<ClubName, List<String>> blueListMap      = new HashMap<ClubName, List<String>>();
    private Map<ClubName, Stack<Date>>  blackoutMap      = new HashMap<ClubName, Stack<Date>>();
    private Map<ClubName, String>       tueMatchTimeMap  = new HashMap<ClubName, String>();
    private Map<ClubName, String>       wedMatchTimeMap  = new HashMap<ClubName, String>();
    private Map<String,List<String>>    teamMap          = new HashMap<String, List<String>>();
 
    /**
     * This constructor will read the default Club Properties file and store
     * the results.
     */
    ClubProperties() {
        int    year         = TennisScheduler.schedProperties.getYear();
        String tueMatchTime = TennisScheduler.schedProperties.getTueMatchTime();
        String wedMatchTime = TennisScheduler.schedProperties.getWedMatchTime();
        for ( ClubName clubName : TennisScheduler.clubNameMgr) {
            log.info(String.format("ClubProperties() for %s", clubName));
            Section sect  = new Section(clubName.getStr(),propertyFilename);
            clubNameStrMap.put(clubName, "No Name");
            doubleBookTueMap.put(clubName, Boolean.FALSE);
            doubleBookWedMap.put(clubName, Boolean.FALSE);
            List<String> goldList = new ArrayList<String>();
            List<String> blueList = new ArrayList<String>();
            goldListMap.put(clubName, goldList);
            blueListMap.put(clubName, blueList);
            tueMatchTimeMap.put(clubName, tueMatchTime);
            wedMatchTimeMap.put(clubName, wedMatchTime);
            Stack<Date>  blackout = new Stack<Date>();
            blackoutMap.put(clubName, blackout);
            Enumeration e = sect.getProp().propertyNames();
            while (e.hasMoreElements()) {
                Month mon  = new Month();
                String key = e.nextElement().toString();
                try {
                    mon.set(key);
                } catch (Exception ex) {
                    // It was not a valid 'mon' arg
                    if (key.matches("Name")) {
                        String value = sect.getProp().getProperty(key);
                        clubNameStrMap.put(clubName, value);
                        continue;
                    } else if (key.matches("DoubleBookTue")) {
                        log.debug(String.format("ClubProperties() %s: DoubleBookTue",clubName));
                        doubleBookTueMap.put(clubName, Boolean.TRUE);
                        continue;
                    } else if (key.matches("DoubleBookWed")) {
                        log.debug(String.format("ClubProperties() %s: DoubleBookWed",clubName));
                        doubleBookWedMap.put(clubName, Boolean.TRUE);
                        continue;
                    } else if (key.matches("GoldDays")) {
                        log.debug(String.format("ClubProperties() %s: EvenDay",clubName));
                        String values = sect.getProp().getProperty(key);
                        addTokensToList(values, goldList, clubName);
                        continue;
                    } else if (key.matches("BlueDays")) {
                        log.debug(String.format("ClubProperties() %s: OddDay",clubName));
                        String values = sect.getProp().getProperty(key);
                        addTokensToList(values, blueList, clubName);
                        continue;
                    } else if (key.matches("MatchTimeTue")) {
                        String value = sect.getProp().getProperty(key);
                        tueMatchTimeMap.put(clubName,value);
                        continue;
                    } else if (key.matches("MatchTimeWed")) {
                        String value = sect.getProp().getProperty(key);
                        wedMatchTimeMap.put(clubName,value);
                        continue;
                    } else {
                        log.err(String.format("ClubProperties() '%s' Unknown Property '%s'",clubName,key));
                        System.exit(1);
                    }
                }
                // It is a Club Blackout entry
                int yr;
                if (mon.toInt() > 5) yr = year;
                else                 yr = year + 1;
                String pList = sect.getProp().getProperty(key);
                StringTokenizer stringTokenizer = new StringTokenizer(pList,"    ");
                while (stringTokenizer.hasMoreElements()) {
                    Integer day  = Integer.parseInt(stringTokenizer.nextToken());
                    Date    date = strToDate(mon.toInt(), day, yr);
                    blackout.push(date);
                }
            }
        log.debug(String.format("ClubProperties() %s Gold: %d Blue: %d Blackouts: %d",clubName,goldList.size(),blueList.size(),blackout.size()));
        getTeamPropStrings();
        }
    }
    /**
     * Get the list of the Divisions with 'Blue' affinitity
     * @param ClubNameStr    The string value of the Clubname
     * @return List of Divisions from the specified Club with 'Blue' affinity
     */
    public List<String> getBlueList(ClubName clubName)      {return blueListMap.get(clubName);}
    /**
    * Get the list of the Divisions with 'Gold' affinitity
    * @param ClubNameStr    The string value of the Clubname
    * @return List of Divisions with 'Gold' affinity
    */
    public List<String> getGoldList(ClubName clubName)      {return goldListMap.get(clubName);}
    /**
    * Check if double booking is allowed on Tuesday
    * @param ClubNameStr    The string value of the Clubname
    * @return boolean
    */
    public boolean      isDoubleBookTue(ClubName clubName)  {return doubleBookTueMap.get(clubName);}
    /**
    * Check if double booking is allowed on Wednesday
    * @param ClubNameStr    The string value of the Clubname
    * @return boolean
    */
    public boolean      isDoubleBookWed(ClubName clubName)  {return doubleBookWedMap.get(clubName);}
    /**
    * Get the Tuesday match time 
    * @param ClubNameStr    The string value of the Clubname
    * @return boolean
    */
    public String       getTueMatchTime(ClubName clubName)  {return tueMatchTimeMap.get(clubName);}
    /**
    * Get the Wednesday match time 
    * @param ClubNameStr The string value of the Clubname
    * @return boolean
    */
    public String       getWedMatchTime(ClubName clubName)  {return wedMatchTimeMap.get(clubName);}
    /**
    * Get the Stack of Club Blackouts
    * @param ClubNameStr
    * @return Stack of Blackout Dates
    */
    public Stack<Date>  getBlackouts(ClubName clubName)     {return blackoutMap.get(clubName);}
    /**
     * Get the List of Teams that are in the specified Division. Each team is a
     * 4-tuple.
     * 
     * @param divName
     * @return The list of Team Tuples
     */
    public List<String> getTeamTupleList(String divName)  {return teamMap.get(divName);}
    /**
    * 
    * @param values
    * @param list
    * @param ClubNameStr 
    */
    private void addTokensToList(String values, List<String> list, ClubName clubName) {
        StringTokenizer stringTokenizer = new StringTokenizer(values,"    ");
        while (stringTokenizer.hasMoreElements()) {
            String token = stringTokenizer.nextToken();
            log.debug(String.format("ClubProperties.addValuesToList() %s %s", clubName, token));
            list.add(token);
        }
    }
    /**
     * 
     */
    private void getTeamPropStrings() {
        //Initialize the Array Lists for 'teamMap'
        for (Division.NAME divName : Division.NAME.values()) {
            List<String> teamList = new ArrayList<String>();
            teamMap.put(divName.toString(), teamList);
        }
        // The 'team' properties are located in the "'club'-TEAM" section of 'clubProperyFile'
        //for (String clubNameStr : new ClubName()) {
        for ( ClubName clubName : TennisScheduler.clubNameMgr) {
            String clubNameStr = clubName.getStr();
            Section  sect = new Section(clubNameStr+"-TEAMS",TennisScheduler.clubPropertyFileName);
            Enumeration e = sect.getProp().propertyNames();
            while (e.hasMoreElements()) {
                // The 'key' is the Division NAME
                String key = (String)e.nextElement();
                // Validate the division NAME
                Division.NAME divName = TennisScheduler.schedProperties.str2Name(key);
                if (divName == null) {
                    log.err(String.format("Invalid Division Name: '%s' Club: %s in %s",
                            key,clubNameStr,TennisScheduler.clubPropertyFileName));
                    System.exit(1);
                }
                // The 'value' is one or more unique team names
                String value = sect.getProp().getProperty(key);
                StringTokenizer stringTokenizer = new StringTokenizer(value,"    ");
                while (stringTokenizer.hasMoreElements()) {
                    String teamStr = stringTokenizer.nextToken();
                    // The team string is a colon seperated 4-tuple: TeamStr:TeamID:Group:Club
                    teamStr = teamStr.concat(String.format(":%s",clubNameStr.toString()));
                    teamMap.get(key).add(teamStr);
                }
            }
        }
   }
}
