package database.ObjT;

import java.util.ArrayList;

import database.Actions.Acts;
import database.Requirements.Requirement;

/**
 * Created by Dale on 4/5/2015.
 */
public class
        Preparing  extends ObjT{
    public Acts preparingAct;
    public ArrayList<Requirement> requirements;

    public Preparing(int oID, Acts act, ArrayList<Requirement> reqs){
        super("Preparing", -1, oID, 0);
        preparingAct = act;
        requirements = reqs;

    }

    @Override
    public boolean isPreparing(){
        return true;
    }



}
