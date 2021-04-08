package io.pitex.engines.amazo;

import io.pitex.engines.amazo.operators.MutationOperator;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.reloc.asm.tree.MethodNode;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AmazoMutationEngine implements MutationEngine {
    public static final String NAME = "amazo";

    private final Set<MutationOperator> operators;
    private final Predicate<MethodNode> excludedMethods;

    public AmazoMutationEngine(Collection<MutationOperator> operators, Predicate<MethodNode> excludedMethods) {
        Objects.requireNonNull(operators);
        if(operators.isEmpty())
            throw new IllegalStateException("Engine requires one mutation operator at least");
        this.operators = Set.copyOf(operators);
        Objects.requireNonNull(excludedMethods, "Method exclusion criterion can not be null");
        this.excludedMethods = excludedMethods;
    }

    @Override
    public Mutater createMutator(ClassByteArraySource classByteArraySource) {
        return new AmazoMutater(classByteArraySource, operators, excludedMethods);
    }

    @Override
    public Collection<String> getMutatorNames() {
        return operators.stream().map(MutationOperator::identifier).collect(Collectors.toSet());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
