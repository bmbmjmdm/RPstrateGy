package Utilities;

import java.io.Serializable;

import database.Coord;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;

/**
 * Created by Dale on 1/25/2015.
 */
public class CoordObj implements Serializable {
    public CObj o;
    public Coord c;

    public CoordObj(CObj O, Coord C){
        o = O;
        c = C;
    }

}
