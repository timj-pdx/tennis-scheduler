/*
 */
package tennisscheduler;

import java.util.StringTokenizer;
import java.util.Map;
import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
/**
 * Class for allocating the Team Classes. It will request the required
 * properties from the ClubProperties Class.
 * 
 * @author timj
 */
public class TeamMgr {
    
    private Logger                         log    = TennisScheduler.log;
    private Map<Division.NAME, List<Team>> divMap = new EnumMap<Division.NAME, List<Team>>(Division.NAME.class);
    private TeamProperties                 teamProperties = new TeamProperties();
    private Map<String,List<Date>>         groupBlackoutMap;
    /**
     * Instanciate the Team classes. The required property information is
     * located in the Club Properties file and has already been read by the
     * ClubProperties class.
     */
    TeamMgr() {
        Map<Integer,Integer> fullTeamList = new TreeMap<Integer,Integer>();
        groupBlackoutMap = TennisScheduler.schedProperties.getGroupBlackouts();
        for (Division.NAME divName : Division.NAME.values()) {
            List<Team> teamList = new ArrayList<Team>();
            divMap.put(divName, teamList);
            List<String> teamTupleList = TennisScheduler.clubProperties.getTeamTupleList(divName.toString());
            Iterator it = teamTupleList.iterator();
            while(it.hasNext()) {
                String teamTuple = (String)it.next();
                StringTokenizer stringTokenizer = new StringTokenizer(teamTuple,"    ");
                while (stringTokenizer.hasMoreElements()) {
                    // The team string is a colon seperated 4-tuple: TeamStr:TeamID:Group:Club
                    String teamStr = stringTokenizer.nextToken();
                    String array[] = teamStr.split(":");
                    if (array.length != 4) {
                        log.err(String.format("Expecting 'team:ID:group' got '%s' in %s",
                                teamStr,TennisScheduler.clubPropertyFileName));
                        System.exit(1);
                    }
                    String teamName  = array[0];
                    //int    teamID    = Integer.parseInt(array[1]);
                    int    teamID    = 0;
                    try {
                        teamID = Integer.parseInt(array[1]);
                    } catch (Exception ex) {
                        log.err(String.format("TeamMgr() Error in club.properties file: %s", ex.getMessage()));
                        log.err("Invalid TeamID, Expected an 'int'");
                        System.exit(1);
                    }
                    String groupName = array[2];
                    String clubName  = array[3];
                    log.debug(String.format("TeamMgr(): Adding %s to %s",teamName,divName.toString()));
                    Club club = TennisScheduler.clubMgr.getClub(clubName);
                    Team team = new Team(teamID,teamName, divName, club,teamProperties);
                    team.addAwayBlackoutDates(groupBlackoutMap.get(groupName));
                    team.addHomeBlackoutDates(groupBlackoutMap.get(groupName));
                    divMap.get(divName).add(team);
                    fullTeamList.put(teamID,null);
                }
            }
        }
        teamProperties.validateTeamList(fullTeamList);
        
    }
 
    /**
     * Given a Division Name, return the list of Teams for that Division
     * 
     * @param name The Division Name
     * @return The list of Teams in the Division
     */
   public List<Team> getList(Division.NAME name) {return divMap.get(name);}
    
}
