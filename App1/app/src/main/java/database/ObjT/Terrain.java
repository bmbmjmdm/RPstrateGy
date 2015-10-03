package database.ObjT;

import java.util.ArrayList;

/**
 * Created by Dale on 1/23/2015.
 */
public class Terrain extends ObjT {

    public Terrain(int oID){
        super("Terrain", -1, oID, 1);
    }

    @Override
    public boolean isTerrain(){
        return true;
    }

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        return desc;
    }
}
