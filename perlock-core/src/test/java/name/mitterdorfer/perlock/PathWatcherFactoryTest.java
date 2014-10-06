package name.mitterdorfer.perlock;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathWatcherFactoryTest {
    // as starting/stop happens in the background we might miss events. Wait a bit...
    private static final long TIME_GAP_LIFE_CYCLE = 100L;
    // Jimfs polls only every 5 seconds...
    private static final long TIME_GAP_POLL_INTERVAL = 5500L;

    private Path rootPath;
    private PathWatcherFactory pathWatcherFactory;
    private AssertingPathChangeListener pathChangeListener;
    private AssertingLifecycleListener lifecycleListener;

    @Before
    public void setUp() throws Exception {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        rootPath = fs.getPath("/rootPath");
        Files.createDirectory(rootPath);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        lifecycleListener = new AssertingLifecycleListener();
        pathWatcherFactory = new PathWatcherFactory(executor, lifecycleListener);
        pathChangeListener = new AssertingPathChangeListener();
    }

    @Test
    public void testNonRecursiveWatcher() throws Exception {
        PathWatcher watcher = pathWatcherFactory.createNonRecursiveWatcher(rootPath, pathChangeListener);
        assertFalse(watcher.isRunning());
        watcher.start();
        Thread.sleep(TIME_GAP_LIFE_CYCLE);
        assertTrue(watcher.isRunning());

        Path fileInRootPath = rootPath.resolve("text.txt");
        Path dirLevel0 = rootPath.resolve("dir0");
        Path dirLevel1 = dirLevel0.resolve("dir1");
        Files.createFile(fileInRootPath);
        Files.createDirectory(dirLevel0);

        Thread.sleep(TIME_GAP_POLL_INTERVAL);
        Files.createDirectory(dirLevel1);
        Files.delete(fileInRootPath);
        Thread.sleep(TIME_GAP_POLL_INTERVAL);

        watcher.stop();
        Thread.sleep(TIME_GAP_LIFE_CYCLE);
        assertFalse(watcher.isRunning());

        pathChangeListener.assertPathCreated(dirLevel0);
        pathChangeListener.assertPathModified(dirLevel0);
        pathChangeListener.assertPathNotDeleted(dirLevel0);

        pathChangeListener.assertNoEventForPath(dirLevel1);

        pathChangeListener.assertPathCreated(fileInRootPath);
        pathChangeListener.assertPathNotModified(fileInRootPath);
        pathChangeListener.assertPathDeleted(fileInRootPath);
    }

    @Test
    public void testRecursiveWatcher() throws Exception {
        PathWatcher watcher = pathWatcherFactory.createRecursiveWatcher(rootPath, pathChangeListener);
        assertFalse(watcher.isRunning());
        watcher.start();
        Thread.sleep(TIME_GAP_LIFE_CYCLE);
        assertTrue(watcher.isRunning());

        Path fileInRootPath = rootPath.resolve("text.txt");
        Path dirLevel0 = rootPath.resolve("dir0");
        Path dirLevel1 = dirLevel0.resolve("dir1");
        Files.createFile(fileInRootPath);
        Files.createDirectory(dirLevel0);
        Thread.sleep(TIME_GAP_POLL_INTERVAL);
        Files.createDirectory(dirLevel1);
        Thread.sleep(TIME_GAP_POLL_INTERVAL);
        Path fileInDirLevel1 = dirLevel1.resolve("quotes.txt");
        Files.createFile(fileInDirLevel1);
        Thread.sleep(TIME_GAP_POLL_INTERVAL);
        Files.write(fileInDirLevel1, Collections.singleton("Oh gravity, thou art so heartless."), StandardCharsets.UTF_8);
        Files.delete(fileInRootPath);
        Thread.sleep(TIME_GAP_POLL_INTERVAL);
        Files.delete(fileInDirLevel1);
        // Unfortunately, JimFs is not able to spot that the file and the directory have been deleted
        // so we wait another round before deleting the directory too...
        Thread.sleep(TIME_GAP_POLL_INTERVAL);
        Files.delete(dirLevel1);
        Thread.sleep(TIME_GAP_POLL_INTERVAL);

        watcher.stop();
        Thread.sleep(TIME_GAP_LIFE_CYCLE);
        assertFalse(watcher.isRunning());

        pathChangeListener.assertPathCreated(dirLevel0);
        pathChangeListener.assertPathModified(dirLevel0);
        pathChangeListener.assertPathNotDeleted(dirLevel0);

        pathChangeListener.assertPathCreated(dirLevel1);
        pathChangeListener.assertPathModified(dirLevel1);
        pathChangeListener.assertPathDeleted(dirLevel1);

        pathChangeListener.assertPathCreated(fileInRootPath);
        pathChangeListener.assertPathNotModified(fileInRootPath);
        pathChangeListener.assertPathDeleted(fileInRootPath);

        pathChangeListener.assertPathCreated(fileInDirLevel1);
        pathChangeListener.assertPathModified(fileInDirLevel1);
        pathChangeListener.assertPathDeleted(fileInDirLevel1);
    }

    @Test
    public void testLifecycle() throws Exception {
        PathWatcher watcher = pathWatcherFactory.createRecursiveWatcher(rootPath, pathChangeListener);
        assertFalse(watcher.isRunning());
        lifecycleListener.assertOnStartNotCalled();
        lifecycleListener.assertOnStopNotCalled();

        Path fileInRootPath = rootPath.resolve("text.txt");
        Files.createFile(fileInRootPath);
        Thread.sleep(TIME_GAP_POLL_INTERVAL);

        pathChangeListener.assertNoEvents();

        watcher.start();
        Thread.sleep(TIME_GAP_LIFE_CYCLE);
        assertTrue(watcher.isRunning());
        lifecycleListener.assertOnStartCalled();
        lifecycleListener.assertOnStopNotCalled();

        Path dirLevel0 = rootPath.resolve("dir0");
        Files.createDirectory(dirLevel0);

        Thread.sleep(TIME_GAP_POLL_INTERVAL);
        pathChangeListener.assertPathCreated(dirLevel0);

        watcher.stop();
        Thread.sleep(TIME_GAP_LIFE_CYCLE);
        assertFalse(watcher.isRunning());
        lifecycleListener.assertOnStopCalled();

        // Should not receive any more updates
        Path dir1Level0 = rootPath.resolve("dir0.1");
        Files.createDirectory(dir1Level0);

        Thread.sleep(TIME_GAP_POLL_INTERVAL);
        pathChangeListener.assertNoEventForPath(dir1Level0);
    }

    @Test
    public void testRogueListener() throws Exception {
        final RuntimeException testException = new RuntimeException("exception by rogue listener");
        testException.setStackTrace(new StackTraceElement[0]);
        PathWatcher watcher = pathWatcherFactory.createNonRecursiveWatcher(rootPath, new AbstractPathChangeListener() {
            @Override
            public void onPathCreated(Path path) {
                throw testException;
            }
        });
        assertFalse(watcher.isRunning());
        watcher.start();
        Thread.sleep(TIME_GAP_LIFE_CYCLE);
        assertTrue(watcher.isRunning());

        Path fileInRootPath = rootPath.resolve("text.txt");
        Files.createFile(fileInRootPath);

        Thread.sleep(TIME_GAP_POLL_INTERVAL);

        assertFalse(watcher.isRunning());

        lifecycleListener.assertNumberOfReportedExceptions(1);
        lifecycleListener.assertSameException(testException);
        lifecycleListener.assertOnStopCalled();
    }

}
