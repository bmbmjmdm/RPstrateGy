package database.Objs.CObjs;

import java.util.ArrayList;

import Utilities.DamageType;
import database.Coord;
import database.ObjT.Damage_Taken_Modifier;
import database.ObjT.Hard;
import database.ObjT.Stable;
import database.ObjT.doesntTakeUpSpace;
import database.ObjT.Standable;

/**
 * Created by Dale on 2/6/2015.
 */
public class Rock extends CObj {

    //ROCK SHOULD NOT BE CREATED EXCEPT FOR START OF GAME. OTHERWISE THIS VIOLATES RANDOM CONTRACT

    public Rock(ArrayList<Coord> co, int own, int height, int width, int weight, int health){
        super(co, own, height, Math.min(50, width), weight, health, height<40? "Small Rocks":"Large Rock");
        if(height<40) {
            image = " \u05C5\u032F ";
            if(height<15)
                types.add(new doesntTakeUpSpace(id));

            types.add(new Stable(id, 70));
        }
        else {
            image = "\u2229";
            types.add(new Standable(id));
            types.add(new Stable(id, 95));
            types.add(new Hard(id, 1));
        }
        types.add(new Damage_Taken_Modifier(id, 0, 0.25, DamageType.fire, -1));
        types.add(new Damage_Taken_Modifier(id, 0, 0.5, DamageType.sharp, -1));

    }

    //size is 0-100, 100 being roughly the size of a person
    public Rock(ArrayList<Coord> co, int size, int own){
        this(co, own, (int) (size*(1+(Math.random()*1))), size, (int) ((Math.pow(size,3))*0.02), size*30);

    }
}
