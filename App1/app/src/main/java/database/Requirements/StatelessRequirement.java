package database.Requirements;

import database.Players.CharacterPlayer;

/**
 * Created by Dale on 1/4/2015.
 */
public abstract class StatelessRequirement extends Requirement{


    public StatelessRequirement(String s){
        super(s);;
    }
    public abstract boolean canUse(CharacterPlayer p);
}
