package database.Actions.SubActions;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import Managers.GameManager;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Narration;
import database.ObjT.ObjT;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.State;

/**
 * Created by Dale on 7/3/2015.
 */
public class AddNarration extends SubAction{

    HashSet<Integer> involved;
    String text;
    Stat stat;

    public AddNarration(HashSet<Integer> involved, String text, Stat stat){
        this.involved = involved;
        this.text = text;
        this.stat = stat;
    }



    public void useContinue(User u, int speed){
        try {
            HashSet<Obj> narrationInvolves = new HashSet<>();
            State s = GameManager.getInstance().getState();

            for(Integer i : involved){
                narrationInvolves.add(s.getObjID(i));
            }

            new Narration(text, narrationInvolves, stat);
        }
        catch(RemovedException e){
            Log.e("AddNarration>useContinue", "one or more involved obj were removed from state when narration was being made");
        }
    }


    public void useStop(User u, int speed){
        try {
            HashSet<Obj> narrationInvolves = new HashSet<>();
            State s = GameManager.getInstance().getState();

            for(Integer i : involved){
                narrationInvolves.add(s.getObjID(i));
            }

            new Narration(text, narrationInvolves, stat);
        }
        catch(RemovedException e){
            Log.e("AddNarration>useContinue", "one or more involved obj were removed from state when narration was being made");
        }
    }
}
