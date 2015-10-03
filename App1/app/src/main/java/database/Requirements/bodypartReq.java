package database.Requirements;

import java.util.ArrayList;

import Utilities.IntObj;
import database.Objs.PObjs.User;
import Managers.Logic.LogicCalc;

/**
 * Created by Dale on 1/1/2015.
 */
public class bodypartReq extends Requirement {
    public String whichBP;

    //from 0 (none) to 100
    public int healthReq;
    public int mobilityReq;

    public int numNeeded;


    public bodypartReq(String which, int health, int mobility) {
        this(which, health, mobility, 1);
    }

    public bodypartReq(String which, int health, int mobility, int num) {
        super("bodypartReq");
        healthReq = health;
        mobilityReq = mobility;
        whichBP = which;
        numNeeded = num;
    }

    public boolean canUse(User p) {
        //for each part the user has
        ArrayList<IntObj> parts = p.getChildren();
        int numFound = 0;
        for (IntObj part : parts) {
            //if it is needed
            if (part.o.name.contains(whichBP)) {
                //check to see its health and mobility via logic
                LogicCalc lc = new LogicCalc();
                if (lc.getMobility(part.o) >= mobilityReq)
                    if (lc.getUsability(part.o) >= healthReq)
                        numFound++;
            }
        }

        if (numFound >= numNeeded) {
            return true;
        } else {
            return false;
        }
    }
}
