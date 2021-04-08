package io.pitex.engines.amazo.testutils;

import io.pitex.engines.amazo.AmazoMutater;
import io.pitex.engines.amazo.operators.MutationOperator;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.engine.Location;
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

    public static Class<?> loadClass(String name, byte[] bytes) {
        return new DynamicClassLoader().load(name, bytes);
    }

    public static Class<?> mutate(MutationIdentifier id,  MutationOperator operator) {
        AmazoMutater mutater = mutater(operator);
        Mutant mutant = mutater.getMutation(id);
        return loadClass(id.getClassName().asJavaName(), mutant.getBytes());
    }

    public static Class<?> mutate(Location location, MutationOperator operator) {
        AmazoMutater mutater = new AmazoMutater(
                ClassloaderByteArraySource.fromContext(),
                List.of(operator),
                (node) -> !(node.name.equals(location.getMethodName().name()) && node.desc.equals(location.getMethodDesc()))
        );
        List<MutationDetails>  mutations = mutater.findMutations(location.getClassName());
        if(mutations.size() != 1) {
            throw new AssertionError("Expecting only one mutation at " + location + " got: " + mutations.size() + " " +
                    "instead");
        }
        Mutant mutant = mutater.getMutation(mutations.get(0).getId());
        return loadClass(location.getClassName().asJavaName(), mutant.getBytes());
    }

}
