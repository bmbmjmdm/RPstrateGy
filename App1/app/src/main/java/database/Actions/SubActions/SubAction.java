package database.Actions.SubActions;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import Managers.GameManager;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Objs.PObjs.User;
import database.Requirements.Requirement;

/**
 * Created by Dale on 4/5/2015.
 */
public abstract class SubAction  implements Serializable {

    //used when the action is being continued
    public abstract void useContinue(User u, int speed);

    //used when the action is being stopped
    public abstract void useStop(User u, int speed);
}
