package database.ObjT;

import android.util.Log;

import Managers.GameManager;
import database.State;

/**
 * Created by Dale on 4/13/2015.
 */

//Be sure that you don't add 2 of these to an object, always check for an existing one and add to it

//severity ranges from 1-100 inclusive
public class SharpInjury extends Injury {

    public SharpInjury(int oID, int sev){
        super("SharpInjury", oID, sev);
        State s = GameManager.getInstance().getState();
        s.addEOTObjT(id, s.getTime()+3000);
    }

    //called whenever severity changes
    @Override
    protected void updateCalled(){
        if(severity <= 15){
            called = "Cut";
        }

        else if (severity <= 50){
            called = "Gash";
        }

        else if (severity <= 85){
            called = "Major Laceration";
        }

        else{
            called = "Gaping Wound";
        }
    }


    @Override
    protected void updateInfection(){
        infection_rate = Math.pow(severity, 2)/20000;
    }


    @Override
    protected void updateActionTime(){

    }


    protected void applyInfected(){
        damageOverTime = (int) Math.round(damageOverTime * 1.3);
    }

    protected void unApplyInfected(){
        damageOverTime = (int) Math.round(damageOverTime / 1.3);
    }


    @Override
    protected void updateDamageOverTime(){
        damageOverTime = (int) Math.round(Math.pow(severity,2)/2000);
    }

    @Override
    protected void updateUsability(){
    };

}
