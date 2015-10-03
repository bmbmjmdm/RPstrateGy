package database.Objs.CObjs;

import java.util.ArrayList;

import Utilities.DamageType;
import database.Coord;
import database.ObjT.Damage_Taken_Modifier;
import database.ObjT.doesntTakeUpSpace;

/**
 * Created by Dale on 2/6/2015.
 */
public class Bush extends CObj {


    public Bush(ArrayList<Coord> co, int own, double random){
        super(co, own, (int) (30 + 70*random), (int) (30 + 20*random), (int) (5 + 15*random), (int) (25 + 25*random), "Bush");
        image = "o";
        types.add(new Damage_Taken_Modifier(id, 0, 1.5, DamageType.fire, -1));
        types.add(new Damage_Taken_Modifier(id, 0, 1.5, DamageType.sharp, -1));
        types.add(new doesntTakeUpSpace(id));
    }

}
