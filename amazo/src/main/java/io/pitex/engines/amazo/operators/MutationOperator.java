package io.pitex.engines.amazo.operators;

import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.reloc.asm.tree.ClassNode;
import org.pitest.reloc.asm.tree.MethodNode;

import java.util.List;

public abstract class MutationOperator implements ClientClasspathPlugin {

    public abstract List<MutationDetails> findMutations(MethodNode method, ClassNode owner);

    public abstract void mutate(MutationIdentifier id, MethodNode method, ClassNode owner);

    public String identifier() {
        return getClass().getName();
    }

    @Override
    public String description() {
        Description description = getClass().getAnnotation(Description.class);
        if(description == null)
            throw new IllegalStateException("Requested description for unannotated mutation operator class " + getClass().getName());
        return getClass().getAnnotation(Description.class).value();
    }
}
