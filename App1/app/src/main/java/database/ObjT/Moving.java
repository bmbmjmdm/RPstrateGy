package database.ObjT;

import java.util.ArrayList;
import java.util.HashSet;

import Utilities.UnspecifiedMovement;
import database.Coord;

/**
 * Created by Dale on 1/1/2015.
 */
public class Moving extends ObjT{
    ArrayList<Coord> movingTo;
    int speed;
    public String called;
    public int range;

    //for use if you want to decide the first X objs to collide with when moving
    HashSet<Integer> collide = null;

    //for use if you want to end this movement standing on something
    Integer landsOn = null;
    Boolean climbing = null;
    Integer zMovementUp = null;
    Integer zMovementDown = null;

    //determines whether this moving adheres to limited space rules on coord (DANGEROUS to use with range>1)
    public boolean takesUpSpace = true;

    //the coords will become the new loc of the cobj of oID
    //currently the LAST MOVEMENT to process is the prevailing one
    public Moving(int spe, int oID, ArrayList<Coord> coords, String call, int rang){
        super("Moving", 1, oID, 0);
        movingTo = coords;
        speed = spe;
        called = call;
        range = rang;
    }

    public void setMovement(ArrayList<Coord> coords){
        movingTo = coords;
    }

    @Override
    public ArrayList<Coord> movement(){
        return movingTo;
    }

    @Override
    public int speed(){
        return speed;
    }

    @Override
    public boolean isMoving(){return true;}

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        if(called != null)
            desc.add("<font color=#000000>"+called+"</font>");
        return desc;
    }





    public void setCollide(Integer... i){
        collide = new HashSet<>();
        for(Integer cur: i)
            collide.add(cur);
    }

    public void setLandsOn(int land, int zUp, int zDown, boolean climb){
        landsOn = land;
        zMovementDown = zDown;
        zMovementUp = zUp;
        climbing = climb;
    }

    public HashSet<Integer> getCollide() throws UnspecifiedMovement{
        if(collide == null)
            throw new UnspecifiedMovement();
        else
            return collide;
    }






    public Integer getLandsOn() throws UnspecifiedMovement{
        if(landsOn == null)
            throw new UnspecifiedMovement();
        else
            return landsOn;
    }

    public Integer getzMovementUp() throws UnspecifiedMovement{
        if(zMovementUp == null)
            throw new UnspecifiedMovement();
        else
            return zMovementUp;
    }

    public Integer getzMovementDown() throws UnspecifiedMovement{
        if(zMovementDown == null)
            throw new UnspecifiedMovement();
        else
            return zMovementDown;
    }

    public Boolean getClimbing() throws UnspecifiedMovement{
        if(climbing == null)
            throw new UnspecifiedMovement();
        else
            return climbing;
    }

}
