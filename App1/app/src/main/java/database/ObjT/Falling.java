package database.ObjT;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

import Managers.GameManager;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Narration;
import database.Objs.Obj;

/**
 * Created by Dale on 4/13/2015.
 */
public class Falling extends ObjT {
    public int speed = 75;
    public int counter = 0;
    public HashSet<Obj> preferredPart = new HashSet<>();

    public Falling(int oID){
        super("Falling", -1, oID, -1);

        try {
            //narration
            Obj o = GameManager.getInstance().getState().getObjID(oID);
            HashSet<Obj> narrationInvolves = new HashSet<>();
            narrationInvolves.add(o);
            String text = o.name + " is falling!";
            new Narration(text, narrationInvolves, Stat.MISC);
        }
        catch(RemovedException e){
            Log.e("Falling objT created but userId not found in state!", "");
        }
    }

    @Override
    public int speed(){
        return speed;
    }

    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        desc.add("<font color=#E42217>"+name+"</font>");
        return desc;
    }


    @Override
    public boolean isMoving(){return true;}

    @Override
    public boolean isFalling(){return true;}

    @Override
    public boolean isInAir(){return true;}

    public int getResetCounters(){
        int count = counter;
        counter = 0;
        return count;
    }
}
