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
public class Encompassing extends ObjT {

    //ENCOMPASSING THINGS SHOULD NOT MOVE!!!!
    public Encompassing(int oID){
        super("Encompassing", -1, oID, -1);
    }

    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        return desc;
    }


    @Override
    public boolean encompassing(){return true;}
}
