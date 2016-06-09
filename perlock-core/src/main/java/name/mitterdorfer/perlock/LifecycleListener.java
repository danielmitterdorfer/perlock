package name.mitterdorfer.perlock;

/**
 * Callback interface to notify clients of lifecycle events for path watchers.
 *
 * Implementations are required to be thread safe. The thread on which a lifecycle listener is notified is unspecified
 * but implementations are generally expected not to block infinitely, wait for user feedback or anything similar.
 * Implementations may throw exceptions but the caller (i.e. Perlock) is free to silently ignore them.
 */
public interface LifecycleListener {
    /**
     * Called when a path watcher is actively watching for events. It is safe to assume any changes to files
     * within the path will be picked up by the path watcher after this event has been fired.
     *
     * @param pathWatcher The path watcher that is about to start. Must not be null.
     */
    void onStart(PathWatcher pathWatcher);

    /**
     * Called when an exception within a path watcher has occurred. The lifecycle listener cannot influence how the
     * exception is handled internally (currently, the path watcher will be stopped after an exception has occurred).
     *
     * @param pathWatcher The path watcher instance where an exception has occurred. Must not be null.
     * @param ex          The exception that has occurred. Must not be null.
     */
    void onException(PathWatcher pathWatcher, Exception ex);

    /**
     * Called when a path watcher is about to stop. It is unspecified whether the provided path watcher is already
     * stopped in the exact moment when this method is called (i.e. pathWatcher#isRunning() may or may not return
     * true).
     *
     * @param pathWatcher The path watcher that is about to stop. Must not be null.
     */
    void onStop(PathWatcher pathWatcher);
}
