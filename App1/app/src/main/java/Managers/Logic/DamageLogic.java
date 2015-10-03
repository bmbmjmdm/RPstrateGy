package Managers.Logic;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;

import Managers.GameManager;
import Managers.timeKeeper;
import Utilities.DamageType;
import Utilities.IntObj;
import Utilities.RemovedException;
import Utilities.Stat;
import database.Narration;
import database.ObjT.BloodlossInjury;
import database.ObjT.BluntInjury;
import database.ObjT.FireInjury;
import database.ObjT.Falling;
import database.ObjT.Injury;
import database.ObjT.ObjT;
import database.ObjT.SharpInjury;
import database.ObjT.bpStrike;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.Objs.PObjs.PObj;
import database.Objs.PObjs.User;
import database.State;
import shenronproductions.app1.Alert;

/**
 * Created by Dale on 5/1/2015.
 */
public class DamageLogic {
    //TODO NOTE always add narration BEFORE doing damage! Otherwise things can be removed from the state and the narration will say no one saw it!
    GameManager sh = GameManager.getInstance();


    //this does not check "if" two objects collide. This is only to be called AFTER IT HAS BEEN DETERMINED that two objects collide
    //it is assumed that o1 is the one that initiated the collision (through movement, etc)
    public void collision(Obj o1, Obj o2){
        ArrayList<ObjT> types1 = o1.getTypePath();

        for(ObjT ot: types1){
            if(ot instanceof Falling) {
                fallingCollision(o1, o2, (Falling) ot);
            }
            if(ot instanceof bpStrike) {
                bodyStrikeCollision(o1, o2, (bpStrike) ot);
            }
        }

        ArrayList<ObjT> types2 = o2.getTypePath();

        for(ObjT ot: types2){
            if(ot instanceof bpStrike) {
                bodyStrikeCollision(o2, o1, (bpStrike) ot);
            }
        }


    }






    //o1 is falling
    //o1 will recieve damage based on the time they were falling and o2's cushiness.
    //if o1 falls on a part (like humans do), they will recieve 50% overall dmg and 100% damage to the part. they can have a "preffered" part to land on if they manauver in time
    //o2 also recieves full damage as well, modified based on its stability
    void fallingCollision(Obj o1, Obj o2, Falling fallingType){
        ArrayList<ObjT> types2 = o2.getTypePath();
        int cushioned = 0;
        int stability = 0;
        for(ObjT ot: types2){
            cushioned += ot.cushion();
            stability += ot.stable();
        }

        ArrayList<ObjT> types1 = o1.getTypePath();
        boolean partFall = false;
        for(ObjT ot: types1){
            if(ot.fallsOnPart())
                partFall = true;
        }

        int fallTime = fallingType.getResetCounters();
        if(fallTime < 3)
            return;

        int dmg = (int) Math.round(Math.pow(fallTime, 1.5));

        if(partFall){
            damage(o1, (int) ((dmg*((100.0-cushioned)/100.0))/2), DamageType.blunt);
            ArrayList<IntObj> children = ((PObj) o1).getChildren();

            if(!fallingType.preferredPart.isEmpty()){
                int divide = fallingType.preferredPart.size();
                for(IntObj io: children)
                    if(fallingType.preferredPart.contains(io.o)) {
                        damage(io.o, (int) ((dmg * ((100.0 - cushioned) / 100.0)) / divide), DamageType.blunt);
                    }
            }
            else {
                ArrayList<Integer> randomInputs = new ArrayList<>();
                randomInputs.add(o1.id);
                randomInputs.add(o2.id);
                double random = sh.getTimeline().getRand(randomInputs, "Random part fell on from" + o1.id);

                int index = (int) Math.floor(random * children.size());


                damage(children.get(index).o, (int) (dmg * ((100.0 - cushioned) / 100.0)), DamageType.blunt);
            }

        }
        else
            damage(o1, (int) (dmg*((100.0-cushioned)/100.0)), DamageType.blunt);


        damage(o2, (int) (dmg*((100.0-stability)/100.0)), DamageType.blunt);
    }



    //o1 is punching
    void bodyStrikeCollision(Obj o1, Obj o2, bpStrike strike){
        //set the actual damage amount
        int dmg = (int) Math.round(strike.maxDamage * strike.strengthP);


        //find what we are actually hitting
        ArrayList<CObj> leafs = o2.getAllLeafs();
        CObj highest = null;
        double highestD = 0;

        for(CObj leaf: leafs){
            if(leaf.collides(o1, 0) > highestD)
                highest = leaf;
        }


        if(highest != null) {
            //add narration
            HashSet<Obj> narrationInvolves = new HashSet<>();
            narrationInvolves.add(o1);
            narrationInvolves.add(highest);
            String text = o1.getTop().name + strike.onHit + highest.name;
            new Narration(text, narrationInvolves, Stat.WARRIOR);



            //damage obj's
            damage(highest, dmg, strike.damageT);
            o1.removeTypePath(strike.id);

            //check for hardness to reflect some damage
            double hardness = 0;
            for(ObjT type: highest.getTypePath()){
                hardness += type.hardness();
            }
            if(hardness > 0){
                int backDmg = (int) Math.round(hardness*dmg);
                damage(o1, backDmg, DamageType.blunt);
            }
        }
    }







    //an obj should never have damage() called directly on them. this is the logical call to damage on object, so use this
    public void damage(Obj o, int dmg, DamageType dt){
        if(dmg < 1)
            return;

        ArrayList<ObjT> types = o.getTypePath();
        double dmgP = 1.0;
        int dmgC = 0;
        for(ObjT ot: types){
            dmgP = dmgP * ot.dmgTakeP(dt);
            dmgC = dmgC + ot.dmgTakeC(dt);
        }
        dmg = (int) Math.round((dmg * dmgP));
        dmg = dmg + dmgC;

        if(dmg < 1)
            return;

        //if the object is a player, aler them
        timeKeeper tk = GameManager.getInstance().getTimeline();
        Alert possiblyMe = new Alert("Ouch!", dt.getHitDesc(), tk.getId());
        possiblyMe.addClearButton();
        tk.addAlertIfUser(o.getTop().id, possiblyMe);

        //damage obj
        o.damage(dmg, dt);

    }





    //this applies either a new injury or updates an old one to reflect the damage taken (damage taken is in respect to total health, 0-100)
    public void applyInjury(Obj o, DamageType dt, int damage){
        //TODO add other damage types
        Class injuryClass = null;

        if(dt == DamageType.fire){
            injuryClass = FireInjury.class;
        }

        else if (dt == DamageType.blunt){
            injuryClass = BluntInjury.class;
        }

        else if (dt == DamageType.sharp){
            injuryClass = SharpInjury.class;
        }

        else if (dt == DamageType.bloodloss){
            injuryClass = BloodlossInjury.class;
        }



        //check to see if the injury objT already exists on user
        boolean makeNew = true;

        for(ObjT type: o.getTypeSelf()){

            //if so, add damage to it
            if(type.getClass() == injuryClass){
                ((Injury) type).changeSeverity(damage);
                makeNew = false;
            }
        }

        //if no injury objT was found, make a new one
        if(makeNew){
            try{
                //use reflection to get the only constructor and pass the same argument no matter the injury type
                Injury newInj = (Injury) injuryClass.getConstructors()[0].newInstance(o.id, damage);
                o.addType(newInj);
            }

            catch(InstantiationException | IllegalAccessException | InvocationTargetException e){
                Log.e("Unable to create new instance of "+ injuryClass.toString() +".", e.getMessage());
            }
        }

    }








    public void processInjury(Injury objT){
        State s = GameManager.getInstance().getState();

        try{
            Obj owner = s.getObjID(objT.belongsTo);

            //damage owner if this injury causes damage over time
            int damage = objT.getDOT();
            if(damage > 0){
                damage(owner, damage, objT.dotType());

                //make narration
                HashSet<Obj> narrationInvolves = new HashSet<>();
                narrationInvolves.add(owner);
                String text = owner.getTop().name + "'s " + objT.getCalled()+" takes its toll.";
                new Narration(text, narrationInvolves, Stat.MISC);
            }


            //try to infect injury if it has a chance and is not already infected
            if(!objT.isInfected()) {

                double infectionChance = objT.getInfectionRate();
                if(infectionChance > 0) {

                    //get random
                    ArrayList<Integer> involves = new ArrayList<>();
                    involves.add(objT.id);
                    involves.add(objT.belongsTo);
                    double randomChance = GameManager.getInstance().getTimeline().getRand(involves, "Random chance to infect injury");

                    //if it passes, infect the injury
                    if(infectionChance > randomChance){
                        objT.makeInfected();

                        //make narration
                        HashSet<Obj> narrationInvolves = new HashSet<>();
                        narrationInvolves.add(owner);
                        String text = owner.getTop().name + "'s " + objT.getCalled()+" has become infected! Gross.";
                        new Narration(text, narrationInvolves, Stat.MISC);
                    }
                }
            }

        }
        catch(RemovedException re){
            Log.e("Obj removed in DamageLogic>processInjury", "This shouldnt happen");
        }

        //add the recuring injury back to the EOT objT
        s.addEOTObjT(objT.id, s.getTime() + 3000);
    };


}
