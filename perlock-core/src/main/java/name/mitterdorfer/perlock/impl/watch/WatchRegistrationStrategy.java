package name.mitterdorfer.perlock.impl.watch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchService;

/**
 * A WatchRegistrationStrategy registers paths with a WatchService
 */
public interface WatchRegistrationStrategy {
    /**
     * Registers the root path with a watch service.
     *
     * @param watchService The watch service for which a path should be registered. Must not be null.
     * @param rootPath     The root path that should be registered. Must not be null.
     * @throws IOException In case of any I/O related problems.
     */
    void registerRoot(WatchService watchService, Path rootPath) throws IOException;

    /**
     * Registers a child path with a watch service. This method is intended to be called when a directory is recursively
     * watched and newly created subpaths have to be watched too.
     *
     * @param watchService The watch service for which a path should be registered. Must not be null.
     * @param childPath    The child path that should be registered. Must not be null. Should be placed 'below' the
     *                     originally registered root path.
     * @throws IOException In case of any I/O related problems.
     */
    void registerChild(WatchService watchService, Path childPath) throws IOException;
}
