package database.ObjT;

import java.util.ArrayList;

/**
 * Created by Dale on 8/30/2015.
 */
public class Intimidated extends ObjT{
    double intimidation;
    String alertText;

    public Intimidated(int oID, double intimidating, String type, String alertT){
        super(type+" Intimidated", -1, oID, 1);
        intimidation = intimidating;
        alertText = alertT;

    }

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        desc.add("<font color=#E42217>Intimidated</font>");
        return desc;
    }

    @Override
    public double actionTimeMod(){return intimidation;}

    @Override
    public String getAlerText(){
        return alertText;
    }

    @Override
    public boolean isIntimidated(){return true;}
}
