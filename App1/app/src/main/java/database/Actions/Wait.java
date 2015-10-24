package database.Actions;

import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import Utilities.Callable;

import Managers.GameManager;
import Managers.Logic.LogicCalc;
import Managers.timeKeeper;
import Utilities.Stat;
import database.Actions.ActionSteps.ActionStep;
import database.Actions.SubActions.SubAction;
import database.Coord;
import database.Requirements.Requirement;
import database.State;
import shenronproductions.app1.R;
import shenronproductions.app1.Activities.gameAct;

/**
 * Created by Dale on 1/1/2015.
 */
public class Wait extends Action {

    public Wait() {
        super("Wait",
                "The user waits. That's it.",
                0,
                0,
                Stat.MISC);


        description.add(new Callable<String>() {
            @Override
            public String call() {
                return "Waits &#160;for &#160;a &#160;specified &#160;number &#160;of &#160;milliseconds";
            }
        });

        description.add(new Callable<String>() {
            public String call() {
                return "At &#160;least &#160;1 &#160;millisecond";
            }
        });

        cost = 0;
        setBuyReq("None");

    }

    @Override
    public Action getCopy(int u) {
        LogicCalc calc = new LogicCalc();
        Wait wt = new Wait();
        wt.curUser = u;
        calc.modAction(wt, u);

        return wt;
    }


    @Override
    public Action getCopy() {
        return new Wait();
    }


    @Override
    public void useAction() {

        final GameManager gm = GameManager.getInstance();
        final gameAct context = gm.getGameAct();
        ((LinearLayout) context.findViewById(R.id.actInOptions)).removeAllViews();
        ((HorizontalScrollView) context.findViewById(R.id.actionsInInnerScroll)).removeAllViews();


        ((TextView) context.findViewById(R.id.actInInfo)).setText("Enter a max number of milliseconds to wait. While waiting, you can click the screen to stop waiting at any moment!");

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.gravity = Gravity.CENTER_HORIZONTAL;


        final Typeface font = Typeface.createFromAsset(context.getAssets(), "njnaruto.ttf");
        LinearLayout.LayoutParams useParams = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics()));

        final EditText waitTime = new EditText(context);
        final Button use = new Button(context);



        waitTime.setTypeface(font);
        waitTime.setInputType(InputType.TYPE_CLASS_NUMBER);
        waitTime.setTextAlignment(EditText.TEXT_ALIGNMENT_CENTER);
        waitTime.setTextColor(context.getResources().getColorStateList(R.color.full_black));
        waitTime.setImeOptions(EditorInfo.IME_ACTION_DONE);
        waitTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        waitTime.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    use.setEnabled(true);
                }
                return false;
            }
        });
        waitTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                use.setEnabled(false);
            }
        });


        container.addView(waitTime, useParams);


        use.setTypeface(font);
        use.setText("Wait");
        use.setEnabled(false);
        use.setTextColor(context.getResources().getColorStateList(R.color.white_text_button));
        use.setBackground(context.getResources().getDrawable(R.drawable.brush1_button));
        use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.takeFoc();
                String input = waitTime.getText().toString();
                if(input.isEmpty()) {
                    use.setEnabled(false);
                    return;
                }
                int i = Integer.parseInt(input);

                waitNow(i);

            }

        });

        container.addView(use, useParams);

        ((LinearLayout) context.findViewById(R.id.actInOptions)).addView(container, containerParams);
    }

    @Override
    public void mapClicked(Coord c) {
        gameAct gc = GameManager.getInstance().getGameAct();

        gc.showMapInfo(null);

        gc.setObjects(c);
    }


    private void waitNow(int i) {
        final GameManager gm = GameManager.getInstance();
        State s = gm.getState();
        timeKeeper tk = gm.getTimeline();

        tk.setCurAction(this);

        int myId =  GameManager.getInstance().getTimeline().getId();

        //create the wait actionstep solely for the purpose of stopping early
        ActionStep waitTill = new ActionStep(myId, "Wait", new ArrayList<Requirement>(), curUser, 0, new ArrayList<SubAction>(), new ArrayList<SubAction>(), 0, Stat.MISC);
        tk.addAction(s.getTime() + i-1, waitTill);

        //this processes to the next available time
        gm.processTime(s.getTime(), s.getTime() + i);
    }

}