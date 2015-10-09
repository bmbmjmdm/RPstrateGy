package Managers;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import Utilities.Callable;
import Utilities.Constants;
import Utilities.DeepCopy;
import Utilities.ProcessCallable;
import Utilities.RemovedException;
import Utilities.StateKit;
import Utilities.UnableException;
import database.Actions.ActionSteps.ActionStep;
import database.Levels.Level;
import database.ObjT.ObjT;
import database.State;
import Managers.Logic.LogicCalc;
import shenronproductions.app1.Activities.gameAct;
import shenronproductions.app1.Activities.offlineAct;
import shenronproductions.app1.gameUtils.Alert;
import shenronproductions.app1.R;

/**
 * Created by Dale on 12/31/2014.
 */
public final class GameManager {
    String fileloc = null;
    private static GameManager inst = null;
    private State curState = null;
    private timeKeeper timeline = null;
    private gameAct gc = null;
    public offlineAct offAct = null;
    private State oldState = null;
    boolean gameOver = false;

    boolean copying = false;
    Object lockCopy = new Object();
    boolean savingCur = false;
    Object lockSave = new Object();
    boolean waiting = false;
    Object waitLock = new Object();
    boolean processing = false;
    Object procLock = new Object();
    boolean saveStopProcessing = false;
    Object saveStopProcLock = new Object();

    public boolean stopProcessing = false;
    boolean noChange = false;


    Runnable lastCallback = new Runnable() {
        public void run() {
            synchronized (gc.visionLock) {
                synchronized(waitLock){
                    while (waiting) {
                        try {
                            waitLock.wait();
                        } catch (InterruptedException ie) {
                            Log.e("Interupted while waiting on waitlock in lastCallback runnable", "going back into loop");
                        }
                    }
                }
                if(!gc.isDestroyed())
                    gc.updateTime();

                //skipped when there are no times to process, so GM just goes ahead a little without processing anything
                if(!noChange) {
                    if(!gc.isDestroyed()) {
                        gc.newVision();
                        if(gc.visionChanged) {
                            gc.updateCoords();
                        }
                    }
                }

                noChange = false;
                gc.waitOnVision = false;;
                gc.visionLock.notify();
            }
        }

    };


    //these are needed to sort objT and actionSteps by descending speed
    Comparator<ActionStep> compareSA = new Comparator<ActionStep>() {
        @Override
        public int compare(ActionStep lhs, ActionStep rhs) {
            return rhs.getSpeed() - lhs.getSpeed();
        }
    };

    Comparator<Integer> compareOT = new Comparator<Integer>() {
        @Override
        public int compare(Integer lhsI, Integer rhsI) {
            State s = GameManager.getInstance().getState();
            try{
                ObjT lhs = s.getObjT(lhsI);
                try{
                    ObjT rhs = s.getObjT(rhsI);

                    return rhs.speed() - lhs.speed();
                }
                catch(RemovedException e){ return 1;}
            }
            catch(RemovedException e){ return -1;}

        }
    };


    public void oldGame(String fileLoc) throws IOException, ClassNotFoundException{
        fileloc = fileLoc;
        curState = loadState();
        timeline = loadTimeline();
        new CopyTask(false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //Only done for new games
    public void newGame(StateKit s) throws IOException{
        fileloc = Environment.getExternalStorageDirectory() + "/RPstrateGy/friendly/" + UserProfile.getInstance().curChar.charId + "/" + UserProfile.getInstance().getId();
        timeline = new timeKeeper();
        prepareStateLoop(s);
        new CopyTask(false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        saveStateName();
    }

    //Only done for new games
    public void newGame(Level lev) throws IOException{
        fileloc = Environment.getExternalStorageDirectory() + "/RPstrateGy/cpu/" + UserProfile.getInstance().curChar.charId + "/" + lev.fileName;
        timeline = new timeKeeper();
        prepareStateLoop(lev);
        oldState = curState;
    }

    public void oldGame(Level lev) throws IOException, ClassNotFoundException{
        fileloc = Environment.getExternalStorageDirectory() + "/RPstrateGy/cpu/" + UserProfile.getInstance().curChar.charId + "/" + lev.fileName;
        curState = loadState();
        timeline = loadTimeline();
        oldState = curState;
    }


    private void prepareStateLoop(StateKit sK){
        try {
            State s = new State(sK);
            curState = s;
            s.prepare(sK);
        }
        catch(UnableException e){
            Log.e("Preparation of state failed", e.getMessage());
            prepareStateLoop(sK);
        }
    }

    private void prepareStateLoop(Level lev){
        try {
            State s = new State(lev);
            curState = s;
            s.prepare(lev);
        }
        catch(UnableException e){
            Log.e("Preparation of state failed", e.getMessage());
            prepareStateLoop(lev);
        }
    }


    //********************************************************************** Accessors
    public static GameManager getInstance(){
        if(inst == null){
            inst = new GameManager();
        }

        return inst;
    }

    public timeKeeper getTimeline(){
        return timeline;
    }

    public State getState(){
        return curState;
    }

    public gameAct getGameAct(){
        return gc;
    }

    public void setGameAct(gameAct game){
        gc = game;
    }

    public void clearManager(){
        new ClearTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void saveGame(){
        if(!savingCur) {
            new SaveTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void clearContinue(){
        curState = null;
        timeline = null;
        fileloc = null;
        gc = null;
        oldState = null;
        gameOver = false;
    }








    //*********************************************************************** Processors


    //TODO WARNING! DO NOT CALL processTime AND THEN DO OTHER STUFF! processTime must be the LAST thing anything does!


    //processes from-to, inclusive from exclusive to
    public void processTime(int fromTime, int toTime){
        ProcessTask pt = new ProcessTask();
        pt.fromTime = fromTime;
        pt.toTime = toTime;
        pt.onPost = new ProcessCallable(){
            public void call(boolean passed){
                if(!gc.isDestroyed())
                    gc.setUpGame();
            }
        };
        pt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //processes from-to, inclusive from exclusive to
    public void processTimeBeginning(){

        ProcessTask pt = new ProcessTask();
        pt.fromTime = curState.getTime();
        pt.toTime = timeline.time;
        pt.onPost = new ProcessCallable(){
            public void call(boolean passed){
                if(!gc.isDestroyed()) {
                    gc.setUpGame();
                    gc.newTurnPhase(false);
                }
            }
        };
        pt.canStop = false;
        pt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //this will attempt to process up to the current user's last actionstep
    //it will also update the timeline, state, and gamemanager accordingly when it is done processing (see pastTurnProcessed)
    public void processPastTurn() {
        int lastAction = timeline.getLastASTime(timeline.turnObjectID);

        //normal case
        if(lastAction > 0) {
            ProcessTask pt = new ProcessTask();
            pt.fromTime = curState.getTime();
            pt.toTime = lastAction + 1;

            pt.onPost = new ProcessCallable() {
                public void call(boolean passed) {
                    pastTurnProcessed(passed);
                    if(!gc.isDestroyed())
                        gc.setUpGame();
                }
            };
            pt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        //it is the first turn
        else{
            pastFirstTurnProcessed();
        }
    }


    //flushes the timeline, updates firstAction in timeline, copies the new solidified state, and creates a new narration block symbolizing that the old one is solidified
    //passed is whether the past turn processing reuslted in the current user passing the opponents last action
    public void pastTurnProcessed(boolean passed){
        if(!timeline.pausedAction) {
            curState.newNarration();
            if(!timeline.stoppedShort) {
                timeline.updateTimesNoChange(curState.getTime());
            }
            timeline.flush(timeline.firstAction);
            new CopyTask(passed).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            if(!passed) {
                if(!gc.isDestroyed())
                    gc.showTurnMessage();
            }
        }
    }

    //similar to above, but the special case of the first turn
    public void pastFirstTurnProcessed(){
        timeline.updateTimesNoChange(curState.getTime());
        if(!gc.isDestroyed())
            gc.showTurnMessage();
        new CopyTask(false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    //called when processTime (or rarely processPastTurn) passes the opponents last action
    public void passedOpponent() {

        //if past turn processing passes opponent, then pastTurnProcessed() is called just before this, so wait for the copy to finish
        synchronized(lockCopy) {
            try {
                while (copying)
                   lockCopy.wait();
            } catch (InterruptedException e) {
                Log.e("Interrupted wait on lockCopy", "passedOpponent>GameManger");
            }
        }

        //dont update the map while we are reverting
        gc.noUpdateMap = true;

        //set the lastAction variable appropriately
        timeline.updateTimesSurpass(timeline.getLastASTime(timeline.turnObjectID));

        //revert
        curState = oldState;

        //change turnplayer and turnObjectID
        timeline.nextTurnUser();

        //reset the time so that processTimeBeginning doesn't go too far
        timeline.time = curState.getTime();

        //setup
        if(!gc.isDestroyed()) {
            gc.setUpGame();
            gc.newTurnPhase(true);
        }
    }




    //checks to see if the user is currently using an action
    //if they are it gives them the option to stop/continue it, processing these choices appropriately
    //this is only static because of a wierd error with anonymous classes. DO NOT ACTUALLY TREAT THIS LIKE A STATIC METHOD
    private static void stopContinueAction(final int fromFinal, final int toFinal){
        GameManager gm = GameManager.getInstance();
        final boolean onePlayer = gm.getState().onePlayer;
        final ActionStep currentlyUsing = gm.timeline.getNextAS(fromFinal, gm.timeline.turnObjectID);

        if(currentlyUsing != null) {
            gm.timeline.pausedAction = true;

            //if the next action is stopped, the user does not have the option to stop it
            boolean stopped = currentlyUsing.stopped;
            String alertTitle;
            String alertMessage;

            if(stopped){
                alertTitle = "You are currently stopping an action!";
                alertMessage = "Not much you can do about it now.";
            }
            else{
                alertTitle = "You are currently using an action!";
                alertMessage = "Do you want to continue with the action " +currentlyUsing.name +"?";
            }

            Alert newAlert = new Alert(alertTitle, alertMessage, Constants.STOPPED_ACTION_ID);

            //give the option to continue as if nothing happened
            newAlert.addButton("Continue", new Callable<Void>() {
                @Override
                public Void call() {
                    GameManager gm = GameManager.getInstance();

                    gm.timeline.curActionAlert = null;
                    gm.timeline.pausedAction = false;

                    //if its one player, we never have to processPastTurn
                    if (gm.timeline.passedLastTurn || onePlayer) {
                        gm.processTime(fromFinal, toFinal);
                    } else {
                        gm.processPastTurn();
                    }

                    return null;
                }
            });

            if(!stopped) {
                newAlert.addButton("Stop", new Callable<Void>() {
                    @Override
                    public Void call() {
                        GameManager gm = GameManager.getInstance();

                        gm.timeline.curActionAlert = null;
                        gm.timeline.pausedAction = false;

                        int stopTimeUsed;
                        boolean passedLastTurn = gm.timeline.passedLastTurn;

                        //wait incase we are saving
                        gm.new ProgressDialogTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        synchronized (gm.lockSave){
                            while(gm.savingCur) {
                                try {
                                    gm.lockSave.wait();
                                }
                                catch (InterruptedException e) {
                                    Log.e("Interrupted wait on save lock", "stopContinueAction>GameManger");
                                }
                            }

                            gm.timeline.stoppedShort = true;

                            //move the next action step to now + the time it needs to complete and remove all actions steps after it
                            //if user is viewing previous turn, make proper updates to firstTime and etc
                            gm.timeline.stopAction(currentlyUsing, fromFinal);

                            stopTimeUsed = gm.timeline.getLastASTime(gm.timeline.turnObjectID);
                        }

                        //be sure to processPastTurn if it is still in that phase
                        //if it is one player mode we never have to processPastTurn
                        if(passedLastTurn || onePlayer) {
                            gm.processTime(fromFinal, stopTimeUsed + 1);
                        }
                        else{
                            gm.processPastTurn();
                        }

                        //nothing
                        return null;
                    }
                });
            }

            gm.timeline.curActionAlert = newAlert;
        }
    }


    //this applies all the changes from subActions and objT to the state
    //inclusive from, exclusive to.
    private boolean applyChanges(int fromTime, int toTime){
        LogicCalc calc = new LogicCalc();
        boolean passed = false;
        timeline.clearUserAlerts();

        if(toTime > timeline.lastAction) {
            //we passed the opponent, process until the end of the current "turn"
            toTime = timeline.lastAction;
            passed = true;

        }

        //loop until there are no more changes that fall between fromTime and toTime
        while(fromTime < toTime) {

            //if you are trying to save, stop processing asap
            synchronized(saveStopProcLock) {
                if (saveStopProcessing){
                    stopProcessing = true;
                    saveStopProcessing = false;
                }
            }

            HashSet<Integer> allTimes = new HashSet<>();

            //get all subaction times
            HashMap<Integer, ArrayList<ActionStep>> mapAct = timeline.getActionsThisTurn();
            allTimes.addAll(mapAct.keySet());

            //get all objT times
            HashMap<Integer, ArrayList<Integer>> mapObjt = curState.getEndOfTurnOT();
            allTimes.addAll(mapObjt.keySet());

            allTimes.remove(-1);


            if(stopProcessing){

                //the user has stopped processing, check to see if they were using an action and give them options to continue/stop if they were
                stopContinueAction(fromTime, toTime);

                if(fromTime <= timeline.lastAction) {
                    passed = false;
                }

                //exiting this function cleanly
                toTime = fromTime;
            }


            //we are not stopped, continue with processing time
            else{
                //check to see if there is something we can do (actionstep or objt) at the current time (fromTime)
                if(allTimes.contains(fromTime)){
                    synchronized (gc.visionLock) {
                            while (gc.waitOnVision) {
                                try {
                                    gc.visionLock.wait();

                                } catch (InterruptedException e) {
                                    Log.e("Interrupted wait on visionLock 1", "applyChanges>GameManger");
                                }
                            }


                        gc.waitOnVision = true;
                            /*if (gc.vision != null)
                                gc.curUser.setVision(gc.vision);*/

                        curState.setTime(fromTime);

                        //retrieve any subactions at this time
                        ArrayList<ActionStep> actions = mapAct.get(fromTime);
                        if (actions != null) {
                            //sort the list based on speed
                            Collections.sort(actions, compareSA);

                            //process all subactions (if any)
                            for (ActionStep sa : actions) {
                                //implies that this was stopped in a previous run, so it is not being performed currently via ui. do not need to exit, just run stop
                                if(sa.stopped){
                                    sa.stopUse();
                                }
                                //otherwise try to use it successfully, if you cant run stop and record it and exit cleanly
                                else {
                                    boolean success = sa.useNow();

                                    //the action failed
                                    if (!success) {
                                        //move the next action step to now + the time it needs to complete and remove all actions steps after it
                                        boolean stopPassed = timeline.stopAction(sa, fromTime);

                                        //if it is currently the time that it should be stopped, we call stopUse now because otherwise it will never be called
                                        int stopTimeUsed = timeline.getLastASTime(sa.userId);
                                        if(fromTime == stopTimeUsed) {
                                            sa.stopUse();
                                        }

                                        //see if the stopepd action caused the current user to pass the opponents last move
                                        if(stopPassed){
                                            //we did pass the opponent, process until the end of the current "turn"
                                            toTime = timeline.lastAction;
                                            passed = true;
                                        }

                                        else {
                                            //we did not pass opponent, everything proceeds as normal
                                            //if the current user is the one who's action was stopped, make sure we don't process past it
                                            if(timeline.turnObjectID == sa.userId) {
                                                toTime = stopTimeUsed + 1;
                                                Alert stoppedAlert = new Alert("Action failed!", "You failed to complete the action "+sa.name, Constants.FAILED_ACTION_ID);
                                                stoppedAlert.addClearButton();
                                                timeline.addAlert(timeline.turnObjectID, stoppedAlert);
                                            }

                                            //if the current user would have passed the opponent, however an action being stopped changes that, we want to keep going
                                            passed = false;
                                        }
                                    }
                                }

                                //process all objT that need to be processed at any change
                                ArrayList<Integer> constObjT = mapObjt.get(-1);
                                Collections.sort(constObjT, compareOT);
                                for (Integer constID : constObjT) {
                                    calc.applyObjType(constID);
                                }
                            }
                        }

                        //retrieve any objT at this time
                        ArrayList<Integer> types = mapObjt.get(fromTime);
                        if (types != null) {
                            //sort the list based on speed
                            Collections.sort(types, compareOT);

                            //process all objT (if any)
                            for (Integer id : types) {
                                calc.applyObjType(id);

                                //process all objT that need to be processed at any change
                                ArrayList<Integer> constObjT = mapObjt.get(-1);
                                Collections.sort(constObjT, compareOT);
                                for (Integer constID : constObjT) {
                                    calc.applyObjType(constID);
                                }

                            }
                            //these objT are never used again, so this just saves memory
                            mapObjt.remove(fromTime);
                        }


                        //finished processing the time curI, wrap up and move on
                        gc.curUser.minimumVision();
                        gc.mainHandler.removeCallbacks(lastCallback);
                        gc.mainHandler.post(lastCallback);

                        fromTime++;

                        continue;
                    }

                }

                //the current time does not have any action steps or objT to process, update timebar and move on
                else{
                    synchronized (gc.visionLock) {
                        try {
                            while (gc.waitOnVision)
                                gc.visionLock.wait();
                        } catch (InterruptedException e) {
                            Log.e("Interrupted wait on visionLock 2", "applyChanges>GameManger");
                        }

                        fromTime++;
                        //this makes time go by at 2 ms each iteration, assuming there is nothing happening at fromTime+1

                        for(int i = 0; i <5; i++){
                            //this makes time go by 1 ms faster
                            if(!(mapObjt.containsKey(fromTime) || mapAct.containsKey(fromTime))) {
                                if (fromTime < toTime) {
                                    fromTime++;
                                }

                                else
                                    break;
                            }

                            else
                                break;
                        }

                        curState.setTime(fromTime);
                        gc.waitOnVision = true;
                        noChange = true;

                        gc.mainHandler.removeCallbacks(lastCallback);
                        gc.mainHandler.post(lastCallback);

                        continue;
                    }
                }
            }


        }

        //fromTime has exceeded toTime either naturally or through stopProcessing
        curState.setTime(toTime);
        synchronized (gc.visionLock) {
            gc.curUser.minimumVision();

            if(gc.waitOnVision) {
                gc.mainHandler.removeCallbacks(lastCallback);
                gc.waitOnVision = false;
            }
        }
        timeline.time = toTime;

        return passed;
    }







    //this applies all the changes from subActions and objT to the state
    //inclusive from, exclusive to.
    //this also adds the level's actions to the timeline
    private void applyChangesSinglePlayer(int fromTime, int toTime){
        LogicCalc calc = new LogicCalc();
        timeline.clearUserAlerts();

        //loop until there are no more changes that fall between fromTime and toTime
        while(fromTime < toTime) {
            curState.setTime(fromTime);

            //if you are trying to save, stop processing asap
            synchronized(saveStopProcLock) {
                if (saveStopProcessing){
                    stopProcessing = true;
                    saveStopProcessing = false;
                }
            }

            //check to see if the game is over
            Level curLevel = curState.level;
            int over = curLevel.isOver();
            if(over != 0){
                gameOver = true;
                gc.gameOver = over;
                return;
            }

            //if the current level is supposed to have a turn now, it happens here (very similar to when a player would add their actions)
            if(fromTime == curLevel.nextTurn){

                //wait on vision first
                synchronized (gc.visionLock) {
                    while (gc.waitOnVision) {
                        try {
                            gc.visionLock.wait();

                        } catch (InterruptedException e) {
                            Log.e("Interrupted wait on visionLock 1", "applyChangesOnePlayer>GameManger");
                        }
                    }

                    //do level
                    curLevel.doTurn();
                }
            }

            HashSet<Integer> allTimes = new HashSet<>();

            //get all subaction times
            HashMap<Integer, ArrayList<ActionStep>> mapAct = timeline.getActionsThisTurn();
            allTimes.addAll(mapAct.keySet());

            //get all objT times
            HashMap<Integer, ArrayList<Integer>> mapObjt = curState.getEndOfTurnOT();
            allTimes.addAll(mapObjt.keySet());

            allTimes.remove(-1);


            if(stopProcessing){

                //the user has stopped processing, check to see if they were using an action and give them options to continue/stop if they were
                stopContinueAction(fromTime, toTime);

                //exiting this function cleanly
                toTime = fromTime;
            }


            //we are not stopped, continue with processing time
            else{
                //check to see if there is something we can do (actionstep or objt) at the current time (fromTime)
                if(allTimes.contains(fromTime)){
                    synchronized (gc.visionLock) {
                        while (gc.waitOnVision) {
                            try {
                                gc.visionLock.wait();

                            } catch (InterruptedException e) {
                                Log.e("Interrupted wait on visionLock 2", "applyChangesOnePlayer>GameManger");
                            }
                        }


                        gc.waitOnVision = true;
                            /*if (gc.vision != null)
                                gc.curUser.setVision(gc.vision);*/

                        //retrieve any subactions at this time
                        ArrayList<ActionStep> actions = mapAct.get(fromTime);
                        if (actions != null) {
                            //sort the list based on speed
                            Collections.sort(actions, compareSA);

                            //process all subactions (if any)
                            for (ActionStep sa : actions) {
                                //implies that this was stopped in a previous run, so it is not being performed currently via ui. do not need to exit, just run stop
                                if(sa.stopped){
                                    sa.stopUse();
                                }
                                //otherwise try to use it successfully, if you cant run stop exit cleanly
                                else {
                                    boolean success = sa.useNow();

                                    //the action failed
                                    if (!success) {
                                        //move the next action step to now + the time it needs to complete and remove all actions steps after it
                                        timeline.stopAction(sa, fromTime);

                                        //if it is currently the time that it should be stopped, we call stopUse now because otherwise it will never be called
                                        int stopTimeUsed = timeline.getLastASTime(sa.userId);
                                        if(fromTime == stopTimeUsed) {
                                            sa.stopUse();
                                        }

                                        //if the current user is the one who's action was stopped, make sure we don't process past it
                                        if(timeline.turnObjectID == sa.userId) {
                                            toTime = stopTimeUsed + 1;
                                            timeline.curActionAlert = new Alert("Action failed!", "You failed to complete the action "+sa.name, Constants.FAILED_ACTION_ID);
                                            timeline.curActionAlert.addClearButton();
                                        }

                                    }
                                }

                                //process all objT that need to be processed at any change
                                ArrayList<Integer> constObjT = mapObjt.get(-1);
                                Collections.sort(constObjT, compareOT);
                                for (Integer constID : constObjT) {
                                    calc.applyObjType(constID);
                                }
                            }
                        }

                        //retrieve any objT at this time
                        ArrayList<Integer> types = mapObjt.get(fromTime);
                        if (types != null) {
                            //sort the list based on speed
                            Collections.sort(types, compareOT);

                            //process all objT (if any)
                            for (Integer id : types) {
                                calc.applyObjType(id);

                                //process all objT that need to be processed at any change
                                ArrayList<Integer> constObjT = mapObjt.get(-1);
                                Collections.sort(constObjT, compareOT);
                                for (Integer constID : constObjT) {
                                    calc.applyObjType(constID);
                                }

                            }
                            //these objT are never used again, so this just saves memory
                            mapObjt.remove(fromTime);
                        }


                        //finished processing the time curI, wrap up and move on
                        gc.curUser.minimumVision();
                        gc.mainHandler.removeCallbacks(lastCallback);
                        gc.mainHandler.post(lastCallback);

                        fromTime++;

                        continue;
                    }

                }

                //the current time does not have any action steps or objT to process, update timebar and move on
                else{
                    synchronized (gc.visionLock) {
                        try {
                            while (gc.waitOnVision)
                                gc.visionLock.wait();
                        } catch (InterruptedException e) {
                            Log.e("Interrupted wait on visionLock 3", "applyChangesOnePlayer>GameManger");
                        }

                        fromTime++;

                        for(int i = 0; i < 5; i++) {
                            //this makes time go by at 1 ms faster
                            if (!(mapObjt.containsKey(fromTime) || mapAct.containsKey(fromTime))) {
                                if (fromTime < toTime) {
                                    if (fromTime != curLevel.nextTurn) {
                                        fromTime++;
                                    }

                                    else
                                        break;
                                }

                                else
                                    break;
                            }

                            else
                                break;
                        }

                        curState.setTime(fromTime);
                        gc.waitOnVision = true;
                        noChange = true;

                        gc.mainHandler.removeCallbacks(lastCallback);
                        gc.mainHandler.post(lastCallback);

                        continue;
                    }
                }
            }


        }

        //fromTime has exceeded toTime either naturally or through stopProcessing
        curState.setTime(toTime);
        synchronized (gc.visionLock) {
            gc.curUser.minimumVision();

            if(gc.waitOnVision) {
                gc.mainHandler.removeCallbacks(lastCallback);
                gc.waitOnVision = false;
            }
        }
        timeline.time = toTime;

        curState.newNarration();
    }








    //*****************************************************************************************State File Handlers

    private void saveState() throws IOException {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(fileloc);
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
            State myS = oldState;
            oos.writeObject(myS);
            oos.close();
            bos.close();
            fos.close();
        }
        catch(IOException e){
            try {
                if (fos != null)
                    fos.close();
            }
            catch(Exception p){}

            try {
                if (bos != null)
                    bos.close();
            }
            catch(Exception p){}

            try {
                if (oos != null)
                    oos.close();
            }
            catch(Exception p){}

            throw e;
        }
    }

    public State loadState() throws IOException, ClassNotFoundException{
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream ois = null;
        State s;
        try {
            fis = new FileInputStream(fileloc);
            bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);
            s = (State) ois.readObject();
            ois.close();
            bis.close();
            fis.close();
            return s;
        }
        catch(IOException | ClassNotFoundException e){
            try {
                if (fis != null)
                    fis.close();
            }
            catch(Exception p){}

            try {
                if (bis != null)
                    bis.close();
            }
            catch(Exception p){}

            try {
                if (ois != null)
                    ois.close();
            }
            catch(Exception p){}

            throw e;
        }
    }




    private void saveStateName() throws IOException {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(fileloc+"Name");
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(curState.name);
            oos.close();
            bos.close();
            fos.close();
        }
        catch(IOException e){
            try {
                if (fos != null)
                    fos.close();
            }
            catch(Exception p){}

            try {
                if (bos != null)
                    bos.close();
            }
            catch(Exception p){}

            try {
                if (oos != null)
                    oos.close();
            }
            catch(Exception p){}

            throw e;
        }
    }


    private void deleteFiles()  {
        synchronized(lockSave) {
            while (savingCur) {
                try {
                    lockSave.wait();
                } catch (InterruptedException e) {
                    Log.e("Interrupted, resuming to wait", "deleteFiles");
                }
            }
        }

        String state = fileloc;
        String timeline = fileloc+"Timeline";
        try{
            File stateFile = new File(state);
            stateFile.delete();
        }
        catch(Exception e){
            Log.e("Exception while deleting state file:", e.getMessage());
        }
        try{
            File timeFile = new File(timeline);
            timeFile.delete();
        }
        catch(Exception e){
            Log.e("Exception while deleting timeline file:", e.getMessage());
        }

    }


    //*****************************************************************************************Timeline File Handlers


    public timeKeeper loadTimeline() throws IOException, ClassNotFoundException {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream ois = null;
        timeKeeper tk;
        try {
            fis = new FileInputStream(fileloc+"Timeline");
            bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);
            tk = (timeKeeper) ois.readObject();
            ois.close();
            bis.close();
            fis.close();
            return tk;
        }
        catch(IOException | ClassNotFoundException e){
            try {
                if (fis != null)
                    fis.close();
            }
            catch(Exception p){}

            try {
                if (bis != null)
                    bis.close();
            }
            catch(Exception p){}

            try {
                if (ois != null)
                    ois.close();
            }
            catch(Exception p){}

            throw e;
        }
    }

    private void saveTimeline() throws IOException {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(fileloc+"Timeline");
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(timeline);
            oos.close();
            bos.close();
            fos.close();
        }
        catch(IOException e){
            try {
                if (fos != null)
                    fos.close();
            }
            catch(Exception p){}

            try {
                if (bos != null)
                    bos.close();
            }
            catch(Exception p){}

            try {
                if (oos != null)
                    oos.close();
            }
            catch(Exception p){}

            throw e;
        }
    }




















    /********************************************************** Threads and tasks************************************/
    public class SaveTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            //wait to stop both copying and processing

            boolean bothDone = false;

            while(!bothDone) {
                bothDone = true;

                synchronized (procLock) {
                    while (processing) {
                        bothDone = false;
                        try {
                            procLock.wait();
                        } catch (InterruptedException e) {
                            Log.e("Interrupted, resuming to wait", "doInBackground saveTask1");
                        }
                    }
                }

                synchronized (lockCopy) {
                    while (copying) {
                        bothDone = false;
                        try {
                            lockCopy.wait();
                        } catch (InterruptedException e) {
                            Log.e("Interrupted, resuming to wait", "doInBackground saveTask2");
                        }
                    }
                }
            }

            //now save it all
            try {
                if(!gameOver) {
                    saveState();
                }
            }
            catch(IOException e){
                throw new RuntimeException("Could not save state, crashing: \n"+e.getMessage());
            }
            try{
                if(!gameOver) {
                    saveTimeline();
                }
            }
            catch(IOException e){
                throw new RuntimeException("Could not save timeline, crashing: \n"+e.getMessage());
            }

            synchronized(lockSave) {
                savingCur = false;

                if(progress != null){
                    if(!gc.isDestroyed())
                        progress.dismiss();
                }

                synchronized(saveStopProcLock){
                    stopProcessing = false;
                }


                lockSave.notifyAll();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //put it all at the end of inBackground
        }

        @Override
        protected void onPreExecute() {
            synchronized(lockSave) {
                savingCur = true;
            }

            //signals to any applyChanges currently happening to stop asap
            synchronized(saveStopProcLock){
                stopProcessing = true;
            }

            synchronized(lockCopy) {
                synchronized(procLock) {
                    if (copying | processing) {
                        if(!gc.isDestroyed())
                            progress = ProgressDialog.show(gc, "Preparing Game", "This can take up to 25 seconds.");
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }


    public class CopyTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = null;
        boolean passed;

        public CopyTask(boolean pass){
            passed = pass;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                synchronized(lockSave) {
                    while (savingCur) {
                        try {
                            lockSave.wait();
                        } catch (InterruptedException e) {
                            Log.e("Interrupted, resuming to wait", "doInBackground copyTask");
                        }
                    }

                }

                oldState = (State) DeepCopy.copy(curState);

            }
            catch(IOException | ClassNotFoundException e){
                Log.e("Could not copy state in doInBackground, aborting",  e.getMessage());
                throw new RuntimeException("Could not copy, crashing!");
            }

            synchronized(lockCopy) {
                copying = false;
                lockCopy.notifyAll();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            if(progress != null){
                if(!gc.isDestroyed())
                    progress.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            synchronized(lockSave) {
                if (savingCur || passed) {
                    if(!gc.isDestroyed())
                        progress = ProgressDialog.show(gc, "Preparing Game", "This can take up to 25 seconds.");
                }
            }

            synchronized(lockCopy){
                copying = true;
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }



    public class ProgressDialogTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            boolean bothDone = false;

            //have to wait for both to be done
            while(!bothDone) {
                bothDone = true;

                synchronized (lockCopy) {
                    while (copying) {
                        bothDone = false;
                        try {
                            lockCopy.wait();
                        } catch (InterruptedException e) {
                            Log.e("Interrupted, resuming to wait", "doInBackground ProgressDialogTask1");
                        }
                    }
                }

                synchronized (lockSave) {
                    while (savingCur) {
                        bothDone = false;
                        try {
                            lockSave.wait();
                        } catch (InterruptedException e) {
                            Log.e("Interrupted, resuming to wait", "doInBackground ProgressDialogTask2");
                        }
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(progress != null)
                if(!gc.isDestroyed())
                    progress.dismiss();

        }

        @Override
        protected void onPreExecute() {
            synchronized(lockCopy) {
                synchronized(lockSave) {
                    if (copying || savingCur) {
                        if(!gc.isDestroyed())
                            progress = ProgressDialog.show(gc, "Preparing Game", "This can take up to 25 seconds.");
                    }
                }
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }


/*   public class LoadStateTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog progress;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                curState = loadState();
                return true;
            }
            catch(IOException | ClassNotFoundException e){
                Log.e("Could not load state in endTurn, aborting",  e.getMessage());
                gc.finish();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progress.dismiss();

            if(result)

        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(gc, "Preparing Turn", "This can take up to 25 seconds.");
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }*/



    public class ProcessTask extends AsyncTask<Void, Void, Boolean> {
        public int fromTime;
        public int toTime;
        public ProcessCallable onPost;
        public boolean canStop = true;

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean passed;
            boolean bothDone = false;

            //have to wait for both to be done
            while(!bothDone) {
                bothDone = true;

                synchronized (lockCopy) {
                    while (copying) {
                        bothDone = false;
                        try {
                            lockCopy.wait();
                        } catch (InterruptedException e) {
                            Log.e("Thread was interrupted, going back into while loop", "ProcessTask doInBackground1");
                        }
                    }
                }

                synchronized (lockSave) {
                    while (savingCur) {
                        bothDone = false;
                        try {
                            lockSave.wait();
                        } catch (InterruptedException e) {
                            Log.e("Thread was interrupted, going back into while loop", "ProcessTask doInBackground2");
                        }
                    }
                }
            }

            synchronized(procLock){
                processing = true;
            }

            //one player matches never pass the opponent
            if(curState.onePlayer){
                applyChangesSinglePlayer(fromTime, toTime);
                passed = false;
            }

            //2 player matches might
            else{
                passed = applyChanges(fromTime, toTime);
            }


            return passed;
        }

        @Override
        protected void onPostExecute(Boolean passed) {
            synchronized (procLock) {
                synchronized (lockCopy) {

                    if(!gameOver) {
                        if (!gc.isDestroyed()) {
                            gc.findViewById(R.id.gameBackground).setBackground(gc.getResources().getDrawable(R.drawable.mainbackground));
                            LinearLayout notMap = (LinearLayout) gc.findViewById(R.id.notMap);
                            notMap.setVisibility(View.VISIBLE);
                        }
                    }

                    gc.processing = false;
                    gc.dispatchTouch = null;
                    stopProcessing = false;
                    timeline.stoppedShort = false;

                    if(!gameOver) {
                        onPost.call(passed);

                        if (passed) {
                            passedOpponent();
                        }
                    }

                    processing = false;
                    procLock.notifyAll();

                    if(gameOver){
                        deleteFiles();
                        gc.endGame();
                    }
                }
            }
        }

        @Override
        protected void onPreExecute() {
            synchronized (procLock) {
                gc.processing = true;
                new ProgressDialogTask().execute();

                if (!gc.isDestroyed()){
                    gc.findViewById(R.id.gameBackground).setBackgroundColor(gc.getResources().getColor(R.color.full_black));
                    LinearLayout notMap = (LinearLayout) gc.findViewById(R.id.notMap);
                    notMap.setVisibility(View.GONE);
                    gc.updateAllHighlighted();
                }

                if (canStop) {
                    getGameAct().dispatchTouch = new Callable<Boolean>() {
                        public Boolean call() {
                            stopProcessing = true;
                            return true;
                        }
                    };
                }

            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }





    public class ClearTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress;

        @Override
        protected Void doInBackground(Void... params) {
            boolean ready = false;
            while(!ready) {
                ready = true;
                synchronized (lockSave) {
                    while (savingCur ) {
                        try {
                            ready = false;
                            lockSave.wait();
                        } catch (InterruptedException e) {
                            Log.e("Thread was interrupted, going back into while loop", "doInBackground of ClearTask");
                        }
                    }
                }
                synchronized (lockCopy) {
                    while (copying) {
                        try {
                            ready = false;
                            lockCopy.wait();
                        } catch (InterruptedException e) {
                            Log.e("Thread was interrupted, going back into while loop", "doInBackground of ClearTask");
                        }
                    }
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            synchronized (lockSave) {
                progress.dismiss();
                clearContinue();
            }
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(offAct, "Saving Game", "This can take up to 25 seconds.");
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }




    public class WaitTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            waiting = true;

            try {
                Thread.currentThread().sleep(50);
            }
            catch(InterruptedException ie){
                Log.e("Interrupted while waiting in WaitTask", "continuing");
            }


            synchronized (waitLock) {
                waiting = false;
                waitLock.notify();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }



}
