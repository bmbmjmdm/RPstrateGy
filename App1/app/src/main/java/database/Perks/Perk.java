package database.Perks;

import java.io.Serializable;
import java.util.ArrayList;

import database.Players.CharacterPlayer;
import database.Requirements.StatelessRequirement;

/**
 * Created by Dale on 1/4/2015.
 */
public abstract class Perk implements Serializable {
    public ArrayList<StatelessRequirement> requirements;
    public String name;
    public String description;
    public int cost;
    public String requirementsDes;

    public Perk(String nam, String desc, String reqDes, int cos){
        requirements = new ArrayList<StatelessRequirement>();
        name=nam;
        description=desc;
        requirementsDes = reqDes;
        cost = cos;
    }

    public abstract void statelessApply(CharacterPlayer c);

    public abstract void statelessUnApply(CharacterPlayer c);

    public abstract void statefullApply(int ObjId);

    public boolean canUse(CharacterPlayer p){
        for(StatelessRequirement sr : requirements){
            if(!sr.canUse(p))
                return false;
        }
        return true;
    }

}
