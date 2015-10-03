package database.Requirements;

import java.io.Serializable;

import database.Objs.PObjs.User;

/**
 * Created by Dale on 12/29/2014.
 */
public abstract class Requirement implements Serializable {
    //name is often the same as the class name
    public String name;

    public Requirement(String s){
        name = s;
    }

    public abstract boolean canUse(User p);
}
