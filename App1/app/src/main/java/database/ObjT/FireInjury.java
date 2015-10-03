package database.ObjT;

import Managers.GameManager;
import Utilities.RemovedException;
import database.Objs.Obj;
import database.State;

/**
 * Created by Dale on 4/13/2015.
 */

//Be sure that you don't add 2 of these to an object, always check for an existing one and add to it

//severity ranges from 1-100 inclusive
public class FireInjury extends Injury {

    public FireInjury(int oID, int sev){
        super("FireInjury", oID, sev);
        State s = GameManager.getInstance().getState();
        s.addEOTObjT(id, s.getTime() + 3000);
    }

    //called whenever severity changes
    @Override
    protected void updateCalled(){
        if(severity <= 15){
            called = "1st Degree Burn";
        }

        else if (severity <= 50){
            called = "2nd Degree Burn";
        }

        else if (severity <= 85){
            called = "3rd Degree Burn";
        }

        else{
            called = "4th Degree Burn";
        }
    }

    //exponential
    @Override
    protected void updateInfection(){
        infection_rate = Math.pow(severity,2)/10000;
    }


    @Override
    protected void updateActionTime(){
        actionTime = 1 + (Math.pow(severity,2)/20000);
    }


    protected void applyInfected(){
        actionTime = actionTime * 1.5;
        usability = usability / 1.5;
    }

    protected void unApplyInfected(){
        actionTime = actionTime / 1.5;
        usability = usability * 1.5;
    }


    @Override
    protected void updateDamageOverTime(){}

    @Override
    protected void updateUsability(){
        usability = 1 - (Math.pow(severity,2)/20000);
    };

}
