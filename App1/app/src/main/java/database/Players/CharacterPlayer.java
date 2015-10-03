package database.Players;

import java.util.ArrayList;

import database.Actions.Run;
import database.Perks.Perk;
import database.StatelessItems.StatelessItem;

/**
 * Created by Dale on 1/4/2015.
 */
public class CharacterPlayer extends Player {
    public int charId;
    public ArrayList<Perk> inactivePerks = new ArrayList<Perk>();
    public ArrayList<StatelessItem> inactiveItems = new ArrayList<StatelessItem>();
    public int sp = 100;
    public int spSpent = 0;

    public double wolfLevel = 0.1;


    public CharacterPlayer(String n, int ID){
        super(n);
        charId = ID;
    }
}
