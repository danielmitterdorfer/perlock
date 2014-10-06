package name.mitterdorfer.perlock.impl;

import name.mitterdorfer.perlock.impl.watch.WatchRegistrationFactory;
import name.mitterdorfer.perlock.impl.util.Preconditions;
import name.mitterdorfer.perlock.impl.watch.WatchRegistrationStrategy;
import name.mitterdorfer.perlock.PathChangeListener;
import name.mitterdorfer.perlock.PathWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * <p>A PathWatcher based on the JDK 7+ WatchService API.</p>
 *
 * <p><a href="http://mail.openjdk.java.net/pipermail/nio-dev/2012-June.txt>This discussion</a> on an OpenJDK mailing list
 * hints that JDK 7 uses a poll-based WatchService implementation which leads to long time lags. Fortunately, the JDK 8
 * implementation is already based on kqueue (as can be seen in the JDK 8 source in
 * <code>jdk8/jdk/src/macosx/classes/sun/nio</code>). On other tested platforms (Linux, Windows) the WatchService is
 * already efficiently implemented in JDK 7.</p>
 *
 * <p>THIS IS AN INTERNAL IMPLEMENTATION CLASS AND DOES NOT BELONG TO THE API. DO NOT USE IT DIRECTLY.</p>
 */
public final class WatchServicePathWatcher implements PathWatcher {
    private static final Logger LOG = LoggerFactory.getLogger(WatchServicePathWatcher.class);

    private final Map<WatchKey,Path> keys;
    private final PathChangeListener listener;
    private final Path rootPath;
    private final WatchRegistrationStrategy registry;
    //no need to declare watcher as volatile. It should only be used from the Watcher thread, otherwise something went
    //seriously wrong...
    private WatchService watcher;
    // due to #isRunning() we need visibility of running across threads...
    private volatile boolean running;

    public WatchServicePathWatcher(Path rootPath, WatchRegistrationFactory factory, boolean recursive, PathChangeListener listener) {
        Preconditions.isNotNull(rootPath, "rootPath");
        Preconditions.isNotNull(factory, "factory");
        Preconditions.isNotNull(listener, "listener");
        Preconditions.isTrue(Files.exists(rootPath), String.format("'rootPath' (%s) must exist", rootPath.getFileName()));
        Preconditions.isTrue(Files.isReadable(rootPath), String.format("'rootPath' (%s) must be readable", rootPath.getFileName()));
        Preconditions.isTrue(Files.isDirectory(rootPath), String.format("'rootPath' (%s) must be a directory", rootPath.getFileName()));

        this.keys = new HashMap<>();
        this.rootPath = rootPath;
        this.listener = listener;
        this.registry = factory.createRegistrationStrategy(keys, recursive);
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    private void prepareWatcher() throws IOException {
        if (running) {
            throw new IllegalStateException("Cannot start a PathWatcher that is already running.");
        }
        // Always create a new WatchService instance as the old one will get closed on stop().
        // We'll assume that it's safe to use the associated watch service of our root path.
        this.watcher = rootPath.getFileSystem().newWatchService();
    }

    private void performRegistration() throws IOException {
        registry.registerRoot(watcher, rootPath);
    }

    /**
     * Process all events for keys queued to the watcher
     */
    @Override
    public void start() throws IOException {
        prepareWatcher();
        performRegistration();
        running = true;
        watch();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void watch() {
        LOG.trace("Waiting for file system events");
        boolean moreKeysToProcess = true;
        while(moreKeysToProcess && !Thread.currentThread().isInterrupted()) {
            // wait for key to be signalled
            WatchKey key = takeKey();
            if (key != null) {
                handleKey(key);
                moreKeysToProcess = resetKey(key);
            } else {
                moreKeysToProcess = false;
            }
        }
        //close watch service also when exiting
        stop();
    }

    private WatchKey takeKey() {
        try {
            return watcher.take();
        } catch (InterruptedException x) {
            LOG.trace("Current watcher thread has been interrupted while waiting for an event to occur.");
            //signal interrupted status again
            Thread.currentThread().interrupt();
            //use 'null' as special value to indicate that the main run loop has to exit
            return null;
        }
    }

    private void handleKey(WatchKey key) {
        Path dir = keys.get(key);
        if (dir != null) {
            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                LOG.trace("Received watch event with kind '{}'", kind);
                //too many events
                if (kind != OVERFLOW) {
                    // The context for a directory entry event is the file name of entry
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path child = dir.resolve(name);
                    LOG.trace("Handling watch event with kind '{}' for path '{}'.", kind, child);
                    handleEvent(ev, child);

                    if (kind == ENTRY_CREATE) {
                        try {
                            registry.registerChild(watcher, child);
                        } catch (IOException e) {
                            LOG.warn("Could not register watch for '{}'.", child);
                        }
                    }
                }
            }
        } else {
            LOG.warn("WatchKey '{}' not recognized", key);
        }
    }

    private boolean resetKey(WatchKey key) {
        // reset key and remove from set if directory no longer accessible
        boolean valid = key.reset();
        if (!valid) {
            keys.remove(key);

            // all directories are inaccessible
            if (keys.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void stop() {
        LOG.trace("Stopping '{}'", this);
        try {
            if (this.watcher != null) {
                this.watcher.close();
            }
        } catch (IOException ex) {
            LOG.warn("Could not close '" + this + "' properly.", ex);
        } finally {
            this.watcher = null;
            //no more keys to watch for
            this.keys.clear();
            //we're not running anymore
            this.running = false;
        }
    }

    private void handleEvent(WatchEvent<Path> event, Path child) {
        if (event.kind().equals(ENTRY_CREATE)) {
            listener.onPathCreated(child);
        } else if (event.kind().equals(ENTRY_MODIFY)) {
            listener.onPathModified(child);
        } else if (event.kind().equals(ENTRY_DELETE)) {
            listener.onPathDeleted(child);
        } else {
            throw new IllegalArgumentException("Unrecognized event kind '" + event.kind() + "'.");
        }
    }

    @Override
    public String toString() {
        return "PathWatcher for '" + rootPath + "'";
    }
}
