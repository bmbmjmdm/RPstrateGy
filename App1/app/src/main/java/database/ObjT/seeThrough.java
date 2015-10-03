package database.ObjT;

import java.util.ArrayList;

/**
 * Created by Dale on 1/23/2015.
 */
public class seeThrough extends ObjT {

    public seeThrough(int oID){
        super("See Through", -1, oID, 1);
    }

    @Override
    public boolean seeThrough(){
        return true;
    }

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        return desc;
    }

}
