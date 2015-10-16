package shenronproductions.app1.gameUtils;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashSet;

import Managers.GameManager;
import Managers.timeKeeper;
import Utilities.Callable;
import database.ObjT.ObjT;
import shenronproductions.app1.Activities.gameAct;
import shenronproductions.app1.R;

/**
 * Created by Dale on 2/17/2015.
 */
public class AlertAdapter extends RecyclerView.Adapter<NarrateHolder>  {

    private final gameAct mContext;
    ArrayList<Alert> alerts;

    public AlertAdapter() {
        GameManager gm = GameManager.getInstance();
        mContext = gm.getGameAct();
        timeKeeper tk = gm.getTimeline();

        ArrayList<ObjT> types = mContext.curUser.getTypeFull();

        alerts = new ArrayList<>();

        HashSet<Integer> stillExist = new HashSet<>();

        //find all alerts that can be made from objT on the curUser. Ignore any ones that we have been told to ignore
        for(ObjT typ: types){
            if(!mContext.ignoreAlerts.contains(typ.id)) {



                if (typ.isFalling()) {
                    Alert newAlert = new Alert("You are falling!", "Do you want to wait until you land?", typ.id);
                    newAlert.addButton("Yes",  new Callable<Void>(){
                        public Void call(){
                            mContext.continueFall();
                            return null;
                        }
                    });

                    alerts.add(newAlert);
                }


                if (typ.isIntimidated()) {
                    Alert newAlert = new Alert("You piss yourself a little!", typ.getAlerText(), typ.id);
                    newAlert.addClearButton();

                    alerts.add(newAlert);
                }



            }
            else{
                stillExist.add(typ.id);
            }
        }

        ArrayList<Alert> userAlerts = tk.getUserAlerts();
        for(Alert curAlert: userAlerts){
            if(!mContext.ignoreAlerts.contains(curAlert.objTID)) {
                alerts.add(curAlert);
            }
            else{
                stillExist.add(curAlert.objTID);
            }
        }


        //update the ignore list with ones that were found, meaning that if an objT is being ignored and is removed from the user, it is removed from the ignored list
        mContext.ignoreAlerts = stillExist;

        if(alerts.size() > 0) {
            mContext.defaultButtonDrawableOverview = mContext.getResources().getDrawable(R.drawable.smudge_red);
            mContext.defaultButtonNarrationAlerts = mContext.getResources().getDrawable(R.drawable.eye_red);
            mContext.defaultTextColorAlertBut =  mContext.getResources().getColor(R.color.alert_but_text);

        }

    }




    @Override
    public NarrateHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LinearLayout newL = mContext.getNarrationView(true);
        NarrateHolder mh = new NarrateHolder(newL);
        return mh;
    }

    @Override
    public void onBindViewHolder(NarrateHolder nHold, int i) {
        final Alert alertMe = alerts.get(i);
        mContext.fillNarrationView((LinearLayout) nHold.itemView, alertMe);
    }




    @Override
    public int getItemCount() {
        return alerts.size();
    }




}