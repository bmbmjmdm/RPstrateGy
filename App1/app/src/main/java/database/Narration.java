package database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import Managers.GameManager;
import Managers.Logic.LogicCalc;
import Utilities.Stat;
import database.Objs.CObjs.BodyPart;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.Objs.PObjs.User;

/**
 * Created by Dale on 5/8/2015.
 */
public class Narration implements Serializable {
    String text;

    //a list of all obj involved, to be used when the narration is clicked to see the obj in map info
    HashSet<Obj>  involves = new HashSet<>();

    //a list of all user id's who can see this
    HashSet<Integer> usersSee = new HashSet<>();

    int time;

    Stat picture;

    public Narration(String says, HashSet<Obj> involve, Stat pic){
        text= says;
        involves = involve;
        picture = pic;

        State s = GameManager.getInstance().getState();

        time = s.getTime();

        //for each user in the state
        for(User u: s.getUsers()){

            //for each object involved
            boolean canSee = true;
            for(Obj o : involves){
                //if they cant see even a single object in involves, they cannot see this narration
                if(u.getVision(o.id).isEmpty())
                    canSee = false;
            }

            if(canSee)
                usersSee.add(u.id);
        }

        s.addNarration(this);

    }




    public HashSet<Integer> getUsersSee(){
        return (HashSet<Integer>) usersSee.clone();
    }

    public HashSet<Obj> getInvolves(){
        return (HashSet<Obj>) involves.clone();
    }

    public String getText(){
        return text;
    }

    public int getTime(){ return time;}

    public Stat getPic(){ return picture;}
}
