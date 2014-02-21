package name.mitterdorfer.perlock;

import java.nio.file.Path;

/**
 * Convenience base class as alternative to implementing <code>PathChangeListener</code> directly. This base class can
 * be extended by clients that are only interested in certain types of changes (such as file deletions).
 */
public abstract class AbstractPathChangeListener implements PathChangeListener {
    /**
     * @see PathChangeListener#onPathCreated(java.nio.file.Path)
     */
    @Override
    public void onPathCreated(Path path) {
        //empty default implementation
    }

    /**
     * @see PathChangeListener#onPathModified(java.nio.file.Path)
     */
    @Override
    public void onPathModified(Path path) {
        //empty default implementation
    }

    /**
     * @see PathChangeListener#onPathDeleted(java.nio.file.Path)
     */
    @Override
    public void onPathDeleted(Path path) {
        //empty default implementation
    }
}
