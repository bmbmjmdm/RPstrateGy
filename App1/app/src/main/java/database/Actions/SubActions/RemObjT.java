package database.Actions.SubActions;

import android.util.Log;

import Managers.GameManager;
import Utilities.ArgCaller;
import Utilities.RemovedException;
import database.ObjT.ObjT;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.State;

/**
 * Created by Dale on 8/1/2015.
 */
public class RemObjT extends SubAction{

    ArgCaller<Integer> objTID;

    //this is highly preferred
    public RemObjT(final int objTID){

        this.objTID = new ArgCaller<Integer>(){
            public Integer call(){
                return objTID;
            }
        };
    }

    //as is this
    public RemObjT(ArgCaller<Integer> objTID){
        this.objTID = objTID;
    }




    public void useContinue(User u, int speed){
        State s = GameManager.getInstance().getState();
        try {
            ObjT ot = s.getObjT(objTID.call());
            try{
                Obj owner = s.getObjID(ot.belongsTo);
                owner.removeTypeSelf(ot.id);
            }

            catch(RemovedException e){
                s.remObjT(ot.id);
            }
        }
        catch(RemovedException e){
            Log.e("Type has been removed before RemObjT SubAction can remove it", "Not found in state");
        }
    }


    public void useStop(User u, int speed){
        State s = GameManager.getInstance().getState();
        try {
            ObjT ot = s.getObjT(objTID.call());
            try{
                Obj owner = s.getObjID(ot.belongsTo);
                owner.removeTypeSelf(ot.id);
            }

            catch(RemovedException e){
                s.remObjT(ot.id);
            }
        }
        catch(RemovedException e){
            Log.e("Type has been removed before RemObjT SubAction can remove it", "Not found in state");
        }
    }
}
