package Popups;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import shenronproductions.app1.R;

/**
 * Created by Dale on 1/3/2015.
 */
public class NewCharGuide extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.newCharGuide)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setPositiveButton("Next", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new NewCharGuide2().show(getFragmentManager(), "NewCharGuideDialogFragment");
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
