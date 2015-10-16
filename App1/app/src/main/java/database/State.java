package database;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import Managers.GameManager;
import Managers.Logic.LogicCalc;
import Managers.timeKeeper;
import Utilities.Constants;
import Utilities.RemovedException;
import Utilities.Stat;
import Utilities.StateKit;
import Utilities.UnableException;
import Utilities.WritableComparator;
import database.Levels.Level;
import database.ObjT.DefaultStatRestore;
import database.Objs.CObjs.Bush;
import database.Objs.CObjs.CObj;
import database.Objs.CObjs.ComparatorObject;
import database.Objs.CObjs.Dirt;
import database.Objs.CObjs.Grass;
import database.Objs.CObjs.Rock;
import database.Objs.CObjs.Stick;
import database.Objs.Obj;
import database.ObjT.ObjT;
import database.Objs.PObjs.*;
import database.Objs.PObjs.Character;
import database.Players.CharacterPlayer;

/**
 * Created by Dale on 12/24/2014.
 */
public class State implements Serializable {
    public String name;
    //by default, the first object in the array list at any coordinate is ALWAYS terrain (grass, dirt, concrete, etc)
    TreeSet<CObj>[][] objsC;

    HashMap<Integer,Obj> objsID = new HashMap<Integer, Obj>();

    HashMap<Integer, ObjT> allobjsTID = new HashMap<>();

    ArrayList<ObjT> globalTypes = new ArrayList<ObjT>();

    //(time)->list of objT that need EOT processing
    //also be sure to do -1->list of objT that need EOT processing WITH ALL EOT PROCESSING
    HashMap<Integer, ArrayList<Integer>> endOfTurnOT = new HashMap<>();

    //this is the order of turns. currently only supports 2 players each with 1 user. will make more advanced later
    ArrayList<User> users = new ArrayList<User>();

    public boolean dayTime = false;
    public int visionHor = 4;
    public int visionVert = 600;


    String terrain;
    int turnLimit;

    //10-120
    private int size;

    //milliseconds
    //not that each
    int clock = 1;

    ArrayList<ArrayList<Narration>> narration = new ArrayList<>();

    public Level level = null;
    public boolean onePlayer = false;

    //n for name, ter for terrain, tur for turn limit
    public State(String n, String ter, String tur, String si, boolean day){
        name = n;

        if(tur.compareToIgnoreCase("None") == 0)
            turnLimit = -1;
        else
            turnLimit = Integer.parseInt(tur);

        terrain = ter;
        if(si.compareToIgnoreCase("Small") == 0)
            size = 10;
        else if(si.compareToIgnoreCase("Medium") == 0)
            size = 30;
        else
            size = 50;

        endOfTurnOT.put(-1, new ArrayList<Integer>());

        dayTime = day;
        if(day){
            visionHor = size;
            visionVert = 1000000;
        }

        narration.add(new ArrayList<Narration>());
    }


    public State(StateKit sK){
        this(sK.name, sK.terrain, sK.turns, sK.size, sK.dayTime);
    }
    public State(Level l) {
        this(l.fileName, l.terrain, l.turnLimit, l.size, l.day);
    }


    public int getSize(){
        return size;
    }



    public ArrayList<Narration> getNarrationNew() {
        return narration.get(narration.size()-1);
    }

    public ArrayList<ArrayList<Narration>> getNarrationOld() {
        ArrayList<ArrayList<Narration>> reMe = (ArrayList<ArrayList<Narration>>) narration.clone();
        reMe.remove(reMe.size()-1);
        return reMe;
    }

    public void addNarration(Narration n){
        narration.get(narration.size()-1).add(n);
    }

    public int getTime(){
        return clock;
    }

    public void setTime(int d){
        clock = d;
    }


    public void newNarration(){
        narration.add(new ArrayList<Narration>());
    }








    /************************************ Starting game *********************************/

    public void prepare(StateKit sK) throws UnableException{
        setUpState();
        placeCharacters(sK.p1, sK.p2);
        new Narration("Let the battle begin!", new HashSet<Obj>(), Stat.MISC);
    }

    public void prepare(Level lev) throws UnableException{
        onePlayer = true;
        level = lev;
        setUpState();
        lev.setUpState();
        new Narration("You find yourself stranded in the wild.", new HashSet<Obj>(), Stat.MISC);
        new Narration("Oh shit, wolves!", new HashSet<Obj>(), Stat.MISC);
    }


    //this sets up the objC lists as well as the terrain
    private void setUpState(){
        //initiate the objsC treeset
        objsC = new TreeSet[size][size];

        for(int x = 0; x<size; x++){
            for(int y=0; y<size; y++){
                final int giveX = x;
                final int giveY = y;
                objsC[x][y] = new TreeSet<CObj>(new WritableComparator<CObj>() {
                    @Override
                    public int compare(CObj lhs, CObj rhs) {
                        Integer botL = lhs.getZ(giveX, giveY);
                        Integer botR = rhs.getZ(giveX, giveY);
                        if(botL == null)
                            Log.e("returning null", lhs.name);
                        if(botR == null)
                            Log.e("returning null", rhs.name);

                        int dif = botL-botR;

                        //if they have equal bottoms
                        if(dif == 0){

                            //if one is a comparator, position it correctly so that it will capture ALL obj higher/lower than it
                            if(lhs instanceof ComparatorObject){
                                ComparatorObject co = (ComparatorObject) lhs;
                                if(co.lessThan){
                                    return 1;
                                }
                                else
                                    return -1;
                            }
                            if(rhs instanceof ComparatorObject){
                                ComparatorObject co = (ComparatorObject) rhs;
                                if(co.lessThan){
                                    return -1;
                                }
                                else
                                    return 1;
                            }

                            //neither is a comparator, their order is only relevant for the set's operations
                            return lhs.id-rhs.id;
                        }
                        //if they are not equal return dif
                        return dif;
                    }
                });
            }
        }


        //add the terrain
        if(terrain.compareToIgnoreCase("Forest") == 0) {
            generateForest();
        }


        //add the periodic restoration of stats
        addEOTObjT(new DefaultStatRestore().id, 3000);
    }




    private void placeCharacters(CharacterPlayer a, CharacterPlayer b) throws UnableException{
        timeKeeper tk = GameManager.getInstance().getTimeline();

        String nameA = a.name;
        String nameB = b.name;
        if(nameA.compareTo(nameB) == 0){
            nameA = nameA + " 1";
            nameB = nameB + " 2";
        }

        User u = new Character(a, 1, nameA);
        getFreeCoord(false, u);
        tk.turnObjectID = u.id; //TODO make the owner id you give the user be retrievable from individual device
        u.minimumVision();

        User u2 = new database.Objs.PObjs.Character(b, 2, nameB);
        getFreeCoord(true, u2);
        tk.nextTurnOID = u2.id;
        u2.minimumVision();

    }

    private void getFreeCoord(boolean reverse, User u) throws UnableException{
        LogicCalc lc = new LogicCalc();
        if(reverse) {
            int farCorner = 3*size/4;
            int closeCorner = (size/4)-2;
            for(int y = farCorner; y > closeCorner; y--){
                for(int x = farCorner; x > closeCorner; x--){
                    try {
                        Coord cord = new Coord(x, y);
                        CObj terrain = getTerrainObj(cord);
                        if (lc.canFit(u, cord, (int) (terrain.getWidth() * 1.5))) {
                            ArrayList<Coord> moveTo = new ArrayList<>();
                            moveTo.add(cord);
                            //first the user moves, this auto updates whatever the user was standing on to tell it the user is no longer standing on it
                            u.move(moveTo, true);
                            //update whatever the user moves to to tell it the user is now standing on it
                            u.standOnThis(terrain.id);
                            terrain.supportThis(u.id);
                            return;
                        }
                    }
                    catch(RemovedException re){
                        continue;
                    }
                }
            }
        }
        else{
            int farCorner = (3*size/4)+1;
            int closeCorner = (size/4)-1;
            for(int y = closeCorner; y < farCorner; y++){
                for(int x = closeCorner; x < farCorner; x++){
                    try {
                        Coord cord = new Coord(x,y);
                        CObj terrain = getTerrainObj(cord);
                        if(lc.canFit(u, cord, (int) (terrain.getWidth()*1.5))) {
                            ArrayList<Coord> moveTo = new ArrayList<>();
                            moveTo.add(cord);
                            //first the user moves, this auto updates whatever the user was standing on to tell it the user is no longer standing on it
                            u.move(moveTo, true);
                            //update whatever the user moves to to tell it the user is now standing on it
                            u.standOnThis(terrain.id);
                            terrain.supportThis(u.id);
                            return;
                        }
                    }
                    catch(RemovedException re){
                        continue;
                    }
                }
            }
        }
        throw new UnableException("Cannot find usable coordinate to place player");
    }

    //does not comply with random generation consistancy
    public void getRandomFreeCoord(User u, int maxTries) throws UnableException {
        LogicCalc lc = new LogicCalc();
        int curTry = 0;

        while(curTry < maxTries) {
            curTry++;

            double rand1 = Math.random();
            double rand2 = Math.random();
            int x = (int) Math.min(size-1, (rand1*size));
            int y = (int) Math.min(size-1, (rand2*size));
            try {
                Coord cord = new Coord(x, y);
                CObj terrain = getTerrainObj(cord);
                if (lc.canFit(u, cord, (int) (terrain.getWidth() * 1.5))) {
                    ArrayList<Coord> moveTo = new ArrayList<>();
                    moveTo.add(cord);
                    //first the user moves, this auto updates whatever the user was standing on to tell it the user is no longer standing on it
                    u.move(moveTo, true);
                    //update whatever the user moves to to tell it the user is now standing on it
                    u.standOnThis(terrain.id);
                    terrain.supportThis(u.id);
                    return;
                }
            } catch (RemovedException re) {
                continue;
            }
        }


        throw new UnableException("Cannot find usable coordinate to place player");

    }


    private void generateForest(){
        double ran;

        //terrain
        for(int x = 0; x< size; x++){
            for(int y = 0; y<size; y++){

                Obj dirt;
                Obj grassD;
                //ground
                ran = Math.random();
                ArrayList<Coord> groundAL = new ArrayList<Coord>();
                groundAL.add(new Coord(x, y, -1));
                dirt = new Dirt(groundAL);
                grassD = dirt;

                if(ran > 0.25){
                    ArrayList<Coord> al = new ArrayList<Coord>();
                    al.add(new Coord(x, y));
                    ran = Math.random();
                    Obj grass = new Grass(al, Constants.STATE_ID, Math.max(1, (int)(ran*20)));
                    grass.standingOn = dirt.id;
                    dirt.supportThis(grass.id);
                    grassD = grass;
                }

                //large objs
                ran = Math.random();
                if(ran > 0.8){
                    ran = Math.random();
                    Obj tree = new Tree(new Coord(x, y), Math.max(1, (int)(ran*100)), Constants.STATE_ID);
                    tree.standingOn = dirt.id;
                    dirt.supportThis(tree.id);
                }
                //small objs
                else {
                    ran = Math.random();
                    if(ran>0.6){
                        ArrayList<Coord> al = new ArrayList<Coord>();
                        al.add(new Coord(x, y));
                        ran = Math.random();
                        Obj rock = new Rock(al, Math.max(1, (int)(ran*ran*ran*ran*100)), Constants.STATE_ID);
                        rock.standingOn = grassD.id;
                        grassD.supportThis(rock.id);
                    }
                    ran = Math.random();
                    if(ran>0.6){
                        ArrayList<Coord> al = new ArrayList<Coord>();
                        al.add(new Coord(x, y));
                        Obj stick = new Stick(al, Constants.STATE_ID);
                        stick.standingOn = grassD.id;
                        grassD.supportThis(stick.id);
                    }
                    ran = Math.random();
                    if(ran>0.8){
                        ran = Math.random();
                        ArrayList<Coord> al = new ArrayList<Coord>();
                        al.add(new Coord(x, y));
                        Bush bush = new Bush(al, Constants.STATE_ID, ran);
                        bush.standingOn = grassD.id;
                        grassD.supportThis(bush.id);
                    }
                }


            }
        }

    }










    //accessing the sets*********************************************************

    private CObj getTerrainObj(Coord c) throws RemovedException{
        TreeSet<CObj> onSpot = getObjC(c);
        for(CObj co: onSpot){
            ArrayList<ObjT> types = co.getTypeSelf();
            for(ObjT ot: types)
                if(ot.isTerrain())
                    return co;
        }
        throw new RemovedException("Not found");
    }

    public int getLowestSurface(Coord c){
        int lowest = Integer.MAX_VALUE;
        LogicCalc lc = new LogicCalc();
        for(CObj co: getObjC(c)){
            ArrayList<ObjT> types = co.getTypePath();
            boolean stand = false;
            for(ObjT t: types){
                if(t.standable()) {
                    stand = true;
                    break;
                }
            }
            if(stand) {
                int cur = lc.getStepableHeight(co) + co.getZ(c.x, c.y);
                if (cur < lowest)
                    lowest = cur;
            }
        }
        return lowest;
    }

    public HashMap<Integer, ArrayList<Integer>> getEndOfTurnOT() {
         return endOfTurnOT;
    }

    //returns true if the objTID was present and removed from the list, false otherwise
    public boolean removeEndOfTurnOT(int time, int objTID){
        Iterator<Integer> allObjT = endOfTurnOT.get(time).iterator();

        while(allObjT.hasNext()){
            if(allObjT.next() == objTID) {
                allObjT.remove();
                return true;
            }
        }

        return false;
    }

    public ArrayList<ObjT> getGlobalTypes(){
        return globalTypes;
    }


    public ArrayList<User> getUsers() {
        return users;
    }

    //if an Obj of the same id is already at coord, it will not be added
    //returns true if object added succesfully
    //returns false if object already existed at coord co
    public boolean addObjC(Coord co, CObj addMe){
        TreeSet<CObj> objs = objsC[co.x][co.y];
        return objs.add(addMe);
    }


    public boolean remObjC(Coord co, CObj obj){
        TreeSet<CObj> objs =  objsC[co.x][co.y];
        return objs.remove(obj);
    }

    public TreeSet<CObj> getObjC(Coord co){
        return  new TreeSet<CObj>(objsC[co.x][co.y]);
    }


    public TreeSet<CObj> getObjCBelow(Coord co){
       TreeSet<CObj> set =   new TreeSet<CObj>((objsC[co.x][co.y]).headSet(new ComparatorObject(co, false)));
       return set;
    }

    public void addObjID(Obj o) {
        objsID.put(o.id, o);
    }

    public void remObjID(int id){
        objsID.remove(id);
    }

    public Obj getObjID(int id) throws RemovedException{
        Obj reMe = objsID.get(id);
        if(reMe == null)
            throw new RemovedException("Obj "+id+" is being accessed but has been removed from the state.");
        else
            return reMe;
    }

    public void addObjT(ObjT o) {
        allobjsTID.put(o.id, o);
    }

    public void remObjT(int id){
        allobjsTID.remove(id);
        ArrayList<Integer> eotOT = endOfTurnOT.get(-1);
        Iterator<Integer> it = eotOT.iterator();
        while(it.hasNext()){
            Integer i = it.next();
            if(i==id)
                it.remove();
        }
    }

    public ObjT getObjT(int id) throws RemovedException{
        ObjT reMe = allobjsTID.get(id);
        if(reMe == null)
            throw new RemovedException("ObjT "+id+" is being accessed but has been removed from the state.");
        else
            return reMe;
    }

    public HashSet<Obj> getAllObj(){
        HashSet<Obj> objs = new HashSet<Obj>();
        for(int i : objsID.keySet()){
            try{
                objs.add(getObjID(i));
            }
            catch(RemovedException e){System.out.println(e.getMessage());}
        }
        return objs;
    }

    public HashSet<ObjT> getAllObjT(){
        HashSet<ObjT> objs = new HashSet<ObjT>();
        for(int i : objsID.keySet()){
            try{
                objs.add(getObjT(i));
            }
            catch(RemovedException e){System.out.println(e.getMessage());}

        }
        return objs;
    }

    public void addEOTObjT(int objTID, int time){
        ArrayList<Integer> objTs = endOfTurnOT.get(time);
        if(objTs == null){
            objTs = new ArrayList<>();
        }

        objTs.add(objTID);
        endOfTurnOT.put(time, objTs);
    }



    public boolean testOffMap(Coord c){
        if(c.x < getSize())
            if(c.x > -1)
                if(c.y < getSize())
                    if(c.y > -1)
                        return false;
        return true;
    }

    public int coordToPosition(Coord c){
        return c.x + c.y*size;
    }

    public Coord positionToCoord(int pos){
        final int x = pos%size;
        final int y = (int) Math.floor(pos/size);
        return new Coord(x, y);
    }



}
