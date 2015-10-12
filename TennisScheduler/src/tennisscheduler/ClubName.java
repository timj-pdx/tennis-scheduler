
package tennisscheduler;

/**
 * A class for the Club Name. The 'value' is unique ID for the club that is
 * provided in the 'scheduler.properties' file. That ID is used as the hashCode.
 * 
 * 
 * @author timj
 */
public class ClubName {
    
    private int    value;
    private String str;
    
    ClubName(String s) {
        if (!TennisScheduler.clubNameMgr.containsKey(s)) {
            System.err.format("ClubName() Invalid key: %s%n", s);
            System.exit(1);
        }   
        value = TennisScheduler.clubNameMgr.getValue(s);
        str   = s;
    }
    /**
     * Get the Integer Value of the ClubName
     * 
     * @return The value of the ClubName
     */
    public int     getValue()           {return value;}
    /**
     * Get the String Value of the ClubName
     * @return The string value
     */
    public String  getStr()             {return str;}
    @Override public String  toString() {return str;}
    /**
     * The 'hashCode()' & 'equals()' methods. Since the 'value' is unique, just
     * use it for the hash code. The 'equals()' method is require when you
     * overrided the 'hashCode()' method.
     * 
     * @return 
     */
    @Override public int     hashCode() {return value;}
    @Override public boolean equals(Object obj) {
      if (obj == null)
          return false;  
      if (this.getClass() != obj.getClass())  
         return false;
      ClubName cn = (ClubName)obj;
      if (cn.getValue() != value)
          return false;
      return true;
    }
    
}
