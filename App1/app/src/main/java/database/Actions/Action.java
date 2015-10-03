package database.Actions;

import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


import Managers.UserProfile;
import Managers.timeKeeper;
import Utilities.Callable;
import Utilities.RemovedException;
import Utilities.Stat;
import database.ActionT.ActionT;
import database.Actions.ActionSteps.ActionStep;
import database.Actions.SubActions.NewObjT;
import database.Actions.SubActions.RemObjT;
import database.Actions.SubActions.SubAction;
import database.Actions.SubActions.CustomSubAction;
import database.Actions.SubActions.SubActCallable;
import database.Coord;
import database.ObjT.ObjT;
import database.ObjT.Preparing;
import database.Objs.PObjs.User;
import database.Players.CharacterPlayer;
import database.Requirements.Requirement;
import database.Requirements.StatelessRequirement;
import database.State;
import Managers.GameManager;
import Managers.Logic.LogicCalc;
import shenronproductions.app1.Activities.gameAct;
import shenronproductions.app1.R;

/**
 * Created by Dale on 12/29/2014.
 */
public abstract class Action implements Serializable {
    public String name;
    public int cost;
    public ArrayList<ActionT> types = new ArrayList<ActionT>();
    public ArrayList<Requirement> requirements = new ArrayList<Requirement>();
    public ArrayList<StatelessRequirement> statelessRequirements = new ArrayList<StatelessRequirement>();


    //time in centiseconds
    String flavor;
    String buyReq;
    ArrayList<Callable<String>> description = new ArrayList<>();
    HashMap<Requirement, Callable<String>> reqDes = new HashMap<>();

    public HashSet<Stat> classes = new HashSet<>();

    public int minSpeed;
    public int maxTimeNeeded;

    public Acts thisAct;

    int curUser;

    int prepareTime = 0;

    protected boolean needsAskToContinue = false;

    //notet that if the action needs to be prepared it must set thisAct manually
    public Action(String nam, String flav, int speed, int timeNeeded, Stat... classes){
        name = nam;
        flavor = flav;
        for(Stat c : classes)
            this.classes.add(c);
        minSpeed = speed;
        maxTimeNeeded = timeNeeded;
    }



    public String getDescription(){
        String desc = "<font color=#000CCC>";
        for(Callable<String> part: description){
            desc = desc + part.call()+"<br>";
        }
        desc = desc+"</font>";
        return desc;
    }

    public String getDescriptionOOC(){
        String desc = "<font color=#000000>Description:</font><font color=#000CCC>";
        for(Callable<String> part: description){
            desc = desc + "<br>" +part.call();
        }
        desc = desc+"</font>";
        return desc;
    }

    public String getBuyReq(){
        return "<font color=#000000>Requirements:<br></font><font color=#000CCC>"+buyReq+"</font>";
    }

    public String getFlavor(){
        return "<font color=#000000>"+flavor+"</font>";
    }

    public void setBuyReq(String s){
        buyReq = s;
    }



    public void setReqDesc(Requirement r, Callable<String> s){
        reqDes.put(r, s);
    }
    public String getReqDesc(User u){
        Iterator<Requirement> reqIter= reqDes.keySet().iterator();
        String reqs = "";

        while (reqIter.hasNext()) {
            Requirement r = reqIter.next();
            if (r.canUse(u)) {
                reqs = reqs + "<font color=#41A317>" + reqDes.get(r).call() + "</font><br>";
            } else {
                reqs = reqs + "<font color=#E42217>" + reqDes.get(r).call() + "</font><br>";
            }
        }

        return reqs;
    }

    public String getReqDesc(){
        String reqs = "<font color=#000000>Requires:</font><br>";
        Iterator<Requirement> reqIter= reqDes.keySet().iterator();

        if(reqIter.hasNext()) {
            while (reqIter.hasNext()) {
                Requirement r = reqIter.next();
                try {
                    reqs = reqs + "<font color=#000CCC>" + reqDes.get(r).call() + "</font><br>";
                }
                catch(Exception e){
                    Log.e("A requirement description call threw an exception", "Action>getReqDesc()");
                }
            }

            return reqs;
        }

        else{
            //no requirements
            return "";
        }

    }

    //must set curUser to uid as well as modAction with LogicCalc
    //also be sure to copy over the old value of prepTime if the action has a prep
    public abstract Action getCopy(int uId);

    //for viewing actions out of combat
    public abstract Action getCopy();


    //set needsAskToContinue to true if user needs to be asked if they want to continue
    //this is used when an action is set to curAction aka the user was using it and now they can continue to use it
    public void continueUse(){
        LogicCalc lc = new LogicCalc();
        GameManager gm = GameManager.getInstance();
        State stat = gm.getState();
        timeKeeper tk = gm.getTimeline();
        final gameAct context = gm.getGameAct();
        int userId = tk.turnObjectID;

        try {
            User u = (User) stat.getObjID(userId);

            if (lc.canUse(u, this)) {
                //user must be asked to continue action
                if(needsAskToContinue){

                    //create 2 buttons
                    ((LinearLayout) context.findViewById(R.id.actInOptions)).removeAllViews();

                    ((TextView) context.findViewById(R.id.actInInfo)).setText("Do you want to use " + name +" again?");
                    LinearLayout options = ((LinearLayout) context.findViewById(R.id.actInOptions));
                    options.removeAllViews();

                    final Typeface font = Typeface.createFromAsset(context.getAssets(), "njnaruto.ttf");

                    //the first button, yes, will make the user use this action as if they just clicked use on it
                    Button use = new Button(context);
                    use.setTypeface(font);
                    use.setText("Yes");
                    use.setTextColor(context.getResources().getColorStateList(R.color.white_text_button));
                    use.setBackground(context.getResources().getDrawable(R.drawable.brush1_button));
                    LinearLayout.LayoutParams useParams = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, context.getResources().getDisplayMetrics()),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics()));
                    useParams.gravity = Gravity.CENTER_HORIZONTAL;
                    use.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            useFront();
                        }
                    });

                    options.addView(use, useParams);

                    //the second button, no, will make the user return to the default actions menu
                    Button use2 = new Button(context);
                    use2.setTypeface(font);
                    use2.setText("No");
                    use2.setTextColor(context.getResources().getColorStateList(R.color.white_text_button));
                    use2.setBackground(context.getResources().getDrawable(R.drawable.brush1flip_button));
                    LinearLayout.LayoutParams useParams2 = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, context.getResources().getDisplayMetrics()),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics()));
                    useParams2.gravity = Gravity.CENTER_HORIZONTAL;
                    use2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            context.actionsReturn(null);
                        }
                    });

                    options.addView(use2, useParams2);
                }



                //dont need to ask, just do it
                else{
                    useFront();
                }
            }
            else{
                //user is unable to continue due to requirements
                context.actionsReturn(null);
            }
        }
        catch(RemovedException e){
            Log.e("continueUse in Action", "User cannot be found in state, why hasn't the game ended yet?");
        }
    }

    public void useFront() {
        GameManager gm = GameManager.getInstance();
        State stat = gm.getState();
        timeKeeper tk = gm.getTimeline();


        if (prepareTime != 0) {
            int curTime = stat.getTime();
            int actTime = curTime+prepareTime-1;

            int myId =  GameManager.getInstance().getTimeline().getId();

            //add the prepare action to the timeline
            final NewObjT startPrep = new NewObjT(Preparing.class, new Object[]{curUser, thisAct, requirements});
            startPrep.setOwnerID(curUser);

            ArrayList<SubAction> firstActions = new ArrayList<>();
            firstActions.add(startPrep);
            ArrayList<SubAction> mtArray = new ArrayList<>();

            ActionStep runStart = new ActionStep(myId, "Prepare "+name, requirements, curUser, minSpeed, firstActions, mtArray, 0, Stat.MISC);
            tk.addAction(curTime, runStart);



            //the second actionstep exists solely for the sake of canceling this action
            RemObjT remPrep = new RemObjT(startPrep.getObjTID);

            CustomSubAction setPrep = new CustomSubAction(new SubActCallable() {
                @Override
                public void callCont(User u, int speed) {
                    ArrayList<ObjT> types = u.getTypeSelf();
                    for(ObjT curType: types){
                        if(curType.id == startPrep.getObjTID.call()){
                            prepareTime = 0;
                            break;
                        }
                    }
                }

                @Override
                public void callStop(User u, int speed) {

                }
            });

            ArrayList<SubAction> endArrayCont = new ArrayList<>();
            endArrayCont.add(setPrep);
            endArrayCont.add(remPrep);

            ArrayList<SubAction> endArrayStop = new ArrayList<>();
            endArrayCont.add(remPrep);

            ActionStep prepEnd = new ActionStep(myId, "Prepare", requirements, curUser, 0, endArrayCont, endArrayStop, 0, Stat.MISC);
            tk.addAction(actTime, prepEnd);



            //tell the game manager that the user is currently "looking at" this action to use
            tk.setCurAction(this);

            //process time enough to prepare this action, also reloads screen
            gm.processTime(curTime, actTime+1);

        }
        else {
            useAction();
        }


    }

    //TODO be sure that when an action is actually done, call clearCurAction() if it is not being set with setCurAction() by this action
    public abstract void useAction();

    public abstract void mapClicked(Coord c);


    public int getTimeNeeded(int maxTime, boolean decrease){
        GameManager gm = GameManager.getInstance();
        int mod = 1;
        if(decrease)
            mod = -1;


        //out of game, calc based on character stats
        if(gm.getTimeline() == null){
            CharacterPlayer c = UserProfile.getInstance().curChar;
            int total = 0;
            int it = 0;
            for (Stat s : classes) {
                if (s != Stat.MISC) {
                    it++;
                    total = c.getStats().get(s);
                }
            }
            if (it != 0) {
                total = total / it;
                maxTime = maxTime + (mod* (int) ((double) maxTime / 3.0 * (double) total / 100.0));
            }
        }

        //in-game, calc based on user stats
        else {
            try {
                User u = (User) gm.getState().getObjID(gm.getTimeline().turnObjectID);
                int total = 0;
                int it = 0;
                for (Stat s : classes) {
                    if (s != Stat.MISC) {
                        it++;
                        total = u.getStats().get(s);
                    }
                }
                if(it != 0) {
                    total = total / it;
                    maxTime = maxTime + (mod* (int) ((double) maxTime / 3.0 * (double) total / 100.0));
                }
            } catch (RemovedException e) {
                Log.e("Action>getPrepTime", "couldnt calculate prep time because user does not exist in state");
            }
        }

        return maxTime;
    }


}
