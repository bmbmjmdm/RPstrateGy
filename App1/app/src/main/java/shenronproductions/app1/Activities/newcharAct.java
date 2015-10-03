package shenronproductions.app1.Activities;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import Managers.UserProfile;
import Popups.NewCharGuide;
import Popups.NoNameEr;
import Utilities.FullLists;
import database.Players.CharacterPlayer;
import shenronproductions.app1.R;


public class newcharAct extends ActionBarActivity {
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.create_character);
        name = "";
        prepareCC();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_newchar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(item.getItemId()){
            case android.R.id.home:
                    finish();
                    return true;

        }

        return super.onOptionsItemSelected(item);
    }

    //the create character view needs 2 things:
    //1: make sure the input text for name doesn't hog focus when you leave it
    //2: make sure the keyboard doesnt allow special characters and has a max length
    public void prepareCC(){
        final EditText editText = (EditText) findViewById(R.id.inName);
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
        editText.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(10)});

        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        ((Button) findViewById(R.id.charNext)).setTypeface(font);
        ((TextView) findViewById(R.id.newcharTitle)).setTypeface(font);

        Typeface font2 = Typeface.createFromAsset(getAssets(), "BOD_BLAR.TTF");
        editText.setTypeface(font2);
    }

    public void takeFoc(View v){
        View view = findViewById(R.id.takeFoc);
        view.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


    public void newCharInfo(View v){
        new NewCharGuide().show(getFragmentManager(), "CustGuideDialogFragment");
    }

    public void doneChar(View v){
        takeFoc(v);
        name = ((EditText) findViewById(R.id.inName)).getText().toString();
        name = name.trim();
        if(name.compareTo("") == 0){
            new NoNameEr().show(getFragmentManager(), "CharGuideDialogFragment");
        }
        else {
            try {
                UserProfile inst = UserProfile.getInstance();

                CharacterPlayer c = new CharacterPlayer(name, inst.getId());
                inst.curChar = c;

                c.actions.addAll(FullLists.getDefaultActions());

                inst.saveChar();
                setTitle(c.name);
                finish();
            }
            catch(Exception e){
                //Log.e("IO Error in newcharAct, doneChar:", e.getMessage());
            }
        }
    }





}
