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
import Utilities.CoordInt;
import Utilities.DamageType;
import Utilities.IntObj;
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
import database.ObjT.CustomCallable;
import database.ObjT.Moving;
import database.ObjT.ObjT;
import database.ObjT.TurnBP;
import database.ObjT.bpStrike;
import database.Objs.CObjs.BodyPart;
import database.Objs.CObjs.CObj;
import database.Objs.PObjs.User;
import database.Requirements.Requirement;
import database.Requirements.bodypartReq;
import database.Requirements.statCost;
import database.State;
import shenronproductions.app1.Activities.gameAct;
import shenronproductions.app1.R;

/**
 * Created by Dale on 1/1/2015.
 */
public class Kick extends Action{
    int zRange = 50;
    HashMap<Coord, HashSet<CObj>> canHit;

    int curSelected =  -404;

    int damage = 6;

    Coord legsC;

    ArrayList<Requirement> punchReq = new ArrayList<>();
    bodypartReq armReq;

    BodyPart leg;


    public Kick(){
        super("Kick",
                "For when your fists get tired.",
                40,
                800,
                Stat.WARRIOR);



        description.add(new Callable<String>() {
            @Override
            public String call() {
                return "Kicks &#160;at &#160;" + Math.round((getTimeNeeded(minSpeed, false)/10.0)*100.0)/100.0 +"m/s";
            }
        });
        description.add(new Callable<String>() {
            @Override
            public String call() {
                return "Deals &#160;" + damage + " &#160;damage.";
            }
        });

        //this is to make sure we have 2 legs
        bodypartReq bpReq = new bodypartReq("Leg", 30, 30, 2);
        requirements.add(bpReq);
        punchReq.add(bpReq);
        setReqDesc(bpReq, new Callable<String>(){
            public String call() {
                return "Two &#160;usable &#160;legs";
            }
        });

        //this is for the leg that will be doing the actual kick
        armReq = new bodypartReq("Leg", 30, 30);
        requirements.add(armReq);
        punchReq.add(armReq);

        HashMap<Stat, Integer> stats = new HashMap<>();
        stats.put(Stat.CUR_STAMINA, 6);
        final statCost stamReq = new statCost(stats);
        requirements.add(stamReq);
        setReqDesc(stamReq, new Callable<String>(){
            public String call() {
                return stamReq.getStat(Stat.CUR_STAMINA)+" &#160;stamina &#160;per &#160;kick";
            }
        });

        HashMap<Stat, Integer> stats2 = new HashMap<>();
        stats2.put(Stat.CUR_FOCUS, 1);
        final statCost focReq = new statCost(stats2);
        requirements.add(focReq);
        setReqDesc(focReq, new Callable<String>(){
            public String call() {
                return focReq.getStat(Stat.CUR_FOCUS)+" &#160;focus &#160;per &#160;kick";
            }
        });

        description.add(new Callable<String>(){
            public String call() {
                double punchTime = getTimeNeeded(maxTimeNeeded, true);
                double toPunch = punchTime/2;
                return toPunch+ " &#160;ms &#160;to &#160;kick, &#160;" + toPunch+ " &#160;ms &#160;to &#160;finish";
            }
        });



        cost = 3;
        setBuyReq("3 &#160;skill &#160;points\nLevel &#160;5 &#160;Warrior");

    }

    @Override
    public Action getCopy(int u){
        LogicCalc calc = new LogicCalc();
        Kick run = new Kick();
        calc.modAction(run, u);
        run.curUser = u;

        return run;
    }


    @Override
    public Action getCopy(){
        return new Kick();
    }



    @Override
    public void useAction(){
        try {
            GameManager gm = GameManager.getInstance();
            State s = gm.getState();
            User u = (User) s.getObjID(gm.getTimeline().turnObjectID);
            final gameAct context = gm.getGameAct();
            context.actionTakesMapClick = true;
            curSelected =  -404;
            ((LinearLayout) context.findViewById(R.id.actInOptions)).removeAllViews();
            LogicCalc lc = new LogicCalc();

            //TODO right now we assume that it doesnt matter which arm we use, that may not be the case however.
            //get the first usable leg and set it as the leg we're using
            for(IntObj io : u.getChildren()){
                String armName = io.o.name;

                if(armName.contains("Leg")){
                    //we can do this cause theres only 1 requirement in punchReq
                    bodypartReq bpReq = new bodypartReq(armName, armReq.healthReq, armReq.mobilityReq);
                    if(bpReq.canUse(u)){
                        armReq.whichBP = armName;
                        leg = (BodyPart) io.o;
                        break;
                    }
                }
            }

            if(leg == null){
                Log.e("leg is null in Kick>hit", "this shouldn't happen");
            }

            //get all coord/cobj the user can hit
            legsC = u.getTorsoCoord();
            legsC = new Coord(legsC.x, legsC.y, legsC.z-(leg.getHeight()/2));
            canHit = new HashMap<Coord, HashSet<CObj>>();

            for(int x = legsC.x-1; x<= legsC.x+1; x++){
                for(int y = legsC.y-1; y<= legsC.y+1; y++){
                    Coord newC = new Coord(x, y);
                    //cant be off map
                    if(!s.testOffMap(newC)){
                        //within range (z of torso z - half height of leg)
                        HashSet<CObj> hitem = lc.getCObjAround(new Coord(x, y, legsC.z), zRange);
                        //can see
                        lc.removeCantSee(hitem, u, newC);
                        //not a part of the user
                        Iterator<CObj> it = hitem.iterator();
                        while(it.hasNext()){
                            CObj co = it.next();
                            if(u.contains(co))
                                it.remove();
                        }
                        //possible target
                        if(hitem.size()!= 0)
                            canHit.put(newC, hitem);
                    }
                }
            }


            if (canHit.size() != 0) {
                ((TextView) context.findViewById(R.id.actInInfo)).setText("Select a highlighted space to kick at.");
            } else {
                ((TextView) context.findViewById(R.id.actInInfo)).setText("There is nothing you can kick!");
            }

            context.actionHighlightCoordsWhite(canHit.keySet());
        }
        catch(RemovedException e){
            Log.e("User has been removed from the state", "Kick>useAction");
        }

    }

    @Override
    public void mapClicked(Coord c){
        GameManager gm = GameManager.getInstance();
        final gameAct context = gm.getGameAct();

        if(curSelected ==  -404) {
            HashSet<CObj> applicants = canHit.get(c);

            //is the coordinate ok to punch at
            if (applicants != null) {

                //create a list of options
                ((TextView) context.findViewById(R.id.actInInfo)).setText("Select what you would like to kick");
                makeOptions(applicants, c, context);

            }
        }
    }



    private void hit(CObj co, Coord c){
        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();
        User u = null;


        try{
            u = (User) s.getObjID(curUser);
        }
        catch(RemovedException e){
            Log.e("User removed when processing Kick>hit", "this shouldn't happen");
        }









        //add the action steps to the timeline
        final CoordInt hit = new CoordInt(co.id, c);
        int realTime = getTimeNeeded(maxTimeNeeded, true);

        //this adds running to the timeline
        int hitTime = s.getTime()+(realTime/2);
        int retTime = s.getTime()+realTime-1;

        int myId =  GameManager.getInstance().getTimeline().getId();

        //this gives the arm its punching status, at half strangth
        final NewObjT addPunch = new NewObjT(bpStrike.class, new Object[]{leg.id, 0.5, damage, DamageType.blunt, "Kicking", " kicked the shit out of "});
        addPunch.setOwnerID(leg.id);

        //add narration
        HashSet<Integer> narrationInvolves = new HashSet<>();
        narrationInvolves.add(leg.id);
        AddNarration punchNarrate = new AddNarration(narrationInvolves, u.name + " plants one foot firmly down and winds up the other.", Stat.WARRIOR);

        //this is moving the arm to the coordinate its punching at, trying to collide with its target first
        ArrayList<Coord> coordsPunchAt = new ArrayList<>();
        coordsPunchAt.add(hit.c);
        NewObjT moveArmForward = new NewObjT(Moving.class, 0, new Object[]{null, leg.id, coordsPunchAt, null, 1});
        moveArmForward.setOwnerID(leg.id);
        moveArmForward.setModObjT(new ObjTModCallable() {
            @Override
            public void call(ObjT modThis, User u) {
                ((Moving) modThis).setCollide(hit.i);
                ((Moving) modThis).takesUpSpace = false;
            }
        });

        //construct arrays
        ArrayList<SubAction> firstStepCont = new ArrayList<>();
        firstStepCont.add(addPunch);
        firstStepCont.add(punchNarrate);
        firstStepCont.add(moveArmForward);

        //add actionStep
        ActionStep punchStart = new ActionStep(myId, "Kick", requirements, curUser, minSpeed, firstStepCont, new ArrayList<SubAction>(), 0, Stat.WARRIOR);
        tk.addAction(s.getTime(), punchStart);




        //this updates the punch strength once it is "thrown"
        NewObjT upgradeStrength = new NewObjT(CustomCallable.class, 0, 1, new Object[]{null, new Callable<Boolean>(){
            @Override
            public Boolean call() {
                GameManager gm = GameManager.getInstance();
                State s = gm.getState();
                try {
                    ((bpStrike) s.getObjT(addPunch.getObjTID.call())).strengthP = 1;
                    return true;
                }
                catch(RemovedException e){
                    Log.e("Cannot update strength of kick", "removed from state");
                    return false;
                }
            }
        }});


        //this turns the arm horizontal before it moves
        NewObjT turnHor = new NewObjT(TurnBP.class, 0, 1, new Object[]{null, leg.id, false});
        turnHor.setOwnerID(leg.id);


        //this moves the arm back to the user's torso when it is time to
        Coord torso = u.getTorsoCoord();
        ArrayList<Coord> coordsReturnTo = new ArrayList<>();
        coordsReturnTo.add(torso);
        final NewObjT moveArmBack = new NewObjT(Moving.class, 0, new Object[]{null, leg.id, coordsReturnTo, null, 1});
        moveArmBack.setOwnerID(leg.id);
        moveArmBack.setModObjT(new ObjTModCallable() {
            @Override
            public void call(ObjT modThis, User u) {
                ((Moving) modThis).takesUpSpace = false;
            }
        });


        //this initiates the move forward of the arm
        AddEOT initiateArmMoveForward = new AddEOT(moveArmForward.getObjTID);

        //initiates the upgrade to strength
        AddEOT initiateStrengthUpgrade = new AddEOT(upgradeStrength.getObjTID);

        //initiates the horizontal turn
        AddEOT initiateHorTurn = new AddEOT(turnHor.getObjTID);


        //construct cont array
        ArrayList<SubAction> secondStepCont = new ArrayList<>();
        secondStepCont.add(upgradeStrength);
        secondStepCont.add(turnHor);
        secondStepCont.add(moveArmBack);
        secondStepCont.add(initiateArmMoveForward);
        secondStepCont.add(initiateStrengthUpgrade);
        secondStepCont.add(initiateHorTurn);


        //this removes the punching ObjT incase things are stopped
        RemObjT remPunch = new RemObjT(addPunch.getObjTID);

        //remove the moving objT incase things are stopped
        RemObjT remMove = new RemObjT(moveArmForward.getObjTID);

        //construct stop array
        ArrayList<SubAction> secondStepStop = new ArrayList<>();
        secondStepStop.add(remPunch);
        secondStepStop.add(remMove);

        //add actionStep
        ActionStep punchCont = new ActionStep(myId, "Kick", punchReq, curUser, minSpeed, secondStepCont, secondStepStop, realTime/4, Stat.WARRIOR);
        tk.addAction(hitTime, punchCont);




        //this turns the arm vertical
        NewObjT turnVert = new NewObjT(TurnBP.class, 0, 1, new Object[]{null, leg.id, true});
        turnVert.setOwnerID(leg.id);

        //initiates the vertical turn
        AddEOT initiateVertTurn = new AddEOT(turnVert.getObjTID);

        //this updates the torso coord at any change
        NewObjT updateTorso = new NewObjT(CustomCallable.class, new Object[]{0, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                GameManager gm = GameManager.getInstance();
                State s = gm.getState();
                try {
                    Coord c = ((User) s.getObjID(curUser)).getTorsoCoord();
                    ArrayList<Coord> array = new ArrayList<>();
                    array.add(c);
                    try {
                        ((Moving) s.getObjT(moveArmBack.getObjTID.call())).setMovement(array);
                        return true;
                    }
                    catch(RemovedException e){
                        Log.e("Cannot update torso coord for kick", "removed from state");
                        return false;
                    }
                }
                catch(RemovedException e){
                    Log.e("Cannot find user when trying to update torso kick", "in kick updateTorso subAction");
                    return false;
                }

            }
        }});

        //initiates the torso update
        AddEOT initiateUpdateTorso = new AddEOT(updateTorso.getObjTID, -1);

        //this removes the torso update
        RemObjT remUpdate = new RemObjT(updateTorso.getObjTID);

        //initiates the arms return to the torsoe
        AddEOT initiateArmMoveBack = new AddEOT(moveArmBack.getObjTID);

        //construct cont/stop array
        ArrayList<SubAction> thirdStepContStop = new ArrayList<>();
        thirdStepContStop.add(remPunch);
        thirdStepContStop.add(turnVert);
        thirdStepContStop.add(initiateVertTurn);
        thirdStepContStop.add(updateTorso);
        thirdStepContStop.add(initiateUpdateTorso);
        thirdStepContStop.add(initiateArmMoveBack);
        thirdStepContStop.add(remUpdate);


        //add actionStep
        //the wanky stop time there (second to last arg) just means that this actionstep will take the same amount of time to stop as it would to complete
        ActionStep punchFinish = new ActionStep(myId, "Kick", new ArrayList<Requirement>(), curUser, minSpeed, thirdStepContStop, thirdStepContStop, realTime/2, Stat.WARRIOR);
        tk.addAction(retTime, punchFinish);









        //this saves this action in the timeline, telling it that this is what should appear in the "actions" section in the gameView
        tk.setCurAction(this);

        //this processes to the next available time (hence the +1)
        gm.processTime(s.getTime(), retTime+1);
    }









    private void makeOptions(HashSet<CObj> applicants, final Coord c, final gameAct context){
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


        for(final CObj co: applicants){
            final TextView element = context.getElement(co.name, gameAct.ElementT.NORMAL);
            element.setId(View.generateViewId());
            if(first) {
                element.setPadding(0, 0, 0, 0);
                first = false;
            }
            element.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (curSelected == element.getId()) {
                        element.setTextColor(black);
                        curSelected = -404;
                        detailsList.removeAllViews();
                        context.actionHighlightCoordsWhite(canHit.keySet());
                    } else {
                        element.setTextColor(white);
                        if (curSelected != -404)
                            ((TextView) context.findViewById(curSelected)).setTextColor(black);
                        curSelected = element.getId();
                        makeDetails(co, detailsList, c, context);
                        context.actionHighlightCoordsWhite(context.getHighlightableCoords(co));
                    }

                }
            });

            optionsList.addView(element);
        }
    }

    private void makeDetails(final CObj co, LinearLayout detailsList, final Coord c, final gameAct context){
        final int sp10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, context.getResources().getDisplayMetrics());
        final int dp8 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
        final Typeface font = Typeface.createFromAsset(context.getAssets(), "njnaruto.ttf");
        detailsList.removeAllViews();

        Button use = new Button(context);
        use.setTypeface(font);
        use.setText("Kick");
        use.setTextColor(context.getResources().getColorStateList(R.color.white_text_button));
        use.setBackground(context.getResources().getDrawable(R.drawable.brush2_button));
        LinearLayout.LayoutParams useParams = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 135, context.getResources().getDisplayMetrics()),
                                                                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, context.getResources().getDisplayMetrics()));
        useParams.gravity = Gravity.CENTER_HORIZONTAL;
        use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogicCalc lc = new LogicCalc();
                hit(co, new Coord(c.x, c.y, lc.getAccCenter(co, new Coord(c.x, c.y, legsC.z), zRange)));
            }
        });

        detailsList.addView(use, useParams);


        ArrayList<String> types = co.getDescription();
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
