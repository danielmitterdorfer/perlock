package name.mitterdorfer.perlock.examples.spring;

import name.mitterdorfer.perlock.PathWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Main entry point for the Spring demo application. It starts a path watcher for "/tmp" that will check for XML files.
 *
 * To try the demo, start the application and then issue the following command in a shell in the "/tmp" directory:
 *
 * "touch hello{1,2,3}.xml && sleep 2 && touch hello{4,5,6}.xml"
 *
 */
public class SpringPathWatcherDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringPathWatcherDemo.class);

    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "/name/mitterdorfer/perlock/examples/spring/application-context.xml");
        ctx.registerShutdownHook();

        PathWatcher pathWatcher = ctx.getBean("incomingMessagesWatcher", PathWatcher.class);
        pathWatcher.start();

        LOGGER.info("Press any key to stop the demo");
        //noinspection ResultOfMethodCallIgnored
        System.in.read();

        // On exit, the container will automatically shutdown any thread pools. All code should be implemented to
        // handle interruption gracefully (see MessageProcessor for an example). This is not a specific requirement
        // of perlock but applies to multithreaded programs in general.

        pathWatcher.stop();
        ctx.close();
    }
}
