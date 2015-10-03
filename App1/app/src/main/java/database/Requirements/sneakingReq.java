package database.Requirements;

import java.util.ArrayList;

import database.ObjT.ObjT;
import database.ObjT.Stealthed;
import database.Objs.PObjs.User;

/**
 * Created by Dale on 4/7/2015.
 */
public class sneakingReq extends Requirement {
    boolean sneaking;

    public sneakingReq(boolean sneaking){
        super("Sneaking Req");
        this.sneaking = sneaking;
    }

    public boolean canUse(User p){
        ArrayList<ObjT> types = p.getTypePath();
        boolean sneak = false;
        for(ObjT ot: types){
            if(ot instanceof Stealthed)
                sneak = true;
        }

        return (sneak == sneaking);
    }
}
