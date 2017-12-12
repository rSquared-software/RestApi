package software.rsquared.restapi;

/**
 * Timer class provides method to measure elapsed time
 *
 * @author Rafał Zajfert
 */
class Timer {

	private long startTime;
	private long endTime;

	/**
	 * Save actual time as start time of timer
	 */
	void start() {
		startTime = System.nanoTime();
	}


	/**
	 * Save actual time as end time of timer
	 */
	void stop() {
		endTime = System.nanoTime();
	}

	/**
	 * Clear Timer state, can be used before next measurement
	 */
	void clear() {
		startTime = 0;
		endTime = 0;
	}

	/**
	 * Sets initial values for the new measurement
	 */
	void reset() {
		startTime = System.nanoTime();
		endTime = 0;
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
		if (endTime > 0) {
			end = endTime;
		}
		return end - startTime;
	}

	/**
	 * @return elapsed time between start and stop (or now if stop wasn't call before) in ms
	 */
	long getElapsedTimeMs() {
		return getElapsedTimeNs() / 1_000_000;
	}
}
