package Popups;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import Managers.GameManager;
import Managers.timeKeeper;
import Utilities.RemovedException;
import database.Levels.Level;
import database.Objs.PObjs.User;
import database.State;

/**
 * Created by Dale on 1/3/2015.
 */
public class GameOver extends DialogFragment {
    int gameOver;

    public GameOver(int game){
        super();
        gameOver = game;
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        GameManager.getInstance().getGameAct().finish();
        super.onDismiss(dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Level l = GameManager.getInstance().getState().level;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(l.getEndMessage(gameOver))
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
