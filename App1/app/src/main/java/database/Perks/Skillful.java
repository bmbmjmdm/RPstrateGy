package database.Perks;

import Utilities.Stat;
import database.Players.*;

/**
 * Created by Dale on 1/4/2015.
 */
public class Skillful extends Perk{


    public Skillful(){
        super("Skillful",
                "<font color=#41A317>All &#160;Classes &#160;+10</font><br><font color=#E42217>All &#160;Stat &#160;-15</font>",
                "<font color=#000000>Requirements:<br></font><font color=#000CCC>Costs &#160;10 &#160;SP</font><br><font color=#000000>Effects:<br></font>",
                10);
    }

    public void statelessApply(CharacterPlayer c){
        Stat[] alterAdd = {Stat.NINJA, Stat.MARKSMAN, Stat.MAD_DOCTOR, Stat.ELEMENTAL, Stat.WARRIOR, Stat.MAGICIAN, Stat.ENGINEER, Stat.ACROBAT};

        for(Stat s: alterAdd){
            c.setStat(s, c.getStat(s)+10);
        }


        Stat[] alterSub = {Stat.MAX_HEALTH, Stat.MAX_FOCUS, Stat.MAX_STAMINA, Stat.MAX_MANA};

        for(Stat s: alterSub){
            c.setStat(s, c.getStat(s)-15);
        }
    }

    public void statelessUnApply(CharacterPlayer c){
        Stat[] alterAdd = {Stat.NINJA, Stat.MARKSMAN, Stat.MAD_DOCTOR, Stat.ELEMENTAL, Stat.WARRIOR, Stat.MAGICIAN, Stat.ENGINEER, Stat.ACROBAT};

        for(Stat s: alterAdd){
            c.setStat(s, c.getStat(s)-10);
        }


        Stat[] alterSub = {Stat.MAX_HEALTH, Stat.MAX_FOCUS, Stat.MAX_STAMINA, Stat.MAX_MANA};

        for(Stat s: alterSub){
            c.setStat(s, c.getStat(s)+15);
        }
    }

    public void statefullApply(int ObjId){};
}
