package io.pitex.engines.amazo.operators;

import org.pitest.reloc.asm.tree.AbstractInsnNode;
import org.pitest.reloc.asm.tree.ClassNode;
import org.pitest.reloc.asm.tree.InsnList;
import org.pitest.reloc.asm.tree.MethodNode;

public abstract class InstructionReplacementOperator<T extends AbstractInsnNode> extends InstructionOperator<T> {

    public InstructionReplacementOperator(Class<T> type) {
        super(type);
    }

    @Override
    public InsnList replacementFor(T instruction, MethodNode method, ClassNode owner) {
        InsnList result = new InsnList();
        result.add(getReplacementInstruction(instruction, method, owner));
        return result;
    }

    public abstract AbstractInsnNode getReplacementInstruction(T instruction, MethodNode method, ClassNode owner);

}
