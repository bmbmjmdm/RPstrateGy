package database.Levels;

import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;

import Managers.GameManager;
import Managers.Logic.LogicCalc;
import Managers.UserProfile;
import Managers.timeKeeper;
import Utilities.ClimbObj;
import Utilities.Direction;
import Utilities.RemovedException;
import Utilities.Stat;
import Utilities.UnableException;
import database.Actions.Action;
import database.Actions.Bark;
import database.Actions.Bite;
import database.Actions.Rest;
import database.Actions.Run;
import database.Actions.Sneak;
import database.Coord;
import database.ObjT.ObjT;
import database.ObjT.Stealthed;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.Objs.PObjs.*;
import database.Objs.PObjs.Character;
import database.Players.CharacterPlayer;
import database.Players.Player;
import database.Players.WolfPlayer;
import database.State;
import shenronproductions.app1.Activities.gameAct;

/**
 * Created by Dale on 8/23/2015.
 */
public class wolfPack extends Level {
    int[] wolfIDs;
    HashMap<Integer, Integer> wolfTurns = new HashMap<>();


    //TODO make these values based on char, possibly change when State needs these?
    public wolfPack(){
        super("wolfPack", "Wolf Pack");
    }


    @Override
    protected void setStateParams(){
        terrain = "Forest";
        turnLimit = "None";
        day = false;

        //set map size based on level difficulty
        if(difficulty <= 0.4){
            size = "Small";
        }
        else if (difficulty <= 2){
            size = "Medium";
        }
        else{
            size = "Large";
        }
    }

    //0 == not over
    //1 == user won!
    //-1 == user lost :(
    public int isOver(){
        State s = GameManager.getInstance().getState();
        timeKeeper tk = GameManager.getInstance().getTimeline();

        //see if user exists
        try{
            s.getObjID(tk.turnObjectID);
        }
        catch(RemovedException re){
            //user does not exist, game over
            return -1;
        }

        //see if any wolves exist
        for(int id: wolfIDs) {
            try {
                s.getObjID(id);
                //a wolf was found, game still going on
                return 0;
            }
            catch (RemovedException re) {}
        }

        //no wolves found and user is alive, victory!
        CharacterPlayer player1 = UserProfile.getInstance().curChar;
        player1.wolfLevel = difficulty + 0.15;
        try {
            UserProfile.getInstance().saveChar();
        }
        catch(Exception e){
            Log.e("Exception while saving character in isOver", e.getMessage());
        }

        return 1;
    }


    public String getEndMessage(int status){
        //victory
        if(status > 0){
            return "You have slain the wolves! Better start running before the vultures settle in...";
        }
        //defeat
        else{
            return "Your body is ripped to shreds by the carnivorous beasts. Nasty.";
        }
    }


    public void getNarration(LinearLayout container){
        State s = GameManager.getInstance().getState();
        gameAct gc = GameManager.getInstance().getGameAct();
        int numLeft = 0;

        //count how many are left in the state
        for(int i : wolfIDs){
            try {
                s.getObjID(i);
                numLeft++;
            }
            catch(RemovedException e){}
        }

        //make a textView and tell the user their progress
        Typeface font = Typeface.createFromAsset(gc.getAssets(), "njnaruto.ttf");

        int sp14 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, gc.getResources().getDisplayMetrics());
        int dp20 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, gc.getResources().getDisplayMetrics());

        final TextView tV = new TextView(gc);
        tV.setText(Html.fromHtml("There &#160;are &#160;<font color=#E42217>" + numLeft + "</font> &#160;wolves &#160;left!"));
        tV.setTypeface(font);
        tV.setTextSize(sp14);
        tV.setPadding(0, 0, 0, dp20);
        tV.setGravity(Gravity.CENTER);

        //make the text container fill its parent and center
        int fill = LinearLayout.LayoutParams.MATCH_PARENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(fill, fill);
        params.gravity = Gravity.CENTER;
        tV.setLayoutParams(params);

        container.addView(tV);

        //make the linearlayout containing the text container fill its parent and center
        FrameLayout.LayoutParams paramsContainer = new FrameLayout.LayoutParams(fill, fill);
        params.gravity = Gravity.CENTER;
        container.setLayoutParams(paramsContainer);

        //make the scrollview containing the linearlayout fill its parent
        ScrollView scroller = (ScrollView) container.getParent();
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(fill, fill);
        scroller.setLayoutParams(scrollParams);
    }



    public void setUpState() throws UnableException {
        GameManager gm = GameManager.getInstance();
        State s = GameManager.getInstance().getState();
        timeKeeper time = gm.getTimeline();

        CharacterPlayer player1 = UserProfile.getInstance().curChar;

        //6* difficulty, rounded down, min 1
        int numWolves = (int)  Math.max(1, Math.floor(difficulty*6));

        //create and randomly place user
        User u = new Character(player1, 1, player1.name);
        s.getRandomFreeCoord(u, (int) (difficulty *1000));
        time.turnObjectID = u.id;
        u.minimumVision();

        //now wolves
        wolfIDs  = new int[numWolves];
        for(int i = 0; i < numWolves; i++){

            //create each
            Player wolfPlayer = new WolfPlayer(difficulty);
            Wolf wolf = new Wolf(wolfPlayer, 2, "Wolf "+(i+1));

            //remember their id/turn time
            wolfIDs[i] = wolf.id;
            wolfTurns.put(wolf.id, 1+i);

            //randomly place it
            s.getRandomFreeCoord(wolf, (int) (difficulty *1000));
            wolf.minimumVision();
        }

    }



    public void doTurn(){
        //one of the wolves must have a time == nextTurn, which curTIme equals
        for(int wolf : wolfIDs){
            if(wolfTurns.get(wolf) == nextTurn){
                //do the AI, which returns when that wolve's next turn is as well
                wolfTurns.put(wolf, wolfAI(wolf));

                //we don't break because there may be multiple wolves that have their turn now
            }
        }

        int lowestNextTurn = Integer.MAX_VALUE;
        //find the next wolf turn, which must be higher than the current nextTurn but the lowest one that is
        for(int wolf : wolfIDs){
            int wolfTurn = wolfTurns.get(wolf);

            if(wolfTurn > nextTurn){
                if(wolfTurn < lowestNextTurn){
                    lowestNextTurn = wolfTurn;
                }
            }
        }

        //this can be MAX_VALUE, but that doesnt matter because the game would end right after processing
        nextTurn = lowestNextTurn;
    }


    private int wolfAI(int wolfID){
        try {
            GameManager gm = GameManager.getInstance();
            State s = GameManager.getInstance().getState();
            timeKeeper time = gm.getTimeline();

            Wolf wolf = (Wolf) s.getObjID(wolfID);

            int nextActionTime = time.getNextASTime(nextTurn, wolfID);

            //check to see if the current wolf has their last action LATER than the current time, indicating things got switched around (probably stopped an action and the stopped action pushed past current time)
            if(nextActionTime != -1){
                //return when their next available action Actually is
                return nextActionTime+1;
            }

            //if not, run the AI to produce an action and update this wolf's nextTurn
            else{
                boolean offensive = shouldBeOffensive(wolf);
                boolean defensive = shouldBeDefensive(wolf);

                //if wolf has a reason to be defensive, it will never be neutral
                if(defensive){

                    //it may, however, randomly act offensively
                    if(offensive){
                        ArrayList<Integer> involved = new ArrayList<>();
                        involved.add(wolf.id);
                        double offensiveChance = time.getRand(involved, "Offensive chance while defensive");

                        //15% chance to be offensive
                        if(offensiveChance > 0.85){
                            //we are acting offensively
                            int offensiveTime = beOffensive(wolf);

                            //make sure we actually did an action, and return the next available action time if so
                            if(offensiveTime != -1){
                                return offensiveTime;
                            }
                        }
                    }

                    //we are not acting offensively
                    int defensiveTime = beDefensive(wolf);

                    //make sure we actually did an action, and return the next available action time if so
                    if(defensiveTime != -1){
                        return defensiveTime;
                    }
                }

                //if wolf has a reason to be offensive, it will try to be aggresive
                if(offensive){

                    //it may, however, randomly act neutral
                    ArrayList<Integer> involved = new ArrayList<>();
                    involved.add(wolf.id);
                    double neutralChance = time.getRand(involved, "Neutral chance while offensive");

                    //15% chance to be neutral
                    if(neutralChance > 0.85){
                        //we are acting neutrally
                        int neutralTime = beNeutral(wolf);

                        //make sure we actually did an action, and return the next available action time if so
                        if(neutralTime != -1){
                            return neutralTime;
                        }
                    }

                    //we are not acting neutrally
                    int offensiveTime = beOffensive(wolf);

                    //make sure we actually did an action, and return the next available action time if so
                    if(offensiveTime != -1){
                        return offensiveTime;
                    }
                }

                //we are not acting offensively or defensively
                int neutralTime = beNeutral(wolf);

                //make sure we actually did an action, and return the next available action time if so
                if(neutralTime != -1){
                    return neutralTime;
                }
            }
        }

        //wolf has been removed from state
        catch(RemovedException e){
            //return -1 so this wolf's turn will never come
            return -1;
        }

        //if we got here it is because the wolf tried but was not able to make an action
        //try again next time available
        return nextTurn+1;
    }


    //Returns whether the wolf has a reason to act defensively
    private boolean shouldBeDefensive(Wolf w){
        //if the wolf was damaged within the past 0.5 seconds, be defensive
        if(w.lastTimeDamaged() > nextTurn-500){
            return true;
        }

        return false;
    }


    //Returns whether the wolf has a reason to act aggressively
    private boolean shouldBeOffensive(Wolf w){
        //if health is less than 5%, dont be offensive
        if(w.getHealthPercent() < 0.05) {
            return false;
        }


        timeKeeper time = GameManager.getInstance().getTimeline();

        //if the player is within sight, be offensive
        if(w.getVision(time.turnObjectID).size() > 0){
            return true;
        }


        return false;
    }








    //returns -1 if the wolf could not be offensive for whatever reason
    private int beOffensive(Wolf w){
        try {
            timeKeeper time = GameManager.getInstance().getTimeline();
            State s = GameManager.getInstance().getState();
            LogicCalc lc = new LogicCalc();

            ArrayList<Coord> canSeeAt = w.getVision(time.turnObjectID);

            //we dont want to check multiple coordinates that have the same x and y
            ArrayList<Coord> canSeeNoDup = new ArrayList<>();
            for(Coord c: canSeeAt){
                boolean isDup = false;

                for(Coord c2: canSeeNoDup){
                    if(c.x == c2.x)
                        if(c.y == c2.y){
                            isDup = true;
                            break;
                        }
                }

                if(!isDup)
                    canSeeNoDup.add(c);
            }


            ArrayList<CObj> children = s.getObjID(time.turnObjectID).getAllLeafs();
            Coord torso = w.getTorsoCoord();
            int headMiddle = torso.z+(w.getHead().getHeight()/2);

            //try biting
            //look at each coord the user is at.
            for (Coord c : canSeeNoDup) {

                //if the wolf is adjacent to it
                if (c.x >= torso.x - 1)
                    if (c.x <= torso.x + 1)
                        if (c.y <= torso.y + 1)
                            if (c.y >= torso.y - 1) {

                                //if the wolf can reach it z-wise
                                HashSet<CObj> hitem = lc.getCObjAround(new Coord(c.x, c.y, headMiddle), w.getZMovement(), children);

                                if(hitem.size() > 0){
                                    //we found parts of the user we can bite at. immediately bite and finish

                                    int afterBite = useBite(w, hitem, new Coord(c.x, c.y));
                                    if(afterBite != -1)
                                        return afterBite;
                                }
                            }
            }

            //cant bite at any of the user's locs, try running towards their center
            Coord userMiddle = s.getObjID(time.turnObjectID).getMiddlemostCoord();

            //try to run towards user, returning either the next time we can use an action or -1 if cant run
            return tryRunTowards(w, userMiddle);
        }

        catch(RemovedException re){
            //user has been removed from state, cant do anything
            return -1;
        }
    }








    //returns -1 if the wolf could not be defensive for whatever reason
    private int beDefensive(Wolf w){
        try {
            timeKeeper time = GameManager.getInstance().getTimeline();

            //try to run away from user, returning either the next time we can use an action or -1 if cant run
            State s = GameManager.getInstance().getState();
            Coord userMiddle = s.getObjID(time.turnObjectID).getMiddlemostCoord();

            return tryRunAway(w, userMiddle);

        }
        catch(RemovedException re){
            //user has been removed from state, cant do anything
            return -1;
        }
    }







    private int beNeutral(Wolf w){
        timeKeeper tk = GameManager.getInstance().getTimeline();

        boolean shouldHeal = false;
        boolean shouldRest = false;
        boolean shouldStealth;

        if(w.getHealthPercent() < 1)
            shouldHeal = true;

        //ugly expression is stamina percent
        if(((double) w.getStat(Stat.CUR_STAMINA))/w.getStat(Stat.MAX_STAMINA) < 1)
            shouldRest = true;


        //check whether we should use the stealth action (whether its turning on or off)
        boolean alreadyStealthed = isWolfStealthed(w);
        if(alreadyStealthed)
            shouldStealth = shouldStopStealth(w);
        else
            shouldStealth = shouldUseStealth(w);







        //now there's a percent chance to do the following actions in the order they appear
        ArrayList<Integer> ranVals = new ArrayList<>();
        ranVals.add(w.id);



        //start/stop stealth - 90%
        double neutralRand = tk.getRand(ranVals, "Neutral random action stealth");
        if(neutralRand > 0.1 && shouldStealth){

            //use rest and return if successful
            int nextMove = useSneak(w, alreadyStealthed);
            if(nextMove != -1)
                return nextMove;
        }



        //rest - 50%
        neutralRand = tk.getRand(ranVals, "Neutral random action rest");
        if(neutralRand > 0.5 && shouldRest){

            //use rest and return if successful
            int nextMove = useRest(w);
            if(nextMove != -1)
                return nextMove;
        }




        //lick wound - 50%
        /*neutralRand = tk.getRand(ranVals, "Neutral random action heal");
        if(neutralRand > 0.5 && shouldHeal){

            //use lick wound and return if successful
            int nextMove = useLickWound(w);
            if(nextMove != -1)
                return nextMove;

        }*/



        //run randomly - 75%
        neutralRand = tk.getRand(ranVals, "Neutral random action run rand");
        if(neutralRand > 0.25){

            //use random run and return if successful
            int nextMove = tryRunRandom(w);
            if(nextMove != -1)
                return nextMove;
        }




        //bark - default
        //use bark and return if successful
        int nextMove = useBark(w);
        if(nextMove != -1)
            return nextMove;


        //if we get here that means the wolf cannot make ANY actions this turn, but still exists. queue him for the next millisecond
        return nextTurn+1;
    }









    private int tryRunAway(Wolf w, Coord to){
        LogicCalc lc = new LogicCalc();

        //find where we can run
        HashMap<Coord, HashSet<ClimbObj>> canMove = lc.movementHashMap(w.id, 1, w.getZMovement(), w.getZMovement(), false, true);
        Coord torso = w.getTorsoCoord();
        boolean stealthy = isWolfStealthed(w);

        //find the coord furthest to the user
        Coord runTo = null;
        //use the wolf's current position as the current best coord
        int preDifY = Math.abs(torso.x-to.x);
        int preDifX = Math.abs(torso.y - to.y);
        int bestDist = preDifX+preDifY;


        //for each coord we can run to
        for(Coord c: canMove.keySet()){

            //find the distance from user
            int xDif = Math.abs(c.x-to.x);
            int yDif = Math.abs(c.y - to.y);
            int distance = xDif+yDif;
            boolean stealthySpot = false;

            //if the spot is stealthy, it gets a +1
            if(stealthy){
                if(isSpotStealthy(c, w)){
                    distance++;
                    stealthySpot = true;
                }
            }

            // keep the coord if it is further than our current furthest coord
            if (distance > bestDist) {
                runTo = c;
                bestDist = distance;
            }

            //if the distance is equal to the furthest coord but this one is stealthy, prefer this
            if((distance == bestDist) && stealthySpot){
                runTo = c;
            }
        }

        //if we found a spot to run to, run
        if(runTo != null){
            //there is a spot we can move to (furthest) that will bring us further away from the user
            ClimbObj moveOn = canMove.get(runTo).iterator().next();

            //immediately run and return the next action time if successful or -1 if not
            return useRun(w, moveOn, runTo);
        }
        else
            return -1;
    }









    private int tryRunTowards(Wolf w, Coord to){
        LogicCalc lc = new LogicCalc();

        //find where we can run
        HashMap<Coord, HashSet<ClimbObj>> canMove = lc.movementHashMap(w.id, 1, w.getZMovement(), w.getZMovement(), false, true);
        Coord torso = w.getTorsoCoord();
        boolean stealthy = isWolfStealthed(w);

        //find the coord closest to the user
        Coord runTo = null;

        //use the wolf's current position as the current best coord
        Direction toUser = Direction.findDir(torso, to);
        int bestDistClicks = Direction.degreesDifference(toUser, Direction.NONE);


        //for each coord we can run to
        for(Coord c: canMove.keySet()){

            //find the direction this coord will be moving us in
            Direction thisDir = Direction.findDir(torso, c);
            int dirClicks = Direction.degreesDifference(toUser, thisDir);
            boolean stealthySpot = false;

            //if the spot is stealthy, it gets a -1 clicks
            if(stealthy){
                if(isSpotStealthy(c, w)){
                    dirClicks--;
                    stealthySpot = true;
                }
            }

            //keep the coord if it is better aligned to bring us towards the user
            if (dirClicks < bestDistClicks) {
                runTo = c;
                bestDistClicks = dirClicks;
            }

            //if the distance is equal to the closest coord but this one is stealthy, prefer this
            if((dirClicks  == bestDistClicks) && stealthySpot){
                runTo = c;
            }
        }

        //if we found a spot to run to, run
        if(runTo != null){
            //there is a spot we can move to (closest) that will bring us closer to the user
            ClimbObj moveOn = canMove.get(runTo).iterator().next();

            //immediately run and return the next action time if successful or -1 if not
            return useRun(w, moveOn, runTo);
        }
        else
            return -1;
    }










    private int tryRunRandom(Wolf w){
        LogicCalc lc = new LogicCalc();
        timeKeeper tk = GameManager.getInstance().getTimeline();
        ArrayList<Integer> ranVals = new ArrayList<>();
        ranVals.add(w.id);


        //find where we can run to
        HashMap<Coord, HashSet<ClimbObj>> canMove = lc.movementHashMap(w.id, 1, w.getZMovement(), w.getZMovement(), false, true);
        if(!canMove.isEmpty()) {




            //if the wolf is stealthed, pick the first stealthy spot that is available
            if (isWolfStealthed(w)) {
                Iterator<Coord> iterate = canMove.keySet().iterator();

                while (iterate.hasNext()) {
                    Coord checkMe = iterate.next();

                    //the spot is stealthy
                    if (isSpotStealthy(checkMe, w)) {
                        ClimbObj co = canMove.get(checkMe).iterator().next();
                        //use run and return if successful
                        int nextMove = useRun(w, co, checkMe);
                        if (nextMove != -1)
                            return nextMove;
                    }
                }
            }





            //cannot stealthily run, pick a random coord
            double neutralRand = tk.getRand(ranVals, "Neutral run in random direction");
            int randomIndex = (int) Math.min(canMove.size() - 1, Math.floor(neutralRand * canMove.size()));

            Coord c = null;
            Iterator<Coord> iterate = canMove.keySet().iterator();

            for (int i = 0; i < canMove.size(); i++) {
                Coord checkMe = iterate.next();
                if (i == randomIndex) {
                    c = checkMe;
                    break;
                }
            }

            ClimbObj co = canMove.get(c).iterator().next();

            //use run and return whether successful or not
            return useRun(w, co, c);
        }




        //nowhere to run to
        return -1;
    }


















    private boolean isWolfStealthed(Wolf w){
        for(ObjT type: w.getTypePath()){
            if(type.isStealthed())
                return true;
        }

        return false;
    }


    private boolean shouldUseStealth(Wolf w){
        //get our copy of sneak
        for(Action a: w.actions) {
            if (a instanceof Sneak) {
                LogicCalc lc = new LogicCalc();

                //make sure we can use it
                if(!lc.canUse(w, a)){
                    return false;
                }

                //do we have > 50% focus?
                double focusPercent = ((double) w.getStat(Stat.CUR_FOCUS))/w.getStat(Stat.MAX_FOCUS);
                if(focusPercent < 0.5){
                    return false;
                }

                //are we within range of the user + 5?
                try {
                    timeKeeper time = GameManager.getInstance().getTimeline();
                    State s = GameManager.getInstance().getState();

                    User curUser = (User) s.getObjID(time.turnObjectID);
                    Coord userCoord = curUser.getMiddlemostCoord();
                    Coord wolfCoord = w.getMiddlemostCoord();
                    int visDist = lc.getVisionDistance(curUser);

                    if(userCoord.distance(wolfCoord) > visDist+5){
                        return false;
                    }

                    //we are within range, able to use stealth, and have enough focus
                    return true;
                }

                //user was removed ???
                catch(RemovedException re){
                    return false;
                }
            }
        }

        //sneak was not found
        return false;
    }




    private boolean shouldStopStealth(Wolf w){
        //are we within range of the user + 5?
        try {
            LogicCalc lc = new LogicCalc();
            timeKeeper time = GameManager.getInstance().getTimeline();
            State s = GameManager.getInstance().getState();

            User curUser = (User) s.getObjID(time.turnObjectID);
            Coord userCoord = curUser.getMiddlemostCoord();
            Coord wolfCoord = w.getMiddlemostCoord();
            int visDist = lc.getVisionDistance(curUser);

            if(userCoord.distance(wolfCoord) <= visDist+5){
                //you are in range, do not turn off stealth!
                return false;
            }

        }

        //user was removed ???
        catch(RemovedException re){}

        //you are outside of range, no need to stealth
        return true;
    }




    private boolean isSpotStealthy(Coord on, Wolf w) {
        //lets pretend the wolf is on that spot
        w.useImaginaryLoc(on);

        //get our copy of sneak
        for(Action a: w.actions) {
            if (a instanceof Sneak) {
                LogicCalc lc = new LogicCalc();

                //see if we can stealth
                Sneak sneak = (Sneak) a.getCopy(w.id);
                boolean wolfStealth = true;

                //now look at each part of the wolf
                for (CObj wolfPart : w.getAllLeafs()) {

                    //if theres at least one wolfPart on the spot that wouldnt be concealed, this wolf as a whole is considered to not be concealed
                    if (!lc.conceals(wolfPart, on, sneak.widthMod, sneak.heightMod)) {
                        wolfStealth = false;
                        break;
                    }

                }

                w.stopUsingImaginaryLoc();
                return wolfStealth;
            }
        }

        //sneak was not found
        w.stopUsingImaginaryLoc();
        return false;
    }





    //returns when the wolf's next move will be, or -1 if cant bite
    private int useBite(Wolf w, HashSet<CObj> co, Coord coord){
        LogicCalc lc = new LogicCalc();


        //extract a random CObj from the list to bite
        ArrayList<Integer> ranVals = new ArrayList<>();
        ranVals.add(w.id);
        double bitingRand = GameManager.getInstance().getTimeline().getRand(ranVals, "Bite random target " + coord.x + "," + coord.y);
        int randomIndex = (int) Math.min(co.size()-1, Math.floor(bitingRand*co.size()));

        CObj biting = null;
        Coord realCoord = null;
        Iterator<CObj> iterate = co.iterator();
        boolean found = false;

        for(int i = 0; i < co.size(); i++){
            CObj checkMe = iterate.next();
            if(i == randomIndex){
                found = true;
                biting = checkMe;
                int middleOfHeadZ = w.getTorsoCoord().z+(w.getHead().getHeight()/2);
                Coord torsoC = new Coord(coord.x, coord.y, middleOfHeadZ);
                realCoord = new Coord(coord.x, coord.y, lc.getAccCenter(checkMe, torsoC, w.getZMovement()));
            }
        }


        //find bite
        if(found) {
            for (Action a : w.actions) {
                if (a instanceof Bite) {
                    Bite bite = (Bite) a.getCopy(w.id);

                    //make sure we can use bite first
                    if (lc.canUse(w, bite)) {
                        //bite and return
                        return bite.useWolf(biting, realCoord);
                    }

                    //cannot use bite
                    else
                        return -1;
                }
            }
        }

        //no bite found or no cobj to bite
        return -1;
    }


    //returns when the wolf's next move will be, or -1 if cant run
    private int useRun(Wolf w, ClimbObj co, Coord coord){
        LogicCalc lc = new LogicCalc();

        //find run
        for(Action a: w.actions){
            if(a instanceof Run){
                Run run = ((Run) a).getWolfCopy(w.id);

                //make sure we can use run first
                if(lc.canUse(w, run)){
                    //run and return
                    return run.useWolf(co, coord);
                }

                //cannot use run
                else
                    return -1;
            }
        }

        //no run found
        return -1;
    }



    //returns when the wolf's next move will be, or -1 if cant bark
    private int useBark(Wolf w){
        LogicCalc lc = new LogicCalc();

        //find bark
        for(Action a: w.actions){
            if(a instanceof Bark){
                Bark bark = (Bark) a.getCopy(w.id);

                //make sure we can use bite first
                if(lc.canUse(w, bark)){

                    return bark.useWolf(difficulty);
                }

                //cannot use bark
                else
                    return -1;
            }
        }

        //no bark found
        return -1;
    }


    //returns when the wolf's next move will be, or -1 if cant lick wound
    /*private int useLickWound(Wolf w){
        LogicCalc lc = new LogicCalc();

        //find lick wound
        for(Action a: w.actions){
            if(a instanceof LickWound){
                LickWound lick = (LickWound) a;

                //make sure we can use lick wound first
                if(lc.canUse(w, lick)){

                    //TODO make the lickwound action and return when wolf's next move is
                }

                //cannot use lick wound
                else
                    return -1;
            }
        }

        //no lick wound found
        return -1;
    }*/


    //returns when the wolf's next move will be, or -1 if cant rest
    private int useRest(Wolf w){
        LogicCalc lc = new LogicCalc();

        //find rest
        for(Action a: w.actions){
            if(a instanceof Rest){
                Rest rest = (Rest) a.getCopy(w.id);

                //make sure we can use rest first
                if(lc.canUse(w, rest)){

                    return rest.useWolf();
                }

                //cannot use rest
                else
                    return -1;
            }
        }

        //no rest found
        return -1;
    }



    //returns when the wolf's next move will be, or -1 if cant sneak
    private int useSneak(Wolf w, boolean alreadyStealthed){
        LogicCalc lc = new LogicCalc();

        //find bark
        for(Action a: w.actions){
            if(a instanceof Sneak){
                Sneak sneak = ((Sneak) a).getWolfCopy(w.id, difficulty);

                //make sure we can use bite first
                if(lc.canUse(w, sneak)){

                    return sneak.useWolf(alreadyStealthed);
                }

                //cannot use bark
                else
                    return -1;
            }
        }

        //no bark found
        return -1;
    }

}
