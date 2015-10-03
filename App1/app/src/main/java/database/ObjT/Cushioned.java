package database.ObjT;

import java.util.ArrayList;

/**
 * Created by Dale on 4/13/2015.
 */
public class Cushioned extends ObjT {
    //0-100
    //100 = reduces fall damage FROM the object by 100%
    int cushion;

    public Cushioned(int oID, int cush){
        super("Cushioned", -1, oID, 0);
        cushion = cush;

    }


    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        return desc;
    }

    @Override
    public int cushion(){return cushion;}

}
