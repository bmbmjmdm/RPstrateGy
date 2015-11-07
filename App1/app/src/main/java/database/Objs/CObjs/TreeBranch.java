package database.Objs.CObjs;

import java.util.ArrayList;
import java.util.HashSet;

import Utilities.Stat;
import database.Coord;
import database.Narration;
import database.ObjT.Hard;
import database.ObjT.Standable;
import Managers.GameManager;
import Managers.timeKeeper;
import database.Objs.Obj;

/**
 * Created by Dale on 2/6/2015.
 */
public class TreeBranch extends CObj{

    public TreeBranch(ArrayList<Coord> co, int own, int hei, int wid, int weigh, int heal){
        super(co, own, hei, wid, weigh, heal, "Tree Branch");
        image = "\uFEB3";
        types.add(new Standable(id));
        types.add(new Hard(id, 0.75));


        showByDefault = false;
    }

    @Override
    public void whenDestroyed(){
        if(parent.getHealth() != 0) {
            HashSet<Obj> destroyedNarration = new HashSet<>();
            destroyedNarration.add(this);
            String failText = this.name + " has been destroyed!";
            //add narration
            new Narration(failText, destroyedNarration, Stat.MISC);
        }

        double ran;
        timeKeeper tk = GameManager.getInstance().getTimeline();
        int i = 0;

        for(Coord c: loc){
            i++;
            ArrayList<Integer> identifiers = new ArrayList<>();
            identifiers.add(id);
            identifiers.add(i);

            ran = tk.getRand(identifiers, "Branch Destroyed");
            if(ran>0.7) {
                ArrayList<Coord> newLoc = new ArrayList<Coord>();
                newLoc.add(new Coord(c.x, c.y));
                new Stick(newLoc, -1);
            }
        }
        whenRemoved();
    }
}
