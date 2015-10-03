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


public class Hard extends ObjT {
    double hard;

    public Hard(int oID, double hardness){
        super("Hard", -1, oID, 0);
        hard = hardness;
    }

    @Override
    public double hardness(){
        return hard;
    }

}
