package io.pitex.engines.amazo;

import io.pitex.engines.amazo.operators.MutationOperator;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.EngineArguments;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.reloc.asm.tree.MethodNode;
import org.pitest.util.Glob;
import org.pitest.util.ServiceLoader;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class AmazoMutationEngineFactory implements MutationEngineFactory {

    @Override
    public MutationEngine createEngine(EngineArguments engineArguments) {

        Collection<MutationOperator> operators = getAvailableMutationOperators();

        for (MutationOperator operator : operators) {
            System.out.println("=== " + operator.identifier());
        }

        Set<String> selectedMutators = Set.copyOf(engineArguments.mutators());
        if(!selectedMutators.isEmpty()) {
            operators = operators.stream()
                    .filter(op -> selectedMutators.contains(op.identifier()))
                    .collect(Collectors.toSet());
        }
        Predicate<MethodNode> excludedMethods = (method) -> false;
        if(!engineArguments.excludedMethods().isEmpty()) {
            Predicate<String> criterion = Prelude.or(Glob.toGlobPredicates(engineArguments.excludedMethods()));
            excludedMethods = (method) -> criterion.test(method.name);
        }
        return new AmazoMutationEngine(operators, excludedMethods);
    }

    private Collection<MutationOperator> getAvailableMutationOperators() {

        try {
            Enumeration<URL> resources =
                    getClass().getClassLoader().getResources("META-INF/services/" + MutationOperator.class.getName());
            while (resources.hasMoreElements()) {
                System.out.println("::::===>" + resources.nextElement());
            }
        }catch (IOException exc) {
            System.out.println(exc);
        }

        return ServiceLoader.load(MutationOperator.class,  Thread.currentThread().getContextClassLoader()); //getClass
        // ().getClassLoader());
    }

    @Override
    public String name() {
        return AmazoMutationEngine.NAME;
    }

    @Override
    public String description() {
        return "Extensible mutation engine";
    }

}
