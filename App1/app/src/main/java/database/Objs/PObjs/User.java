package database.Objs.PObjs;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import Utilities.IntObj;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Actions.Action;
import database.Coord;
import database.ObjT.Cushioned;
import database.ObjT.FallsOnPart;
import database.ObjT.Hard;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.Perks.Perk;
import database.Players.Player;
import database.State;
import database.StatelessItems.StatelessItem;
import database.Objs.CObjs.BodyPart;
import Managers.GameManager;
import Managers.Logic.LogicCalc;

/**
 * Created by Dale on 12/29/2014.
 */
public abstract class User extends PObj{
    HashMap<Stat, Integer> userStats = new HashMap<>();

    public ArrayList<Obj> inventory = new ArrayList<Obj>();
    public ArrayList<Action> actions;
    HashMap<Integer, ArrayList<Coord>> vision = new HashMap<>();


    //IF USER EVER CHANGES, UPDATE ALL USES (right now we assume a user has a torso, head, etc
    public User(Player p, int own, String uName){
        super(own, uName);

        //fill stats
        HashMap<Stat, Integer> stats = p.getStats();
        Iterator<Stat> it = stats.keySet().iterator();
        while(it.hasNext()) {
            Stat name = it.next();
            userStats.put(name, stats.get(name));
        }
        userStats.put(Stat.CUR_HEALTH, stats.get(Stat.MAX_HEALTH));
        userStats.put(Stat.CUR_MANA, stats.get(Stat.MAX_MANA));
        userStats.put(Stat.CUR_FOCUS, stats.get(Stat.MAX_FOCUS));
        userStats.put(Stat.CUR_STAMINA, stats.get(Stat.MAX_STAMINA));


        //get actions
        actions = p.actions;


        //fill inventory
        ArrayList<Coord> invLoc = new ArrayList<Coord>();
        //invLoc.add(location);
        for(StatelessItem SI : p.inventory){
            inventory.add(SI.getItem(invLoc, own));
        }

        //misc
        State state = GameManager.getInstance().getState();
        state.getUsers().add(this);

        for(Perk curPer : p.perks){
            curPer.statefullApply(id);
        }
    }

    @Override
    public void whenRemoved(){
        State s = GameManager.getInstance().getState();
        Iterator<User> users = s.getUsers().iterator();
        while(users.hasNext()){
            User u = users.next();
            if(u.id == id){
                users.remove();
            }
        }
        super.whenRemoved();
    }

    public HashMap<Stat, Integer> getStats(){
        userStats.put(Stat.CUR_HEALTH, (int) (getHealthPercent()*userStats.get(Stat.MAX_HEALTH)));
        return userStats;
    }

    public Integer getStat(Stat s){
        userStats.put(Stat.CUR_HEALTH, (int) (getHealthPercent()*userStats.get(Stat.MAX_HEALTH)));
        return userStats.get(s);
    }

    public void setStat(Stat name, int value){
        userStats.put(name, value);
    }



    //returns all coord the user cansee the obj. if it's present, it is returned. if it isn't, it is calculated, added, and returned
    public ArrayList<Coord> getVision(int cobj){
        ArrayList<Coord> returnMe = new ArrayList<>();
        try {
            Obj o = GameManager.getInstance().getState().getObjID(cobj);
            LogicCalc lc = new LogicCalc();

            for(CObj leaf: o.getAllLeafs()){
                ArrayList<Coord> canSee = vision.get(leaf.id);
                if(canSee == null){
                    canSee = lc.canSeeCObj(this, leaf);
                    vision.put(leaf.id, canSee);
                }
                returnMe.addAll(canSee);
            }
        }
        catch (RemovedException e) {
            Log.e("Obj has been removed before it can be seen", "User>getVision("+cobj+")");
        }
        return returnMe;
    }

    //same as ^ but doesnt update hashmap
    public ArrayList<Coord> getVisionNoUpdate(int cobj){
        ArrayList<Coord> returnMe = new ArrayList<>();
        try {
            Obj o = GameManager.getInstance().getState().getObjID(cobj);
            LogicCalc lc = new LogicCalc();

            for(CObj leaf: o.getAllLeafs()){
                ArrayList<Coord> canSee = vision.get(leaf.id);
                if(canSee == null){
                    canSee = lc.canSeeCObj(this, leaf);
                }
                returnMe.addAll(canSee);
            }
        }
        catch (RemovedException e) {
            Log.e("Obj has been removed before it can be seen", "User>getVisionNoUpdate(("+cobj+")");
        }
        return returnMe;
    }


    public Set<Integer> getVisionKeySet(){
        return vision.keySet();
    }

    public void minimumVision(){
        new LogicCalc().minVision(this);
    }

    public void clearVision(int id){
        State s = GameManager.getInstance().getState();
        try{
            Obj o = s.getObjID(id);
            for(CObj co: o.getAllLeafs()){
                vision.remove(co.id);
            }
        }
        catch(RemovedException e){
            Log.e("Object was removed when trying to update vision of it in User>updateVision(id)", id+"");
        }
    }

    public void clearVisionAll(){
        vision = new HashMap<>();
    }


    public abstract void move(ArrayList<Coord> moveTo, boolean removeStanding);


    public Coord getTorsoCoord() {
        for (IntObj io : children) {
            if (io.o.name.contains("Torso")) {
                Coord middleMost = io.o.getMiddlemostCoord();
                return new Coord(middleMost.x, middleMost.y, io.o.getLowestZ());
            }

        }
        return null;
    }

    public Integer getTorsoHeight() {
        for (IntObj io : children) {
            if (io.o.name.contains("Torso")) {
                return io.o.getTallestHeight();
            }

        }
        return null;
    }

    public abstract int getFallingWidth();


    public BodyPart getHead() {
        for (IntObj io : children) {
            Obj o = io.o;
            if (o.name.compareToIgnoreCase("Head") == 0)
                return (BodyPart) o;
        }
        return null;
    }


    @Override
    public boolean canHear(){
        return true;
    }

}
