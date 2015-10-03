package database.Objs.CObjs;

import android.annotation.TargetApi;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.lang.Math;

import Managers.GameManager;
import Utilities.DamageType;
import Utilities.IntObj;
import database.Coord;
import database.ObjT.ObjT;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;


/**
 * Created by Dale on 12/24/2014.
 */
public class BodyPart extends CObj {
    int vertWidth;
    int vertHeight;
    public int lastTimeDamaged = -1000;

    public BodyPart(ArrayList<Coord> co, int own, int hei, int wid, int weigh, int heal, String nam, String im){
        super(co, own, hei, wid, weigh, heal, nam);
        image = im;
        vertHeight = height;
        vertWidth = width;
    }

    @Override
    public Obj getPresentable(Coord c){
        if(parent != null) {
            for (IntObj io : parent.getChildren()) {
                if (io.o.name.contains("Torso")) {
                    Coord middle = io.o.getMiddlemostCoord();
                    if (middle.x == c.x)
                        if (middle.y == c.y)
                            return parent;
                    break;
                }
            }
        }
        return this;
    }


    @Override
    public String getFilterText(){
        if(parent != null) {
            for (IntObj io : parent.getChildren()) {
                if (io.o.name.contains("Torso")) {
                    Coord middle = io.o.getMiddlemostCoord();
                    boolean allThere = true;
                    for (Coord c : loc) {
                        if (middle.x != c.x)
                            allThere = false;
                        if (middle.y != c.y)
                            allThere = false;
                    }

                    if (allThere)
                        return "Player";
                    break;
                }
            }
        }
        String reMe = name;
        reMe.replace("Left ","");
        reMe.replace("Right ", "");
        return reMe;

    }

    @Override
    public String getIcon(Coord c){
        if(!name.contains("Torso")) {
            if (parent != null) {
                for (IntObj io : parent.getChildren()) {
                    if (io.o.name.contains("Torso")) {
                        Coord middle = io.o.getMiddlemostCoord();
                        if (middle.x == c.x)
                            if (middle.y == c.y)
                                return ((CObj) io.o).getIcon(c);
                        break;
                    }
                }
            }
        }
        return image;
    }

    @Override
    public String getIcon(){
        //for all non-torso body parts
        if(!name.contains("Torso")) {
            //check the parents children (aka all body parts)
            if (parent != null) {
                for (IntObj io : parent.getChildren()) {
                    //find the torso
                    if (io.o.name.contains("Torso")) {
                        Coord middle = io.o.getMiddlemostCoord();

                        //if this body part is all on the location of the torso, return the torso's icon
                        boolean allThere = true;
                        for (Coord c : loc) {
                            if (middle.x != c.x)
                                allThere = false;
                            if (middle.y != c.y)
                                allThere = false;
                        }

                        if (allThere)
                            return ((CObj) io.o).getIcon();
                        break;
                    }
                }
            }
        }
        return image;
    }


    public void turnHorizontal(){
        height = vertWidth;
        width = (int) Math.sqrt(vertWidth*vertHeight);
    }

    public void turnVertical(){
        height = vertHeight;
        width = vertWidth;
    }

    @Override
    public void damage(int i, DamageType x){
        lastTimeDamaged = GameManager.getInstance().getState().getTime();
        super.damage(i, x);
    }


}
