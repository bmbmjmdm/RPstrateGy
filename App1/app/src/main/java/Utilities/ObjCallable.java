package Utilities;

import java.io.Serializable;

import database.Objs.Obj;

/**
 * Created by Dale on 10/24/2015.
 */
public interface ObjCallable extends Serializable {

    public void call(Obj o);
}
