package database.ObjT;

import java.util.ArrayList;

import Utilities.DamageType;
import Utilities.PerformanceTest;

/**
 * Created by Dale on 2/10/2015.
 */
public class Damage_Taken_Modifier extends ObjT {
    int dmgTakeC;
    double dmgTakeP;
    DamageType alter;

    //C's are for constant changes. 0 represents no change, positive represents increase, negative represents decrease
    //P's are for percent changes. <1 represents decrease, >1 represents increase, 1 represents no change (no negatives!)
    public Damage_Taken_Modifier(int oID, int dmgTakeC, double dmgTakeP, DamageType source, int duration){
        super("Damage Taken Modifier", duration, oID, 0);
        this.dmgTakeC = dmgTakeC;
        this.dmgTakeP = dmgTakeP;
        alter = source;

    }

    @Override
    public int dmgTakeC(DamageType s){
        if (s == alter || alter == DamageType.all)
            return dmgTakeC;
        else
            return 0;
    }

    @Override
    public double dmgTakeP(DamageType s){
        if (s == alter || alter == DamageType.all)
            return dmgTakeP;
        else
            return 1;
    }

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        if(dmgTakeC < 0 || dmgTakeP < 1){
            desc.add("<font color=#41A317>Resistant</font> &#160<font color=#000000>to  &#160</font>"+alter);
        }
        if (dmgTakeC > 0 || dmgTakeP > 1){
            desc.add("<font color=#E42217>Weak</font> &#160<font color=#000000>to  &#160</font>"+alter);
        }

        return desc;
    }

}
