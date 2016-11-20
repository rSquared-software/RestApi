package com.rafalzajfert.restapi;

/**
 * Timer class provides method to measure elapsed time
 *
 * @author Rafał Zajfert
 */
class Timer {

    private long mStartTime;
    private long mEndTime;

    /**
     * Save actual time as start time of timer
     */
    void start() {
        mStartTime = System.nanoTime();
    }


    /**
     * Save actual time as end time of timer
     */
    void stop() {
        mEndTime = System.nanoTime();
    }

    /**
     * Clear Timer state, can be used before next measurement
     */
    void clear() {
        mStartTime = 0;
        mEndTime = 0;
    }

    /**
     * Sets initial values for the new measurement
     */
    void reset(){
        mStartTime = System.nanoTime();
        mEndTime = 0;
    }

    /**
     * @return elapsed time between start and stop (or now if stop wasn't call before) in ms
     */
    double getElapsedTime() {
        return ((double) getElapsedTimeNs()) / 1_000_000d;
    }

    /**
     * @return elapsed time between start and stop (or now if stop wasn't call before) in ns
     */
    long getElapsedTimeNs() {
        long end = System.nanoTime();
        if (mEndTime > 0) {
            end = mEndTime;
        }
        return end - mStartTime;
    }

    /**
     * @return elapsed time between start and stop (or now if stop wasn't call before) in ms
     */
    long getElapsedTimeMs() {
        return getElapsedTimeNs() / 1_000_000;
    }
}
