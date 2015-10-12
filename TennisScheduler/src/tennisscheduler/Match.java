
package tennisscheduler;

import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class Match implements Comparable<Match> {
    
    public enum WEEKDAY {Tue,Wed};
    public enum SEASON  {Fall,Spring};
    
    private int              num;
    private Club             club;
    private String           matchTime;
    private Logger           log         = TennisScheduler.log;
    private SimpleDateFormat dateFormat  = new SimpleDateFormat("EEE MMM dd yyyy");
    private int              homeID      = -1;   // The 'id' of the homeID team
    private Team             homeTeam    = null;
    private int              awayID      = -1;
    private Team             awayTeam    = null;
    private Date             date        = null;
    private MatchWeight      weight      = null;
    
    Match(int h, int a, Team ht, Team at, int n) {
        homeID      = h;
        homeTeam    = ht;
        awayID      = a;
        awayTeam    = at;
        num         = n;
        this.weight = null;
    }
    /**
     * CompareTo function to support sorting
     * 
     * @param other The Match to compare T
     * @return 
     */
    @Override
    public int compareTo(Match other) {return this.date.compareTo(other.getDate());}
    /**
     * Change a Match from being unscheduled to scheduled
     * 
     * @param d Date of the Match
     * @param c Club that will be hosting the Match
     */
    public void   schedule(Date d, MatchWeight minWeight, Club c) {
        this.club   = c;
        this.weight = minWeight;
        this.date   = d;
        WEEKDAY day = WEEKDAY.Tue; // Initialize, to keep the compiler quiet
        // We need to know to 'day' so we can get the matchTime from the Club.
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        switch ( cal.get(Calendar.DAY_OF_WEEK) ) {
            case 3: day = WEEKDAY.Tue; break;
            case 4: day = WEEKDAY.Wed; break;
            default:
                log.err(String.format("Match.schedule() : Date '%s' is invalid", dateFormat.format(date)));
                System.exit(1);
        }
        matchTime = club.getMatchTime(day);
    }
    /**
     * Get the Match Time
     * 
     * @return The Match Time
     */
    public String getMatchTime() {return matchTime;}
    /**
     * Get the Club that is hosting this Match
     * 
     * @return Club that is hosting the Match
     */
    public Club   getClub()      {return club;}
    /**
     * Get the Date for this match. If the match has not been scheduled the Date
     * is null.
     * 
     * @return The Date
     */
    public Date   getDate()      {return date;}
    /**
     * Get the ID for the home team.
     * 
     * @return The ID for the home team
     */
    public int    getHomeID()    {return homeID;}
    /**
     * Get the home team.
     * 
     * @return The home team
     */
    public Team   getHomeTeam()  {return homeTeam;}
    /**
     * Get the ID for the away team.
     * 
     * @return The ID for the away team
     */
    public int    getAwayID()    {return awayID;}
    /**
     * Get the away team.
     * @return The away Team
     */
    public Team   getAwayTeam()  {return awayTeam;}
    /**
     * Get the weight assigned to the match
     * 
     * @return 
     */
    public MatchWeight getWeight()    {return this.weight;}
    @Override
    public String toString() {
        String str = "";
        // Date is null when Match is not scheduled
        if (date == null)
            str = String.format("%-9s  %-9s  Unscheduled",homeTeam.getName(),awayTeam.getName());
        else
            str = String.format("%-9s  %-9s  %s",homeTeam.getName(),awayTeam.getName(),dateFormat.format(date));
        return str;
    }
    
}
