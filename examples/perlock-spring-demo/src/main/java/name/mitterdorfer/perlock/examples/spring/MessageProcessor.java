package name.mitterdorfer.perlock.examples.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class MessageProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);
    /** Simulates how long a message processor needs to process a message */
    private static final int WORK_LOAD = 5;

    private final Path pathToProcess;

    public MessageProcessor(Path pathToProcess) {
        this.pathToProcess = pathToProcess;
    }

    @Override
    public void run() {
        LOGGER.trace("Simulating work on '{}'", pathToProcess);
        try {
            for (int countDown = WORK_LOAD; countDown > 0; countDown--) {
                LOGGER.trace("Processing '{}'. {} seconds left...", pathToProcess, countDown);
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            //signal interruption to caller and clean up asap...
            Thread.currentThread().interrupt();
        } finally {
            LOGGER.trace("Done. Cleaning up... '{}'", pathToProcess);
            try {
                Files.delete(pathToProcess);
            } catch (IOException e) {
                //delete quietly..
            }
        }
    }
}
