package database.Actions.SubActions;

import Managers.GameManager;
import Utilities.ArgCaller;
import database.Objs.PObjs.User;
import database.State;

/**
 * Created by Dale on 8/1/2015.
 */
public class AddEOT extends SubAction{

    ArgCaller<Integer> objTID;
    Integer time = null;

    //this is highly preferred
    public AddEOT(final int objTID){

        this.objTID = new ArgCaller<Integer>(){
            public Integer call(){
                return objTID;
            }
        };
    }

    //as is this
    public AddEOT(ArgCaller<Integer> objTID){
        this.objTID = objTID;
    }


    //this is for special cases
    public AddEOT(int objTID, int time){
        this(objTID);
        this.time = time;
    }

    //as is this
    public AddEOT(ArgCaller<Integer> objTID, int time){
        this(objTID);
        this.time = time;
    }







    public void useContinue(User u, int speed){
        State s = GameManager.getInstance().getState();
        if(time == null)
            s.addEOTObjT(objTID.call(), s.getTime());
        else
            s.addEOTObjT(objTID.call(), time);
    }


    public void useStop(User u, int speed){
        State s = GameManager.getInstance().getState();
        if(time == null)
            s.addEOTObjT(objTID.call(), s.getTime());
        else
            s.addEOTObjT(objTID.call(), time);
    }


}



