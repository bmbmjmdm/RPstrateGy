package database.ObjT;

import java.util.ArrayList;

/**
 * Created by Dale on 2/16/2015.
 */
public class doesntTakeUpSpace extends ObjT {

    public doesntTakeUpSpace(int oID){
        super("Doesn't take up space", -1, oID, 1);
    }

    @Override
    public boolean passable(){
        return true;
    }

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        return desc;
    }

}
