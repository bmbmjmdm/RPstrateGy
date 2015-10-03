package database.Objs.CObjs;

import java.util.ArrayList;

import Utilities.DamageType;
import database.Coord;
import database.ObjT.Damage_Taken_Modifier;
import database.ObjT.doesntTakeUpSpace;

/**
 * Created by Dale on 2/6/2015.
 */
public class Stick extends CObj {


    public Stick(ArrayList<Coord> co, int own){
        super(co, own, 3, 35, 5, 30, "Stick");
        image = "\u005f";
        types.add(new Damage_Taken_Modifier(id, 0, 1.5, DamageType.fire, -1));
        types.add(new Damage_Taken_Modifier(id, 0, 1.5, DamageType.sharp, -1));
        types.add(new doesntTakeUpSpace(id));
    }

}
