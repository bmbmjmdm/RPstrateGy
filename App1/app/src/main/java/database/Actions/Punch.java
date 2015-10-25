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
import Utilities.Callable;

import Managers.GameManager;
import Managers.Logic.LogicCalc;
import Managers.timeKeeper;
import Utilities.CoordInt;
import Utilities.DamageType;
import Utilities.IntObj;
import Utilities.NameObjCallable;
import Utilities.ObjCallable;
import Utilities.RandomSelect;
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
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.Requirements.Requirement;
import database.Requirements.bodypartReq;
import database.Requirements.statCost;
import database.State;
import shenronproductions.app1.Activities.gameAct;
import shenronproductions.app1.R;
import shenronproductions.app1.gameUtils.viewGrid;

/**
 * Created by Dale on 1/1/2015.
 */
public class Punch extends Action{
    int zRange = 50;
    HashMap<Coord, HashSet<CObj>> canHit;


    int damage = 3;

    Coord torso;

    ArrayList<Requirement> punchReq = new ArrayList<>();
    bodypartReq armReq;

    Coord curC = null;


    public Punch(){
        super("Punch",
                "You got tickets to the gun show? This is a straight punch; it doesn't deal a lot of damage, but is pretty fast.",
                45,
                450,
                Stat.WARRIOR);



        description.add(new Callable<String>() {
            @Override
            public String call() {
                return "Punches &#160;at &#160;" + Math.round((getTimeNeeded(minSpeed, false)/10.0)*100.0)/100.0 +"m/s";
            }
        });
        description.add(new Callable<String>() {
            @Override
            public String call() {
                return "Deals &#160;" + damage + " &#160;damage.";
            }
        });

        armReq = new bodypartReq("Arm", 30, 30);
        requirements.add(armReq);
        punchReq.add(armReq);
        setReqDesc(armReq, new Callable<String>(){
            public String call() {
                return "One &#160;usable &#160;arm";
            }
        });

        HashMap<Stat, Integer> stats = new HashMap<>();
        stats.put(Stat.CUR_STAMINA, 3);
        final statCost stamReq = new statCost(stats);
        requirements.add(stamReq);
        setReqDesc(stamReq, new Callable<String>(){
            public String call() {
                return stamReq.getStat(Stat.CUR_STAMINA)+" &#160;stamina &#160;per &#160;punch";
            }
        });

        HashMap<Stat, Integer> stats2 = new HashMap<>();
        stats2.put(Stat.CUR_FOCUS, 1);
        final statCost focReq = new statCost(stats2);
        requirements.add(focReq);
        setReqDesc(focReq, new Callable<String>(){
            public String call() {
                return focReq.getStat(Stat.CUR_FOCUS)+" &#160;focus &#160;per &#160;punch";
            }
        });

        description.add(new Callable<String>(){
            public String call() {
                double punchTime = getTimeNeeded(maxTimeNeeded, true);
                double toPunch = punchTime/2;
                return toPunch+ " &#160;ms &#160;to &#160;punch, &#160;" + toPunch+ " &#160;ms &#160;to &#160;finish";
            }
        });



        cost = 3;
        setBuyReq("3 &#160;skill &#160;points\nLevel &#160;5 &#160;Warrior");

    }

    @Override
    public Action getCopy(int u){
        LogicCalc calc = new LogicCalc();
        Punch run = new Punch();
        calc.modAction(run, u);
        run.curUser = u;

        return run;
    }


    @Override
    public Action getCopy(){
        return new Punch();
    }



    @Override
    public void useAction(){
        try {
            GameManager gm = GameManager.getInstance();
            State s = gm.getState();
            User u = (User) s.getObjID(gm.getTimeline().turnObjectID);
            final gameAct context = gm.getGameAct();
            context.actionTakesMapClick = true;
            zoomedIn = false;
            ((LinearLayout) context.findViewById(R.id.actInOptions)).removeAllViews();
            ((HorizontalScrollView) context.findViewById(R.id.actionsInInnerScroll)).removeAllViews();
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
                ((TextView) context.findViewById(R.id.actInInfo)).setText("Select a highlighted space to punch at.");
            } else {
                ((TextView) context.findViewById(R.id.actInInfo)).setText("There is nothing you can punch!");
            }

            context.actionHighlightCoordsWhite(canHit.keySet());
        }
        catch(RemovedException e){
            Log.e("User has been removed from the state", "Punch>useAction");
        }

    }

    @Override
    public void mapClicked(Coord c){
        GameManager gm = GameManager.getInstance();
        final gameAct context = gm.getGameAct();

        if(!zoomedIn) {
            HashSet<CObj> applicants = canHit.get(c);

            //is the coordinate ok to punch at
            if (applicants != null) {

                //create a list of options
                ((TextView) context.findViewById(R.id.actInInfo)).setText("Select what you would like to hit");
                curC = c;
                setupGrid();

            }
        }
    }



    private void hit(CObj co, Coord c){
        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();
        BodyPart arm = null;
        User u = null;

        //TODO right now we assume that it doesnt matter which arm we use, that may not be the case however.
        try{
            u = (User) s.getObjID(curUser);

            //get the first usable arm and set it as the arm we're using
            for(IntObj io : u.getChildren()){
                String armName = io.o.name;

                if(armName.contains("Arm")){
                    //we can do this cause theres only 1 requirement in punchReq
                    bodypartReq bpReq = new bodypartReq(armName, armReq.healthReq, armReq.mobilityReq);
                    if(bpReq.canUse(u)){
                        arm = (BodyPart) io.o;
                        armReq.whichBP = armName;
                        break;
                    }
                }
            }
        }
        catch(RemovedException e){
            Log.e("User removed when processing Punch>hit", "this shouldn't happen");
        }

        if(arm == null){
            Log.e("arm is null in Punch>hit", "this shouldn't happen");
        }









        //add the action steps to the timeline
        final CoordInt hit = new CoordInt(co.id, c);
        int realTime = getTimeNeeded(maxTimeNeeded, true);

        //this adds running to the timeline
        int hitTime = s.getTime()+(realTime/2);
        int retTime = s.getTime()+realTime-1;

        int myId =  GameManager.getInstance().getTimeline().getId();

        //this gives the arm its punching status, at half strangth
        final NewObjT addPunch = new NewObjT(bpStrike.class, new Object[]{arm.id, 0.5, damage, DamageType.blunt, "Punching", " landed a solid bruiser on "});
        addPunch.setOwnerID(arm.id);

        //add narration
        HashSet<Integer> narrationInvolves = new HashSet<>();
        narrationInvolves.add(arm.id);
        AddNarration punchNarrate = new AddNarration(narrationInvolves, u.name + " winds up his fist", Stat.WARRIOR);

        //this is moving the arm to the coordinate its punching at, trying to collide with its target first
        ArrayList<Coord> coordsPunchAt = new ArrayList<>();
        coordsPunchAt.add(hit.c);
        NewObjT moveArmForward = new NewObjT(Moving.class, 0, new Object[]{null, arm.id, coordsPunchAt, null, 1});
        moveArmForward.setOwnerID(arm.id);
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
        ActionStep punchStart = new ActionStep(myId, "Punch", requirements, curUser, minSpeed, firstStepCont, new ArrayList<SubAction>(), 0, Stat.WARRIOR);
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
                    Log.e("Cannot update strength of punch", "removed from state");
                    return false;
                }
            }
        }});


        //this turns the arm horizontal before it moves
        NewObjT turnHor = new NewObjT(TurnBP.class, 0, 1, new Object[]{null, arm.id, false});
        turnHor.setOwnerID(arm.id);


        //this moves the arm back to the user's torso when it is time to
        Coord torso = u.getTorsoCoord();
        ArrayList<Coord> coordsReturnTo = new ArrayList<>();
        coordsReturnTo.add(torso);
        final NewObjT moveArmBack = new NewObjT(Moving.class, 0, new Object[]{null, arm.id, coordsReturnTo, null, 1});
        moveArmBack.setOwnerID(arm.id);
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
        ActionStep punchCont = new ActionStep(myId, "Punch", punchReq, curUser, minSpeed, secondStepCont, secondStepStop, realTime/4, Stat.WARRIOR);
        tk.addAction(hitTime, punchCont);




        //this turns the arm vertical
        NewObjT turnVert = new NewObjT(TurnBP.class, 0, 1, new Object[]{null, arm.id, true});
        turnVert.setOwnerID(arm.id);

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
        thirdStepContStop.add(turnVert);
        thirdStepContStop.add(initiateVertTurn);
        thirdStepContStop.add(updateTorso);
        thirdStepContStop.add(initiateUpdateTorso);
        thirdStepContStop.add(initiateArmMoveBack);
        thirdStepContStop.add(remUpdate);


        //add actionStep
        //the wanky stop time there (second to last arg) just means that this actionstep will take the same amount of time to stop as it would to complete
        ActionStep punchFinish = new ActionStep(myId, "Punch", new ArrayList<Requirement>(), curUser, minSpeed, thirdStepContStop, thirdStepContStop, realTime/2, Stat.WARRIOR);
        tk.addAction(retTime, punchFinish);









        //this saves this action in the timeline, telling it that this is what should appear in the "actions" section in the gameView
        tk.setCurAction(this);

        //this processes to the next available time (hence the +1)
        gm.processTime(s.getTime(), retTime+1);
    }












    @Override
    public void defaultHighlight(){
        GameManager.getInstance().getGameAct().actionHighlightCoordsWhite(canHit.keySet());
    }

    @Override
    public void setupGrid(){
        zoomedIn = false;

        //extract all obj on the spot we are interested in using
        final HashSet<CObj> useThese = canHit.get(curC);

        //set up onclick event to use an observed/selected obj
        String butName = "Hit";
        ObjCallable butClick = new ObjCallable() {
            @Override
            public void call(Obj o) {
                CObj useMe = null;

                //if a parent is returned to this, pick one of its usable children at random
                if(o.isParent()){
                    HashSet<CObj> allChildren = new HashSet<>();
                    for(CObj co: useThese){
                        if(o.contains(co)){
                            allChildren.add(co);
                        }
                    }

                    useMe = new RandomSelect<CObj>().getRandom(allChildren);
                }

                //if its a child, just use it
                else{
                    for(CObj co: useThese){
                        if(co == o){
                            useMe = co;
                            break;
                        }
                    }
                }

                //now make the call
                LogicCalc lc = new LogicCalc();
                hit(useMe, new Coord(curC.x, curC.y, lc.getAccCenter(useMe, new Coord(curC.x, curC.y, torso.z), zRange)));
            }
        };
        NameObjCallable button = new NameObjCallable(butName, butClick);

        //create grid
        viewGrid newGrid = new viewGrid(useThese, curC, false, true);
        newGrid.addToView(button);
    }
}
