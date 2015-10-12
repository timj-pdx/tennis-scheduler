/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tennisscheduler;

public class Weight {
    // Direct schedule conflict. A match with a total weight greater than
    // 'Direct' will not be scheduled.
    final static public  int directValue = 25;
    // The club allows double booking and a match has already scheduled for
    // this date at this club
    final static public  int clubValue   =  1;
    final static private int oneValue    =  5; // Team is scheduled +/- 1 week
    final static private int twoValue    =  1; // Team is scheduled +/- 2 weeks
    final static private int colorValue  =  2; // The 'color' is wrong for this team/date
    ////////////////////////////////////////////////////////////////////////////
    private boolean oneBefore;
    private boolean oneAfter;
    private boolean twoBefore;
    private boolean twoAfter;
    private boolean color;
    private boolean direct;
    
    Weight() {
        oneBefore = oneAfter = twoBefore = twoAfter = color = direct = false;
    }
    
    public void setDirect()    {direct    = true;}
    public void setOneBefore() {oneBefore = true;}
    public void setOneAfter()  {oneAfter  = true;}
    public void setTwoBefore() {twoBefore = true;}
    public void setTwoAfter()  {twoAfter  = true;}
    public void setColor()     {color     = true;}
    public void clear()        {
        oneBefore = oneAfter = twoBefore = twoAfter = color = direct = false;
    }
    public boolean isDirect()  {return this.direct;}
    public int     getValue()  {
        int weight = 0;
        if (direct) return directValue;
        if (oneBefore) weight += oneValue;
        if (oneAfter)  weight += oneValue;
        if (twoBefore) weight += twoValue;
        if (twoAfter)  weight += twoValue;
        if (color)     weight += colorValue;
        return weight;
    }
    @Override
    public String  toString()  {
        String str = "";
        if (direct) {return String.format("D:%d", this.getValue());}
        if (oneBefore || oneAfter) {
            str += "1";
            if (oneBefore) str += "B";
            if (oneAfter)  str += "A";
        }
        if (twoBefore || twoAfter) {
            str += "2";
            if (twoBefore) str += "B";
            if (twoAfter)  str += "A";
        }
        if (color) str += "c";
        return String.format("%s:%d", str, this.getValue());
    }
}
