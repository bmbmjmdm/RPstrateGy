package database.Objs.CObjs;

import android.util.Log;

import java.util.ArrayList;
import java.util.TreeSet;

import Managers.GameManager;
import database.Coord;
import database.ObjT.Encompassing;
import database.ObjT.Squishable;
import database.ObjT.Stable;
import database.ObjT.colorCoord;
import database.ObjT.doesntTakeUpSpace;
import database.ObjT.Standable;
import database.ObjT.Terrain;
import shenronproductions.app1.R;

/**
 * Created by Dale on 1/23/2015.
 */
public class Dirt extends CObj {

    public Dirt(ArrayList<Coord> loc){
        super(loc, -1, 1, 50, 500, 2000 /*see below*/, "Dirt");
        types.add(new Terrain(id));
        types.add(new colorCoord(R.drawable.brown_dirt, id));
        types.add(new Standable(id));
        types.add(new Stable(id, 95));
        types.add(new Encompassing(id));

    }


    //uncomment when you have functionality for holes

    @Override
    public void whenDestroyed(){/*
        for(Coord c : getLoc()){
            TreeSet<CObj> onSpot = GameManager.getInstance().getState().getObjC(c);
            for(CObj co: onSpot){
                if(co instanceof Grass)
                    co.whenDestroyed();
            }
        }*///concurrent modification exception, prevent! TODO
        /*SmallHole h = new SmallHole(getLoc(), -1);
        State s = GameManager.getInstance().getNewState();

        s.remObjID(id);*/
        super.whenDestroyed();
    }


    @Override
    public String getFilterText(){
        return null;
    }

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = super.getDescription();
        //remove onGround
        desc.remove(1);
        //remove height
        desc.remove(1);
        //remove width
        desc.remove(1);

        return desc;
    }
}

