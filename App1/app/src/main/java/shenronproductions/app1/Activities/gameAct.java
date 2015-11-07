package shenronproductions.app1.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import Managers.GameManager;
import Managers.timeKeeper;
import Popups.GameOver;
import Popups.NewTurnPhase;
import Popups.ProcessPastTurn;
import Utilities.Callable;
import Utilities.Constants;
import Utilities.EmptyCallable;
import shenronproductions.app1.gameUtils.HScroll;
import Utilities.IntObj;
import shenronproductions.app1.gameUtils.VScroll;
import database.Actions.ActionSteps.ActionStep;
import database.Actions.Climb;
import database.Actions.SubActions.SubAction;
import database.Objs.PObjs.PObj;
import database.Requirements.Requirement;
import shenronproductions.app1.gameUtils.ActionsAdapter;
import Utilities.ClimbObj;
import shenronproductions.app1.gameUtils.FixedGridLayoutManager;
import shenronproductions.app1.gameUtils.AlertAdapter;
import shenronproductions.app1.gameUtils.CoordHolder;
import shenronproductions.app1.gameUtils.NarrationAdapter;
import Utilities.RemovedException;
import Utilities.Stat;
import shenronproductions.app1.gameUtils.StateAdapter;
import database.Actions.Action;
import database.Coord;
import database.Narration;
import database.ObjT.ObjT;
import database.Objs.CObjs.CObj;
import database.Objs.Obj;
import database.Objs.PObjs.User;
import database.State;
import Managers.Logic.LogicCalc;
import shenronproductions.app1.gameUtils.Alert;
import shenronproductions.app1.R;
import shenronproductions.app1.gameUtils.viewGrid;


public class gameAct extends Activity {
    TextView curSelected = null;
    LinearLayout curOpened = null;
    HashSet<Coord> curHighlighted = new HashSet<Coord>();
    public HashSet<Coord> defaultHighlight = new HashSet<Coord>();
    public View curInfoView = null;
    public View curNarrateView = null;

    HashSet<String> filterThese = new HashSet<String>();
    HashSet<String> manualFiltered = new HashSet<String>();
    HashSet<String> manualUnFiltered = new HashSet<>();
    boolean customFilter = true;
    String presetFilter = "";
    ImageButton presetFilterSelected = null;
    HashMap<Coord, HashSet<CObj>> canClimb = new HashMap<>();

    public Set<Coord> actionHighlightedWhite = new HashSet<>();
    public Set<Coord> actionHighlightedGreen = new HashSet<>();
    public Set<Coord> actionHighlightedRed = new HashSet<>();

    Action curAction = null;

    RecyclerView gameMap;

    Stack<Object> mapInfoHistory = new Stack<Object>();

    boolean continueFalling = false;

    boolean selectingActionInput = false;

    public boolean actionTakesMapClick = false;

    public User curUser;

    public Boolean noUpdateMap = false;

    public Handler mainHandler;

    public boolean processing = false;

    public Object visionLock = new Object();

    public Boolean waitOnVision = false;

    HashSet<String> alreadyDisplayed = new HashSet<String>();

    public Callable<Boolean> dispatchTouch = null;

    public HashSet<Integer> ignoreAlerts = new HashSet<>();

    HashSet<Button> highlightedNav = new HashSet<>();
    HashSet<Button> highlightedOverview = new HashSet<>();
    Drawable defaultButtonDrawable;
    ColorStateList defaultButtonText;
    Drawable selectedButtonDrawable;
    ColorStateList selectedButtonText;
    Drawable defaultButtonNarration;
    Drawable selectedButtonNarration;
    public Drawable defaultButtonDrawableOverview;
    public Drawable defaultButtonNarrationAlerts;
    public int defaultTextColorAlertBut;
    ColorStateList defaultNarrationButtonText;
    ColorStateList selectedNarrationButtonText;

    public int gameOver = 0;


    HashMap<Integer, ArrayList<Coord>> visionCur = new HashMap<>();
    HashMap<Integer, ArrayList<Coord>> visionPast = new HashMap<>();
    HashMap<Integer, HashMap<Coord, String>> iconCur = new HashMap<>();
    HashMap<Integer, HashMap<Coord, String>> iconPast = new HashMap<>();
    HashMap<Integer, Integer> colorCur = new HashMap<>();
    HashMap<Integer, Integer> colorPast = new HashMap<>();
    public boolean visionChanged = false;

    //used by viewGrid for breaking up or putting together obj
    public HashMap<Integer, Boolean> showParent = new HashMap<>();
    public HashMap<Integer, Boolean> showSelf = new HashMap<>();
    //used by viewGrid for breaking up or putting together obj while viewing parent obj
    public HashMap<Integer, Boolean> showParentPOBJ = new HashMap<>();
    public HashMap<Integer, Boolean> showSelfPOBJ = new HashMap<>();
    public boolean inPOBJ = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_view);

        mainHandler = new Handler();

        defaultButtonDrawable = getResources().getDrawable(R.drawable.smudge);
        selectedButtonDrawable = getResources().getDrawable(R.drawable.smudge_inverted);
        defaultButtonText = getResources().getColorStateList(R.color.white_text_button);
        selectedButtonText = getResources().getColorStateList(R.color.black_text_button);
        defaultNarrationButtonText = getResources().getColorStateList(R.color.black_text_button);
        selectedNarrationButtonText = getResources().getColorStateList(R.color.white_text_button);
        defaultButtonNarration = getResources().getDrawable(R.drawable.eye);
        selectedButtonNarration = getResources().getDrawable(R.drawable.eye_inverted);
        defaultButtonDrawableOverview = getResources().getDrawable(R.drawable.smudge);
        defaultButtonNarrationAlerts =  getResources().getDrawable(R.drawable.eye);
        defaultTextColorAlertBut =  getResources().getColor(R.color.black_text_button);


        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();
        gm.setGameAct(this);

        try {
            curUser = (User) s.getObjID(tk.turnObjectID);
            visionChanged = true;
        }
        catch(RemovedException e){
            Log.e("User is gone when in onCreate of gameAct", "gameOver?");
            //TODO
        }

        //create the map
        gameMap = (RecyclerView) findViewById(R.id.recycler);
        FixedGridLayoutManager layoutMan = new FixedGridLayoutManager();
        layoutMan.setTotalColumnCount(s.getSize());
        layoutMan.setMaxScrollSpeed(90);
        layoutMan.setMinScrollSpeed(-90);
        gameMap.setLayoutManager(layoutMan);
        gameMap.setAdapter(new StateAdapter());


        //give map info grid scrollability
        findViewById(R.id.mapInfoScroller).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return scrollDiagnalGrid(motionEvent, false);
            }
        });

        //give map info grid scrollability
        findViewById(R.id.actionsInScroller).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return scrollDiagnalGrid(motionEvent, true);
            }
        });


        //format actionInput view
        Typeface font3 = Typeface.createFromAsset(getAssets(), "MTCORSVA.TTF");
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        TextView actInfo = ((TextView) findViewById(R.id.actInInfo));
        actInfo.setTypeface(font3);
        actInfo.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        actInfo.setTextColor(getResources().getColor(R.color.full_black));

        ((Button) findViewById(R.id.actReturn)).setTypeface(font);

        //format turn status view
        ((TextView) findViewById(R.id.timeText)).setTypeface(font3);

        //format buttons
        ((Button) findViewById(R.id.mapFilterBut)).setTypeface(font);
        ((Button) findViewById(R.id.narrateBut)).setTypeface(font);
        ((Button) findViewById(R.id.mapInfoBut)).setTypeface(font);
        ((Button) findViewById(R.id.actionsBut)).setTypeface(font);
        ((Button) findViewById(R.id.findCurUser)).setTypeface(font);
        ((Button) findViewById(R.id.turnStatusBut)).setTypeface(font3);
        ((Button) findViewById(R.id.userStatusBut)).setTypeface(font3);
        ((Button) findViewById(R.id.historyBut)).setTypeface(font3);
        ((Button) findViewById(R.id.alertsBut)).setTypeface(font3);
        ((Button) findViewById(R.id.customFiltersBut)).setTypeface(font3);
        ((Button) findViewById(R.id.presetFiltersBut)).setTypeface(font3);
        ((Button) findViewById(R.id.infoBackBut)).setTypeface(font3);


        showTurnStatus(null);

        //process state up to the current time
        if(!getIntent().getExtras().getBoolean("New"))
            gm.processTimeBeginning();
        else {
            setUpGame();
            newTurnPhase(false);
        }

        offlineAct.progress.dismiss();


    }



    public void setUpGame(){
        findViewById(R.id.actionsInZoomed).setVisibility(View.GONE);
        findViewById(R.id.mapInfoZoomed).setVisibility(View.GONE);

        if(continueFalling)
            continueFall();
        else
            continueSetUp();

    }


    private void continueSetUp(){
        clearInfo();
        mapInfoHistory = new Stack<>();
        State s = GameManager.getInstance().getState();
        timeKeeper tk = GameManager.getInstance().getTimeline();


        try {
            curUser = (User) s.getObjID(tk.turnObjectID);
            visionChanged = true;

            //populate actions
            GridView actionOpt = (GridView) findViewById(R.id.actionsAvailable);
            actionOpt.setAdapter(new ActionsAdapter(curUser));
            ((LinearLayout) findViewById(R.id.actionsInfo)).removeAllViews();


            Action a = tk.getCurAction();
            //load up the action input view if the user has the option to continue with an action

            if(a != null){
                startAction(a, true);
            }
            //default view is narration
            else{
                curAction = null;
                showActView(false);
                showNarration(null);
                showTurnStatus(null);
            }


            //setup narration
            RecyclerView narration = (RecyclerView) findViewById(R.id.history);
            LinearLayoutManager narrationLayout = new LinearLayoutManager(this);
            narrationLayout.setOrientation(LinearLayoutManager.VERTICAL);
            narrationLayout.setStackFromEnd(true);
            narration.setLayoutManager(narrationLayout);
            narration.setAdapter(new NarrationAdapter());


            updateTurnStatus();
            updateUserStatus();
            updateTime();
            updateAlerts();
            updateActionAlert();
            updateCustomFilters();


        }
        catch(RemovedException e){
            //TODO user is gone, game is over?
            Log.e("User not found in state during setUp", "Is game over?");
        }
    }




    //TODO the NewTurnPhase() popup needs a catch block because it looks up the user
    public void newTurnPhase(boolean processPastTurn){

        findViewById(R.id.mapInfoZoomed).setVisibility(View.INVISIBLE);
        ((ScrollView) findViewById(R.id.mapInfoZoomedScroll)).fullScroll(View.FOCUS_UP);
        showParent = new HashMap<>();
        showSelf = new HashMap<>();
        showParentPOBJ = new HashMap<>();
        showSelfPOBJ = new HashMap<>();
        inPOBJ = false;

        filterThese = new HashSet<>();
        manualFiltered = new HashSet<>();
        manualUnFiltered = new HashSet<>();

        updateCustomFilters();
        updatePresetFilters();
        ((ScrollView) findViewById(R.id.viewCustomFilters)).fullScroll(View.FOCUS_UP);
        ((ScrollView) findViewById(R.id.viewPresetFilters)).fullScroll(View.FOCUS_UP);

        //set the gamemap to view the new player
        centerView();
        updateCoords();


        if(processPastTurn){
            new ProcessPastTurn().show(getFragmentManager(), "ProcessPastTurnFragmentManager");
        }
        else {
            showTurnMessage();
        }

    }


    public void showTurnMessage(){
        new NewTurnPhase().show(getFragmentManager(), "NewTurnPhaseFragmentManager");
    }



    public void endGame(){
        new GameOver(gameOver).show(getFragmentManager(), "GameOverFragmentManager");
    }






    public void updateCustomFilters(){
        State s = GameManager.getInstance().getState();

        //populate the custom filter options based on what objects are visible
        LinearLayout custFilterOptions = (LinearLayout) findViewById(R.id.customFilterOptions);
        custFilterOptions.removeAllViews();


        alreadyDisplayed = new HashSet<String>();
        Iterator<Integer> canSeeIDS = curUser.getVisionKeySet().iterator();
        while(canSeeIDS.hasNext()){
            try {
                //get each cobj the user can see
                CObj co = (CObj) s.getObjID(canSeeIDS.next());
                String icon = co.getIcon();

                //did we make a filter for this type of cobj already?
                if (!alreadyDisplayed(icon)) {

                    //can we see this cobj?
                    ArrayList<Coord> seeAt = getVision(co.id);
                    if (seeAt != null) {
                        if (seeAt.size() > 0) {

                            //does this cobj have a filter?
                            String text = co.getFilterText();
                            if (text != null) {

                                //create filter and remember
                                alreadyDisplayed.add(icon);
                                custFilterOptions.addView(getCustomFilter(text, icon, co.showByDefault));
                            }
                        }
                    }
                }
            }
            catch(RemovedException e){}
        }


        //for all objects the user manually filtered, check to see if they exist on the map anymore. if they do not, remove them from the manual filter list
        Iterator<String> filterIt = manualFiltered.iterator();
        while(filterIt.hasNext()){
            String usedToFilter = filterIt.next();
            if(!alreadyDisplayed.contains(usedToFilter)){
                filterIt.remove();
                filterThese.remove(usedToFilter);
            }
        }
    }

    public void updatePresetFilters(){
        //set up all preset filters based on new turn player's actions
        LinearLayout presetFilterOptions = (LinearLayout) findViewById(R.id.presetFilterOptions);
        presetFilterOptions.removeAllViews();
        for(Action a: curUser.actions){
            if(a instanceof Climb){
                presetFilterOptions.addView(getPresetFilter("Climber  View"));
            }
        }
        presetFilter = "";
        presetFilterSelected = null;


        //also reset preset filters
        canClimb = new HashMap<>();
    }




    public void updateUserStatus(){

        //stats first
        LinearLayout statView = (LinearLayout) findViewById(R.id.statsView);
        statView.removeAllViews();

        TextView stamina = getElement(Html.fromHtml("Stamina: &#160;"+curUser.getStat(Stat.CUR_STAMINA)+"/"+curUser.getStat(Stat.MAX_STAMINA)).toString(), ElementT.NORMAL);
        stamina.setTextColor(getResources().getColor(R.color.stamina_green));
        statView.addView(stamina);

        TextView mana = getElement(Html.fromHtml("Mana: &#160;"+curUser.getStat(Stat.CUR_MANA)+"/"+curUser.getStat(Stat.MAX_MANA)).toString(), ElementT.NORMAL);
        mana.setTextColor(getResources().getColor(R.color.mana_purple));
        statView.addView(mana);

        TextView focus = getElement(Html.fromHtml("Focus: &#160;"+curUser.getStat(Stat.CUR_FOCUS)+"/"+curUser.getStat(Stat.MAX_FOCUS)).toString(), ElementT.NORMAL);
        focus.setTextColor(getResources().getColor(R.color.focus_blue));
        statView.addView(focus);


        //then classes
        LinearLayout classView = (LinearLayout) findViewById(R.id.classesView);
        classView.removeAllViews();

        classView.addView(getElement(Html.fromHtml("Elemental: &#160;" + curUser.getStat(Stat.ELEMENTAL)).toString(), ElementT.SUB));

        classView.addView(getElement(Html.fromHtml("Warrior: &#160;"+curUser.getStat(Stat.WARRIOR)).toString(), ElementT.SUB));

        classView.addView(getElement(Html.fromHtml("Ninja: &#160;"+curUser.getStat(Stat.NINJA)).toString(), ElementT.SUB));

        classView.addView(getElement(Html.fromHtml("Marksman: &#160;"+curUser.getStat(Stat.MARKSMAN)).toString(), ElementT.SUB));

        classView.addView(getElement(Html.fromHtml("Engineer: &#160;"+curUser.getStat(Stat.ENGINEER)).toString(), ElementT.SUB));

        classView.addView(getElement(Html.fromHtml("Acrobat: &#160;"+curUser.getStat(Stat.ACROBAT)).toString(), ElementT.SUB));

        classView.addView(getElement(Html.fromHtml("Mad Doctor: &#160;"+curUser.getStat(Stat.MAD_DOCTOR)).toString(), ElementT.SUB));

        classView.addView(getElement(Html.fromHtml("Magician: &#160;"+curUser.getStat(Stat.MAGICIAN)).toString(), ElementT.SUB));

        //TODO items!!!!!!!!!!!!!!
    }



    public void updateTurnStatus() {
        GameManager gm = GameManager.getInstance();
        State s = gm.getState();

        //update time
        ((TextView) findViewById(R.id.timeText)).setText((s.getTime() / 1000.0) + " seconds");

        //now update turn narration
        LinearLayout curTurnNar = (LinearLayout) findViewById(R.id.curTurnNarrate);
        curTurnNar.removeAllViews();


        if(s.onePlayer) {
            //we are in multiplayer, show narration relevant to the level
            s.level.getNarration(curTurnNar);
        }

        else {
            //we are in multiplayer, show narration of not-yet-solidified events

            ArrayList<Narration> canViewThese = new ArrayList<>();
            for (Narration n : s.getNarrationNew()) {
                boolean canSee = false;
                for (Integer id : n.getUsersSee()) {
                    if (id == curUser.id)
                        canSee = true;
                }
                if (canSee) {
                    canViewThese.add(n);
                }
            }

            LinearLayout nar = getNarrationView(false);
            fillNarrationView(nar, canViewThese);
            curTurnNar.addView(nar);

            ((ScrollView) findViewById(R.id.curTurnNarScroller)).fullScroll(View.FOCUS_DOWN);
        }
    }

    public void updateAlerts() {
        defaultButtonDrawableOverview = getResources().getDrawable(R.drawable.smudge);
        defaultButtonNarrationAlerts =  getResources().getDrawable(R.drawable.eye);
        defaultTextColorAlertBut =  getResources().getColor(R.color.black_text_button);

        RecyclerView alertsRecycler = (RecyclerView) findViewById(R.id.alerts);
        LinearLayoutManager narrationLayout = new LinearLayoutManager(this);
        narrationLayout.setOrientation(LinearLayoutManager.VERTICAL);
        alertsRecycler.setLayoutManager(narrationLayout);
        alertsRecycler.setAdapter(new AlertAdapter());

        for(Button curViewing: highlightedNav){
            Button narrateBut = (Button) findViewById(R.id.narrateBut);
            if(curViewing != narrateBut){
                narrateBut.setBackground(defaultButtonDrawableOverview);
            }
        }

        for(Button curViewingNarration: highlightedOverview){
            Button alertsBut = (Button) findViewById(R.id.alertsBut);
            if(curViewingNarration != alertsBut){
                alertsBut.setBackground(defaultButtonNarrationAlerts);
                alertsBut.setTextColor(defaultTextColorAlertBut);
            }
        }
    }


    public void updateActionAlert() {
        LinearLayout actionAlertLayout = (LinearLayout) findViewById(R.id.actionAlert);
        actionAlertLayout.removeAllViews();
        Alert curAlert = GameManager.getInstance().getTimeline().curActionAlert;
        if(curAlert != null) {
            LinearLayout curAlertLayout = getNarrationView(false);
            fillNarrationView(curAlertLayout, curAlert);
            actionAlertLayout.addView(curAlertLayout);
        }
    }



    public void centerView(){
        //scroll to new turn player
        State s = GameManager.getInstance().getState();
        int size = s.getSize();

        //make sure the screen wont try looking off the bottom or right edge
        Coord middle = curUser.getMiddlemostCoord();
        int Ymod =  middle.y;
        int Xmod = middle.x;

        //if we are to far right or down, center on the most right or down we can go. note this sometimes causes the map to overshoot and "wrap" by 1 column on the right, because of how large the screen is, but it is still better than not centering right enough
        if(Xmod > size-6)
            Xmod = size-6;
        if(Ymod > size-5)
            Ymod = size-5;

        //offset the y position to go up by 3 rows, trying to center the user.
        Ymod = Ymod - 3;
        Ymod = Math.max(Ymod, 0);

        //offset the x position to go left 4 columns, trying to center the user
        Xmod = Xmod - 4;
        Xmod = Math.max(Xmod, 0);

        final int position =  s.coordToPosition(new Coord(Xmod, Ymod));


        gameMap.post( new Runnable() {
            @Override
            public void run() {
                gameMap.getLayoutManager().scrollToPosition(position);
                noUpdateMap = false;
                newVision();
                updateCoords();
            }
        });
    }

    public void updateTime(){
        State s = GameManager.getInstance().getState();
        int time = s.getTime()%3000;
        ((ProgressBar) findViewById(R.id.timeBar)).setProgress(time);
    }










    /**************************************************** Save stuff ***************************************************/


    @Override
    protected void onDestroy(){
        GameManager gm = GameManager.getInstance();
        gm.clearManager();
        super.onDestroy();
    }

    @Override
    protected void onPause (){
        if(gameOver == 0) {
            GameManager gm = GameManager.getInstance();
            gm.saveGame();
        }
        super.onPause();
    }










    /**************************************************** Misc Stuff *******************************************************/
    public enum ElementT {
        NORMAL, TITLE, SUB
    }

    public void updateCoords(){
        if(!noUpdateMap) {
            gameMap.getAdapter().notifyDataSetChanged();
        }
    }

    public void takeFoc() {
        View view = findViewById(R.id.takeFoc);
        view.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent ev){
        if(dispatchTouch == null)
            return super.dispatchTouchEvent(ev);
        else
            return dispatchTouch.call();
    }

    public void updateCoords(Set<Coord> these, boolean highlighting){
        if(!noUpdateMap) {
            State s = GameManager.getInstance().getState();
            for (Coord c : these) {
                int position = s.coordToPosition(c);
                RecyclerView.ViewHolder vh = gameMap.findViewHolderForAdapterPosition(position);
                if(vh != null) {
                    StateAdapter sa = (StateAdapter) gameMap.getAdapter();
                    sa.isHighlighting = true;
                    sa.bindViewHolder((CoordHolder) vh, position);
                    sa.isHighlighting = false;
                }
            }
        }
    }


    public void findCurUser(View v){
        setObjects(curUser);
        showMapInfo(v);
    }













    /**************************************************** Change View Stuff *******************************************************/


    private void crossFadeViews(final View fadeIn, final View fadeOut, final Runnable afterFade){
        //set up fade in so it can be faded in
        fadeIn.setAlpha(0f);
        fadeIn.setVisibility(View.VISIBLE);
        //begin fading out
        fadeOut.animate()
                .alpha(0f)
                .setDuration(200)
                //finish fading out
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        fadeOut.setVisibility(View.GONE);
                    }
                });

        //begin fading in
        fadeIn.animate()
                .alpha(1f)
                .setDuration(200)
                .withEndAction(afterFade);
    }


    private void changeCurInfoView(View newView, final Button newBut, final Callable afterFade){
        newBut.setBackground(selectedButtonDrawable);
        newBut.setTextColor(selectedButtonText);
        highlightedNav.add(newBut);

        Runnable callMe = new Runnable() {
            @Override
            public void run() {
                Iterator<Button> iterator = highlightedNav.iterator();

                while (iterator.hasNext()) {
                    Button curViewing = iterator.next();

                    if(curViewing != newBut) {

                        Button narrateBut = (Button) findViewById(R.id.narrateBut);

                        if (curViewing == narrateBut) {
                            curViewing.setBackground(defaultButtonDrawableOverview);

                        } else {
                            curViewing.setBackground(defaultButtonDrawable);
                        }

                        curViewing.setTextColor(defaultButtonText);
                        iterator.remove();
                    }

                }

                newBut.setBackground(selectedButtonDrawable);
                newBut.setTextColor(selectedButtonText);
                highlightedNav.add(newBut);

                afterFade.call();
                updateAllHighlighted();
            }
        };


        if ((curInfoView != null) && (curInfoView != newView)) {
            crossFadeViews(newView, curInfoView, callMe);
        }

        else {
            newView.setVisibility(View.VISIBLE);
            callMe.run();
        }
    }


    private void changeNarrateInfoView(View newView, final Button newBut, final Callable afterFade){
        newBut.setBackground(selectedButtonNarration);
        newBut.setTextColor(selectedNarrationButtonText);
        highlightedOverview.add(newBut);

        Runnable callMe = new Runnable() {
            @Override
            public void run() {
                Iterator<Button> iterator = highlightedOverview.iterator();

                while (iterator.hasNext()) {
                    Button curViewingNarration = iterator.next();

                    if (curViewingNarration != newBut) {
                        Button alertsBut = (Button) findViewById(R.id.alertsBut);
                        if (curViewingNarration == alertsBut) {
                            curViewingNarration.setBackground(defaultButtonNarrationAlerts);
                            curViewingNarration.setTextColor(defaultTextColorAlertBut);
                        } else {
                            curViewingNarration.setBackground(defaultButtonNarration);
                            curViewingNarration.setTextColor(defaultNarrationButtonText);
                        }

                        iterator.remove();
                    }
                }

                newBut.setBackground(selectedButtonNarration);
                newBut.setTextColor(selectedNarrationButtonText);
                highlightedOverview.add(newBut);

                afterFade.call();
                updateAllHighlighted();
            }
        };



        if ((curNarrateView != null) && (curNarrateView != newView)) {
            crossFadeViews(newView, curNarrateView, callMe);
        }

        else {
            newView.setVisibility(View.VISIBLE);
            callMe.run();
        }

    }




    public void showNarration(View v) {
        takeFoc();

        Button newBut = (Button) findViewById(R.id.narrateBut);
        LinearLayout newView = (LinearLayout) findViewById(R.id.narrateView);
        changeCurInfoView(newView, newBut, new EmptyCallable());
        curInfoView = newView;
    }


    public void showTurnStatus(View v) {

        Button newBut = (Button) findViewById(R.id.turnStatusBut);
        LinearLayout newView = (LinearLayout) findViewById(R.id.turnStatusView);
        changeNarrateInfoView(newView, newBut, new EmptyCallable());
        curNarrateView = newView;
    }

    public void showHistory(View v) {

        Button newBut = (Button) findViewById(R.id.historyBut);
        LinearLayout newView = (LinearLayout) findViewById(R.id.historyView);
        changeNarrateInfoView(newView, newBut, new EmptyCallable());
        curNarrateView = newView;
    }

    public void showUserStatus(View v) {

        Button newBut = (Button) findViewById(R.id.userStatusBut);
        LinearLayout newView = (LinearLayout) findViewById(R.id.userStatusView);
        changeNarrateInfoView(newView, newBut, new EmptyCallable());
        curNarrateView = newView;
    }

    public void showAlerts(View v) {

        Button newBut = (Button) findViewById(R.id.alertsBut);
        LinearLayout newView = (LinearLayout) findViewById(R.id.alertsView);
        changeNarrateInfoView(newView, newBut, new EmptyCallable());
        curNarrateView = newView;
    }




    public void showMapInfo(View v) {
        takeFoc();

        Button newBut = (Button) findViewById(R.id.mapInfoBut);
        LinearLayout newView = (LinearLayout) findViewById(R.id.mapInfo);
        changeCurInfoView(newView, newBut, new EmptyCallable());
        curInfoView = newView;

    }

    public void showMapFilter(View v) {
        takeFoc();

        Button newBut = (Button) findViewById(R.id.mapFilterBut);
        LinearLayout newView = (LinearLayout) findViewById(R.id.mapFilter);
        changeCurInfoView(newView, newBut, new EmptyCallable());
        curInfoView = newView;
    }

    public void showActions(View v) {
        if(GameManager.getInstance().getTimeline().pausedAction){
            showActionAlert();
        }
        else{
            takeFoc();

            Button newBut = (Button) findViewById(R.id.actionsBut);
            LinearLayout newView = (LinearLayout) findViewById(R.id.actionMenuHolder);
            changeCurInfoView(newView, newBut, new EmptyCallable());
            curInfoView = newView;
        }
    }



    public void showActionAlert(){
        takeFoc();

        Button newBut = (Button) findViewById(R.id.actionsBut);
        LinearLayout newView = (LinearLayout) findViewById(R.id.actionAlert);
        changeCurInfoView(newView, newBut, new EmptyCallable());
        curInfoView = newView;
    }


    public void customFilters(View v) {
        customFilter = true;

        Button customBut = (Button) findViewById(R.id.customFiltersBut);
        customBut.setBackground(getResources().getDrawable(R.drawable.hair_flipped_inverted));
        customBut.setTextColor(getResources().getColor(R.color.full_black));

        Runnable callMe = new Runnable() {
            @Override
            public void run() {
                Button customBut = (Button) findViewById(R.id.customFiltersBut);
                customBut.setBackground(getResources().getDrawable(R.drawable.hair_flipped_inverted));
                customBut.setTextColor(getResources().getColor(R.color.full_black));
                Button presetBut = (Button) findViewById(R.id.presetFiltersBut);
                presetBut.setBackground(getResources().getDrawable(R.drawable.hair));
                presetBut.setTextColor(getResources().getColor(R.color.full_white));

                updateCoords();
            }
        };

        crossFadeViews(findViewById(R.id.viewCustomFilters), findViewById(R.id.viewPresetFilters), callMe);


    }

    public void presetFilters(View v) {
        customFilter = false;

        Button presetBut = (Button) findViewById(R.id.presetFiltersBut);
        presetBut.setBackground(getResources().getDrawable(R.drawable.hair_inverted));
        presetBut.setTextColor(getResources().getColor(R.color.full_black));

        Runnable callMe = new Runnable() {
            @Override
            public void run() {
                Button presetBut = (Button) findViewById(R.id.presetFiltersBut);
                presetBut.setBackground(getResources().getDrawable(R.drawable.hair_inverted));
                presetBut.setTextColor(getResources().getColor(R.color.full_black));
                Button customBut = (Button) findViewById(R.id.customFiltersBut);
                customBut.setBackground(getResources().getDrawable(R.drawable.hair_flipped));
                customBut.setTextColor(getResources().getColor(R.color.full_white));

                updateCoords();
            }
        };

        crossFadeViews(findViewById(R.id.viewPresetFilters), findViewById(R.id.viewCustomFilters), callMe);
    }


    public void clearInfo(){
        unHighlightCoords(curHighlighted);
        curHighlighted.clear();
        curSelected = null;
        curOpened = null;
        defaultHighlight.clear();
        ((HorizontalScrollView) findViewById(R.id.mapInfoInnerScroll)).removeAllViews();
    }


    public void updateInfo(){
        if(!mapInfoHistory.empty()) {
            mapInfoHistory.push(new Object());
            infoBack(null);
        }
    }




    public void showActInput(){
        if(GameManager.getInstance().getTimeline().pausedAction){
            showActionAlert();
        }

        else {
            final Runnable callMeAfter = new Runnable() {
                @Override
                public void run() {
                }
            };

            if(!selectingActionInput) {
                View inputs = findViewById(R.id.actionsInput);
                View actions = findViewById(R.id.actionsView);

                crossFadeViews(inputs, actions, callMeAfter);

                selectingActionInput = true;
            }

            Button newBut = (Button) findViewById(R.id.actionsBut);
            LinearLayout newView = (LinearLayout) findViewById(R.id.actionMenuHolder);
            changeCurInfoView(newView, newBut, new EmptyCallable());
            curInfoView = newView;

        }
    }



    public void showActView(final boolean b) {
        takeFoc();

        final Runnable callMeAfter = new Runnable() {
            @Override
            public void run() {
                if(GameManager.getInstance().getTimeline().pausedAction){
                    showActionAlert();
                }
            }
        };

        if(selectingActionInput) {
            View inputs = findViewById(R.id.actionsInput);
            View actions = findViewById(R.id.actionsView);

            crossFadeViews(actions, inputs, callMeAfter);

            selectingActionInput = false;
        }

        Button newBut = (Button) findViewById(R.id.actionsBut);
        LinearLayout newView = (LinearLayout) findViewById(R.id.actionMenuHolder);
        changeCurInfoView(newView, newBut, new EmptyCallable());
        curInfoView = newView;
    }
















    /**************************************************** Map Info Stuff *******************************************************/

    public void infoBack(View v){

        if(!mapInfoHistory.empty()) {
            //offset the push from the last call we just made
            Object last = mapInfoHistory.pop();

            if(!mapInfoHistory.empty()) {

                Object o = mapInfoHistory.pop();

                if (o instanceof Narration)
                    setObjects((Narration) o);

                else if (o instanceof Obj)
                    setObjects((Obj) o);

                else if (o instanceof Coord)
                    setObjects((Coord) o);
            }

            else
                mapInfoHistory.push(last);
        }
    }


    public void setObjects(Narration n){
        inPOBJ = false;
        mapInfoHistory.push(n);
        unHighlightCoords(curHighlighted);
        defaultHighlight.clear();

        HashSet<Obj> objs = new HashSet<Obj>();
        for(Obj o : n.getInvolves()) {
            if(isVisible(o)) {
                objs.add(o);
            }

        }

        viewGrid gridBuilder = new viewGrid(objs, false, false);
        gridBuilder.addToView();
    }


    public void setObjects(Coord c){
        inPOBJ = false;
        mapInfoHistory.push(c);
        HashSet<Coord> coords = new HashSet<Coord>();
        coords.add(c);
        highlightCoords(coords);
        defaultHighlight.clear();
        defaultHighlight.add(c);

        TreeSet<CObj> Cobjs = GameManager.getInstance().getState().getObjC(c);
        HashSet<CObj> objs = new HashSet<CObj>();
        for(CObj co : Cobjs) {
            if(isVisible(co, c)) {
                objs.add(co);
            }
        }
        viewGrid gridBuilder = new viewGrid(objs, c, false, false);
        gridBuilder.addToView();
    }


    public void setObjects(final Obj po){
        inPOBJ = true;
        mapInfoHistory.push(po);
        HashSet<Coord> coords = getHighlightableCoords(po);
        highlightCoords(coords);
        defaultHighlight.clear();
        defaultHighlight.addAll(coords);

        HashSet<Obj> objs = new HashSet<Obj>();

        if(po instanceof PObj) {
            PObj poParent = (PObj) po;
            ArrayList<IntObj> Cobjs = poParent.getChildren();

            for (IntObj co : Cobjs) {
                Obj o = co.o;
                if (isVisible(o)) {
                    objs.add(o);
                }
            }

        }
        else if (isVisible(po)){
            objs.add(po);
        }


        viewGrid gridBuilder = new viewGrid(objs, false, false);
        gridBuilder.addToView();
    }


    //this allows for diagnal scrolling
    private float mx, my;
    private int scrollBy = 0;
    private boolean scrollDiagnalGrid(MotionEvent motionEvent, boolean action) {
        float curX, curY;

        ScrollView vScroll;
        HorizontalScrollView hScroll;
        if(action){
            vScroll = (VScroll) findViewById(R.id.actionsInOuterScroll);
            hScroll = (HScroll) findViewById(R.id.actionsInInnerScroll);
        }
        else{
            vScroll = (VScroll) findViewById(R.id.mapInfoOuterScroll);
            hScroll = (HScroll) findViewById(R.id.mapInfoInnerScroll);
        }

        View gridView = hScroll.getChildAt(0);

        switch (motionEvent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mx = motionEvent.getX();
                my = motionEvent.getY();
                scrollBy = 0;
                return true;
            case MotionEvent.ACTION_MOVE:
                curX = motionEvent.getX();
                curY = motionEvent.getY();
                float newX = mx-curX;
                float newY = my-curY;
                vScroll.scrollBy((int) newX, (int) newY);
                hScroll.scrollBy((int) newX, (int) newY);
                scrollBy += Math.max(Math.abs(newX), Math.abs(newY));
                mx = curX;
                my = curY;
                return true;
            case MotionEvent.ACTION_UP:
                curX = motionEvent.getX();
                curY = motionEvent.getY();
                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));

                //this is needed to process click events and send them to the proper element, since otherwise they would be eaten by the scroller
                if(gridView != null) {
                    if(scrollBy < 20) {
                        float xOffset = Math.max(hScroll.getScrollX(), 0);
                        float yOffset = Math.max(vScroll.getScrollY(), 0);

                        MotionEvent firstDown = MotionEvent.obtain(motionEvent);
                        firstDown.setAction(MotionEvent.ACTION_DOWN);
                        firstDown.offsetLocation(xOffset, yOffset);

                        MotionEvent thenUp = MotionEvent.obtain(motionEvent);
                        thenUp.offsetLocation(xOffset, yOffset);

                        gridView.dispatchTouchEvent(firstDown);
                        gridView.dispatchTouchEvent(thenUp);
                    }
                }
                return true;
        }

        return false;


    }
/*
    //this should only be called from setObjects
    //onclick will expand all of the objects below it and change the font color of branch(10)
    //also when you click something it will drop down "see parent" and "see children" options (these should be renamed)
    private void setObjects(HashMap<String, HashSet<Obj>> objsMap, Obj container){
        //LinearLayout objV = (LinearLayout) findViewById(R.id.objectView);

        if(container != null) {
            final PObj parent = container.parent;

            if (parent != null) {
                TextView parentTV = getElement("Part  of  " + parent.name, ElementT.NORMAL);
                parentTV.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        setObjects(parent);
                    }
                });
                //objV.addView(parentTV);
            }
        }

        final int black = getResources().getColor(R.color.full_black);
        int dp25 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());
        int fill = LinearLayout.LayoutParams.MATCH_PARENT;
        int wrap_content = LinearLayout.LayoutParams.WRAP_CONTENT;
        final int white = getResources().getColor(R.color.full_white);
        final int grey = getResources().getColor(R.color.grey);
        final LinearLayout.LayoutParams listClosed = new LinearLayout.LayoutParams(fill, 0);
        final LinearLayout.LayoutParams listOpened = new LinearLayout.LayoutParams(fill, wrap_content);

        //get all objects/objectlists in objsMap
        Set<String> keyset = objsMap.keySet();
        for(String s: keyset){
            final HashSet<Obj> objs = objsMap.get(s);


            //multiple objects with the same name
            if(objs.size() > 1){
                //create list where copies will show, dont add it yet
                final LinearLayout listCopies = new LinearLayout(this);
                listCopies.setOrientation(LinearLayout.VERTICAL);
                listCopies.setPadding(dp25, 0, 0, 0);

                //create list name and a clicker to change appearence of itself and listCopies
                final TextView listName = getElement(s + " (" + objs.size() + ")", ElementT.NORMAL);
                listName.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (listName.getCurrentTextColor() == grey) {
                            listName.setTextColor(black);
                            listCopies.setLayoutParams(listClosed);
                        } else {
                            listName.setTextColor(grey);
                            listCopies.setLayoutParams(listOpened);
                        }
                    }
                });
                //add the list name to the screen
                //objV.addView(listName);

                for(final Obj o: objs) {
                    final LinearLayout listRelatives = new LinearLayout(this);
                    final boolean hasRelatives;
                    final PObj elParent = o.parent;
                    boolean hasParent = elParent != container;
                    boolean hasChildren = o.isParent();
                    if (hasParent || hasChildren) {
                        hasRelatives = true;
                        listRelatives.setOrientation(LinearLayout.VERTICAL);
                        listRelatives.setPadding(dp25, 0, 0, 0);
                        if (hasParent) {
                            TextView parentTV = getElement("Part  of  " + elParent.name, ElementT.SUB);
                            parentTV.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    setObjects(elParent);
                                }
                            });
                            listRelatives.addView(parentTV);
                        }
                        if (hasChildren) {
                            TextView childrenTV = getElement("Has  parts", ElementT.SUB);
                            childrenTV.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    setObjects((PObj) o);
                                }
                            });
                            listRelatives.addView(childrenTV);
                        }
                    } else {
                        hasRelatives = false;
                    }
                    final TextView aCopy = getElement(s, ElementT.NORMAL);
                    aCopy.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (curSelected == aCopy) {
                                aCopy.setTextColor(black);
                                //((LinearLayout) findViewById(R.id.typeView)).removeAllViews();
                                curSelected = null;
                                if (hasRelatives) {
                                    listRelatives.setLayoutParams(listClosed);
                                }
                                highlightCoords(defaultHighlight);
                            } else {
                                aCopy.setTextColor(white);
                                if (curSelected != null)
                                    curSelected.setTextColor(black);
                                if (curOpened != null)
                                    curOpened.setLayoutParams(listClosed);
                                curSelected = aCopy;
                                setTypes(o);
                                if (hasRelatives) {
                                    listRelatives.setLayoutParams(listOpened);
                                    curOpened = listRelatives;
                                }
                                highlightCoords(getHighlightableCoords(o));
                            }

                        }
                    });
                    listCopies.addView(aCopy);
                    if (hasRelatives) {
                        listCopies.addView(listRelatives, listClosed);
                    }

                }
                //add the list of copies of objects to the screen, by default not appearing until opened
               // objV.addView(listCopies, listClosed);

            }


            else { //no copies
                final Obj o = objs.iterator().next();

                final LinearLayout listRelatives = new LinearLayout(this);
                final boolean hasRelatives;
                final PObj elParent = o.parent;
                boolean hasParent = elParent != container;
                boolean hasChildren = o.isParent();

                if (hasParent || hasChildren) {
                    hasRelatives = true;
                    listRelatives.setOrientation(LinearLayout.VERTICAL);
                    listRelatives.setPadding(dp25, 0, 0, 0);
                    if (hasParent) {
                        TextView parentTV = getElement("Part  of  " + elParent.name, ElementT.SUB);
                        parentTV.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                setObjects(elParent);
                            }
                        });
                        listRelatives.addView(parentTV);
                    }
                    if (hasChildren) {
                        TextView childrenTV = getElement("Has  parts", ElementT.SUB);
                        childrenTV.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                setObjects((PObj) o);
                            }
                        });
                        listRelatives.addView(childrenTV);
                    }
                } else {
                    hasRelatives = false;
                }

                final TextView element = getElement(o.name, ElementT.NORMAL);
                element.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (curSelected == element) {
                            element.setTextColor(black);
                            curSelected = null;
                            //((LinearLayout) findViewById(R.id.typeView)).removeAllViews();
                            if (hasRelatives) {
                                listRelatives.setLayoutParams(listClosed);
                            }
                            highlightCoords(defaultHighlight);
                        } else {
                            element.setTextColor(white);
                            if (curSelected != null)
                                curSelected.setTextColor(black);
                            curSelected = element;
                            setTypes(o);
                            if (curOpened != null)
                                curOpened.setLayoutParams(listClosed);
                            if (hasRelatives) {
                                listRelatives.setLayoutParams(listOpened);
                                curOpened = listRelatives;
                            }
                            highlightCoords(getHighlightableCoords(o));
                        }

                    }
                });

                //objV.addView(element);
                //if (hasRelatives)
                    //objV.addView(listRelatives, listClosed);


            }

        }
        //out of keyset loop

    }



    public void setTypes(Obj obj){
        State state = GameManager.getInstance().getState();
        //LinearLayout typesV = (LinearLayout) findViewById(R.id.typeView);
        //typesV.removeAllViews();
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");

        int sp10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics());
        int dp8 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        ArrayList<String> types = obj.getDescription();

        for(String s: types){
            final TextView tV = new TextView(this);
            tV.setText(Html.fromHtml(s));
            tV.setTypeface(font);
            tV.setTextSize(sp10);
            tV.setPadding(0, 0, 0, dp8);
            //typesV.addView(tV);
        }

        Integer standingOn = obj.standingOn;
        if(standingOn != null){
            try {
                final Obj onThis = state.getObjID(standingOn);
                if(isVisible(onThis)) {
                    final TextView tV = new TextView(this);
                    tV.setText("On  " + onThis.name);
                    tV.setTypeface(font);
                    tV.setTextSize(sp10);
                    tV.setPadding(0, 0, 0, dp8);
                    tV.setTextColor(getResources().getColor(R.color.black_text_button));
                    tV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setObjects(onThis);
                        }
                    });
                    //typesV.addView(tV);
                }

            }
            catch(RemovedException e) {
                Log.e("Object standing on was removed from state when trying to display details", "setTypes");
            }
        }

        HashSet<Integer> supporting = obj.supportingThese;
        for(Integer i: supporting) {
            try {
                final Obj supThis = state.getObjID(i);
                if (isVisible(supThis)) {
                    final TextView tV = new TextView(this);
                    tV.setText("Supporting  " + supThis.name);
                    tV.setTypeface(font);
                    tV.setTextSize(sp10);
                    tV.setPadding(0, 0, 0, dp8);
                    tV.setTextColor(getResources().getColor(R.color.black_text_button));
                    tV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setObjects(supThis);
                        }
                    });
                    //typesV.addView(tV);
                }
            }

            catch(RemovedException e) {
                Log.e("Object supporting on was removed from state when trying to display details", "setTypes");
            }


        }


    }*/

    public TextView getElement(String name, ElementT type){
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        int black = getResources().getColor(R.color.full_black);
        int padding;
        if(type == ElementT.NORMAL)
            padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        else
            padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());

        int textSize;
        if(type == ElementT.TITLE)
            textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());
        else if(type == ElementT.NORMAL)
            textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics());
        else //sub case
            textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8, getResources().getDisplayMetrics());

        TextView element = new TextView(this);
        element.setText(name);
        element.setTextColor(black);
        element.setTypeface(font);
        element.setTextSize(textSize);

        if(type == ElementT.TITLE)
            element.setPadding(0, 0, 0, padding);
        else
            element.setPadding(0, padding, 0, 0);



        return element;

    }














    /*********************************************** Visibility stuff ***********************************************/

    public boolean isVisible(CObj o, Coord c){
        ArrayList<Coord> sees = getVision(o.id);
        for(Coord cur: sees){
            if(cur.x == c.x && cur.y == c.y)
                return true;
        }

        return false;
    }

    public boolean isVisible(Obj o){
        if(o.isParent()){
            for(CObj co: o.getAllLeafs()){
                if(isVisible(co))
                    return true;
            }
        }
        else {
            ArrayList<Coord> sees = getVision(o.id);
            if (sees.size() > 0)
                return true;

        }
        return false;
    }

    public ArrayList<Coord> getVision(int oid){
        ArrayList<Coord> returnMe = visionCur.get(oid);

        //we already looked up vision of this obj, return it
        if(returnMe != null){
            return returnMe;
        }

        //if we haven't already looked up vision, look it up on the user
        //we do no update for concurrency reasons i think
        else{
            returnMe = curUser.getVisionNoUpdate(oid);

            //record it for later
            visionCur.put(oid, returnMe);

            //if we haven't already, see if vision has changed since visionPast
            if(!visionChanged){
                //check to see if returnMe is different now then it was then
                ArrayList<Coord> pastReturnMe = visionPast.get(oid);

                //we never looked it up before, assume this means it changed
                if(pastReturnMe == null){
                    visionChanged = true;
                    return returnMe;
                }
                else{
                    //check the coords we see now
                    for(Coord newSee: returnMe) {
                        //if we find one that we hadnt seen before, this means it changed
                        if (!pastReturnMe.contains(newSee)) {
                            visionChanged = true;
                            return returnMe;
                        }
                    }

                    //we've seen all these coords before! check that the lengths are equal so that we know we haven't lost vision of any
                    if(pastReturnMe.size() != returnMe.size()){
                        visionChanged = true;
                        return returnMe;
                    }
                }

            }

            //we can now return this to the user
            return returnMe;
        }
    }


    //returns the color that cobj's space should be, or -1 if cobj has no color
    public int getColor(CObj cobj){
        Integer returnMe = colorCur.get(cobj.id);

        //we already looked up color of this obj, return it
        if(returnMe != null){
            return returnMe;
        }

        //if we haven't already looked up color, look it up
        else {
            int finalColor = -1;

            //get the color based on the objT
            for (ObjT ot : cobj.getTypeSelf()) {
                int color = ot.getBGColor();
                //this makes the last found color be the one we keep
                if (color != -1) {
                    finalColor = color;
                }
            }

            colorCur.put(cobj.id, finalColor);

            //if we haven't already, see if color has changed since colorPast
            if (!visionChanged) {
                //check to see if returnMe is different now then it was then
                Integer oldColor = colorPast.get(cobj.id);

                //we never looked it up before, assume this means it changed
                if (oldColor == null) {
                    visionChanged = true;
                } else {
                    //compare new to old
                    if (oldColor != finalColor) {
                        visionChanged = true;
                    }
                }
            }

            return finalColor;
        }
    }

    public String getIcon(CObj cobj, Coord c){
        HashMap<Coord, String> innerHashmap = iconCur.get(cobj.id);

        //we already looked up icon of this obj, return it
        if(innerHashmap != null){
            String returnMe = innerHashmap.get(c);
            if(returnMe != null) {
                return returnMe;
            }
        }
        //make sure we have something to put the icon into
        else{
            innerHashmap = new HashMap<>();
        }

        //if we haven't already looked up icon, look it up
        String newIcon = cobj.getIcon(c);

        innerHashmap.put(c, newIcon);
        iconCur.put(cobj.id, innerHashmap);

        //if we haven't already, see if icon has changed since iconPast
        if (!visionChanged) {
            //check to see if returnMe is different now then it was then
            HashMap<Coord, String> innerHashmapOld = iconPast.get(cobj.id);

            //we never looked it up before, assume this means it changed
            if(innerHashmapOld == null){
                visionChanged = true;
            }
            else{
                String oldIcon = innerHashmap.get(c);
                //we never looked it up before, assume this means it changed
                if(oldIcon == null) {
                    visionChanged = true;
                }
                else {
                    //compare new to old
                    if (newIcon.compareTo(oldIcon) != 0) {
                        visionChanged = true;
                    }
                }
            }
        }

        return newIcon;

    }

    public void newVision(){
        visionChanged = false;
        visionPast = visionCur;
        visionCur = new HashMap<>();
        iconPast = iconCur;
        iconCur = new HashMap<>();
        colorPast = colorCur;
        colorCur = new HashMap<>();
        State s = GameManager.getInstance().getState();

        //get the top left coord being displayed
        Coord topLeft = s.positionToCoord(((FixedGridLayoutManager) gameMap.getLayoutManager()).mFirstVisiblePosition);

        //look at all coords from top left to the bottom right (assuming bottom right is within DEFAULT_SCREEN_BLOCKS distance
        for(int x = 0; x <= Constants.MAP_X; x++){
            for(int y = 0; y <= Constants.MAP_Y; y++) {
                Coord lookOn = new Coord(topLeft.x+x, topLeft.y+y);

                //get vision of all cobj on all those coords
                if(!s.testOffMap(lookOn)) {
                    for (CObj co : s.getObjC(lookOn)) {
                        getVision(co.id);
                        getColor(co);
                        getIcon(co, lookOn);
                    }
                }
            }
        }
    }

    /*********************************************** Highlight stuff *************************************************/

    public void unHighlightCoords(HashSet<Coord> coords) {
        HashSet<Coord> oldCoords = new HashSet<>(coords);
        curHighlighted.removeAll(coords);

        updateCoords(oldCoords, true);
    }

    public void highlightCoords(Set<Coord> coords) {
        unHighlightCoords(curHighlighted);
        curHighlighted.addAll(coords);
        updateCoords(coords, true);
    }

    public void actionUnHighlightCoordsAll() {
        HashSet<Coord> coords = new HashSet<Coord>();
        coords.addAll(actionHighlightedRed);
        coords.addAll(actionHighlightedWhite);
        coords.addAll(actionHighlightedGreen);

        actionHighlightedWhite.clear();
        actionHighlightedGreen.clear();
        actionHighlightedRed.clear();

        updateCoords(coords, true);
    }

    public void actionUnHighlightCoordsWhite(Set<Coord> coords) {
        HashSet<Coord> oldCoords = new HashSet<>(coords);
        actionHighlightedWhite.removeAll(coords);

        updateCoords(oldCoords, true);
    }

    public void actionHighlightCoordsWhite(Set<Coord> coords) {
        actionUnHighlightCoordsWhite(actionHighlightedWhite);
        actionHighlightedWhite.addAll(coords);
        updateCoords(coords, true);
    }

    public void actionUnHighlightCoordsRed(Set<Coord> coords) {
        HashSet<Coord> oldCoords = new HashSet<>(coords);
        actionHighlightedRed.removeAll(coords);

        updateCoords(oldCoords, true);
    }

    public void actionHighlightCoordsRed(Set<Coord> coords) {
        actionUnHighlightCoordsRed(actionHighlightedRed);
        actionHighlightedRed.addAll(coords);
        updateCoords(coords, true);
    }

    public void actionUnHighlightCoordsGreen(Set<Coord> coords) {
        HashSet<Coord> oldCoords = new HashSet<>(coords);
        actionHighlightedGreen.removeAll(coords);

        updateCoords(oldCoords, true);
    }

    public void actionHighlightCoordsGreen(Set<Coord> coords) {
        actionUnHighlightCoordsGreen(actionHighlightedGreen);
        actionHighlightedGreen.addAll(coords);
        updateCoords(coords, true);
    }



    public HashSet<Coord> getHighlightableCoords(Obj o){
        ArrayList<CObj> possible = o.getAllLeafs();
        HashSet<Coord> returnMe = new HashSet<Coord>();
        for(CObj co: possible){
            ArrayList<Coord> allowed = getVision(co.id);
            for (Coord cur : allowed) {
                returnMe.add(cur);
            }
        }
        return returnMe;
    }

    public boolean isHighlighted(Coord c){
        if(processing)
            return false;

        if(curInfoView == findViewById(R.id.mapInfo)){
            for(Coord co: curHighlighted){
                if(co.x == c.x)
                    if(co.y == c.y)
                        return true;
            }
        }
        if((curInfoView == findViewById(R.id.actionMenuHolder) && selectingActionInput)){
            for(Coord co: actionHighlightedWhite){
                if(co.x == c.x)
                    if(co.y == c.y)
                        return true;
            }
        }

        return false;
    }

    public boolean isHighlightedGreen(Coord c){
        if(processing)
            return false;

        if((curInfoView == findViewById(R.id.actionMenuHolder) && selectingActionInput)){
            for(Coord co: actionHighlightedGreen){
                if(co.x == c.x)
                    if(co.y == c.y)
                        return true;
            }
        }

        return false;
    }

    public boolean isHighlightedRed(Coord c){
        if(processing)
            return false;

        if((curInfoView == findViewById(R.id.actionMenuHolder) && selectingActionInput)){
            for(Coord co: actionHighlightedRed){
                if(co.x == c.x)
                    if(co.y == c.y)
                        return true;
            }
        }

        return false;
    }

    public void updateAllHighlighted(){
        HashSet<Coord> allHighlighted = new HashSet<>();
        allHighlighted.addAll(actionHighlightedGreen);
        allHighlighted.addAll(actionHighlightedWhite);
        allHighlighted.addAll(actionHighlightedRed);
        allHighlighted.addAll(curHighlighted);
        updateCoords(allHighlighted, true);
    }

















    /*********************************************** Filter stuff *************************************************/

    public LinearLayout getCustomFilter(final String text, final String icon, boolean defaultShow){
        if(manualFiltered.contains(icon)) {
            defaultShow = false;
        }
        if (manualUnFiltered.contains(icon)) {
            defaultShow = true;
        }

        LinearLayout filter = new LinearLayout(this);
        LinearLayout.LayoutParams filterParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int dp10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        int dp5 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        int dp40 =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        filter.setOrientation(LinearLayout.HORIZONTAL);
        filter.setLayoutParams(filterParams);
        filter.setPadding(0, dp10, 0, 0);


        final RelativeLayout selHold = new RelativeLayout(this);
        LinearLayout.LayoutParams paramsS = new LinearLayout.LayoutParams(dp40, dp40);
        selHold.setPadding(0, 0, 0, dp10);

        final ImageButton select = new ImageButton(this);
        RelativeLayout.LayoutParams paramsIB = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        select.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if(!defaultShow){
            select.setImageResource(R.drawable.empty_checkbox);
            filterThese.add(icon);
        }
        else {
            select.setImageResource(R.drawable.full_checkbox);
        }
        select.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        select.setClickable(false);

        selHold.addView(select, paramsIB);
        filter.addView(selHold, paramsS);

        int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics());
        int black = getResources().getColor(R.color.full_black);

        TextView iconEl = new TextView(this);
        iconEl.setText(icon);
        iconEl.setTextSize(textSize);
        iconEl.setPadding(0,0, dp5,0);
        iconEl.setTextColor(black);

        filter.addView(iconEl);

        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");

        TextView element = new TextView(this);
        element.setText(text);
        element.setTextColor(black);
        element.setTypeface(font);
        element.setTextSize(textSize);

        filter.addView(element);


        filter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (filterThese.contains(icon)) {
                    select.setImageResource(R.drawable.full_checkbox);
                    filterThese.remove(icon);
                    manualUnFiltered.add(icon);
                    manualFiltered.remove(icon);
                    updateCoords();
                    updateInfo();
                } else {
                    select.setImageResource(R.drawable.empty_checkbox);
                    filterThese.add(icon);
                    manualUnFiltered.remove(icon);
                    manualFiltered.add(icon);
                    updateCoords();
                    updateInfo();
                }
            }
        });

        return filter;

    }

    public LinearLayout getPresetFilter(final String text){
        LinearLayout filter = new LinearLayout(this);
        LinearLayout.LayoutParams filterParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int dp10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        int dp40 =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        filter.setOrientation(LinearLayout.HORIZONTAL);
        filter.setLayoutParams(filterParams);
        filter.setPadding(0, dp10, 0, 0);



        final RelativeLayout selHold = new RelativeLayout(this);
        LinearLayout.LayoutParams paramsS = new LinearLayout.LayoutParams(dp40, dp40);
        selHold.setPadding(0, 0, 0, dp10);

        final ImageButton select = new ImageButton(this);
        RelativeLayout.LayoutParams paramsIB = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        select.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if(presetFilter.compareToIgnoreCase(text) == 0)
            select.setImageResource(R.drawable.full_checkbox);
        else
            select.setImageResource(R.drawable.empty_checkbox);

        select.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        select.setClickable(false);

        selHold.addView(select, paramsIB);
        filter.addView(selHold, paramsS);

        int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics());
        int black = getResources().getColor(R.color.full_black);
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");

        TextView element = new TextView(this);
        element.setText(text);
        element.setTextColor(black);
        element.setTypeface(font);
        element.setTextSize(textSize);

        filter.addView(element);


        filter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (presetFilterSelected != select) {
                    if(presetFilterSelected != null){
                        presetFilterSelected.setImageResource(R.drawable.empty_checkbox);
                    }
                    select.setImageResource(R.drawable.full_checkbox);
                    presetFilterSelected = select;
                    presetFilter = text;
                    updateCoords();
                    updateInfo();
                } else {
                    select.setImageResource(R.drawable.empty_checkbox);
                    presetFilterSelected = null;
                    presetFilter = "";
                    updateCoords();
                    updateInfo();
                }
            }
        });

        return filter;

    }


    //c can be null!!!!!!
    public boolean isFiltered(CObj o, Coord c){
        if(customFilter) {
            String icon = o.getIcon();
            if(icon == "")
                return false;

            if (filterThese.contains(icon)) {
                return true;
            }
        }

        else{
            if(curUser.contains(o))
                return false;

            if (presetFilter.compareToIgnoreCase("Climber  View") == 0) {
                if(c!= null) {
                    int zUp = 0;
                    int zDown = 0;
                    for(Action a: curUser.actions){
                        if(a instanceof Climb){
                            Climb climbAct = (Climb) a.getCopy(curUser.id);
                            zUp = climbAct.zMovementUp;
                            zDown = climbAct.zMovementDown;
                        }
                    }

                    HashSet<CObj> coordClimbable = canClimb.get(c);
                    if (coordClimbable == null) {
                        coordClimbable = new HashSet<CObj>();
                        HashSet<ClimbObj> set = new LogicCalc().movementOnCoord(curUser.id, c, zUp, zDown, true, true);
                        for (ClimbObj climbObj : set) {
                            coordClimbable.add(climbObj.co);
                        }

                        canClimb.put(c, coordClimbable);
                    }

                    if (!coordClimbable.isEmpty()) {
                        return !coordClimbable.contains(o);
                    } else
                        return true;
                }
                else{
                    HashSet<Coord> allCoords = o.getAllCoords();
                    for(Coord curC : allCoords){
                        if(!isFiltered(o, curC))
                            return false;
                    }
                    return true;
                }

            }


            //TODO code for each preset filter
        }

        return false;
    }
    public boolean isFiltered(Obj o) {
        for(CObj c: o.getAllLeafs()) {
            if(!isFiltered(c, null))
                return false;
        }
        return true;
    }


    public boolean alreadyDisplayed(String text){
        for(String s : alreadyDisplayed){
            if(s.compareTo(text) == 0)
                return true;
        }

        return false;
    }

















    /*********************************************** Action stuff *************************************************/

    public void actionInfo(final Action a){
        LinearLayout infoView = (LinearLayout) findViewById(R.id.actionsInfo);
        infoView.removeAllViews();
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        Typeface font3 = Typeface.createFromAsset(getAssets(), "MTCORSVA.TTF");

        //add button to use action
        Button useBut = new Button(this);
        useBut.setText("Use");
        useBut.setTypeface(font);
        useBut.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        useBut.setTextColor(getResources().getColorStateList(R.color.white_text_button));
        useBut.setBackground(getResources().getDrawable(R.drawable.brush2_button));
        useBut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startAction(a, false);
            }
        });
        LinearLayout.LayoutParams paramsBut = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
        paramsBut.gravity = Gravity.CENTER_HORIZONTAL;
        if(!(new LogicCalc().canUse(curUser, a)))
            useBut.setEnabled(false);

        infoView.addView(useBut, paramsBut);


        final LinearLayout.LayoutParams openFull = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final LinearLayout.LayoutParams closedPart = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);

        //add text view for action reqs
        TextView reqTitle = new TextView(this);
        reqTitle.setText("Requirements");
        reqTitle.setTypeface(font);
        reqTitle.setTextColor(getResources().getColorStateList(R.color.black_text_button));
        reqTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        final TextView requirements = new TextView(this);
        requirements.setText(Html.fromHtml(a.getReqDesc(curUser)));
        requirements.setTypeface(font);
        requirements.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        reqTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requirements.getLayoutParams() == closedPart)
                    requirements.setLayoutParams(openFull);
                else
                    requirements.setLayoutParams(closedPart);
            }
        });


        infoView.addView(reqTitle, openFull);
        infoView.addView(requirements, closedPart);

        //add text view for action desc
        TextView descTitle = new TextView(this);
        descTitle.setText("Description");
        descTitle.setTypeface(font);
        descTitle.setTextColor(getResources().getColorStateList(R.color.black_text_button));
        descTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        final TextView description = new TextView(this);
        description.setText(Html.fromHtml(a.getDescription()));
        description.setTypeface(font);
        description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        descTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(description.getLayoutParams() == closedPart)
                    description.setLayoutParams(openFull);
                else
                    description.setLayoutParams(closedPart);
            }
        });


        infoView.addView(descTitle, openFull);
        infoView.addView(description, closedPart);

        //add text view for action flavor
        TextView flavor = new TextView(this);
        flavor.setText(Html.fromHtml(a.getFlavor()));
        flavor.setTypeface(font3);
        flavor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        infoView.addView(flavor, openFull);
    }


    public void startAction(final Action a, boolean continuing){
        curAction = a;
        showActInput();

        ((LinearLayout) findViewById(R.id.actInOptions)).removeAllViews();
        ((HorizontalScrollView) findViewById(R.id.actionsInInnerScroll)).removeAllViews();
        ((TextView) findViewById(R.id.actInInfo)).setText("");

        actionUnHighlightCoordsAll();
        if(continuing)
            a.continueUse();
        else
            a.useFront();

    }

    public void mapClicked(Coord c){
        if(!processing) {
            takeFoc();

            if (((curInfoView == findViewById(R.id.actionMenuHolder)) && selectingActionInput) && actionTakesMapClick) {
                curAction.mapClicked(c);
            }

            else {
                if (curInfoView != findViewById(R.id.mapInfo))
                    showMapInfo(null);

                setObjects(c);

            }
        }
    }

    public void actionsReturn(View v){
        takeFoc();
        actionTakesMapClick = false;
        curAction = null;
        showActView(true);
    }

    public void actionInputGridRefresh(){
        curAction.setupGrid();
    }

    public void getActionDefaultHighlight(){
        curAction.defaultHighlight();
    }

    public void zoomAction(boolean zoom){
        curAction.zoomedIn = zoom;
    }









    /***********************************************Alerts ***************************************/


    public void removeAlert(Alert a){
        ignoreAlerts.add(a.objTID);
        updateAlerts();
    }

    public void continueFall(){
        continueFalling = true;
        GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper timeline = gm.getTimeline();

        boolean falling = false;
        for (ObjT types : curUser.getTypePath()) {
            if (types.isFalling())
                falling = true;
        }

        if (falling){
            Coord middle = curUser.getMiddlemostCoord();
            int x = middle.x;
            int y = middle.y;
            LogicCalc lc = new LogicCalc();

            TreeSet<CObj> onSpot = s.getObjCBelow(new Coord(x, y, curUser.getLowestZ()+1));
            int highest = Integer.MIN_VALUE;
            //find the object on the spot that has the highest z+stepableHeight and is still below the user.
            for (CObj curC : onSpot) {
                int top = curC.getZ(x, y) + lc.getStepableHeight(curC);
                if (top <= curUser.getLowestZ()) {
                    ArrayList<ObjT> types = curC.getTypePath();
                    for (ObjT t : types) {
                        if (t.standable()) {
                            if (top > highest) {
                                highest = top;
                            }
                        }
                    }
                }
            }

            int timeNeeded = ((int) Math.ceil((curUser.getLowestZ() - highest) / 50.0)) * 67+1;


            int myId =  timeline.getId();

            //create the wait actionstep solely for the purpose of stopping early
            ActionStep waitTill = new ActionStep(myId, "Wait", new ArrayList<Requirement>(), curUser.id, 0, new ArrayList<SubAction>(), new ArrayList<SubAction>(), 0, Stat.MISC);
            timeline.addAction(s.getTime() +  timeNeeded-1, waitTill);

            //this stops the auto wait if the user is going to reach the end of the turn
            if(s.getTime()+timeNeeded > timeline.getLastAction()){
                continueFalling = false;
            }

            //this processes to the next available time
            gm.processTime(s.getTime(), s.getTime() + timeNeeded);
        }


        else {
            continueFalling = false;
            continueSetUp();
        }
    }













    //**************************************************************** Narration stuff *****************************************************/

    public LinearLayout getNarrationView(boolean recycler){
        LinearLayout newL = new LinearLayout(this);
        newL.setOrientation(LinearLayout.VERTICAL);

        if(recycler) {
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
            newL.setLayoutParams(params);
        }
        else{
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            newL.setLayoutParams(params);
        }

        return newL;
    }





    private LinearLayout preFill(LinearLayout ll){
        ll.removeAllViews();

        int wrap = LinearLayout.LayoutParams.WRAP_CONTENT;
        int match = LinearLayout.LayoutParams.MATCH_PARENT;

        ImageView top = new ImageView(this);
        top.setBackground(getResources().getDrawable(R.drawable.bordertop));
        LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(match, wrap);
        top.setLayoutParams(topParams);
        ll.addView(top);

        LinearLayout middle = new LinearLayout(this);
        middle.setOrientation(LinearLayout.VERTICAL);
        int paddingVert = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
        int paddingHor = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, getResources().getDisplayMetrics());
        middle.setPadding(paddingHor, paddingVert, paddingHor, paddingVert);
        LinearLayout.LayoutParams middleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        middle.setBackground(getResources().getDrawable(R.drawable.bordermiddle));
        middle.setLayoutParams(middleParams);

        return middle;
    }

    private void postFill(LinearLayout ll, LinearLayout middle){
        ll.addView(middle);

        int wrap = LinearLayout.LayoutParams.WRAP_CONTENT;
        int match = LinearLayout.LayoutParams.MATCH_PARENT;


        ImageView bottom = new ImageView(this);
        bottom.setBackground(getResources().getDrawable(R.drawable.borderbottom));
        LinearLayout.LayoutParams bottomParams = new LinearLayout.LayoutParams(match, wrap);
        bottom.setLayoutParams(bottomParams);
        ll.addView(bottom);
    }



    public void fillNarrationView(LinearLayout ll, ArrayList<Narration> narrate) {
        LinearLayout middle = preFill(ll);
        int i = 0;
        int wrap = LinearLayout.LayoutParams.WRAP_CONTENT;
        int match = LinearLayout.LayoutParams.MATCH_PARENT;


        for (final Narration n : narrate) {


            LinearLayout newL = new LinearLayout(this);
            newL.setOrientation(LinearLayout.HORIZONTAL);

            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            newL.setPadding(padding, padding, padding, padding);
            newL.setWeightSum(4);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, wrap);
            newL.setLayoutParams(params);


            //the clickable
            newL.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!n.getInvolves().isEmpty()) {
                        setObjects(n);
                        showMapInfo(null);
                    }
                }
            });


            int five = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            int picSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

            //the image of the narration
            LinearLayout pic = new LinearLayout(this);
            pic.setBackground(getResources().getDrawable(n.getPic().getPicEnabled()));
            pic.setPadding(five, five, five, five);
            LinearLayout.LayoutParams picParams = new LinearLayout.LayoutParams(match, picSize);
            picParams.weight = 3;
            picParams.gravity = Gravity.CENTER;
            pic.setGravity(Gravity.CENTER);
            pic.setLayoutParams(picParams);



            //text of narration
            Typeface font3 = Typeface.createFromAsset(getAssets(), "MTCORSVA.TTF");
            TextView text = new TextView(this);
            text.setText(n.getText());
            text.setTypeface(font3);
            text.setTextColor(getResources().getColorStateList(R.color.black_text_button));
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(match, wrap);
            textParams.weight = 1;
            textParams.gravity = Gravity.CENTER;
            text.setGravity(Gravity.CENTER);
            text.setLayoutParams(textParams);


            if (i % 2 == 0) {
                newL.addView(pic);
                newL.addView(text);
            } else {
                newL.addView(text);
                newL.addView(pic);
            }

            i++;
            middle.addView(newL);
        }


        postFill(ll, middle);
    }



    public void fillNarrationView(LinearLayout ll, final Alert alert) {
        LinearLayout middle = preFill(ll);

        int wrap = LinearLayout.LayoutParams.WRAP_CONTENT;
        int match = LinearLayout.LayoutParams.MATCH_PARENT;
        Typeface font3 = Typeface.createFromAsset(getAssets(), "MTCORSVA.TTF");
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");


        int paddingBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        int buttonHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());


        //title of alert
        TextView title = new TextView(this);
        title.setText(alert.title);
        title.setTypeface(font);
        title.setTextColor(getResources().getColor(R.color.full_black));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(match, wrap);
        textParams.gravity = Gravity.CENTER;
        title.setPadding(0,0,0,paddingBottom);
        title.setGravity(Gravity.CENTER);
        title.setLayoutParams(textParams);
        title.setTextColor(getResources().getColor(R.color.alert_title));

        middle.addView(title);

        //description of alert
        TextView desc = new TextView(this);
        desc.setText(alert.description);
        desc.setTypeface(font3);
        desc.setTextColor(getResources().getColor(R.color.full_black));
        desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textParams.gravity = Gravity.CENTER;
        desc.setPadding(0,0,0,paddingBottom);
        desc.setGravity(Gravity.CENTER);
        desc.setLayoutParams(textParams);

        middle.addView(desc);


        //response options of alert
        for(int i = 0; i< alert.respond.size(); i++){
            Button response = new Button(this);
            response.setText(alert.respondText.get(i));
            response.setTypeface(font);
            response.setTextColor(getResources().getColor(R.color.white_text_button));
            response.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(wrap, buttonHeight);
            response.setLayoutParams(buttonParams);
            final Callable<Void> callMe = alert.respond.get(i);
            response.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callMe.call();
                }
            });
            buttonParams.gravity = Gravity.CENTER_HORIZONTAL;
            response.setGravity(Gravity.CENTER_HORIZONTAL);
            response.setBackground(getResources().getDrawable(R.drawable.brush2_button));

            middle.addView(response);
        }




        postFill(ll, middle);
    }

}












