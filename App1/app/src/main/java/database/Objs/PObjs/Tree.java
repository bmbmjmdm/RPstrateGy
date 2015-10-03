package database.Objs.PObjs;

import java.util.ArrayList;

import Utilities.DamageType;
import Utilities.IntObj;
import database.Coord;
import database.ObjT.Damage_Taken_Modifier;
import database.Objs.CObjs.CObj;
import database.Objs.CObjs.TreeBranch;
import database.Objs.CObjs.TreeTrunk;
import database.State;
import Managers.GameManager;
import Managers.Logic.LogicCalc;

/**
 * Created by Dale on 2/6/2015.
 */
public class Tree extends PObj{

    //BIG TREES SHOULD NEVER BE CREATED EXCEPT FOR START OF GAME. OTHERWISE THIS VIOLATES RANDOM CONTRACT

    //size is from 0 to 100, average distribution
    public Tree(Coord location, int size, int own) {
        super(own, "Tree");
        if(size < 10){
            size = 10;
        }
        int height = size*20;
        ArrayList<Coord> trunkLoc = new ArrayList<Coord>();
        trunkLoc.add(location);
        String trunkPic;
        if(size<40)
            trunkPic ="\uD83C\uDF32";
        else
            trunkPic = "\uD83C\uDF33";
        TreeTrunk trunk = new TreeTrunk(trunkLoc, own, height, Math.min(50, size), (int) Math.pow((size/2),2)*size/2, size*20, trunkPic);
        addChild(trunk, 100);

        State sh = GameManager.getInstance().getState();
        //this is a very basic branch builder that will make a star-shaped tree (+ meets x)
        for(int xMod = -1; xMod<2; xMod++){
            for(int yMod = -1; yMod<2; yMod++) {
                if(xMod == 0 && yMod == 0)
                    continue;

                double ran;
                ran = Math.random();
                int i = 180 + (int) (20 * ran);

                while (i < height) {

                    ran = Math.random();
                    if (ran > 0.95) {
                        ArrayList<Coord> branchLoc = new ArrayList<Coord>();

                        for (int len = 1; len < size / 3; len++) {
                            ran = Math.random();
                            if (len > 5 && ran > 0.85) {
                                break;
                            }
                            Coord newC = new Coord(location.x + (xMod * len), location.y + (yMod * len), i);
                            if (!sh.testOffMap(newC)) {
                                branchLoc.add(newC);
                            }
                            else{
                                break;
                            }
                        }

                        if(!branchLoc.isEmpty()) {
                            ran = Math.random();

                            int sizeMod = (int) (ran * ran * ran * size / 8);
                            TreeBranch branch = new TreeBranch(branchLoc, own, 5 + size / 10 + sizeMod, Math.min(50, 24 + size / 20 + sizeMod), (int) Math.pow((7 + size / 10 + sizeMod) , 2) , 100);
                            addChild(branch, 1);
                        }
                    }


                    i = i + 150 + (int) (50 * ran);
                }
            }
        }
        types.add(new Damage_Taken_Modifier(id, 0, 2.5, DamageType.fire, -1));
        types.add(new Damage_Taken_Modifier(id, 0, 2.5, DamageType.sharp, -1));
    }




    public void move(ArrayList<Coord> moveTo, boolean removeStanding){
        //TREES CURRENTLY DO NOT MOVE
    }
    public int getFallingWidth(){
        return 999;
        //TREES CURRENTLY DO NOT FALL
    }


    public int getMovingHeight(){return getTallestHeight();}

}
