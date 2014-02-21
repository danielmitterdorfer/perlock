package name.mitterdorfer.perlock.impl.watch.registration;

import name.mitterdorfer.perlock.impl.watch.WatchRegistrationStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class NonRecursiveRegistrationStrategyTest {
    private WatchRegistrationStrategy strategy;
    private Map<WatchKey, Path> keys;

    @Before
    public void setUp() {
        this.keys = new HashMap<>();
        this.strategy = new NonRecursiveRegistrationStrategy(keys);
    }

    @Test
    public void testRegistersRootPath() throws Exception {
        Path rootPath = mock(Path.class);
        WatchKey watchKeyForRootPath = mock(WatchKey.class);
        WatchService watchService = mock(WatchService.class);
        when(rootPath.register(eq(watchService), Matchers.<WatchEvent.Kind>anyVararg())).thenReturn(watchKeyForRootPath);

        strategy.registerRoot(watchService, rootPath);

        assertEquals(1, keys.size());
        assertTrue(keys.containsKey(watchKeyForRootPath));
        assertEquals(rootPath, keys.get(watchKeyForRootPath));
    }

    @Test
    public void testDoesNotRegisterChildPath() throws Exception {
        Path childPath = mock(Path.class);
        WatchService watchService = mock(WatchService.class);

        strategy.registerChild(watchService, childPath);

        verifyZeroInteractions(watchService);
        assertTrue(keys.isEmpty());
    }
}
