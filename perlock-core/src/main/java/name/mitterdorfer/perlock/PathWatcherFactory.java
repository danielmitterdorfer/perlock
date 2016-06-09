package name.mitterdorfer.perlock;

import name.mitterdorfer.perlock.impl.WatchServicePathWatcher;
import name.mitterdorfer.perlock.impl.util.Preconditions;
import name.mitterdorfer.perlock.impl.watch.DefaultWatchRegistrationFactory;
import name.mitterdorfer.perlock.impl.watch.WatchRegistrationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <code>PathWatcherFactory</code> is the main entry point for clients. It creates new {@link PathWatcher}
 * instances. A <code>PathWatcherFactory</code> can (and should be) reused when creating multiple
 * {@link PathWatcher} instances. <code>PathWatcherFactory</code> can safely be used by multiple threads.
 */
public final class PathWatcherFactory {
    private final ExecutorService executorService;

    private final WatchRegistrationFactory watchRegistrationFactory;

    private final LifecycleListener globalLifecycleListener;

    /**
     * Creates a new <code>PathWatcherFactory</code> instance. Exceptions within path watchers will be handled
     * internally without notification of clients.
     *
     * @param executorService An <code>ExecutorService</code> that will be used to create watcher threads. Clients
     *                        should expect that the <code>PathWatcherFactory</code> will request a new thread from the
     *                        executorService for each new <code>PathWatcher</code> (even if the same path is watched
     *                        twice). It is up to clients to provide an executor service that can create enough threads
     *                        for all path watchers. Must not be null. Must not be shutdown.
     */
    public PathWatcherFactory(ExecutorService executorService) {
        this(executorService, DefaultWatchRegistrationFactory.INSTANCE, NoOpLifecycleListener.INSTANCE);
    }

    /**
     * Creates a new <code>PathWatcherFactory</code> instance. All lifecycle events for path watchers will be reported
     * to the provided lifecycle listener.
     *
     * @param executorService   An <code>ExecutorService</code> that will be used to create watcher threads. Clients
     *                          should expect that the <code>PathWatcherFactory</code> will request a new thread from
     *                          the executorService for each new <code>PathWatcher</code> (even if the same path is
     *                          watched twice). It is up to clients to provide an executor service that can create
     *                          enough threads for all path watchers. Must not be null. Must not be shutdown.
     * @param lifecycleListener A <code>LifeCycleListener</code> implementation that is called every time a lifecycle
     *                          event happens for a path watcher. Must not be null.
     */
    public PathWatcherFactory(ExecutorService executorService, LifecycleListener lifecycleListener) {
        this(executorService, DefaultWatchRegistrationFactory.INSTANCE, lifecycleListener);
    }

    //internal constructor needed for testing
    protected PathWatcherFactory(ExecutorService executorService,
                                 WatchRegistrationFactory watchRegistrationFactory,
                                 LifecycleListener lifecycleListener) {
        Preconditions.isNotNull(executorService, "executorService");
        Preconditions.isTrue(!executorService.isShutdown(), "executorService must not be shutdown");
        Preconditions.isNotNull(lifecycleListener, "lifecycleListener");
        this.executorService = executorService;
        this.globalLifecycleListener = lifecycleListener;
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
     * Creates a new <code>PathWatcher</code> that can watch the provided root path but none of its subdirectories.
     * Note that the <code>PathWatcher</code> does not watch before {@link PathWatcher#start()} is invoked.
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


    /**
     * Creates a {@link PathWatcher} that watched for changes to the designated file, backed by a single thread executor.
     * @param path The path to a precise file or directory to watch for changes.
     * @param pathChangeListener The callback handler for when something happens to the file
     * @return A new <code>PathWatcher</code> instance
     */
    public static PathWatcher createSinglePathWatcher(Path path, PathChangeListener pathChangeListener){
        return createSinglePathWatcher(path, pathChangeListener, Executors.newSingleThreadExecutor());
    }

    /**
     * Creates a {@link PathWatcher} that watched for changes to the designated file.
     * @param path The path to a precise file or directory to watch for changes.
     * @param pathChangeListener The callback handler for when something happens to the file
     * @param executorService The executor service to run the PatchWatcher thread on.
     * @return A new <code>PathWatcher</code> instance
     */
    public static PathWatcher createSinglePathWatcher(
            Path path,
            PathChangeListener pathChangeListener,
            ExecutorService executorService){
        return createSinglePathWatcher(path, pathChangeListener, executorService, new PathWatcherFactory.NoOpLifecycleListener());
    }

    public static PathWatcher createSinglePathWatcher(
            Path path,
            PathChangeListener pathChangeListener,
            LifecycleListener lifecycleListener){
        return createSinglePathWatcher(path, pathChangeListener, Executors.newSingleThreadExecutor(), lifecycleListener);
    }

    /**
     * Creates a {@link PathWatcher} that watched for changes to the designated file.
     * @param path The path to a precise file or directory to watch for changes.
     * @param pathChangeListener The callback handler for when something happens to the file
     * @param executorService The executor service to run the PatchWatcher thread on.
     * @param lifecycleListener A <code>LifeCycleListener</code> implementation that is called every time a lifecycle
     *                          event happens for a path watcher. Must not be null.
     * @return A new <code>PathWatcher</code> instance
     */
    public static PathWatcher createSinglePathWatcher(
            Path path,
            PathChangeListener pathChangeListener,
            ExecutorService executorService,
            LifecycleListener lifecycleListener){
        Preconditions.isNotNull(path, "path");
        Preconditions.isNotNull(pathChangeListener, "pathChangeListener");
        Preconditions.isNotNull(executorService, "executorService");
        Preconditions.isNotNull(lifecycleListener, "lifecycleListener");
        PathWatcherFactory pathWatcherFactory = new PathWatcherFactory(executorService, lifecycleListener);
        return pathWatcherFactory.createNonRecursiveWatcher(path.getParent(),
                new SinglePathChangeListener(path, pathChangeListener));
    }

    private PathWatcher createWatcher(Path rootPath, boolean recursive, PathChangeListener listener) {
        WatchServicePathWatcher watcherDelegate = new WatchServicePathWatcher(rootPath, watchRegistrationFactory,
                recursive, listener);
        return new RunnablePathWatcherAdapter(watcherDelegate, executorService, globalLifecycleListener);
    }

    private static final class SinglePathChangeListener implements PathChangeListener {

        private final Path path;
        private final PathChangeListener pathChangeListener;

        SinglePathChangeListener(Path path, PathChangeListener pathChangeListener) {
            this.path = path;
            this.pathChangeListener = pathChangeListener;
        }

        @Override
        public void onPathChanged(EventKind eventKind, Path path) {
            if(path.equals(this.path)){
                pathChangeListener.onPathChanged(eventKind, path);
            }
        }
    }

    private static final class RunnablePathWatcherAdapter implements Runnable, PathWatcher {
        private static final Logger LOG = LoggerFactory.getLogger(RunnablePathWatcherAdapter.class);

        private final WatchServicePathWatcher delegate;
        private final ExecutorService executorService;
        private final LifecycleListener lifecycleListener;
        // We cannot ensure that #start() and #stop() are called from the same thread.
        // Make volatile to ensure writes are visible across threads.
        private volatile Future<?> future;

        /**
         * Creates a new <code>RunnablePathWatcherAdapter</code> instance.
         *
         * @param delegate          The <code>PathWatcher</code> instance to which this <code>PathWatcher</code> will
         *                          delegate to. Must not be null. Must not be started or already managed by any other
         *                          means.
         * @param executorService   The executor service that will be used to schedule the <code>delegate</code>
         * @param lifecycleListener Lifecycle listener that will be notified on lifecycle events of this path watcher.
         */
        private RunnablePathWatcherAdapter(WatchServicePathWatcher delegate,
                                           ExecutorService executorService,
                                           LifecycleListener lifecycleListener) {
            this.delegate = delegate;
            this.executorService = executorService;
            this.lifecycleListener = lifecycleListener;
        }

        /**
         * @see PathWatcher#start()
         */
        @Override
        public PathWatcher start() throws IOException {
            if (future != null) {
                throw new IllegalStateException("Cannot start a PathWatcher that is already running.");
            }
            delegate.start();
            LOG.trace("Submitting '{}' to executor service.", delegate);
            //submit itself when client wants to start watching. The pool will invoke the runnable when its ready
            future = executorService.submit(this);
            return this;
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
            Silently.run(new Block() {
                @Override
                public void run() {
                    lifecycleListener.onStart(RunnablePathWatcherAdapter.this);
                }
            });
            try {
                delegate.watch();
                //Catch all exceptions - not just IOException. Implementation might throw other exceptions as well
            } catch (final Exception e) {
                LOG.trace("'" + delegate + "' threw an exception", e);
                Silently.run(new Block() {
                    @Override
                    public void run() {
                        lifecycleListener.onException(RunnablePathWatcherAdapter.this, e);
                    }
                });
                // we have to stop now but as we are about to exit the run loop of this runnable we have
                // to clean up manually.
                try {
                    LOG.trace("Stopping '{}' due to an exception that had occurred earlier.", delegate);
                    future = null;
                    delegate.stop();
                } catch (Exception ex) {
                    LOG.trace("Could not close path watcher '" + delegate + "' properly.", ex);
                }
            } finally {
                Silently.run(new Block() {
                    @Override
                    public void run() {
                        lifecycleListener.onStop(RunnablePathWatcherAdapter.this);
                    }
                });
            }
        }
    }

    // Utility class to safely call client classes ignoring any exceptions they might throw
    private static final class Silently {
        // use as if it were a logger of the outer class to hide internal implementation structure
        private static final Logger LOG = LoggerFactory.getLogger(PathWatcherFactory.class);

        static void run(Block block) {
            try {
                block.run();
            } catch (Exception ex) {
                LOG.warn("Exception occurred.", ex);
            }
        }
    }

    private interface Block {
        void run();
    }

    static final class NoOpLifecycleListener implements LifecycleListener {
        static final NoOpLifecycleListener INSTANCE = new NoOpLifecycleListener();

        @Override
        public void onStart(PathWatcher pathWatcher) {
            // do nothing
        }

        @Override
        public void onException(PathWatcher pathWatcher, Exception ex) {
            // do nothing
        }

        @Override
        public void onStop(PathWatcher pathWatcher) {
            // do nothing
        }
    }
}
