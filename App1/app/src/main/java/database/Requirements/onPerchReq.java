package database.Requirements;

import java.util.ArrayList;

import Managers.GameManager;
import database.Coord;
import database.ObjT.ObjT;
import database.Objs.PObjs.User;
import database.State;

/**
 * Created by Dale on 4/7/2015.
 */
public class onPerchReq extends Requirement {

    public onPerchReq(){
        super("On Perch");
    }

    public boolean canUse(User p){
        Coord middle = p.getMiddlemostCoord();
        int x = middle.x;
        int y = middle.y;
        int z = p.getLowestZ();

        Integer standingOn = p.standingOn;
        State s = GameManager.getInstance().getState();

        if(standingOn != null) {
            for (int curX = x - 1; curX < x + 2; curX++) {
                for (int curY = y - 1; curY < y + 2; curY++) {
                    Coord atXY = new Coord(curX, curY);
                    if (!s.testOffMap(atXY)) {
                        if (s.getLowestSurface(atXY) < z)
                            return true;
                    }

                }
            }
        }

        return false;
    }
}
