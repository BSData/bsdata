/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bsdata.utils;

/**
 *
 * @author Jonskichov
 */
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
