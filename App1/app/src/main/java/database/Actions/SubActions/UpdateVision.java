package database.Actions.SubActions;

import android.util.Log;

import Managers.GameManager;
import Managers.Logic.LogicCalc;
import Utilities.ArgCaller;
import Utilities.RemovedException;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.State;

/**
 * Created by Dale on 8/1/2015.
 */
public class UpdateVision extends SubAction{
    int objID;


    public UpdateVision(final int objID){
        this.objID = objID;
    }




    public void useContinue(User u, int speed){
        State s = GameManager.getInstance().getState();
        try{
            Obj o = s.getObjID(objID);
            new LogicCalc().updateVisionOf(o);
        }
        catch(RemovedException re){
            Log.e("Obj was removed before actionstep continue could update vision", ""+objID);
        }
    }


    public void useStop(User u, int speed){
        State s = GameManager.getInstance().getState();
        try{
            Obj o = s.getObjID(objID);
            new LogicCalc().updateVisionOf(o);
        }
        catch(RemovedException re){
            Log.e("Obj was removed before actionstep stop could update vision", ""+objID);
        }
    }


}



