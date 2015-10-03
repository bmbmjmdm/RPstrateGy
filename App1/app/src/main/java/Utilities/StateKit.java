package Utilities;

import database.Players.CharacterPlayer;

/**
 * Created by Dale on 4/15/2015.
 */
public class StateKit {
    public String name;
    public String turns;
    public String terrain;
    public String size;
    public CharacterPlayer p1; public CharacterPlayer p2;
    public boolean dayTime;

    public StateKit(String n, String t, String ter, CharacterPlayer play1, CharacterPlayer play2, String si, boolean day){
        name = n;
        turns = t;
        terrain = ter;
        size = si;
        p1 = play1;
        p2 = play2;
        dayTime = day;
    }
}