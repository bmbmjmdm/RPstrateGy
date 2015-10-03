package database.ObjT;

import android.util.Log;

import java.util.ArrayList;

import Managers.GameManager;
import Utilities.RemovedException;
import database.Objs.CObjs.CObj;
import database.Objs.PObjs.User;
import database.State;

/**
 * Created by Dale on 1/23/2015.
 */


//TODO ONLY USE ON CObj's (see getDescription for why)


public class Standable extends ObjT {

    public Standable(int oID){
        super("Can Stand On", -1, oID, 1);
    }

    @Override
    public boolean standable(){
        return true;
    }

    @Override
    public ArrayList<String> getDescription(){
        State s = GameManager.getInstance().getState();
        ArrayList<String> desc = new ArrayList<String>();
        try {
            User u = (User)s.getObjID(GameManager.getInstance().getTimeline().turnObjectID);
            CObj o = (CObj) s.getObjID(tempBelongsTo);
            if(u.getFallingWidth() <= o.getWidth()*1.5)
                desc.add("<font color=#000000>" + name + "</font>");
        }
        catch(RemovedException e){
            Log.e("Something removed while calling getDescription() in Standable", "Either user or belongsTo");
        }
        return desc;
    }

}
