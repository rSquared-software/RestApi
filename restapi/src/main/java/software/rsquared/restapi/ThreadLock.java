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
     *
     * @throws InterruptedException
     */
    synchronized void lock() {
        waitIfLocked();
        isLocked = true;
    }

    /**
     * Unlock resource
     *
     * @see {@link Object#notify()}.
     */
    synchronized void unlock() {
        isLocked = false;
        notifyAll();
    }

    /**
     * wait for resource unlock
     *
     * @throws InterruptedException
     */
    synchronized void waitIfLocked() {
        try {
            while (isLocked) {
                wait();
            }
        } catch (InterruptedException e) {
            RestApi.getLogger().w(e);
        }
    }
}
