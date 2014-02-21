package name.mitterdorfer.perlock.spring;

import name.mitterdorfer.perlock.PathWatcherFactory;
import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.ExecutorService;

/**
 * FactoryBean implementation to create <code>PathWatcherFactory</code> instances. A <code>PathWatcherFactory</code>
 * should only be created once per application. It can then be used to create multiple <code>PathWatcher</code> instances.
 *
 * In very simple applications (only one <code>PathWatcher</code>) you can get away with an implementation that creates
 * the <code>PathWatcherFactory</code> together with the <code>PathWatcher</code>.
 *
 */
public class PathWatcherFactoryFactoryBean implements FactoryBean<PathWatcherFactory> {
    private final PathWatcherFactory factory;

    public PathWatcherFactoryFactoryBean(ExecutorService executorService) {
        this.factory = new PathWatcherFactory(executorService);
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
