package name.mitterdorfer.perlock;

import name.mitterdorfer.perlock.PathChangeListener;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AssertingPathChangeListener implements PathChangeListener {
    private final Set<Event> events = Collections.newSetFromMap(new ConcurrentHashMap<Event, Boolean>());

    @Override
    public void onPathCreated(Path path) {
        this.events.add(new Event(path, Kind.CREATE));
    }

    @Override
    public void onPathModified(Path path) {
        this.events.add(new Event(path, Kind.MODIFY));
    }

    @Override
    public void onPathDeleted(Path path) {
        this.events.add(new Event(path, Kind.DELETE));
    }

    public void assertPathCreated(Path path) {
        assertEvent(path, Kind.CREATE);
    }

    public void assertPathNotCreated(Path path) {
        assertNoEvent(path, Kind.CREATE);
    }

    public void assertPathModified(Path path) {
        assertEvent(path, Kind.MODIFY);
    }

    public void assertPathNotModified(Path path) {
        assertNoEvent(path, Kind.MODIFY);
    }

    public void assertPathDeleted(Path path) {
        assertEvent(path, Kind.DELETE);
    }

    public void assertPathNotDeleted(Path path) {
        assertNoEvent(path, Kind.DELETE);
    }

    private void assertEvent(Path path, Kind kind) {
        assertTrue("Expected event '" + kind + "' for path '" + path + "' is missing.", events.contains(new Event(path, kind)));
    }

    private void assertNoEvent(Path path, Kind kind) {
        assertFalse("Unexpected event '" + kind + "' for path '" + path + "' found.", events.contains(new Event(path, kind)));
    }

    public void assertNoEvents() {
        assertTrue("No events expected but have recorded " + events.size() + "events", events.isEmpty());
    }

    public void assertNoEventForPath(Path path) {
        assertPathNotCreated(path);
        assertPathNotModified(path);
        assertPathNotDeleted(path);
    }


    private static enum Kind {
        CREATE, MODIFY, DELETE
    }

    private static class Event {
        private final Path path;
        private final Kind kind;

        private Event(Path path, Kind kind) {
            this.path = path;
            this.kind = kind;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Event event = (Event) o;

            if (kind != event.kind) return false;
            if (!path.equals(event.path)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = path.hashCode();
            result = 31 * result + kind.hashCode();
            return result;
        }
    }
}
