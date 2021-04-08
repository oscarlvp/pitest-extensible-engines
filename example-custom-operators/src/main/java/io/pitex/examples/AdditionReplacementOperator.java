package io.pitex.examples;

import io.pitex.engines.amazo.operators.Description;
import io.pitex.engines.amazo.operators.InstructionReplacementOperator;
import org.pitest.reloc.asm.tree.AbstractInsnNode;
import org.pitest.reloc.asm.tree.ClassNode;
import org.pitest.reloc.asm.tree.InsnNode;
import org.pitest.reloc.asm.tree.MethodNode;

import static org.pitest.reloc.asm.Opcodes.*;


@Description("Replaces addition operators by subtractions")
public class AdditionReplacementOperator extends InstructionReplacementOperator<InsnNode> {

    public AdditionReplacementOperator() {
        super(InsnNode.class);
    }

    @Override
    public boolean canMutate(InsnNode instruction, MethodNode method, ClassNode owner) {
        switch (instruction.getOpcode()) {
            case IADD:
            case LADD:
            case FADD:
            case DADD:
                return true;
            default:
                return false;
        }
    }

    @Override
    public AbstractInsnNode getReplacementInstruction(InsnNode instruction, MethodNode method, ClassNode owner) {
        return new InsnNode(instruction.getOpcode() + 4); // Subtractions are shifted by 4 with respect to additions
    }
}
