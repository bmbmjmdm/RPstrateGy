package database.Objs.CObjs;

import java.util.ArrayList;
import java.util.TreeSet;

import Managers.Logic.LogicCalc;
import Utilities.DamageType;
import database.Coord;
import database.ObjT.Cushioned;
import database.ObjT.Damage_Taken_Modifier;
import database.ObjT.Invisible;
import database.ObjT.ObjT;
import database.ObjT.Stable;
import database.ObjT.colorCoord;
import database.ObjT.doesntTakeUpSpace;
import database.ObjT.Squishable;
import database.ObjT.Standable;
import database.ObjT.Terrain;
import database.State;
import Managers.GameManager;
import shenronproductions.app1.R;

/**
 * Created by Dale on 1/15/2015.
 */
public class Grass extends CObj {


    public Grass(ArrayList<Coord> loc, int own, int hei){
        super(loc, own, hei, 50, 5, 100, "Grass");
        types.add(new Damage_Taken_Modifier(id, 0, 2.5, DamageType.fire, -1));
        types.add(new Damage_Taken_Modifier(id, 0, 1.5, DamageType.sharp, -1));
        types.add(new colorCoord(R.drawable.green_grass, id));
        types.add(new doesntTakeUpSpace(id));
        types.add(new Squishable(id));
        types.add(new Standable(id));
        types.add(new Cushioned(id, hei/2));
        types.add(new Stable(id, 90));
        TreeSet<CObj> onSpot = GameManager.getInstance().getState().getObjC(loc.get(0));
        for(CObj co: onSpot){
            if(co instanceof Dirt)
                co.addType(new Invisible(co.id));
        }
    }

    @Override
    public String getFilterText(){
        return null;
    }

    @Override
    public void whenRemoved(){
        TreeSet<CObj> onSpot = GameManager.getInstance().getState().getObjC(loc.get(0));
        for(CObj co: onSpot){
            if(co instanceof Dirt) {
                ArrayList<ObjT> types = co.getTypeSelf();
                for(ObjT curT: types){
                    if(curT instanceof Invisible) {
                        co.removeTypeSelf(curT.id);
                        new LogicCalc().updateVisionOf(co);
                    }
                }

            }
        }
        super.whenRemoved();
    }

}
