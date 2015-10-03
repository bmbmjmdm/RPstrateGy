package Managers.Logic;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import Utilities.ClimbObj;
import Utilities.IntPair;
import Utilities.RemovedException;
import Utilities.Stat;
import Utilities.UnspecifiedMovement;
import database.Coord;
import database.Narration;
import database.ObjT.Falling;
import database.ObjT.Moving;
import database.ObjT.ObjT;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.State;
import Managers.GameManager;

/**
 * Created by Dale on 4/29/2015.
 */
public class MovementLogic {
    GameManager sh = GameManager.getInstance();
    LogicCalc lc = new LogicCalc();

    //Moving stance checks for range and space available
    //moving stance collides with as much as possible
    public void processMoving(Moving moveOT){
        State s = sh.getState();

        try {
            Obj user = s.getObjID(moveOT.belongsTo);

            Coord fallingCoord = user.getMiddlemostCoord();
            Coord toC = moveOT.movement().get(0);
            Coord newC = new Coord(toC.x, toC.y);
            int xDif = Math.abs(newC.x-fallingCoord.x);
            int yDif = Math.abs(newC.y-fallingCoord.y);
            int distance = xDif > yDif ? xDif : yDif;
            boolean canMove = false;
            Obj onto = null;


            //this means that the user is able to move onto the object that it originally was trying to move onto
            if(distance <= moveOT.range) {

                //check to see if there is space enough for the user
                //if specified, check to make sure the obj the user is moving onto is present at an acceptable location
                try {
                    onto = s.getObjID(moveOT.getLandsOn());

                    HashSet<ClimbObj> canMoveList = movementOnCoord(user.id, newC, moveOT.getzMovementUp(), moveOT.getzMovementDown(), moveOT.getClimbing(), moveOT.takesUpSpace);
                    for (ClimbObj check : canMoveList) {
                        if (check.co == onto)
                            if (check.climbPoints.contains(toC.z))
                                canMove = true;
                    }
                }

                catch(UnspecifiedMovement e){
                    if(moveOT.takesUpSpace) {
                        if (canFit(user, toC, 75))
                            canMove = true;
                    }
                    else
                        canMove = true;
                }


                if (canMove) {

                    //first the user moves, this auto updates whatever the user was standing on to tell it the user is no longer standing on it
                    user.move(moveOT.movement(), true);

                    if(onto != null) {
                        //update whatever the user moves to to tell it the user is now standing on it
                        user.standOnThis(onto.id);
                        onto.supportThis(user.id);
                    }

                    //do narration
                    if(moveOT.called != null) {
                        HashSet<Obj> narrationInvolves = new HashSet<>();
                        narrationInvolves.add(user);
                        String text = user.name + " is " + moveOT.called;
                        if(onto != null) {
                            narrationInvolves.add(onto);
                            text = text + " onto " + onto.name + ".";
                        }
                        new Narration(text, narrationInvolves, Stat.ACROBAT);
                    }




                    TreeSet<CObj> onSpot = sh.getState().getObjCBelow(new Coord(toC.x, toC.y, user.getHighestPoint()+1));

                    //now we do collisions. first, try to find objects wer are Trying to collide with
                    try {

                        Iterator<CObj> it = onSpot.iterator();
                        HashSet<CObj> colliding = new HashSet<>();
                        HashSet<Integer> tryingToCollide = moveOT.getCollide();
                        //if we found any of the obj we are trying to collide with, they are removed from the onSpot list and added to colliding
                        while (it.hasNext()) {
                            CObj co = it.next();
                            for(Integer curI: tryingToCollide) {
                                if (co.isOrIsPartOf(curI)) {
                                    it.remove();
                                    colliding.add(co);
                                }
                            }
                        }

                        //for all obj we found that we're trying to collide with, attempt collision at 2x rate
                        if (!colliding.isEmpty()) {
                            for(CObj co: colliding) {
                                double chanceCollide = (user.collides(co, 0)) * 2;
                                ArrayList<Integer> randomInputs = new ArrayList<>();
                                randomInputs.add(user.id);
                                randomInputs.add(co.id);
                                double rand = sh.getTimeline().getRand(randomInputs, "Collision while "+moveOT.name);
                                if (chanceCollide > rand) {
                                    lc.collision(user, co);
                                }
                            }
                        }
                    }

                    catch(UnspecifiedMovement e){
                        //there is nothing to specificly collide with, move on
                    }


                    //check rest of the obj on the spot for collisions
                    for (CObj co : onSpot) {
                        if (user.fullContainsPath(co))
                            continue;
                        double chanceCollide = user.collides(co, 0);
                        ArrayList<Integer> randomInputs = new ArrayList<>();
                        randomInputs.add(user.id);
                        randomInputs.add(co.id);
                        double rand = sh.getTimeline().getRand(randomInputs, "Collision while "+moveOT.name);
                        if (chanceCollide > rand) {
                            lc.collision(user, co);
                            break;
                        }
                    }



                    //now update vision
                    lc.updateVisionOf(user);

                }
            }

            //if the user could not move, this block gets called to tell the player they failed at moving
            if(!canMove){
                String called = moveOT.called;
                if(called == null)
                    called = "Moving";
                HashSet<Obj> landNarrationInvolves = new HashSet<>();
                landNarrationInvolves.add(user);
                String landText = user.name + " failed to complete the action: " + called;
                //add narration
                new Narration(landText, landNarrationInvolves, Stat.MISC);
            }

            //finally, remove the type
            user.removeTypeSelf(moveOT.id);
        }

        catch(RemovedException e){
            Log.e("processMoving of moveOT " + moveOT.id, "Either the user moving or the obj they were moving onto has been removed from the state");
        }

    }









    //TODO currently falling works by assuming each obj can be "moved" by only moving its lowest coord
    //TODO currently assumes falling obj exists on one x/y point
    //note this assumes that anything who's top is equal or above the user's bottom will not be hit on the way down
    public void processFalling(Falling moveOT, int fallDist){
        State s = sh.getState();

        try {
            //first grab list of objects on the same x/y
            Obj user = s.getObjID(moveOT.belongsTo);
            Coord Loc = user.getMiddlemostCoord();
            final int userBottom = user.getLowestZ();
            final int userX = Loc.x;
            final int userY = Loc.y;
            ArrayList<CObj> onSpot = new ArrayList<>();
            onSpot.addAll(sh.getState().getObjCBelow(new Coord(userX, userY, userBottom+1)));

            //then find all that exist within the 0.5m below the user
            Iterator<CObj> cycler = onSpot.iterator();
            while(cycler.hasNext()){
                CObj co = cycler.next();
                int coBot = co.getZ(userX, userY);
                int coTop = coBot + co.getHeight();
                if((coTop > userBottom) ||
                        (coTop+fallDist < userBottom) ||
                        user.contains(co))
                    cycler.remove();
            }

            //order each of these lists from highest top to lowest top
            Collections.sort(onSpot, new Comparator<CObj>() {
                @Override
                public int compare(CObj lhs, CObj rhs) {
                    int lhsTop = lhs.getZ(userX, userY)+lc.getStepableHeight(lhs);
                    int rhsTop = rhs.getZ(userX, userY)+lc.getStepableHeight(rhs);
                    if(lhsTop == rhsTop){
                        lhsTop = lhs.getZ(userX, userY)+lhs.getHeight();
                        rhsTop = rhs.getZ(userX, userY)+rhs.getHeight();
                    }

                    return rhsTop - lhsTop;

                }
            });

            boolean stillFalling = true;
            //for each item in the list, proboblistically caluclate whether the user hits it
            for(CObj co: onSpot){
                if(!stillFalling)
                    break;
                ArrayList<Integer> randomInputs = new ArrayList<Integer>();
                randomInputs.add(user.id);
                randomInputs.add(co.id);
                double rand = sh.getTimeline().getRand(randomInputs, "Collision while falling");
                double chanceCollide = user.collides(co, fallDist);

                //if they hit something, if that thing's width is above threashold, see if what they hit is standable. if it is, the user is done falling
                if(chanceCollide > rand){
                    //narration setup
                    boolean narrate = false;
                    if(moveOT.counter >= 3)
                        narrate = true;

                    HashSet<Obj> landNarrationInvolves = new HashSet<>();
                    landNarrationInvolves.add(user);
                    landNarrationInvolves.add(co);
                    String landText = user.name + " collided with " + co.name + " while falling!";

                    if(co.getWidth() >= 39){
                        boolean standable = false;
                        for(ObjT ot: co.getTypePath())
                            if(ot.standable())
                                standable = true;
                        if(standable){
                            stillFalling = false;
                            user.removeTypeSelf(moveOT.id);
                            ArrayList<Coord> moveTo = new ArrayList<>();
                            moveTo.add(new Coord(userX, userY, co.getZ(userX, userY)+lc.getStepableHeight(co)));
                            //first the user moves, this auto updates whatever the user was standing on to tell it the user is no longer standing on it
                            user.move(moveTo, true);

                            //update whatever the user moves to to tell it the user is now standing on it
                            user.standOnThis(co.id);
                            co.supportThis(user.id);

                            lc.updateVisionOf(user);

                            landText = user.name+" landed on " + co.name+".";
                            narrate = true;
                        }
                    }

                    //add narration
                    if(narrate)
                        new Narration(landText, landNarrationInvolves, Stat.MISC);

                    //calculate damage in full and reset the counter of the fall objT.
                    lc.collision(user, co);
                }

                //if the user is still falling, make sure they can fit beyond the object they just passed
                if(stillFalling) {
                    int coBot = co.getZ(userX, userY);
                    int coTop = coBot + co.getHeight();
                    if (!canFit(user, new Coord(userX, userY, coTop-1), 75)){

                        //if they cannot, move them to an adjascent open spot
                        int modX = 0;
                        int modY = 0;
                        while(true){
                            modX++;
                            modY++;
                            for(int uX = userX-modX; uX <= userX+modX; uX+=modX) {
                                for (int uY = userY - modY; uY <= userY + modY; uY += modY) {
                                    if (uX == 0 && uY == 0)
                                        continue;
                                    Coord newC = new Coord(uX, uY, coTop - 1);
                                    if (!s.testOffMap(newC)) {
                                        if(canFit(user, newC, 75)) {
                                            ArrayList<Coord> moveHere = new ArrayList<>();
                                            moveHere.add(newC);
                                            user.move(moveHere, true);
                                            lc.updateVisionOf(user);
                                            processFalling(moveOT, fallDist - (userBottom - (coTop + 1)));
                                            return;
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }

            //if they do not hit anything standable, re-add the falling objT to the EOT objT's and increment its counter. Additionally move the user down 0.5m
            if(stillFalling){
                ArrayList<Coord> moveTo = new ArrayList<>();
                moveTo.add(new Coord(userX, userY, userBottom-fallDist));
                user.move(moveTo, true);
                lc.updateVisionOf(user);
                moveOT.counter++;
                s.addEOTObjT(moveOT.id, s.getTime()+67);
            }

        }

        catch(RemovedException e){
            Log.e("processFalling of moveOT " + moveOT.id, "The user falling has been removed from the state");
        }
    }





















    //loc can be null
    public boolean climbable(CObj co, Coord loc, int up, int down, int uBot){
        ArrayList<ObjT> types = co.getTypePath();
        boolean canClimb = stepable(co, loc, up, down, uBot);

        int top;
        int bot;

        if(loc == null)
            bot = co.getLowestZ();
        else
            bot =  co.getZ(loc.x, loc.y);

        top = bot+co.getHeight();


        //now check side to see if it can be climbed on
        boolean climbableSide = false;
        for(ObjT t: types){
            if(t.climable())
                climbableSide = true;
        }

        if(((uBot <= top) && ((uBot+up) >= bot)) ||
                ((uBot >= top) && ((uBot-down) <= top))){ //TODO fix this so it takes into consideration the users abilities, as well as can show partial climbability (incase objects are slanted over many spaces, for example)
            if(climbableSide)
                canClimb = true;
        }


        return canClimb;
    }






    //loc can be null
    public boolean stepable(CObj co, Coord loc, int up, int down, int uBot){
        ArrayList<ObjT> types = co.getTypePath();
        boolean canClimb = false;

        //first check top if it can be climbed up to or down to
        boolean climbableTop = false;
        for(ObjT t: types){
            if(t.standable())
                climbableTop = true;
        }

        int top = getStepableHeight(co);

        if(loc == null)
            top += co.getLowestZ();
        else
            top += co.getZ(loc.x, loc.y);


        if(((uBot <= top) && ((uBot+up) >= top)) ||
                ((uBot >= top) && ((uBot-down) <= top))){ //TODO fix this so it takes into consideration the users abilities, as well as can show partial climbability (incase objects are slanted over many spaces, for example)
            if(climbableTop)
                canClimb = true;
        }


        return canClimb;
    }





    public int getStepableHeight(CObj co){
        ArrayList<ObjT> types = co.getTypePath();
        boolean squishable = false;
        for(ObjT t: types){
            if(t.squishable())
                squishable = true;
        }

        if(squishable)
            return 0;
        else
            return co.getHeight();
    }





    private Integer getZFromList(ArrayList<Coord> locs, int x, int y){
        for(Coord c: locs){
            if(c.x == x)
                if(c.y == y)
                    return c.z;
        }

        return null;
    }


    //note a fully passable obj will always return true
    public boolean canFit(Obj co, Coord lo, int maxWidth){
        int z = lo.z;
        int x = lo.x;
        int y = lo.y;
        int height = co.getMovingHeight();
        TreeSet<CObj> onSpot = sh.getState().getObjCBelow(new Coord(x, y, z + height));


        //construct a list of obj that interext with the co on that spot, including leaves of co
        HashSet<CObj> intersecting = new HashSet<>();
        //find out what leaves of the current object we need to check to see if they fit
        HashSet<CObj> checkLeaves = new HashSet<CObj>();
        boolean allPassable = true;

        for(CObj child: co.getAllLeafs()) {
            boolean passable = false;

            for (ObjT ot : child.getTypePath()) {
                if (ot.passable()) {
                    passable = true;
                    break;
                }
            }
            if (!passable) {
                checkLeaves.add(child);
                intersecting.add(child);
                allPassable = false;
            }
        }

        //if obj is entirely passable, auto return true
        if(allPassable)
            return true;

        //now find out which obj on the spot overlap with co and add to intersecting list
        for(CObj cur: onSpot){
            if(co.fullContainsPath(cur))
                continue;

            if (!lc.overlapping(cur, z, height, x, y))
                continue;

            boolean passable = false;
            boolean encompassing = false;
            for (ObjT ot : cur.getTypePath()) {
                if (ot.passable())
                    passable = true;
                if(ot.encompassing())
                    encompassing = true;
            }

            //if the object isnt passable it can be added to the intersecting list
            if (!passable) {
                //if its encompassing though auto fail
                if(encompassing) {
                    return false;
                }
                intersecting.add(cur);
            }
        }

        //before checking the actual width, let's pretend the obj is already on the coord its trying to move to
        co.useImaginaryLoc(lo);

        //now find the actual maximum width occupied given these intersecting objects
        int highestWidth = 0;
        for(CObj cur: checkLeaves){

            int tempWidth = overLappingFull(intersecting, cur, lo);

            if(highestWidth < tempWidth){
                highestWidth = tempWidth;
            }

        }

        //stop pretending
        co.stopUsingImaginaryLoc();

        //final check
        if(highestWidth > maxWidth) {
            return false;
        }

        else
            return true;
    }



    //co is the obj to check for overlapping against allMusOverlap
    //moverMap is the imaginary coordinates (z in particular) of an object(s) that are moving
    public int overLappingFull(HashSet<CObj> allMustOverlap, CObj co, Coord lo){
        int z = co.getZ(lo.x, lo.y);
        int height = co.getHeight();

        int maxWidth = 0;
        HashSet<CObj> overlapping = new HashSet<>();

        //for everything in overlapping, find what overlaps with co
        for(CObj cur: allMustOverlap) {
            if (cur != co) {

                if(lc.overlapping(cur, z, height, lo.x, lo.y)) {
                    overlapping.add(cur);
                }
            }

        }

        //for everything overlapping with co, find the max overlap width
        for(CObj cur: overlapping){
            //uncomment for high-precision measurement
            /*
            int temp = overLappingFull(overlapping, cur, lo, moverMap);
            if(temp > maxWidth){
                maxWidth = temp;
            }
            */

            //take out this line for high-precision measurement
            maxWidth += cur.getWidth();
        }


        return maxWidth + co.getWidth();

    }











    public HashMap<Coord, HashSet<ClimbObj>> movementHashMap(int curUserID, int range, int zMovementUp, int zMovementDown, boolean climbing, boolean checkSpaceAvail){
        HashMap<Coord, HashSet<ClimbObj>> canMoveTo = new HashMap<>();

        try {
            Obj curUser = GameManager.getInstance().getState().getObjID(curUserID);
            Coord middle = curUser.getMiddlemostCoord();
            int curX = middle.x;
            int curY = middle.y;

            //for each coordinate within range of the user
            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    Coord myC = new Coord(curX + x, curY + y);
                    HashSet<ClimbObj> moveTo = movementOnCoord(curUserID, myC, zMovementUp, zMovementDown, climbing, checkSpaceAvail);
                    if(!moveTo.isEmpty())
                        canMoveTo.put(myC, moveTo);
                }
            }
        }
        catch(RemovedException e){
            Log.e("Object moving was removed from the state by the time movementHashMap was called", "sorry");
        }

        return canMoveTo;
    }



    //this does not use the z coordinate on c
    //checkspace indicates whether you check for canFit
    public HashSet<ClimbObj> movementOnCoord(int curUserID, Coord myC, int zMovementUp, int zMovementDown, boolean climbing, boolean checkSpace){
        HashSet<ClimbObj> canMove = new HashSet<ClimbObj>();
        try {
            State s = GameManager.getInstance().getState();
            Obj curUser = s.getObjID(curUserID);
            int curZ = curUser.getLowestZ();
            Coord middle = curUser.getMiddlemostCoord();
            int curX = middle.x;
            int curY = middle.y;
            boolean isOnMyCoord = (curX == myC.x) && (curY == myC.y);
            Obj curUserTop = curUser.getTop();

            //is it off the map?
            if (!s.testOffMap(myC)) {

                //if not, check each object on it
                for (CObj co : s.getObjC(myC)) {

                    //skip this obj if it is part of the moving user
                    if(curUserTop.contains(co))
                        continue;

                    int objZ = co.getZ(myC.x, myC.y);
                    int objTop = objZ + getStepableHeight(co);
                    //figure out each height we want to try step/climbing onto
                    ArrayList<IntPair> moveOntoUp = new ArrayList<>();
                    ArrayList<IntPair> moveOntoDown = new ArrayList<>();

                    //can we step/climb onto this?
                    boolean stepClimbable;
                    if (climbing) {
                        stepClimbable = climbable(co, myC, zMovementUp, zMovementDown, curZ);

                        boolean climbableSide = false;
                        boolean standAtop = false;
                        for(ObjT t: co.getTypePath()){
                            if(t.climable())
                                climbableSide = true;
                            if(t.standable())
                                standAtop = true;
                        }

                        if(climbableSide) {
                            int moveDown = curZ - zMovementDown;
                            //find the lowest point below the user and above the bottom of the obj climbing onto
                            while(moveDown<curZ) {
                                if (objZ <= moveDown) {
                                    //fullest climb down
                                    if(curZ-moveDown > 40)
                                        moveOntoDown.add(new IntPair(moveDown, 75));

                                    //half fullest climb down
                                    int halfway = (moveDown+curZ)/2;
                                    if(halfway < curZ)
                                        if(curZ-halfway > 40)
                                            moveOntoDown.add(new IntPair(halfway, 75));
                                    break;
                                }
                                moveDown++;
                            }
                        }


                        if(standAtop) {
                            //climbing on top of object
                            if ((objTop >= curZ - zMovementDown) && (objTop < curZ))
                                moveOntoDown.add(new IntPair(objTop, (int) (co.getWidth() * 1.5)));
                            if ((objTop <= curZ + zMovementUp) && (objTop > curZ))
                                moveOntoUp.add(new IntPair(objTop, (int) (co.getWidth() * 1.5)));
                        }

                        if(climbableSide) {
                            int moveUp = curZ + zMovementUp;
                            //find the heightest point above the user and below the top of the obj climbing onto
                            while(moveUp>curZ) {
                                if (objTop >= moveUp) {
                                    //fullest climb up

                                    if(moveUp-curZ > 40)
                                        moveOntoUp.add(new IntPair(moveUp, 75));

                                    //half  fullestclimb up
                                    int halfway = (moveUp+curZ)/2;
                                    if(halfway> curZ)
                                        if(halfway-curZ > 40)
                                            moveOntoUp.add(new IntPair(halfway, 75));

                                    break;
                                }
                                moveUp--;
                            }
                        }
                    }
                    else {
                        stepClimbable = stepable(co, myC, zMovementUp, zMovementDown, curZ);
                        moveOntoUp.add(new IntPair(objTop, (int) (co.getWidth() * 1.5)));
                    }

                    if (stepClimbable) {
                        ArrayList<Integer> canFitAt = new ArrayList<Integer>();

                        //must check each height we can step/climb onto, and add the fullest climb we can to the list for each
                        for(IntPair ip: moveOntoUp) {
                            //we don't want to climb onto something that is at the same level as the user
                            if (climbing){
                                if (ip.i1 == curZ)
                                    continue;
                            }
                            //we also dont want to be able to run perfectly in place
                            else
                                if((ip.i1 == curZ) && isOnMyCoord)
                                    continue;

                            boolean canFit = true;
                            if(checkSpace)
                                canFit = canFit(curUser, new Coord(myC.x, myC.y, ip.i1), ip.i2);
                            if (canFit) {
                                //we can climb/step to the coord as well as fit, add it to the list and break (since this is the fullest climb in this direction)
                                canFitAt.add(ip.i1);
                                break;
                            }
                        }

                        for(IntPair ip: moveOntoDown) {
                            //we don't want to climb onto something that is at the same level as the user
                            if(climbing) {
                                if (ip.i1 == curZ)
                                    continue;
                            }
                            //we also dont want to be able to run perfectly in place
                            else
                                if((ip.i1 == curZ) && isOnMyCoord)
                                    continue;

                            boolean canFit = true;
                            if(checkSpace)
                                canFit = canFit(curUser, new Coord(myC.x, myC.y, ip.i1), ip.i2);
                            if (canFit) {
                                //we can climb/step to the coord as well as fit, add it to the list and break (since this is the fullest climb in this direction)
                                canFitAt.add(ip.i1);
                                break;
                            }
                        }

                        //if we fit in at least one spot, add this obj to the returned list
                        if(!canFitAt.isEmpty())
                            canMove.add(new ClimbObj(co, canFitAt));
                    }

                }
            }
        }
        catch(RemovedException e){
            Log.e("Object moving was removed from the state by the time movementHashMap was called", "sorry");
        }

        return canMove;
    }


























}

