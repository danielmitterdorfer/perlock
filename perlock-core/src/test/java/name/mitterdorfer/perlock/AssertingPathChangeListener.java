package name.mitterdorfer.perlock;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AssertingPathChangeListener implements PathChangeListener {
    private final Set<Event> events = Collections.newSetFromMap(new ConcurrentHashMap<Event, Boolean>());

    @Override
    public void onPathChanged(EventKind eventKind, Path path) {
        this.events.add(new Event(path, eventKind));
    }

    public void assertPathCreated(Path path) {
        assertEvent(path, EventKind.CREATE);
    }

    public void assertPathNotCreated(Path path) {
        assertNoEvent(path, EventKind.CREATE);
    }

    public void assertPathModified(Path path) {
        assertEvent(path, EventKind.MODIFY);
    }

    public void assertPathNotModified(Path path) {
        assertNoEvent(path, EventKind.MODIFY);
    }

    public void assertPathDeleted(Path path) {
        assertEvent(path, EventKind.DELETE);
    }

    public void assertPathNotDeleted(Path path) {
        assertNoEvent(path, EventKind.DELETE);
    }

    private void assertEvent(Path path, EventKind kind) {
        assertTrue("Expected event '" + kind + "' for path '" + path + "' is missing.", events.contains(new Event(path, kind)));
    }

    private void assertNoEvent(Path path, EventKind kind) {
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

    private static class Event {
        private final Path path;
        private final EventKind eventKind;

        private Event(Path path, EventKind eventKind) {
            this.path = path;
            this.eventKind = eventKind;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Event event = (Event) o;

            if (path != null ? !path.equals(event.path) : event.path != null) return false;
            return eventKind == event.eventKind;

        }

        @Override
        public int hashCode() {
            int result = path != null ? path.hashCode() : 0;
            result = 31 * result + (eventKind != null ? eventKind.hashCode() : 0);
            return result;
        }
    }
}
