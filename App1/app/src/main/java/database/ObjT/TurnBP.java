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


//OWNER MUST BE BODYPART

public class TurnBP extends ObjT {
    public boolean vert;
    int speed;

    public TurnBP(int speed, int oID, boolean verticle){
        super("TurnBP", -1, oID, 0);
        vert = verticle;
        this.speed = speed;
    }

    @Override
    public int speed(){
        return speed;
    }

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        return desc;
    }

}
