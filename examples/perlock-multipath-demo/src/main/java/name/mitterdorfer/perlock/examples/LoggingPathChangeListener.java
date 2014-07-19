package name.mitterdorfer.perlock.examples;

import name.mitterdorfer.perlock.PathChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Simple <code>PathChangeListener</code> implementation that just logs events.
 */
public final class LoggingPathChangeListener implements PathChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingPathChangeListener.class);

    @Override
    public void onPathCreated(Path path) {
        LOGGER.info("Created '" + path + "'");
    }

    @Override
    public void onPathModified(Path path) {
        LOGGER.info("Modified '" + path + "'");
    }

    @Override
    public void onPathDeleted(Path path) {
        LOGGER.info("Deleted '" + path + "'");
    }
}
