package io.pitex.engines.amazo.testutils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class ReflectionHelper {

    public static Instance on(Object instance) {
        return new Instance(instance);
    }

    public static Instantiation createInstance(Class<?> type) {
        return new Instantiation(type);
    }

    public static Instance onClass(Class<?> type) { return new Instance(type); }

    public static class Instantiation {

        private final Class<?> type;

        public Instantiation(Class<?> type) {
            Objects.requireNonNull(type);
            this.type = type;
        }

        public ConstructorInvocation usingConstructor(Class<?>... parameterTypes) {
            try {
                return new ConstructorInvocation(type, type.getDeclaredConstructor(parameterTypes));
            } catch (NoSuchMethodException e) {
                throw new AssertionError("No such constructor for type " + type.getName());
            }
        }

        public Object usingDefaultConstructor() {
            return usingConstructor().with();
        }

    }

    public static class Instance {

        private final Object instance;
        private final Class<?> type;

        public Instance(Object instance, Class<?> type) {
            Objects.requireNonNull(instance, "Given instance can not be null");
            Objects.requireNonNull(type, "Given type can not be null");
            if(!type.isAssignableFrom(instance.getClass())) {
                throw new AssertionError("Given instance of type " +
                        instance.getClass().getName() +
                        " incompatible with given type " + type.getName());
            }
            this.instance = instance;
            this.type = type;
        }

        public Instance(Class<?> type) {
            Objects.requireNonNull(type, "Type for null reference can not be null");
            this.type = type;
            instance = null;
        }

        public Instance(Object instance) {
            Objects.requireNonNull(instance);
            this.instance = instance;
            type = instance.getClass();
        }

        public Invocation invoke(String methodName, Class<?>... parameterTypes) {
            try {
                Method method = type.getMethod(methodName, parameterTypes);
                return new Invocation(instance, method);
            } catch (NoSuchMethodException e) {
                throw new AssertionError("Not such method " + methodName + " for type" + type.getName());
            }
        }

        public Object invoke(String methodName) {
            return invoke(methodName, new Class<?>[0]).withNoArguments();
        }

    }

    public static class ConstructorInvocation {

        private final Class<?> type;
        private final Constructor<?> constructor;

        public ConstructorInvocation(Class<?> type, Constructor<?> constructor) {
            Objects.requireNonNull(type);
            Objects.requireNonNull(constructor);
            this.type = type;
            this.constructor = constructor;
        }

        public Object with(Object... arguments) {
            try {
                return constructor.newInstance(arguments);
            } catch (InstantiationException | InvocationTargetException e) {
                throw new AssertionError("Unexpected error instantiating type " + type.getName(), e);
            } catch (IllegalAccessException e) {
                throw new AssertionError("Such constructor can not be accessed", e);
            }
        }
    }

    public static class Invocation {

        private final Object receiver;
        private final Method method;

        public Invocation(Object receiver, Method method) {
            Objects.requireNonNull(method, "Method to invoke can not be null");
            this.receiver = receiver;
            this.method = method;
        }

        public Object with(Object... arguments) {
            try {
                return method.invoke(receiver, arguments);
            } catch (IllegalAccessException e) {
                throw new AssertionError("Invoked method " + method.toGenericString() + " can not be accessed", e);
            } catch (InvocationTargetException e) {
                throw new AssertionError("Error while invoking method " + method.toGenericString(), e);
            }
        }

        public Object withNoArguments() {
            return with();
        }
    }

}
