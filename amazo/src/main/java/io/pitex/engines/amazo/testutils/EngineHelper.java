package io.pitex.engines.amazo.testutils;

import io.pitex.engines.amazo.AmazoMutater;
import io.pitex.engines.amazo.MutationOperator;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

import java.util.List;

public class EngineHelper {

    public static AmazoMutater mutater(MutationOperator operator) {
        ClassByteArraySource source = ClassloaderByteArraySource.fromContext();
        return new AmazoMutater(source, List.of(operator));
    }

    public static List<MutationDetails> findMutations(Class<?> target, MutationOperator operator) {
        return mutater(operator).findMutations(ClassName.fromClass(target));
    }

    public static Class<?> mutate(MutationOperator operator, MutationIdentifier id) {
        AmazoMutater mutater = mutater(operator);
        Mutant mutant = mutater.getMutation(id);
        return loadClass(id.getClassName().asJavaName(), mutant.getBytes());
    }

    public static Class<?> loadClass(String name, byte[] bytes) {
        return new ClassLoader() {
            Class<?> load() {
                return defineClass(name, bytes, 0, bytes.length);
            }
        }.load();
    }

}
