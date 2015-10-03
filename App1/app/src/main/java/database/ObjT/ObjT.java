package database.ObjT;

import java.io.Serializable;
import java.util.ArrayList;

import Utilities.DamageType;
import database.Coord;
import Managers.GameManager;

/**
 * Created by Dale on 12/29/2014.
 */
public abstract class ObjT implements Serializable{
    //id is unique for each status created
    public int id;
    //name is often the same as the class name
    public String name;
    //-1 for infinite, 1 if it goes away at the end of the post, 0 should not be used
    public int lifespan;
    public int belongsTo;

    //-1 means you cant have duplicates and must KEEP the old value
    //1 means you cant have duplicates and must GET RID OF the old value
    //0 means you can have duplicates
    //objT that use special cases should not use this variable
    public int duplicates;

    //this is used when a child or parent takes an objT of their parent/child and treat it as their own (getTypePath or getTypeFull)
    public int tempBelongsTo;


    //NOTE: AN OBJT CAN ONLY HAVE 1 CONSTRUCTOR
    public ObjT(String nam, int life, int oID, int dup){
        name = nam;
        id =  GameManager.getInstance().getTimeline().getId();
        lifespan = life;
        belongsTo = oID;
        tempBelongsTo = belongsTo;
        duplicates = dup;
        GameManager.getInstance().getState().addObjT(this);
    }


    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        desc.add("<font color=#000000>" + name + "</font>");
        return desc;
    }

    //this is called based on the objT
    //in logic calc, when this is called, be sure to add the objT to an array, This array contains everything it needs to remove or add to the new state at the end of the resolution of objT.
    //it should be sure to re-check the health/lifespan of whatever its removing before removing it.
    public double decrement(){
        lifespan--;
        return lifespan;
    }

    //called when this is removed from the state for ANY REASON
    public void whenRemoved(){
        GameManager.getInstance().getState().remObjT(id);
    }




    public int getBloodloss(){
        return 0;
    }

    public ArrayList<Coord> movement(){
        return new ArrayList<Coord>();
    }

    public int getBGColor(){
        return -1;
    }

    public int speed(){
        return Integer.MAX_VALUE;
    }

    public boolean invisible(){
        return false;
    }
    public boolean seeThrough(){
        return false;
    }
    public int restModC(){
        return 0;
    }
    public int recModC(){
        return 0;
    }
    public double restModP(){
        return 1;
    }
    public double recModP(){
        return 1;
    }


    public int dmgTakeC(DamageType s){ return 0;}
    public double dmgTakeP(DamageType s){return 1;}

    public boolean climable(){return false;}
    public boolean standable(){return false;}

    public boolean passable(){
        return false;
    }

    public boolean isPreparing(){
        return false;
    }

    public boolean squishable(){
        return false;
    }

    public boolean isMoving(){return false;}

    public boolean isFalling(){return false;}

    public boolean isInAir(){return false;}

    public int cushion(){return 0;}

    public int stable(){return 0;}

    public boolean fallsOnPart(){
        return false;
    }

    public boolean isTerrain(){ return false;}


    public boolean encompassing(){return false;}


    public double hardness(){
        return 0;
    }


    //ranges from 0-infinity. less than 1 makes action faster, greater than 1 makes them slower
    public double actionTimeMod(){return 1;}


    public String getAlerText(){return "";}


    public boolean isStealthed(){
        return false;
    }

    public boolean isIntimidated() {return false;}

    //infection rate is 0-1, 0 is no chance, 1 is 100% chance
    public double getInfectionRate(){ return 0;}

    //less than one makes it harder to use a body part
    //greater than one makes it easier
    public double getUsabilityMod(){return 1;}

    //damage is 0+
    public int getDOT(){return 0;}
}
