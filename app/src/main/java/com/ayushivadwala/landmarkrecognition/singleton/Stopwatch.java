package com.ayushivadwala.landmarkrecognition.singleton;


import java.util.Observable;


public class Stopwatch extends Observable {

    private static final Stopwatch INSTANCE = new Stopwatch();


    enum State {
        RUNNING,
        STOPPED
    }

    private State state = State.STOPPED;

    private long startTime;

    private long runningTime = 0;

    private Stopwatch() {

    }

    public Stopwatch(long runningTime) {
        this.runningTime = runningTime;
    }

    public static Stopwatch getInstance() {
        return INSTANCE;
    }

    public void start() {
        startTime = System.currentTimeMillis();
        state = State.RUNNING;
        setChanged();
        notifyObservers();
    }

    public void stop() {
        if(state != State.RUNNING) {
            return;
        }
        runningTime += System.currentTimeMillis() - startTime;
        state = State.STOPPED;
        setChanged();
        notifyObservers();
    }

    public void reset() {
        state = State.STOPPED;
        runningTime = 0;
        setChanged();
        notifyObservers();
    }

    public boolean isRunning() {
        return state == State.RUNNING;
    }

    public long getRunningTime() {
        if(state == State.RUNNING) {
            return runningTime + (System.currentTimeMillis() - startTime);
        }
        return runningTime;
    }


}