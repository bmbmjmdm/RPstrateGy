package database.Requirements;

import database.Objs.PObjs.User;

/**
 * Created by Dale on 1/4/2015.
 */
public class orReq extends Requirement {
    public Requirement reqA;
    public Requirement reqB;

    public orReq(Requirement a, Requirement b){
        super("or");
        reqA = a;
        reqB = b;
    }

    public boolean canUse(User p){
        if(reqA.canUse(p))
            return true;
        return reqB.canUse(p);
    }
}
