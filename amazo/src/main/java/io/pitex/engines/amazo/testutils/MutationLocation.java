package io.pitex.engines.amazo.testutils;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.reloc.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

public class MutationLocation {

    private final Class<?> toMutate;

    private MutationLocation(Class<?> toMutate) {
        Objects.requireNonNull(toMutate, "Class to mutate can not be null");
        this.toMutate = toMutate;
    }

    public Location method(String name, Class<?>... parameterTypes) {
        try {
            Method method = toMutate.getMethod(name, parameterTypes);
            return Location.location(ClassName.fromClass(toMutate),
                    MethodName.fromString(name),
                    Type.getMethodDescriptor(method));
        }
        catch (NoSuchMethodException exc) {
            throw new AssertionError(exc);
        }
    }

    public Location constructor(Class<?>... parameterTypes) {
        try {
            Constructor ctor = toMutate.getConstructor(parameterTypes);
            return Location.location(ClassName.fromClass(toMutate),
                    MethodName.fromString(ctor.getName()),
                    Type.getConstructorDescriptor(ctor));
        }
        catch (NoSuchMethodException exc) {
            throw new AssertionError(exc);
        }
    }

    public Location staticInitializer() {
        //TODO: ?
        return null;
    }

    public static MutationLocation in(Class<?> toMutate) {
        return new MutationLocation(toMutate);
    }

}
