package database.Actions;

import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
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
import Utilities.StringModifier;
import database.Actions.ActionSteps.ActionStep;
import database.Actions.SubActions.AddEOT;
import database.Actions.SubActions.AddNarration;
import database.Actions.SubActions.NewObjT;
import database.Actions.SubActions.ObjTModCallable;
import database.Actions.SubActions.RemObjT;
import database.Actions.SubActions.SubAction;
import database.Actions.SubActions.UpdateVision;
import database.Coord;
import database.ObjT.Moving;
import database.ObjT.ObjT;
import database.ObjT.UserStealthed;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.Requirements.Requirement;
import database.Requirements.bodypartReq;
import database.Requirements.inAirReq;
import database.Requirements.sneakingReq;
import database.Requirements.statCost;
import database.Requirements.statReq;
import database.State;
import shenronproductions.app1.Activities.gameAct;
import shenronproductions.app1.R;

/**
 * Created by Dale on 1/1/2015.
 */
public class Sneak extends Action{
    //stealth objects' height are multiplied by this to conceal user
    public double heightMod = 2;

    //stealth objects' width are multiplied by this to conceal user
    public double widthMod = 1.5;

    public Sneak(){
        super("Sneak",
                "You conceal yourself with your surroundings all sneaky like. Consumes focus over time and slows all non-Ninja actions.",
                10,
                1000,
                Stat.NINJA);



        description.add(new Callable<String>() {
            @Override
            public String call() {
                return StringModifier.addSpaces("Conceals body behind objects on the same space. Body is considered to be "+widthMod+"x thinner and "+heightMod+"x shorter for this concealment.");
            }
        });


        HashMap<Stat, Integer> stats = new HashMap<>();
        stats.put(Stat.CUR_FOCUS, 2);
        final statReq stamReq = new statReq(stats);
        requirements.add(stamReq);
        setReqDesc(stamReq, new Callable<String>() {
            public String call() {
                return StringModifier.addSpaces(stamReq.getStat(Stat.CUR_FOCUS) + " focus every 500 ms");
            }
        });



        cost = 5;
        setBuyReq(StringModifier.addSpaces("5 skill points\nLevel 5 Ninja"));

    }

    @Override
    public Action getCopy(int u){
        LogicCalc calc = new LogicCalc();
        Sneak run = new Sneak();
        calc.modAction(run, u);
        run.curUser = u;


        return run;
    }

    @Override
    public Action getCopy(){
        return new Sneak();
    }


    @Override
    public void mapClicked(Coord c){
        gameAct gc = GameManager.getInstance().getGameAct();

        gc.showMapInfo(null);

        gc.setObjects(c);
    }



    @Override
    public void useAction(){
        GameManager gm = GameManager.getInstance();
        final gameAct context = gm.getGameAct();
        try {
            //is the user already sneaking?
            boolean sneaking = false;
            Obj user = gm.getState().getObjID(curUser);

            for(ObjT type: user.getTypePath()){
                if(type.isStealthed()){
                    sneaking = true;
                    break;
                }
            }

            String infoText;
            String buttonText;
            if(sneaking) {
                infoText = "You are already sneaking, do you want to stop?";
                buttonText = "Stop Sneaking";
            }
            else {
                infoText = "Sneaking consumes focus over time and slows all non-ninja actions. When you want to stop sneaking, use the Sneak action again.";
                buttonText = "Start Sneaking";
            }

            //make option to stop/start sneaking
            ((HorizontalScrollView) context.findViewById(R.id.actionsInInnerScroll)).removeAllViews();

            ((TextView) context.findViewById(R.id.actInInfo)).setText(infoText);
            LinearLayout options = ((LinearLayout) context.findViewById(R.id.actInOptions));
            options.removeAllViews();

            final Typeface font = Typeface.createFromAsset(context.getAssets(), "njnaruto.ttf");

            Button use = new Button(context);
            use.setTypeface(font);
            use.setText(buttonText);
            use.setTextColor(context.getResources().getColorStateList(R.color.white_text_button));
            use.setBackground(context.getResources().getDrawable(R.drawable.brush1_button));
            LinearLayout.LayoutParams useParams = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, context.getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics()));
            useParams.gravity = Gravity.CENTER_HORIZONTAL;
            final boolean finalSneak = sneaking;
            use.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initiateSneak(finalSneak);
                }
            });

            options.addView(use, useParams);
        }


        //this shouldnt happen
        catch(RemovedException re){
            Log.e("User was removed in sneak>useAction", "what how");
        }
    }


    //changes behavior based on whether user is already sneaking or not
    public void initiateSneak(boolean sneaking){
        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();

        int realTime = getTimeNeeded(maxTimeNeeded, true);

        //this clears the timekeeper incase the user was using something else before
        tk.clearCurAction();

        int runTime;
        //add the action steps
        if(sneaking)
            runTime = actionStepsStop(realTime);
        else
            runTime = actionStepsStart(realTime);

        //this processes to the next available time
        gm.processTime(s.getTime(), runTime);
    }



    private int actionStepsStart(int realTime){
        try {
            GameManager gm = GameManager.getInstance();
            State s = gm.getState();
            timeKeeper tk = gm.getTimeline();

            //the first actionstep adds the movement objT to the user/state

            //some constants
            int stealthTime = s.getTime() + realTime - 1;

            int myId = GameManager.getInstance().getTimeline().getId();

            //add narration
            HashSet<Integer> narrationInvolves = new HashSet<>();
            narrationInvolves.add(curUser);
            Obj o = s.getObjID(curUser);
            AddNarration punchNarrate = new AddNarration(narrationInvolves, o.name+" is acting all sneaky and shit.", Stat.NINJA);

            //create array for first action step
            ArrayList<SubAction> firstSubs = new ArrayList<>();
            firstSubs.add(punchNarrate);

            //create an empty array to stop action
            ArrayList<SubAction> mtArray = new ArrayList<>();

            ActionStep runStart = new ActionStep(myId, "Sneak", requirements, curUser, minSpeed, firstSubs, mtArray, 0, Stat.NINJA);
            tk.addAction(s.getTime(), runStart);


            //the second actionstep exists to either initiate the stealth or cancel it

            //create the stealth objT
            NewObjT addStealth = new NewObjT(UserStealthed.class, new Object[]{curUser, widthMod, heightMod});
            addStealth.setOwnerID(curUser);

            //set up the stealth EOT objT to drain focus over time
            AddEOT stealthEOT = new AddEOT(addStealth.getObjTID);

            //create the vision update
            UpdateVision upVis = new UpdateVision(curUser);

            //create the second continue action array
            ArrayList<SubAction> secondActions = new ArrayList<>();
            secondActions.add(addStealth);
            secondActions.add(upVis);
            secondActions.add(stealthEOT);


            //create the second action step and add it
            ActionStep runEnd = new ActionStep(myId, "Sneak", new ArrayList<Requirement>(), curUser, minSpeed, secondActions, mtArray, realTime / 4, Stat.NINJA);
            tk.addAction(stealthTime, runEnd);

            return stealthTime + 1;
        }

        catch(RemovedException re){
            Log.e("User removed in actionStepsStart of Sneak", "this should not happen");
            return -1;
        }
    }






    private int actionStepsStop(int realTime){
        try {
            realTime = realTime / 4;
            GameManager gm = GameManager.getInstance();
            State s = gm.getState();
            timeKeeper tk = gm.getTimeline();

            //get the sneaking id from the user
            Obj user = s.getObjID(curUser);
            Integer sneakId = null;
            for(ObjT type: user.getTypePath()){
                if(type.isStealthed()){
                    sneakId = type.id;
                    break;
                }
            }

            //some constants
            int stealthTime = s.getTime() + realTime - 1;

            int myId = GameManager.getInstance().getTimeline().getId();

            //the only subaction is removing stealth
            RemObjT remStealth = new RemObjT(sneakId);

            //create the second continue action array
            ArrayList<SubAction> secondActions = new ArrayList<>();
            secondActions.add(remStealth);


            //create the second action step and add it
            ActionStep runEnd = new ActionStep(myId, "Stop Sneaking", new ArrayList<Requirement>(), curUser, minSpeed, secondActions, new ArrayList<SubAction>(), realTime, Stat.NINJA);
            tk.addAction(stealthTime, runEnd);

            return stealthTime + 1;
        }

        catch(RemovedException re){
            Log.e("User removed in actionStepsStop of Sneak", "this should not happen");
            return -1;
        }
    }
















    public int useWolf(boolean sneaking){
        int realTime = getTimeNeeded(maxTimeNeeded, true);

        if(sneaking)
            return actionStepsStop(realTime);
        else
            return actionStepsStart(realTime);
    }


    public Sneak getWolfCopy(int u, double difficulty){
        LogicCalc calc = new LogicCalc();
        Sneak run = new Sneak();
        calc.modAction(run, u);
        run.curUser = u;
        heightMod = heightMod*Math.max(1, difficulty);

        widthMod = widthMod*Math.max(1, difficulty);

        return run;

    }




}
