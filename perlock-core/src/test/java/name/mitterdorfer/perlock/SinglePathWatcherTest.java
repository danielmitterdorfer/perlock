package name.mitterdorfer.perlock;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.WatchServiceConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;


public class SinglePathWatcherTest {

    private static final Long testTimeout = 1L;

    private Path rootPath;
    private Path filepath;

    @Before
    public void setUp() throws Exception {
        WatchServiceConfiguration wsc = WatchServiceConfiguration.polling(100, TimeUnit.MILLISECONDS);
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix().toBuilder().setWatchServiceConfiguration(wsc).build());
        rootPath = fs.getPath("/rootPath/");
        filepath = rootPath.resolve("filepath.txt");
        Files.createDirectory(filepath.getParent());
    }

    @Test
    public void testNonRecursiveWatcher() throws Exception {
        final BlockingQueue<EventKind> queue = new ArrayBlockingQueue<>(3);
        PathChangeListener pathChangeListener =  new PathChangeListener() {
            @Override
            public void onPathChanged(EventKind eventKind, Path path) {
                queue.add(eventKind);
            }
        };

        PathWatcherFactory.createSinglePathWatcher(filepath, pathChangeListener).start();

        Files.createFile(filepath);
        assertEquals(EventKind.CREATE, queue.poll(testTimeout, TimeUnit.SECONDS));

        Files.write(filepath, new byte[1]);
        assertEquals(EventKind.MODIFY, queue.poll(testTimeout, TimeUnit.SECONDS));

        Files.delete(filepath);
        assertEquals(EventKind.DELETE, queue.poll(testTimeout, TimeUnit.SECONDS));

        //check that changes to files not specified don't create watch events.
        Path irrelevantPath = rootPath.resolve("dont-care.txt");
        Files.createFile(irrelevantPath);
        assertEquals(null, queue.poll(testTimeout, TimeUnit.SECONDS));

        Files.write(irrelevantPath, new byte[1]);
        assertEquals(null, queue.poll(testTimeout, TimeUnit.SECONDS));

        Files.delete(irrelevantPath);
        assertEquals(null, queue.poll(testTimeout, TimeUnit.SECONDS));

        Files.createFile(filepath);
        queue.poll(testTimeout, TimeUnit.SECONDS);

        //open a file channel, write some date, then move the file and write some data again.
        FileChannel fileChannel = FileChannel.open(filepath, StandardOpenOption.WRITE);
        fileChannel.write(ByteBuffer.allocate(1));
        assertEquals(EventKind.MODIFY, queue.poll(testTimeout, TimeUnit.SECONDS));

        Files.move(filepath, irrelevantPath);
        assertEquals(EventKind.DELETE, queue.poll(testTimeout, TimeUnit.SECONDS));

        assertEquals(null, queue.poll(testTimeout, TimeUnit.SECONDS));

    }


}
