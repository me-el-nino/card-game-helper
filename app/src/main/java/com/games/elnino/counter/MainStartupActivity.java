package com.games.elnino.counter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

@SuppressWarnings({"ConstantConditions","FieldCanBeLocal"})
public class MainStartupActivity extends AppCompatActivity {

    public final static String GOAL_POINTS = "GOAL_POINTS";
    public final static String PLAYER_LIST = "PLAYER_LIST";
    public final static String ALL_NEED_TO_FINISH = "ALL_NEED_TO_FINISH";
    public final static String FINISH_BY_POINTS = "FINISH_BY_POINTS";
    public final static String COUNT_TO_ZERO = "COUNT_TO_ZERO";
    public final static String GAME_CHOSEN ="GAME_CHOSEN";

    //to add more games, add it to string resource, array resource, getDropDownMenuListener() and setGameRules()
    //for specific rules in PlayersActivity.applyExtraRules
    private String[] gameList;

    private ArrayList<String> playerList = new ArrayList<>();

    private LinearLayout playerListView;
    private TextView messagePrint;
    private RadioGroup winRequirements;
    private RadioButton winByRounds;
    private RadioButton winByPoints;
    private CheckBox countToZero;
    private CheckBox allToFinish;
    private Button playerNameEnter;
    private EditText playerNameInput;
    private Spinner dropDownMenu;
    private Button startGame;
    private EditText pointsOrRoundsInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_startup);

        gameList = getResources().getStringArray(R.array.GameList);

        //get all necessary objects from ids
        winRequirements = (RadioGroup) findViewById(R.id.winRequirements);
        winByRounds = (RadioButton) findViewById(R.id.winByRounds);
        winByPoints = (RadioButton) findViewById(R.id.winByPoints);
        countToZero = (CheckBox) findViewById(R.id.countToZero);
        allToFinish = (CheckBox) findViewById(R.id.allToFinish);
        playerNameEnter = (Button) findViewById(R.id.playerNameEnter);
        playerNameInput = (EditText) findViewById(R.id.playerNamesInput);
        dropDownMenu = (Spinner) findViewById(R.id.dropDownMenu);
        startGame = (Button) findViewById(R.id.startGame);
        pointsOrRoundsInput = (EditText) findViewById(R.id.pointsOrRoundsInput);
        messagePrint = (TextView) findViewById(R.id.textView);
        playerListView = (LinearLayout) findViewById(R.id.scrollViewLayout);

        winRequirements.setOnCheckedChangeListener(getWinRequirementsChangedListener());

        countToZero.setOnClickListener(getCountToZeroOnClickListener());

        // not yet working as intended, focus leaves text edit, when enter is pressed
        /*
        playerNameInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    inputPlayer();
                    //playerNameInput.invalidate();
                    //playerNameInput.performClick();
                    return true;
                }
                return false;
            }
        });
        */

        playerNameEnter.setOnClickListener(getPlayerEnteredListener());

        startGame.setOnClickListener(getStartListener());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, gameList);
        dropDownMenu.setAdapter(adapter);
        dropDownMenu.setOnItemSelectedListener(getDropDownMenuListener());

        winByPoints.toggle();
    }

    private RadioGroup.OnCheckedChangeListener getWinRequirementsChangedListener(){
        return new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.winByRounds:
                        allToFinish.setVisibility(View.INVISIBLE);
                        allToFinish.setChecked(false);
                        countToZero.setVisibility(View.INVISIBLE);
                        countToZero.setChecked(false);
                        pointsOrRoundsInput.setHint(getResources().getString(R.string.RoundsInputHint));
                        break;
                    case R.id.winByPoints:
                        allToFinish.setVisibility(View.VISIBLE);
                        countToZero.setVisibility(View.VISIBLE);
                        pointsOrRoundsInput.setHint(getResources().getString(R.string.PointsInputHint));
                        break;
                }
            }
        };
    }

    private View.OnClickListener getCountToZeroOnClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countToZero.isChecked()) {
                    pointsOrRoundsInput.setHint(getResources().getString(R.string.CountToZeroHint));
                } else {
                    pointsOrRoundsInput.setHint(getResources().getString(R.string.PointsInputHint));
                }
            }
        };
    }

    private View.OnClickListener getPlayerEnteredListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputted = String.valueOf(playerNameInput.getText());
                if ((inputted.length() > 0) && !playerList.contains(inputted)) {
                    if (inputted.length() <= 10) {
                        playerList.add(inputted);
                        final Switch temporarySwitch = new Switch(MainStartupActivity.this);
                        temporarySwitch.setText(inputted);
                        temporarySwitch.setChecked(true);
                        temporarySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (!isChecked) {
                                    playerList.remove(temporarySwitch.getText().toString());
                                    playerListView.removeView(temporarySwitch);
                                }
                            }
                        });
                        playerListView.addView(temporarySwitch);
                        playerNameInput.setText(getResources().getString(R.string.EmptyString));
                        messagePrint.setText(getResources().getString(R.string.EmptyString));
                    } else {
                        messagePrint.setText(getResources().getString(R.string.NameTooLongError));
                    }
                } else {
                    messagePrint.setText(getResources().getString(R.string.NameDuplicateError));
                }
            }
        };
    }

    private View.OnClickListener getStartListener() {
        return new View.OnClickListener(){
            private boolean finishByPoints;
            private boolean allNeedToReachPoints;
            private boolean countPointsToZero;
            private int points = 0;
            private int game_chosen;

            @Override
            public void onClick(View v) {
                playerList.trimToSize();

                setGameRules();

                if (points > 0) {
                    if (!playerList.isEmpty()) {
                        Intent intent = new Intent(MainStartupActivity.this, PlayersActivity.class);
                        intent.putExtra(GOAL_POINTS, points);
                        intent.putStringArrayListExtra(PLAYER_LIST, playerList);
                        intent.putExtra(ALL_NEED_TO_FINISH, allNeedToReachPoints);
                        intent.putExtra(FINISH_BY_POINTS, finishByPoints);
                        intent.putExtra(COUNT_TO_ZERO,countPointsToZero);
                        intent.putExtra(GAME_CHOSEN,game_chosen);
                        startActivity(intent);
                    } else {
                        messagePrint.setText(getResources().getString(R.string.NotEnoughPlayersError));
                    }
                } else {
                    messagePrint.setText(getResources().getString(R.string.NumberInputError));
                }
            }

            private void setGameRules(){
                if (dropDownMenu.getSelectedItem().toString().equals(gameList[1])) {
                    // 20 ab
                    finishByPoints = true;
                    allNeedToReachPoints = false;
                    countPointsToZero = true;
                    points = 20;
                    game_chosen = 1;
                } else {
                    // custom game
                    finishByPoints = winByPoints.isChecked();
                    allNeedToReachPoints = allToFinish.isChecked();
                    countPointsToZero = countToZero.isChecked();
                    try {
                        points = Integer.parseInt(pointsOrRoundsInput.getText().toString());
                    } catch (Exception e){
                        //happens if no number is entered
                    }
                    game_chosen = 0;
                }
            }
        };
    }

    private AdapterView.OnItemSelectedListener getDropDownMenuListener(){
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // custom game
                        pointsOrRoundsInput.setVisibility(View.VISIBLE);
                        winByRounds.setVisibility(View.VISIBLE);
                        winByPoints.setVisibility(View.VISIBLE);
                        allToFinish.setVisibility(View.VISIBLE);
                        countToZero.setVisibility(View.VISIBLE);
                        break;
                    case 1: // 20 ab
                        pointsOrRoundsInput.setVisibility(View.INVISIBLE);
                        winByRounds.setVisibility(View.INVISIBLE);
                        winByPoints.setVisibility(View.INVISIBLE);
                        allToFinish.setVisibility(View.INVISIBLE);
                        countToZero.setVisibility(View.INVISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing, not sure how this can happen
            }
        };
    }
}