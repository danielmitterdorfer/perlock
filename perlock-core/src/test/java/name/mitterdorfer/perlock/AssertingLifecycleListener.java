package name.mitterdorfer.perlock;

import name.mitterdorfer.perlock.LifecycleListener;
import name.mitterdorfer.perlock.PathWatcher;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class AssertingLifecycleListener implements LifecycleListener {
    private volatile Exception reportedException;
    private final AtomicInteger numberOfReportedExceptions = new AtomicInteger();
    private volatile boolean onStartCalled;
    private volatile boolean onStopCalled;

    @Override
    public void onStart(PathWatcher pathWatcher) {
        onStartCalled = true;
    }

    public void assertOnStartCalled() {
        assertTrue("Expected that #onStart() has been called already", onStartCalled);
    }

    public void assertOnStartNotCalled() {
        assertFalse("Expected that #onStart() has not been called", onStartCalled);
    }

    @Override
    public void onStop(PathWatcher pathWatcher) {
        onStopCalled = true;
    }

    public void assertOnStopCalled() {
        assertTrue("Expected that #onStop() has been called already", onStopCalled);
    }

    public void assertOnStopNotCalled() {
        assertFalse("Expected that #onStop() has not been called", onStopCalled);
    }

    @Override
    public void onException(PathWatcher pathWatcher, Exception ex) {
        numberOfReportedExceptions.incrementAndGet();
        reportedException = ex;
    }

    public void assertNumberOfReportedExceptions(int expectedExceptions) {
        assertEquals(expectedExceptions, numberOfReportedExceptions.get());
    }

    public void assertSameException(Exception expectedException) {
        assertSame(expectedException, reportedException);
    }
}
