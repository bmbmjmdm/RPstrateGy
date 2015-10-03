package database.ObjT;

import java.util.ArrayList;

/**
 * Created by Dale on 2/10/2015.
 */
public class Stamina_Modifier extends ObjT {
    int restModC;
    int recModC;
    double restModP;
    double recModP;

    //C's are for constant changes. 0 represents no change, positive represents increase, negative represents decrease
    //P's are for percent changes. <1 represents decrease, >1 represents increase, 1 represents no change (no negatives!)
    public Stamina_Modifier(int oID, int restModC, int recModC, double restModP, double recModP, int life){
        super("Stamina Modifier", life, oID, 0);
        this.restModC = restModC;
        this.recModC = recModC;
        this.restModP = restModP;
        this.recModP = recModP;

    }

    @Override
    public int restModC(){
        return restModC;
    }

    @Override
    public int recModC(){
        return recModC;
    }

    @Override
    public double restModP(){
        return restModP;
    }

    @Override
    public double recModP(){
        return recModP;
    }


    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        if(restModC < 0 || restModP < 1){
            desc.add("<font color=#E42217>Restless</font>");
        }
        if(restModC > 0 || restModP > 1){
            desc.add("<font color=#41A317>Restful</font>");
        }
        if(recModC < 0 || recModP < 1){
            desc.add("<font color=#E42217>Tired</font>");
        }
        if(recModC > 0 || recModP > 1){
            desc.add("<font color=#41A317>Energetic</font>");
        }

        return desc;
    }
}
