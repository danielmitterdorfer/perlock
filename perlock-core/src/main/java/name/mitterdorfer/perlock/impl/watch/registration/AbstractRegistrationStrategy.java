package name.mitterdorfer.perlock.impl.watch.registration;

import name.mitterdorfer.perlock.impl.watch.WatchRegistrationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * <p>THIS IS AN INTERNAL IMPLEMENTATION CLASS AND DOES NOT BELONG TO THE API. DO NOT USE IT DIRECTLY.</p>
 */
abstract class AbstractRegistrationStrategy implements WatchRegistrationStrategy {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private final Map<WatchKey,Path> keys;

    public AbstractRegistrationStrategy(Map<WatchKey, Path> keys) {
        this.keys = keys;
    }

    /**
     * Register the given directory with the WatchService
     */
    protected final void register(WatchService watchService, Path dir) throws IOException {
        WatchKey key = registerSingleDirectory(watchService, dir);
        if (LOG.isTraceEnabled()) {
            Path prev = keys.get(key);
            if (prev == null) {
                LOG.trace("Registering Path: '{}'", dir);
            } else {
                if (!dir.equals(prev)) {
                    LOG.trace("Updating Path: '{}' -> '{}'", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    protected WatchKey registerSingleDirectory(WatchService watchService, Path dir) throws IOException {
        return dir.register(watchService, getWatchEventKinds());
    }

    protected WatchEvent.Kind<?>[] getWatchEventKinds() {
        return new WatchEvent.Kind<?>[] {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};
    }
}
