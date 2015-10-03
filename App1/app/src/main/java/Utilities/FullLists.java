package Utilities;

import java.util.ArrayList;

import database.Actions.Action;
import database.Actions.Bark;
import database.Actions.Bite;
import database.Actions.Climb;
import database.Actions.Drop;
import database.Actions.Kick;
import database.Actions.Punch;
import database.Actions.Rest;
import database.Actions.Run;
import database.Actions.Sneak;
import database.Actions.Wait;
import database.Perks.Athletic;
import database.Perks.Battle_Hardened;
import database.Perks.Perk;
import database.Perks.Skillful;
import database.StatelessItems.StatelessItem;

/**
 * Created by Dale on 2/7/2015.
 */
public class FullLists {

    public static ArrayList<StatelessItem> getItems(){
        return null; //TODO
    }

    public static ArrayList<Action> getActions(){
        ArrayList<Action> ret = new ArrayList<Action>();
        return ret; //TODO
    }

    public static ArrayList<Action> getDefaultActions(){
        ArrayList<Action> ret = new ArrayList<Action>();
        ret.add(new Run());
        ret.add(new Climb());
        ret.add(new Wait());
        ret.add(new Drop());
        ret.add(new Punch());
        ret.add(new Bite());
        ret.add(new Rest());
        ret.add(new Bark());
        ret.add(new Kick());
        ret.add(new Sneak());
        return ret; //TODO
    }

    public static ArrayList<Perk> getPerks(){
        ArrayList<Perk> ret = new ArrayList<Perk>();
        ret.add(new Skillful());
        ret.add(new Battle_Hardened());
        ret.add(new Athletic());

        return ret;
    }
}
