package database.Actions.SubActions;

import java.io.Serializable;

import database.Objs.PObjs.User;

/**
 * Created by Dale on 7/3/2015.
 */
public interface SubActCallable extends Serializable {
    public void callCont(User u, int speed);
    public void callStop(User u, int speed);
}
