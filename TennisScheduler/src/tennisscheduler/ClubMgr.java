
package tennisscheduler;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;

/**
 * This Class manages the Club Classes. This includes instantiation or all the
 * Club classes and running 'printAvailailable()' method on all of the Club
 * Classes.
 * 
 * @author timj
 * 
 */
public class ClubMgr {
    
    private Map<ClubName, Club>  cMap       = new HashMap<ClubName, Club>();
    private Logger               log        = TennisScheduler.log;
    
    ClubMgr() {
        
        //for (String key : new ClubName())
        //    cMap.put(key, new Club(new ClubName(key)));
        for (ClubName clubName : TennisScheduler.clubNameMgr)
            cMap.put(clubName, new Club(clubName));
    }
    /**
     * Return the Club class for the specified 'clubName'
     * 
     * @param n The Club Name
     * @return The Club Class
     */
    public Club getClub(String n) {
        ClubName clubName = TennisScheduler.clubNameMgr.getClubName(n);
        return cMap.get(clubName);
    }
    /**
     * Print available time-slots for all the clubs  for the specified 'season'
     * They will be printed to the specified 'pathName'.
     * @param season
     * @param pathName 
     */
    public void printAvailable(Match.SEASON season, String pathName) {
        FileOutputStream out;
        PrintStream ps = null;
        try {
            out = new FileOutputStream(pathName);
            ps = new PrintStream(out);
        }   catch (Exception e) {
            log.err("Error opening file");
        }
        for (ClubName clubName : TennisScheduler.clubNameMgr)
            cMap.get(clubName).printAvailable(season,ps);
        
    }
    @Override public String toString() {
        String str = String.format("%n%s%n%n", "########## Club Contents ##########");
        //for (String key : new ClubName())
        for (ClubName clubName : TennisScheduler.clubNameMgr)
            str   += String.format("%s%n", cMap.get(clubName));
        str += String.format("%n");
        return str;
    }
    
}
