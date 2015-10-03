package Managers.Logic;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import Utilities.ClimbObj;
import Utilities.IntPair;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Coord;
import database.Narration;
import database.ObjT.ObjT;
import database.ObjT.Stealthed;
import database.Objs.CObjs.BodyPart;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import Managers.GameManager;
import Managers.timeKeeper;
import database.State;
import shenronproductions.app1.Alert;

/**
 * Created by Dale on 4/29/2015.
 */
public class DetectionLogic {
    GameManager sh = GameManager.getInstance();

    public int getVisionDistance(User u){
        //TODO include any objT modifiers from the user u
        return 8;

    }

    // given a user, find all cobj within a 5 coord radius around them and add it to their vision if its not already there
    public void minVision(User u){
        //the max distance the user can see. determined by base value and Marksman modifier
        int vision_distance = getVisionDistance(u);

        //get the head, which will act as the source of vision
        BodyPart head = u.getHead();
        int fromX;
        int toX;
        int fromY;
        int toY;

        if(head != null){
            Coord source = head.getMiddlemostCoord();
            fromX = Math.max(source.x-vision_distance, 0);
            toX = Math.min(source.x+vision_distance, sh.getState().getSize());
            fromY = Math.max(source.y-vision_distance, 0);
            toY = Math.min(source.y+vision_distance, sh.getState().getSize());
        }
        else{
            Log.e("LogicCalc>getVision()", "Trying to get vision of somebody without a head!");
            u.clearVisionAll();
            return;
        }


        HashSet<CObj> in_vision = new HashSet<CObj>();
        //for each coordinate that can be seen given the head and vision_distance, get all CObj present
        for(int x = fromX; x <= toX; x++){
            for(int y = fromY; y <= toY; y++){
                Coord c = new Coord(x, y);
                if(!sh.getState().testOffMap(c)) {
                    TreeSet<CObj> cobjs = sh.getState().getObjC(c);
                    in_vision.addAll(cobjs);
                }
            }
        }

        /*HashSet<CObj> in_vision = new HashSet<CObj>();
        int size = GameManager.getInstance().getState().getSize();
        //for each coordinate that can be seen given the head and vision_distance, get all CObj present
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                Coord c = new Coord(x, y);
                TreeSet<CObj> cobjs = sh.getState().getObjC(c);
                in_vision.addAll(cobjs);
            }
        }*/

        //for all CObj present, see if user u can see them. if they can, only the locations they can see it will be added to the hashmap
        for(CObj cobj: in_vision){
            u.getVision(cobj.id);
        }

    }









    //find all coord a user can see an obj at
    public ArrayList<Coord> canSeeCObj(User u, CObj cobj){
        ArrayList<Coord> canSee = new ArrayList<Coord>();
        BodyPart head = u.getHead();
        Coord from = head.getMiddlemostCoord();
        int headX = from.x;
        int headY = from.y;
        int headZ = from.z;
        int toHeight = cobj.getHeight();
        int toWidth = cobj.getWidth();
        State s = sh.getState();
        int vision_distance = s.visionHor;
        int vision_dist_z = s.visionVert;
        int numLocs = 0;
        // cobj.clearObscuredBy(u.id);

        LogicCalc lc = new LogicCalc();

        int highestPath = 0;


        for(Coord to: cobj.getLoc()) {

            if(u.contains(cobj)){
                canSee.add(to);
                continue;
            }

            int toZ = to.z;

            //if this coord is out of our vision range, skip it
            if(toZ> headZ)
                if(toZ> vision_dist_z+headZ)
                    continue;

            if(toZ< headZ)
                if(toZ< headZ-vision_dist_z)
                    continue;

            if(to.distance(from) > vision_distance)
                continue;


            if(cantBeSeen(cobj, to)){
                continue;
            }

            /*
            //0=100% chance, 1=0% chance
            double chance_to_see = 0;

            //get the path between the head and the object
            ArrayList<Coord> vision_path = lc.getStraightPathNoZ(from, to);
            if(vision_path.size() > highestPath){
                highestPath= vision_path.size();
            }

            //z_mod being negative means the cobj is lower than the user
            int divide = (vision_path.size()-1);
            if(divide == 0)
                divide = 1;
            double z_mod = (to.z-from.z)/divide;

            //if the obj's bottom is below user's eyes and its top is above them, then it is "on level" with the user
            boolean onLevel = false;
            if(to.z<from.z && to.z+toHeight > from.z) {
                onLevel = true;
                z_mod = Math.pow((double) toHeight, (1/Math.max(1, (vision_path.size()-2.0))));
            }

            double z_vision = (head.getAverageZ()+(head.getHeight()/2));

            //first case, the target is on the same coordinate as the user. here, we check to see if it is below/above the user and, if so, check for obstructions
            if(vision_path.size() == 1){
                if(!onLevel) {
                    //note that these assume user can see their own body parts just fine and that they do not obstruct from other objects
                    if (z_mod >= 0) {
                        TreeSet<CObj> onCoord = s.getObjCBelow(new Coord(from.x, from.y, toZ));

                        //target is above user, check everything from top of head to bottom of target
                        for (CObj curObj : onCoord) {
                            if (seeThrough(curObj))
                                continue;
                            if (u.contains(curObj))
                                continue;
                            if (cobj.contains(curObj))
                                continue;
                            int cur_z = curObj.getZ(from.x, from.y);

                            //these first two checks exclude obj that are "next to" the user or the target
                            if (cur_z > z_vision)
                                if (cur_z+curObj.getHeight() < toZ)
                                    if (curObj.getWidth() > toWidth / 2) {
                                        //its in the way and big enough, calculate how likely the user will not be able to see to based on size
                                        chance_to_see = chanceSee(curObj.getWidth(), toWidth, chance_to_see);
                                        // curObj.addObscuring(u.id, cobj.id);
                                        // cobj.addObscuredBy(u.id, curObj.id);
                                        if(chance_to_see == 1)
                                            break;
                                    }
                        }
                    } else if (z_mod < 0) {
                        TreeSet<CObj> onCoord = s.getObjCBelow(new Coord(from.x, from.y, headZ));

                        //target is below, check from bot of head to top of target
                        for (CObj curObj : onCoord) {
                            if (seeThrough(curObj))
                                continue;
                            if (u.contains(curObj))
                                continue;
                            if (cobj.contains(curObj))
                                continue;
                            int cur_z = curObj.getZ(from.x, from.y);

                            //these first two checks excluide obj that are "next to" the user or the target
                            if (cur_z+curObj.getHeight() < z_vision)
                                if (cur_z > toZ + toHeight)
                                    if (curObj.getWidth() > toWidth / 2) {
                                        //its in the way and big enough, calculate how likely the user will not be able to see to based on size
                                        chance_to_see = chanceSee(curObj.getWidth(), toWidth, chance_to_see);
                                        // curObj.addObscuring(u.id, cobj.id);
                                        // cobj.addObscuredBy(u.id, curObj.id);
                                        if(chance_to_see == 1)
                                            break;
                                    }
                        }
                    }
                }
            }

            //if the target is away from the user, follow a z staggared diagnal
            else{
                int zOne;
                int zTwo;
                int min_z;
                int max_z;
                if(!onLevel) {
                    zOne = (int) Math.ceil(z_vision + (z_mod / 2));
                    zTwo = (int) Math.floor(z_vision - (z_mod / 2));
                    min_z = Math.min(zOne, zTwo);
                    max_z = Math.max(zOne, zTwo) + toHeight;
                }
                else{
                    min_z = toZ;
                    max_z = toZ + toHeight;
                }

                //the from and to locations are special cases
                //for from, we look at the space between head and the edge of the first staggered z area

                int next_z_edge = (int) Math.floor(z_vision+(z_mod/2));
                if (z_mod >= 0 && !onLevel){
                    TreeSet<CObj> onCoordF = s.getObjCBelow(new Coord(from.x, from.y, next_z_edge));

                    //target is above user, check everything from top of head to bottom of the next min_z
                    for(CObj curObj : onCoordF){
                        if(seeThrough(curObj))
                            continue;
                        if(u.contains(curObj))
                            continue;
                        if(cobj.contains(curObj))
                            continue;

                        int cur_z = curObj.getZ(from.x, from.y);


                        if(cur_z+curObj.getHeight() > z_vision)
                            if(cur_z < next_z_edge)
                                if(curObj.getWidth() > toWidth/2) {

                                    int coverageHeight = curObj.getHeight();
                                    if(min_z > cur_z){
                                        coverageHeight = coverageHeight-(min_z-cur_z);
                                    }
                                    if(cur_z+curObj.getHeight() > max_z){
                                        coverageHeight = coverageHeight-((cur_z+curObj.getHeight()) - max_z);
                                    }

                                    if(coverageHeight > 0 ) {
                                        //its in the way and big enough, calculate how likely the user will not be able to see to based on size
                                        chance_to_see = chanceSee(curObj.getWidth(), coverageHeight, toWidth, toHeight, chance_to_see);
                                        // curObj.addObscuring(u.id, cobj.id);
                                        // cobj.addObscuredBy(u.id, curObj.id);
                                    }

                                    if(chance_to_see == 1)
                                        break;
                                }
                    }
                }
                else if (z_mod < 0){
                    TreeSet<CObj> onCoordF = s.getObjCBelow(new Coord(from.x, from.y, headZ));

                    //target is below, check from bot of head to top of target
                    for(CObj curObj : onCoordF){
                        if(seeThrough(curObj))
                            continue;
                        if(u.contains(curObj))
                            continue;
                        if(cobj.contains(curObj))
                            continue;
                        int cur_z = curObj.getZ(from.x, from.y);


                        if(cur_z < headZ)
                            if(cur_z+curObj.getHeight() > next_z_edge)
                                if(curObj.getWidth() > toWidth/2){

                                    int coverageHeight = curObj.getHeight();
                                    if(min_z > cur_z){
                                        coverageHeight = coverageHeight-(min_z-cur_z);
                                    }
                                    if(cur_z+curObj.getHeight() > max_z){
                                        coverageHeight = coverageHeight-((cur_z+curObj.getHeight()) - max_z);
                                    }

                                    if(coverageHeight > 0) {
                                        //its in the way and big enough, calculate how likely the user will not be able to see to based on size
                                        chance_to_see = chanceSee(curObj.getWidth(), coverageHeight, toWidth, toHeight, chance_to_see);
                                        // curObj.addObscuring(u.id, cobj.id);
                                        // cobj.addObscuredBy(u.id, curObj.id);
                                    }

                                    if(chance_to_see == 1)
                                        break;
                                }
                    }
                }

                if(chance_to_see == 1)
                    continue;

                //remove the from and to coordinates since we only care about the path in-between
                vision_path.remove(vision_path.size() - 1);
                vision_path.remove(0);

                for(int i = 0; i< vision_path.size(); i++) {
                    Coord loc = vision_path.get(i);
                    if(!onLevel) {
                        z_vision += z_mod;
                        zOne = (int) Math.ceil(z_vision + (z_mod / 2));
                        zTwo = (int) Math.floor(z_vision - (z_mod / 2));
                        min_z = Math.min(zOne, zTwo);
                        max_z = Math.max(zOne, zTwo) + toHeight;
                    }
                    else{
                        double visMod = (toHeight-Math.pow(z_mod, (i+1)))/2;
                        zOne = (int) Math.ceil(toZ+visMod);
                        zTwo = (int) Math.floor(toZ+toHeight-visMod);
                        min_z = Math.min(zOne, zTwo);
                        max_z = Math.max(zOne, zTwo) + toHeight;
                    }

                    //for each coord along the path, get all cobj on it

                    TreeSet<CObj> onPath = s.getObjCBelow(new Coord(loc.x, loc.y, max_z));

                    for (CObj curObj: onPath) {
                        if(seeThrough(curObj))
                            continue;
                        if(u.contains(curObj))
                            continue;
                        if(cobj.contains(curObj))
                            continue;
                        //for each object along the path, check to see if it is "in between" from and to
                        //if it is, check the dimensions and adjust chance_to_see according to its size relative to the size of the cobj your trying to see

                        //this can be broken up into several cases:
                        //find the z difference and divide it among the number of spots in the path
                        //increase/decrease net z along path, check if any object has a z equal to or lower than the current +/- half mod
                        //if any do, check their height to see if they pass the z +/- half mod
                        //if they do, check the dimensions because it is definitely in the line of sight

                        int curZ = curObj.getZ(loc.x, loc.y);


                        //check if its in the way
                        if(curZ < max_z)
                            if(curZ+curObj.getHeight() > min_z)
                                //its in the way, is it big enough?
                                if(curObj.getWidth() > toWidth/2) {

                                    //if the obstruction is not fully alligned with the target, it will only partially cover. find height coverage by subtracting overflow
                                    int coverageHeight = curObj.getHeight();
                                    if(min_z > curZ){
                                        coverageHeight = coverageHeight-(min_z-curZ);
                                    }
                                    if(curZ+curObj.getHeight() > max_z){
                                        coverageHeight = coverageHeight-((curZ+curObj.getHeight()) - max_z);
                                    }

                                    if (coverageHeight > toHeight / 2) {
                                        //its in the way and big enough, calculate how likely the user will not be able to see to based on size
                                        chance_to_see = chanceSee(curObj.getWidth(), coverageHeight, toWidth, toHeight, chance_to_see);
                                        // curObj.addObscuring(u.id, cobj.id);
                                        // cobj.addObscuredBy(u.id, curObj.id);
                                        if(chance_to_see == 1)
                                            break;
                                    }
                                }

                    }
                    if(chance_to_see == 1)
                        break;
                }

                if(chance_to_see == 1)
                    continue;

                //now the special case of to. same thing as from but backwards
                int last_z_edge = (int) Math.floor(z_vision+(z_mod/2));
                z_vision += z_mod;
                zOne = (int) Math.ceil(z_vision+(z_mod/2));
                zTwo = (int) Math.floor(z_vision-(z_mod/2));
                min_z = Math.min(zOne, zTwo);
                max_z = Math.max(zOne, zTwo)+toHeight;

                if (z_mod >= 0 && !onLevel) {
                    TreeSet<CObj> onCoordT = s.getObjCBelow(new Coord(to.x, to.y, to.z));

                    //target is above user, check everything from the last edge to the target
                    for (CObj curObj : onCoordT) {
                        if (seeThrough(curObj))
                            continue;
                        if (u.contains(curObj))
                            continue;
                        if (cobj.contains(curObj))
                            continue;
                        int cur_z = curObj.getZ(to.x, to.y);

                        if (cur_z + curObj.getHeight() > last_z_edge)
                            if (cur_z < to.z)
                                if (curObj.getWidth() > toWidth / 2) {

                                    int coverageHeight = curObj.getHeight();
                                    if (min_z > cur_z) {
                                        coverageHeight = coverageHeight - (min_z - cur_z);
                                    }
                                    if (cur_z + curObj.getHeight() > max_z) {
                                        coverageHeight = coverageHeight - ((cur_z + curObj.getHeight()) - max_z);
                                    }

                                    if (coverageHeight > 0) {
                                        //its in the way and big enough, calculate how likely the user will not be able to see to based on size
                                        chance_to_see = chanceSee(curObj.getWidth(), coverageHeight, toWidth, toHeight, chance_to_see);
                                        // curObj.addObscuring(u.id, cobj.id);
                                        // cobj.addObscuredBy(u.id, curObj.id);
                                    }

                                    if (chance_to_see == 1)
                                        break;
                                }
                    }
                } else if (z_mod < 0) {
                    TreeSet<CObj> onCoordT = s.getObjCBelow(new Coord(to.x, to.y, last_z_edge));

                    //target is below, check from bot of last box to top of target
                    for (CObj curObj : onCoordT) {
                        if (seeThrough(curObj))
                            continue;
                        if (u.contains(curObj))
                            continue;
                        if (cobj.contains(curObj))
                            continue;
                        int cur_z = curObj.getZ(to.x, to.y);

                        if (cur_z < last_z_edge) {
                            if ((cur_z + curObj.getHeight() > to.z + toHeight)) {
                                if (curObj.getWidth() > toWidth / 2) {

                                    int coverageHeight = curObj.getHeight();
                                    if (min_z > cur_z) {
                                        coverageHeight = coverageHeight - (min_z - cur_z);
                                    }

                                    if (cur_z + curObj.getHeight() > max_z) {
                                        coverageHeight = coverageHeight - ((cur_z + curObj.getHeight()) - max_z);
                                    }

                                    if (coverageHeight > 0) {
                                        //its in the way and big enough, calculate how likely the user will not be able to see to based on size
                                        chance_to_see = chanceSee(curObj.getWidth(), coverageHeight, toWidth, toHeight, chance_to_see);
                                        // curObj.addObscuring(u.id, cobj.id);
                                        // cobj.addObscuredBy(u.id, curObj.id);
                                    }

                                    if (chance_to_see == 1)
                                        break;
                                }
                            }
                        }
                    }

                }

            }

            if(chance_to_see == 1)
                continue;

            //given the obstacles, calculate if the user can see it given some randomness
            timeKeeper tk = GameManager.getInstance().getTimeline();
            ArrayList<Integer> identifiers = new ArrayList<>();
            numLocs++;
            identifiers.add(u.id);
            identifiers.add(cobj.id);
            identifiers.add(numLocs);
            double d = tk.getRand(identifiers, "Chance To See");
            if(s.dayTime)
                d = Math.min(0.99, d*10);

            if(d >= chance_to_see)*/
                canSee.add(to);


        }

        return canSee;
    }





    public double chanceSee(double obsWidth, double obsHeight, double toWidth, double toHeight, double chance){
        if(toHeight == 0)
            toHeight = 1;
        if(toWidth == 0)
            toWidth = 1;
        return chance + Math.max(0.0, ((1 - chance) * Math.min(1, Math.abs(obsHeight / toHeight)) * Math.min(1, Math.abs(obsWidth / toWidth)) * Math.min(1, Math.abs(obsWidth / 50))));
    }

    public double chanceSee(int obsWidth, int toWidth, double chance){
        return chanceSee(obsWidth, 1, toWidth, 1, chance);
    }











    //removes all entries that cannot be seen by the user
    public void removeCantSee(HashMap<Coord, HashSet<ClimbObj>> climbTo, User u){
        Iterator<Coord> climbCoordIt = climbTo.keySet().iterator();
        while(climbCoordIt.hasNext()){
            Coord c = climbCoordIt.next();
            Iterator<ClimbObj> climbObjIt =  climbTo.get(c).iterator();
            while(climbObjIt.hasNext()){
                ClimbObj co = climbObjIt.next();
                boolean canSee = false;
                ArrayList<Coord> sees = u.getVision(co.co.id);

                for(Coord seeOn: sees){
                    if(seeOn.x == c.x && seeOn.y == c.y) {
                        canSee = true;
                        break;
                    }
                }

                if(!canSee)
                    climbObjIt.remove();
            }

            if(climbTo.get(c).isEmpty())
                climbCoordIt.remove();
        }
    }


    //removes all entries that cannot be seen by the user
    public void removeCantSee(HashSet<CObj> cobjs, User u){
        Iterator<CObj> iterator = cobjs.iterator();
        while(iterator.hasNext()){
            CObj co = iterator.next();
            ArrayList<Coord> sees = u.getVision(co.id);
            if(sees.size()>0)
                continue;
            iterator.remove();
        }
    }

    //removes all entries that cannot be seen by the user at corod c
    public void removeCantSee(HashSet<CObj> cobjs, User u, Coord c){
        Iterator<CObj> iterator = cobjs.iterator();
        while(iterator.hasNext()){
            CObj co = iterator.next();
            ArrayList<Coord> sees = u.getVision(co.id);
            boolean canSee = false;

            for(Coord seeOn: sees){
                if(seeOn.x == c.x)
                    if(seeOn.y==c.y) {
                        canSee = true;
                        break;
                    }
            }

            if(!canSee)
                iterator.remove();
        }
    }



    public void updateVisionOf(Obj obj){
        State s = GameManager.getInstance().getState();
        for(User u: s.getUsers()){
            u.clearVision(obj.id);
        }
        if(obj instanceof User){
            ((User) obj).clearVisionAll();
        }
        /*for(CObj co: obj.getAllLeafs()){
            for(User u: s.getUsers()) {
                for (Integer obsc : co.getObscuring(u.id)) {
                    try {
                        Obj obscObj = s.getObjID(obsc);
                        if (obscObj.isObscuredBy(u.id, co.id))
                            updateVisionOf(obscObj);
                    }
                    catch (RemovedException e) {
                        //was removed, doesnt matter
                    }
                }

                co.clearObscuredBy(u.id);
                co.clearObscuring(u.id);
            }
        }*/
    }




    public boolean cantBeSeen(CObj co, Coord on){
        ArrayList<ObjT> types = co.getTypePath();
        for(ObjT curT: types){
            if(curT.invisible())
                return true;


            //if an object is stealthed, we must check its surroundings to see if it is still stealthed
            if(curT instanceof Stealthed){
                Stealthed stealth = (Stealthed) curT;


                //see if the obj is stealthed based on all CObj on its spot
                if(conceals(co, on, stealth.widthMod, stealth.heightMod)){
                    return true;
                }

            }
        }


        return false;
    }





    //heightMod mods height but not z
    //moverMap simmulates Z position of concealMe if they are moving to onHere
    public boolean conceals(CObj concealMe, Coord onHere, double widthMod, double heightMod){
        LogicCalc lc = new LogicCalc();
        State s = GameManager.getInstance().getState();
        ArrayList<IntPair> concealors = new ArrayList<>();

        //used to set boundaries for concealment area
        int minZ = concealMe.getZ(onHere.x, onHere.y);
        int maxHeight = concealMe.getHeight();

        //look at all cobj on the location
        for(CObj iConceal: s.getObjC(onHere)) {

            //you cannot conceal yourself
            if(concealMe.fullContainsPath(iConceal)){
                continue;
            }

            //TODO include shadowing objT as an exemption to this
            if(iConceal instanceof BodyPart)
                continue;

            //invisible and see-through objects cannot conceal other objects
            boolean cont = false;
            for(ObjT type: iConceal.getTypePath()){
                if(type.invisible()){
                    cont = true;
                    break;
                }
                if (type.seeThrough()){
                    cont = true;
                    break;
                }
            }

            if(cont) {
                continue;
            }


            //for each one, we need to find out how much it covers of concealMe
            //to do so, we make a list of A,B pairs, where A is the concealing obj's z in relation to concealMe, and B is its height in relation to concealMe
            int concZ = iConceal.getZ(onHere.x, onHere.y);
            int concHeight = (int) (iConceal.getHeight() * heightMod);

            //if they are overlapping vertically given heightmod
            if (lc.overlapping(concealMe, concZ, concHeight, onHere.x, onHere.y)) {

                //and iConceal has greater or equal width given widthMod
                if (Math.round(iConceal.getWidth() * widthMod) >= concealMe.getWidth()) {

                    //we know they are elligable to be put in the list
                    int zA = concZ;
                    int heightB = concHeight;

                    //if the z is below concealMe's z, set it to concealMe's z
                    if(zA < minZ) {
                        int dif = minZ - zA;
                        zA = minZ;

                        //adjust height to take into consideration the change
                        heightB = heightB - dif;
                    }

                    //if the heightB+zA is above concealMe's height+z, set heightB = height+z-zA
                    if(heightB + zA > maxHeight + minZ){
                        heightB = maxHeight + minZ - zA;
                    }

                    //add to list
                    concealors.add(new IntPair(zA, heightB));
                }
            }
        }

        //sort the list in ascending order by z
        Collections.sort(concealors, new Comparator<IntPair>() {
            @Override
            public int compare(IntPair lhs, IntPair rhs) {
                return lhs.i1 - rhs.i1;
            }
        });


        //now we modify the list to ensure there is no duplicate coverage
        //get the first member so that we start iteration at index 1
        Iterator<IntPair> iterator = concealors.iterator();
        if(iterator.hasNext()){
            IntPair ip1 = iterator.next();

            while (iterator.hasNext()) {
                IntPair ip2 = iterator.next();

                //we check the past ip vs the current
                //if the past z + height is greater than present z, set past height to be present z - past z
                if (ip1.i1 + ip1.i2 > ip2.i1) {
                    ip1.i2 = ip2.i1 - ip1.i1;
                }
            }
        }

        //now see how much of the height we actually cover by adding up all the concealors heights
        int concealedHeight = 0;
        for(IntPair ip: concealors){
            concealedHeight += ip.i2;
        }


        //we must cover the entire obj's height
        if(concealedHeight >= concealMe.getHeight()){
            return true;
        }
        else{
            return false;
        }
    }





}
