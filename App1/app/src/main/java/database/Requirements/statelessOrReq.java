package database.Requirements;

import database.Objs.PObjs.User;
import database.Players.CharacterPlayer;

/**
 * Created by Dale on 1/4/2015.
 */
public class statelessOrReq extends StatelessRequirement{
    public StatelessRequirement reqA;
    public StatelessRequirement reqB;

    public statelessOrReq(StatelessRequirement a, StatelessRequirement b){
        super("or");
        reqA = a;
        reqB = b;
    }

    public boolean canUse(User p){
        if(reqA.canUse(p))
            return true;
        return reqB.canUse(p);
    }

    public boolean canUse(CharacterPlayer p){
        if(reqA.canUse(p))
            return true;
        return reqB.canUse(p);
    }
}
