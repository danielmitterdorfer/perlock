package name.mitterdorfer.perlock.spring;

import org.springframework.core.convert.converter.Converter;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A simple custom converter from String -> Path as Spring currently provides none. It has to be registered with Spring in the
 * application context file.
 */
public final class PathConverter implements Converter<String, Path> {
    @Override
    public Path convert(String source) {
        return Paths.get(source);
    }
}
