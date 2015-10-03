package Utilities;

import java.io.Serializable;
import java.util.ArrayList;

import database.Objs.CObjs.CObj;
import database.Objs.Obj;

/**
 * Created by Dale on 5/12/2015.
 */
public class ClimbObj implements Serializable {
    public CObj co;
    //guarenteed to have at least 1
    public ArrayList<Integer> climbPoints = new ArrayList<Integer>();

    public ClimbObj(CObj O, ArrayList<Integer> climbable){
        co = O;
        climbPoints = climbable;
    }
}
