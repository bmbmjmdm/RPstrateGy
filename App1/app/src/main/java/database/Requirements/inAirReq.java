package database.Requirements;

import java.util.ArrayList;

import Managers.GameManager;
import database.ObjT.ObjT;
import database.Objs.PObjs.User;
import database.State;

/**
 * Created by Dale on 4/7/2015.
 */
public class inAirReq extends Requirement {
    boolean inAir;

    public inAirReq(boolean inAir){
        super("In Air");
        this.inAir = inAir;
    }

    public boolean canUse(User p){
        ArrayList<ObjT> types = p.getTypePath();
        boolean isAirborn = false;
        for(ObjT ot: types){
            if(ot.isInAir())
                isAirborn = true;
        }

        return (isAirborn == inAir);
    }
}
