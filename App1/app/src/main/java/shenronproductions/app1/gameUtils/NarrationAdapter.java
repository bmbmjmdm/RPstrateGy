package shenronproductions.app1.gameUtils;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import Managers.GameManager;
import database.Narration;
import database.State;
import shenronproductions.app1.Activities.gameAct;

/**
 * Created by Dale on 2/17/2015.
 */
public class NarrationAdapter extends RecyclerView.Adapter<NarrateHolder>  {

    private gameAct mContext;
    ArrayList<ArrayList<Narration>> canView = new ArrayList<>();

    public NarrationAdapter() {
        GameManager gm = GameManager.getInstance();
        this.mContext = gm.getGameAct();
        State s = gm.getState();
        int userId = gm.getTimeline().turnObjectID;


        for(ArrayList<Narration> story: s.getNarrationOld()){
            ArrayList<Narration> canViewThese = new ArrayList<>();
            for(Narration n: story) {
                boolean canSee = false;
                for (Integer id : n.getUsersSee()) {
                    if (id == userId)
                        canSee = true;
                }
                if(canSee){
                    canViewThese.add(n);
                }

            }
            if(canViewThese.size() > 0)
                canView.add(canViewThese);
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
        final ArrayList<Narration> narrate = canView.get(i);
        mContext.fillNarrationView((LinearLayout) nHold.itemView, narrate);
    }




    @Override
    public int getItemCount() {
        return canView.size();
    }




}