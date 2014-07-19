package name.mitterdorfer.perlock.examples;

import name.mitterdorfer.perlock.PathWatcher;
import name.mitterdorfer.perlock.PathWatcherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This example application demonstrates how to create and use path watchers to monitor multiple paths.
 * The demo accepts two or more command line parameters:
 *
 * <ol>
 *     <li><code>--recursive</code> to watch paths recursively or <code>--non-recursive</code> to watch
 * non-recursively (i.e. only in the provided paths but not below)</li>
 *      <li><code>path</code> one ore more arguments which are the paths to watch</li>
 * </ol>
 *
 * First build the demo with <code>gradle fatJar</code>. If you invoke the sample application as follows, it watches
 * the home and the /tmp folder (on Unix):
 *
 * <code>java -jar examples/perlock-multipath-demo/build/libs/perlock-multipath-demo-0.2.0.jar --non-recursive ~ /tmp</code>
 */
public class MultiPathWatcherDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPathWatcherDemo.class);

    // Some (arbitrary) limit on how much paths we allow to watch
    private static final int MAXIMUM_NUMBER_OF_SUPPORTED_PATHS = 32;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            LOGGER.error("Usage: (--recursive|--non-recursive) paths_to_watch");
            System.exit(65);
        }
        //this is a very simple demo application and we won't go overboard with param parsing; this should suffice
        boolean recursive = "--recursive".equals(args[0]);
        // first argument is recursive or not, the rest are paths
        int numberOfPathsToWatch = args.length - 1;
        if (numberOfPathsToWatch > MAXIMUM_NUMBER_OF_SUPPORTED_PATHS) {
            LOGGER.error("Requested to watch {} paths simultaneously but only {} paths are supported.",
                    numberOfPathsToWatch, MAXIMUM_NUMBER_OF_SUPPORTED_PATHS);
            System.exit(66);
        }
        // Path watching requires background threads. Those are managed by this thread pool. Please ensure that the
        // thread pool is large enough for all path watchers.
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfPathsToWatch);
        // Create a new path watcher factory. There is only one factory instance needed. It is thread safe and
        // can be reused to create multiple path watcher instances
        PathWatcherFactory pathWatcherFactory = new PathWatcherFactory(executorService);

        // To get a single event stream we always need to register the same listener instance. Note that the path
        // change listener *will* get called from multiple threads. Guard any state in the listener properly.
        LoggingPathChangeListener pathChangeListener = new LoggingPathChangeListener();
        // We still need one path watcher per path
        PathWatcher[] pathWatchers = new PathWatcher[numberOfPathsToWatch];

        // argument index 0 was the recursive / non-recursive argument. Paths start at index 1
        LOGGER.info("Registering {} paths with path watcher service", numberOfPathsToWatch);
        for (int argIndex = 1; argIndex < args.length; argIndex++) {
            Path rootPath = Paths.get(args[argIndex]);
            LOGGER.debug("Registering path {} with path watcher service", rootPath);
            // Now we'll create a new path watcher and register the callback. Note that we will not receive any
            // changes until the PathWatcher has been started.
            pathWatchers[argIndex - 1] = recursive ?
                    pathWatcherFactory.createRecursiveWatcher(rootPath, pathChangeListener) :
                    pathWatcherFactory.createNonRecursiveWatcher(rootPath, pathChangeListener);

        }
        LOGGER.info("Successfully registered {} paths with path watcher service. Press any key to stop", numberOfPathsToWatch);
        //Start all watchers. Now we'll receive file system events.
        for (PathWatcher pathWatcher : pathWatchers) {
            pathWatcher.start();
        }

        //noinspection ResultOfMethodCallIgnored
        System.in.read();
        LOGGER.debug("Stopping path watchers");

        //We're not interested in watching anymore. Stop the watchers and the corresponding thread pool
        for (PathWatcher pathWatcher : pathWatchers) {
            pathWatcher.stop();
        }
        executorService.shutdown();
    }
}
