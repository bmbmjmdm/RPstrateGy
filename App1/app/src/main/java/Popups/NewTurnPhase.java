package Popups;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import Managers.GameManager;
import Managers.timeKeeper;
import Utilities.RemovedException;
import database.Objs.PObjs.User;
import database.State;
import shenronproductions.app1.R;

/**
 * Created by Dale on 1/3/2015.
 */
public class NewTurnPhase extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        State s = GameManager.getInstance().getState();
        timeKeeper tk = GameManager.getInstance().getTimeline();
        String name = "";
        try {
            //get vision
            User user = (User) s.getObjID(tk.turnObjectID);
            name = user.name;
        }
        catch(RemovedException e){
            //TODO user is gone, game is over?
        }

        String message = "It's "+name+"'s turn!";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
