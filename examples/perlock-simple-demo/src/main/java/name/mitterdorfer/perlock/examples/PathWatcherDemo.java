package name.mitterdorfer.perlock.examples;

import name.mitterdorfer.perlock.PathChangeListener;
import name.mitterdorfer.perlock.PathWatcher;
import name.mitterdorfer.perlock.PathWatcherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This example application demonstrates how to create and use a path watcher. The demo accepts two command line
 * parameters:
 *
 * <ol>
 *     <li><code>--recursive</code> to watch a path recursively or <code>--non-recursive</code> to watch
 * non-recursively (i.e. only in the provided path but not below)</li>
 *      <li><code>path</code> which is the path to watch</li>
 * </ol>
 *
 * If you invoke the sample application as follows, it watches the home folder (on Unix): <code>java -jar perlock-examples.jar --non-recursive ~</code>
 */
public class PathWatcherDemo implements PathChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathWatcherDemo.class);

    @Override
    public void onPathCreated(Path path) {
        LOGGER.info("Created '" + path + "'");
    }

    @Override
    public void onPathModified(Path path) {
        LOGGER.info("Modified '" + path + "'");
    }

    @Override
    public void onPathDeleted(Path path) {
        LOGGER.info("Deleted '" + path + "'");
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            LOGGER.error("Usage: (--recursive|--non-recursive) path_to_watch");
            System.exit(65);
        }
        //this is a very simple demo application and we won't go overboard with param parsing; this should suffice
        boolean recursive = "--recursive".equals(args[0]);
        Path rootPath = Paths.get(args[1]);

        // Path watching requires background threads. Those are managed by this thread pool. Please ensure that the
        // thread pool is large enough for all path watchers. We watch only one path here so there is no need for a
        // larger thread pool.
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        // Create a new path watcher factory. There is only one factory instance needed. It is thread safe and
        // can be reused to create multiple path watcher instances
        PathWatcherFactory pathWatchers = new PathWatcherFactory(executorService);

        LOGGER.info("Registering with path watcher service");
        // Now we'll create a new path watcher and register the callback. Note that we will not receive any
        // changes until the PathWatcher has been started.
        PathWatcher watcher = recursive ?
                pathWatchers.createRecursiveWatcher(rootPath, new PathWatcherDemo()) :
                pathWatchers.createNonRecursiveWatcher(rootPath, new PathWatcherDemo());
        LOGGER.info("Successfully registered with path watcher service. Press any key to stop");
        //Start the watcher. Now we'll receive file system events.
        watcher.start();

        //noinspection ResultOfMethodCallIgnored
        System.in.read();
        LOGGER.debug("Stopping path watcher");

        //We're not interested in watching anymore. Stop the watcher and the corresponding thread pool
        watcher.stop();
        executorService.shutdown();
    }
}
