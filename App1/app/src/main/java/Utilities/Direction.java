package Utilities;

import database.Coord;

/**
 * Created by Dale on 5/18/2015.
 */
public enum Direction {
    N,S,W,E,NE,SE,NW,SW, NONE;


    public static Direction findDir(Coord from, Coord to){
        int x = from.x - to.x;
        int y = from.y - to.y;
        boolean north = false;
        boolean east = false;
        boolean south = false;
        boolean west = false;

        if(x>0)
            east = true;
        if(x<0)
            west = true;
        if(y>0)
            north = true;
        if(y<0)
            south = true;

        if(north && east)
            return NE;
        if(north && west)
            return NW;
        if(north)
            return N;
        if(south && east)
            return SE;
        if(south && west)
            return SW;
        if(south)
            return S;
        if(east)
            return E;
        if(west)
            return W;

        return NONE;
    }


    //give the degrees difference between two dir. deg dif is equal to # of ticks around compass to get to other dir. NONE is equal to 2 ticks from any direction
    public static Integer degreesDifference(Direction from, Direction to){
        if(from == N){
            if(to == N)
                return 0;
            if(to == NE || to == NW)
                return 1;
            if(to == E || to == W || to == NONE)
                return 2;
            if(to == SE || to == SW)
                return 3;
            if(to == S)
                return 4;
        }
        if(from == NE){
            if(to == NE)
                return 0;
            if(to == E || to == N)
                return 1;
            if(to == SE || to == NW || to == NONE)
                return 2;
            if(to == S || to == W)
                return 3;
            if(to == SW)
                return 4;
        }

        if(from == E){
            if(to == E)
                return 0;
            if(to == NE || to == SE)
                return 1;
            if(to == N || to == S || to == NONE)
                return 2;
            if(to == SW || to == NW)
                return 3;
            if(to == W)
                return 4;
        }
        if(from == SE){
            if(to == SE)
                return 0;
            if(to == E || to == S)
                return 1;
            if(to == SW || to == NE || to == NONE)
                return 2;
            if(to == W || to == N)
                return 3;
            if(to == NW)
                return 4;
        }
        if(from == S){
            if(to == S)
                return 0;
            if(to == SE || to == SW)
                return 1;
            if(to == W || to == E || to == NONE)
                return 2;
            if(to == NW || to == NE)
                return 3;
            if(to == N)
                return 4;
        }
        if(from == SW){
            if(to == SW)
                return 0;
            if(to == W || to == S)
                return 1;
            if(to == NW || to == SE || to == NONE)
                return 2;
            if(to == E || to == N)
                return 3;
            if(to == NE)
                return 4;
        }
        if(from == W){
            if(to == W)
                return 0;
            if(to == SW || to == NW)
                return 1;
            if(to == S || to == N || to == NONE)
                return 2;
            if(to == SE || to == NE)
                return 3;
            if(to == E)
                return 4;
        }
        if(from == NW){
            if(to == NW)
                return 0;
            if(to == W || to == N)
                return 1;
            if(to == SW || to == NE || to == NONE)
                return 2;
            if(to == E || to == S)
                return 3;
            if(to == SE)
                return 4;
        }
        if(from == NONE){
            if(to == NONE)
                return 0;
            else
                return 2;
        }

        //unreachable
        return null;
    }
}
