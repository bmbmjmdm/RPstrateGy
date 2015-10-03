package database.ObjT;

import android.util.Log;

import java.util.ArrayList;

import Managers.GameManager;
import Utilities.RemovedException;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.State;

/**
 * Created by Dale on 1/23/2015.
 */



public class Climbable extends ObjT {

    public Climbable(int oID){
        super("Can Climb Sides", -1, oID, 1);
    }

    @Override
    public boolean climable(){
        return true;
    }



    //TODO only call if the obj in question is a cobj!
    @Override
    public ArrayList<String> getDescription(){
        State s = GameManager.getInstance().getState();
        ArrayList<String> desc = new ArrayList<String>();
        try {
            User u = (User)s.getObjID(GameManager.getInstance().getTimeline().turnObjectID);
            CObj o = (CObj) s.getObjID(tempBelongsTo);
            if(u.getFallingWidth() <= (75- o.getWidth()))
                desc.add("<font color=#000000>" + name + "</font>");
        }
        catch(RemovedException e){
            Log.e("Something removed while calling getDescription() in Climbable", "Either user or belongsTo");
        }
        return desc;
    }

}
