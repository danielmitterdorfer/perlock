package name.mitterdorfer.perlock;

import name.mitterdorfer.perlock.impl.WatchServicePathWatcher;
import name.mitterdorfer.perlock.impl.watch.DefaultWatchRegistrationFactory;
import name.mitterdorfer.perlock.impl.util.Preconditions;
import name.mitterdorfer.perlock.impl.watch.WatchRegistrationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * <code>PathWatcherFactory</code> is the main entry point for clients. It creates new {@link PathWatcher}
 * instances. A <code>PathWatcherFactory</code> can (and should be) reused when creating multiple
 * {@link PathWatcher} instances. <code>PathWatcherFactory</code> can safely be used by multiple threads.
 */
public final class PathWatcherFactory {
    private final ExecutorService executorService;

    private final WatchRegistrationFactory watchRegistrationFactory;

    /**
     * Creates a new <code>PathWatcherFactory</code> instance.
     *
     * @param executorService An <code>ExecutorService</code> that will be used to create watcher threads. Clients
     *                        should expect that the <code>PathWatcherFactory</code> will request a new thread from the
     *                        executorService for each new <code>PathWatcher</code> (even if the same path is watched
     *                        twice). It is up to clients to provide an executor service that can create enough threads
     *                        for all path watchers. Must not be null. Must not be shutdown.
     */
    public PathWatcherFactory(ExecutorService executorService) {
        this(executorService, new DefaultWatchRegistrationFactory());
    }

    //internal constructor needed for testing
    protected PathWatcherFactory(ExecutorService executorService, WatchRegistrationFactory watchRegistrationFactory) {
        Preconditions.isNotNull(executorService, "executorService");
        Preconditions.isTrue(!executorService.isShutdown(), "executorService must not be shutdown");
        Preconditions.isNotNull(watchRegistrationFactory, "watchRegistrationFactory");
        this.executorService = executorService;
        this.watchRegistrationFactory = watchRegistrationFactory;
    }

    /**
     * Creates a new <code>PathWatcher</code> that can watch the provided root path and all of its subdirectories. Note
     * that the <code>PathWatcher</code> does not watch before {@link PathWatcher#start()} is invoked.
     *
     * @param rootPath The root path to watch. It has to be a readable directory. The directory has to exist when this
     *                 method is called. Must not be null.
     * @param listener The listener to notify when a file change event occurs. If the same listener is provided for
     *                 multiple PathWatcher instances the listener has to be thread safe as it might get called from
     *                 multiple watcher threads. Must not be null.
     * @return A new <code>PathWatcher</code> instance that will watch the complete file tree below (and including) the
     *         provided <code>rootPath</code> after {@link PathWatcher#start()} has been called on that instance. Never
     *         null.
     */
    public PathWatcher createRecursiveWatcher(Path rootPath, PathChangeListener listener) {
        return createWatcher(rootPath, true, listener);
    }

    /**
     * Creates a new <code>PathWatcher</code> that can watch the provided root path but none of its subdirectories. Note
     * that the <code>PathWatcher</code> does not watch before {@link PathWatcher#start()} is invoked.
     *
     * @param rootPath The root path to watch. It has to be a readable directory. The directory has to exist when this
     *                 method is called. Must not be null.
     * @param listener The listener to notify when a file change event occurs. If the same listener is provided for
     *                 multiple <code>PathWatcher</code> instances the listener has to be thread safe as it might get
     *                 called from multiple (watcher) threads. Must not be null.
     * @return A new <code>PathWatcher</code> instance that will watch the the provided <code>rootPath</code> after
     *         {@link PathWatcher#start()} has been called on that instance. Never null.
     */
    public PathWatcher createNonRecursiveWatcher(Path rootPath, PathChangeListener listener) {
        return createWatcher(rootPath, false, listener);
    }

    private PathWatcher createWatcher(Path rootPath, boolean recursive, PathChangeListener listener) {
        //TODO: Deal with exceptions happening in background threads... -> dedicated ExceptionListener or more generic StatusListener
        PathWatcher watcherDelegate = new WatchServicePathWatcher(rootPath, watchRegistrationFactory, recursive, listener);
        return new RunnablePathWatcherAdapter(watcherDelegate, executorService);
    }

    private static class RunnablePathWatcherAdapter implements Runnable, PathWatcher {
        private static final Logger LOG = LoggerFactory.getLogger(RunnablePathWatcherAdapter.class);

        private final PathWatcher delegate;
        private final ExecutorService executorService;
        private Future<?> future;

        /**
         * Creates a new <code>RunnablePathWatcherAdapter</code> instance.
         *
         * @param delegate        The <code>PathWatcher</code> instance to which this <code>PathWatcher</code> will
         *                        delegate to. Must not be null. Must not be started or already managed by any other
         *                        means.
         * @param executorService The executor service that will be used to schedule the <code>delegate</code>
         *                        PathWatcher. Must not be null.
         */
        private RunnablePathWatcherAdapter(PathWatcher delegate, ExecutorService executorService) {
            this.delegate = delegate;
            this.executorService = executorService;
        }

        /**
         * @see PathWatcher#start()
         */
        @Override
        public void start() {
            if (future != null) {
                throw new IllegalStateException("Cannot start a PathWatcher that is already running.");
            }
            LOG.trace("Submitting '{}' to executor service.", delegate);
            //submit itself when client wants to start watching. The pool will invoke the runnable when its ready
            future = executorService.submit(this);
        }

        @Override
        public boolean isRunning() {
            return delegate.isRunning();
        }

        /**
         * @see PathWatcher#stop()
         */
        @Override
        public void stop() {
            if (future == null) {
                throw new IllegalStateException("Cannot stop a PathWatcher that is not running.");
            }

            LOG.trace("Requesting that '{}' stops.", delegate);
            //have the executor service interrupt the file watcher
            future.cancel(true);
        }

        @Override
        public void run() {
            LOG.trace("About to run '{}'.", delegate);
            try {
                this.delegate.start();
                //Catch all exceptions - not just IOException. Implementation might throw other exceptions as well
            } catch (Exception e) {
                LOG.trace("'" + delegate + "' threw an exception", e);
                //TODO: For now, we'll just throw a raw runtime exception. Find a better base class and do something
                //TODO: This exception gets swallowed by the thread pool. How do we communicate exceptions to callers? Via the PathChangeListener?

                // Other ideas:
                // * Client can provide some sort of ExceptionListener which will be notified
                // * Client can call a method #isRunning() or #isWatching() on PathWatcher which might throw an ExecutionException (compare Future#get())

                throw new RuntimeException(e);
            }
        }
    }
}
