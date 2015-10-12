
package tennisscheduler;

public class MatchWeight {
    private Weight home;
    private Weight away;
    private int    clubValue;
    
    MatchWeight(Weight h, Weight a) {
        home     = h;
        away      = a;
        clubValue = 0;
    }
    MatchWeight() {
        home = new Weight();
        home.setDirect();
        away = new Weight();
        away.setDirect();
        clubValue = 0;
    }
    
    public int getValue() {
        return home.getValue() + away.getValue() + clubValue;
    }
    @Override
    public String toString() {
        String str = "";
        if (clubValue != 0)
            str = String.format("C:%d",clubValue);
        return String.format("%8s%8s%4s",home,away,str);
    }
    public boolean isNone() {
        if (home.getValue() + away.getValue() == 0 && clubValue == 0)
            return true;
        else
            return false;
    }
    public boolean isDirect() {
        return (home.isDirect() || away.isDirect());
    }
    public void setClub()      {clubValue = Weight.clubValue;}
}
