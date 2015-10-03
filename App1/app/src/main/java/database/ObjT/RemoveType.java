package database.ObjT;

import Utilities.Constants;

/**
 * Created by Dale on 5/28/2015.
 */
public class RemoveType extends ObjT {
    public int typeID;
    int speed;

    public RemoveType(int spe, int id){
        super("Remove", -1, Constants.NO_OWNER_ID, 0);
        typeID = id;
        speed = spe;
    }

    @Override
    public int speed(){
        return speed;
    }
}
