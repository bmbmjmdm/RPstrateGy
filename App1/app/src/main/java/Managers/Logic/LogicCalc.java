package Managers.Logic;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Utilities.ClimbObj;
import Utilities.DamageType;
import Utilities.RemovedException;
import database.Actions.Action;
import database.Coord;
import database.ObjT.Barking;
import database.ObjT.CustomCallable;
import database.ObjT.DefaultStatRestore;
import database.ObjT.Falling;
import database.ObjT.Injury;
import database.ObjT.Moving;
import database.ObjT.ObjT;
import database.ObjT.Preparing;
import database.ObjT.RemoveType;
import database.ObjT.Resting;
import database.ObjT.TurnBP;
import database.ObjT.UserStealthed;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.State;
import Managers.GameManager;

/**
 * Created by Dale on 12/31/2014.
 */
public class LogicCalc {


    public void applyObjType(int otid) {
        GameManager sh = GameManager.getInstance();
        State s = sh.getState();
        try {
            ObjT o = s.getObjT(otid);





            if (o instanceof Moving) {
                MovementLogic movementLogic = new MovementLogic();
                movementLogic.processMoving((Moving) o);
                return;
            }

            if (o instanceof Falling) {
                MovementLogic movementLogic = new MovementLogic();
                movementLogic.processFalling((Falling) o, 50);
                return;
            }










            if(o instanceof Preparing){
                ActionLogic actionLogic = new ActionLogic();
                actionLogic.processPreparing((Preparing) o);
                return;
            }

            if(o instanceof Barking){
                ActionLogic actionLogic = new ActionLogic();
                actionLogic.processBarking((Barking) o);
                return;
            }






            if(o instanceof CustomCallable){
                UtilityLogic uLogic = new UtilityLogic();
                uLogic.processCustom((CustomCallable) o);
                return;
            }

            if(o instanceof RemoveType){
                UtilityLogic uLogic = new UtilityLogic();
                uLogic.processRemoveType((RemoveType) o);
                return;
            }

            if(o instanceof RemoveType){
                UtilityLogic uLogic = new UtilityLogic();
                uLogic.processRemoveType((RemoveType) o);
                return;
            }







            if(o instanceof TurnBP){
                UserLogic ul = new UserLogic();
                ul.processTurnBP((TurnBP) o);
            }

            if(o instanceof Resting){
                UserLogic ul = new UserLogic();
                ul.processResting((Resting) o);
            }

            if(o instanceof DefaultStatRestore){
                UserLogic ul = new UserLogic();
                ul.turnStatsRecover(otid);
            }

            if(o instanceof UserStealthed){
                UserLogic ul = new UserLogic();
                ul.processUserStealth((UserStealthed) o);
            }






            if(o instanceof Injury){
                DamageLogic ul = new DamageLogic();
                ul.processInjury((Injury) o);
            }
        }
        catch(RemovedException e){
            Log.e("applyObjType in LogicCalc", "objT has been removed from the state before it could be processed: "+otid);
        }
    }









/****************************************** Action Logic ******************************/

    public boolean canUse(User u, Action a){
        ActionLogic actionLogic = new ActionLogic();
        return actionLogic.canUse(u, a);
    }


    public Action modAction(Action a, int userId){
        ActionLogic actionLogic = new ActionLogic();
        return actionLogic.modAction(a, userId);
    }









/****************************************** Utility Logic ******************************/

    public boolean overlapping(CObj co, int z, int height, int x, int y){
        UtilityLogic utilityLogic = new UtilityLogic();
        return utilityLogic.overlapping(co, z, height, x, y);
    }

    //gets any CObj that is overlapping at +/- zLenience
    public HashSet<CObj> getCObjAround(Coord at, int zLenience){
        UtilityLogic utilityLogic = new UtilityLogic();
        return utilityLogic.getCObjAround(at, zLenience);
    }

    //gets any CObj that is overlapping at +/- zLenience
    //only check the given list
    public HashSet<CObj> getCObjAround(Coord at, int zLenience, ArrayList<CObj> checkThese){
        UtilityLogic utilityLogic = new UtilityLogic();
        return utilityLogic.getCObjAround(at, zLenience, checkThese);
    }

    public static ArrayList<Coord> getStraightPathNoZ(Coord from, Coord to){
        UtilityLogic utilityLogic = new UtilityLogic();
        return utilityLogic.getStraightPathNoZ(from, to);
    }

    //gets as centered an overlay as possible (returned as c1's z) between c1 and c2, given zLenience and c1's current coordinate
    public int getAccCenter(CObj c2, Coord atCurrent, int zLenience){
        UtilityLogic utilityLogic = new UtilityLogic();
        return utilityLogic.getAccCenter(c2, atCurrent, zLenience);
    }









/****************************************** Detection Logic ******************************/

// given a user, find all cobj within a 5 coord radius around them and add it to their vision if its not already there
    public void minVision(User u){
        DetectionLogic detectionLogic = new DetectionLogic();
        detectionLogic.minVision(u);
    }

    public ArrayList<Coord> canSeeCObj(User u, CObj cobj){
        DetectionLogic detectionLogic = new DetectionLogic();
        return detectionLogic.canSeeCObj(u, cobj);
    }

    //removes all entries that cannot be seen by the user
    public void removeCantSee(HashMap<Coord, HashSet<ClimbObj>> climbTo, User u){
        DetectionLogic detectionLogic = new DetectionLogic();
        detectionLogic.removeCantSee(climbTo, u);
    }

    //Any time anything is created, moved, or removed, you must call lc.updateVisionOf(obj) !!!!!!!!!!!!!!!!!!!!!!!
    public void updateVisionOf(Obj user){
        DetectionLogic detectionLogic = new DetectionLogic();
        detectionLogic.updateVisionOf(user);
    }


    public void removeCantSee(HashSet<CObj> cobjs, User u){
        DetectionLogic detectionLogic = new DetectionLogic();
        detectionLogic.removeCantSee(cobjs, u);
    }

    public void removeCantSee(HashSet<CObj> cobjs, User u, Coord c){
        DetectionLogic detectionLogic = new DetectionLogic();
        detectionLogic.removeCantSee(cobjs, u, c);
    }


    public boolean conceals(CObj concealMe, Coord spot, double widthMod, double heightMod){
        DetectionLogic detectionLogic = new DetectionLogic();
        return detectionLogic.conceals(concealMe, spot, widthMod, heightMod);
    }


    public int getVisionDistance(User u){
        DetectionLogic detectionLogic = new DetectionLogic();
        return detectionLogic.getVisionDistance(u);
    }









/****************************************** Movement Logic ******************************/


    //loc can be null
    public boolean climbable(CObj co, Coord loc, int up, int down, int uBot){
        MovementLogic movementLogic = new MovementLogic();
        return movementLogic.climbable(co, loc, up, down, uBot);
    }

    //loc can be null
    public boolean stepable(CObj co, Coord loc, int up, int down, int uBot){
        MovementLogic movementLogic = new MovementLogic();
        return movementLogic.stepable(co, loc, up, down, uBot);
    }

    public int getStepableHeight(CObj co){
        MovementLogic movementLogic = new MovementLogic();
        return movementLogic.getStepableHeight(co);
    }


    public boolean canFit(Obj co, Coord lo, int maxWidth){
        MovementLogic movementLogic = new MovementLogic();
        return movementLogic.canFit(co, lo, maxWidth);
    }

    public HashMap<Coord, HashSet<ClimbObj>> movementHashMap(int curUser, int range, int zMovementUp, int zMovementDown, boolean climbing, boolean checkSpace){
        MovementLogic movementLogic = new MovementLogic();
        return movementLogic.movementHashMap(curUser, range, zMovementUp, zMovementDown, climbing, checkSpace);
    }

    public HashSet<ClimbObj> movementOnCoord(int curUserID, Coord myC, int zMovementUp, int zMovementDown, boolean climbing, boolean checkSpace){
        MovementLogic movementLogic = new MovementLogic();
        return movementLogic.movementOnCoord(curUserID, myC, zMovementUp, zMovementDown, climbing, checkSpace);
    }







/****************************************** Damage Logic ******************************/

    //this does not check "if" two objects collide. This is only to be called AFTER IT HAS BEEN DETERMINED that two objects collide
    public void collision(Obj o1, Obj o2){
        DamageLogic damageLogic = new DamageLogic();
        damageLogic.collision(o1, o2);
    }

    //an obj should never have damage() called directly on them. this is the logical call to damage on object, so use this
    public void damage(Obj o1, int dmg, DamageType dt){
        DamageLogic damageLogic = new DamageLogic();
        damageLogic.damage(o1, dmg, dt);
    }

    //when an obj takes damage above certain threshholds, this is called to apply appropriate objT (bruises, bleeding, etc)
    public void applyInjury(Obj o, DamageType dt, int threshhold) {
        DamageLogic damageLogic = new DamageLogic();
        damageLogic.applyInjury(o, dt, threshhold);
    };











/****************************************** User Logic ****************************/

    public int getMobility(Obj o){
        UserLogic movementLogic = new UserLogic();
        return movementLogic.getMobility(o);
    }

    public int getUsability(Obj o){
        UserLogic userLogic = new UserLogic();
        return userLogic.getUsability(o);
    }

}