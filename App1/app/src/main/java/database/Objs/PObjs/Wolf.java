package database.Objs.PObjs;

import java.util.ArrayList;
import java.util.HashMap;

import Utilities.IntObj;
import Utilities.Stat;
import database.Coord;
import database.ObjT.Cushioned;
import database.ObjT.FallsOnPart;
import database.ObjT.Hard;
import database.ObjT.doesntTakeUpSpace;
import database.Objs.CObjs.BodyPart;
import database.Objs.Obj;
import database.Players.Player;

/**
 * Created by Dale on 12/29/2014.
 */
public class Wolf extends User{
    int levelOwner;


    public Wolf(Player p, int own, String uName){
        super(p, own, uName);

        types.add(new Cushioned(id, 50));
        types.add(new FallsOnPart(id));


        //make the body
        ArrayList<Coord> headLoc = new ArrayList<Coord>();
        BodyPart head = new BodyPart(headLoc, own, 35, 20, 10, userStats.get(Stat.MAX_HEALTH), "Head",  "\ud83d\udc3a");
        addChild(head, 100);
        head.addType(new Hard(head.id, 0.1));

        ArrayList<Coord> legOne = new ArrayList<Coord>();
        BodyPart leftLeg = new BodyPart(legOne, own, 35, 6, 5, userStats.get(Stat.MAX_HEALTH), "Leg", "\uD83D\uDC63");
        addChild(leftLeg, 20);

        ArrayList<Coord> legTwo = new ArrayList<Coord>();
        BodyPart leftRight = new BodyPart(legTwo, own, 35, 6, 5, userStats.get(Stat.MAX_HEALTH), "Leg", "\uD83D\uDC63");
        addChild(leftRight, 20);

        ArrayList<Coord> legThree = new ArrayList<Coord>();
        BodyPart legFront = new BodyPart(legThree, own, 35, 6, 5, userStats.get(Stat.MAX_HEALTH), "Leg", "\uD83D\uDC63");
        addChild(legFront, 20);

        ArrayList<Coord> legFour = new ArrayList<Coord>();
        BodyPart legBack = new BodyPart(legFour, own, 35, 6, 5, userStats.get(Stat.MAX_HEALTH), "Leg", "\uD83D\uDC63");
        addChild(legBack, 20);

        ArrayList<Coord> tailCoord = new ArrayList<Coord>();
        BodyPart tail = new BodyPart(tailCoord, own, 5, 15, 5, (int) (userStats.get(Stat.MAX_HEALTH) * 0.5), "Tail", "=");
        tail.addType(new doesntTakeUpSpace(id));
        addChild(tail, 80);

        ArrayList<Coord> torsoLoc = new ArrayList<Coord>();
        BodyPart torso = new BodyPart(torsoLoc, own, 30, 50, 30, (int) (userStats.get(Stat.MAX_HEALTH)*1.5), "Torso", "\ud83d\udc15");
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

            if(io.o.name.contains("Torso") || io.o.name.contains("Head") || io.o.name.contains("Tail")){
                ArrayList<Coord> moveToTA = new ArrayList<>();
                moveToTA.add(new Coord(c.x, c.y, c.z+35));
                io.o.move(moveToTA, removeStanding);
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

            if(io.o.name.contains("Torso") || io.o.name.contains("Head") || io.o.name.contains("Tail")){
                io.o.useImaginaryLoc(new Coord(c.x, c.y, c.z+35));
            }
        }

    }

    public int getFallingWidth(){
        int legs = 0;
        int rest = 0;
        for(IntObj io: children) {
            if (io.o.name.contains("Torso"))
                rest += io.o.getFallingWidth();
            if (io.o.name.contains("Leg"))
                legs += io.o.getFallingWidth();
            if (io.o.name.contains("Head"))
                rest += io.o.getFallingWidth();
        }
        return Math.max(rest, legs);
    }

    public int lastTimeDamaged(){
        int highest = -1000;
        for(IntObj io: children) {
            if(io.o instanceof BodyPart){
                int partDamaged;
                partDamaged = ((BodyPart) io.o).lastTimeDamaged;

                if(partDamaged > highest)
                    highest = partDamaged;
            }
        }

        return highest;
    }

    public int getZMovement(){
        return 25;
    }

    public int getMovingHeight(){
        int totalHeight = 0;
        for(IntObj io: children) {
            if (io.o.name.contains("Torso"))
                totalHeight += io.o.getMovingHeight();
            if (io.o.name.contains("Leg"))
                totalHeight += io.o.getMovingHeight();
        }
        return totalHeight;
    }
}
