package database.Requirements;

import java.util.HashMap;
import java.util.Iterator;

import Utilities.Stat;
import database.Objs.PObjs.User;

/**
 * Created by Dale on 4/6/2015.
 */
public class statCost extends statReq {

    public statCost(HashMap<Stat, Integer> stats){
        super(stats, "statCost");
    }

    //this assumes the user can already use this
    public void pay(User u) {
        HashMap<Stat, Integer> stats = u.getStats();
        Iterator<Stat> it = statsReq.keySet().iterator();
        while (it.hasNext()) {
            Stat name = it.next();
            u.setStat(name, stats.get(name) - statsReq.get(name));
        }
    }


}
