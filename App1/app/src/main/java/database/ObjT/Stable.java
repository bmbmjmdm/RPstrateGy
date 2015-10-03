package database.ObjT;

import java.util.ArrayList;

/**
 * Created by Dale on 5/1/2015.
 */
public class Stable extends ObjT{
    //0-100
    //100 = doesn't take any damage when objects fall on it
    //0 = takes full damage when objects fall on it
    int stability;

    public Stable(int oID, int stab){
        super("Stable", -1, oID, 0);
        stability = stab;

    }


    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        return desc;
    }

    @Override
    public int stable(){return stability;}
}
