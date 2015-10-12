
package tennisscheduler;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.EnumMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;

/**
 * This Class is the destination for the schedule as it is being generated. It
 * also contains the 'print' methods for the schedule in numerous formats.
 * 
 * @author timj
 */
public class Schedule {
    
    private Map<Division.NAME, List<Match>> fallSchedMap
            = new EnumMap<Division.NAME, List<Match>>(Division.NAME.class);
    private Map<Division.NAME, List<Match>> springSchedMap
            = new EnumMap<Division.NAME, List<Match>>(Division.NAME.class);
    
    private Logger           log        = TennisScheduler.log;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd");
    private String           schedDir   = "schedule";
    private int sepCount;
    private int octCount;
    private int novCount;
    private int decCount;
    private int janCount;
    private int febCount;
    private int marCount;
    private int aprCount;
    private int mayCount;
    
    /**
     * The Constructor will remove the old schedule directory and create a
     * new, empty one to populate.
     */
    Schedule () {
        // Remove the schedule directory
        File dir = new File(schedDir);
        if (dir.exists()) {
            if (dir.isDirectory())
                for (File f : dir.listFiles())
                    f.delete();
            dir.delete();
        }
        dir.mkdir();
        // Allocate empty schedule lists
        for (Division.NAME d : Division.NAME.values()) {
            List<Match>  sList  = new ArrayList<Match>();
            fallSchedMap.put(d,sList);
            sList  = new ArrayList<Match>();
            springSchedMap.put(d,sList);
        }
    }
    /**
     * Add a match to the schedule
     * 
     * @param season The Season that is being scheduled
     * @param division The Division the match is for
     * @param match The Match that is being scheduled
     */
    public void add(Match.SEASON season, Division.NAME division, Match match) {
        List<Match> s;
        if (season.equals(Match.SEASON.Fall))
            s = fallSchedMap.get(division);
        else
            s = springSchedMap.get(division);
        s.add(match);
    }
    /**
     * Print the schedule, by Division.
     * 
     * @param season The Season to print
     */
    public void printDivision(Match.SEASON season, boolean csv) {
        String fileName = String.format("%s/%s", schedDir,season.toString().toLowerCase());
        if (csv) fileName = fileName.concat(".csv");
        FileOutputStream out;
        PrintStream      ps   = null;
        try {
            out = new FileOutputStream(fileName);
            ps  = new PrintStream(out);
        }   catch (Exception e) {
            log.err("Error opening file");
        }
        List<Match> mList;
        for (Division.NAME div : Division.NAME.values())
            if (csv) printDivCSV(div,season,ps);
            else     printDivHuman(div,season,ps);
    }
    private void printDivCSV(Division.NAME div, Match.SEASON season, PrintStream ps) {
        List<Match> mList;
        if (season.equals(Match.SEASON.Fall))
            mList = fallSchedMap.get(div);
        else
            mList = springSchedMap.get(div);
        Collections.sort(mList);
        Iterator it = mList.iterator();
        while ( it.hasNext() ) {
            Match match = (Match)it.next();
            Team homeTeam = match.getHomeTeam();
            Team awayTeam = match.getAwayTeam();
            SimpleDateFormat csvDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            ps.format("%s,%d,%d,%d,%s,%s%n", div.name(), homeTeam.getID(),
                awayTeam.getID(), match.getClub().getID(),
                csvDateFormat.format(match.getDate()),match.getMatchTime());
        }
    }
    private void printDivHuman(Division.NAME div, Match.SEASON season, PrintStream ps) {
        List<Match> mList;
        if (season.equals(Match.SEASON.Fall))
            mList = fallSchedMap.get(div);
        else
            mList = springSchedMap.get(div);
        Collections.sort(mList);
        ps.format("##### Division %s %2d Games   ##%n",div.toString(),mList.size());
        Iterator it = mList.iterator();
        while ( it.hasNext() ) {
            Match match = (Match)it.next();
            ps.format("%s %-9s %-9s %-6s %s%n", div.toString(),
                    match.getHomeTeam().getName(),match.getAwayTeam().getName(),
                    match.getClub().getName(),dateFormat.format(match.getDate()));
        }
    }
    /**
     * Print The Team schedules for the specified season. Wrie to a file in the
     * 'schedule' directory (schedule/teasm-'season'). If 'csv' is true write
     * the file in CSV format and append a '.csv' suffix to the file name.
     * 
     * @param season The Season to print
     * @param csv The boolean to specify CSF format
     */
    public void printTeamSchedules(Match.SEASON season) {
        String fileName = String.format("%s/%s-%s", schedDir,"teams",season.toString().toLowerCase());
        FileOutputStream out;
        PrintStream      ps   = null;
        try {
            out = new FileOutputStream(fileName);
            ps  = new PrintStream(out);
        }   catch (Exception e) {
            log.err("Error opening file");
        }
        List<Match> matchList;
        for (Division.NAME div : Division.NAME.values()) {
            if (season.equals(Match.SEASON.Fall)) matchList = fallSchedMap.get(div);
            else                                  matchList = springSchedMap.get(div);
            Collections.sort(matchList);
            List<Team> teamList = TennisScheduler.teamMgr.getList(div);
            Iterator it = teamList.iterator();
            while (it.hasNext()) {
                Team team = (Team)it.next();
                Date prevDate = null;
                ps.format("##### %s %s #####%n",team.getName(), div.toString());
                initMonthSummary();
                Iterator it1 = matchList.iterator();
                while ( it1.hasNext() ) {
                    Match match = (Match)it1.next();
                    Team homeTeam = match.getHomeTeam();
                    Team awayTeam = match.getAwayTeam();
                    if (   team.getName().equals(homeTeam.getName()) ||
                       team.getName().equals(awayTeam.getName())) {
                        Date date = match.getDate();
                        String str = "";
                        if (consecutiveWeeks(prevDate, date)) str = "Consecutive";
                        ps.format("%-9s %-9s %-5s %s %-12s (%20s)%n", match.getHomeTeam().getName(),
                            match.getAwayTeam().getName(),match.getClub().getName(),
                            dateFormat.format(date), str, match.getWeight());
                        prevDate = date;
                        countMonths(season, date);
                    }
                }
                printMonthSummary(season,ps);
            }
        }
    }
    /**
     * Print the Club schedules for the specified season to a file.
     * 
     * @param season The Season to print
     */
    public void printClubSchedules(Match.SEASON season) {
        String fileName = String.format("%s/%s-%s", schedDir,"clubs",season.toString().toLowerCase());
        FileOutputStream out;
        PrintStream      ps   = null;
        try {
            out = new FileOutputStream(fileName);
            ps  = new PrintStream(out);
        }   catch (Exception e) {
            log.err("Error opening file");
        }
        List<Match> matchList;
        List<Match> clubMatchList = new ArrayList<Match>();
        //for (String clubName : new ClubName()) {
        for ( ClubName clubName : TennisScheduler.clubNameMgr) {
            ps.format("#### Club Schedule for %s%n", clubName);
            for (Division.NAME div : Division.NAME.values()) {
                if (season.equals(Match.SEASON.Fall))
                    matchList = fallSchedMap.get(div);
                else
                    matchList = springSchedMap.get(div);
                Iterator it = matchList.iterator();
                while ( it.hasNext() ) {
                    Match match = (Match)it.next();
                    //if (clubName.equals(match.getHomeTeam().getName())) {
                    if (clubName.getStr().equals(match.getHomeTeam().getName())) {
                        clubMatchList.add(match);
                    }
                }
            }
            Collections.sort(clubMatchList);
            Iterator it = clubMatchList.iterator();
            while ( it.hasNext() ) {
                Match match = (Match)it.next();
                ps.format("%-9s %-9s %s %s%n", match.getHomeTeam().getName(),
                        match.getAwayTeam().getName(),dateFormat.format(match.getDate()),match.getMatchTime());
            }
            clubMatchList.clear();
        }
    }
    /**
     * Initialize all the month counts to zero
     */
    private void initMonthSummary() {
        sepCount = octCount = novCount = decCount = janCount = 0;
        febCount = marCount = aprCount = mayCount = 0;
    }
    /**
     * Increment the month count for the specified date
     * 
     * @param season
     * @param date 
     */
    private void countMonths(Match.SEASON season, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String dStr = cal.toString();
        if (season.equals(Match.SEASON.Fall)) {
            switch(cal.get(Calendar.MONTH)) {
                case Calendar.SEPTEMBER: sepCount++; break;
                case Calendar.OCTOBER:   octCount++; break;
                case Calendar.NOVEMBER:  novCount++; break;
                case Calendar.DECEMBER:  decCount++; break;
                case Calendar.JANUARY:   janCount++; break;
                default:
                    log.err("Schedule.coutMonths(): Internal Error");
                    System.exit(1);
            }
        } else {
            switch(cal.get(Calendar.MONTH)) {
                case Calendar.JANUARY:  janCount++; break;
                case Calendar.FEBRUARY: febCount++; break;
                case Calendar.MARCH:    marCount++; break;
                case Calendar.APRIL:    aprCount++; break;
                case Calendar.MAY:      mayCount++; break;
                default:
                    log.err("Schedule.coutMonths(): Internal Error");
                    System.exit(1);
            }
        }
    }
    /**
     * Print the month summary for the specified season to the specified
     * PrintStream
     * 
     * @param season
     * @param ps 
     */
    private void printMonthSummary(Match.SEASON season, PrintStream ps) {
        int max = 2;
        if (season.equals(Match.SEASON.Fall)) {
            ps.format("Sep: %d Oct: %d Nov: %d Dec: %d Jan: %d  ",
                sepCount, octCount, novCount, decCount, janCount);
            if ( sepCount > max || octCount > 3 || novCount > max || decCount > max || janCount > max ||
                 sepCount == 0  || octCount == 0  || novCount == 0  || decCount == 0 )
                ps.println(" Check");
            else
                ps.println();
        } else {
            ps.format("Jan: %d Feb: %d Mar: %d Apr: %d May: %d  ",
                janCount, febCount, marCount, aprCount, mayCount);
            if ( janCount > max || febCount > max || marCount > max || aprCount > max || mayCount > max ||
                    febCount == 0 || marCount == 0 || aprCount == 0 || mayCount == 0 )
                ps.println(" Check");
            else
                ps.println();
        }
        ps.println();
    }
    /**
     * If 'curDate' is less than 8 days older than 'prevDate' return 'true'.
     * Else return 'false'.
     * 
     * @param prevDate
     * @param curDate
     * @return 
     */
    private static  boolean consecutiveWeeks(Date prevDate, Date curDate) {
        if (prevDate == null) return false;
        Calendar cal  = Calendar.getInstance();
        cal.setTime(curDate);
        cal.add(Calendar.DATE, -8); // Set Date backward
        return prevDate.compareTo(cal.getTime()) >= 0;
    }
    
    @Override
    public String toString() {
        String str = String.format("%n##### Schedule #####%n");
        //SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd yyyy");
        for (Division.NAME div : Division.NAME.values()) {
            str += String.format("## Division %s ##%n", div.toString());
            List<Match> mList = fallSchedMap.get(div);
            Iterator it = mList.iterator();
            while ( it.hasNext() )
                str += String.format("%s%n", it.next());
        }
        return str;
    }
    
}
