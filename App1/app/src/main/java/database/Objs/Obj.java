package database.Objs;


import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import Managers.Logic.LogicCalc;
import Utilities.DamageType;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Coord;
import database.Narration;
import database.ObjT.Falling;
import database.ObjT.ObjT;
import database.Objs.CObjs.CObj;
import database.Objs.PObjs.PObj;
import database.State;
import Managers.GameManager;
import shenronproductions.app1.R;

/**
 * Created by Dale on 12/29/2014.
 */
public abstract class Obj implements Serializable {
    public int id;
    protected ArrayList<ObjT> types = new ArrayList<ObjT>();
    public String name;

    public int owner;

    public PObj parent = null;

    public Integer standingOn = null;
    public HashSet<Integer> supportingThese = new HashSet<Integer>();




    public Obj(int own, String nam, boolean isReal){
        if(isReal) {
            id = GameManager.getInstance().getTimeline().getId();
            owner = own;
            GameManager.getInstance().getState().addObjID(this);
            name = nam;
        }
    }

    //simple adding function
    //will remove/keep types based on the NAME OF THE TYPE
    //only checks self!!!!
    public void addType(ObjT type){
        //duplicates not allowed
        if(type.duplicates != 0){

            //go through obj types
            Iterator<ObjT> it = types.iterator();
            while(it.hasNext()){
                ObjT hasType = it.next();

                //if they are the same
                if(hasType.name.compareToIgnoreCase(type.name) == 0){

                    if(type.duplicates == -1){
                        //keep the old
                        return;
                    }
                    else{
                        //keep the new
                        it.remove();
                        break;
                    }

                }
            }
        }

        //we are either allowing duplicates or adding the new one over the old
        types.add(type);
    }

    //if its a CObj, this will have no duplicates. if its a PObj, this can have duplicates. therefore this can have duplicates for an Obj
    public abstract ArrayList<Coord> getLoc();

    //called when this is removed from the state for due to having 0 or less health
    public void whenDestroyed(){
        HashSet<Obj> destroyedNarration = new HashSet<>();
        destroyedNarration.add(this);
        String failText = this.name + " has been destroyed!";
        //add narration
        new Narration(failText, destroyedNarration, Stat.MISC);
        whenRemoved();
    }

    //called when this is removed from the state for ANY REASON
    public abstract void whenRemoved();

    //in lb
    public abstract int getWeight();


    //remove type from you
    public boolean removeTypeSelf(int otid){
        Iterator<ObjT> it = types.iterator();
        while(it.hasNext()) {
            ObjT cur = it.next();
            if(cur.id == otid) {
                it.remove();
                GameManager.getInstance().getState().remObjT(cur.id);
                return true;
            }
        }
        return false;
    }

    //remove type from you and all your children
    public abstract boolean removeTypeFull(int otid);

    //remove type from you and your parents/grandparents up to the top
    public boolean removeTypePath(int otid){
        if(removeTypeSelf(otid))
            return true;
        else if (parent != null)
            return parent.removeTypePath(otid);
        else
            return false;
    }

    //get your types
    public ArrayList<ObjT> getTypeSelf(){
        ArrayList<ObjT> retMe = (ArrayList<ObjT>) types.clone();
        for(ObjT type: retMe){
            type.tempBelongsTo = id;
        }

        return retMe;
    }

    //get you and all your childrens' types
    public abstract ArrayList<ObjT> getTypeFull();

    //get your types and all the types of your parents/grandparents up to the top
    //this is what is used for seeing what a perticular obj's types are
    public ArrayList<ObjT> getTypePath(){
        ArrayList<ObjT> ret = new ArrayList<ObjT>();
        ret.addAll(types);
        if(parent != null)
            ret.addAll(parent.getTypePath());

        for(ObjT type: ret){
            type.tempBelongsTo = id;
        }

        return ret;
    }

    public Obj getTop(){
        if(parent == null)
            return this;
        else
            return parent.getTop();
    }

    public abstract int getHealth();

    public abstract int getMaxHealth();

    //from 0 to 1, should be used for any display-health or to see if an obj is dead
    //should be used in general
    public abstract double getHealthPercent();


    //collision will be based on a "damaging" objT that will have the type of damage and severety.
    public abstract void damage(int x, DamageType y);

    //this does not apply any injury based on damage type, just pure damage
    public abstract void damage(int x);

    //returns % chance of colliding
    //returns the raw % of collision based on (x == x1) && (y == y1) &&
    //                                          z+height =~(zLenience)~= z!+height1 &&
    //                                          w ~~ w1
    //lenience in cm
    public abstract float collides(Obj o, int zLenience);

    public abstract ArrayList<CObj> getAllLeafs();

    //override if objects should be represented differently than they are on the map. for example bodyparts
    public Obj getPresentable(Coord c){
        return this;
    }
    public Obj getPresentable(){
        return this;
    }

    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        desc.add("<font color=#E42217>"+Math.ceil((getHealthPercent()*100))+"%</font> &#160<font color=#000000>Health</font>");
        for(ObjT type : getTypePath()){
            if(type.isPreparing())
                desc.addAll(0, type.getDescription());
            else
                desc.addAll(type.getDescription());
        }
        return desc;
    }

    public boolean isParent(){
        return false;
    }

    public abstract HashSet<Coord> getAllCoords();

    public abstract int getTallestHeight();

    public abstract int getHighestPoint();

    public abstract int getLowestZ();

    public abstract int getHighestZ();

    public abstract Coord getMiddlemostCoord();

    //This adds an object with id == id to stand on top of this object
    public void supportThis(int id){
        supportingThese.add(id);
    }

    //This removes an object with id == id from standing on top of this object
    public void stopSupportingThis(int id){
        supportingThese.remove(id);
    }

    //This makes it appear that this cobj is standing on the obj of id
    public void standOnThis(int id){
        standingOn = id;
    }

    //This makes it appear that this cobj is not standing on any obj
    public void standOnNothing(){
        State newS = GameManager.getInstance().getState();
        standingOn = null;
        ObjT air = new Falling(id);
        addType(air);
        newS.addEOTObjT(air.id, newS.getTime()+1);
    }

    public abstract void move(ArrayList<Coord> coords, boolean removeStanding);

    public void removeAllStanding(){
        State newS = GameManager.getInstance().getState();
        //update anything that is standing on this object or anything this object is standing on
        try{
            if(standingOn != null) {
                newS.getObjID(standingOn).stopSupportingThis(id);
            }
        }
        catch(RemovedException e){}
        for(Integer i: supportingThese){
            try{
                Obj o = newS.getObjID(i);
                o.standOnNothing();
            }
            catch(RemovedException e){}
        }

        standingOn = null;
        supportingThese = new HashSet<>();
    }

    //Returns the bottom most coordinate for each x/y this obj is on
    //public abstract HashMap<Coord, Coord> getFallingCoords();

    public abstract int getFallingWidth();

    public abstract boolean contains(Obj o);

    public boolean fullContainsPath(Obj o){
        if (parent != null){
            return parent.fullContainsPath(o);
        }
        else
            return contains(o);
    }

    public void useImaginaryLoc(Coord c){
        ArrayList<CObj> objs = getAllLeafs();

        for(CObj co: objs){
            co.addImaginaryLoc(c);
        }
    }

    public void stopUsingImaginaryLoc(){
        ArrayList<CObj> objs = getAllLeafs();

        for(CObj co: objs){
            co.clearImaginaryLoc();
        }
    }


    public boolean isOrIsPartOf(int oId){
        if(id == oId){
            return true;
        }

        else {
            if (parent != null){
                return parent.isOrIsPartOf(oId);
            }
            else{
                return false;
            }
        }
    }


    public boolean canHear(){
        return false;
    }

    public abstract int getMovingHeight();

    public int getImage(){
        return R.drawable.cpu;//TODO
        //TODO
        //TODO
    }


    public boolean hasParent(){
        if(parent == null){
            return false;
        }
        else{
            return true;
        }
    }
}
