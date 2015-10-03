package database.ObjT;

import java.io.Serializable;
import java.util.ArrayList;

import Managers.GameManager;
import Utilities.DamageType;
import database.Coord;

/**
 * Created by Dale on 12/29/2014.
 */
public class Punching extends ObjT{
    public double pStrength;


    public Punching(int user, double percent){
        super("Punching", -1, user, 1);
        pStrength = percent;
    }

}
