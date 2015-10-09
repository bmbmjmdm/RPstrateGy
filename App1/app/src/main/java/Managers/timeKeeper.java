package Managers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import database.Actions.Action;
import database.Actions.ActionSteps.ActionStep;
import shenronproductions.app1.gameUtils.Alert;

/**
 * Created by Dale on 4/9/2015.
 */
public class timeKeeper implements Serializable {
    HashMap<Integer, ArrayList<ActionStep>> actionsPast = new HashMap<>();

    HashMap<Integer, ArrayList<ActionStep>> actionsThisTurn = new HashMap<>();

    //user alerts can be added by anything at any time, however they will be removed immediately after the user is able to see them for the first time
    HashMap<Integer, ArrayList<Alert>> userAlerts = new HashMap<>();

    public int time = 1;

    HashMap<Integer, Action> curAction = new HashMap<Integer, Action>();

    HashMap<String, Double> randValues = new HashMap<>();

    //currently this is being used for ONLY Users. if this ever becomes not the case, update where this is used
    public int turnObjectID;

    public int nextTurnOID = -1;

    //this will block the user from using an action, but not show up in their alert dashboard
    public Alert curActionAlert = null;

    public boolean pausedAction = false;

    //not used in 1 player
    public boolean stoppedShort = false;

    /**
     * @Changed lastAction can only change if you use an action that exceeds your opponent's, or if you stop using an action and cause your opponent's future actions (if any) to be removed.
     * @note when this is changed, it is changed to the actual last action +1, this solves some off by 1 bugs
     * @Used if you exceed lastAction time, control switches to opponent.
     * @inclusive? inclusive, this IS THE TIME that the last actionStep is performed
     * @Note not used in 1 player
     */
    int lastAction = 1;

    public int getLastAction(){return lastAction;}

    /**
     * @Changed when you use an action for the first time in a turn, firstAction gets set
     * @Used when firstAction gets set, all time before it gets solidified
     * @inclusive? inclusive, this IS THE TIME that the first action is used at
     * @Note not used in 1 player
     */
    int firstAction = 1;

    //represents whether all of the current user's last turn actions have been passed
    //not used in 1 player
    public boolean passedLastTurn = false;







    //any time something needs to use a random value (aka Math.rand()) it should call this with a list of ALL id's involved as well as a label of what it is randomizing.
    //Note that this takes the current time from the state into consideration as well
    //This is used to ensure that all rand() used during state resolution is consistent throughout resolutions of the same event
    public Double getRand(List<Integer> values, String label){
        String key = "";
        key = key.concat(label);
        key = key.concat(" "+ GameManager.getInstance().getState().getTime());

        Collections.sort(values);
        for(Integer i: values){
            key = key.concat(" "+i);
        }

        if(!randValues.containsKey(key)){
            randValues.put(key, Math.random());
        }

        return randValues.get(key);
    }



    public HashMap<Integer, ArrayList<ActionStep>> getActionsPast() {
        return actionsPast;
    }

    public HashMap<Integer, ArrayList<ActionStep>> getActionsThisTurn() {
        return actionsThisTurn;
    }

    public void addAction(int time, ActionStep sa) {
        ArrayList<ActionStep> acts = actionsThisTurn.get(time);
        if(acts == null){
            acts = new ArrayList<>();
        }
        if(!acts.contains(sa))
            acts.add(sa);
        actionsThisTurn.put(time, acts);
    }

    //clears all actions in actionsThisTurn up to upTo, not inclusive
    public void flush(int upTo) {
        Iterator<Integer> it= actionsThisTurn.keySet().iterator();
        while(it.hasNext()){
            int d = it.next();
            if(d < upTo) {
                ArrayList acts = actionsPast.get(d);
                if (acts == null)
                    acts = new ArrayList<>();
                acts.addAll(actionsThisTurn.get(d));
                actionsPast.put(d, acts);
                it.remove();
            }
        }
    }


    //This sets curAction to null so that when gameView asks for the current action when trying to refresh its action view, it clears this pre-emptively for the next action
    public Action getCurAction() {
        return curAction.get(turnObjectID);
    }

    public void clearCurAction(){
        curAction.remove(turnObjectID);
    }

    public void clearCurAction(int userId){
        curAction.remove(userId);
    }

    public void setCurAction(Action newAction) {
        curAction.put(turnObjectID, newAction);
    }




    //swaps the turnObjectID and nextTurnOID
    public void nextTurnUser(){
        int holder = turnObjectID;
        turnObjectID = nextTurnOID;
        nextTurnOID = holder;
    }





    public void clearUserAlerts(){
        userAlerts.remove(turnObjectID);
    }


    //this will also clear the cur user's alerts
    public ArrayList<Alert> getUserAlerts(){
        ArrayList<Alert> these = userAlerts.get(turnObjectID);
        if(these == null){
            these = new ArrayList<>();
        }

        return these;
    }


    //used if you don't know who is a player obj
    public void addAlertIfUser(int who, Alert what){
        if(who == turnObjectID)
            addAlert(who, what);
        if(who == nextTurnOID)
            addAlert(who, what);

    }

    public void addAlert(int who, Alert what){
        ArrayList<Alert> these = userAlerts.get(who);
        if(these == null){
            these = new ArrayList<>();
        }

        these.add(what);

        userAlerts.put(who, these);
    }




    //non-inclusive, will not remove action steps at curTime
    //used when an action is stopped
    public void clearFutureActionSteps(int userID, int curTime){
        Iterator<Integer> it = actionsThisTurn.keySet().iterator();

        while(it.hasNext()){
            int i = it.next();
            if(i<= curTime)
                continue;

            Iterator<ActionStep> arraySteps = actionsThisTurn.get(i).iterator();
            while(arraySteps.hasNext()){
                ActionStep ap = arraySteps.next();
                if(ap.userId == userID){
                    arraySteps.remove();
                }
            }
        }
    }

    //non-inclusive, will not remove action steps at curTime
    //used when the current user changes their moves from last turn, so we remove all future action steps of the other player except for the one they are currently using
    public void clearFutureActionStepsExcept(int userID, int curTime, int actionID){
        Iterator<Integer> it = actionsThisTurn.keySet().iterator();

        while(it.hasNext()){
            int i = it.next();
            if(i<= curTime)
                continue;

            Iterator<ActionStep> arraySteps = actionsThisTurn.get(i).iterator();
            while(arraySteps.hasNext()){
                ActionStep ap = arraySteps.next();
                if(ap.userId == userID){
                    if(ap.id != actionID) {
                        arraySteps.remove();
                    }
                }
            }
        }
    }

    //non-inclusive, does not count an action step at curTime as last one to process
    public int timeSinceLastStep(int actionID, int curTime){
        Iterator<Integer> it = actionsThisTurn.keySet().iterator();
        int highest = 0;
        boolean found = false;

        while(it.hasNext()){
            int i = it.next();
            if(i>= curTime)
                continue;

            for(ActionStep ap: actionsThisTurn.get(i)){
                if(ap.id == actionID){
                    found = true;
                    if(i > highest)
                        highest = i;
                }
            }
        }

        if(found){
            return curTime - highest;
        }
        else
            return 0;
    }

    //inclusive, will return an action step if it is at curTime
    public ActionStep getNextAS(int curTime, int userId){
        Iterator<Integer> it = actionsThisTurn.keySet().iterator();
        int lowest = Integer.MAX_VALUE;
        ActionStep retMe = null;

        while(it.hasNext()){
            int i = it.next();
            if(i< curTime)
                continue;

            for(ActionStep ap: actionsThisTurn.get(i)){
                if(ap.userId == userId){
                    if(i < lowest) {
                        lowest = i;
                        retMe = ap;
                    }
                }
            }
        }

        return retMe;
    }


    //inclusive, will return an action step time if it is at curTime. returns a negative if there is none
    public int getNextASTime(int curTime, int userId){
        Iterator<Integer> it = actionsThisTurn.keySet().iterator();
        int lowest = Integer.MAX_VALUE;
        int retMe = -1;

        while(it.hasNext()){
            int i = it.next();
            if(i< curTime)
                continue;

            for(ActionStep ap: actionsThisTurn.get(i)){
                if(ap.userId == userId){
                    if(i < lowest) {
                        lowest = i;
                        retMe = i;
                    }
                }
            }
        }

        return retMe;
    }


    //gets last ActionStep done by the user's time in the timeline
    //returns a negative if none are found
    public int getLastASTime(int userId){
        Iterator<Integer> it = actionsThisTurn.keySet().iterator();
        int highest = Integer.MIN_VALUE;
        int retMe = -1;

        while(it.hasNext()){
            int i = it.next();
            for(ActionStep ap: actionsThisTurn.get(i)){
                if(ap.userId == userId){
                    if(i > highest) {
                        highest = i;
                        retMe = i;
                    }
                }
            }
        }

        return retMe;
    }


    /**
     * Updates the actionStep object as well as all of its user's future actionSteps in the timeline.
     * Also possibly updates the firstAction, lastAction, and passedLastTurn variables
     * @param actionStep the actionStep that needs to be stopped
     * @param curTime the current time
     * @return whether the curUser has surpassed their opponents last action as a result of actionStep being stopped
     */
    public boolean stopAction(ActionStep actionStep, int curTime){
        actionStep.stopped = true;

        clearCurAction(actionStep.userId);

        //clear any and all future ActionSteps of actionStep's user, including actionStep (-1 because actionStep could be at curTime)
        clearFutureActionSteps(actionStep.userId, curTime-1);

        //find out how much time it needs to stop and add it to the timeline at the proper stopping time
        actionStep.timeElaps = timeSinceLastStep(actionStep.id, curTime);
        int stopTimeUsed = curTime + actionStep.stopTime();
        addAction(stopTimeUsed, actionStep);

        //update times affected based on current turnObjectID
        //find out if the current user has passed their opponent's last action due to actionStep being stopped
        boolean passed;

        //actionStep is owned by the current user
        if(turnObjectID == actionStep.userId){

            //the user is creating their new turn
            if(passedLastTurn){
                passed = didPass(stopTimeUsed);
            }

            //the user has modified their previous turn
            else{
                passed = updateTimesChange(curTime, stopTimeUsed);
            }
        }

        //its the opponent's. update time
        else{
            updateTimesStopped(stopTimeUsed);
            passed = didPass(stopTimeUsed);
        }

        return passed;
    }

    //sees if the cur user has passed the opponent as a result of an ActionStep being stopped.
    //updates lastAction if so and returns whether it passed or not
    private boolean didPass(int stopTimeUsed){
        int lastActionByCurUser = getLastASTime(turnObjectID);
        return lastActionByCurUser > stopTimeUsed;
    }

    /**
     * this is called when the curUser is viewing their previous turn's actions and decides to manually stop
     * @param curTime is the current time
     * @param stoppingAt the time that the previous turn's actionStep is being stopped at
     * @return a boolean indicating whether the user has surpassed the opponent
     */
    public boolean updateTimesChange(int curTime, int stoppingAt){
        //clear all future actions for the opponent user, except for the one they're currently using
        ActionStep opponentCurrentAction = getNextAS(curTime, nextTurnOID);

        if(opponentCurrentAction !=  null) {
            clearFutureActionStepsExcept(nextTurnOID, curTime, opponentCurrentAction.id);
        }

        int opponentStopsAt = getNextASTime(curTime, nextTurnOID);


        firstAction = curTime;

        //now see if we surpassed the opponent
        if(stoppingAt > opponentStopsAt){
            //if so dont update times, leave that for gameManager
            return true;
        }

        else{
            //if not update times
            lastAction = opponentStopsAt+1;

            passedLastTurn = true;

            return false;
        }
    }


    /**
     * this is called when the user has passed all of the time from their previous turn without stopping
     * @param curTime is the current time
     */
    public void updateTimesNoChange(int curTime){
        firstAction = curTime;
        passedLastTurn = true;
    }


    /**
     * this is called when the curUser surpasses lastAction with an actionStep
     * @param newLastTime is the last actionStep the curUser has set up
     */
    public void updateTimesSurpass(int newLastTime){
        lastAction = newLastTime+1;
        passedLastTurn = false;
    }


    /**
     * this is called when the player who is NOT curUser has one of their actionSteps stopped because it wasn't able to complete, in which case their future action steps are also removed
     * this does not include action steps that were already stopped (only newly stopped ones)
     * @param newLastTime is the time at which the stopped actionStep is scheduled in the timeline for
     */
    public void updateTimesStopped(int newLastTime){
        lastAction = newLastTime+1;
    }





    int idCounter = 0;
    public int getId(){
        idCounter++;
        return idCounter;
    }

}
