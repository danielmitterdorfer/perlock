package name.mitterdorfer.perlock.impl.watch.registration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;

/**
 * THIS IS AN INTERNAL IMPLEMENTATION CLASS AND DOES NOT BELONG TO THE API. DO NOT USE IT DIRECTLY.
 */
public final class NonRecursiveRegistrationStrategy extends AbstractRegistrationStrategy {
    public NonRecursiveRegistrationStrategy(Map<WatchKey, Path> keys) {
        super(keys);
    }

    @Override
    public void registerRoot(WatchService watchService, Path rootPath) throws IOException {
        register(watchService, rootPath);
    }

    @Override
    public void registerChild(WatchService watchService, Path childPath) throws IOException {
        //Nothing to do - child paths never have to be registered for a non-recursive watcher
    }
}
