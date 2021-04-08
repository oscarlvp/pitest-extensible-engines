package io.pitex.engines.amazo.operators;

import org.junit.jupiter.api.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.reloc.asm.Opcodes;
import org.pitest.reloc.asm.tree.AbstractInsnNode;
import org.pitest.reloc.asm.tree.ClassNode;
import org.pitest.reloc.asm.tree.InsnNode;
import org.pitest.reloc.asm.tree.MethodNode;

import java.util.List;

import static io.pitex.engines.amazo.testutils.EngineHelper.findMutations;
import static io.pitex.engines.amazo.testutils.EngineHelper.mutate;
import static io.pitex.engines.amazo.testutils.MutationLocation.in;
import static io.pitex.engines.amazo.testutils.ReflectionHelper.createInstance;
import static io.pitex.engines.amazo.testutils.ReflectionHelper.on;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

class InstructionReplacementOperatorTest {

    @Test
    void shouldFindMutation() {
        List<MutationDetails> mutations = findMutations(Target.class, new IntegerAdditionReplacement());
        assertThat(mutations, hasSize(1));
    }

    @Test
    void shouldApplyMutation() {
        IntegerAdditionReplacement operator = new IntegerAdditionReplacement();
        Class<?> mutant = mutate(in(Target.class).method("addition", int.class, int.class), operator);
        Object instance = createInstance(mutant).usingDefaultConstructor();
        int first = 7, second = 4;
        Object result = on(instance).invoke("addition", int.class, int.class).with(first, second);
        assertThat(result, equalTo(first - second));
    }

    @Description("Replaces integer addition by integer subtraction")
    static class IntegerAdditionReplacement extends InstructionReplacementOperator<InsnNode> {

        public IntegerAdditionReplacement() {
            super(InsnNode.class);
        }

        @Override
        public boolean canMutate(InsnNode instruction, MethodNode method, ClassNode owner) {
            return instruction.getOpcode() == Opcodes.IADD;
        }

        @Override
        public AbstractInsnNode getReplacementInstruction(InsnNode instruction, MethodNode method, ClassNode owner) {
            return new InsnNode(Opcodes.ISUB);
        }
    }

    public static class Target {
        public int addition(int x, int y) {
            return x + y;
        }
    }
}