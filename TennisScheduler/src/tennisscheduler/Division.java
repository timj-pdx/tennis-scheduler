
package tennisscheduler;

import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.util.Calendar;
import java.util.Stack;
import java.util.Queue;
import java.text.SimpleDateFormat;
import java.io.PrintStream;
/**
 * Given this list of teams it will create the list of
 * matches that need to be scheduled in the fail & spring season. The Division
 * class will schedule the matches.
 *
 * @author timj
 */
public class Division implements Comparable<Division> {
    
    public enum NAME {A, B, C, D, E, F, G, H, I, J, K}
    
    private NAME             name;
    private Match.WEEKDAY    matchDay;
    private int              matchCount;
    private List<Team>       teamList;
    private Stack<Match>     fallMatchStack    = new Stack<Match>();  // Fall matches that need to be scheduled
    private Stack<Match>     springMatchStack  = new Stack<Match>();  // Spring matches that need to be scheduled
    private SimpleDateFormat dateFormat        = new SimpleDateFormat("EEE MMM dd yyyy");
    private Logger           log               = TennisScheduler.log;
    private Stack<Match>     fallFailedStack   = new Stack<Match>();  // Fall matches that could not be scheduled
    private Stack<Match>     springFailedStack = new Stack<Match>();  // Spring matches that coutld not be scheduled
      
    Division(NAME d) {
        name = d;
        matchCount = 1;
        
        if (d.ordinal() < NAME.G.ordinal())
            matchDay = Match.WEEKDAY.Wed;
        else
            matchDay = Match.WEEKDAY.Tue;
        teamList = TennisScheduler.teamMgr.getList(this.name);
        createMatchStack();
        
    } 
    @Override
    public int compareTo(Division other) {
        return this.name.ordinal() - other.getName().ordinal();
        //return other.getName().ordinal() - this.name.ordinal();
    }
    /**
     * Generate the list of matches the need to be scheduled. Each Team will
     * play all the other teams in thier division once in Fall and once in
     * Spring. Split the home/away teams evenly between Fall & Spring 
     * Make sure that home games are evenly split between Fall & Spring
     */
    private void createMatchStack() {
        int len = teamList.size();
        for (int i=0; i<len; i++) {
            for (int j=i; j<len; j++ ) {
                if (j==i) continue;
                Team   ht   = teamList.get(i);
                //String hStr = ht.getName();
                Team   vt   = teamList.get(j);
                //String vStr = vt.getName();
                Match m     = new Match(i,j,ht,vt,matchCount);
                // Home 'i' Away 'j'
                if (matchCount%2 == 1) fallMatchStack.push(m);
                else                   springMatchStack.push(m);
                ht   = teamList.get(j);
                //hStr = ht.getName();
                vt   = teamList.get(i);
                //vStr = vt.getName();
                m     = new Match(j,i,ht,vt,matchCount++);
                // Away 'i' Home 'j'
                if (matchCount%2 == 1) fallMatchStack.push(m);
                else                   springMatchStack.push(m);
            }
        }
        log.debug(String.format("Division.createMatchStack() for Division %s %d Matches",name.toString(),fallMatchStack.size()));
        Collections.shuffle(fallMatchStack);
        Collections.shuffle(springMatchStack);
    }
    /**
     * Get the Division Name
     * 
     * @return The division Name
     */
    public NAME getName() {return this.name;}
    /**
     * Schedule the specified number of matches
     * 
     * @param num The number of games to schedule
     * @param season The season we are scheduling
     * @return The number of match scheduled
     */
    public int  schedule(int num, Match.SEASON season) {
        Stack<Match> matchStack;
        Stack<Match> failedStack;
        if (season == Match.SEASON.Fall) {
            matchStack  = fallMatchStack;
            failedStack = fallFailedStack;
        } else {
            matchStack  = springMatchStack;
            failedStack = springFailedStack;
        }
        Collections.shuffle(matchStack);
        int count = 0;
        while ( !matchStack.empty() ) {
            MatchWeight minWeight = new MatchWeight();
            ColorDate   minDate   = null;
            Match       mOld      = matchStack.pop();
            int         h         = mOld.getHomeID();
            int         v         = mOld.getAwayID();
            Team        hTeam     = teamList.get(h);
            Team        aTeam     = teamList.get(v);
            Club        club      = hTeam.getClub();
            log.info(String.format("Division.schedule() %s %s Match: %d Club: %s Teams: %s vrs %s",
                    season.toString(),name.toString(),matchStack.size(),club.getName(),
                    hTeam.getName(),aTeam.getName()));
            Queue<ColorDate> dQueue;
            boolean loHome = false;
            if (name == NAME.F &&
                  ((hTeam.getName().equals("LO-1") && aTeam.getName().equals("LO-2")) ||
                   (hTeam.getName().equals("LO-2") && aTeam.getName().equals("LO-1"))))
                loHome = true;
            if (loHome)
                dQueue  = club.getDateQueue(season,this.name,Match.WEEKDAY.Tue);
            else
                dQueue  = club.getDateQueue(season,this.name,this.matchDay);
            if (dQueue.isEmpty()) {
                log.err(String.format("%-6s Division %s %s Match %-9s vs %-9s could not be scheduled for %s (No available time slots)",
                    season.toString(), name.toString(), matchDay.toString(), hTeam.getName(),aTeam.getName(),club.getName()));
                failedStack.add(mOld);
                continue;
            }
            while (!dQueue.isEmpty()) {
                ColorDate date = dQueue.remove();
                //if (loHome) date = wedToTue(date);
                if (hTeam.blackout(date,Team.LOCATION.Home) || aTeam.blackout(date,Team.LOCATION.Away)) continue;
                Weight hWeight = hTeam.getWeight(date);
                Weight vWeight = aTeam.getWeight(date);
                log.debug(String.format("Division.schedule() Weight: H: %2d A: %2d",hWeight.getValue(),vWeight.getValue()));
                MatchWeight totalWeight = new MatchWeight(hWeight,vWeight);
                if (club.isWeighted(date)) totalWeight.setClub();
                if (totalWeight.getValue() < minWeight.getValue()) {
                    minWeight = totalWeight;
                    minDate   = date;
                    if (totalWeight.isNone()) break;
                }
            }
            if (minWeight.isDirect()) {
                log.err(String.format("%-6s Division %s %s Match %-9s vs %-9s could not use any of the dates provided by %s",
                    season.toString(), name.toString(), matchDay.toString(), hTeam.getName(),aTeam.getName(),club.getName()));
                failedStack.add(mOld);
                continue;
            }
            log.info(String.format("Division.schedule() Accepted: %s %-6s vs %-6s %s Weight %d",
                    this.name,hTeam.getName(),aTeam.getName(),dateFormat.format(minDate),minWeight.getValue()));
            if (loHome)
                club.acceptDate(season,Match.WEEKDAY.Tue,minDate);
            else
                club.acceptDate(season,matchDay,minDate);
            hTeam.accept(minDate);
            aTeam.accept(minDate);
            Match mNew = mOld;
            mNew.schedule(minDate,minWeight,club);
            TennisScheduler.schedule.add(season, name, mNew);
            count++;
            if (count >= num) break;
        }
        int size = failedStack.size();
        log.info(String.format("Division.schedule() Number of Failed Matches: %s %s %d",season,name.toString(),size));
        return count;
    }
    //public ColorDate wedToTue(ColorDate date) {
    //    System.out.println(date);
    //    Calendar cal = Calendar.getInstance();
    //    cal.setTime(date);
    //    cal.add(Calendar.DATE, -1);    // Move date backward 1 day
    //    ColorDate tmpDate = new ColorDate(cal.getTime(),date.getColor());
    //    return tmpDate;
    //}
    /**
     * Get number of teams in this division
     * 
     * @return The number of teams
     */
    public int  getNumOfTeams() {return teamList.size();}
    /**
     * Print the Failed Matches Stack for the specified season to the specified
     * PrintStream
     * 
     * @param season The season to print
     * @param ps The Stream to print to
     * @return The number of teams printed
     */
    public int  printFailedStack(Match.SEASON season, PrintStream ps) {
        Stack<Match> failedStack;
        if (season == Match.SEASON.Fall) failedStack = fallFailedStack;
        else                       failedStack = springFailedStack;
        int retVal = failedStack.size();
        if (retVal == 0) return 0;
        ps.format("##### Matches(%s) for Division %s #####%n", failedStack.size(), name.toString());
        Iterator it = failedStack.iterator();
        while ( it.hasNext() )
            ps.format("%s %s%n", name.toString(), it.next());
        return retVal;
    }
    /**
     * Print the Match Stack for the specified season to the specified
     * PrintStream
     * @param season The season to print
     * @param ps The Stream to print to
     */
    public void printMatchStack(Match.SEASON season, PrintStream ps) {
        // Print Match Stack (Unscheduled Matches)
        // Print the MatchStack for a Divisions for the specified Season.
        // Called from DivisionMgr. Use existing PrintStream.
        Stack<Match> matchStack;
        if (season == Match.SEASON.Fall) matchStack = fallMatchStack;
        else                       matchStack = springMatchStack;
        if (matchStack.size() == 0) return;
        ps.format("##### Matches(%s) for Division %s #####%n", matchStack.size(), name.toString());
        Iterator it = matchStack.iterator();
        while ( it.hasNext() )
            ps.format("%s %s%n", name.toString(), it.next());
    }
    /**
     * Print the Teams for for this division to the specified stream
     * 
     * @param ps The stream to print to
     * @return The number of teams printed
     */
    public int  printTeams(PrintStream ps) {
        int size = teamList.size();
        ps.format("##### Division %s Teams (%d) #####%n", name.toString(),size);
        Iterator it = teamList.iterator();
        while ( it.hasNext() )
            ps.format("%s %s%n", name.toString(), it.next());
        return size;
    }   
    @Override public String toString() {
        Iterator it = fallMatchStack.iterator();
        String str = "";
        //str += String.format("%3d Teams %3d Matches%n", value.size(), fallMatchStack.size());
        //str += String.format(" %-10s %-10s      %s%n","Home", "Visitor", "Date");
        while ( it.hasNext() )
            str += String.format("%s##%n", it.next());
        return str;
    }
// Private Methods
    
    
}
