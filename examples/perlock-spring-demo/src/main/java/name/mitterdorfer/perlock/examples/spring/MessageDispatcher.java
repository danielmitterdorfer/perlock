package name.mitterdorfer.perlock.examples.spring;

import name.mitterdorfer.perlock.AbstractPathChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.Executor;

/**
 * A simple dispatcher: It gets events from the file system, decides whether they need processing and dispatches
 * them to a <code>MessageProcessor</code> which will handle the message.
 */
public class MessageDispatcher extends AbstractPathChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDispatcher.class);

    private final Executor executor;

    public MessageDispatcher(Executor executor) {
        this.executor = executor;
    }

    /**
     * A dispatcher is only interested in new paths, specifically XML files. Everything else will be ignored.
     *
     * @param path The path that has been created. Must not be null.
     */
    @Override
    public void onPathCreated(Path path) {
        if (path.toString().endsWith("xml")) {
            LOGGER.info("Starting handling path '{}'", path);
            //Simulate work. Note that work is offloaded to a dedicated thread to ensure we do not block path watching
            executor.execute(new MessageProcessor(path));
        } else {
            LOGGER.debug("Ignoring path '{}'", path);
        }
    }
}
