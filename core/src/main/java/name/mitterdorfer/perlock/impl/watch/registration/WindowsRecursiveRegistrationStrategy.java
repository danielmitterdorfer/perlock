package name.mitterdorfer.perlock.impl.watch.registration;

import com.sun.nio.file.ExtendedWatchEventModifier;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

/**
 * THIS IS AN INTERNAL IMPLEMENTATION CLASS AND DOES NOT BELONG TO THE API. DO NOT USE IT DIRECTLY.
 */
public final class WindowsRecursiveRegistrationStrategy extends AbstractRegistrationStrategy {
    public WindowsRecursiveRegistrationStrategy(Map<WatchKey, Path> keys) {
        super(keys);
    }

    @Override
    public void registerRoot(WatchService watchService, Path rootPath) throws IOException {
        register(watchService, rootPath);
    }

    @Override
    public void registerChild(WatchService watchService, Path childPath) throws IOException {
        //This is a no op on Windows - according to my tests on Windows XP SP 3 with Java 1.7.0_45 the
        // FILE_TREE modifier is sufficient.
        //
        // For other platforms we still need to watch recursively
    }

    @Override
    protected WatchKey registerSingleDirectory(WatchService watchService, Path dir) throws IOException {
        return dir.register(watchService, getWatchEventKinds(), ExtendedWatchEventModifier.FILE_TREE);
    }
}
