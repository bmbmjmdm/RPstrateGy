package Utilities;

import java.io.Serializable;

import shenronproductions.app1.R;

/**
 * Created by Dale on 3/12/2015.
 */
public enum Stat implements Serializable {
    ELEMENTAL, WARRIOR, MAGICIAN, MAD_DOCTOR, MARKSMAN, NINJA, ENGINEER, ACROBAT,
    MAX_STAMINA, MAX_HEALTH, MAX_MANA, MAX_FOCUS, CUR_STAMINA, CUR_HEALTH, CUR_MANA, CUR_FOCUS,
    MISC;


    //TODO set the different types of actions backgrounds
    public int getPicEnabled(){
        //if (this == Stat.ACROBAT)
        return R.drawable.acrobat_button;
    }

    public int getPicDisabled(){
        //if (this == Stat.ACROBAT)
        return R.drawable.acrobat_button_unusable;
    }

}
