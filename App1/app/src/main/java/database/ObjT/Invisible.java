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




public class Invisible extends ObjT {

    public Invisible(int oID){
        super("Invisible", -1, oID, 1);
    }

    @Override
    public boolean invisible(){
        return true;
    }

}
