package database.Actions.SubActions;

import database.Objs.PObjs.User;

/**
 * Created by Dale on 7/3/2015.
 */
public class CustomSubAction extends SubAction {
    SubActCallable callMe;


    public CustomSubAction(SubActCallable callThis){
        callMe = callThis;
    }


    public void useContinue(User u, int speed){
        callMe.callCont(u, speed);
    }

    //if you want the ObjT to be added at useWhen, set eotTime to anything but null
    public void useStop(User u, int speed){
        callMe.callStop(u, speed);
    }
}
