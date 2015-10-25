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
import Utilities.Callable;

import Managers.GameManager;
import Managers.Logic.LogicCalc;
import Managers.timeKeeper;
import Utilities.ClimbObj;
import Utilities.CoordInt;
import Utilities.NameObjCallable;
import Utilities.ObjCallable;
import Utilities.RandomSelect;
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
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.Requirements.Requirement;
import database.Requirements.andReq;
import database.Requirements.bodypartReq;
import database.Requirements.fallingReq;
import database.Requirements.statCost;
import database.State;
import shenronproductions.app1.R;
import shenronproductions.app1.Activities.gameAct;
import shenronproductions.app1.gameUtils.viewGrid;

/**
 * Created by Dale on 1/1/2015.
 */
public class Climb extends Action{
    public int defaultZMovement = 200;
    public int zMovementUp = defaultZMovement;
    public int zMovementDown = defaultZMovement;
    HashMap<Coord, HashSet<ClimbObj>> canMoveTo;
    Coord curC = null;

    ArrayList<Requirement> climbReq = new ArrayList<>();

    public Climb(){
        super("Climb",
                "Through an incredible display of strength and dexterity, the user can climb onto objects and stuff.",
                15,
                2000,
                Stat.ACROBAT);



        description.add(new Callable<String>() {
            @Override
            public String call() {
                return "Can &#160;climb &#160;up &#160;objects &#160;up &#160;to &#160;0.3m &#160;above &#160;the &#160;user's &#160;head &#160;or &#160;down &#160;objects &#160;up &#160;to &#160;2m &#160;below &#160;their &#160;feet";
            }
        });

        Requirement leftL = new bodypartReq("Left Leg", 50, 50);
        Requirement rightL = new bodypartReq("Right Leg", 50, 50);
        Requirement leftA = new bodypartReq("Left Arm", 50, 50);
        Requirement rightA = new bodypartReq("Right Arm", 50, 50);
        Requirement and = new andReq(new andReq(leftL, rightL), new andReq(leftA, rightA));
        requirements.add(and);
        climbReq.add(and);
        setReqDesc(and, new Callable<String>(){
            public String call() {
                return "Two &#160;usable &#160;legs &#160;and &#160;two &#160;usable &#160;arms";
            }
        });

        Requirement airReq = new fallingReq(false);
        requirements.add(airReq);
        climbReq.add(airReq);
        setReqDesc(airReq, new Callable<String>(){
            public String call() {
                return "Cannot &#160;be &#160;falling";
            }
        });

        HashMap<Stat, Integer> stats = new HashMap<>();
        stats.put(Stat.CUR_STAMINA, 8);
        final statCost stamReq = new statCost(stats);
        requirements.add(stamReq);
        setReqDesc(stamReq, new Callable<String>() {
            public String call() {
                return stamReq.getStat(Stat.CUR_STAMINA) + " &#160;stamina";
            }
        });

        description.add(new Callable<String>(){
            public String call() {
                double stepTime = getTimeNeeded(maxTimeNeeded, true)/1000.0;
                return stepTime+ " &#160;seconds &#160;to &#160;climb";
            }
        });

        cost = 3;
        setBuyReq("3 &#160;skill &#160;points\nLevel &#160;5 &#160;Acrobat");

    }

    @Override
    public Action getCopy(int u){
        LogicCalc calc = new LogicCalc();
        Climb climb = new Climb();
        calc.modAction(climb, u);
        climb.curUser = u;


        return climb;
    }

    @Override
    public Action getCopy(){
        return new Climb();
    }



    @Override
    public void useAction(){
        GameManager gm = GameManager.getInstance();
        final gameAct context = gm.getGameAct();
        ((HorizontalScrollView) context.findViewById(R.id.actionsInInnerScroll)).removeAllViews();
        canMoveTo = new HashMap<>();
        zoomedIn = false;

        ((TextView) context.findViewById(R.id.actInInfo)).setText("Do you want to climb up or down?");
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
                presentOptions(true);
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
                presentOptions(false);
            }
        });

        options.addView(use2, useParams2);

    }

    @Override
    public void mapClicked(Coord c){
        GameManager gm = GameManager.getInstance();
        final gameAct context = gm.getGameAct();

        if(!zoomedIn) {
            HashSet<ClimbObj> applicants = canMoveTo.get(c);
            //is the coordinate ok to move onto?
            if (applicants != null) {
                //is there more than one object the user can step on?

                if (applicants.size() > 1) {
                    //more than one option, create a list of options
                    ((TextView) context.findViewById(R.id.actInInfo)).setText("There is more than one thing you can climb onto on this space; please select one.");
                    curC = c;
                    setupGrid();
                }
                else {
                    ClimbObj onThis = applicants.iterator().next();
                    //there is only one object the user can step on, automatically use it and move onto the next
                    climbTo(onThis.co, new Coord(c.x, c.y, onThis.climbPoints.get(0)));
                }

            }
        }
    }


    private void presentOptions(boolean up){
        try {
            GameManager gm = GameManager.getInstance();
            User u = (User) gm.getState().getObjID(gm.getTimeline().turnObjectID);
            final gameAct context = gm.getGameAct();
            context.actionTakesMapClick = true;
            LogicCalc lc = new LogicCalc();
            zMovementDown = defaultZMovement;
            zMovementUp = defaultZMovement;

            //get all coord/cobj the user can run to
            if (up)
                zMovementDown = 0;
            else
                zMovementUp = 0;

            canMoveTo = lc.movementHashMap(curUser, 1, zMovementUp, zMovementDown, true, true);

            lc.removeCantSee(canMoveTo, u);


            if (canMoveTo.size() != 0) {
                ((TextView) context.findViewById(R.id.actInInfo)).setText("Select a highlighted space to climb to.");
            } else {
                ((TextView) context.findViewById(R.id.actInInfo)).setText("There is nowhere else to climb to!.");
            }

            context.actionHighlightCoordsWhite(canMoveTo.keySet());
        }
        catch(RemovedException e){
            Log.e("User has been removed from the state", "Run>useAction");
        }

    }

    private void climbTo(CObj co, Coord c){
        final CoordInt moveTo = new CoordInt(co.id, c);

        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();

        //this saves this action in the timeline, telling it that this is what should appear in the "actions" section in the gameView
        tk.setCurAction(this);


        int realTime = getTimeNeeded(maxTimeNeeded, true);
        //the first actionstep adds the movement objT to the user/state

        //get some constants
        int climbTime = s.getTime()+realTime-1;
        ArrayList<Coord> coords = new ArrayList<>();
        coords.add(moveTo.c);

        int myId =  GameManager.getInstance().getTimeline().getId();


        //creat the climb subaction
        NewObjT addClimb = new NewObjT(Moving.class, 0, new Object[]{null, curUser, coords, "Climbing",1});
        addClimb.setOwnerID(curUser);
        addClimb.setModObjT(new ObjTModCallable() {
            @Override
            public void call(ObjT modThis, User u) {
                ((Moving) modThis).setLandsOn(moveTo.i, zMovementUp, zMovementDown, true);
            }
        });

        //create the continue array and an empty stop array
        ArrayList<SubAction> firstActions = new ArrayList<>();
        firstActions.add(addClimb);
        ArrayList<SubAction> mtArray = new ArrayList<>();

        //create the first acton step and add it
        ActionStep climbStart = new ActionStep(myId, "Climb", requirements, curUser, minSpeed, firstActions, mtArray, 0, Stat.ACROBAT);
        tk.addAction(s.getTime(), climbStart);


        //the second action step exists to either cancel this action or initiate the climb as an EOT ObjT

        //initiate the climb
        AddEOT initiateClimb = new AddEOT(addClimb.getObjTID);

        //create the second continue action array
        ArrayList<SubAction> secondActions = new ArrayList<>();
        secondActions.add(initiateClimb);


        //remove the climb if stopped
        RemObjT remClimb = new RemObjT(addClimb.getObjTID);

        //create the second stop action array
        ArrayList<SubAction> stopActions = new ArrayList<>();
        stopActions.add(remClimb);

        //create the second action step and add it
        ActionStep climbEnd = new ActionStep(myId, "Climb", climbReq, curUser, 0, secondActions, stopActions, getTimeNeeded(300, true), Stat.MISC);
        tk.addAction(climbTime, climbEnd);

        //this processes to the next available time (hence the +1)
        gm.processTime(s.getTime(), climbTime+1);
    }







    @Override
    public void defaultHighlight(){
        GameManager.getInstance().getGameAct().actionHighlightCoordsWhite(canMoveTo.keySet());
    }

    @Override
    public void setupGrid(){
        zoomedIn = false;

        //extract all obj on the spot we are interested in using
        final HashSet<ClimbObj> applicants = canMoveTo.get(curC);
        HashSet<CObj> useThese = new HashSet<CObj>();
        for(ClimbObj co: applicants){
            useThese.add(co.co);
        }

        //set up onclick event to use an observed/selected obj
        String butName = "Climb Onto";
        ObjCallable butClick = new ObjCallable() {
            @Override
            public void call(Obj o) {
                ClimbObj useMe = null;

                //if a parent is returned to this, pick one of its usable children at random
                if(o.isParent()){
                    HashSet<ClimbObj> allChildren = new HashSet<>();
                    for(ClimbObj co: applicants){
                        if(o.contains(co.co)){
                            allChildren.add(co);
                        }
                    }

                    useMe = new RandomSelect<ClimbObj>().getRandom(allChildren);
                }

                //if its a child, just use it
                else{
                    for(ClimbObj co: applicants){
                        if(co.co == o){
                            useMe = co;
                            break;
                        }
                    }
                }

                //now make the call
                climbTo(useMe.co, new Coord(curC.x, curC.y, useMe.climbPoints.get(0)));
            }
        };
        NameObjCallable button = new NameObjCallable(butName, butClick);

        //create grid
        viewGrid newGrid = new viewGrid(useThese, curC, false, true);
        newGrid.addToView(button);
    }


}
