package database.Requirements;

import java.util.ArrayList;

import database.ObjT.ObjT;
import database.Objs.PObjs.User;

/**
 * Created by Dale on 4/7/2015.
 */
public class fallingReq extends Requirement {
    boolean falling;

    public fallingReq(boolean falling){
        super("Falling");
        this.falling = falling;
    }

    public boolean canUse(User p){
        ArrayList<ObjT> types = p.getTypePath();
        boolean isFalling = false;
        for(ObjT ot: types){
            if(ot.isFalling())
                isFalling = true;
        }

        return (isFalling == falling);
    }
}
