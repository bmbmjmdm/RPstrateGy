package database.ObjT;

import java.util.ArrayList;

/**
 * Created by Dale on 1/23/2015.
 */
public class Squishable extends ObjT {

    public Squishable(int oID){
        super("Squishable", -1, oID, 1);
    }

    @Override
    public boolean squishable(){
        return true;
    }

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        return desc;
    }

}
