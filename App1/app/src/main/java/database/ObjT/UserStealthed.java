package database.ObjT;

/**
 * Created by Dale on 9/15/2015.
 */

//TODO this assumes the objT belongs to a user
//TODO the EOT logic for this ObjT pays focus
public class UserStealthed  extends Stealthed{
    public int baseFocus = 2;
    public double baseSpeedReduce = 0.8;

    public UserStealthed(int oID, double width, double height){
        super(oID, width, height);

    }


    public double actionTimeMod(){return baseSpeedReduce;}

}
