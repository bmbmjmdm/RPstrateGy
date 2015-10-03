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

/**
 * Created by Dale on 1/3/2015.
 */
public class ProcessPastTurn extends DialogFragment {

    @Override
    public void onDismiss(DialogInterface dialog){
        final GameManager gm = GameManager.getInstance();
        gm.processPastTurn();
        super.onDismiss(dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();
        String name = "";
        try {
            //get vision
            User user = (User) s.getObjID(tk.turnObjectID);
            name = user.name;
        }
        catch(RemovedException e){
            //TODO user is gone, game is over?
        }

        String message = "It's "+name+"'s turn! First, watch your past turn finish and decide if you want to change anything!";
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
