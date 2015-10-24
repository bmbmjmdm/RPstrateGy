package shenronproductions.app1.gameUtils;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.HashMap;

/**
 * Created by Dale on 2/17/2015.
 */
public class CoordHolder extends RecyclerView.ViewHolder {
    //given position in list, is this set
    public Integer isSet = null;
    public Integer defaultBackground = null;

        public CoordHolder(View view) {
            super(view);
        }


}
