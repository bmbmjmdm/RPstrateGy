package database.Actions.SubActions;

import java.io.Serializable;

import database.ObjT.ObjT;
import database.Objs.PObjs.User;

/**
 * Created by Dale on 7/3/2015.
 */
public interface ObjTModCallable extends Serializable {
    public void call(ObjT modThis, User u);
}
