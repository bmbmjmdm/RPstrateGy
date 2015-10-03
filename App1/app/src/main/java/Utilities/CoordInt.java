package Utilities;

import java.io.Serializable;

import database.Coord;
import database.Objs.CObjs.CObj;

/**
 * Created by Dale on 5/10/2015.
 */
public class CoordInt implements Serializable {
    public int i;
    public Coord c;

    public CoordInt(int I, Coord C){
        i = I;
        c = C;
    }
}
