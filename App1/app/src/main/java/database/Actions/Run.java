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
import Utilities.Callable;

import Utilities.ClimbObj;
import Utilities.CoordInt;
import Utilities.Direction;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Actions.ActionSteps.ActionStep;
import database.Actions.SubActions.AddEOT;
import database.Actions.SubActions.NewObjT;
import database.Actions.SubActions.ObjTModCallable;
import database.Actions.SubActions.RemObjT;
import database.Actions.SubActions.SubAction;
import database.Coord;
import database.ObjT.Moving;
import database.ObjT.ObjT;
import database.ObjT.RemoveType;
import database.Objs.CObjs.CObj;
import database.Objs.PObjs.User;
import database.Objs.PObjs.Wolf;
import database.Requirements.Requirement;
import database.Requirements.andReq;
import database.Requirements.bodypartReq;
import database.Requirements.inAirReq;
import database.Requirements.statCost;
import database.State;
import Managers.GameManager;
import Managers.Logic.LogicCalc;
import shenronproductions.app1.R;
import shenronproductions.app1.Activities.gameAct;
import Managers.timeKeeper;

/**
 * Created by Dale on 1/1/2015.
 */
public class Run extends Action{
    int zMovement = 40;
    HashMap<Coord, HashSet<ClimbObj>> canMoveToForward;
    HashMap<Coord, HashSet<ClimbObj>> canMoveToBackward;

    ArrayList<Requirement> stepReq = new ArrayList<>();

    int curSelected =  -404;
    public boolean firstStep = true;

    Direction dir = null;
    Coord curC;

    bodypartReq legReq;

    public Run(){
        super("Run",
                "You run from one space to another, you really need me to explain that?",
                25,
                450,
                Stat.ACROBAT);



        description.add(new Callable<String>() {
            @Override
            public String call() {
                return "Runs &#160;at &#160;" + Math.round((getTimeNeeded(minSpeed, false)/10.0)*100.0)/100.0 +"m/s &#160;to &#160;start, &#160;but &#160;quickly &#160;accelerates &#160;to &#160;"+ Math.round((getTimeNeeded(minSpeed*3, false)/10.0)*100.0)/100.0 + "m/s";
            }
        });

        legReq = new bodypartReq("Leg", 50, 50, 2);
        requirements.add(legReq);
        stepReq.add(legReq);
        setReqDesc(legReq, new Callable<String>() {
            public String call() {
                return "Two &#160;usable &#160;legs";
            }
        });

        Requirement airReq = new inAirReq(false);
        requirements.add(airReq);
        stepReq.add(airReq);
        setReqDesc(airReq, new Callable<String>() {
            public String call() {
                return "Cannot &#160;be &#160;airborne";
            }
        });

        HashMap<Stat, Integer> stats = new HashMap<>();
        stats.put(Stat.CUR_STAMINA, 2);
        final statCost stamReq = new statCost(stats);
        requirements.add(stamReq);
        setReqDesc(stamReq, new Callable<String>() {
            public String call() {
                return stamReq.getStat(Stat.CUR_STAMINA) + " &#160;stamina &#160;per &#160;step";
            }
        });


        description.add(new Callable<String>() {
            public String call() {
                double stepTime = getTimeNeeded(maxTimeNeeded, true) / 1000.0;
                return stepTime + " &#160;seconds &#160;for &#160;first &#160;step, &#160;" + Math.round((stepTime / 3) * 1000.0) / 1000.0 + " &#160;seconds &#160;for &#160;each &#160;step &#160;after";
            }
        });

        cost = 0;
        setBuyReq("None");

    }

    @Override
    public Action getCopy(int u){
        LogicCalc calc = new LogicCalc();
        Run run = new Run();
        run.firstStep = firstStep;
        calc.modAction(run, u);
        run.curUser = u;
        run.dir = dir;


        return run;
    }

    public Run getWolfCopy(int u){
        LogicCalc calc = new LogicCalc();
        Run run = new Run();

        //four legs
        run.legReq.numNeeded = 4;

        try {
            User w = (User) GameManager.getInstance().getState().getObjID(u);

            //with four legs, the wolf is considered to always be running at top speed
            if (run.legReq.canUse(w)){
                run.minSpeed = minSpeed*3;
                run.maxTimeNeeded = maxTimeNeeded/3;
            }

            //the wolf can run with 3 legs if 4 are not available, but they then run at the speed of walking
            else{
                run.legReq.numNeeded = 3;
            }
        }
        catch(RemovedException re){
            Log.e("throw new RuntimeException", "Getting here should not be possible. The wolf was decided to be able to run, however when trying to (currently in getWolfCopy), the wolf doesnt exist.");
            throw new RuntimeException("Getting here should not be possible. The wolf was decided to be able to run, however when trying to (currently in getWolfCopy), the wolf doesnt exist.");
        }


        calc.modAction(run, u);
        run.curUser = u;

        return run;
    }


    @Override
    public Action getCopy(){
        return new Run();
    }



    @Override
    public void useAction(){
        try {
            GameManager gm = GameManager.getInstance();
            User u = (User) gm.getState().getObjID(gm.getTimeline().turnObjectID);
            final gameAct context = gm.getGameAct();
            context.actionTakesMapClick = true;
            curSelected =  -404;
            ((LinearLayout) context.findViewById(R.id.actInOptions)).removeAllViews();
            LogicCalc lc = new LogicCalc();
            curC = u.getMiddlemostCoord();


            //get all coord/cobj the user can run to
            canMoveToForward = lc.movementHashMap(curUser, 1, zMovement, zMovement, false, true);
            canMoveToBackward = new HashMap<>();

            lc.removeCantSee(canMoveToForward, u);


            if(!firstStep){

                //if its not the first step, the user has momentum. move any steps that are turning from the moveForward list to the moveBackward list
                Iterator<Coord> iterator = canMoveToForward.keySet().iterator();
                while(iterator.hasNext()){
                    Coord to = iterator.next();
                    Direction d = Direction.findDir(curC, to);
                    if(Direction.degreesDifference(dir, d) > 1){
                        canMoveToBackward.put(to, canMoveToForward.get(to));
                        iterator.remove();
                    }
                }
            }

            if (canMoveToForward.size()+canMoveToBackward.size() != 0) {
                ((TextView) context.findViewById(R.id.actInInfo)).setText("Select a highlighted space to run to. A red tint indicates that you will be changing direction, resulting in a slower run!");
            } else {
                ((TextView) context.findViewById(R.id.actInInfo)).setText("There is nowhere else to run to!.");
            }

            context.actionHighlightCoordsWhite(canMoveToForward.keySet());
            context.actionHighlightCoordsRed(canMoveToBackward.keySet());
        }
        catch(RemovedException e){
            Log.e("User has been removed from the state", "Run>useAction");
        }

    }

    @Override
    public void mapClicked(Coord c){
        GameManager gm = GameManager.getInstance();
        final gameAct context = gm.getGameAct();

        if(curSelected ==  -404) {
            HashSet<ClimbObj> applicants = canMoveToForward.get(c);
            if(applicants == null)
                applicants = canMoveToBackward.get(c);

            //is the coordinate ok to move onto?
            if (applicants != null) {
                //is there more than one object the user can step on?

                if (applicants.size() > 1) {
                    //more than one option, create a list of options
                    ((TextView) context.findViewById(R.id.actInInfo)).setText("There is more than one thing you can run onto on this space; please select one.");
                    makeOptions(applicants, c, context);
                }
                else {
                    //there is only one object the user can step on, automatically use it and move onto the next
                    upOrDown(applicants.iterator().next(), c);
                }

            }
        }
    }


    private void upOrDown(final ClimbObj co, final Coord c){
        if(co.climbPoints.size() == 1)
            runTo(co.co, new Coord(c.x, c.y, co.climbPoints.get(0)));

        else{
            GameManager gm = GameManager.getInstance();
            final gameAct context = gm.getGameAct();
            ((TextView) context.findViewById(R.id.actInInfo)).setText("Do you want to run up or down?");
            LinearLayout options = ((LinearLayout) context.findViewById(R.id.actInOptions));
            options.removeAllViews();

            final Typeface font = Typeface.createFromAsset(context.getAssets(), "njnaruto.ttf");

            Button use = new Button(context);
            use.setTypeface(font);
            use.setText("Up");
            use.setTextColor(context.getResources().getColorStateList(R.color.white_text_button));
            use.setBackground(context.getResources().getDrawable(R.drawable.brush1_button));
            LinearLayout.LayoutParams useParams = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, context.getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics()));
            useParams.gravity = Gravity.CENTER_HORIZONTAL;
            use.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runTo(co.co, new Coord(c.x, c.y, co.climbPoints.get(0)));
                }
            });

            options.addView(use, useParams);

            Button use2 = new Button(context);
            use2.setTypeface(font);
            use2.setText("Down");
            use2.setTextColor(context.getResources().getColorStateList(R.color.white_text_button));
            use2.setBackground(context.getResources().getDrawable(R.drawable.brush1flip_button));
            LinearLayout.LayoutParams useParams2 = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, context.getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics()));
            useParams2.gravity = Gravity.CENTER_HORIZONTAL;
            use2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runTo(co.co, new Coord(c.x, c.y, co.climbPoints.get(1)));
                }
            });

            options.addView(use2, useParams2);

        }

    }

    private void runTo(CObj co, Coord c){
        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();

        //prepare to move
        dir = Direction.findDir(curC, c);

        int realTime = getTimeNeeded(maxTimeNeeded, true);
        //if its not the first step, divide realTime by 3
        if(!firstStep) {
            //however it wont be divided if they're moving backwards
            if(!canMoveToBackward.containsKey(c)) {
                realTime = realTime / 3;
                minSpeed = minSpeed*3;
            }
        }
        //if it is the first step make it not the first step
        else
            firstStep = false;

        //this saves this action in the timeline, telling it that this is what should appear in the "actions" section in the gameView
        tk.setCurAction(this);

        //add the action steps
        int runTime = actionSteps(realTime, c, co);

        //this processes to the next available time (hence the +1)
        gm.processTime(s.getTime(), runTime);
    }



    private int actionSteps(int realTime, Coord c, CObj co){
        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();

        final CoordInt moveTo = new CoordInt(co.id, c);

        //the first actionstep adds the movement objT to the user/state

        //some constants
        int runTime = s.getTime()+realTime-1;
        ArrayList<Coord> coords = new ArrayList<>();
        coords.add(moveTo.c);

        int myId =  GameManager.getInstance().getTimeline().getId();


        //create the run objT
        NewObjT addRun = new NewObjT(Moving.class, 0,  new Object[]{null, curUser, coords, "Running",1});
        addRun.setOwnerID(curUser);
        addRun.setModObjT(new ObjTModCallable() {
            @Override
            public void call(ObjT modThis, User u) {
                ((Moving) modThis).setLandsOn(moveTo.i, zMovement, zMovement, false);
            }
        });


        //create the continue action array and an empty array to stop action
        ArrayList<SubAction> firstActions = new ArrayList<>();
        firstActions.add(addRun);
        ArrayList<SubAction> mtArray = new ArrayList<>();

        ActionStep runStart = new ActionStep(myId, "Run", requirements, curUser, minSpeed, firstActions, mtArray, 0, Stat.ACROBAT);
        tk.addAction(s.getTime(), runStart);


        //the second actionstep exists to either initiate the run or cancel it

        //initiate the run
        AddEOT initiateRun = new AddEOT(addRun.getObjTID);

        //create the second continue action array
        ArrayList<SubAction> secondActions = new ArrayList<>();
        secondActions.add(initiateRun);


        //remove the run if stopped
        RemObjT remRun = new RemObjT(addRun.getObjTID);

        //create the second stop action array
        ArrayList<SubAction> stopActions = new ArrayList<>();
        stopActions.add(remRun);

        //create the second action step and add it
        ActionStep runEnd = new ActionStep(myId, "Run",  stepReq, curUser, 0, secondActions, stopActions, realTime/3, Stat.MISC);
        tk.addAction(runTime, runEnd);

        return runTime+1;
    }






    public int useWolf(ClimbObj moveOn, Coord c){
        int realTime = getTimeNeeded(maxTimeNeeded, true);

        //we use 0 since the wolf cannot climb, so there is no up or down option
        Coord moveTo = new Coord(c.x, c.y, moveOn.climbPoints.get(0));

        //setup action steps
        int runTime = actionSteps(realTime, moveTo, moveOn.co);

        //return the next available time
        return runTime;
    }




















    /******************************************************************************************************* UI MENUS BUTTONS ***********************************************/

    private void makeOptions(HashSet<ClimbObj> applicants, final Coord c, final gameAct context){
        ((LinearLayout) context.findViewById(R.id.actInOptions)).removeAllViews();
        LinearLayout twoLists = new LinearLayout(context);
        twoLists.setOrientation(LinearLayout.HORIZONTAL);
        twoLists.setWeightSum(2);
        LinearLayout.LayoutParams twoListsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ((LinearLayout) context.findViewById(R.id.actInOptions)).addView(twoLists, twoListsParams);

        LinearLayout optionsList = new LinearLayout(context);
        optionsList.setOrientation(LinearLayout.VERTICAL);
        final LinearLayout detailsList = new LinearLayout(context);
        detailsList.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        listParams.weight = 1;
        twoLists.addView(optionsList, listParams);
        twoLists.addView(detailsList, listParams);
        final int black = context.getResources().getColor(R.color.full_black);
        final int white = context.getResources().getColor(R.color.full_white);
        boolean first = true;

        for(final ClimbObj co: applicants){
            final TextView element = context.getElement(co.co.name, gameAct.ElementT.NORMAL);
            element.setId(View.generateViewId());
            if(first) {
                element.setPadding(0, 0, 0, 0);
                first = false;
            }
            element.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (curSelected == element.getId()) {
                        element.setTextColor(black);
                        curSelected =  -404;
                        detailsList.removeAllViews();
                        context.actionHighlightCoordsWhite(canMoveToForward.keySet());
                        context.actionHighlightCoordsRed(canMoveToBackward.keySet());
                    } else {
                        element.setTextColor(white);
                        if (curSelected !=  -404)
                            ((TextView) context.findViewById(curSelected)).setTextColor(black);
                        curSelected = element.getId();
                        makeDetails(co, detailsList, c, context);
                        context.actionHighlightCoordsWhite(context.getHighlightableCoords(co.co));
                    }

                }
            });

            optionsList.addView(element);
        }
    }

    private void makeDetails(final ClimbObj co, LinearLayout detailsList, final Coord c, final gameAct context){
        final int sp10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, context.getResources().getDisplayMetrics());
        final int dp8 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
        final Typeface font = Typeface.createFromAsset(context.getAssets(), "njnaruto.ttf");
        detailsList.removeAllViews();

        Button use = new Button(context);
        use.setTypeface(font);
        use.setText("Run On");
        use.setTextColor(context.getResources().getColorStateList(R.color.white_text_button));
        use.setBackground(context.getResources().getDrawable(R.drawable.brush2_button));
        LinearLayout.LayoutParams useParams = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 135, context.getResources().getDisplayMetrics()),
                                                                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, context.getResources().getDisplayMetrics()));
        useParams.gravity = Gravity.CENTER_HORIZONTAL;
        use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upOrDown(co, c);
            }
        });

        detailsList.addView(use, useParams);


        ArrayList<String> types = co.co.getDescription();
        for(String s: types){
            final TextView tV = new TextView(context);
            tV.setText(Html.fromHtml(s));
            tV.setTypeface(font);
            tV.setTextSize(sp10);
            tV.setPadding(0, dp8, 0, 0);
            detailsList.addView(tV);
        }
    }
}
