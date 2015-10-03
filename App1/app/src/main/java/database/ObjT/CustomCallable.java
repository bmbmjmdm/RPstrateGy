package database.ObjT;

import android.util.Log;

import Utilities.Callable;
import Utilities.Constants;


/**
 * Created by Dale on 5/29/2015.
 */
public class CustomCallable extends ObjT{
    Callable<Boolean> caller;
    int speed;

    public CustomCallable(int speed, Callable<Boolean> callMe){
        super("Custom", -1, Constants.NO_OWNER_ID, 0);
        caller =  callMe;
        this.speed = speed;
    }

    public boolean call(){
        try {
            return caller.call();
        }
        catch(Exception e){
            Log.e("Exception in calling callable in CustomCallable", e.getMessage());
            return false;
        }
    }

    @Override
    public int speed(){
        return speed;
    }
}
