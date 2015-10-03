package shenronproductions.app1.Adapters;

import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;

import Managers.GameManager;
import database.Actions.Action;
import database.Objs.PObjs.User;
import Managers.Logic.LogicCalc;
import shenronproductions.app1.R;
import shenronproductions.app1.Activities.gameAct;

/**
 * Created by Dale on 2/17/2015.
 */
public class ActionsAdapter extends BaseAdapter {

    private gameAct mContext;
    ArrayList<Action> canUse = new ArrayList<>();
    ArrayList<Action> cannotUse = new ArrayList<>();

    public ActionsAdapter(User u) {
        this.mContext = GameManager.getInstance().getGameAct();
        LogicCalc calc = new LogicCalc();
        ArrayList<Action> userActions = u.actions;
        Action curUsing = GameManager.getInstance().getTimeline().getCurAction();


        for(Action a: userActions){
            Action actual = a.getCopy(u.id);
            if(curUsing != null) {
                if (curUsing.name.compareTo(a.name) == 0) {
                    actual = curUsing.getCopy(u.id);
                }
            }

            if(calc.canUse(u, actual)) {
                canUse.add(actual);
            }
            else {
                cannotUse.add(actual);
            }

        }

    }

    @Override
    public int getCount(){
        return canUse.size()+cannotUse.size();
    }

    @Override// create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        Button nameB;
        if(convertView == null) {
            nameB = new Button(mContext);
            final Action act;

            // if it's not recycled, initialize some attributes
            if (position < canUse.size()) {
                act = canUse.get(position);
                nameB.setBackground(mContext.getResources().getDrawable(act.classes.iterator().next().getPicEnabled()));
            } else {
                act = cannotUse.get(position - canUse.size());
                nameB.setBackground(mContext.getResources().getDrawable(act.classes.iterator().next().getPicDisabled()));
            }


            nameB.setPadding(0, 0, 0, 0);

            //set layout's height to be small
            nameB.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, mContext.getResources().getDisplayMetrics())));

            //set name of act
            nameB.setText(act.name);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), "njnaruto.ttf");
            nameB.setTextColor(mContext.getResources().getColorStateList(R.color.white_text_button));
            nameB.setTypeface(font);

            //add event listener
            nameB.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mContext.actionInfo(act);
                }
            });
        }

        else{
            nameB = (Button) convertView;
        }


        return nameB;
    }





    public long getItemId(int position) {
        return position;
    }



    public Object getItem(int position) {
        if(position< canUse.size()){
            return canUse.get(position);
        }
        else{
            return cannotUse.get(position-canUse.size());
        }
    }




}