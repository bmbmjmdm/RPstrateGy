package Utilities;


/**
 * Created by Dale on 5/4/2015.
 */
public enum DamageType {
    //bloodloss cannot be a damage type of an attack. it is not an injury, an injury causes it

    sharp, fire, blunt, abrasive, biological, electrical, cold, bloodloss, all;



    public String getHitDesc(){
        if (this == DamageType.sharp)
            return "You feel a sharp sting pierce your body.";

        if (this == DamageType.fire)
            return "An over-heated sensation alerts you to a burn.";

        if (this == DamageType.blunt)
            return "A dull, painful impact hurts you.";

        if (this == DamageType.bloodloss)
            return "You can feel yourself losing blood.";

        else
            return "I didn't do the alert for this yet";
    }

}
