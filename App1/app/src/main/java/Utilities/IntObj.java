package Utilities;

import java.io.Serializable;

import database.Objs.Obj;

/**
 * Created by Dale on 1/25/2015.
 */
public class IntObj implements Serializable {
    public Obj o;
    public int i;

    public IntObj(Obj O, int I){
        o = O;
        i = I;
    }

}
