package shenronproductions.app1.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import Managers.GameManager;
import Managers.UserProfile;
import Popups.FriendlyGuide;
import Popups.NewFriendly;
import Popups.NewFriendlyGuide;
import Popups.OfflineGuide;
import Utilities.StateKit;
import Utilities.StringInt;
import database.Levels.Level;
import database.Levels.wolfPack;
import database.Players.CharacterPlayer;
import shenronproductions.app1.R;


public class offlineAct extends ActionBarActivity {
    ArrayList<Integer> menuItems = new ArrayList<Integer>();
    String view;
    boolean dayTime;
    public static ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        GameManager.getInstance().offAct = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offline_menu);
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        ((TextView) findViewById(R.id.offTitle)).setTypeface(font);
        view = "offline";
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (view.compareTo("friendly") == 0) {
                setContentView(R.layout.offline_menu);
                Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
                ((TextView) findViewById(R.id.offTitle)).setTypeface(font);
                view = "offline";
                return true;
            }
            if (view.compareTo("new friendly") == 0) {
                view = "friendly";
                takeFoc();
                setContentView(R.layout.vs_friend);
                prepareFriendly();
                return true;
            }
            if (view.compareTo("cpu") == 0) {
                setContentView(R.layout.offline_menu);
                Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
                ((TextView) findViewById(R.id.offTitle)).setTypeface(font);
                view = "offline";
                return true;
            }
            if (view.compareTo("level") == 0) {
                view = "cpu";
                setContentView(R.layout.vs_cpu);
                prepareCPU();
                return true;
            }
            finish();
            return true;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (id == R.id.create_character) {
            createNewChar();
            return true;
        } else if (menuItems.contains(id)) {
            try {
                UserProfile up = UserProfile.getInstance();
                up.loadChar(item.getItemId());
                invalidateOptionsMenu();
                if (view.compareTo("friendly") == 0)
                    prepareFriendly();
                if(view.compareTo("new friendly") == 0)
                    prepareNewFriendly();
                if(view.compareTo("cpu") == 0)
                    prepareCPU();
                if(view.compareTo("level") == 0){
                    onCPU(null);
                }
                return true;
            } catch (Exception e) {
                Log.e("onOptionsItemSelected", e.getMessage());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuItems = new ArrayList<Integer>();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        try {
            CharacterPlayer c = UserProfile.getInstance().curChar;
            if (c == null) {
                setTitle(" ");
                getSupportActionBar().setTitle(" ");
            } else {
                setTitle(c.name);
                getSupportActionBar().setTitle(c.name);
            }
            UserProfile ins = UserProfile.getInstance();
            HashMap<Integer, String> chars = ins.getAllChar();
            Iterator<Integer> it = chars.keySet().iterator();
            while (it.hasNext()) {
                int i = it.next();
                String s = chars.get(i);
                menu.add(0, i, 0, s);
                menuItems.add(i);
            }
            return true;
        } catch (Exception e) {
            Log.e("onCreateOptionsMenu", e.getMessage());
            return false;
        }
    }

    public void createNewChar() {
        Intent intent = new Intent(this, newcharAct.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivityForResult(intent, 100);
    }


    public void offlineInfo(View v){
        new OfflineGuide().show(getFragmentManager(), "OfflineGuideDialogFragment");
    }

    public void friendlyInfo(View v){
        new FriendlyGuide().show(getFragmentManager(), "FriendlyGuideDialogFragment");
    }

    public void cpuInfo(View v){
        //TODO new CpuGuide().show(getFragmentManager(), "CpuGuideDialogFragment");
    }

    public void levelInfo(View v){
        //TODO new CpuGuide().show(getFragmentManager(), "CpuGuideDialogFragment");
    }

    public void newFriendlyInfo(View v){
        new NewFriendlyGuide().show(getFragmentManager(), "NFGuideDialogFragment");
    }

    public void setDay(View v){
        if(dayTime){
            dayTime = false;
            ImageButton ib = (ImageButton) findViewById(R.id.dayButton);
            ib.setBackground(getResources().getDrawable(R.drawable.empty_checkbox));
        }
        else{
            dayTime = true;
            ImageButton ib = (ImageButton) findViewById(R.id.dayButton);
            ib.setBackground(getResources().getDrawable(R.drawable.full_checkbox));
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            invalidateOptionsMenu();
            if (view.compareToIgnoreCase("friendly") == 0)
                prepareFriendly();
            if(view.compareToIgnoreCase("new friendly") == 0)
                prepareNewFriendly();
            if(view.compareToIgnoreCase("cpu") == 0)
                prepareCPU();
            if(view.compareToIgnoreCase("level") == 0)
                onCPU(null);
            return;

        } catch (Exception e) {
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void onFriendly(View v) {
        view = "friendly";
        setContentView(R.layout.vs_friend);
        try {
            new File(Environment.getExternalStorageDirectory() + "/RPstrateGy/friendly").mkdir();
        } catch (Exception e) {
            Log.e("onFriendly", e.getMessage());
        }

        prepareFriendly();
    }


    public void prepareFriendly() {
        try {
            Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
            ((Button) findViewById(R.id.newFriendly)).setTypeface(font);
            ((TextView) findViewById(R.id.friendlyTitle)).setTypeface(font);
            HashMap<Integer, String> states = UserProfile.getInstance().getFriendly();
            Integer[] stateIds = states.keySet().toArray(new Integer[states.keySet().size()]);
            LinearLayout layout = (LinearLayout) findViewById(R.id.listFriendly);
            layout.removeAllViews();
            for (int i = 0; i < stateIds.length; i++) {
                final Integer id = stateIds[i];
                final Button nameB = new Button(this);
                if (i % 2 == 0)
                    nameB.setBackground(getResources().getDrawable(R.drawable.brush1_button));
                else
                    nameB.setBackground(getResources().getDrawable(R.drawable.brush1flip_button));
                nameB.setPadding(0, 0, 0, 0);
                nameB.setText(states.get(id));
                nameB.setTypeface(font);
                nameB.setTextColor(getResources().getColorStateList(R.color.white_text_button));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, getResources().getDisplayMetrics()));
                params.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
                params.gravity = Gravity.CENTER_HORIZONTAL;
                nameB.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        new LoadGameTask().execute(id);
                    }
                });

                layout.addView(nameB, params);
            }
        } catch (Exception e) {
            Log.e("prepareFriendly", e.getMessage());
        }

    }


    public void newFriendly(View v) {
        setContentView(R.layout.new_friendly);
        view = "new friendly";
        prepareNewFriendly();

    }

    public void takeFoc(View v){
        takeFoc();
    }

    public void takeFoc(){
        View view = findViewById(R.id.newtakeFoc);
        view.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void prepareNewFriendly() {
        try {
            new File(Environment.getExternalStorageDirectory() + "/RPstrateGy/friendly/" + UserProfile.getInstance().curChar.charId).mkdir();
        } catch (Exception e) {
            Log.e("onCreate", e.getMessage());
        }
        try {
            dayTime = false;
            Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
            ((TextView) findViewById(R.id.newfriendlyTitle)).setTypeface(font);
            ((Button) findViewById(R.id.newfriendlyDone)).setTypeface(font);
            ((TextView) findViewById(R.id.dayLabel)).setTypeface(font);

            final EditText editText = (EditText) findViewById(R.id.newfriendlyName);
            editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId== EditorInfo.IME_ACTION_DONE){
                        takeFoc(v);
                    }
                    return false;
                }
            });

            InputFilter filter = new InputFilter() {
                final String blockCharacterSet = "\"\\";

                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {


                    if (source != null && blockCharacterSet.contains(("" + source))) {
                        return "";
                    }
                    return null;
                }
            };
            editText.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(20)});
            Typeface font2 = Typeface.createFromAsset(getAssets(), "BOD_BLAR.TTF");
            editText.setTypeface(font2);

            HashMap<Integer, String> allChars = UserProfile.getInstance().getAllChar();
            Integer[] keys = allChars.keySet().toArray(new Integer[allChars.keySet().size()]);
            StringInt[] charsArr = new StringInt[keys.length];
            for(int i = 0; i<keys.length; i++){
                charsArr[i] = new StringInt(allChars.get(keys[i]), keys[i]);
            }

            ((TextView) findViewById(R.id.p1Label)).setTypeface(font);
            TextView p1 = ((TextView) findViewById(R.id.p1Name));
            p1.setTypeface(font);
            p1.setText(UserProfile.getInstance().curChar.name);
            ArrayAdapter playerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, charsArr)       {
                public View getView(int position, View convertView,ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    ((TextView) v).setTextSize(25);
                    ((TextView) v).setTextColor(getResources().getColor(R.color.black));
                    ((TextView) v).setTypeface(Typeface.createFromAsset(getAssets(), "njnaruto.ttf"));
                    ((TextView) v).setGravity(Gravity.RIGHT);
                    return v; }};

            ((TextView) findViewById(R.id.p2Label)).setTypeface(font);
            ((Spinner) findViewById(R.id.p2Spinner)).setAdapter(playerAdapter);


            //all terrains go here
            String[] terrains = {"Forest"};
            ((TextView) findViewById(R.id.terrainLabel)).setTypeface(font);
            ArrayAdapter terrainAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, terrains)       {
                public View getView(int position, View convertView,ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    ((TextView) v).setTextSize(25);
                    ((TextView) v).setTextColor(getResources().getColor(R.color.black));
                    ((TextView) v).setTypeface(Typeface.createFromAsset(getAssets(), "njnaruto.ttf"));
                    ((TextView) v).setGravity(Gravity.RIGHT);
                    return v; }};
            ((Spinner) findViewById(R.id.terrainSpinner)).setAdapter(terrainAdapter);

            String[] turns = {"None", "15", "30", "45", "60", "120", "240"};
            ((TextView) findViewById(R.id.turnLabel)).setTypeface(font);
            ArrayAdapter turnAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, turns)       {
                public View getView(int position, View convertView,ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    ((TextView) v).setTextSize(25);
                    ((TextView) v).setTextColor(getResources().getColor(R.color.black));
                    ((TextView) v).setTypeface(Typeface.createFromAsset(getAssets(), "njnaruto.ttf"));
                    ((TextView) v).setGravity(Gravity.RIGHT);
                    return v; }};
            ((Spinner) findViewById(R.id.turnSpinner)).setAdapter(turnAdapter);

            String[] sizes = {"Small", "Medium", "Large"};
            ((TextView) findViewById(R.id.sizeLabel)).setTypeface(font);
            ArrayAdapter sizeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, sizes)       {
                public View getView(int position, View convertView,ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    ((TextView) v).setTextSize(25);
                    ((TextView) v).setTextColor(getResources().getColor(R.color.black));
                    ((TextView) v).setTypeface(Typeface.createFromAsset(getAssets(), "njnaruto.ttf"));
                    ((TextView) v).setGravity(Gravity.RIGHT);
                    return v; }};
            ((Spinner) findViewById(R.id.sizeSpinner)).setAdapter(sizeAdapter);
            ((Spinner) findViewById(R.id.sizeSpinner)).setSelection(1);


        }
        catch(Exception e){
            Log.e("Trying to start new friendly game", e.getMessage());
        }

    }

    public void tryFinishNewFriendly(View v){
        String name = ((EditText) findViewById(R.id.newfriendlyName)).getText().toString();
        name.trim();
        if(name.compareToIgnoreCase("") == 0){
            new NewFriendly().show(getFragmentManager(), "NewFriendlyFragment");
        }
        else{
            finishNewFriendly(name);
        }
    }
    public void finishNewFriendly(String name){
        //create state with inputs
        String terrain = (String) ((Spinner) findViewById(R.id.terrainSpinner)).getSelectedItem();
        String turns = (String) ((Spinner) findViewById(R.id.turnSpinner)).getSelectedItem();
        String size = (String) ((Spinner) findViewById(R.id.sizeSpinner)).getSelectedItem();


        //get characters
        int p2 = ((StringInt) ((Spinner) findViewById(R.id.p2Spinner)).getSelectedItem()).i;

        CharacterPlayer player1 = UserProfile.getInstance().curChar;

        CharacterPlayer player2 = null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try{
            fis = new FileInputStream(Environment.getExternalStorageDirectory() +"/RPstrateGy/characters/"+p2);
            ois = new ObjectInputStream(fis);
            player2 = (CharacterPlayer) ois.readObject();
            fis.close();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            try {
                if (fis != null)
                    fis.close();
            } catch (Exception p) {}

            try {
                if (ois != null)
                    ois.close();
            } catch (Exception p) {}

            Log.e("!!!!finishNewFriendly in offlineAct!!!!!", e.getMessage());
        }

        //add the state to the manager and prepare it
        StateKit s = new StateKit(name, turns, terrain, player1, player2, size, dayTime);

        //reset the view for when they back
        view = "friendly";
        setContentView(R.layout.vs_friend);
        prepareFriendly();

        new CreateGameTask().execute(s);
    }











    public void onCPU(View v) {
        view = "cpu";
        setContentView(R.layout.vs_cpu);
        try {
            new File(Environment.getExternalStorageDirectory() + "/RPstrateGy/cpu").mkdir();
        } catch (Exception e) {
            Log.e("onCPU", e.getMessage());
        }

        prepareCPU();
    }


    private void prepareCPU(){
        CharacterPlayer c = UserProfile.getInstance().curChar;

        try {
            new File(Environment.getExternalStorageDirectory() + "/RPstrateGy/cpu/"+ c.charId).mkdir();
        } catch (Exception e) {
            Log.e("onCPU", e.getMessage());
        }

        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        ((TextView) findViewById(R.id.cpuTitle)).setTypeface(font);
        //TODO disable levels that are too high a level for cur user
    }



    public void wolfPack(View v){
        prepareLevel(new wolfPack());
    }



    private void prepareLevel(Level lev){
        //set view
        view = "level";
        setContentView(R.layout.cpu_level);

        //make things look nice
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");

        ((Button) findViewById(R.id.newLevel)).setTypeface(font);

        Button contLevel = ((Button) findViewById(R.id.continueLevel));
        contLevel.setTypeface(font);
        contLevel.setEnabled(false);

        TextView levelTitle = ((TextView) findViewById(R.id.levelTitle));
        levelTitle.setTypeface(font);
        levelTitle.setText(lev.presentableName);

        //remember level
        curLevel = lev;

        //see if we already have a file for this level
        File cpuFolder = new File(Environment.getExternalStorageDirectory() +"/RPstrateGy/cpu/"+ UserProfile.getInstance().curChar.charId);
        File[] fs = cpuFolder.listFiles();
        for(File f :  fs) {
            if(f.getName().contains(lev.fileName)){
                contLevel.setEnabled(true);
                break;
            }
        }
    }


    Level curLevel;


    public void newLevel(View v){
        //reset the view for when they back
        view = "cpu";
        setContentView(R.layout.vs_cpu);
        prepareCPU();

        new CreateGameTaskCPU().execute(curLevel);
    }

    public void continueLevel(View v){
        //reset the view for when they back
        view = "cpu";
        setContentView(R.layout.vs_cpu);
        prepareCPU();

        new LoadGameTaskCPU().execute(curLevel);
    }






































    public class LoadGameTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            Integer id = params[0];
            try {
                GameManager.getInstance().oldGame(Environment.getExternalStorageDirectory() + "/RPstrateGy/friendly/" + UserProfile.getInstance().curChar.charId + "/" + id);
            }

            catch(IOException | ClassNotFoundException e){
                Log.e("loadingFriendly", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //display the new game
            Intent intent = new Intent(offlineAct.this, gameAct.class);
            intent.putExtra("New", false);
            startActivityForResult(intent, 5050);

        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(offlineAct.this,
                    "Loading Game", "This can take up to 25 seconds.");
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }


    public class LoadGameTaskCPU extends AsyncTask<Level, Void, Void> {

        @Override
        protected Void doInBackground(Level... params) {
            Level id = params[0];
            try {
                GameManager.getInstance().oldGame(id);
            }

            catch(IOException | ClassNotFoundException e){
                Log.e("loadingFriendly", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //display the new game
            Intent intent = new Intent(offlineAct.this, gameAct.class);
            intent.putExtra("New", false);
            startActivityForResult(intent, 5050);

        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(offlineAct.this,
                    "Loading Game", "This can take up to 25 seconds.");
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }






    public class CreateGameTask extends AsyncTask<StateKit, Void, Void> {

        @Override
        protected Void doInBackground(StateKit... params) {
            StateKit s = params[0];
            try {
                GameManager.getInstance().newGame(s);
            }

            catch(IOException e){
                Log.e("!!!!creating new game in offlineAct!!!!!", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //display the new game
            Intent intent = new Intent(offlineAct.this, gameAct.class);
            intent.putExtra("New", true);
            startActivityForResult(intent, 5050);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(offlineAct.this,
                    "Creating Game", "This can take up to 25 seconds.");
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }


    public class CreateGameTaskCPU extends AsyncTask<Level, Void, Void> {

        @Override
        protected Void doInBackground(Level... params) {
            Level s = params[0];
            try {
                GameManager.getInstance().newGame(s);
            }

            catch(IOException e){
                Log.e("!!!!creating new game in offlineAct!!!!!", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //display the new game
            Intent intent = new Intent(offlineAct.this, gameAct.class);
            intent.putExtra("New", true);
            startActivityForResult(intent, 5050);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(offlineAct.this,
                    "Creating Game", "This can take up to 25 seconds.");
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

    }
}