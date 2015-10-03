package database.Objs.PObjs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import Utilities.DamageType;
import Utilities.IntObj;
import Utilities.StringInt;
import database.Coord;
import database.ObjT.ObjT;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.State;
import Managers.GameManager;

/**
 * Created by Dale on 1/24/2015.
 */
public abstract class PObj extends Obj {
    protected ArrayList<IntObj> children = new ArrayList<IntObj>();
    ArrayList<StringInt> permDamage = new ArrayList<>();



    public PObj(int own, String nam){
        super(own, nam, true);
    }

    @Override
    public ArrayList<Coord> getLoc(){
        ArrayList<Coord> ret = new ArrayList<Coord>();
        for(IntObj io : children){
            ret.addAll(io.o.getLoc());
        }
        return ret;
    }

    //there shouldnt be any duplicates here, but incase there are we allow them through so that errors will reveal themselves
    //importance is from 0 to 100
    public void addChild(Obj o, int importance){
        children.add(new IntObj(o, importance));
        o.parent = this;
    }

    //for now we assume any removal of a child will damage the parent permanantly
    public void removeChild(int id){
        Iterator<IntObj> it = children.iterator();
        while(it.hasNext()){
            IntObj io = it.next();
            if(io.o.id == id){
                it.remove();
                permDamage.add(new StringInt(io.o.name, io.i));
                io.o.parent = null;
            }
        }

    }


    public ArrayList<IntObj> getChildren(){
        return (ArrayList<IntObj>) children.clone();
    }


    //this will likely need to be revised per PObj
    public void whenRemoved(){
        Iterator<IntObj> it = ((ArrayList<IntObj>)children.clone()).iterator();
        while(it.hasNext()){
            IntObj io = it.next();
            io.o.whenRemoved();
        }

        for(ObjT ot: types){
            ot.whenRemoved();
        }
        State s = GameManager.getInstance().getState();
        s.remObjID(id);

        if(parent != null){
            parent.removeChild(id);
        }

        removeAllStanding();
    }


    public boolean removeTypeFull(int otid){
        if(removeTypeSelf(otid))
            return true;

        else
            for(IntObj io : children)
                if(io.o.removeTypeFull(otid))
                    return true;

        return false;
    }


    public ArrayList<ObjT> getTypeFull(){
        ArrayList<ObjT> ret = new ArrayList<ObjT>();
        ret.addAll(types);
        for(IntObj io : children)
            ret.addAll(io.o.getTypeFull());

        for(ObjT type: ret){
            type.tempBelongsTo = id;
        }


        return ret;
    }


    public int getHealth(){
        int health = 0;
        for(IntObj io : children){
            health += io.o.getHealth();
        }
        return health;
    }

    //needs to be fixed so it doesnt get to 0 as fast TODO
    public double getHealthPercent(){
        double health = 1;
        for(IntObj io : children){
            health = health - ((1 - io.o.getHealthPercent()) * (double) io.i/100.0);
        }
        for(StringInt si : permDamage){
            health = health - ((double) si.i/100.0);
        }
        return Math.max(0.0, health);
    }

    public int getWeight(){
        int weight = 0;
        for(IntObj io : children){
            weight += io.o.getWeight();
        }
        return weight;
    }


    //should be overriden
    //x shouldnt be 0
    public void damage(int x, DamageType y){
        double totalImportance = 0;
        for(IntObj io : children){
            totalImportance += io.i;
        }
        for(IntObj io : children){
            io.o.damage((int) Math.max(1, Math.round((x*(io.i/totalImportance)))), y);
        }


        //check if we need to destroy ourselves
        if(getHealthPercent() == 0){
            whenDestroyed();
        }

        //check if the parent is destroyed
        if(parent != null){
            if(parent.getHealthPercent() == 0){
                parent.whenDestroyed();
            }
        }

    }


    //should be overriden
    //x shouldnt be 0
    public void damage(int x){
        double totalImportance = 0;
        for(IntObj io : children){
            totalImportance += io.i;
        }
        for(IntObj io : children){
            io.o.damage((int) Math.max(1, Math.round((x*(io.i/totalImportance)))));
        }

        if(getHealthPercent() == 0){
            if(parent != null){
                if(parent.getHealthPercent() == 0){
                    parent.whenDestroyed();
                }
                else{
                    whenDestroyed();
                }
            }
            else{
                whenDestroyed();
            }
        }
    }

    public int getMaxHealth(){
        int health = 0;
        for(IntObj io : children){
            health += io.o.getMaxHealth();
        }
        return health;
    }

    //lenience in cm
    public float collides(Obj o, int zLenience){
        float ret = 0;
        for(CObj co : o.getAllLeafs()){
            ret += (1-ret) * co.collides(this, zLenience);
        }
        return ret;
    }

    public ArrayList<CObj> getAllLeafs(){
        ArrayList<CObj> ret = new ArrayList<CObj>();
        for(IntObj io : children){
            ret.addAll(io.o.getAllLeafs());
        }
        return ret;
    }

    @Override
    public boolean isParent(){
        return true;
    }


    public HashSet<Coord> getAllCoords(){
        HashSet<Coord> ret = new HashSet<Coord>();
        for(IntObj o: children){
            ret.addAll(o.o.getAllCoords());
        }
        return ret;
    }

    @Override
    public int getTallestHeight(){
        double max = Double.NEGATIVE_INFINITY;
        for(IntObj io: children){
            Obj o = io.o;
            if (o.getTallestHeight() > max)
                max = o.getTallestHeight();
        }
        return (int) max;
    }

    @Override
    public int getLowestZ(){
        double min = Double.POSITIVE_INFINITY;
        for(IntObj io: children){
            Obj o = io.o;
            if (o.getLowestZ() < min)
                min = o.getLowestZ();
        }
        return (int) min;
    }

    @Override
    public int getHighestZ(){
        double max = Double.NEGATIVE_INFINITY;
        for(IntObj io: children){
            Obj o = io.o;
            if (o.getHighestZ() > max)
                max = o.getHighestZ();
        }
        return (int) max;
    }

    @Override
    public int getHighestPoint(){
        double max = Double.NEGATIVE_INFINITY;
        for(IntObj io: children){
            Obj o = io.o;
            if (o.getHighestPoint() > max)
                max = o.getHighestPoint();
        }
        return (int) max;
    }


    @Override
    public Coord getMiddlemostCoord(){
        HashSet<Coord> loc = getAllCoords();
        int size = loc.size();
        if(size > 0) {
            long avgX = 0;
            long avgY = 0;
            long avgZ = 0;
            for (Coord c : loc) {
                avgX += c.x;
                avgY += c.y;
                avgZ += c.z;
            }

            avgX = avgX/size;
            avgY = avgY/size;
            avgZ = avgZ/size;

            Coord closest = new Coord(0,0,0);
            long offBy = 999999;
            long largestOff = 9999999;
            for(Coord c: loc){
                long difX = Math.abs(avgX - c.x);
                long difY = Math.abs(avgY - c.y);
                long difZ = Math.abs(avgZ - c.z)/50;
                long largestDif = Math.max(difX, difY);

                long totalDif = difX + difY + difZ;

                if(totalDif < offBy){
                    closest = c;
                    offBy = totalDif;
                    largestOff = largestDif;
                }
                else if (totalDif == offBy){
                    if(largestDif < largestOff){
                        closest = c;
                        offBy = totalDif;
                        largestOff = largestDif;
                    }
                }
            }

            return closest;

        }
        else
            return null;
    }

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = super.getDescription();
        String onGround;
        int zLow = getLowestZ();
        if(zLow == 0)
            onGround =  "<font color=#000000>On &#160;ground</font>";
        else if(zLow > 0)
            onGround =  "<font color=#0000CC>"+((double) zLow/100)+"</font><font color=#000000>m &#160;Above &#160;ground</font>";
        else
            onGround =  "<font color=#996633>"+((double) zLow/100)+"</font><font color=#000000>m &#160;Below &#160;ground</font>";
        desc.add(onGround);
        return desc;
    }

    /*public HashMap<Coord, Coord>  getFallingCoords(){
        HashMap<Coord, Coord> minZs = new HashMap<>();

        for(IntObj io: children){
            HashMap<Coord, Coord> curMinZs = io.o.getFallingCoords();

            for(Coord look: curMinZs.keySet()){
                Coord co = curMinZs.get(look);
                Coord got = minZs.get(look);
                if(got == null)
                    minZs.put(look, co);
                else {
                    if (got.z > co.z)
                        minZs.put(look, co);
                }
            }

        }

        return minZs;
    }*/

    public boolean contains(Obj o){
        if(this == o)
            return true;
        for(IntObj io: children){
            if(io.o.contains(o))
                return true;
        }
        return false;
    }

}
