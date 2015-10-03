package database.Requirements;

import database.Actions.Action;
import database.Objs.PObjs.User;
import database.State;
import Managers.GameManager;

/**
 * Created by Dale on 4/7/2015.
 */
public class prepareReq extends Requirement {
    public int time;
    public boolean prepared = false;

    public prepareReq(int time){
        super("prepare");
        this.time = time;
    }

    public boolean canUse(User p){
       return true;
    }

    public int getTime(){
        if(prepared)
            return 0;
        else
            return time;
    }
}
