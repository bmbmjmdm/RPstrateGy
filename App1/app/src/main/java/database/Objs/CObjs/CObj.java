package database.Objs.CObjs;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Utilities.DamageType;
import Utilities.IntObj;
import database.Coord;
import database.ObjT.ObjT;
import database.Objs.Obj;
import database.State;
import Managers.GameManager;
import Managers.Logic.LogicCalc;

/**
 * Created by Dale on 1/25/2015.
 */
public abstract class CObj extends Obj {
    int health;
    //in cm. 0 is a valid height, it means on the ground
    protected int height;
    int maxHealth;
    //width max is 50, 100 for oversized objects
    protected int width;

    //in lb
    private int weight;


    //unicode
    String image = "";

    ArrayList<Coord> loc = new ArrayList<Coord>();


    HashMap<DamageType, Integer> damageTaken = new HashMap<>();

    ArrayList<Coord> imaginaryLoc = new ArrayList<>();


    public CObj(ArrayList<Coord> co, int own, int hei, int wid, int weigh, int heal, String nam){
        super(own, nam, own==-123456789?false:true); //this tests if the object is real or not (for comparatorObject)
        health = heal;
        maxHealth = heal;
        height = hei;
        width = wid;
        weight = weigh;
        State s = GameManager.getInstance().getState();

        for(Coord c : co){
            loc.add(c);
            s.addObjC(c, this);
        }
    }


    public int getWeight(){
        return weight;
    }
    public ArrayList<Coord> getLoc(){
        return (ArrayList<Coord>) loc.clone();
    }

    public boolean removeLoc(Coord c){
        for(Coord co : loc){
            if(co.eq(c)) {
                loc.remove(co);
                return true;
            }
        }
        return false;
    }


    //the imaginary loc is used when you want to check an obj on a spot they are not technically on yet. you set it with the "useImaginaryLoc(Coord c)" function of Obj
    //WHEN YOU ARE DONE you MUST call "stopUsingImaginaryLoc()" from Obj
    public void clearImaginaryLoc(){
        imaginaryLoc = new ArrayList<>();
    }

    public void addImaginaryLoc(Coord c){
        imaginaryLoc.add(c);
    }



    //returns null if the obj doesnt exist at the coord, should NEVER HAPPEN
    public Integer getZ(int x, int y){
        for(Coord co : imaginaryLoc){
            if(co.x == x && co.y == y)
                return co.z;
        }

        for(Coord co : loc){
            if(co.x == x && co.y == y)
                return co.z;
        }
        return null;
    }

    @Override
    public Coord getMiddlemostCoord(){
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

    public int getHealth(){
        return health;
    }

    public int getMaxHealth(){
        return maxHealth;
    }

    public double getHealthPercent(){
        return Math.max(0.0, (double)health/(double)maxHealth);
    }

    //at this point x is pure damage, it does not get further mods
    public void damage(int x, DamageType y){
        //if x is greater than health, set to be equal because we can't have negative health
        int realX;
        if(x > health){
            realX = health;
        }
        else{
            realX = x;
        }

        health = health-realX;

        //update damage taken from that damageType
        Integer taken = damageTaken.get(y);
        if(taken == null)
            taken = 0;

        taken += realX;
        damageTaken.put(y, taken);


        //percent damage taken, 0-100
        int percentDamage = (int) Math.round(((double)realX/maxHealth)*100);

        if(percentDamage > 0) {
            new LogicCalc().applyInjury(this, y, percentDamage);
        }

        //check if we need to destroy ourselves
        if(health == 0){
            whenDestroyed();
        }

        //check if the parent is destroyed
        if(parent != null){
            if(parent.getHealthPercent() == 0){
                parent.whenDestroyed();
            }
        }
    }


    //at this point x is pure damage, it does not get further mods
    public void damage(int x){
        //if x is greater than health, set to be equal because we can't have negative health
        int realX;
        if(x > health){
            realX = health;
        }
        else{
            realX = x;
        }

        health = health-realX;

        //check if we need to destroy ourselves
        if(health == 0){
            whenDestroyed();
        }

        //check if the parent is destroyed
        if(parent != null){
            if(parent.getHealthPercent() == 0){
                parent.whenDestroyed();
            }
        }
    }


    //called when this is removed from the state for ANY REASON
    public void whenRemoved(){
        State s = GameManager.getInstance().getState();
        for(Coord c : loc){
            s.remObjC(c, this);
        }
        for(ObjT ot: types){
            ot.whenRemoved();
        }
        s.remObjID(id);

        if(parent != null){
            parent.removeChild(id);
        }

        removeAllStanding();
    }

    //moving removes all coordinates the obj currently is at and assumes its now at the new ones
    public void move(ArrayList<Coord> colist, boolean remStanding){
        State newS = GameManager.getInstance().getState();
        for(Coord co : loc) {
            newS.remObjC(co, this);
        }
        loc = new ArrayList<>();

        for(Coord co : colist) {
            loc.add(co);
            newS.addObjC(co, this);
        }
        if(remStanding)
            removeAllStanding();
    }

    public boolean removeTypeFull(int otid){
        return removeTypeSelf(otid);
    }

    //full means yourself and all your children
    public ArrayList<ObjT> getTypeFull(){
        return getTypeSelf();
    }


    //lenience in cm
    public float collides(Obj o, int zLenience){
        float ret = 0;
        for(CObj co : o.getAllLeafs()){
            for(Coord c : co.getLoc()){
                for(Coord myC : getLoc()){
                    if(myC.x == c.x)
                        if(myC.y == c.y){
                            int minZ = Math.min(myC.z, c.z);
                            int maxZ = Math.max(myC.z+height, c.z+co.height);
                            int hitBox = maxZ-minZ;
                            if(height+co.height+zLenience > hitBox){
                                float perChance = (Math.min(1, (co.width+width)/(float) 51));
                                perChance = perChance * (Math.min(1, (co.height+height)/(float) hitBox));
                                ret  += (1-ret)* perChance;
                            }
                        }
                }

            }
        }
        return ret;
    }

    public ArrayList<CObj> getAllLeafs(){
        ArrayList<CObj> me = new ArrayList<CObj>();
        me.add(this);
        return me;
    }

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = super.getDescription();
        desc.add(0, "<font color=#000000>Symbol: "+getIcon()+"</font>");

        String onGround;
        int zAvg = getLowestZ();
        if(zAvg == 0)
            onGround =  "<font color=#000000>On &#160;ground</font>";
        else if(zAvg > 0)
            onGround =  "<font color=#0000CC>"+((double) zAvg/100)+"</font><font color=#000000>m &#160;Above &#160;ground</font>";
        else
            onGround =  "<font color=#996633>"+((double) zAvg/100)+"</font><font color=#000000>m &#160;Below &#160;ground</font>";
        desc.add(2, onGround);

        desc.add(3, "<font color=#6600CC>"+height+"</font><font color=#000000>cm &#160;Tall</font>");

        desc.add(4, "<font color=#6600CC>"+width+"</font><font color=#000000>cm &#160;wide</font>");

        return desc;
    }

    public HashSet<Coord> getAllCoords(){
        HashSet<Coord> ret = new HashSet<Coord>();
            ret.addAll(loc);
        return ret;
    }

    public String getFilterText(){
        return name;

    }

    public String getIcon(){
        return image;

    }

    public String getIcon(Coord c){
        return image;

    }

    @Override
    public int getTallestHeight(){
        return getHeight();
    }

    public int getHeight(){
        return height;
    }

    public int getWidth(){
        return width;
    }

    @Override
    public int getLowestZ(){
        double min = Double.POSITIVE_INFINITY;
        for(Coord co : loc){
            if(co.z < min){
                min = co.z;
            }
        }
        return (int) min;
    }

    @Override
    public int getHighestZ(){
        double max = Double.NEGATIVE_INFINITY;
        for(Coord co : loc){
            if(co.z > max){
                max = co.z;
            }
        }
        return (int) max;
    }

    @Override
    public int getHighestPoint(){
        return getHighestZ() + getHeight();
    }

    /*public HashMap<Coord, Coord>  getFallingCoords(){
        HashMap<Coord, Coord> minZs = new HashMap<>();
        for(Coord co: loc){
            Coord lookup = new Coord(co.x, co.y);
            Coord got = minZs.get(lookup);
            if(got == null)
                minZs.put(lookup, co);
            else {
                if (got.z > co.z)
                    minZs.put(lookup, co);
            }
        }

        return minZs;
    }*/

    public int getFallingWidth(){
        return width;
    }

    public boolean contains(Obj o) {
        if (this == o)
            return true;
        return false;
    }


    public  int getMovingHeight(){return getHeight();}
}
