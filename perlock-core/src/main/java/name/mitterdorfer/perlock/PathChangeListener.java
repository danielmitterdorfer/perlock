package name.mitterdorfer.perlock;

import java.nio.file.Path;

/**
 * <p>Callback interface to get notified about changes on the file system. Clients that are interested only in a subset of
 * all provided events may extend the convenience base class {@link AbstractPathChangeListener} instead.</p>
 *
 * <p>Implementation note: A <code>PathChangeListener</code> will be called from the internal <code>PathWatcher</code>
 * thread. This implies that callback methods should return reasonably fast and offload heavy lifting to a dedicated
 * thread. Otherwise, events may be lost.</p>
 */
public interface PathChangeListener {
    /**
     * This method has to be called when a new path is created.
     *
     * @param path The path that has been created. Must not be null.
     */
    void onPathCreated(Path path);

    /**
     * This method has to be called when an already existing path has been modified.
     *
     * @param path The path that has been modified. Must not be null.
     */
    void onPathModified(Path path);

    /**
     * This method has to be called when a previously existing path has been deleted.
     *
     * @param path The path that has been deleted. Must not be null.
     */
    void onPathDeleted(Path path);
}
