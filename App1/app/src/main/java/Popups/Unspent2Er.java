package Popups;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Dale on 1/6/2015.
 */
public class Unspent2Er extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        //can pass R.style.CharGuide as second argument to customize dialog*****************************
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("You have unused stat points!")
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
