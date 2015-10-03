package database.Levels;

import android.widget.LinearLayout;

import java.io.Serializable;

import Managers.GameManager;
import Managers.UserProfile;
import Utilities.UnableException;
import database.Objs.PObjs.User;
import database.Players.CharacterPlayer;

/**
 * Created by Dale on 8/23/2015.
 */
public abstract class Level implements Serializable {
    public String fileName;
    public String presentableName;
    public String terrain;
    public String turnLimit;
    public boolean day;
    public String size;

    //when curTime == nextTurn, the AI for the level decides what action it wants to do
    public int nextTurn;

    double difficulty;

    public Level(String nam, String presName){
        fileName = nam;
        presentableName = presName;

        nextTurn = 1;

        CharacterPlayer player1 = UserProfile.getInstance().curChar;

        difficulty = player1.wolfLevel;

        setStateParams();
    }

    protected abstract void setStateParams();

    //0 == not over
    //1 == user won!
    //-1 == user lost :(
    public abstract int isOver();

    public abstract void getNarration(LinearLayout container);

    public abstract void setUpState() throws UnableException;

    //when curTime == nextTurn, the AI for the level decides what action it wants to do
    //doTurn should Always update nextTurn. It can do this with a constant or based on its enemies or what
    //this should also take into account that the level users could have had their actions stopped and so their nextTurn might be different
    public abstract void doTurn();


    public abstract String getEndMessage(int status);
}
