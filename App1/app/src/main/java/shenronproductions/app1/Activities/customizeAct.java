package shenronproductions.app1.Activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import Managers.UserProfile;
import Popups.ActionsGuide;
import Popups.ActivatePerksGuide;
import Popups.BuyActionsGuide;
import Popups.BuyPerksGuide;
import Popups.ClassesGuide;
import Popups.CustomizeGuide;
import Popups.PerksGuide;
import Popups.StatsGuide;
import Popups.ViewActionsGuide;
import Utilities.ContinuousLongClickListener;
import Utilities.FullLists;
import Utilities.Stat;
import database.Actions.Action;
import database.Perks.Perk;
import database.Players.CharacterPlayer;
import database.Requirements.StatelessRequirement;
import shenronproductions.app1.R;


public class customizeAct extends ActionBarActivity {
    ArrayList<Integer> menuItems;
    String view;
    HashMap<Stat, Integer> stats;
    ArrayList<Perk> buyPerks = new ArrayList<Perk>();
    ArrayList<Perk> activePerks = new ArrayList<Perk>();
    ArrayList<Perk> inactivePerks = new ArrayList<Perk>();
    ArrayList<Action> buyActions = new ArrayList<Action>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customize_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prepareMain();
    }

    public void prepareMain(){
        view = "customize";
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        ((Button) findViewById(R.id.perksBut)).setTypeface(font);
        ((Button) findViewById(R.id.itemsBut)).setTypeface(font);
        ((TextView) findViewById(R.id.spLeft)).setTypeface(font);
        ((Button) findViewById(R.id.statsBut)).setTypeface(font);
        ((Button) findViewById(R.id.classBut)).setTypeface(font);
        ((Button) findViewById(R.id.actsBut)).setTypeface(font);
        ((TextView) findViewById(R.id.custTitle)).setTypeface(font);
        try{
            int sp = UserProfile.getInstance().curChar.sp;
            String spLeft = colorSP(sp);
            spLeft = spLeft.concat(" Skill Points");
            ((TextView) findViewById(R.id.spLeft)).setText(Html.fromHtml(spLeft));
        }
        catch(Exception e){
            Log.e("Failed to load User Profile in customize act, prepareMain: ", e.getMessage());
        }
    }

    public String colorSP(int sp){
        String spLeft;
        if(sp > 30){
            spLeft = "<font color=#41A317>"+sp+"</font>";
        }
        else if(sp > 0){
            spLeft = "<font color=#0000CC>"+sp+"</font>";
        }
        else {
            spLeft = "<font color=#E42217>"+sp+"</font>";
        }
        return spLeft;
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
            }
            else{
                setTitle(c.name);
                getSupportActionBar().setTitle(c.name);
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
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if ((view.compareToIgnoreCase("classes") == 0) ||
                    (view.compareToIgnoreCase("perks") == 0) ||
                    (view.compareToIgnoreCase("stats") == 0) ||
                    (view.compareToIgnoreCase("actions") == 0)) {
                setContentView(R.layout.customize_menu);
                prepareMain();
                return true;
            }
            else if ((view.compareToIgnoreCase("buyPerks") == 0) || (view.compareToIgnoreCase("activatePerks") == 0)) {
                setContentView(R.layout.perks_menu);
                preparePerksMenu();
                return true;
            }
            else if ((view.compareToIgnoreCase("buyActions") == 0) || (view.compareToIgnoreCase("viewActions") == 0)) {
                setContentView(R.layout.actions_menu);
                prepareActionsMenu();
                return true;
            }
            else if (view.compareToIgnoreCase("customize") == 0) {
                finish();
                return true;
            }
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
                if (view.compareToIgnoreCase("classes") == 0)
                    prepareClasses();
                else if (view.compareToIgnoreCase("customize") == 0)
                    prepareMain();
                else if (view.compareToIgnoreCase("perks") == 0)
                    preparePerksMenu();
                else if(view.compareToIgnoreCase("buyPerks") == 0)
                    prepareBuyPerks();
                else if(view.compareToIgnoreCase("activatePerks") == 0)
                    prepareActivatePerks();
                else if(view.compareToIgnoreCase("stats") == 0)
                    prepareStats();
                else if(view.compareToIgnoreCase("actions") == 0)
                    prepareActionsMenu();
                else if(view.compareToIgnoreCase("buyActions") == 0)
                    prepareBuyActions();
                else if(view.compareToIgnoreCase("viewActions") == 0)
                    prepareViewActions();
                return true;
            } catch (Exception e) {
                Log.e("onOptionsItemSelected", e.getMessage());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void createNewChar() {
        Intent intent = new Intent(this, newcharAct.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            invalidateOptionsMenu();
            if (view.compareToIgnoreCase("classes") == 0)
                prepareClasses();
            else if(view.compareToIgnoreCase("customize") == 0)
                prepareMain();
            else if(view.compareToIgnoreCase("perks") == 0)
                preparePerksMenu();
            else if(view.compareToIgnoreCase("buyPerks") == 0)
                prepareBuyPerks();
            else if(view.compareToIgnoreCase("activatePerks") == 0)
                prepareActivatePerks();
            else if(view.compareToIgnoreCase("actions") == 0)
                prepareActionsMenu();
            else if(view.compareToIgnoreCase("buyActions") == 0)
                prepareBuyActions();
            else if(view.compareToIgnoreCase("viewActions") == 0)
                prepareViewActions();
            else if(view.compareToIgnoreCase("stats") == 0)
                prepareStats();

        } catch (Exception e) {
        }
        super.onActivityResult(requestCode, resultCode, data);
        //Do your work here in ActivityA

    }

    public void customizeInfo(View v){
        new CustomizeGuide().show(getFragmentManager(), "CustGuideDialogFragment");
    }


    public void editClasses(View v){
        setContentView(R.layout.edit_classes);
        prepareClasses();
    }

    public void classesInfo(View v){
        new ClassesGuide().show(getFragmentManager(), "ClassGuideDialogFragment");
    }

    public void actionsInfo(View v){
        new ActionsGuide().show(getFragmentManager(), "ActionGuideDialogFragment");
    }

    public void editPerks(View v){
        setContentView(R.layout.perks_menu);
        preparePerksMenu();
    }

    public void editActions(View v){
        setContentView(R.layout.actions_menu);
        prepareActionsMenu();
    }

    public void buyPerks(View v){
        setContentView(R.layout.buy_perks);
        prepareBuyPerks();
    }

    public void buyActions(View v){
        setContentView(R.layout.buy_actions);
        prepareBuyActions();
    }

    public void viewActions(View v){
        setContentView(R.layout.view_actions);
        prepareViewActions();
    }

    public void perksInfo(View v){
        new PerksGuide().show(getFragmentManager(), "PerksGuideDialogFragment");
    }

    public void buyPerksInfo(View v){
        new BuyPerksGuide().show(getFragmentManager(), "BuyPerksGuideDialogFragment");
    }

    public void buyActionsInfo(View v){
        new BuyActionsGuide().show(getFragmentManager(), "BuyActionsGuideDialogFragment");
    }

    public void activatePerks(View v){
        setContentView(R.layout.activate_perks);
        prepareActivatePerks();
    }

    public void activatePerksInfo(View v){
        new ActivatePerksGuide().show(getFragmentManager(), "ActivatePerksGuideDialogFragment");
    }

    public void viewActionsInfo(View v){
        new ViewActionsGuide().show(getFragmentManager(), "ViewActionsGuideDialogFragment");
    }

    public void editStats(View v){
        setContentView(R.layout.edit_stats);
        prepareStats();
    }

    public void statsInfo(View v){
        new StatsGuide().show(getFragmentManager(), "StatsGuideDialogFragment");
    }












    public void prepareBuyPerks(){
        view = "buyPerks";
        buyPerks = new ArrayList<Perk>();
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        ((TextView) findViewById(R.id.buyPerksTitle)).setTypeface(font);
        ((TextView) findViewById(R.id.spLeft)).setTypeface(font);
        ((Button) findViewById(R.id.saveBuyPerks)).setTypeface(font);
        CharacterPlayer cur;
        try {
            cur = UserProfile.getInstance().curChar;
        }
        catch(Exception e){
            Log.e("Failed to load User Profile in customize act, prepareBuyPerks: ", e.getMessage());
            return;
        }
        ((TextView) findViewById(R.id.spLeft)).setText(Html.fromHtml(colorSP(cur.sp)));

        ArrayList<Perk> perks = FullLists.getPerks();

        ArrayList<Perk> charPerks = (ArrayList<Perk>) cur.inactivePerks.clone();
        charPerks.addAll(cur.perks);

        //remove all perks the player already has from the list
        Iterator<Perk> it = perks.iterator();
        while(it.hasNext()){
            Perk p = it.next();
            for(Perk p2: charPerks){
                if(p.name.compareToIgnoreCase(p2.name) == 0) {
                    it.remove();
                    break;
                }
            }
        }

        //filter out the ones the player can use
        Iterator<Perk> it2 = perks.iterator();
        while(it2.hasNext()){
            Perk p = it2.next();
            for(StatelessRequirement sr : p.requirements){
                if(!sr.canUse(cur)) {
                    it2.remove();
                    break;
                }
            }
        }

        Typeface font2 = Typeface.createFromAsset(getAssets(), "BOD_BLAR.TTF");
        LinearLayout overView = (LinearLayout) findViewById(R.id.charPerkOptions);
        overView.removeAllViews();
        final int size = perks.size();
        for(int i = 0; i< size ; i++){
            final int r = i+1;
            final int r2 = i+1000;
            final int r3 = i+1000000;

            final Perk p = perks.get(i);
            final RelativeLayout newView = new RelativeLayout(this);

            final Button nameB = new Button(this);

            nameB.setId(r3);
            if(i%2 == 0)
                nameB.setBackground( getResources().getDrawable(R.drawable.brush1_button));
            else
                nameB.setBackground( getResources().getDrawable(R.drawable.brush1flip_button));
            nameB.setPadding(0, 0, 0, 0);

            //set layout's height to be small
            LinearLayout.LayoutParams newViewP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
            //newView.setLayoutParams(newViewP);
            overView.addView(newView, newViewP);
            newView.setId(r);

            //set name of perk button
            nameB.setText(p.name);
            nameB.setTypeface(font2);
            nameB.setTextColor(getResources().getColorStateList(R.color.white_text_button));


            //set perk button in top left of relativelayout and define its width
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -30, getResources().getDisplayMetrics());
            //nameB.setLayoutParams(params);

            //add event listener to expand layout when button is clicked
            nameB.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    int h = newView.getLayoutParams().height;
                    if(h == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())) {
                        newView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        newView.requestLayout();
                    }
                    else {
                        newView.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
                        newView.requestLayout();
                    }
                }
            });

            newView.addView(nameB, params);

            //add text view for perk flavor
            TextView tv = new TextView(this);
            tv.setText(Html.fromHtml(p.requirementsDes+p.description));
            tv.setTypeface(font);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);

            //set flavor in left under the button
            RelativeLayout.LayoutParams paramsT = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsT.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            paramsT.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsT.addRule(RelativeLayout.BELOW, r3);
            //tv.setLayoutParams(paramsT);
            newView.addView(tv, paramsT);


            //set selector button
            final RelativeLayout selHold = new RelativeLayout(this);
            selHold.setId(r2);
            final ImageButton select = new ImageButton(this);
            select.setId(r);

            RelativeLayout.LayoutParams paramsS = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
            paramsS.addRule(RelativeLayout.ALIGN_BOTTOM, r3);
            paramsS.addRule(RelativeLayout.RIGHT_OF, r3);
            paramsS.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            paramsS.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            //select.setLayoutParams(paramsS);

            RelativeLayout.LayoutParams paramsIB = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsIB.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsIB.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            paramsIB.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            select.setScaleType(ImageView.ScaleType.CENTER_CROP);
            select.setImageResource(R.drawable.locked);
            select.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            //add event listener to expand layout when button is clicked
            select.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    if(buyPerks.contains(p)){
                        nameB.setTextColor(getResources().getColor(R.color.white_text_button));
                        select.setImageResource(R.drawable.locked);
                        buyPerks.remove(p);
                        TextView spV = ((TextView) findViewById(R.id.spLeft));
                        int sp = Integer.decode(spV.getText().toString());
                        sp += p.cost;
                        spV.setText(Html.fromHtml(colorSP(sp)));
                    }
                    else{
                        TextView spV = ((TextView) findViewById(R.id.spLeft));
                        int sp = Integer.decode(spV.getText().toString());
                        if(sp>= p.cost){
                            nameB.setTextColor(getResources().getColor(R.color.dark_gold));
                            select.setImageResource(R.drawable.unlocked);
                            buyPerks.add(p);
                            sp -= p.cost;
                            spV.setText(Html.fromHtml(colorSP(sp)));
                        }
                    }
                }
            });

            newView.addView(selHold, paramsS);
            selHold.addView(select, paramsIB);


        }
        overView.invalidate();
        overView.requestLayout();
    }

    public void preparePerksMenu(){
        view = "perks";
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        ((Button) findViewById(R.id.buyPerksBut)).setTypeface(font);
        ((Button) findViewById(R.id.activatePerksBut)).setTypeface(font);
        ((TextView) findViewById(R.id.spLeft)).setTypeface(font);
        ((TextView) findViewById(R.id.perksTitle)).setTypeface(font);
        try{
            int sp = UserProfile.getInstance().curChar.sp;
            String spLeft = colorSP(sp);
            spLeft = spLeft.concat(" Skill Points");
            ((TextView) findViewById(R.id.spLeft)).setText(Html.fromHtml(spLeft));
        }
        catch(Exception e){
            Log.e("Failed to load User Profile in customize act, preparePerksMenu: ", e.getMessage());
        }
    }

    public void saveBuyPerks(View v){
        try{
            UserProfile up = UserProfile.getInstance();
            CharacterPlayer cur = up.curChar;
            for(Perk p : buyPerks){
                cur.perks.add(p);
                p.statelessApply(cur);
            }
            cur.spSpent += cur.sp - Integer.parseInt(((TextView) findViewById(R.id.spLeft)).getText().toString());
            cur.sp = Integer.parseInt(((TextView) findViewById(R.id.spLeft)).getText().toString());
            up.saveChar();
            setContentView(R.layout.perks_menu);
            preparePerksMenu();
        }
        catch(Exception e){
            Log.e("IO exception in customizeAct, saveBuyPerks", e.getMessage());
        }
    }






    public void prepareActivatePerks() {
        view = "activatePerks";
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        ((TextView) findViewById(R.id.activatePerksTitle)).setTypeface(font);
        ((Button) findViewById(R.id.saveActivatePerks)).setTypeface(font);
        final CharacterPlayer cur;
        try {
            cur = UserProfile.getInstance().curChar;
        } catch (Exception e) {
            Log.e("Failed to load User Profile in customize act, prepareBuyPerks: ", e.getMessage());
            return;
        }
        activePerks = (ArrayList) cur.perks.clone();
        inactivePerks = (ArrayList) cur.inactivePerks.clone();

        Typeface font2 = Typeface.createFromAsset(getAssets(), "BOD_BLAR.TTF");
        LinearLayout overView = (LinearLayout) findViewById(R.id.charPerkOptions);
        overView.removeAllViews();
        for (int count = 1; count < 3; count++) {
            final int size;
            ArrayList<Perk> perks;
            int mod = 0;
            if (count == 1) {
                size = activePerks.size();
                perks = activePerks;
            } else {
                size = inactivePerks.size();
                perks = inactivePerks;
                mod = activePerks.size()%2;
            }


            for (int i = 0; i < size; i++) {
                final int r = (i + 1)*count;
                final int r2 = (i + 1000)*count;
                final int r3 = (i + 1000000)*count;
                final int theCount = count;

                final Perk p = perks.get(i);
                final RelativeLayout newView = new RelativeLayout(this);

                final Button nameB = new Button(this);

                nameB.setId(r3);
                if ((i+mod) % 2 == 0)
                    nameB.setBackground(getResources().getDrawable(R.drawable.brush1_button));
                else
                    nameB.setBackground(getResources().getDrawable(R.drawable.brush1flip_button));
                nameB.setPadding(0, 0, 0, 0);

                //set layout's height to be small
                LinearLayout.LayoutParams newViewP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
                //newView.setLayoutParams(newViewP);
                overView.addView(newView, newViewP);
                newView.setId(r);

                //set name of perk button
                nameB.setText(p.name);
                nameB.setTypeface(font2);
                if(theCount == 1)
                    nameB.setTextColor(getResources().getColorStateList(R.color.dark_gold));
                else
                    nameB.setTextColor(getResources().getColorStateList(R.color.white_text_button));


                //set perk button in top left of relativelayout and define its width
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -30, getResources().getDisplayMetrics());
                //nameB.setLayoutParams(params);

                //add event listener to expand layout when button is clicked
                nameB.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int h = newView.getLayoutParams().height;
                        if (h == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())) {
                            newView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            newView.requestLayout();
                        } else {
                            newView.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
                            newView.requestLayout();
                        }
                    }
                });

                newView.addView(nameB, params);

                //add text view for perk flavor
                TextView tv = new TextView(this);
                tv.setText(Html.fromHtml(p.description));
                tv.setTypeface(font);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

                //set flavor in left under the button
                RelativeLayout.LayoutParams paramsT = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                paramsT.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                paramsT.addRule(RelativeLayout.ALIGN_PARENT_START);
                paramsT.addRule(RelativeLayout.BELOW, r3);
                //tv.setLayoutParams(paramsT);
                newView.addView(tv, paramsT);


                //set selector button
                final RelativeLayout selHold = new RelativeLayout(this);
                selHold.setId(r2);
                final ImageButton select = new ImageButton(this);
                select.setId(r);

                RelativeLayout.LayoutParams paramsS = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
                paramsS.addRule(RelativeLayout.ALIGN_BOTTOM, r3);
                paramsS.addRule(RelativeLayout.RIGHT_OF, r3);
                paramsS.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
                paramsS.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
                //select.setLayoutParams(paramsS);

                RelativeLayout.LayoutParams paramsIB = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                paramsIB.addRule(RelativeLayout.ALIGN_PARENT_START);
                paramsIB.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                paramsIB.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                select.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if(theCount==1)
                    select.setImageResource(R.drawable.full_checkbox);
                else
                    select.setImageResource(R.drawable.empty_checkbox);
                select.setBackgroundColor(getResources().getColor(android.R.color.transparent));

                //add event listener to expand layout when button is clicked
                select.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (activePerks.contains(p)) {
                            nameB.setTextColor(getResources().getColor(R.color.white_text_button));
                            select.setImageResource(R.drawable.empty_checkbox);
                            activePerks.remove(p);
                            inactivePerks.add(p);
                        } else {
                            nameB.setTextColor(getResources().getColor(R.color.dark_gold));
                            select.setImageResource(R.drawable.full_checkbox);
                            inactivePerks.remove(p);
                            activePerks.add(p);
                        }
                    }
                });

                newView.addView(selHold, paramsS);
                selHold.addView(select, paramsIB);


            }
        }

        overView.invalidate();
        overView.requestLayout();
    }

    public void saveActivatePerks(View v){
        try{
            UserProfile up = UserProfile.getInstance();
            CharacterPlayer cur = up.curChar;
            ArrayList<Perk> charActivePerks = cur.perks;
            ArrayList<Perk> charInactivePerks = cur.inactivePerks;
            activePerks.removeAll(charActivePerks); //this shows the new active perks
            inactivePerks.removeAll(charInactivePerks); //this shows the new inactive perks

            for(Perk p: activePerks){
                p.statelessApply(cur);
                cur.perks.add(p);
                cur.inactivePerks.remove(p);
            }
            for(Perk p: inactivePerks){
                p.statelessUnApply(cur);
                cur.inactivePerks.add(p);
                cur.perks.remove(p);
            }

            up.saveChar();
            setContentView(R.layout.perks_menu);
            preparePerksMenu();
        }
        catch(Exception e){
            Log.e("IO exception in customizeAct, saveActivePerks", e.getMessage());
        }
    }











    public void prepareActionsMenu(){
        view = "actions";
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        ((Button) findViewById(R.id.buyActionsBut)).setTypeface(font);
        ((Button) findViewById(R.id.viewActionsBut)).setTypeface(font);
        ((TextView) findViewById(R.id.spLeft)).setTypeface(font);
        ((TextView) findViewById(R.id.actionsTitle)).setTypeface(font);
        try{
            int sp = UserProfile.getInstance().curChar.sp;
            String spLeft = colorSP(sp);
            spLeft = spLeft.concat(" Skill Points");
            ((TextView) findViewById(R.id.spLeft)).setText(Html.fromHtml(spLeft));
        }
        catch(Exception e){
            Log.e("Failed to load User Profile in customize act, prepareActionsMenu: ", e.getMessage());
        }
    }

    public void prepareBuyActions(){
        view = "buyActions";
        buyActions = new ArrayList<Action>();
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        Typeface font3 = Typeface.createFromAsset(getAssets(), "MTCORSVA.TTF");
        ((TextView) findViewById(R.id.buyActionsTitle)).setTypeface(font);
        ((TextView) findViewById(R.id.spLeft)).setTypeface(font);
        ((Button) findViewById(R.id.saveBuyActions)).setTypeface(font);
        CharacterPlayer cur;
        try {
            cur = UserProfile.getInstance().curChar;
        }
        catch(Exception e){
            Log.e("Failed to load User Profile in customize act, prepareBuyActions: ", e.getMessage());
            return;
        }
        ((TextView) findViewById(R.id.spLeft)).setText(Html.fromHtml(colorSP(cur.sp)));

        ArrayList<Action> actions = FullLists.getActions();

        ArrayList<Action> charActions = (ArrayList<Action>) cur.actions;

        //remove all perks the player already has from the list
        Iterator<Action> it = actions.iterator();
        while(it.hasNext()){
            Action a = it.next();
            for(Action a2: charActions){
                if(a.name.compareToIgnoreCase(a2.name) == 0) {
                    it.remove();
                    break;
                }
            }
        }

        //filter out the ones the player can use
        Iterator<Action> it2 = actions.iterator();
        while(it2.hasNext()){
            Action a = it2.next();
            for(StatelessRequirement sr : a.statelessRequirements){
                if(!sr.canUse(cur)) {
                    it2.remove();
                    break;
                }
            }
        }

        Typeface font2 = Typeface.createFromAsset(getAssets(), "BOD_BLAR.TTF");
        LinearLayout overView = (LinearLayout) findViewById(R.id.charActionsOptions);
        overView.removeAllViews();
        final int size = actions.size();
        for(int i = 0; i< size ; i++){
            final int r = i+1;
            final int r2 = i+1000;
            final int r3 = i+1000000;
            final int r4 = i+10000000;

            final Action a = actions.get(i).getCopy();
            final RelativeLayout newView = new RelativeLayout(this);

            final Button nameB = new Button(this);

            nameB.setId(r3);
            if(i%2 == 0)
                nameB.setBackground( getResources().getDrawable(R.drawable.brush1_button));
            else
                nameB.setBackground( getResources().getDrawable(R.drawable.brush1flip_button));
            nameB.setPadding(0, 0, 0, 0);

            //set layout's height to be small
            LinearLayout.LayoutParams newViewP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
            //newView.setLayoutParams(newViewP);
            overView.addView(newView, newViewP);
            newView.setId(r);

            //set name of perk button
            nameB.setText(a.name);
            nameB.setTypeface(font2);
            nameB.setTextColor(getResources().getColorStateList(R.color.white_text_button));


            //set perk button in top left of relativelayout and define its width
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -30, getResources().getDisplayMetrics());
            //nameB.setLayoutParams(params);

            //add event listener to expand layout when button is clicked
            nameB.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    int h = newView.getLayoutParams().height;
                    if(h == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())) {
                        newView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        newView.requestLayout();
                    }
                    else {
                        newView.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
                        newView.requestLayout();
                    }
                }
            });

            newView.addView(nameB, params);

            //add text view for perk req
            TextView tv = new TextView(this);
            tv.setText(Html.fromHtml(a.getBuyReq()));
            tv.setTypeface(font);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            tv.setId(r4);

            //set flavor in left under the button
            RelativeLayout.LayoutParams paramsT = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsT.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            paramsT.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsT.addRule(RelativeLayout.BELOW, r3);

            //tv.setLayoutParams(paramsT);
            newView.addView(tv, paramsT);

            //add text view for perk flavor
            TextView tv2 = new TextView(this);
            tv2.setText(Html.fromHtml(a.getFlavor()));
            tv2.setTypeface(font3);
            tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);

            //set flavor in left under the button
            RelativeLayout.LayoutParams paramsT2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsT2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            paramsT.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsT2.addRule(RelativeLayout.BELOW, r4);
            //tv.setLayoutParams(paramsT);
            newView.addView(tv2, paramsT2);


            //set selector button
            final RelativeLayout selHold = new RelativeLayout(this);
            selHold.setId(r2);
            final ImageButton select = new ImageButton(this);
            select.setId(r);

            RelativeLayout.LayoutParams paramsS = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
            paramsS.addRule(RelativeLayout.ALIGN_BOTTOM, r3);
            paramsS.addRule(RelativeLayout.RIGHT_OF, r3);
            paramsS.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            paramsS.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            //select.setLayoutParams(paramsS);

            RelativeLayout.LayoutParams paramsIB = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsIB.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsIB.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            paramsIB.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            select.setScaleType(ImageView.ScaleType.CENTER_CROP);
            select.setImageResource(R.drawable.locked);
            select.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            //add event listener to expand layout when button is clicked
            select.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    if(buyActions.contains(a)){
                        nameB.setTextColor(getResources().getColor(R.color.white_text_button));
                        select.setImageResource(R.drawable.locked);
                        buyActions.remove(a);
                        TextView spV = ((TextView) findViewById(R.id.spLeft));
                        int sp = Integer.decode(spV.getText().toString());
                        sp += a.cost;
                        spV.setText(Html.fromHtml(colorSP(sp)));
                    }
                    else{
                        TextView spV = ((TextView) findViewById(R.id.spLeft));
                        int sp = Integer.decode(spV.getText().toString());
                        if(sp>= a.cost){
                            nameB.setTextColor(getResources().getColor(R.color.dark_gold));
                            select.setImageResource(R.drawable.unlocked);
                            buyActions.add(a);
                            sp -= a.cost;
                            spV.setText(Html.fromHtml(colorSP(sp)));
                        }
                    }
                }
            });

            newView.addView(selHold, paramsS);
            selHold.addView(select, paramsIB);


        }
        overView.invalidate();
        overView.requestLayout();
    }



    public void saveBuyActions(View v){
        try{
            UserProfile up = UserProfile.getInstance();
            CharacterPlayer cur = up.curChar;
            for(Action a : buyActions){
                cur.actions.add(a);
            }
            cur.spSpent += cur.sp - Integer.parseInt(((TextView) findViewById(R.id.spLeft)).getText().toString());
            cur.sp = Integer.parseInt(((TextView) findViewById(R.id.spLeft)).getText().toString());
            up.saveChar();
            setContentView(R.layout.actions_menu);
            prepareActionsMenu();
        }
        catch(Exception e){
            Log.e("IO exception in customizeAct, saveBuyActions", e.getMessage());
        }
    }




    public void prepareViewActions() {
        view = "viewActions";
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        Typeface font3 = Typeface.createFromAsset(getAssets(), "MTCORSVA.TTF");
        ((TextView) findViewById(R.id.viewActionsTitle)).setTypeface(font);
        final CharacterPlayer cur;
        try {
            cur = UserProfile.getInstance().curChar;
        } catch (Exception e) {
            Log.e("Failed to load User Profile in customize act, prepareViewActions: ", e.getMessage());
            return;
        }
        ArrayList<Action> actions = (ArrayList) cur.actions.clone();

        Typeface font2 = Typeface.createFromAsset(getAssets(), "BOD_BLAR.TTF");
        LinearLayout overView = (LinearLayout) findViewById(R.id.charActionsOptions);
        overView.removeAllViews();

        final int size = actions.size();
        for(int i = 0; i< size ; i++){
            final int r = i+1;
            final int r2 = i+1000;
            final int r3 = i+1000000;
            final int r4 = i+10000000;

            final Action a = actions.get(i).getCopy();
            final RelativeLayout newView = new RelativeLayout(this);

            final Button nameB = new Button(this);

            nameB.setId(r3);
            if(i%2 == 0)
                nameB.setBackground( getResources().getDrawable(R.drawable.brush1_button));
            else
                nameB.setBackground( getResources().getDrawable(R.drawable.brush1flip_button));
            nameB.setPadding(0, 0, 0, 0);

            //set layout's height to be small
            LinearLayout.LayoutParams newViewP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
            //newView.setLayoutParams(newViewP);
            overView.addView(newView, newViewP);
            newView.setId(r);

            //set name of perk button
            nameB.setText(a.name);
            nameB.setTypeface(font2);
            nameB.setTextColor(getResources().getColorStateList(R.color.white_text_button));


            //set perk button in top left of relativelayout and define its width
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -30, getResources().getDisplayMetrics());
            //nameB.setLayoutParams(params);

            //add event listener to expand layout when button is clicked
            nameB.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    int h = newView.getLayoutParams().height;
                    if(h == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())) {
                        newView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        newView.requestLayout();
                    }
                    else {
                        newView.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
                        newView.requestLayout();
                    }
                }
            });

            newView.addView(nameB, params);

            //add text view for perk flavor
            TextView tv = new TextView(this);
            tv.setText(Html.fromHtml(a.getReqDesc()+a.getDescriptionOOC()));
            tv.setTypeface(font);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            tv.setId(r4);

            //set flavor in left under the button
            RelativeLayout.LayoutParams paramsT = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsT.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            paramsT.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsT.addRule(RelativeLayout.BELOW, r3);

            //tv.setLayoutParams(paramsT);
            newView.addView(tv, paramsT);

            //add text view for perk flavor
            TextView tv2 = new TextView(this);
            tv2.setText(Html.fromHtml(a.getFlavor()));
            tv2.setTypeface(font3);
            tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);

            //set flavor in left under the button
            RelativeLayout.LayoutParams paramsT2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsT2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            paramsT.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsT2.addRule(RelativeLayout.BELOW, r4);
            //tv.setLayoutParams(paramsT);
            newView.addView(tv2, paramsT2);

        }
        overView.invalidate();
        overView.requestLayout();
    }











    public void prepareClasses(){
        view = "classes";
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        spV.setTypeface(font);

        ((TextView) findViewById(R.id.classTitle)).setTypeface(font);

        ((Button) findViewById(R.id.saveClasses)).setTypeface(font);

        Typeface font3 = Typeface.createFromAsset(getAssets(), "MTCORSVA.TTF");


        try{
            stats = UserProfile.getInstance().curChar.getStats();
            int sp = UserProfile.getInstance().curChar.sp;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            Iterator<Stat> it = stats.keySet().iterator();
            while(it.hasNext()) {
                Stat name = it.next();
                switch (name) {
                    case NINJA:
                        ((TextView) findViewById(R.id.numNin)).setText(""+stats.get(name));
                        break;
                    case MARKSMAN:
                        ((TextView) findViewById(R.id.numMarks)).setText(""+stats.get(name));
                        break;
                    case WARRIOR:
                        ((TextView) findViewById(R.id.numWar)).setText(""+stats.get(name));
                        break;
                    case ACROBAT:
                        ((TextView) findViewById(R.id.numAcro)).setText(""+stats.get(name));
                        break;
                    case ELEMENTAL:
                        ((TextView) findViewById(R.id.numWiz)).setText("" + stats.get(name));
                        break;
                    case ENGINEER:
                        ((TextView) findViewById(R.id.numEng)).setText("" + stats.get(name));
                        break;
                    case MAD_DOCTOR:
                        ((TextView) findViewById(R.id.numMed)).setText("" + stats.get(name));
                        break;
                    case MAGICIAN:
                        ((TextView) findViewById(R.id.numHyp)).setText("" + stats.get(name));
                        break;
                }
            }
        }
        catch(Exception e){
            Log.e("IO exception in customizeAct, prepareClasses", e.getMessage());
        }


        String[] classes = {"Wiz", "Hyp", "War", "Nin", "Eng", "Acro", "Med", "Marks"};
        final customizeAct me = this;
        for(final String curClass : classes){


            Resources res = getResources();
            int bid = res.getIdentifier("in" + curClass + "M", "id", "shenronproductions.app1");
            int vid = res.getIdentifier("view"+curClass, "id", "shenronproductions.app1");
            int tid = res.getIdentifier("text"+curClass, "id", "shenronproductions.app1");
            int t1id = res.getIdentifier("text1"+curClass, "id", "shenronproductions.app1");
            int sid = res.getIdentifier(curClass+"des", "string", "shenronproductions.app1");
            int nid = res.getIdentifier("num"+curClass, "id", "shenronproductions.app1");
            int dbid = res.getIdentifier("des"+curClass, "id", "shenronproductions.app1");

            ((Button) findViewById(vid).findViewById(dbid)).setTypeface(font);

            TextView txt = (TextView) findViewById(tid);
            txt.setTypeface(font);
            String customColorText = getResources().getString(sid);
            txt.setText(Html.fromHtml(customColorText));

            TextView txt1 = (TextView) findViewById(t1id);
            txt1.setTypeface(font3);

            TextView num = (TextView) findViewById(nid);
            num.setTypeface(font);

            Button but = (Button) findViewById(vid).findViewById(bid);

            new ContinuousLongClickListener(but, new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    java.lang.reflect.Method method;
                    try {
                        method = me.getClass().getMethod("dec"+curClass, View.class);
                        method.invoke(me, v);
                    } catch (Exception e) {
                        // ...
                    }
                    return true;
                }
            });

            int bid2 = res.getIdentifier("in" + curClass + "P", "id", "shenronproductions.app1");
            int vid2 = res.getIdentifier("view"+curClass, "id", "shenronproductions.app1");

            Button but2 = (Button) findViewById(vid2).findViewById(bid2);
            new ContinuousLongClickListener(but2, new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    java.lang.reflect.Method method;
                    try {
                        method = me.getClass().getMethod("inc"+curClass, View.class);
                        method.invoke(me, v);
                    } catch (Exception e) {
                        // ...
                    }
                    return true;
                }
            });
        }

    }









    public void incWar(View v){
        CharacterPlayer c = UserProfile.getInstance().curChar;
        ArrayList<Perk> perks = c.perks;
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewWar)).findViewById(R.id.numWar));
        int lvl = Integer.decode(lvlV.getText().toString());
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        int sp = Integer.decode(spV.getText().toString());
        for(Perk p : perks)
            p.statelessUnApply(c);
        if (sp>0 && lvl<100){
            for(Perk p : perks)
                p.statelessApply(c);
            sp--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvl++;
            lvlV.setText(lvl + "");
        }
        else
            for(Perk p : perks)
                p.statelessApply(c);
    }
    public void incWiz(View v){
        CharacterPlayer c = UserProfile.getInstance().curChar;
        ArrayList<Perk> perks = c.perks;
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewWiz)).findViewById(R.id.numWiz));
        int lvl = Integer.decode(lvlV.getText().toString());
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        int sp = Integer.decode(spV.getText().toString());
        for(Perk p : perks)
            p.statelessUnApply(c);
        if (sp>0 && lvl<100){
            for(Perk p : perks)
                p.statelessApply(c);
            sp--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvl++;
            lvlV.setText(lvl + "");
        }
        else
            for(Perk p : perks)
                p.statelessApply(c);
    }
    public void incHyp(View v){
        CharacterPlayer c = UserProfile.getInstance().curChar;
        ArrayList<Perk> perks = c.perks;
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewHyp)).findViewById(R.id.numHyp));
        int lvl = Integer.decode(lvlV.getText().toString());
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        int sp = Integer.decode(spV.getText().toString());
        for(Perk p : perks)
            p.statelessUnApply(c);
        if (sp>0 && lvl<100){
            for(Perk p : perks)
                p.statelessApply(c);
            sp--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvl++;
            lvlV.setText(lvl + "");
        }
        else
            for(Perk p : perks)
                p.statelessApply(c);
    }
    public void incEng(View v){
        CharacterPlayer c = UserProfile.getInstance().curChar;
        ArrayList<Perk> perks = c.perks;
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewEng)).findViewById(R.id.numEng));
        int lvl = Integer.decode(lvlV.getText().toString());
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        int sp = Integer.decode(spV.getText().toString());
        for(Perk p : perks)
            p.statelessUnApply(c);
        if (sp>0 && lvl<100){
            for(Perk p : perks)
                p.statelessApply(c);
            sp--;
            lvl++;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
        else
            for(Perk p : perks)
                p.statelessApply(c);
    }
    public void incAcro(View v){
        CharacterPlayer c = UserProfile.getInstance().curChar;
        ArrayList<Perk> perks = c.perks;
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewAcro)).findViewById(R.id.numAcro));
        int lvl = Integer.decode(lvlV.getText().toString());
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        int sp = Integer.decode(spV.getText().toString());
        for(Perk p : perks)
            p.statelessUnApply(c);
        if (sp>0 && lvl<100){
            for(Perk p : perks)
                p.statelessApply(c);
            sp--;
            lvl++;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
        else
            for(Perk p : perks)
                p.statelessApply(c);
    }
    public void incMarks(View v){
        CharacterPlayer c = UserProfile.getInstance().curChar;
        ArrayList<Perk> perks = c.perks;
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewMarks)).findViewById(R.id.numMarks));
        int lvl = Integer.decode(lvlV.getText().toString());
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        int sp = Integer.decode(spV.getText().toString());
        for(Perk p : perks)
            p.statelessUnApply(c);
        if (sp>0 && lvl<100){
            for(Perk p : perks)
                p.statelessApply(c);
            sp--;
            lvl++;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
        else
            for(Perk p : perks)
                p.statelessApply(c);
    }
    public void incMed(View v){
        CharacterPlayer c = UserProfile.getInstance().curChar;
        ArrayList<Perk> perks = c.perks;
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewMed)).findViewById(R.id.numMed));
        int lvl = Integer.decode(lvlV.getText().toString());
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        int sp = Integer.decode(spV.getText().toString());
        for(Perk p : perks)
            p.statelessUnApply(c);
        if (sp>0 && lvl<100){
            for(Perk p : perks)
                p.statelessApply(c);
            sp--;
            lvl++;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
        else
            for(Perk p : perks)
                p.statelessApply(c);
    }
    public void incNin(View v){
        CharacterPlayer c = UserProfile.getInstance().curChar;
        ArrayList<Perk> perks = c.perks;
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewNin)).findViewById(R.id.numNin));
        int lvl = Integer.decode(lvlV.getText().toString());
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        int sp = Integer.decode(spV.getText().toString());
        for(Perk p : perks)
            p.statelessUnApply(c);
        if (sp>0 && lvl<100){
            for(Perk p : perks)
                p.statelessApply(c);
            sp--;
            lvl++;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
        else
            for(Perk p : perks)
                p.statelessApply(c);
    }

    public void decWiz(View v){
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewWiz)).findViewById(R.id.numWiz));
        int lvl = Integer.decode(lvlV.getText().toString());
        if (lvl>stats.get(Stat.ELEMENTAL)){
            TextView spV = ((TextView) findViewById(R.id.spLeft));
            int sp = Integer.decode(spV.getText().toString());
            sp++;
            lvl--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
    }
    public void decWar(View v){
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewWar)).findViewById(R.id.numWar));
        int lvl = Integer.decode(lvlV.getText().toString());
        if (lvl>stats.get(Stat.WARRIOR)){
            TextView spV = ((TextView) findViewById(R.id.spLeft));
            int sp = Integer.decode(spV.getText().toString());
            sp++;
            lvl--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
    }
    public void decHyp(View v){
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewHyp)).findViewById(R.id.numHyp));
        int lvl = Integer.decode(lvlV.getText().toString());
        if (lvl>stats.get(Stat.MAGICIAN)){
            TextView spV = ((TextView) findViewById(R.id.spLeft));
            int sp = Integer.decode(spV.getText().toString());
            sp++;
            lvl--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
    }
    public void decMarks(View v){
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewMarks)).findViewById(R.id.numMarks));
        int lvl = Integer.decode(lvlV.getText().toString());
        if (lvl>stats.get(Stat.MARKSMAN)){
            TextView spV = ((TextView) findViewById(R.id.spLeft));
            int sp = Integer.decode(spV.getText().toString());
            sp++;
            lvl--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
    }
    public void decMed(View v){
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewMed)).findViewById(R.id.numMed));
        int lvl = Integer.decode(lvlV.getText().toString());
        if (lvl>stats.get(Stat.MAD_DOCTOR)){
            TextView spV = ((TextView) findViewById(R.id.spLeft));
            int sp = Integer.decode(spV.getText().toString());
            sp++;
            lvl--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
    }
    public void decNin(View v){
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewNin)).findViewById(R.id.numNin));
        int lvl = Integer.decode(lvlV.getText().toString());
        if (lvl>stats.get(Stat.NINJA)){
            TextView spV = ((TextView) findViewById(R.id.spLeft));
            int sp = Integer.decode(spV.getText().toString());
            sp++;
            lvl--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
    }
    public void decEng(View v){
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewEng)).findViewById(R.id.numEng));
        int lvl = Integer.decode(lvlV.getText().toString());
        if (lvl>stats.get(Stat.ENGINEER)){
            TextView spV = ((TextView) findViewById(R.id.spLeft));
            int sp = Integer.decode(spV.getText().toString());
            sp++;
            lvl--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
    }
    public void decAcro(View v){
        TextView lvlV = ((TextView) ((RelativeLayout) findViewById(R.id.viewAcro)).findViewById(R.id.numAcro));
        int lvl = Integer.decode(lvlV.getText().toString());
        if (lvl>stats.get(Stat.ACROBAT)){
            TextView spV = ((TextView) findViewById(R.id.spLeft));
            int sp = Integer.decode(spV.getText().toString());
            sp++;
            lvl--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
    }

    public void viewWar(View v){
        RelativeLayout view = (RelativeLayout) findViewById(R.id.viewWar);
        int h = view.getLayoutParams().height;
        if(h == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())) {
            view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.requestLayout();
        }
        else {
            view.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            view.requestLayout();
        }
    }
    public void viewWiz(View v){
        RelativeLayout view = (RelativeLayout) findViewById(R.id.viewWiz);
        int h = view.getLayoutParams().height;
        if(h == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())) {
            view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.requestLayout();
        }
        else {
            view.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            view.requestLayout();
        }
    }
    public void viewHyp(View v){
        RelativeLayout view = (RelativeLayout) findViewById(R.id.viewHyp);
        int h = view.getLayoutParams().height;
        if(h == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())) {
            view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.requestLayout();
        }
        else {
            view.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            view.requestLayout();
        }
    }
    public void viewNin(View v){
        RelativeLayout view = (RelativeLayout) findViewById(R.id.viewNin);
        int h = view.getLayoutParams().height;
        if(h == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())) {
            view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.requestLayout();
        }
        else {
            view.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            view.requestLayout();
        }
    }
    public void viewEng(View v){
        RelativeLayout view = (RelativeLayout) findViewById(R.id.viewEng);
        int h = view.getLayoutParams().height;
        if(h == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())) {
            view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.requestLayout();
        }
        else {
            view.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            view.requestLayout();
        }
    }
    public void viewAcro(View v){
        RelativeLayout view = (RelativeLayout) findViewById(R.id.viewAcro);
        int h = view.getLayoutParams().height;
        if(h == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())) {
            view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.requestLayout();
        }
        else {
            view.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            view.requestLayout();
        }
    }
    public void viewMed(View v){
        RelativeLayout view = (RelativeLayout) findViewById(R.id.viewMed);
        int h = view.getLayoutParams().height;
        if(h == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())) {
            view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.requestLayout();
        }
        else {
            view.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            view.requestLayout();
        }
    }
    public void viewMarks(View v){
        RelativeLayout view = (RelativeLayout) findViewById(R.id.viewMarks);
        int h = view.getLayoutParams().height;
        if(h == (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())) {
            view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.requestLayout();
        }
        else {
            view.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            view.requestLayout();
        }
    }

    public void saveClasses(View v) {
        stats.put(Stat.ELEMENTAL, Integer.parseInt(((TextView) findViewById(R.id.numWiz)).getText().toString()));
        stats.put(Stat.WARRIOR, Integer.parseInt(((TextView) findViewById(R.id.numWar)).getText().toString()));
        stats.put(Stat.MAGICIAN, Integer.parseInt(((TextView) findViewById(R.id.numHyp)).getText().toString()));
        stats.put(Stat.ACROBAT, Integer.parseInt(((TextView) findViewById(R.id.numAcro)).getText().toString()));
        stats.put(Stat.ENGINEER, Integer.parseInt(((TextView) findViewById(R.id.numEng)).getText().toString()));
        stats.put(Stat.MARKSMAN, Integer.parseInt(((TextView) findViewById(R.id.numMarks)).getText().toString()));
        stats.put(Stat.MAD_DOCTOR, Integer.parseInt(((TextView) findViewById(R.id.numMed)).getText().toString()));
        stats.put(Stat.NINJA, Integer.parseInt(((TextView) findViewById(R.id.numNin)).getText().toString()));
        try{
            UserProfile up = UserProfile.getInstance();
            CharacterPlayer cur = up.curChar;
            cur.setStats(stats);
            cur.spSpent += cur.sp - Integer.parseInt(((TextView) findViewById(R.id.spLeft)).getText().toString());
            cur.sp = Integer.parseInt(((TextView) findViewById(R.id.spLeft)).getText().toString());
            up.saveChar();
            setContentView(R.layout.customize_menu);
            prepareMain();
        }
        catch(Exception e){
            Log.e("IO exception in customizeAct, saveClasses", e.getMessage());
        }
    }






    public void prepareStats(){
        view = "stats";
        Typeface font = Typeface.createFromAsset(getAssets(), "njnaruto.ttf");
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        spV.setTypeface(font);

        ((TextView) findViewById(R.id.statsTitle)).setTypeface(font);

        ((Button) findViewById(R.id.saveStats)).setTypeface(font);


        try{
            stats = UserProfile.getInstance().curChar.getStats();
            int sp = UserProfile.getInstance().curChar.sp;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            Iterator<Stat> it = stats.keySet().iterator();
            while(it.hasNext()) {
                Stat name = it.next();
                switch (name) {
                    case MAX_MANA:
                        ((TextView) findViewById(R.id.numMana)).setText(""+stats.get(name));
                        break;
                    case MAX_STAMINA:
                        ((TextView) findViewById(R.id.numStamina)).setText(""+stats.get(name));
                        break;
                    case MAX_HEALTH:
                        ((TextView) findViewById(R.id.numHealth)).setText(""+stats.get(name));
                        break;
                    case MAX_FOCUS:
                        ((TextView) findViewById(R.id.numFocus)).setText(""+stats.get(name));
                        break;
                }
            }
        }
        catch(Exception e){
            Log.e("IO exception in customizeAct, prepareStats", e.getMessage());
        }


        String[] stats = {"Mana", "Health", "Stamina", "Focus"};
        final customizeAct me = this;
        for(final String curStat : stats){

            Resources res = getResources();
            int mid = res.getIdentifier("in" + curStat + "M", "id", "shenronproductions.app1");
            int pid = res.getIdentifier("in" + curStat + "P", "id", "shenronproductions.app1");
            int namid = res.getIdentifier("name"+curStat, "id", "shenronproductions.app1");
            int numid = res.getIdentifier("num"+curStat, "id", "shenronproductions.app1");

            ((TextView) findViewById(namid)).setTypeface(font);
            ((TextView) findViewById(numid)).setTypeface(font);


            Button but = (Button) findViewById(mid);

            new ContinuousLongClickListener(but, new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    java.lang.reflect.Method method;
                    try {
                        method = me.getClass().getMethod("dec"+curStat, View.class);
                        method.invoke(me, v);
                    } catch (Exception e) {
                        // ...
                    }
                    return true;
                }
            });

            Button but2 = (Button) findViewById(pid);

            new ContinuousLongClickListener(but2, new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    java.lang.reflect.Method method;
                    try {
                        method = me.getClass().getMethod("inc"+curStat, View.class);
                        method.invoke(me, v);
                    } catch (Exception e) {
                        // ...
                    }
                    return true;
                }
            });
        }

    }

    public void incMana(View v){
        CharacterPlayer c = UserProfile.getInstance().curChar;
        ArrayList<Perk> perks = c.perks;
        TextView lvlV = (TextView) findViewById(R.id.numMana);
        int lvl = Integer.decode(lvlV.getText().toString());
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        int sp = Integer.decode(spV.getText().toString());
        for(Perk p : perks)
            p.statelessUnApply(c);
        if (sp>0 && lvl<300){
            for(Perk p : perks)
                p.statelessApply(c);
            sp--;
            lvl++;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
        else
            for(Perk p : perks)
                p.statelessApply(c);
    }

    public void decMana(View v){
        TextView lvlV = (TextView) findViewById(R.id.numMana);
        int lvl = Integer.decode(lvlV.getText().toString());
        if (lvl>stats.get(Stat.MAX_MANA)){
            TextView spV = ((TextView) findViewById(R.id.spLeft));
            int sp = Integer.decode(spV.getText().toString());
            sp++;
            lvl--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
    }
    public void incHealth(View v){
        CharacterPlayer c = UserProfile.getInstance().curChar;
        ArrayList<Perk> perks = c.perks;
        TextView lvlV = (TextView) findViewById(R.id.numHealth);
        int lvl = Integer.decode(lvlV.getText().toString());
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        int sp = Integer.decode(spV.getText().toString());
        for(Perk p : perks)
            p.statelessUnApply(c);
        if (sp>0 && lvl<300){
            for(Perk p : perks)
                p.statelessApply(c);
            sp--;
            lvl++;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
        else
            for(Perk p : perks)
                p.statelessApply(c);
    }

    public void decHealth(View v){
        TextView lvlV = (TextView) findViewById(R.id.numHealth);
        int lvl = Integer.decode(lvlV.getText().toString());
        if (lvl>stats.get(Stat.MAX_HEALTH)){
            TextView spV = ((TextView) findViewById(R.id.spLeft));
            int sp = Integer.decode(spV.getText().toString());
            sp++;
            lvl--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
    }
    public void incFocus(View v){
        CharacterPlayer c = UserProfile.getInstance().curChar;
        ArrayList<Perk> perks = c.perks;
        TextView lvlV = (TextView) findViewById(R.id.numFocus);
        int lvl = Integer.decode(lvlV.getText().toString());
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        int sp = Integer.decode(spV.getText().toString());
        for(Perk p : perks)
            p.statelessUnApply(c);
        if (sp>0 && lvl<300){
            for(Perk p : perks)
                p.statelessApply(c);
            sp--;
            lvl++;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
        else
            for(Perk p : perks)
                p.statelessApply(c);
    }

    public void decFocus(View v) {
        TextView lvlV = (TextView) findViewById(R.id.numFocus);
        int lvl = Integer.decode(lvlV.getText().toString());
        if (lvl > stats.get(Stat.MAX_FOCUS)) {
            TextView spV = ((TextView) findViewById(R.id.spLeft));
            int sp = Integer.decode(spV.getText().toString());
            sp++;
            lvl--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
    }
    public void incStamina(View v){
        CharacterPlayer c = UserProfile.getInstance().curChar;
        ArrayList<Perk> perks = c.perks;
        TextView lvlV = (TextView) findViewById(R.id.numStamina);
        int lvl = Integer.decode(lvlV.getText().toString());
        TextView spV = ((TextView) findViewById(R.id.spLeft));
        int sp = Integer.decode(spV.getText().toString());
        for(Perk p : perks)
            p.statelessUnApply(c);
        if (sp>0 && lvl<300){
            for(Perk p : perks)
                p.statelessApply(c);
            sp--;
            lvl++;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
        else
            for(Perk p : perks)
                p.statelessApply(c);
    }

    public void decStamina(View v){
        TextView lvlV = (TextView) findViewById(R.id.numStamina);
        int lvl = Integer.decode(lvlV.getText().toString());
        if (lvl>stats.get(Stat.MAX_STAMINA)){
            TextView spV = ((TextView) findViewById(R.id.spLeft));
            int sp = Integer.decode(spV.getText().toString());
            sp++;
            lvl--;
            String spLeft = colorSP(sp);
            spV.setText(Html.fromHtml(spLeft));
            lvlV.setText(lvl + "");
        }
    }

    public void saveStats(View v) {
        stats.put(Stat.MAX_MANA, Integer.parseInt(((TextView) findViewById(R.id.numMana)).getText().toString()));
        stats.put(Stat.MAX_STAMINA, Integer.parseInt(((TextView) findViewById(R.id.numStamina)).getText().toString()));
        stats.put(Stat.MAX_HEALTH, Integer.parseInt(((TextView) findViewById(R.id.numHealth)).getText().toString()));
        stats.put(Stat.MAX_FOCUS, Integer.parseInt(((TextView) findViewById(R.id.numFocus)).getText().toString()));
        try{
            UserProfile up = UserProfile.getInstance();
            CharacterPlayer cur = up.curChar;
            cur.setStats(stats);
            cur.spSpent += cur.sp - Integer.parseInt(((TextView) findViewById(R.id.spLeft)).getText().toString());
            cur.sp = Integer.parseInt(((TextView) findViewById(R.id.spLeft)).getText().toString());
            up.saveChar();
            setContentView(R.layout.customize_menu);
            prepareMain();
        }
        catch(Exception e){
            Log.e("IO exception in customizeAct, saveStats", e.getMessage());
        }
    }


}

