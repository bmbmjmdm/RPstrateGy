package database.ObjT;

/**
 * Created by Dale on 4/13/2015.
 */

//Be sure that you don't add 2 of these to an object, always check for an existing one and add to it

//severity ranges from 1-100 inclusive
public class BluntInjury extends Injury {

    public BluntInjury(int oID, int sev){
        super("BluntInjury", oID, sev);
    }

    //called whenever severity changes
    @Override
    protected void updateCalled(){
        if(severity <= 15){
            called = "Bruised";
        }

        else if (severity <= 50){
            called = "Sprained Joint";
        }

        else if (severity <= 85){
            called = "Broken Bone";
        }

        else{
            called = "Shattered Muscle";
        }
    }


    @Override
    protected void updateInfection(){
    }


    @Override
    protected void updateActionTime(){
        actionTime = 1 + (Math.pow(severity, 2)/20000);
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
        usability = 1 - (Math.pow(severity,2)/10000);
    };

}
