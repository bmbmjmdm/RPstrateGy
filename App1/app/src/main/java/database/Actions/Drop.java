package database.Actions;

import android.util.Log;
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
import Utilities.IntObj;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Actions.ActionSteps.ActionStep;
import database.Actions.SubActions.AddEOT;
import database.Actions.SubActions.NewObjT;
import database.Actions.SubActions.ObjTModCallable;
import database.Actions.SubActions.SubAction;
import database.Coord;
import database.ObjT.Falling;
import database.ObjT.Moving;
import database.ObjT.ObjT;
import database.Objs.PObjs.User;
import database.Requirements.onPerchReq;
import database.Requirements.prepareReq;
import database.Requirements.statCost;
import database.State;
import shenronproductions.app1.R;
import shenronproductions.app1.Activities.gameAct;

/**
 * Created by Dale on 1/1/2015.
 */
public class Drop extends Action {

    HashSet<Coord> canMoveTo = new HashSet<Coord>();

    Coord curSelected = null;

    public Drop() {
        super("Drop",
                "The user drops off of whatever they're currently on. Best used as a quick escape, worst used as a quick suicide.",
                19,
                0,
                Stat.ACROBAT);


        thisAct = Acts.DROP;
        description.add(new Callable<String>() {
            @Override
            public String call() {
                return "Drops &#160;down, &#160;landing &#160;(on &#160;their &#160;feet) &#160;on &#160;the &#160;first &#160;surface &#160;they &#160;hit.";
            }
        });


        onPerchReq opR = new onPerchReq();
        requirements.add(opR);
        setReqDesc(opR, new Callable<String>(){
            public String call() {
                return "Must &#160;be &#160;able &#160;to &#160;drop &#160;off &#160;of &#160;current &#160;surface";
            }
        });

        prepareTime = getTimeNeeded(300, true);
        description.add(new Callable<String>(){
            public String call() {
                return "Takes &#160;"+Math.round(prepareTime*1000.0)/1000.0 +" &#160;milliseconds &#160;to &#160;prepare";
            }
        });

        cost = 0;
        setBuyReq("None");

    }

    @Override
    public Action getCopy(int u) {
        LogicCalc calc = new LogicCalc();
        Drop wt = new Drop();
        calc.modAction(wt, u);
        wt.curUser = u;
        wt.curSelected = curSelected;
        wt.prepareTime = prepareTime;

        return wt;
    }


    @Override
    public Action getCopy() {
        return new Drop();
    }




    @Override
    public void useAction() {
        try{
            GameManager gm = GameManager.getInstance();
            State s = gm.getState();
            User u = (User) s.getObjID(gm.getTimeline().turnObjectID);
            final gameAct context = gm.getGameAct();
            context.actionTakesMapClick = true;
            curSelected = null;
            ((LinearLayout) context.findViewById(R.id.actInOptions)).removeAllViews();
            ((HorizontalScrollView) context.findViewById(R.id.actionsInInnerScroll)).removeAllViews();
            LogicCalc lc = new LogicCalc();


            //get all coord/cobj the user can drop to
            canMoveTo = new HashSet<>();
            Coord middle = u.getMiddlemostCoord();
            int x = middle.x;
            int y = middle.y;
            int z = u.getLowestZ();

            for (int curX = x - 1; curX < x + 2; curX++) {
                for (int curY = y - 1; curY < y + 2; curY++) {
                    Coord c = new Coord(curX, curY);
                    if(!s.testOffMap(c)) {
                        int minBottom = s.getLowestSurface(c);
                        int descent;
                        if (z - 50 < minBottom)
                            descent = (minBottom + z) / 2;
                        else
                            descent = z - 50;
                        c = new Coord(curX, curY, descent);
                        if (lc.canFit(u, c, 75))
                            canMoveTo.add(c);
                    }

                }
            }

            if (canMoveTo.size() != 0) {
                ((TextView) context.findViewById(R.id.actInInfo)).setText("Select a highlighted space to drop to.");
            } else {
                ((TextView) context.findViewById(R.id.actInInfo)).setText("There is nowhere you can drop to!.");
            }

            context.actionHighlightCoordsWhite(canMoveTo);
        }
        catch(
                RemovedException e){
            Log.e("User has been removed from the state", "Drop>useAction");
        }
    }

    @Override
    public void mapClicked(Coord c) {
        GameManager gm = GameManager.getInstance();
        final gameAct context = gm.getGameAct();

        boolean found = false;

        for(final Coord canGo: canMoveTo) {
            if (canGo.x == c.x)
                if (canGo.y == c.y) {
                    dropNow(canGo);
                }
        }


        if(!found){
            if (canMoveTo.size() != 0) {
                ((TextView) context.findViewById(R.id.actInInfo)).setText("Select a highlighted space to drop to.");
            } else {
                ((TextView) context.findViewById(R.id.actInInfo)).setText("There is nowhere you can drop to!.");
            }
        }
    }



    private void dropNow(Coord c) {
        curSelected = c;

        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();
        int curTime = s.getTime();

        ArrayList<Coord> coords = new ArrayList<>();
        coords.add(curSelected);

        int myId =  GameManager.getInstance().getTimeline().getId();

        //this moves the user to where they will be falling
        NewObjT addMove = new NewObjT(Moving.class, 0, new Object[]{null, curUser, coords, null,1});
        addMove.setOwnerID(curUser);

        //this initiates the move
        AddEOT moveTime = new AddEOT(addMove.getObjTID);


        //this sets how the user will fall
        NewObjT addFall = new NewObjT(Falling.class, new Object[]{curUser});
        addFall.setOwnerID(curUser);
        addFall.setModObjT(new ObjTModCallable() {
            @Override
            public void call(ObjT modThis, User u) {
                for(IntObj io: u.getChildren()){
                    if(io.o.name.contains("Leg")){
                        ((Falling) modThis).preferredPart.add(io.o);
                    }
                }
            }
        });

        //this initiates the fall
        AddEOT fallTime = new AddEOT(addFall.getObjTID, curTime+67);


        //construct the list of actions
        ArrayList<SubAction> firstActions = new ArrayList<>();
        firstActions.add(addMove);
        firstActions.add(addFall);
        firstActions.add(moveTime);
        firstActions.add(fallTime);
        ArrayList<SubAction> mtArray = new ArrayList<>();


        //make the actionstep
        ActionStep dropStep = new ActionStep(myId, "Drop", requirements, curUser, minSpeed, firstActions, mtArray, 0, Stat.ACROBAT);
        tk.addAction(curTime, dropStep);


        //this is not a recurring action
        tk.clearCurAction();

        //process
        gm.processTime(curTime, curTime + 1);


    }

}