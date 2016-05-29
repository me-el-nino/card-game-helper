package com.games.elnino.counter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Stack;

@SuppressWarnings("ConstantConditions")
public class PlayersActivity extends AppCompatActivity {

    ArrayList<String> playerList;
    int goalPoints;
    boolean finishByPoints;
    boolean allNeedToFinish;
    boolean countToZero;
    int gamePlayedId;

    TextView[] allPlayersActualPoints;
    TextView[] allPlayersPointLists;
    TextView[] allPlayersHintView;

    LinearLayout playerActivityLayout;
    EditText inputPoints;
    Button enterPoints;
    Button undoButton;

    int playersOverallPoints[];
    int playerNumberTurn;
    int roundsPlayed;
    boolean finished;
    ArrayList<String> finishedPlayers;
    Stack<roundSaver> undoStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players);

        Intent intent = getIntent();
        playerList = intent.getStringArrayListExtra(MainStartupActivity.PLAYER_LIST);
        goalPoints = intent.getIntExtra(MainStartupActivity.GOAL_POINTS, 0);
        finishByPoints = intent.getBooleanExtra(MainStartupActivity.FINISH_BY_POINTS, false);
        allNeedToFinish = intent.getBooleanExtra(MainStartupActivity.ALL_NEED_TO_FINISH, false);
        countToZero = intent.getBooleanExtra(MainStartupActivity.COUNT_TO_ZERO, false);
        gamePlayedId = intent.getIntExtra(MainStartupActivity.GAME_CHOSEN, 0);

        allPlayersActualPoints = new TextView[playerList.size()];
        allPlayersPointLists = new TextView[playerList.size()];
        allPlayersHintView = new TextView[playerList.size()];

        playerActivityLayout = (LinearLayout) findViewById(R.id.PlayerActivityLayout);
        inputPoints = (EditText) findViewById(R.id.InputPoints);
        enterPoints = (Button) findViewById(R.id.EnterPoints);
        undoButton = (Button) findViewById(R.id.UndoButton);

        for (int i = 0; i < playerList.size(); i++) {
            addPlayersInfo(i);
        }

        playersOverallPoints = new int[playerList.size()];
        if (countToZero) {
            for (int i = 0; i < playersOverallPoints.length; i++) {
                playersOverallPoints[i] = goalPoints;
            }
        } else {
            for (int i = 0; i < playersOverallPoints.length; i++) {
                playersOverallPoints[i] = 0;
            }
        }

        roundsPlayed = 1;
        playerNumberTurn = 0;
        finishedPlayers = new ArrayList<>(playerList.size());
        finished = false;
        undoStack = new Stack<>();

        inputPoints.setHint(getResources().getString(R.string.EnterPointsBeginning) + playerList.get(0));
        enterPoints.setOnClickListener(getEnterPointsListener());

        undoButton.setOnClickListener(getUndoListener());
    }

    private void addPlayersInfo(int i) {
        LinearLayout onePlayerLayout = new LinearLayout(this);
        onePlayerLayout.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(this);
        String actualName = playerList.get(i);

        String temp = getResources().getString(R.string.NewLine) + actualName + getResources().getString(R.string.NewLine);
        name.setText(temp);
        name.setTypeface(null, Typeface.BOLD);
        onePlayerLayout.addView(name);

        final TextView actualPointsView = new TextView(this);
        if (countToZero) {
            temp = getResources().getString(R.string.PointsBeginning) + goalPoints;
        } else {
            temp = getResources().getString(R.string.PointsBeginning) + 0;
        }
        actualPointsView.setText(temp);
        onePlayerLayout.addView(actualPointsView);
        allPlayersActualPoints[i] = actualPointsView;

        final TextView hintView = new TextView(this);
        hintView.setText(getResources().getString(R.string.EmptyString));
        hintView.setTextColor(Color.RED);
        onePlayerLayout.addView(hintView);
        allPlayersHintView[i] = hintView;

        TextView pointListHeader = new TextView(this);
        pointListHeader.setText(getResources().getString(R.string.PointsReceived));
        onePlayerLayout.addView(pointListHeader);

        final TextView pointList = new TextView(this);
        ScrollView pointListScroller = new ScrollView(this);
        pointListScroller.addView(pointList);
        onePlayerLayout.addView(pointListScroller);
        allPlayersPointLists[i] = pointList;

        playerActivityLayout.addView(onePlayerLayout);
    }

    private View.OnClickListener getEnterPointsListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int inputted = 0;
                boolean tryDidWork;
                try {
                    inputted = Integer.parseInt(inputPoints.getText().toString());
                    tryDidWork = true;
                } catch (Exception e) {
                    tryDidWork = false;
                    //happens if no number is entered
                }

                if (tryDidWork) {
                    roundSaver toSave = new roundSaver(playersOverallPoints[playerNumberTurn], false, false);
                    inputPoints.setText(getResources().getString(R.string.EmptyString));

                    String temp = inputted + getResources().getString(R.string.NewLine) + allPlayersPointLists[playerNumberTurn].getText();
                    allPlayersPointLists[playerNumberTurn].setText(temp);

                    if (countToZero) {
                        playersOverallPoints[playerNumberTurn] = playersOverallPoints[playerNumberTurn] - inputted;
                    } else {
                        playersOverallPoints[playerNumberTurn] = playersOverallPoints[playerNumberTurn] + inputted;
                    }

                    temp = getResources().getString(R.string.PointsBeginning) + playersOverallPoints[playerNumberTurn];
                    allPlayersActualPoints[playerNumberTurn].setText(temp);

                    applyExtraRules();

                    if (finishByPoints) {
                        if (allNeedToFinish) {
                            if (countToZero) {
                                if (playersOverallPoints[playerNumberTurn] <= 0) {
                                    toSave.setThisOneFinished(true);
                                    temp = getResources().getString(R.string.Finished) + getResources().getString(R.string.NewLine) + allPlayersPointLists[playerNumberTurn].getText();
                                    allPlayersPointLists[playerNumberTurn].setText(temp);
                                    finishedPlayers.add(playerList.get(playerNumberTurn));
                                }
                            } else {
                                if (playersOverallPoints[playerNumberTurn] >= goalPoints) {
                                    toSave.setThisOneFinished(true);
                                    temp = getResources().getString(R.string.Finished) + getResources().getString(R.string.NewLine) + allPlayersPointLists[playerNumberTurn].getText();
                                    allPlayersPointLists[playerNumberTurn].setText(temp);
                                    finishedPlayers.add(playerList.get(playerNumberTurn));
                                }
                            }
                        } else {
                            if (finished) {
                                toSave.setThisOneFinished(true);
                                temp = getResources().getString(R.string.Finished) + getResources().getString(R.string.NewLine) + allPlayersPointLists[playerNumberTurn].getText();
                                allPlayersPointLists[playerNumberTurn].setText(temp);
                                finishedPlayers.add(playerList.get(playerNumberTurn));
                            } else {
                                if (countToZero) {
                                    if (playersOverallPoints[playerNumberTurn] <= 0) {
                                        toSave.setThisOneFinished(true);
                                        toSave.setMoreThanOneFinished(true);
                                        finished = true;
                                        for (int i = 0; i <= playerNumberTurn; i++) {
                                            temp = getResources().getString(R.string.Finished) + getResources().getString(R.string.NewLine) + allPlayersPointLists[i].getText();
                                            allPlayersPointLists[i].setText(temp);
                                            finishedPlayers.add(playerList.get(i));
                                        }
                                    }
                                } else {
                                    if (playersOverallPoints[playerNumberTurn] >= goalPoints) {
                                        toSave.setThisOneFinished(true);
                                        toSave.setMoreThanOneFinished(true);
                                        finished = true;
                                        for (int i = 0; i <= playerNumberTurn; i++) {
                                            temp = getResources().getString(R.string.Finished) + getResources().getString(R.string.NewLine) + allPlayersPointLists[i].getText();
                                            allPlayersPointLists[i].setText(temp);
                                            finishedPlayers.add(playerList.get(i));
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (roundsPlayed >= goalPoints) {
                            toSave.setThisOneFinished(true);
                            temp = getResources().getString(R.string.Finished) + getResources().getString(R.string.NewLine) + allPlayersPointLists[playerNumberTurn].getText();
                            allPlayersPointLists[playerNumberTurn].setText(temp);
                            finishedPlayers.add(playerList.get(playerNumberTurn));
                        }
                    }
                    undoStack.push(toSave);
                    if (finishedPlayers.size() != playerList.size()) {
                        do {
                            playerNumberTurn = playerNumberTurn + 1;
                            if (playerNumberTurn >= playerList.size()) {
                                playerNumberTurn = 0;
                                roundsPlayed = roundsPlayed + 1;
                            }
                        }
                        while (finishedPlayers.contains(playerList.get(playerNumberTurn)));
                        temp = getResources().getString(R.string.EnterPointsBeginning) + playerList.get(playerNumberTurn);
                        inputPoints.setHint(temp);
                    } else {
                        playerNumberTurn = 0; //so undo gets the last person after finishing
                        enterPoints.setText(getResources().getString(R.string.Finished));
                        inputPoints.setHint(getResources().getString(R.string.GameFinished));
                    }
                }
            }
        };
    }

    private View.OnClickListener getUndoListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!undoStack.empty()) {
                    if (playerNumberTurn == 0) {
                        playerNumberTurn = playerList.size() - 1;
                        if (finishedPlayers.size() != playerList.size()) {
                            roundsPlayed = roundsPlayed - 1;
                        }
                    } else {
                        playerNumberTurn = playerNumberTurn - 1;
                    }

                    roundSaver lastPlayed = undoStack.pop();

                    if (lastPlayed.hasThisOneFinished()) {
                        finishedPlayers.remove(playerList.get(playerNumberTurn));
                        CharSequence textToEdit = allPlayersPointLists[playerNumberTurn].getText();
                        CharSequence textToPutBack = textToEdit.subSequence(9, textToEdit.length());
                        allPlayersPointLists[playerNumberTurn].setText(textToPutBack);
                        if (lastPlayed.hasMoreThanOneFinished()) {
                            finished = false;
                            for (int i = playerNumberTurn - 1; i >= 0; i--) {
                                finishedPlayers.remove(playerList.get(i));
                                CharSequence secondTextToEdit = allPlayersPointLists[i].getText();
                                CharSequence secondTextToPutBack = secondTextToEdit.subSequence(9, secondTextToEdit.length());
                                allPlayersPointLists[i].setText(secondTextToPutBack);
                            }
                        }
                    }

                    playersOverallPoints[playerNumberTurn] = lastPlayed.getPoints();
                    String temp = getResources().getString(R.string.PointsBeginning) + playersOverallPoints[playerNumberTurn];
                    allPlayersActualPoints[playerNumberTurn].setText(temp);

                    applyExtraRules();

                    CharSequence textToEdit = allPlayersPointLists[playerNumberTurn].getText();
                    int i = 0;
                    do {
                        i++;
                    } while (textToEdit.charAt(i) != '\n');
                    CharSequence textToPutBack = textToEdit.subSequence(i + 1, textToEdit.length());
                    allPlayersPointLists[playerNumberTurn].setText(textToPutBack);
                    temp = getResources().getString(R.string.EnterPointsBeginning) + playerList.get(playerNumberTurn);
                    inputPoints.setHint(temp);
                    enterPoints.setText(getResources().getString(R.string.EnterPointsString));
                }
            }
        };
    }

    private void applyExtraRules() {
        switch (gamePlayedId) {
            case 1: // 20 ab
                if (playersOverallPoints[playerNumberTurn] <= 3 && playersOverallPoints[playerNumberTurn] > 0) {
                    allPlayersHintView[playerNumberTurn].setText(getResources().getString(R.string.PlayerIsNotAllowedToSwitchCards));
                }
                else if (playersOverallPoints[playerNumberTurn] <= 5 && playersOverallPoints[playerNumberTurn] > 3) {
                    allPlayersHintView[playerNumberTurn].setText(getResources().getString(R.string.PlayerMustPlay));
                }
                else {
                    allPlayersHintView[playerNumberTurn].setText(getResources().getString(R.string.EmptyString));
                }
                break;
        }
    }
}

