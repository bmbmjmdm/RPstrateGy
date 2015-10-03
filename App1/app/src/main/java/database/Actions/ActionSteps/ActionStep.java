package database.Actions.ActionSteps;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import Managers.GameManager;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Actions.SubActions.SubAction;
import database.Narration;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.Requirements.Requirement;
import database.Requirements.statCost;

/**
 * Created by Dale on 7/3/2015.
 */
public class ActionStep implements Serializable {
    public String name;
    ArrayList<Requirement> requirements;
    public int userId;
    int minSpeed;
    public HashSet<Stat> classes = new HashSet<Stat>();
    ArrayList<SubAction> useActions;
    ArrayList<SubAction> stopActions;
    int minStopTime;
    public boolean stopped = false;
    public int timeElaps;
    public int id;

    /**
     * Used to represents UNMODIFIABLE action steps in the timeline. These have 2 functions, useNow and stopUse. useNow is thenormal case, stopUse is when the user wants to stop their action short
     */
    public ActionStep(int id, String nam, ArrayList<Requirement> reqs, int userId, int speed, ArrayList<SubAction> actionsForUse, ArrayList<SubAction> actionsToStop, int stopTime, Stat... classes){
        name = nam;
        requirements = reqs;
        this.userId = userId;
        minSpeed = speed;
        for(Stat c : classes)
            this.classes.add(c);
        useActions = actionsForUse;
        stopActions = actionsToStop;
        minStopTime = stopTime;
        this.id = id;
    }

    public int getSpeed(){
        try {
            User u = (User) GameManager.getInstance().getState().getObjID(userId);
            int total = 0;
            int it = 0;
            for (Stat s : classes) {
                if (s != Stat.MISC) {
                    it++;
                    total = u.getStats().get(s);
                }
            }
            if(it != 0) {
                total = total / it;
                return (minSpeed + (int) ((double) minSpeed / 3.0 * (double) total / 100.0));
            }
            else
                return minSpeed;
        }

        catch(RemovedException e){
            Log.e("ActionStep>getSpeed", "couldnt calculate speed because user does not exist in state");
            return -1;
        }
    }


    //returns true if this action is completed, returns false if unable
    public boolean useNow(){
        try{
            User u = (User) GameManager.getInstance().getState().getObjID(userId);

            //check all req
            boolean canUse = true;
            for (Requirement r : requirements) {
                if (!r.canUse(u)) {
                    canUse = false;
                    break;
                }
            }

            //if cant use, create a narration and return false to signal to cancel all future actionsteps for this action
            if(!canUse){
                HashSet<Obj> failNarrationInvolves = new HashSet<>();
                failNarrationInvolves.add(u);
                String failText = u.name + " failed to complete the action: "+name;
                //add narration
                new Narration(failText, failNarrationInvolves, Stat.MISC);
                return false;
            }

            //if can use, pay all costs and use all subactions, then return true to signal all is good
            else {
                for (Requirement r : requirements) {
                    if (r instanceof statCost)
                        ((statCost) r).pay(u);
                }

                for (SubAction sa : useActions) {
                    sa.useContinue(u, getSpeed());
                }
                return true;
            }
        }

        catch(RemovedException e){
            Log.e("ActionStep>useNow", "couldnt use action because user does not exist in state");
            return false;
        }
    }

    //timeElaps is how much time has passed between the last ActionStep by this user and right now
    //the returned int is how much more time to process
    public int stopTime(){
        int processTime;

        if(timeElaps>= minStopTime)
            processTime = 0;
        else
            processTime = minStopTime - timeElaps;

        return processTime;
    }



    //called when the action is stopped
    public void stopUse(){
        try{
            User u = (User) GameManager.getInstance().getState().getObjID(userId);

            for(SubAction sa: stopActions){
                sa.useStop(u, getSpeed());
            }
        }

        catch(RemovedException e){
            Log.e("ActionStep>stopUse", "couldnt stop action because user does not exist in state");
        }
    }
}
