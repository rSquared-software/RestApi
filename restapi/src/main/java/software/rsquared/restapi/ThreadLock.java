package software.rsquared.restapi;

/**
 * Thread lock used for block code for different threads
 *
 * @author Rafal Zajfert
 */
class ThreadLock {

	private boolean isLocked = false;

	/**
	 * Try to block or wait for unlock resource
	 */
	synchronized void lock() {
		waitIfLocked();
		isLocked = true;
	}

	/**
	 * Unlock resource
	 *
	 * @see Object#notifyAll()
	 */
	synchronized void unlock() {
		isLocked = false;
		notifyAll();
	}

	/**
	 * wait for resource unlock
	 */
	synchronized void waitIfLocked() {
		try {
			while (isLocked) {
				wait();
			}
		} catch (InterruptedException ignored) {
		}
	}
}
