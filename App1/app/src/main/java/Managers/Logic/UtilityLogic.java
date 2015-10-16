package Managers.Logic;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NavigableSet;

import Managers.GameManager;
import Utilities.RemovedException;
import database.AbstractCoord;
import database.Coord;
import database.ObjT.CustomCallable;
import database.ObjT.ObjT;
import database.ObjT.RemoveType;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.State;

/**
 * Created by Dale on 4/29/2015.
 */
public class UtilityLogic {


    public boolean overlapping(CObj co, int z, int height, int x, int y){
        int curZ = co.getZ(x, y);
        int curHeight = co.getHeight();

        int minZ = Math.min(curZ, z);
        int maxZ = Math.max(z + height, curZ + curHeight);
        int hitBox = maxZ - minZ;

        if (curHeight + height > hitBox) {
            return true;
        }
        else
            return false;
    }



    public boolean overlapping(Obj o, int z){
        final int imaginaryHeight = 1;

        //for all children
        for(CObj co: o.getAllLeafs()) {
            //for all locations child is on
            for(Coord c: co.getLoc()) {

                int curHeight = co.getHeight();

                int minZ = Math.min(co.getLowestZ(), z);
                int maxZ = Math.max(z + imaginaryHeight, co.getHighestZ() + curHeight);
                int hitBox = maxZ - minZ;

                if (curHeight + imaginaryHeight > hitBox) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean overlapping(Obj o, int z, Coord c){
        final int imaginaryHeight = 1;

        //for all children
        for(CObj co: o.getAllLeafs()) {

            //check if the child is on coord c
            Integer curZ = co.getZ(c.x, c.y);
            if(curZ != null) {

                int curHeight = co.getHeight();

                int minZ = Math.min(z, curZ);
                int maxZ = Math.max(z + imaginaryHeight, curZ + curHeight);
                int hitBox = maxZ - minZ;

                if (curHeight + imaginaryHeight > hitBox) {
                    return true;
                }
            }

        }

        return false;
    }



    //gets any CObj that is overlapping at +/- zLenience
    public HashSet<CObj> getCObjAround(Coord at, int zLenience){
        NavigableSet<CObj> onSpot = GameManager.getInstance().getState().getObjCBelow(new Coord(at.x, at.y, at.z + zLenience+1));
        HashSet<CObj> around = new HashSet<>();

        for(CObj co: onSpot){
            if(overlapping(co, at.z-zLenience, zLenience*2, at.x, at.y)){
                around.add(co);
            }
        }

        return around;
    }


    //gets any CObj that is overlapping at +/- zLenience
    //only check the given list
    public HashSet<CObj> getCObjAround(Coord at, int zLenience, ArrayList<CObj> cobjs){
        //from the given list, extract all that are on the coord at
        HashSet<CObj> onSpot = new HashSet();
        for(CObj co: cobjs){
            //if it has a z there then its on at
            if(co.getZ(at.x, at.y) != null){
                onSpot.add(co);
            }
        }

        //now check from onSpot which ones overlap
        HashSet<CObj> around = new HashSet<>();

        for(CObj co: onSpot){
            if(overlapping(co, at.z-zLenience, zLenience*2, at.x, at.y)){
                around.add(co);
            }
        }

        return around;
    }


    //gets as centered an overlay as possible (returned as c1's z) between c1 and c2, given zLenience and c1's current coordinate
    public int getAccCenter(CObj c2, Coord atCurrent, int zLenience){
        int c2_z = c2.getZ(atCurrent.x, atCurrent.y);
        int c2_center  = c2_z + c2.getHeight()/2;

        if((atCurrent.z-zLenience <= c2_center) && (c2_center <= atCurrent.z+zLenience)){
            return c2_center;
        }

        else{
            if(atCurrent.z+zLenience <= c2_center)
                return atCurrent.z+zLenience;
            else
                return atCurrent.z-zLenience;
        }
    }









    /**
     * Returns all coordinates from-to (inclusive) as visited by the straightest possible line (diagnals allowed).
     * Does not take into consideration z value of either from or to (assumed to be 0)
     * @param from
     * @param to
     * @return
     */
    public static ArrayList<Coord> getStraightPathNoZ(Coord from, Coord to){
        int difX = to.x - from.x;
        int difY = to.y - from.y;
        ArrayList<Coord> path = new ArrayList<Coord>();

        //these define the +1,+1, -1,+2, etc pairs that define the direction/distance of each part of the path
        ArrayList<AbstractCoord> offset_path = new ArrayList<AbstractCoord>();
        //these are the signs for the x and y difference
        int xMod;
        int yMod;
        if(difX != 0)
            xMod = difX/Math.abs(difX);
        else
            xMod = 0;
        if(difY != 0)
            yMod = difY/Math.abs(difY);
        else
            yMod = 0;

        //force a diagnal line, then take whats left at the end and disperse it among the offset_path, starting with the middlemost and working outward, relapping if need be

        //this is for an empty diagnal or to make an even distribution of difX/Y when there is a diagnal
        offset_path.add(new AbstractCoord(0,0));

        //none empty diagnal
        while((difX != 0) && (difY != 0)) {
            offset_path.add(new AbstractCoord(xMod, yMod));
            difX = difX - xMod;
            difY = difY - yMod;
        }

        int dif = difX != 0? Math.abs(difX) : Math.abs(difY);

        //total length of path
        int length = offset_path.size() + dif;

        //disperse the + or - 1's evenly among the path
        //iterations represents how many FULL LENGTH changes need to be made
        int iterations = dif>length? (int) Math.floor(length/dif) : 0;
        while(iterations != 0){
            for(AbstractCoord c : offset_path){
                if(difX != 0)
                    c.x = c.x + xMod;
                else
                    c.y = c.y + yMod;
            }
        }
        dif = dif - iterations*length;

        if(dif != 0) {
            //now we need an even distribution of the remaining dif
            int bucketSize = (int) Math.floor(offset_path.size()/dif);

            for (int i = 0; i < dif; i++) {
                AbstractCoord c = offset_path.get((int) Math.floor(bucketSize/2)+(bucketSize*i));
                if (difX != 0)
                    c.x = c.x + xMod;
                else
                    c.y = c.y + yMod;
            }
            //offset_path complete
        }


        //make path now
        path.add(from);
        int currentX = from.x;
        int currentY = from.y;
        for(AbstractCoord c: offset_path){
            //no c will have both c.x and c.y exceeds their mods, so only one diagnal is needed
            if(c.x !=0 && c.y != 0){
                c.x = c.x - xMod;
                c.y= c.y-yMod;
                currentX = currentX+ xMod;
                currentY = currentY+ yMod;
                path.add(new Coord(currentX, currentY));
            }
            //only one of these while loops will be executed
            while (c.x != 0){
                c.x = c.x - xMod;
                currentX = currentX+xMod;
                path.add(new Coord(currentX, currentY));
            }
            while (c.y != 0){
                c.y = c.y - yMod;
                currentY = currentY+yMod;
                path.add(new Coord(currentX, currentY));
            }
        }


        return path;
    }













    public void processCustom(CustomCallable o){
        o.call();
    }



    public void processRemoveType(RemoveType o){
        State s = GameManager.getInstance().getState();
        try {
            ObjT ot = s.getObjT(o.typeID);
            try{
                Obj owner = s.getObjID(ot.belongsTo);
                owner.removeTypeSelf(ot.id);
            }

            catch(RemovedException e){
                s.remObjT(ot.id);
            }
        }
        catch(RemovedException e){
            Log.e("Type has been removed before processRemoveType can remove it", "Not found in state");
        }
    }






}
