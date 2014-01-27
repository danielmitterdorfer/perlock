package name.mitterdorfer.perlock.impl.util;

/**
 * <p>Internal helper class to check preconditions.</p>
 *
 * <p>THIS IS AN INTERNAL IMPLEMENTATION CLASS AND DOES NOT BELONG TO THE API. DO NOT USE IT DIRECTLY.</p>
 */
public final class Preconditions {
    private Preconditions() {
        // no instances intended
    }

    public static void isNotNull(Object o, String name) {
        isTrue(o != null, "'" + name + "' must not be null");
    }

    public static void isTrue(boolean condition, String conditionDescription) {
        if (!condition) {
            throw new IllegalArgumentException(conditionDescription);
        }
    }
}
