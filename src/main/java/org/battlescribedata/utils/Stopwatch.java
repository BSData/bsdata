
package org.battlescribedata.utils;


public class Stopwatch {

    private long startTime;

    public Stopwatch() {
        reset();
    }

    public final void reset() {
        startTime = System.currentTimeMillis();
    }

    public long getTime() {
        return System.currentTimeMillis() - startTime;
    }
}
