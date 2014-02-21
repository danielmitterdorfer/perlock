package name.mitterdorfer.perlock.spring;

import name.mitterdorfer.perlock.PathChangeListener;
import name.mitterdorfer.perlock.PathWatcher;
import name.mitterdorfer.perlock.PathWatcherFactory;
import org.springframework.beans.factory.FactoryBean;

import java.nio.file.Path;

/**
 * FactoryBean implementation to create <code>PathWatcher</code> instances.
 */
public class PathWatcherFactoryBean implements FactoryBean<PathWatcher> {
    private final PathWatcherFactory factory;

    private final Path rootPath;

    private final boolean watchRecursively;

    private final PathChangeListener pathChangeListener;

    /**
     * Creates a new PathWatcherFactoryBean instance.
     *
     * @param pathWatcherFactory The <code>PathWatcherFactory</code> that is used to create <code>PathWatcher</code> instances. Must not be null.
     * @param rootPath           The root path that should be watched.
     * @param watchRecursively   true if and only if a <code>PathWatcher</code> should be created that watches subdirectories below the <code>rootPath</code> recursively.
     * @param pathChangeListener Callback that is notified when a change below the <code>rootPath</code> has occurred. Must not be null.
     * @see name.mitterdorfer.perlock.PathWatcherFactory#createRecursiveWatcher(java.nio.file.Path, name.mitterdorfer.perlock.PathChangeListener).
     * @see name.mitterdorfer.perlock.PathWatcherFactory#createNonRecursiveWatcher(java.nio.file.Path, name.mitterdorfer.perlock.PathChangeListener)
     */
    public PathWatcherFactoryBean(PathWatcherFactory pathWatcherFactory, Path rootPath,
                                  boolean watchRecursively, PathChangeListener pathChangeListener) {
        this.factory = pathWatcherFactory;
        this.rootPath = rootPath;
        this.watchRecursively = watchRecursively;
        this.pathChangeListener = pathChangeListener;
    }

    @Override
    public PathWatcher getObject() throws Exception {
        return watchRecursively ?
                factory.createRecursiveWatcher(rootPath, pathChangeListener) :
                factory.createNonRecursiveWatcher(rootPath, pathChangeListener);
    }

    @Override
    public Class<?> getObjectType() {
        return PathWatcher.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
