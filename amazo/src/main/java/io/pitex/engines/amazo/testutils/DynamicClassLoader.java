package io.pitex.engines.amazo.testutils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class DynamicClassLoader extends ClassLoader {

    public Class<?> load(String name, byte[] code) {
        return defineClass(name, code, 0, code.length);
    }

    public Class<?> load(Class<?> type) {
        try {
            String path = type.getName().replace('.', '/') + ".class";
            InputStream stream = type.getClassLoader().getResourceAsStream(path);
            Objects.requireNonNull(stream, "Could not find class resource stream");
            byte[] code = stream.readAllBytes();
            return load(type.getName(), code);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

}
