package database.Players;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import Utilities.Stat;
import database.Actions.Action;
import database.Perks.Perk;
import database.StatelessItems.StatelessItem;

/**
 * Created by Dale on 12/24/2014.
 * Template for any
 */
public abstract class Player implements Serializable {
    //these represent the raw skills of a player

    //higher stealth will also help detect others sneaking. it wont totally negate the other player's stealth, but it'll definitely help.
    HashMap<Stat, Integer> playerStats = new HashMap<>();
    public String name;

    public ArrayList<Perk> perks = new ArrayList<Perk>();
    public ArrayList<StatelessItem> inventory = new ArrayList<StatelessItem>();
    public ArrayList<Action> actions = new ArrayList<Action>();



    public Player(String n){
        name = n;
        playerStats.put(Stat.NINJA, 0);
        playerStats.put(Stat.MARKSMAN, 0);
        playerStats.put(Stat.MAD_DOCTOR, 0);
        playerStats.put(Stat.WARRIOR, 0);
        playerStats.put(Stat.MAGICIAN, 0);
        playerStats.put(Stat.ENGINEER, 0);
        playerStats.put(Stat.ELEMENTAL, 0);
        playerStats.put(Stat.ACROBAT, 0);
        playerStats.put(Stat.MAX_FOCUS, 100);
        playerStats.put(Stat.MAX_MANA, 100);
        playerStats.put(Stat.MAX_STAMINA, 100);
        playerStats.put(Stat.MAX_HEALTH, 100);
    }

    public void setStats(HashMap<Stat, Integer> stats){
        Iterator<Stat> it = stats.keySet().iterator();
        while(it.hasNext()) {
            Stat name = it.next();
            playerStats.put(name, stats.get(name));
        }
    }

    public Integer getStat(Stat s){
        return playerStats.get(s);
    }

    public void setStat(Stat name, int value){
        playerStats.put(name, value);
    }

    public HashMap<Stat, Integer> getStats(){
        return playerStats;
    }

}
