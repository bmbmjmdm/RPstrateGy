package database.ObjT;

import Utilities.DamageType;

/**
 * Created by Dale on 4/13/2015.
 */

//Be sure that you don't add 2 of these to an object, always check for an existing one and add to it

//severity ranges from 1-100 inclusive
public class BloodlossInjury extends Injury {

    public BloodlossInjury(int oID, int sev){
        super("BloodlossInjury", oID, sev);
    }

    //called whenever severity changes
    @Override
    protected void updateCalled(){
        if(severity <= 25){
            called = "Benine Bloodloss";
        }

        else if (severity <= 50){
            called = "Moderate Bloodloss";
        }

        else if (severity <= 85){
            called = "Major Bloodloss";
        }

        else{
            called = "Blood Deprived";
        }
    }


    @Override
    protected void updateInfection(){
    }


    @Override
    protected void updateActionTime(){
        actionTime = 1 + (Math.pow(severity, 2)/10000);
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
