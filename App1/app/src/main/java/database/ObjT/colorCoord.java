package database.ObjT;

import java.util.ArrayList;

/**
 * Created by Dale on 5/15/2015.
 */
public class colorCoord extends ObjT{
    int color;

    public colorCoord(int col, int oID){
        super("Color Coord", -1, oID, 1);
        color = col;
    }

    @Override
    public int getBGColor(){
        return color;
    }


    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        return desc;
    }
}
