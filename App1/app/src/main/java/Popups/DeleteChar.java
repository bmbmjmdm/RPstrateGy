package Popups;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import shenronproductions.app1.R;
import Managers.UserProfile;

/**
 * Created by Dale on 1/3/2015.
 */
public class DeleteChar extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        //can pass R.style.CharGuide as second argument to customize dialog*****************************
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.deleteChar)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try{
                            UserProfile.getInstance().deleteChar();
                            getActivity().invalidateOptionsMenu();
                        }
                        catch(Exception e){
                            Log.e("deleteChar", e.getMessage());
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
