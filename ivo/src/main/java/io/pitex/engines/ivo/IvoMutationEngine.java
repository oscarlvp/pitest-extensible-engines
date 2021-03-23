package io.pitex.engines.ivo;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationEngine;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class IvoMutationEngine implements MutationEngine {

    public static final String NAME = "ivo";

    private final Collection<MutationEngine> engines;

    public IvoMutationEngine(Collection<MutationEngine> engines) {
        Objects.requireNonNull(engines);
        if (engines.isEmpty())
            throw new IllegalArgumentException("Engine collection can not be empty");
        this.engines = List.copyOf(engines);
    }

    public Mutater createMutator(ClassByteArraySource classByteArraySource) {
        return new IvoMutater(engines.stream().collect(
                toMap(MutationEngine::getName, engine -> engine.createMutator(classByteArraySource))
        ));
    }

    public Collection<String> getMutatorNames() {
        return engines.stream()
                .map(MutationEngine::getMutatorNames)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public String getName() {
        return NAME;
    }
}
