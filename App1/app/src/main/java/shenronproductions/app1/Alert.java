package shenronproductions.app1;

import android.widget.Button;

import java.io.Serializable;
import java.util.ArrayList;

import Managers.GameManager;
import Utilities.Callable;

/**
 * Created by Dale on 6/28/2015.
 */
public class Alert implements Serializable {
    public String title;
    public String description;
    public ArrayList<Callable<Void>> respond = new ArrayList<>();
    public int objTID;
    public ArrayList<String> respondText = new ArrayList<>();

    public Alert(String title, String description, int objTID){
        this.title = title;
        this.description = description;
        this.objTID = objTID;
    }

    public void addButton(String text, Callable<Void> call){
        respond.add(call);
        respondText.add(text);
    }

    public void addClearButton(){
        respond.add(new Callable<Void>(){
            public Void call(){
                GameManager.getInstance().getGameAct().removeAlert(Alert.this);
                return null;
            }
        });
        respondText.add("Dismiss");
    }

}
//test