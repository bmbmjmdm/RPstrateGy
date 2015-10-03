package database.Requirements;

import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

import Utilities.Stat;
import database.Objs.PObjs.User;
import database.Players.CharacterPlayer;

/**
 * Created by Dale on 1/4/2015.
 */
public class statReq extends StatelessRequirement {
    //these are all min
    HashMap<Stat, Integer> statsReq = new HashMap<>();

    //set this if you want to check that they have at most the given stats, as opposed to at least
    public boolean atMost = false;

    public statReq(HashMap<Stat, Integer> stats){
        this(stats, "statReq");
    }

    public statReq(HashMap<Stat, Integer> stats, String reqName){
        super(reqName);
        Iterator<Stat> it = stats.keySet().iterator();
        while(it.hasNext()){
            Stat name = it.next();
            statsReq.put(name, stats.get(name));
        }

    }

    public boolean canUse(User p){
        HashMap<Stat, Integer> stats = p.getStats();
        Iterator<Stat> it = statsReq.keySet().iterator();
        while(it.hasNext()){
            Stat name = it.next();

            //must have at least the given stat
            if(!atMost) {
                if (stats.get(name) < statsReq.get(name))
                    return false;
            }

            //must have at most the given stat
            else {
                if (stats.get(name) > statsReq.get(name))
                    return false;
            }
        }
        return true;
    }

    public boolean canUse(CharacterPlayer p){
        HashMap<Stat, Integer> stats = p.getStats();
        Iterator<Stat> it = statsReq.keySet().iterator();
        while(it.hasNext()){
            Stat name = it.next();

            //must have at least the given stat
            if(!atMost) {
                if (stats.get(name) < statsReq.get(name))
                    return false;
            }

            //must have at most the given stat
            else {
                if (stats.get(name) > statsReq.get(name))
                    return false;
            }
        }
        return true;
    }

    public int getStat(Stat s){
        Integer i =  statsReq.get(s);
        if(i == null)
            return 0;
        else
            return i;
    }

    public void setStat(Stat s, int i){
        statsReq.put(s, i);
    }

}
