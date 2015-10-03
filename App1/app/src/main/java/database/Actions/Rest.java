package database.Actions;

import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import Managers.GameManager;
import Managers.Logic.LogicCalc;
import Managers.timeKeeper;
import Utilities.Callable;
import Utilities.ClimbObj;
import Utilities.CoordInt;
import Utilities.Direction;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Actions.ActionSteps.ActionStep;
import database.Actions.SubActions.AddEOT;
import database.Actions.SubActions.AddNarration;
import database.Actions.SubActions.NewObjT;
import database.Actions.SubActions.ObjTModCallable;
import database.Actions.SubActions.RemObjT;
import database.Actions.SubActions.SubAction;
import database.Coord;
import database.ObjT.Moving;
import database.ObjT.ObjT;
import database.ObjT.Resting;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.Requirements.Requirement;
import database.Requirements.bodypartReq;
import database.Requirements.inAirReq;
import database.Requirements.statCost;
import database.Requirements.statReq;
import database.State;
import shenronproductions.app1.Activities.gameAct;
import shenronproductions.app1.R;

/**
 * Created by Dale on 1/1/2015.
 */
public class Rest extends Action{

    statReq stamMax;

    public Rest(){
        super("Rest",
                "Yeah rest up why don't ya, not like you're in a fight or anything. Recovers stamina.",
                1,
                1000,
                Stat.MISC);




        HashMap<Stat, Integer> stats = new HashMap<>();
        stats.put(Stat.CUR_STAMINA, -1);
        stamMax = new statReq(stats);
        stamMax.atMost = true;

        requirements.add(stamMax);
        setReqDesc(stamMax, new Callable<String>() {
            public String call() {
                return "Cannot &#160;have &#160;full &#160;stamina";
            }
        });


        description.add(new Callable<String>() {
            public String call() {
                double realTime = getTimeNeeded(maxTimeNeeded, true)/1000.0;

                try {
                    GameManager gm = GameManager.getInstance();
                    State s = gm.getState();
                    if(s == null)
                        throw new RemovedException("No state");

                    User u = (User) gm.getState().getObjID(curUser);
                    ArrayList<ObjT> types = u.getTypePath();

                    //get mods
                    int staminaRecoverModC = 0;
                    double staminaRecoverModP = 1;
                    for (ObjT type : types) {
                        staminaRecoverModC += type.recModC();
                        staminaRecoverModP *= type.recModP();
                    }

                    //stamina recover
                    int recovery = (int) ((10 + staminaRecoverModC) * staminaRecoverModP);

                    return "Recovers &#160;"+recovery+" &#160;stamina &#160;after &#160;a &#160;" +realTime+ " &#160;second &#160;rest.";
                }

                //user is dead, game over
                catch(RemovedException re){
                    return "Recovers &#160;5 &#160;stamina &#160;after &#160;a &#160;" +realTime+ " &#160;second &#160;rest.";
                }
            }
        });

        cost = 0;
        setBuyReq("None");

        needsAskToContinue = true;

    }

    @Override
    public Action getCopy(int u){
        LogicCalc calc = new LogicCalc();
        Rest run = new Rest();
        calc.modAction(run, u);
        run.curUser = u;

        //update the maxStamina to be 1 less than the users max stamina. therefore if they have over this (aka = or greater than max stamina), they cant use rest
        try{
            GameManager gm = GameManager.getInstance();
            User user = (User) gm.getState().getObjID(run.curUser);

            run.stamMax.setStat(Stat.CUR_STAMINA, user.getStat(Stat.MAX_STAMINA)-1);
        }
        catch(RemovedException re){
            Log.e("Could not update stamMax in getCopy of Rest", "The user has been removed and the game should end");
        }

        return run;
    }


    @Override
    public Action getCopy(){
        return new Rest();
    }



    @Override
    public void useAction(){
        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();

        int realTime = getTimeNeeded(maxTimeNeeded, true);

        //this clears the timekeeper incase the user was using something else before
        tk.setCurAction(this);

        //add the action steps
        int runTime = actionSteps(realTime);

        //this processes to the next available time (hence the +1)
        gm.processTime(s.getTime(), runTime);
    }

    @Override
    public void mapClicked(Coord c){
        gameAct gc = GameManager.getInstance().getGameAct();

        gc.showMapInfo(null);

        gc.setObjects(c);
    }





    private int actionSteps(int realTime){
        try {
            GameManager gm = GameManager.getInstance();
            State s = gm.getState();
            timeKeeper tk = gm.getTimeline();
            Obj u = s.getObjID(curUser);

            //the first actionstep adds the resting objT to the user/state

            //some constants
            int runTime = s.getTime() + realTime - 1;

            int myId = GameManager.getInstance().getTimeline().getId();


            //create the rest objT
            NewObjT addRest = new NewObjT(Resting.class, new Object[]{curUser});
            addRest.setOwnerID(curUser);


            HashSet<Integer> narrationInvolves = new HashSet<>();
            narrationInvolves.add(curUser);
            AddNarration restNarrate = new AddNarration(narrationInvolves, u.name + " rests his sweepy wittle head", Stat.MISC);


            //create the continue action array and an empty array to stop action
            ArrayList<SubAction> firstActions = new ArrayList<>();
            firstActions.add(addRest);
            firstActions.add(restNarrate);
            ArrayList<SubAction> mtArray = new ArrayList<>();

            ActionStep runStart = new ActionStep(myId, "Rest", requirements, curUser, minSpeed, firstActions, mtArray, 0, Stat.MISC);
            tk.addAction(s.getTime(), runStart);


            //the second actionstep exists to either initiate the rest or cancel it

            //initiate the run
            AddEOT initiateRun = new AddEOT(addRest.getObjTID);

            //create the second continue action array
            ArrayList<SubAction> secondActions = new ArrayList<>();
            secondActions.add(initiateRun);


            //remove the run if stopped
            RemObjT remRun = new RemObjT(addRest.getObjTID);

            //create the second stop action array
            ArrayList<SubAction> stopActions = new ArrayList<>();
            stopActions.add(remRun);

            //create the second action step and add it
            ActionStep runEnd = new ActionStep(myId, "Rest", requirements, curUser, minSpeed, secondActions, stopActions, 0, Stat.MISC);
            tk.addAction(runTime, runEnd);

            return runTime + 1;
        }


        catch(RemovedException re){
            Log.e("User removed in rest>actionSteps", "this shouldnt happen");
            return -1;
        }
    }






    public int useWolf(){
        int realTime = getTimeNeeded(maxTimeNeeded, true);

        //setup action steps
        int restTime = actionSteps(realTime);

        //return the next available time
        return restTime;
    }



}
