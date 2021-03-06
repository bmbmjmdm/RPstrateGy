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
import database.ObjT.bpStrike;
import database.ObjT.TurnBP;
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
public class Bite extends Action{
    int zRange = 35;
    HashMap<Coord, HashSet<CObj>> canHit;

    int curSelected =  -404;

    int damage = 10;

    Coord torso;

    ArrayList<Requirement> biteReq = new ArrayList<>();
    bodypartReq headReq;


    public Bite(){
        super("Bite",
                "Holy shit a bite? You crazy mother-.",
                30,
                1200,
                Stat.WARRIOR);



        description.add(new Callable<String>() {
            @Override
            public String call() {
                return "Bites &#160;at &#160;" + Math.round((getTimeNeeded(minSpeed, false)/10.0)*100.0)/100.0 +"m/s";
            }
        });
        description.add(new Callable<String>() {
            @Override
            public String call() {
                return "Deals &#160;" + damage + " &#160;damage.";
            }
        });

        headReq = new bodypartReq("Head", 50, 50);
        requirements.add(headReq);
        biteReq.add(headReq);
        setReqDesc(headReq, new Callable<String>(){
            public String call() {
                return "One &#160;usable &#160;head";
            }
        });

        HashMap<Stat, Integer> stats = new HashMap<>();
        stats.put(Stat.CUR_STAMINA, 7);
        final statCost stamReq = new statCost(stats);
        requirements.add(stamReq);
        setReqDesc(stamReq, new Callable<String>(){
            public String call() {
                return stamReq.getStat(Stat.CUR_STAMINA)+" &#160;stamina &#160;per &#160;bite";
            }
        });

        HashMap<Stat, Integer> stats2 = new HashMap<>();
        stats2.put(Stat.CUR_FOCUS, 1);
        final statCost focReq = new statCost(stats2);
        requirements.add(focReq);
        setReqDesc(focReq, new Callable<String>(){
            public String call() {
                return focReq.getStat(Stat.CUR_FOCUS)+" &#160;focus &#160;per &#160;bite";
            }
        });

        description.add(new Callable<String>(){
            public String call() {
                double punchTime = getTimeNeeded(maxTimeNeeded, true);
                double toPunch = punchTime/2;
                return toPunch+ " &#160;ms &#160;to &#160;bite, &#160;" + toPunch+ " &#160;ms &#160;to &#160;finish";
            }
        });



        cost = 2;
        setBuyReq("2 &#160;skill &#160;points\nLevel &#160;1 &#160;Warrior");

    }

    @Override
    public Action getCopy(int u){
        LogicCalc calc = new LogicCalc();
        Bite bite = new Bite();
        calc.modAction(bite, u);
        bite.curUser = u;

        return bite;
    }


    @Override
    public Action getCopy(){
        return new Bite();
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


            //get all coord/cobj the user can hit
            torso = u.getTorsoCoord();
            torso = new Coord(torso.x, torso.y, torso.z + u.getTorsoHeight());
            canHit = new HashMap<Coord, HashSet<CObj>>();

            for(int x = torso.x-1; x<= torso.x+1; x++){
                for(int y = torso.y-1; y<= torso.y+1; y++){
                    Coord newC = new Coord(x, y);
                    //cant be off map
                    if(!s.testOffMap(newC)){
                        //within range
                        HashSet<CObj> hitem = lc.getCObjAround(new Coord(x, y, torso.z), zRange);
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
                ((TextView) context.findViewById(R.id.actInInfo)).setText("Select a highlighted space to bite at.");
            } else {
                ((TextView) context.findViewById(R.id.actInInfo)).setText("There is nothing you can bite!");
            }

            context.actionHighlightCoordsWhite(canHit.keySet());
        }
        catch(RemovedException e){
            Log.e("User has been removed from the state", "Bite>useAction");
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
                ((TextView) context.findViewById(R.id.actInInfo)).setText("Select what you would like to bite");
                makeOptions(applicants, c, context);

            }
        }
    }



    private int makeActionSteps(CObj co, Coord c){
        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();
        BodyPart head = null;
        User u = null;

        try{
            u = (User) s.getObjID(curUser);

            //get the first usable arm and set it as the arm we're using
            for(IntObj io : u.getChildren()){
                String armName = io.o.name;

                if(armName.contains("Head")){
                    //we can do this cause theres only 1 requirement in biteReq
                    bodypartReq bpReq = new bodypartReq(armName, headReq.healthReq, headReq.mobilityReq);
                    if(bpReq.canUse(u)){
                        head = (BodyPart) io.o;
                        headReq.whichBP = armName;
                        break;
                    }
                }
            }
        }
        catch(RemovedException e){
            Log.e("User removed when processing Bite>hit", "this shouldn't happen");
            throw new RuntimeException("User removed when processing Bite>hit");
        }

        if(head == null){
            Log.e("head is null in Bite>hit", "this shouldn't happen");
            throw new RuntimeException("head is null in Bite>hit");
        }








        //add the action steps to the timeline
        final CoordInt hit = new CoordInt(co.id, c);
        int realTime = getTimeNeeded(maxTimeNeeded, true);

        //this adds running to the timeline
        int hitTime = s.getTime()+(realTime/2);
        int retTime = s.getTime()+realTime-1;

        int myId =  GameManager.getInstance().getTimeline().getId();

        //this gives the arm its punching status, at half strangth
        final NewObjT addPunch = new NewObjT(bpStrike.class, new Object[]{head.id, 0.5, damage, DamageType.sharp, "Biting", " took a fucking bite out of "});
        addPunch.setOwnerID(head.id);

        //add narration
        HashSet<Integer> narrationInvolves = new HashSet<>();
        narrationInvolves.add(head.id);
        AddNarration punchNarrate = new AddNarration(narrationInvolves, u.name + " licks his lips", Stat.WARRIOR);

        //this is moving the arm to the coordinate its punching at, trying to collide with its target first
        ArrayList<Coord> coordsPunchAt = new ArrayList<>();
        coordsPunchAt.add(hit.c);
        NewObjT moveArmForward = new NewObjT(Moving.class, 0, new Object[]{null, head.id, coordsPunchAt, null, 1});
        moveArmForward.setOwnerID(head.id);
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
        ActionStep punchStart = new ActionStep(myId, "Bite", requirements, curUser, minSpeed, firstStepCont, new ArrayList<SubAction>(), 0, Stat.WARRIOR);
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
                    Log.e("Cannot update strength of bite", "removed from state");
                    return false;
                }
            }
        }});


        //this moves the arm back to the user's torso when it is time to
        Coord torso = u.getTorsoCoord();
        ArrayList<Coord> coordsReturnTo = new ArrayList<>();
        coordsReturnTo.add(torso);
        final NewObjT moveArmBack = new NewObjT(Moving.class, 0, new Object[]{null, head.id, coordsReturnTo, null, 1});
        moveArmBack.setOwnerID(head.id);
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


        //construct cont array
        ArrayList<SubAction> secondStepCont = new ArrayList<>();
        secondStepCont.add(upgradeStrength);
        secondStepCont.add(moveArmBack);
        secondStepCont.add(initiateArmMoveForward);
        secondStepCont.add(initiateStrengthUpgrade);


        //this removes the punching ObjT incase things are stopped
        RemObjT remPunch = new RemObjT(addPunch.getObjTID);

        //remove the moving objT incase things are stopped
        RemObjT remMove = new RemObjT(moveArmForward.getObjTID);

        //construct stop array
        ArrayList<SubAction> secondStepStop = new ArrayList<>();
        secondStepStop.add(remPunch);
        secondStepStop.add(remMove);

        //add actionStep
        ActionStep punchCont = new ActionStep(myId, "Bite", biteReq, curUser, minSpeed, secondStepCont, secondStepStop, realTime/4, Stat.WARRIOR);
        tk.addAction(hitTime, punchCont);





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
                        Log.e("Cannot update torso coord for punch", "removed from state");
                        return false;
                    }
                }
                catch(RemovedException e){
                    Log.e("Cannot find user when trying to update torso coord", "in punch updateTorso subAction");
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
        thirdStepContStop.add(updateTorso);
        thirdStepContStop.add(initiateUpdateTorso);
        thirdStepContStop.add(initiateArmMoveBack);
        thirdStepContStop.add(remUpdate);


        //add actionStep
        //the wanky stop time there (second to last arg) just means that this actionstep will take the same amount of time to stop as it would to complete
        ActionStep punchFinish = new ActionStep(myId, "Bite", new ArrayList<Requirement>(), curUser, minSpeed, thirdStepContStop, thirdStepContStop, realTime/2, Stat.WARRIOR);
        tk.addAction(retTime, punchFinish);

        return retTime+1;
    }










    private void hit(CObj co, Coord c){
        GameManager gm = GameManager.getInstance();
        timeKeeper tk = gm.getTimeline();
        State s = gm.getState();

        int retTime = makeActionSteps(co, c);

        //this saves this action in the timeline, telling it that this is what should appear in the "actions" section in the gameView
        tk.setCurAction(this);

        //this processes to the next available time (hence the +1)
        gm.processTime(s.getTime(), retTime);
    }



    public int useWolf(CObj co, Coord c){
        return makeActionSteps(co, c);
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
        use.setText("Bite");
        use.setTextColor(context.getResources().getColorStateList(R.color.white_text_button));
        use.setBackground(context.getResources().getDrawable(R.drawable.brush2_button));
        LinearLayout.LayoutParams useParams = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 135, context.getResources().getDisplayMetrics()),
                                                                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, context.getResources().getDisplayMetrics()));
        useParams.gravity = Gravity.CENTER_HORIZONTAL;
        use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogicCalc lc = new LogicCalc();
                hit(co, new Coord(c.x, c.y, lc.getAccCenter(co, new Coord(c.x, c.y, torso.z), zRange)));
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
