package shenronproductions.app1.gameUtils;

import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

import Utilities.StringInt;
import database.Coord;
import database.Objs.CObjs.CObj;
import database.State;
import Managers.GameManager;
import shenronproductions.app1.R;
import shenronproductions.app1.Activities.gameAct;

/**
 * Created by Dale on 2/17/2015.
 */
public class StateAdapter extends RecyclerView.Adapter<CoordHolder> {

    private gameAct mContext;

    public StateAdapter() {
        GameManager gm = GameManager.getInstance();
        this.mContext = gm.getGameAct();
    }

    @Override
    public CoordHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Button newB = new Button(mContext);
        newB.setText("");

        newB.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 6, mContext.getResources().getDisplayMetrics()));
        int two = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, mContext.getResources().getDisplayMetrics());
        newB.setPadding(two, two, two, two);
        newB.setSingleLine(true);
        RecyclerView.LayoutParams buttonParams = new RecyclerView.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()));
        newB.setLayoutParams(buttonParams);
        CoordHolder mh = new CoordHolder(newB);
        return mh;
    }

    @Override
    public void onBindViewHolder(CoordHolder coordHold, int i) {
        State s = GameManager.getInstance().getState();
        final Coord c = s.positionToCoord(i);
        Button b = (Button) coordHold.itemView;
        ArrayList<StringInt> textList = new ArrayList<StringInt>();

        //when processing, there are some CoordHolders that go over the state bounds, which should be black
        if(mContext.processing){
            if(s.testOffMap(c)){
                b.setBackground(mContext.getResources().getDrawable(R.drawable.fog_of_war));
                b.setText("");
                return;
            }
        }

        //TODO fix this so it doesnt need synchronized
        synchronized (mContext.visionLock) {
            TreeSet<CObj> os = s.getObjC(c);


            boolean needsColor = true;
            boolean isHighlighted = false;
            boolean outOfSight = true;

            if (mContext.isHighlightedRed(c)) {
                b.setBackgroundColor(mContext.getResources().getColor(R.color.highlight_red));
                isHighlighted = true;
                needsColor = false;
            } else if (mContext.isHighlightedGreen(c)) {
                b.setBackgroundColor(mContext.getResources().getColor(R.color.highlight_green));
                isHighlighted = true;
                needsColor = false;
            } else if (mContext.isHighlighted(c)) {
                b.setBackgroundColor(mContext.getResources().getColor(R.color.full_white));
                isHighlighted = true;
                needsColor = false;
            }

            for (CObj o : os) {
                boolean sees = mContext.isVisible(o, c);

                if (sees) {
                    outOfSight = false;
                    if (!mContext.isFiltered(o, c)) {
                        textList.add(new StringInt(mContext.getIcon(o, c), o.getHeight() + o.getZ(c.x, c.y)));
                        if (!isHighlighted) {
                            int color = mContext.getColor(o);
                            if (color != -1) {
                                b.setBackground(mContext.getResources().getDrawable(color));
                                needsColor = false;
                            }
                        }
                    }
                }
            }
            if (needsColor) {
                if (outOfSight)
                    b.setBackground(mContext.getResources().getDrawable(R.drawable.fog_of_war)); //android.R.drawable.btn_default
                else
                    b.setBackground(mContext.getResources().getDrawable(R.drawable.default_ground));

            }


            Collections.sort(textList, new Comparator<StringInt>() {
                @Override
                public int compare(StringInt p1, StringInt p2) {
                    return p2.i - p1.i; // Ascending
                }
            });
            String text = "";
            for (StringInt SI : textList) {
                if (!text.contains(SI.toString())) {
                    text = text.concat(SI.toString());
                }
            }
            b.setText(text);
            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mContext.mapClicked(c);
                }
            });
        }



    }

    @Override
    public int getItemCount() {
        int x = GameManager.getInstance().getState().getSize();
        int actualSize = x*x;
        //we increase the size when processing to make sure the map doesn't "bottom out" when it expands to fit the screen
        if(mContext.processing)
            return actualSize + (x*20);
        else
            return actualSize;
    }



}