package shenronproductions.app1.Activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import Managers.UserProfile;
import Popups.DeleteChar;
import Popups.MainGuide;
import Popups.NoChar;
import Popups.TitleTips;
import database.Players.CharacterPlayer;
import shenronproductions.app1.R;


public class MainActivity extends ActionBarActivity {
    ArrayList<Integer> menuItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        try {
            new File(Environment.getExternalStorageDirectory() + "/RPstrateGy").mkdir();
        } catch (Exception e) {
            Log.e("onCreate", e.getMessage());
        }
        try {
            new File(Environment.getExternalStorageDirectory() + "/RPstrateGy/characters").mkdir();
        } catch (Exception e) {
            Log.e("onCreate", e.getMessage());
        }
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        Typeface font2 = Typeface.createFromAsset(getAssets(), "BOD_BLAR.TTF");

        ((TextView) findViewById(R.id.mainTitle)).setTypeface(font);
        ((TextView) findViewById(R.id.mainTitle)).setText(Html.fromHtml("<font color=#E42217>RP</font><font color=#000000>strate</font><font color=#E42217>G</font><font color=#000000>y</font>"));
        ((Button) findViewById(R.id.onlineBut)).setTypeface(font);
        ((Button) findViewById(R.id.offlineBut)).setTypeface(font);
        ((Button) findViewById(R.id.customizeBut)).setTypeface(font);
        ((Button) findViewById(R.id.optionsBut)).setTypeface(font);
        ((Button) findViewById(R.id.deleteBut)).setTypeface(font2);

        /*((Button) findViewById(R.id.deleteBut)).setText("\u294A");
        ((Button) findViewById(R.id.deleteBut)).setTypeface(font3);*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuItems = new ArrayList<Integer>();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        try {
            CharacterPlayer c = UserProfile.getInstance().curChar;
            if (c == null){
                setTitle(" ");
                getSupportActionBar().setTitle(" ");
                ((Button) findViewById(R.id.onlineBut)).setEnabled(false);
                ((Button) findViewById(R.id.offlineBut)).setEnabled(false);
                ((Button) findViewById(R.id.customizeBut)).setEnabled(false);
                ((Button) findViewById(R.id.deleteBut)).setEnabled(false);
                new TitleTips().show(getFragmentManager(), "TitleTipsDialogFragment");
            }
            else {
                setTitle(c.name);
                getSupportActionBar().setTitle(c.name);
                ((Button) findViewById(R.id.onlineBut)).setEnabled(true);
                ((Button) findViewById(R.id.offlineBut)).setEnabled(true);
                ((Button) findViewById(R.id.customizeBut)).setEnabled(true);
                ((Button) findViewById(R.id.deleteBut)).setEnabled(true);
            }
            UserProfile ins = UserProfile.getInstance();
            HashMap<Integer, String> chars = ins.getAllChar();
            Iterator<Integer> it = chars.keySet().iterator();
            while(it.hasNext()){
                int i = it.next();
                String s = chars.get(i);
                menu.add(0, i, 0, s);
                menuItems.add(i);
            }
            return true;
        }
        catch(Exception e){
            Log.e("onCreateOptionsMenu", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            if (item.getItemId() ==  R.id.create_character) {
                createNewChar();
                return true;
            }
            else if(menuItems.contains(id)) {
                UserProfile up = UserProfile.getInstance();
                up.loadChar(item.getItemId());
                invalidateOptionsMenu();
                return true;
            }
        }
        catch(Exception e) {
            Log.e("onOptionsItemSelected", e.getMessage());
            return false;

        }
        return super.onOptionsItemSelected(item);
    }

    public void mainInfo(View v) {
        try {
            CharacterPlayer c = UserProfile.getInstance().curChar;
            if (c == null)
                new NoChar().show(getFragmentManager(), "NoCharDialogFragment");
            else
                new MainGuide().show(getFragmentManager(), "MainGuideDialogFragment");
        }
        catch(Exception e){
            Log.e("IO error in MainActivity, mainInfo", e.getMessage());
        }
    }

    public void offlineMenu(View v) {
        Intent intent = new Intent(this, offlineAct.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivityForResult(intent, 10);
    }

    public void customizeMenu(View v) {
        Intent intent = new Intent(this, customizeAct.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivityForResult(intent, 20);
    }

    public void createNewChar(){
        Intent intent = new Intent(this, newcharAct.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivityForResult(intent, 100);
    }

    //TODO make this also delete all games the char is in
    public void deleteChar(View v) throws Exception{
            new DeleteChar().show(getFragmentManager(), "DeleteCharDialogFragment");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        try{
            invalidateOptionsMenu();
        }
        catch(Exception e){}
        super.onActivityResult(requestCode, resultCode, data);
        //Do your work here in ActivityA

    }

    public void deleteStorage(View v){
        try {
            File folder = new File(Environment.getExternalStorageDirectory() + "/RPstrateGy");
            deleteFolder(folder);
            finish();
            System.exit(0);
        } catch (Exception e) {
            Log.e("deleteStorage", e.getMessage());
        }
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }





}
