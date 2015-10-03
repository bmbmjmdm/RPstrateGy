package database.Requirements;

import database.Objs.PObjs.User;

/**
 * Created by Dale on 1/4/2015.
 */
public class andReq extends Requirement {
    public Requirement reqA;
    public Requirement reqB;

    public andReq(Requirement a, Requirement b){
        super("and");
        reqA = a;
        reqB = b;
    }

    public boolean canUse(User p){
        if(reqA.canUse(p))
            return reqB.canUse(p);
        return false;

    }
}
