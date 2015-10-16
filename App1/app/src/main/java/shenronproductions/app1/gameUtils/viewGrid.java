package shenronproductions.app1.gameUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import Managers.GameManager;
import Managers.Logic.LogicCalc;
import Utilities.Callable;
import Utilities.IntObj;
import database.Coord;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.Objs.PObjs.PObj;
import shenronproductions.app1.Activities.gameAct;
import shenronproductions.app1.R;

/**
 * Created by Dale on 10/9/2015.
 */
public class viewGrid {

    HashMap<Integer, HashSet<Obj>> objByZ;
    HashMap<Integer, HashSet<Integer>> zByObj;

    //0-based and not "real" z, z here is 0, 1, 2, 3 etc corresponding to row #
    HashMap<Integer, Integer> leftMostFreeSlotByZ;
    HashMap<Coord, Boolean> usedSpots;
    gameAct gc = GameManager.getInstance().getGameAct();
    boolean findAllChildren;

    //the collection passed is unmodified, it is purely the obj the caller wants to display
    //masterView is where the grid is being added
    //buttons are callables that need to be buttons on each obj's zoomed-in description


    //this is for no particular coordinate, which is when we can have obj
    public viewGrid(Collection<Obj> allObj, boolean checkPresentable, boolean findAll){
        fillMap(allObj, checkPresentable);
        findAllChildren = findAll;
    }


    //same as above, but x/y positioning matters for allObj so we know they are all CObj (though can still belong to an Obj)
    public viewGrid(Collection<CObj> allObj, Coord c, boolean checkPresentable, boolean findAll) {
        fillMap(allObj, c, checkPresentable);
        findAllChildren = findAll;
    }













    private void fillMap(Collection<Obj> allObj, boolean checkPresentable) {
        //get the obj representation of all Obj (aka person vs bodypart, etc) if usePresentable is set
        Collection<Obj> newObj = new HashSet<Obj>();
        for (Obj co : newObj) {
            newObj.add(getMapPresentable(co, checkPresentable));
        }

        allObj = newObj;


        prepGrid(allObj);
        LogicCalc lc = new LogicCalc();

        //find out which brackets each obj belongs to based on which they overlap
        for (Obj o : allObj) {
            for (Integer z : objByZ.keySet()) {

                //if they overlap, add them to the bracket
                if (lc.overlapping(o, z)) {
                    objByZ.get(z).add(o);
                }

            }
        }

        condenseGrid();
    }



    private void fillMap(Collection<CObj> allCObj, Coord c, boolean checkPresentable){
        //get the obj representation of all CObj (aka person vs bodypart, etc)
        Collection<Obj> allObj = new HashSet<Obj>();
        for(CObj co: allCObj){
            allObj.add(getMapPresentable(co, c, checkPresentable));
        }

        prepGrid(allObj);
        LogicCalc lc = new LogicCalc();

        //find out which brackets each obj belongs to based on which they overlap
        for (Obj o: allObj){
            for(Integer z: objByZ.keySet()){

                //if they overlap, add them to the bracket
                if(lc.overlapping(o, z, c)){
                    objByZ.get(z).add(o);
                }

            }
        }

        condenseGrid();
    }

    //not sure if this should be recursive or not
    private Obj getMapPresentable(Obj o, Coord c, boolean checkPresentable){
        //check to see if we force the parent
        Boolean showParent = gc.showParent.get(o.id);
        if(showParent != null){
            if(showParent){
                return o.parent;
            }
        }

        //check to see if we force self
        Boolean showSelf = gc.showSelf.get(o.id);
        if(showSelf != null){
            if(showSelf){
                return o;
            }
        }

        if(checkPresentable) {
            return o.getPresentable(c);
        }
        else{
            return o;
        }
    }

    //not sure if this should be recursive or not
    private Obj getMapPresentable(Obj o, boolean checkPresentable){
        //check to see if we force the parent
        Boolean showParent = gc.showParent.get(o.id);
        if(showParent != null){
            if(showParent){
                return o.parent;
            }
        }

        //check to see if we force self
        Boolean showSelf = gc.showSelf.get(o.id);
        if(showSelf != null){
            if(showSelf){
                return o;
            }
        }

        if(checkPresentable) {
            return o.getPresentable();
        }
        else{
            return o;
        }
    }











    private void prepGrid(Collection<Obj> allObj){
        objByZ = new HashMap<>();

        //determine all the brackets there are, based on the z coordinate of every obj
        for(Obj o: allObj){
            objByZ.put(o.getLowestZ(), new HashSet<Obj>());
        }
    }


    //condense the grid
    //as you condense, set up seperate hashmap representing which layers an obj definitively exists at
    private void condenseGrid(){
        zByObj = new HashMap<>();

        //get all the z that things exist at. sort it into ascending order
        ArrayList<Integer> allHeights = new ArrayList<>(objByZ.keySet());
        Collections.sort(allHeights);

        //if there is nothing, return
        if(allHeights.isEmpty())
            return;

        //otherwise, get the first set to start with
        int curHeight = allHeights.get(0);
        HashSet<Obj> lastSet = objByZ.get(curHeight);

        //for every height in the map
        for(int i = 1; i < allHeights.size() ; i++){
            curHeight = allHeights.get(i);

            //compare it to the last set to see if either are subsets
            HashSet<Obj> nextSet = objByZ.get(curHeight);

            if(nextSet.containsAll(lastSet)){
                //the nextSet contains all of the previous set, meaning it has more members and should remove the previous set from the map
                objByZ.remove(curHeight);
            }

            //we DO NOT NEED TO see if lastSet contains all of nextSet, as the nextSet MUST HAVE HAD a new element in order to have existed as a seperate z in the first place!

            else{
                int lastHeight = allHeights.get(i-1);
                //the last set is unique, so tell all of its members they exist in it via the new map
                for(Obj o: lastSet){
                    HashSet<Integer> oLoc = zByObj.get(o.id);
                    if(oLoc == null)
                        oLoc = new HashSet<Integer>();
                    oLoc.add(lastHeight);
                    zByObj.put(o.id, oLoc);
                }
            }

            //go on to the next
            lastSet = nextSet;
        }

        //the last set is by default unique, so tell all of its members they exist in it via the new map
        for(Obj o: lastSet){
            HashSet<Integer> oLoc = zByObj.get(o.id);
            if(oLoc == null)
                oLoc = new HashSet<Integer>();
            oLoc.add(curHeight);
            zByObj.put(o.id, oLoc);
        }
    }

















    public void addToView(HorizontalScrollView masterView){
        masterView.removeAllViews();

        leftMostFreeSlotByZ = new HashMap<>();
        usedSpots = new HashMap<>();

        GridLayout myGrid = new GridLayout(gc);
        int wrap =  HorizontalScrollView.LayoutParams.WRAP_CONTENT;
        HorizontalScrollView.LayoutParams layoutParams = new HorizontalScrollView.LayoutParams(wrap, wrap);

        //descending
        ArrayList<Integer> sortedZ = new ArrayList <>(objByZ.keySet());
        Collections.sort(sortedZ, new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return t1 - integer;
            }
        });



        //first go through all obj from highest to lowest and ONLY ADD multi-bracket objects
        //record non-multi-bracket objects
        HashMap<Integer, HashSet<Obj>> singletons = new HashMap<>();
        //remember which multi-bracket obj you've already drawn
        HashMap<Integer, Boolean> drawn = new HashMap<>();

        //go through the list from highest z to lowest
        for(int index = 0; index < sortedZ.size(); index++){
            HashSet<Obj> objects = objByZ.get(sortedZ.get(index));

            //go through all obj at that z
            for(Obj o: objects){

                HashSet<Integer> existsOn = zByObj.get(o.id);
                //does it exist on multiple spots?
                if(existsOn.size() > 1){

                    //did we draw it already?
                    if(drawn.get(o.id) == null) {

                        drawn.put(o.id, true);
                        //create this object as a button and add it to the grid
                        //this will also update the placement hashmaps
                        getButton(o, index, existsOn.size(), myGrid);
                    }
                }

                //if it doesnt exist on multiple spots, it only exists on one. add it to the singleton list for this z
                else{
                    HashSet<Obj> singles = singletons.get(index);
                    if(singles == null)
                        singles = new HashSet<Obj>();
                    singles.add(o);
                    singletons.put(index, singles);
                }

            }

        }

        //now go through all obj from highest to lowest and add all obj that were not used above

        //go through the list from highest z to lowest
        for(int index = 0; index < sortedZ.size(); index++){
            HashSet<Obj> objects = singletons.get(index);

            if(objects != null) {
                //go through all obj at that z
                for (Obj o : objects) {

                    //create this object as a button and add it to the grid
                    //this will also update the placement hashmaps
                    getButton(o, index, 1, myGrid);
                }
            }
        }

        //we are done
        masterView.addView(myGrid, layoutParams);
    }



    private void getButton(final Obj o, final int row, final int span, GridLayout grid){
        final int column = getColumnUpdateMaps(row, span);
        final int marginsPadding = 4;
        final int dimensions = 87;
        final int marginsHor = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginsPadding, gc.getResources().getDisplayMetrics());
        final int marginsVert = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginsPadding, gc.getResources().getDisplayMetrics());
        final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (dimensions-(marginsPadding*2)), gc.getResources().getDisplayMetrics());
        final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (dimensions*span - marginsPadding*2), gc.getResources().getDisplayMetrics());
        final int image = o.getImage();


        //padding equal to margines, center the image inside the button with max size while mantaining apsect ratio
        final ImageButton container = new ImageButton(gc);
        container.setPadding(marginsHor, marginsVert, marginsHor, marginsVert);
        container.setImageDrawable(gc.getResources().getDrawable(image));
        container.setScaleType(ImageView.ScaleType.CENTER_INSIDE);


        //small horizontal margins and large vertical margins, set constant width and constant height*span, supply row and column index
        GridLayout.Spec rowSpec;
        if(span == 1){
            rowSpec = GridLayout.spec(row);
        }
        else{
            rowSpec = GridLayout.spec(row, row + span);
        }
        GridLayout.Spec columnSpec = GridLayout.spec(column);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(rowSpec, columnSpec);
        layoutParams.height = height;
        layoutParams.width = width;
        layoutParams.setMargins(marginsHor, marginsVert, marginsHor, marginsVert);


        //set up zoom in function
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomInOnObj(container, image, o);
            }
        });


        //add to grid
        grid.addView(container, layoutParams);

    }




    private int getColumnUpdateMaps(int row, int span){
        //get a free x position at the given grid row as well as all rows this obj spans
        boolean found = false;
        Integer freeX = leftMostFreeSlotByZ.get(row);
        if(freeX == null){
            freeX = 0;
        }

        //loop (increasing x) until we find such a spot
        while(!found) {

            for (int freeY = row; freeY < row + span; freeY++) {
                found = false;
                Coord takeThisSpot = new Coord(freeX, freeY);

                //if it is not null, something else has already taken this spot
                if(usedSpots.get(takeThisSpot) != null){
                    freeX++;
                    break;
                }

                found = true;
            }

        }

        //now update the maps because we found a spot to use!
        for (int freeY = row; freeY < row + span; freeY++) {
            Coord takeThisSpot = new Coord(freeX, freeY);
            //this spot is used
            usedSpots.put(takeThisSpot, true);

            Integer leftMostFree = leftMostFreeSlotByZ.get(freeY);
            //if this row has not been used before and we are in its 0, start it at 1
            if(leftMostFree == null){
                if(freeX == 0){
                    leftMostFreeSlotByZ.put(freeY, 1);
                }
            }
            //if this spot was the first free X at this freeY aka z, increment!
            else if(leftMostFree == freeX){
                leftMostFreeSlotByZ.put(freeY, freeX+1);
            }
        }


        return freeX;
    }
















































    AnimatorSet mCurrentAnimator = null;

    private void zoomInOnObj(final View thumbView, int imageId, final Obj o) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageButton imageInFrame = (ImageButton) gc.findViewById(R.id.mapInfoPicture);
        imageInFrame.setImageResource(imageId);

        // Add the details to the detail list/grid
        final LinearLayout detailsGrid = (LinearLayout) gc.findViewById(R.id.mapInfoDetails);
        detailsGrid.removeAllViews();


        //get the zoomed in frame as a whole and begin morphing
        final FrameLayout expandedImageView = (FrameLayout) gc.findViewById(R.id.mapInfoZoomed);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        gc.findViewById(R.id.mapInfoFrame)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        final int animationDuration = 200;
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(animationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;

                //highlight observed obj's coords
                gc.highlightCoords(gc.getHighlightableCoords(o));
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        final Callable<Void> closeExpanded = new Callable<Void>() {
            @Override
            public Void call() {

                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(animationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;

                        //highlight default after onserved obj has finished zooming out
                        gc.highlightCoords(gc.defaultHighlight);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });


                set.start();
                mCurrentAnimator = set;

                return null;
            }
        };

        detailsGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeExpanded.call();
            }
        });
        imageInFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeExpanded.call();
            }
        });

        fillDetails(o, detailsGrid);
    }




    private void fillDetails(final Obj o, LinearLayout detailsGrid) {
        ArrayList<String> description = o.getDescription();
        int sp12 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, gc.getResources().getDisplayMetrics());
        int wrap = LinearLayout.LayoutParams.WRAP_CONTENT;
        int dp7 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, gc.getResources().getDisplayMetrics());
        int dp30 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, gc.getResources().getDisplayMetrics());
        int dp50 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, gc.getResources().getDisplayMetrics());
        int dp300 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, gc.getResources().getDisplayMetrics());
        Typeface font = Typeface.createFromAsset(gc.getAssets(), "njnaruto.ttf");


        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(dp300, dp50);
        buttonParams.gravity = Gravity.CENTER_HORIZONTAL;
        buttonParams.setMargins(0, 0, 0, dp7);

        //set up buttons to break up a parent into its children
        if(o.isParent()){
            //first the button to break it up
            Button breakUp = new Button(gc);
            breakUp.setTypeface(font);
            breakUp.setTextSize(sp12);
            breakUp.setText("View As Parts");
            breakUp.setTextColor(gc.getResources().getColorStateList(R.color.white_text_button));
            breakUp.setBackground(gc.getResources().getDrawable(R.drawable.brush2_button));
            breakUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //for all my childen
                    for(IntObj io: ((PObj) o).getChildren()){
                        Obj co = io.o;
                        //make them not want to insist on showing me
                        gc.showParent.put(co.id, false);
                        //and make them insist on showing themselves
                        gc.showSelf.put(co.id, true);
                    }
                    //refresh
                    gc.updateInfo();

                }
            });

            detailsGrid.addView(breakUp, buttonParams);

            //things like actions dont want you to find all children seeable
            if(findAllChildren) {
                //next the button to find all children
                Button findAll = new Button(gc);
                findAll.setTypeface(font);
                findAll.setTextSize(sp12);
                findAll.setText("Find All Parts");
                findAll.setTextColor(gc.getResources().getColorStateList(R.color.white_text_button));
                findAll.setBackground(gc.getResources().getDrawable(R.drawable.brush2_button));
                findAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //for all my childen
                        for(IntObj io: ((PObj) o).getChildren()){
                            Obj co = io.o;
                            //make them not want to insist on showing me
                            gc.showParent.put(co.id, false);
                            //and make them insist on showing themselves
                            gc.showSelf.put(co.id, true);
                        }
                        //now view parent
                        gc.setObjects(o);
                    }
                });


                detailsGrid.addView(findAll, buttonParams);
            }
        }


        //set up button to consolidate child into parent
        if(o.hasParent()){
            //next the button to find all children
            Button viewWhole = new Button(gc);
            viewWhole.setTypeface(font);
            viewWhole.setTextSize(sp12);
            viewWhole.setText("View As Whole");
            viewWhole.setTextColor(gc.getResources().getColorStateList(R.color.white_text_button));
            viewWhole.setBackground(gc.getResources().getDrawable(R.drawable.brush2_button));
            viewWhole.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PObj parent = (PObj) o.parent;
                    //for me and all my siblings
                    for(IntObj io: parent.getChildren()){
                        Obj co = io.o;
                        //make them want to show their parent instead of showing self
                        gc.showParent.put(co.id, true);
                        //and make them not insist on showing themselves by default either
                        gc.showSelf.put(co.id, false);
                    }
                    gc.updateInfo();

                }
            });

            detailsGrid.addView(viewWhole, buttonParams);
        }



        //the first detail has a large margin to keep it away from the buttons or top
        int marginSize = dp30;

        //display all objT on alternating sides of the linear layout
        boolean swap = true;
        for (String s : description) {
            TextView gridView;

            gridView = new TextView(gc);
            gridView.setText(Html.fromHtml(s));
            gridView.setTypeface(font);
            gridView.setTextSize(sp12);
            gridView.setBackgroundColor(gc.getResources().getColor(R.color.trans_grey));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(wrap, wrap);
            params.setMargins(0, 0, 0, marginSize);
            if (swap) {
                params.gravity = Gravity.LEFT;
                swap = false;
            } else {
                params.gravity = Gravity.RIGHT;
                swap = true;
            }
            gridView.setLayoutParams(params);
            detailsGrid.addView(gridView);

            marginSize = dp7;
        }
    }


}
