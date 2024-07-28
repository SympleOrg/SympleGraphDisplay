package top.symple.symplegraphdisplay.util;

public class Timer {
    private long _startTime = System.currentTimeMillis();

    private double _timer = 0;

    public Timer() {}

    public void run() {
        long deltaTime = System.currentTimeMillis() - _startTime;
        _startTime = System.currentTimeMillis();

        _timer += deltaTime / 1000f;
    }

    public void reset() {
        this._timer = 0;
    }

    public double getCurrentTime() {
        return _timer;
    }
}
