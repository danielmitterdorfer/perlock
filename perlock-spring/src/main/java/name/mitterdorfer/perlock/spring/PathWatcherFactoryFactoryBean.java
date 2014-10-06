package name.mitterdorfer.perlock.spring;

import name.mitterdorfer.perlock.LifecycleListener;
import name.mitterdorfer.perlock.PathWatcherFactory;
import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.ExecutorService;

/**
 * FactoryBean implementation to create <code>PathWatcherFactory</code> instances. A <code>PathWatcherFactory</code>
 * should only be created once per application. It can then be used to create multiple <code>PathWatcher</code>
 * instances.
 *
 * In very simple applications (only one <code>PathWatcher</code>) you can get away with an implementation that creates
 * the <code>PathWatcherFactory</code> together with the <code>PathWatcher</code>.
 */
public class PathWatcherFactoryFactoryBean implements FactoryBean<PathWatcherFactory> {
    private final PathWatcherFactory factory;

    /**
     * Creates a new PathWatcherFactory using the provided executor service and a default lifecycle listener.
     *
     * @param executorService The executor service where path watcher threads are scheduled. The same preconditions
     *                        apply as specified in PathWatcherFactory#PathWatcherFactory(java.util.concurrent.ExecutorService).
     */
    public PathWatcherFactoryFactoryBean(ExecutorService executorService) {
        this.factory = new PathWatcherFactory(executorService);
    }

    /**
     * Creates a new PathWatcherFactory using the provided executor service and lifecycle listener.
     *
     * @param executorService   The executor service where path watcher threads are scheduled. The same preconditions
     *                          apply as specified in PathWatcherFactory(java.util.concurrent.ExecutorService,
     *                          name.mitterdorfer.perlock.LifecycleListener).
     * @param lifecycleListener The lifecycle listener that gets notified on lifecycle events. The same preconditions
     *                          apply as specified in PathWatcherFactory(java.util.concurrent.ExecutorService,
     *                          name.mitterdorfer.perlock.LifecycleListener).
     */
    public PathWatcherFactoryFactoryBean(ExecutorService executorService, LifecycleListener lifecycleListener) {
        this.factory = new PathWatcherFactory(executorService, lifecycleListener);
    }

    @Override
    public PathWatcherFactory getObject() throws Exception {
        return this.factory;
    }

    @Override
    public Class<?> getObjectType() {
        return PathWatcherFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
