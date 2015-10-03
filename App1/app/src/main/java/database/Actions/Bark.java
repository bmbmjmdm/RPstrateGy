package database.Actions;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import Managers.GameManager;
import Managers.Logic.LogicCalc;
import Managers.timeKeeper;
import Utilities.Callable;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Actions.ActionSteps.ActionStep;
import database.Actions.SubActions.AddEOT;
import database.Actions.SubActions.NewObjT;
import database.Actions.SubActions.RemObjT;
import database.Actions.SubActions.SubAction;
import database.Coord;
import database.ObjT.Barking;
import database.ObjT.ObjT;
import database.ObjT.Resting;
import database.Objs.PObjs.User;
import database.Requirements.Requirement;
import database.Requirements.statCost;
import database.Requirements.statReq;
import database.State;
import shenronproductions.app1.Activities.gameAct;

/**
 * Created by Dale on 1/1/2015.
 */
public class Bark extends Action{
    double intimidation = 1.07;


    public Bark(){
        super("Bark",
                "Bark for me, dog, bark! Bark! Bark!",
                20,
                200,
                Stat.WARRIOR);




        description.add(new Callable<String>() {
            public String call() {
                return "Barks, intimidating foes who hear.";
            }

        });
        HashMap<Stat, Integer> stats = new HashMap<>();
        stats.put(Stat.CUR_STAMINA, 2);
        final statCost stamReq = new statCost(stats);
        requirements.add(stamReq);
        setReqDesc(stamReq, new Callable<String>() {
            public String call() {
                return stamReq.getStat(Stat.CUR_STAMINA) + " &#160;stamina &#160;to &#160;bark";
            }
        });

        cost = 2;
        setBuyReq("2 Skill Points\nLevel &#160;1 &#160;Warrior");

    }

    @Override
    public Action getCopy(int u){
        LogicCalc calc = new LogicCalc();
        Bark run = new Bark();
        calc.modAction(run, u);
        run.curUser = u;

        return run;
    }


    @Override
    public Action getCopy(){
        return new Bark();
    }



    @Override
    public void useAction(){
        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();

        int realTime = getTimeNeeded(maxTimeNeeded, true);

        //this clears the timekeeper incase the user was using something else before
        tk.clearCurAction();

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
        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();

        //the first actionstep adds the resting objT to the user/state

        //some constants
        int runTime = s.getTime()+realTime-1;

        int myId =  GameManager.getInstance().getTimeline().getId();


        //create the rest objT
        NewObjT addRest = new NewObjT(Barking.class,  new Object[]{curUser, intimidation});
        addRest.setOwnerID(curUser);


        //create the continue action array and an empty array to stop action
        ArrayList<SubAction> firstActions = new ArrayList<>();
        firstActions.add(addRest);
        ArrayList<SubAction> mtArray = new ArrayList<>();

        ActionStep runStart = new ActionStep(myId, "Bark", requirements, curUser, minSpeed, firstActions, mtArray, 0, Stat.WARRIOR);
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
        ActionStep runEnd = new ActionStep(myId, "Bark", new ArrayList<Requirement>(), curUser, minSpeed, secondActions, stopActions, realTime/4, Stat.WARRIOR);
        tk.addAction(runTime, runEnd);

        return runTime+1;
    }






    public int useWolf(double strength){
        intimidation = Math.min(2, 1+(strength/4.0));

        int realTime = getTimeNeeded(maxTimeNeeded, true);

        //setup action steps
        int restTime = actionSteps(realTime);

        //return the next available time
        return restTime;
    }



}
