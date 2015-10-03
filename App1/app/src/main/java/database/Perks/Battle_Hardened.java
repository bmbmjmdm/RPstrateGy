package database.Perks;

import android.util.Log;

import java.util.HashMap;

import Utilities.DamageType;
import Utilities.RemovedException;
import Utilities.Stat;
import database.ObjT.Damage_Taken_Modifier;
import database.Objs.Obj;
import database.Players.CharacterPlayer;
import database.Requirements.statReq;
import Managers.GameManager;

/**
 * Created by Dale on 2/10/2015.
 */
public class Battle_Hardened extends Perk{

    public Battle_Hardened(){
        super("Battle Hardened",
                "<font color=#41A317>Damage &#160;taken &#160;reduced &#160;by &#160;10%</font><br><font color=#E42217>Health &#160;reduced &#160;by &#160;50</font>",
                "<font color=#000000>Requirements:<br></font><font color=#000CCC>Level &#160;50 &#160;Warrior<br>Costs &#160;10 &#160;SP</font><br><font color=#000000>Effects:<br></font>",
                10);
        HashMap<Stat, Integer> statR = new HashMap<>();
        statR.put(Stat.WARRIOR, 50);
        requirements.add(new statReq(statR));
    }

    public void statelessApply(CharacterPlayer c){
        HashMap<Stat, Integer> stats = c.getStats();
        stats.put(Stat.MAX_HEALTH, stats.get(Stat.MAX_HEALTH)-50);
        c.setStats(stats);
    }

    public void statelessUnApply(CharacterPlayer c){
        HashMap<Stat, Integer> stats = c.getStats();
        stats.put(Stat.MAX_HEALTH, stats.get(Stat.MAX_HEALTH)+50);
        c.setStats(stats);
    }

    public void statefullApply(int ObjId){
        try{
            Obj o = GameManager.getInstance().getState().getObjID(ObjId);
            o.addType(new Damage_Taken_Modifier(ObjId, 0, 0.9, DamageType.all, -1));
        }
        catch(RemovedException e){
            Log.e("statefullApply in Battle_Hardened", "User was not found in the state; given faulty objId");
        }
    }

}
