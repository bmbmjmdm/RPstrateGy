package database.ObjT;

import java.util.ArrayList;

import Managers.GameManager;
import Managers.Logic.LogicCalc;
import Utilities.RemovedException;
import database.Coord;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.State;

/**
 * Created by Dale on 8/30/2015.
 */
public class Stealthed extends ObjT{
    public double widthMod;
    public double heightMod;

    public Stealthed(int oID, double width, double height){
        super("Stealthed", -1, oID, 1);
        widthMod = width;
        heightMod = height;

    }

    @Override
    public boolean isStealthed(){
        return true;
    }


    @Override
    public ArrayList<String> getDescription(){
        ArrayList<String> desc = new ArrayList<String>();
        LogicCalc lc = new LogicCalc();
        State s = GameManager.getInstance().getState();

        //try to find user and return stealth as red or green based on if all leaves are stealthed or not
        try {
            Obj o = s.getObjID(tempBelongsTo);
            boolean fullStealth = true;

            for(CObj co: o.getAllLeafs()){
                for(Coord loc: co.getLoc()){
                    if(!lc.conceals(co, loc, widthMod, heightMod)){
                        fullStealth = false;
                        break;
                    };
                }

                if(!fullStealth)
                    break;
            }

            if(fullStealth) {
                desc.add("<font color=#41A317>" + name + "</font>");
            }
            else{
                desc.add("<font color=#E42217>" + name + "</font>");
            }
        }

        //user wasn't found
        catch(RemovedException re){
            desc.add("<font color=#000000>" + name + "</font>");
        }


        return desc;
    }
}
