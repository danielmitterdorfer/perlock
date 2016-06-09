package name.mitterdorfer.perlock;


import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Defines the three kinds of events that can occur to a particular path.
 * These event kinds are analogous to the Path type {@link StandardWatchEventKinds}.
 */
public enum EventKind {

    CREATE(ENTRY_CREATE),

    MODIFY(ENTRY_MODIFY),

    DELETE(ENTRY_DELETE);

    private static final Map<WatchEvent.Kind<Path>, EventKind> WATCH_EVENT_KIND_TO_EVENT_KIND;

    static {
        Map<WatchEvent.Kind<Path>, EventKind> eventKindMap = new HashMap<>();
        for (EventKind eventKind : values()) {
            eventKindMap.put(eventKind.watchEventKind, eventKind);
        }
        WATCH_EVENT_KIND_TO_EVENT_KIND = Collections.unmodifiableMap(eventKindMap);
    }

    private final WatchEvent.Kind<Path> watchEventKind;

    EventKind(WatchEvent.Kind<Path> watchEventKind) {
        this.watchEventKind = watchEventKind;
    }

    public static EventKind eventKindForWatchEventKind(WatchEvent.Kind<Path> watchEventKind) {
        return WATCH_EVENT_KIND_TO_EVENT_KIND.get(watchEventKind);
    }

}
