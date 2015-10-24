package Utilities;

import java.io.Serializable;

/**
 * Created by Dale on 10/24/2015.
 */
public class NameObjCallable implements Serializable {
    public ObjCallable oc;
    public String name;

    public NameObjCallable(String nam, ObjCallable call){
        oc = call;
        name = nam;
    }


}
