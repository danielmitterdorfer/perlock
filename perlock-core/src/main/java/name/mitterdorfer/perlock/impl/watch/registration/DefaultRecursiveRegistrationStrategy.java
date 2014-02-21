package name.mitterdorfer.perlock.impl.watch.registration;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * THIS IS AN INTERNAL IMPLEMENTATION CLASS AND DOES NOT BELONG TO THE API. DO NOT USE IT DIRECTLY.
 */
public final class DefaultRecursiveRegistrationStrategy extends AbstractRegistrationStrategy {
    public DefaultRecursiveRegistrationStrategy(Map<WatchKey, Path> keys) {
        super(keys);
    }

    @Override
    public void registerRoot(WatchService watchService, Path rootPath) throws IOException {
        registerAll(watchService, rootPath);
    }

    @Override
    public void registerChild(WatchService watchService, Path childPath) throws IOException {
        if (Files.isDirectory(childPath, NOFOLLOW_LINKS)) {
            registerAll(watchService, childPath);
        }
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final WatchService watchService, final Path start) throws IOException {
        LOG.trace("Scanning '{}' ...", start);
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(watchService, dir);
                return FileVisitResult.CONTINUE;
            }
        });
        LOG.trace("Done.");
    }
}
