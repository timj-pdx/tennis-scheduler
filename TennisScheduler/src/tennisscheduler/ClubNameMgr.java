
package tennisscheduler;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The Club Name Manager Class instanciates and manages the ClubName classes.
 * 
 * @author timj
 */
public class ClubNameMgr implements Iterable<ClubName> {
    
    private Map<ClubName,Integer> nameMap     = null;
    private Map<String,ClubName>  revNameMap  = null;
    private Map<String,Integer>   strMap      = TennisScheduler.schedProperties.getClubNameMap();
    private Logger                log         = TennisScheduler.log;
    
    ClubNameMgr() {
        Iterator it = strMap.keySet().iterator();
        while (it.hasNext()) {
            String str = (String)it.next();
            Integer i  = strMap.get(str);
        }
    }
    /**
     * Iterator method returns an Iterator for a list of ClubName
     * 
     * @return 
     */
    @Override public Iterator<ClubName> iterator() {
        if (nameMap == null) initNameMaps();
        Iterator<ClubName> iprof = this.nameMap.keySet().iterator();
        return iprof; 
    }
    /**
    * Get the Value for the 
    * @param s
    * @return 
    */
    public int      getValue(String s)    {return strMap.get(s);}
    /**
     * Return true if the specified string is a valid club name.
     * 
     * @param s The Club Name String
     * @return Return true if 's' is a valid club name
     */
    public boolean  containsKey(String s) {return strMap.containsKey(s);}
    /**
     * 
     * @param s
     * @return 
     */
    public ClubName getClubName(String s) {
        if (revNameMap == null) initNameMaps();
        if (!revNameMap.containsKey(s)) {
            log.err(String.format("ClubNameMgr().getClubName() : Invalid Club Name String: %s",s));
            System.exit(1);
        }
        return revNameMap.get(s);
    }
    /**
     * Initialise the Name Maps (nameMap & revNameMap). This cannot be called
     * from the constructor because it will be instances of ClubName which
     * requires that this class (ClubNameMgr) is fully instanciated, which is
     * not the case until the ClubMgr constructor returns.
     */
    private void initNameMaps() {
        nameMap    = new HashMap<ClubName,Integer>(strMap.size());
        revNameMap = new HashMap<String,ClubName>(strMap.size());
        Iterator it = strMap.keySet().iterator();
        while (it.hasNext()) {
            String   str      = (String)it.next();
            Integer  i        = strMap.get(str);
            ClubName clubName = new ClubName(str);
            nameMap.put(clubName, i);
            revNameMap.put(str,clubName);
        }
    }
}