package io.pitex.engines.ivo;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class IvoMutater implements Mutater {

    private final Map<String, Mutater> mutaters;

    public IvoMutater(Map<String, Mutater> mutaters) {
        Objects.requireNonNull(mutaters);
        this.mutaters = mutaters;
    }

    @Override
    public Mutant getMutation(MutationIdentifier mutationIdentifier) {
        String decoratedMutator = mutationIdentifier.getMutator();
        int separator = decoratedMutator.indexOf(':');
        String engine = decoratedMutator.substring(0, separator);
        String mutator = decoratedMutator.substring(separator + 1);
        return mutaters.get(engine).getMutation(setMutator(mutationIdentifier, mutator));
    }

    private MutationDetails decorate(MutationDetails details, String engine) {
        return setMutator(details, engine + ":" + details.getMutator());
    }

    private MutationDetails setMutator(MutationDetails details, String mutator) {
        return new MutationDetails(
                setMutator(details.getId(), mutator),
                details.getFilename(),
                details.getDescription(),
                details.getLineNumber(),
                details.getBlock(),
                details.isInFinallyBlock(),
                toPoisonStatus(details)
        );
    }

    private MutationIdentifier setMutator(MutationIdentifier id, String mutator) {
        return new MutationIdentifier(id.getLocation(), id.getIndexes(), mutator);
    }

    PoisonStatus toPoisonStatus(MutationDetails details) {
        if(!details.mayPoisonJVM()) return PoisonStatus.NORMAL;
        if(details.isInStaticInitializer()) return PoisonStatus.IS_STATIC_INITIALIZER_CODE;
        return PoisonStatus.MAY_POISON_JVM;
    }

    @Override
    public List<MutationDetails> findMutations(ClassName className) {
        return mutaters.entrySet().stream()
                .flatMap(entry ->
                        entry.getValue().findMutations(className)
                                .stream()
                                .map(mutation -> decorate(mutation, entry.getKey()))
                ).collect(Collectors.toList());
    }

}
