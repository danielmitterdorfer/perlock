package name.mitterdorfer.perlock.impl.watch;

import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Map;

/**
 * WatchRegistrationFactory abstracts the selection and creation process of a concrete WatchRegistrationStrategy.
 */
public interface WatchRegistrationFactory {
    /**
     * Chooses and creates a new WatchRegistrationStrategy instance.
     *
     * @param keys             The watch keys that are needed by the WatchRegistrationStrategy. Those keys will be
     *                         shared between the client and the WatchRegistrationStrategy. Must not be null.
     * @param recursiveWatcher true if recursive watching is requested, false otherwise.
     * @return A newly created instance of a suitable WatchRegistrationStrategy. Never null.
     */
    WatchRegistrationStrategy createRegistrationStrategy(Map<WatchKey, Path> keys, boolean recursiveWatcher);
}
