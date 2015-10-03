package database.Objs.PObjs;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import Managers.GameManager;
import Managers.Logic.LogicCalc;
import Utilities.IntObj;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Actions.Action;
import database.Coord;
import database.ObjT.Cushioned;
import database.ObjT.FallsOnPart;
import database.ObjT.Hard;
import database.Objs.CObjs.BodyPart;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.Perks.Perk;
import database.Players.Player;
import database.State;
import database.StatelessItems.StatelessItem;

/**
 * Created by Dale on 12/29/2014.
 */
public class Character extends User{

    public Character(Player p, int own, String uName){
        super(p, own, uName);

        types.add(new Cushioned(id, 50));
        types.add(new FallsOnPart(id));


        //make the body
        ArrayList<Coord> headLoc = new ArrayList<Coord>();
        BodyPart head = new BodyPart(headLoc, own, 35, 20, 8, userStats.get(Stat.MAX_HEALTH), "Head", "\uD83D\uDE10");
        addChild(head, 100);
        head.addType(new Hard(head.id, 0.2));

        ArrayList<Coord> rightArmLoc = new ArrayList<Coord>();
        BodyPart rightArm = new BodyPart(rightArmLoc, own, 55, 8, 10, userStats.get(Stat.MAX_HEALTH), "Right Arm", "\u10DA");
        addChild(rightArm, 20);

        ArrayList<Coord> leftArmLoc = new ArrayList<Coord>();
        BodyPart leftArm = new BodyPart(leftArmLoc, own, 55, 8, 10, userStats.get(Stat.MAX_HEALTH), "Left Arm", "\u10DA");
        addChild(leftArm, 20);

        ArrayList<Coord> rightLegLoc = new ArrayList<Coord>();
        BodyPart rightLeg = new BodyPart(rightLegLoc, own, 65, 12, 15, userStats.get(Stat.MAX_HEALTH), "Right Leg", "\uD83D\uDC63");
        addChild(rightLeg, 20);

        ArrayList<Coord> leftLegLoc = new ArrayList<Coord>();
        BodyPart leftLeg = new BodyPart(leftLegLoc, own, 65, 12, 15, userStats.get(Stat.MAX_HEALTH), "Left Leg", "\uD83D\uDC63");
        addChild(leftLeg, 20);

        ArrayList<Coord> torsoLoc = new ArrayList<Coord>();
        BodyPart torso = new BodyPart(torsoLoc, own, 75, 20, 15, (int) (userStats.get(Stat.MAX_HEALTH)*1.5), "Torso", "\uD83D\uDE10");
        addChild(torso, 100);

    }

    //user's move as a whole, so only the first loc of moveTo is used
    public void move(ArrayList<Coord> moveTo, boolean removeStanding){
        Coord c = moveTo.get(0);
        for(IntObj io: children){
            if(io.o.name.contains("Leg")){
                ArrayList<Coord> moveToLE = new ArrayList<>();
                moveToLE.add(c);
                io.o.move(moveToLE, removeStanding);
            }

            if(io.o.name.contains("Torso") || io.o.name.contains("Arm")){
                ArrayList<Coord> moveToTA = new ArrayList<>();
                moveToTA.add(new Coord(c.x, c.y, c.z+65));
                io.o.move(moveToTA, removeStanding);
            }

            if(io.o.name.contains("Head")){
                ArrayList<Coord> moveToHE = new ArrayList<>();
                moveToHE.add(new Coord(c.x, c.y, c.z+140));
                io.o.move(moveToHE, removeStanding);
            }
        }
        if(removeStanding)
            removeAllStanding();
    }


    @Override
    public void useImaginaryLoc(Coord c){


        for(IntObj io: children){
            if(io.o.name.contains("Leg")){
                io.o.useImaginaryLoc(c);
            }

            if(io.o.name.contains("Torso") || io.o.name.contains("Arm")){
                io.o.useImaginaryLoc(new Coord(c.x, c.y, c.z + 65));
            }

            if(io.o.name.contains("Head")){
                io.o.useImaginaryLoc(new Coord(c.x, c.y, c.z+140));
            }
        }

    }

    public int getFallingWidth(){
        int torsoArms = 0;
        int legs = 0;
        int head = 0;
        for(IntObj io: children) {
            if (io.o.name.contains("Torso"))
                torsoArms += io.o.getFallingWidth();
            if (io.o.name.contains("Arm"))
                torsoArms += io.o.getFallingWidth();
            if (io.o.name.contains("Leg"))
                legs += io.o.getFallingWidth();
            if (io.o.name.contains("Head"))
                head += io.o.getFallingWidth();
        }
        return Math.max(head, Math.max(torsoArms, legs));
    }

    public int getMovingHeight(){
        int totalHeight = 0;
        for(IntObj io: children) {
            if (io.o.name.contains("Torso"))
                totalHeight += io.o.getMovingHeight();
            if (io.o.name.contains("Leg"))
                totalHeight += io.o.getMovingHeight();
            if (io.o.name.contains("Head"))
                totalHeight += io.o.getMovingHeight();
        }
        return totalHeight;
    }
}
