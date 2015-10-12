
package tennisscheduler;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * This Class instantiates the Division Classes. It provides methods that
 * call methods (with same name) in each of the Division Classes.
 * 
 * @author timj
 */
public class DivisionMgr {
    
    private int            numMatches;
    private List<Division> sortedList;
    private List<Division> divList  = new ArrayList<Division>();
    
    DivisionMgr() {
        // Get the list of of Division (sorted by scheduling order)
        List<Division.NAME> list  = TennisScheduler.schedProperties.getDivisionOrder();
        Iterator            it    = list.iterator();
        numMatches  = TennisScheduler.schedProperties.getNumMatches();
        // Allocate the Division Classes in the order specified in the Ordered List (divList)
        while (it.hasNext()) {
            Division.NAME divName = (Division.NAME)it.next();
            Division      div     = new Division(divName);
            divList.add(div);
        }
        // Create a Sorted List of the Divisions
        sortedList = new ArrayList<Division>(divList);
        Collections.copy(sortedList,divList);
        Collections.sort(sortedList);
    }
    /**
     * Call the 'schedule()' method for each of the Divison Classes
     * Make these calls in the order specified with the 'DivisionOrder'
     * property.
     * 
     * @param s 
     */
    public void schedule(Match.SEASON s) {
        while ( true ) {
            int      count = 0;
            Iterator it    = divList.iterator();
            while (it.hasNext()) {
                Division d = (Division)it.next();
                int numOfMatches = numMatches;
                if (d.getNumOfTeams() > 8)
                    numOfMatches++;
                count += d.schedule(numOfMatches,s);
            }
            Collections.reverse(divList);
            if (count == 0) break;
        }
    }
    /**
     * Call the 'printTeams()' method for each of the Divison Classes
     * Make these calls in sorted order.
     * 
     * @param fileName The name of the file to print the team to
     */
    public void printTeams(String fileName) {
        // Print out a list of the teams in each Division to 'fileName'
        FileOutputStream out;
        PrintStream ps = null;
        try {
            out = new FileOutputStream("log/"+fileName);
            ps  = new PrintStream(out);
        }   catch (Exception e) {
            System.err.println ("Error opening file");
        }
        int numOfTeams = 0;
        Iterator it    = sortedList.iterator();
        while (it.hasNext()) {
            Division div = (Division)it.next();
            numOfTeams += div.printTeams(ps);
        }
        ps.format("##### Total Number of Teams %d #####%n",numOfTeams);
        ps.close();
    }
    /**
     * Call the 'printFailed()' method for each of the Divison Classes
     * Make these calls in sorted order. If no tests failed the (empty) file
     * will be deleted.
     * 
     * @param season The season to print for
     * @param pathName The name of the file to print to
     */
    public void printFailed(Match.SEASON season, String pathName) {
        // Print the list of matches that could not be scheduled to 'pathName'
        FileOutputStream out;
        PrintStream ps = null;
        try {
            out = new FileOutputStream(pathName);
            ps  = new PrintStream(out);
        }   catch (Exception e) {
            System.err.println ("Error opening file");
        }
        int lineCount = 0;
        Iterator it = sortedList.iterator();
        while (it.hasNext()) {
            Division div = (Division)it.next();
            lineCount += div.printFailedStack(season, ps);
        }
        System.out.format("%d %-6s Matches were not scheduled%n",lineCount,season);
        ps.close();
        // If all matches were scheduled delete the (empty) file
        if (lineCount == 0) {
            File file = new File(pathName);
            file.delete();
        }
        
    }
    /**
     * Print out the list of Matches that need to be scheduled in each Division.
     * Make these calls in division sorted order.
     * 
     * @param season The season to print for
     * @param fileName he name of the file to print to
     */
    public void printToSchedule(Match.SEASON season, String fileName) {
        FileOutputStream out;
        PrintStream ps = null;
        try {
            out = new FileOutputStream("log/"+fileName);
            ps  = new PrintStream(out);
        }   catch (Exception e) {
            System.err.println ("Error opening file");
        }
        Iterator it = sortedList.iterator();
        while (it.hasNext()) {
            Division div = (Division)it.next();
            div.printMatchStack(season, ps);
        }
        ps.close();
    }
    @Override public String toString() {
        System.out.println("##### Division Manager #####");
        String str = "";
        Iterator it = sortedList.iterator();
        while (it.hasNext()) {
            Division div = (Division)it.next();
            str += String.format("%s", div);
        }
        return str;
    }

}
