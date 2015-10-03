package database.Objs.CObjs;

import java.util.ArrayList;

import database.Coord;
import database.ObjT.Climbable;
import database.ObjT.Hard;
import database.ObjT.Standable;

/**
 * Created by Dale on 2/6/2015.
 */
public class TreeTrunk extends CObj {

    public TreeTrunk(ArrayList<Coord> co, int own, int hei, int wid, int weigh, int heal, String pic){
        super(co, own, hei, wid, weigh, heal, "Tree Trunk");
        image = pic;
        if(wid < 40)
            types.add(new Climbable(id));
        types.add(new Standable(id));
        types.add(new Hard(id, 1));
    }
}
