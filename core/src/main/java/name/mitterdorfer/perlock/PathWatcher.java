package name.mitterdorfer.perlock;

import java.io.IOException;

/**
 * <p><code>PathWatcher</code> is a lifecycle callback interface for a concrete path watcher. Note that a path watcher
 * is just pre-configured by <code>PathWatcherFactory</code> after creation and needs to started to enable path watching.</p>
 *
 * <p>Whether a <code>PathWatcher</code> supports subsequent starting and stopping is implementation dependent and hence
 * unspecified.</p>
 */
public interface PathWatcher {
    /**
     * Starts a <code>PathWatcher</code>. Before the start, a <code>PathWatcher</code> will not notify about file system
     * changes.
     *
     * @throws IllegalStateException If <code>#start()</code> is invoked on an already running
     *                               <code>PathWatcher</code>.
     */
    void start() throws IOException;

    /**
     * Determines whether this <code>PathWatcher</code> instance is currently running.
     *
     * @return true iff this <code>PathWatcher</code> instance is currently running.
     */
    boolean isRunning();

    /**
     * <p>Request that a running <code>PathWatcher</code> stops. After the <code>PathWatcher</code> is stopped, it will
     * not watch for file system changes anymore.</p>
     * <p/>
     * <p>Precondition: The <code>PathWatcher</code> has to be running (i.e. <code>PathWatcher#start()</code> has to be
     * invoked before calling <code>#stop()</code>). Otherwise an <code>IllegalStateException</code> will be
     * thrown.</p>
     *
     * @throws IllegalStateException If <code>#stop()</code> is invoked without invoking <code>#start()</code> first.
     */
    void stop();
}
