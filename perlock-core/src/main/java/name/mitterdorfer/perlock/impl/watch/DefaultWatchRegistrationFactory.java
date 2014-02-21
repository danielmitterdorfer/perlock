package name.mitterdorfer.perlock.impl.watch;

import name.mitterdorfer.perlock.impl.watch.registration.DefaultRecursiveRegistrationStrategy;
import name.mitterdorfer.perlock.impl.watch.registration.NonRecursiveRegistrationStrategy;
import name.mitterdorfer.perlock.impl.watch.registration.WindowsRecursiveRegistrationStrategy;

import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Map;

/**
 * THIS IS AN INTERNAL IMPLEMENTATION CLASS AND DOES NOT BELONG TO THE API. DO NOT USE IT DIRECTLY.
 */
public final class DefaultWatchRegistrationFactory implements WatchRegistrationFactory {
    @Override
    public WatchRegistrationStrategy createRegistrationStrategy(Map<WatchKey, Path> keys, boolean recursiveWatcher) {
        if (!recursiveWatcher) {
            // non-recursive watching is treated equally on all systems
            return new NonRecursiveRegistrationStrategy(keys);
        } else if (isWindows()) {
            // on Windows we provide an especially suitable implementation for recursive watching
            return new WindowsRecursiveRegistrationStrategy(keys);
        } else {
            return new DefaultRecursiveRegistrationStrategy(keys);
        }
    }

    private static boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().startsWith("windows");
    }
}
