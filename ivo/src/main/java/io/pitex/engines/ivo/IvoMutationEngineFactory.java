package io.pitex.engines.ivo;

import org.pitest.mutationtest.EngineArguments;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.util.ServiceLoader;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IvoMutationEngineFactory implements MutationEngineFactory {

    Optional<EngineArguments> getArgumentsFor(MutationEngineFactory factory, EngineArguments engineArguments) {
        List<String> configuredMutators =
                engineArguments.mutators().stream()
                        .filter(mutator -> mutator.startsWith(factory.name()))
                        .collect(Collectors.toList());
        if(configuredMutators.isEmpty())
            return Optional.empty();

        final int index = factory.name().length() + 1;
        return Optional.of(EngineArguments.arguments()
                .withExcludedMethods(engineArguments.excludedMethods())
                .withMutators(
                        configuredMutators.stream()
                                .filter(mutator -> mutator.length() > index)
                                .map(mutator -> mutator.substring(index))
                        .collect(Collectors.toList())
                ));
    }

    Optional<MutationEngine> maybeCreate(MutationEngineFactory factory, EngineArguments arguments) {
        return getArgumentsFor(factory, arguments).map(factory::createEngine);
    }

    Collection<MutationEngineFactory> getAvailableFactories() {
        return ServiceLoader.load(MutationEngineFactory.class, getClass().getClassLoader());
    }

    public MutationEngine createEngine(EngineArguments engineArguments) {
        Collection<MutationEngineFactory> factories = getAvailableFactories();
        return new IvoMutationEngine(factories.stream()
                .map(factory -> maybeCreate(factory, engineArguments))
                .flatMap(Optional::stream)
                .collect(Collectors.toList()));
    }

    public String name() {
        return IvoMutationEngine.NAME;
    }

    public String description() {
        return "Aggregated mutation engine";
    }
}
