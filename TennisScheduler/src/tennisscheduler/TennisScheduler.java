
package tennisscheduler;
/**
 * 
 * Tennis Scheduler for Greater Portland City League Tennis
 * 
 * @author timj
 */
public class TennisScheduler {

    static String  clubPropertyFileName = "club.properties";
    // The SchedProperties then logger needs to be instanciated first
    // then everybody else depends on 'Logger'
    static SchedProperties schedProperties = new SchedProperties();
    static Logger          log             = new Logger("log","run.log");
    static ClubNameMgr     clubNameMgr     = new ClubNameMgr();
    static ClubProperties  clubProperties  = new ClubProperties();
    static Schedule        schedule        = new Schedule();
    static ClubMgr         clubMgr         = new ClubMgr();
    static TeamMgr         teamMgr         = new TeamMgr();
    
    public static void main(String[] args) {
        // Print out the available time slots before scheduling has begun
        clubMgr.printAvailable(Match.SEASON.Fall,  "log/avail-fall");
        clubMgr.printAvailable(Match.SEASON.Spring,"log/avail-spring");
        
        DivisionMgr dMgr = new DivisionMgr();
        dMgr.printTeams("teams");
        
        dMgr.printToSchedule(Match.SEASON.Fall,   "to-schedule-fall");
        dMgr.printToSchedule(Match.SEASON.Spring, "to-schedule-spring");
        dMgr.schedule(Match.SEASON.Fall);
        dMgr.schedule(Match.SEASON.Spring);
        // Print out the list of matches that could not be scheduled
        dMgr.printFailed(Match.SEASON.Fall,       "schedule/failed-fall");
        dMgr.printFailed(Match.SEASON.Spring,     "schedule/failed-spring");
        // Print out currently available time slots
        clubMgr.printAvailable(Match.SEASON.Fall,  "schedule/avail-fall");
        clubMgr.printAvailable(Match.SEASON.Spring,"schedule/avail-spring");
        
        schedule.printDivision(Match.SEASON.Fall,false);
        schedule.printDivision(Match.SEASON.Spring,false);
        schedule.printDivision(Match.SEASON.Fall,true);
        schedule.printDivision(Match.SEASON.Spring,true);
        // Print out team Schedules in Readble and 'csv' format
        schedule.printTeamSchedules(Match.SEASON.Fall);
        schedule.printTeamSchedules(Match.SEASON.Spring);
        schedule.printClubSchedules(Match.SEASON.Fall);
        schedule.printClubSchedules(Match.SEASON.Spring);
    }

}