package database.Perks;

import android.util.Log;

import java.util.HashMap;

import Utilities.RemovedException;
import Utilities.Stat;
import database.ObjT.Stamina_Modifier;
import database.Objs.Obj;
import database.Players.CharacterPlayer;
import database.Requirements.statReq;
import Managers.GameManager;

/**
 * Created by Dale on 2/11/2015.
 */
public class Athletic extends Perk {

    public Athletic(){
        super("Athletic",
                "<font color=#41A317>Stamina &#160;recovers &#160;twice &#160;as &#160;fast</font><br><font color=#E42217>Rest &#160;recovers &#160;half &#160;as &#160;much &#160;stamina</font>",
                "<font color=#000000>Requirements:<br></font><font color=#000CCC>Level &#160;10 &#160;Acrobat<br>Costs &#160;10 &#160;SP</font><br><font color=#000000>Effects:<br></font>",
                10);
        HashMap<Stat, Integer> statR = new HashMap<>();
        statR.put(Stat.ACROBAT, 10);
        requirements.add(new statReq(statR));
    }

    public void statelessApply(CharacterPlayer c){}

    public void statelessUnApply(CharacterPlayer c){}

    public void statefullApply(int ObjId){
        try {
            Obj o = GameManager.getInstance().getState().getObjID(ObjId);
            o.addType(new Stamina_Modifier(ObjId, 0, 0, 0.5, 1.5, -1));
        }
        catch(RemovedException e){
            Log.e("statefullApply in Athletic", "User was not found in the state; given faulty objId");
        }
    }

}


