package com.games.elnino.counter;

public class roundSaver {

    private int points;
    private boolean thisOneFinished;
    private boolean moreThanOneFinished;

    public roundSaver(int points, boolean thisOneFinished, boolean moreThanOneFinished) {
        this.points = points;
        this.thisOneFinished = thisOneFinished;
        this.moreThanOneFinished = moreThanOneFinished;
    }


    public int getPoints() {
        return points;
    }

    public boolean hasThisOneFinished() {
        return thisOneFinished;
    }

    public void setThisOneFinished(boolean thisOneFinished) {
        this.thisOneFinished = thisOneFinished;
    }

    public boolean hasMoreThanOneFinished() {
        return moreThanOneFinished;
    }

    public void setMoreThanOneFinished(boolean moreThanOneFinished) {
        this.moreThanOneFinished = moreThanOneFinished;
    }
}
