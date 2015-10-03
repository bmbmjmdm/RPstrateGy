package database.Players;

import java.util.ArrayList;

import Utilities.Stat;
import database.Actions.Bark;
import database.Actions.Bite;
import database.Actions.Rest;
import database.Actions.Run;
import database.Actions.Sneak;
import database.Perks.Perk;
import database.StatelessItems.StatelessItem;

/**
 * Created by Dale on 1/4/2015.
 */
public class WolfPlayer extends Player {


    public WolfPlayer(double strengthLevel){
        super("Wolf");

        //do classes scaling with level
        playerStats.put(Stat.NINJA, (int) Math.min(100, (50*strengthLevel)));
        playerStats.put(Stat.MARKSMAN, (int) Math.min(100, (50*strengthLevel)));
        playerStats.put(Stat.MAD_DOCTOR, (int) Math.min(100, (50*strengthLevel)));
        playerStats.put(Stat.WARRIOR, (int) Math.min(100, (100*strengthLevel)));
        playerStats.put(Stat.MAGICIAN, (int) Math.min(100, (0*strengthLevel)));
        playerStats.put(Stat.ENGINEER, (int) Math.min(100, (0*strengthLevel)));
        playerStats.put(Stat.ELEMENTAL, (int) Math.min(100, (20*strengthLevel)));
        playerStats.put(Stat.ACROBAT, (int) Math.min(100, (150*strengthLevel)));

        //do stats scaling with level + baseline
        playerStats.put(Stat.MAX_FOCUS, (int) (50*strengthLevel));
        playerStats.put(Stat.MAX_MANA, (int) (20*strengthLevel));
        playerStats.put(Stat.MAX_STAMINA, (int) (30*strengthLevel)+30);
        playerStats.put(Stat.MAX_HEALTH, (int) (30*strengthLevel)+30);


        //add all possible actions
        actions.add(new Run());
        actions.add(new Bark());
        actions.add(new Bite());
        actions.add(new Rest());

        if(strengthLevel >= 0.5){
            actions.add(new Sneak());
        }
    }
}
