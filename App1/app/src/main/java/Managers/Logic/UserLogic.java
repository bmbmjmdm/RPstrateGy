package Managers.Logic;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

import Managers.GameManager;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Narration;
import database.ObjT.ObjT;
import database.ObjT.Resting;
import database.ObjT.TurnBP;
import database.ObjT.UserStealthed;
import database.Objs.CObjs.BodyPart;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.State;

/**
 * Created by Dale on 5/7/2015.
 */
public class UserLogic {

    public void turnStatsRecover(int id){
        State s = GameManager.getInstance().getState();

        for (User u : s.getUsers()) {
            ArrayList<ObjT> types = u.getTypePath();

            //get mods
            int staminaRecoverModC = 0;
            double staminaRecoverModP = 1;
            for (ObjT type : types) {
                staminaRecoverModC += type.recModC();
                staminaRecoverModP *= type.recModP();
            }

            //stamina
            int recovery = (int) ((2 + staminaRecoverModC) * staminaRecoverModP);
            int newStamina = u.getStat(Stat.CUR_STAMINA) + recovery;
            int maxStamina = u.getStat(Stat.MAX_STAMINA);
            if (newStamina > maxStamina)
                newStamina = maxStamina;
            u.setStat(Stat.CUR_STAMINA, newStamina);

            //focus
            int newFocus = u.getStat(Stat.CUR_FOCUS) + 2;
            int maxFocus = u.getStat(Stat.MAX_FOCUS);
            if (newFocus > maxFocus)
                newFocus = maxFocus;
            u.setStat(Stat.CUR_FOCUS, newFocus);

            //mana
            int newMana = u.getStat(Stat.CUR_MANA) + 2;
            int maxMana = u.getStat(Stat.MAX_MANA);
            if (newMana > maxMana)
                newMana = maxMana;
            u.setStat(Stat.CUR_MANA, newMana);
        }

        //this occurs every 3000ms
        s.addEOTObjT(id, s.getTime()+3000);
    }





    public void processTurnBP(TurnBP objT){
        State s = GameManager.getInstance().getState();
        try{
            BodyPart bp = (BodyPart) s.getObjID(objT.belongsTo);
            if(objT.vert)
                bp.turnVertical();
            else
                bp.turnHorizontal();
            bp.removeTypeSelf(objT.id);

        }
        catch(RemovedException e){
            Log.e("Object trying to be turned in processTurnBP has been removed from state", objT.belongsTo+"");
        }
    }






    public void processResting(Resting objT){
        try{
            GameManager gm = GameManager.getInstance();
            User u = (User) gm.getState().getObjID(objT.belongsTo);
            ArrayList<ObjT> types = u.getTypePath();

            //get mods
            int staminaRecoverModC = 0;
            double staminaRecoverModP = 1;
            for(ObjT type: types){
                staminaRecoverModC += type.restModC();
                staminaRecoverModP *= type.restModP();
            }

            //stamina recover
            int recovery = (int) ((5 + staminaRecoverModC) * staminaRecoverModP);
            int newStamina = u.getStat(Stat.CUR_STAMINA) + recovery;
            int maxStamina = u.getStat(Stat.MAX_STAMINA);
            if(newStamina > maxStamina)
                newStamina = maxStamina;
            u.setStat(Stat.CUR_STAMINA, newStamina);


            //remove resting
            u.removeTypeSelf(objT.id);
        }
        catch(RemovedException e){
            Log.e("User not found for processResting", objT.belongsTo+"");
        }
    }



    public void processUserStealth(UserStealthed objT){

        try{
            GameManager gm = GameManager.getInstance();
            State s = gm.getState();
            User u = (User) s.getObjID(objT.belongsTo);
            //ArrayList<ObjT> types = u.getTypePath();

            //get mods
            //int staminaRecoverModC = 0;
            //double staminaRecoverModP = 1;
            //for(ObjT type: types){
            //    staminaRecoverModC += type.restModC();
            //    staminaRecoverModP *= type.restModP();
            //}

            //focus cost
            int cost = objT.baseFocus;
            int userFocus = u.getStat(Stat.CUR_FOCUS);

            //pay it
            if(userFocus >= cost){
                //pay
                u.setStat(Stat.CUR_FOCUS, userFocus-cost);

                //set up to pay it again soon
                s.addEOTObjT(objT.id, s.getTime()+500);
            }


            //cannot pay for it, lose stealthed
            else{
                //remove objT
                u.removeTypeSelf(objT.id);

                //update vision
                new LogicCalc().updateVisionOf(u);

                //make narration
                HashSet<Obj> narrationInvolves = new HashSet<>();
                narrationInvolves.add(u);
                String text = u.name + " is tired of being stealthy, \"Fuck sneaking!\" he says.";
                new Narration(text, narrationInvolves, Stat.NINJA);
            }

        }
        catch(RemovedException e){
            Log.e("User not found for processUserStealthed", objT.belongsTo+"");
        }
    }











    public int getMobility(Obj o){
        //TODO
        return 100;
    }


    public int getUsability(Obj o){
        double health = o.getHealthPercent() * 100;

        for(ObjT type: o.getTypePath()){
            health *= type.getUsabilityMod();
        }

        return (int) Math.round(health);
    }


}
