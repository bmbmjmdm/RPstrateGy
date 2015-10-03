package database.ObjT;

import Utilities.DamageType;

/**
 * Created by Dale on 12/29/2014.
 */
public class bpStrike extends ObjT{
    public double strengthP;
    public int maxDamage;
    public DamageType damageT;

    //a description when the strikes hit. follows the form: userName + onHit + objHit
    public String onHit;



    public bpStrike(int user, double percent, int maxDmg, DamageType type, String name, String hitDesc){
        super(name, -1, user, 1);
        strengthP = percent;
        maxDamage = maxDmg;
        damageT = type;
        onHit = hitDesc;
    }

}
