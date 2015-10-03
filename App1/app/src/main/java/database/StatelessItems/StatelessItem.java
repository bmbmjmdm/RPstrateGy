package database.StatelessItems;

import java.util.ArrayList;

import database.Coord;
import database.Objs.Obj;
import database.Requirements.StatelessRequirement;

/**
 * Created by Dale on 1/15/2015.
 */
public abstract class StatelessItem {
    String name;
    public ArrayList<StatelessRequirement> statelessRequirements;
    public String desc;

    abstract public Obj getItem(ArrayList<Coord> co, int own);

}
