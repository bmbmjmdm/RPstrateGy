package Managers.Logic;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

import Managers.GameManager;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Actions.Action;
import database.Coord;
import database.Narration;
import database.ObjT.Barking;
import database.ObjT.Intimidated;
import database.ObjT.ObjT;
import database.ObjT.Preparing;
import database.ObjT.RemoveType;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.Requirements.Requirement;
import database.State;

/**
 * Created by Dale on 4/29/2015.
 */
public class ActionLogic {



    public boolean canUse(User u, Action a){
        ArrayList<Requirement> requires = a.requirements;

        for(Requirement req : requires){
            if(!req.canUse(u))
                return false;
        }

        return true;
    }




    public Action modAction(Action a, int userId){
        try {
            State s = GameManager.getInstance().getState();
            Obj u = s.getObjID(userId);

            //find out if the action time needs being modded
            double timeMod = 1;

            for (ObjT type: u.getTypePath()) {
                //these stack, so its a tangent near 0 and inifinity
                timeMod *= type.actionTimeMod();
            }

            //this is a percent of the normal useTime
            //so 1.05 would be 5% longer, 0.95 would be 5% faster
            a.maxTimeNeeded = (int) (a.maxTimeNeeded * timeMod);

        }

        catch(RemovedException e){
            Log.e("modAction could not find the user", "end game?");
        }



        return a;
    }





    public void processPreparing(Preparing objT){
        try {
            State s = GameManager.getInstance().getState();
            User u = (User) s.getObjID(objT.belongsTo);
            boolean canUse = true;
            for (Requirement req : objT.requirements) {
                if (!req.canUse(u))
                    canUse = false;
            }

            if(!canUse){
                s.removeEndOfTurnOT(-1, objT.id);
                u.removeTypeSelf(objT.id);
            }

        }
        catch(RemovedException e){

        }
    }






    public void processBarking(Barking objT){
        try {
            State s = GameManager.getInstance().getState();
            User u = (User) s.getObjID(objT.belongsTo);
            Coord barker = u.getHead().getMiddlemostCoord();

            //intimidate everyone within earshot

            //lookthrough the users list
            for(User user: s.getUsers()) {
                //don't check yourself or allies
                if (user.owner != u.owner) {
                    //if they can hear
                    if (user.canHear()) {
                        //and are within hearing distance
                        Coord hearer = user.getHead().getMiddlemostCoord();
                        int distance = hearer.distance(barker);
                        if (distance < 11) {

                            //add the type
                            Intimidated newT = new Intimidated(user.id, objT.intimidation, "Bark", "A blood-thirsty bark resonates loud and clear.");
                            user.addType(newT);

                            //set it up to be removed 3 seconds later
                            RemoveType remT = new RemoveType(0, newT.id);
                            s.addEOTObjT(remT.id, s.getTime()+3000);

                        }
                    }
                }
            }

            //add narration
            HashSet<Obj> narrationInvolves = new HashSet<>();
            narrationInvolves.add(u);
            String text = u.name + " barks like an animal.";
            new Narration(text, narrationInvolves, Stat.WARRIOR);

            //no longer barking
            u.removeTypeSelf(objT.id);

        }
        catch(RemovedException e){

        }
    }
}
