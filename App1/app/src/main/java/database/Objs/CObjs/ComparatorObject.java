package database.Objs.CObjs;

import java.util.ArrayList;

import Managers.GameManager;
import database.Coord;

/**
 * Created by Dale on 6/12/2015.
 */
public class ComparatorObject extends CObj {
    public boolean lessThan;

    //to be used when retrieving cobj from the state above or below a certain z coord
    public ComparatorObject(Coord c, boolean lessT){
        super(new ArrayList<Coord>(), -123456789, 0, 0, 0, 0, "ComparatorObject");
        loc.add(c);
        lessThan = lessT;
    }
}
