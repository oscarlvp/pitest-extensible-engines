package io.pitex.engines.amazo.operators;

import org.pitest.reloc.asm.tree.AbstractInsnNode;
import org.pitest.reloc.asm.tree.ClassNode;
import org.pitest.reloc.asm.tree.InsnList;
import org.pitest.reloc.asm.tree.MethodNode;

public abstract class InstructionRemovalOperator<T extends AbstractInsnNode> extends InstructionOperator<T> {

    public InstructionRemovalOperator(Class<T> type) {
        super(type);
    }

    @Override
    public InsnList replacementFor(T instruction, MethodNode method, ClassNode owner) {
        return new InsnList();
    }
}
