package database.ObjT;

import java.util.ArrayList;

/**
 * Created by Dale on 5/1/2015.
 */
public class FallsOnPart extends ObjT{

    public FallsOnPart(int oID){
        super("Falls On Part", -1, oID, 1);
    }

    @Override
    public boolean fallsOnPart(){
        return true;
    }

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        return desc;
    }
}
