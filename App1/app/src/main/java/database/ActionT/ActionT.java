package database.ActionT;

import java.io.Serializable;

/**
 * Created by Dale on 12/29/2014.
 */
public abstract class ActionT implements Serializable {
    //name is often the same as the class name
    public String name;
    //severity is from 0 (none) to 100
    public int severity;

    public ActionT(String s, int s2){
        name = s;
        severity = s2;
    }

}
