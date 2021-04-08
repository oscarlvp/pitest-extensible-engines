package io.pitex.engines.amazo.operators;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.reloc.asm.tree.ClassNode;
import org.pitest.reloc.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.pitex.engines.amazo.code.LineTracker.firstLineOf;

public abstract class MethodOperator extends MutationOperator {

    @Override
    public List<MutationDetails> findMutations(MethodNode method, ClassNode owner) {
        if(!canMutate(method, owner)) return Collections.emptyList();
        return List.of(
                new MutationDetails(
                        new MutationIdentifier(
                                Location.location(
                                        ClassName.fromString(owner.name),
                                        MethodName.fromString(method.name),
                                        method.desc
                                ),
                                // TODO: Returning 1 for now until clarifying the role of MutationDetails
                                //  .getInstructionIndex
                                IntStream.range(1, method.instructions.size()).boxed().collect(Collectors.toList()),
                                identifier()
                        ),
                        owner.sourceFile,
                        description(),
                        firstLineOf(method.instructions),
                        0)
        );
    }

    public boolean canMutate(MethodNode method, ClassNode owner) {
        return canPerformMutation(method) && willMutateMethod(method, owner);
    }

    private boolean canPerformMutation(MethodNode method) {
        return !(Modifier.isAbstract(method.access) || Modifier.isNative(method.access));
    }

    public abstract boolean willMutateMethod(MethodNode method, ClassNode owner);

}
