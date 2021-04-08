package io.pitex.engines.amazo.operators;

import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.reloc.asm.tree.ClassNode;
import org.pitest.reloc.asm.tree.InsnList;
import org.pitest.reloc.asm.tree.MethodNode;


public abstract class MethodRewritingOperator extends MethodOperator {

    @Override
    public void mutate(MutationIdentifier id, MethodNode method, ClassNode owner) {
        method.instructions = generateCode(method, owner);
    }

    public abstract InsnList generateCode(MethodNode method, ClassNode owner);
}
