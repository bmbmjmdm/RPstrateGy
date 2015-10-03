package database.ObjT;

import java.util.ArrayList;

import Managers.GameManager;
import Utilities.DamageType;
import Utilities.RemovedException;
import database.Objs.Obj;
import database.State;

/**
 * Created by Dale on 9/19/2015.
 */
//Be sure that you don't add 2 of the same type of injury to an object, always check for an existing one and add to it

//severity ranges from 1-100 inclusive
public abstract class Injury extends ObjT {
    String called;
    int severity;

    double infection_rate = 0;
    double infection_mod = 1;

    double actionTime = 1;
    double actionTime_mod = 1;

    double usability = 1;
    double usablity_mod = 1;

    int damageOverTime = 0;
    double damage_mod = 1;

    boolean infected = false;

    public Injury(String name, int oID, int sev){
        super(name, -1, oID, 0);
        severity = sev;
        updateCalled();
        updateInfection();
        updateActionTime();
        updateUsability();
        updateDamageOverTime();
    }

    //called whenever severity changes
    protected abstract void updateCalled();

    //exponential
    protected abstract void updateInfection();

    //exponential
    protected abstract void updateActionTime();

    //exponential
    protected abstract void updateUsability();

    //exponential
    protected abstract void updateDamageOverTime();

    protected abstract void applyInfected();

    protected abstract void unApplyInfected();

    public void makeInfected(){
        if(!infected) {
            infected = true;
            applyInfected();
        }
    }

    public void cureInfection(){
        if(infected){
            infected = false;
            unApplyInfected();
        }
    }


    public void changeSeverity(int sevChange){
        severity += sevChange;
        if(severity > 100)
            severity = 100;

        updateInfection();
        updateCalled();
        updateActionTime();
        updateDamageOverTime();
        updateUsability();
        if(infected)
            applyInfected();

        //if this injury is fully healed
        if(severity <= 0){
            try{
                //remove it from owner
                State s = GameManager.getInstance().getState();
                Obj owner = s.getObjID(belongsTo);
                owner.removeTypeSelf(id);
            }
            catch(RemovedException re){
                //owner not found, remove ourselves
                whenRemoved();
            }
        }
    }


    @Override
    public double getInfectionRate(){ return infection_rate * infection_mod;}

    public int getSeverity(){return severity;}

    public String getCalled(){return called;}

    @Override
    public double actionTimeMod(){return actionTime * actionTime_mod;}


    @Override
    public double getUsabilityMod(){return usability * usablity_mod;}


    @Override
    public int getDOT(){return (int) Math.round(damageOverTime * damage_mod);}

    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        desc.add("<font color=#E42217>"+called+"</font>");
        return desc;
    }


    public boolean isInfected(){
        return infected;
    }

    public DamageType dotType(){return DamageType.bloodloss;}
}
