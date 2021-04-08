package io.pitex.engines.amazo.operators;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.reloc.asm.Opcodes;
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
import static org.hamcrest.Matchers.hasSize;

class InstructionRemovalOperatorTest {

    @Test
    void  shouldFindMutation() {
        List<MutationDetails> mutations = findMutations(Target.class, new IntegerNegationRemoval());
        assertThat(mutations, hasSize(1));
    }

    @Test
    void shouldApplyMutation() {
        IntegerNegationRemoval operator = new IntegerNegationRemoval();
        Class<?> mutant = mutate(in(Target.class).method("opposite", int.class),  operator);
        Object instance = createInstance(mutant).usingDefaultConstructor();
        int operand = 3;
        Object result = on(instance).invoke("opposite", int.class).with(operand);
        assertThat(result, IsEqual.equalTo(operand));
    }

    @Description("Removes integer negation instructions")
    static class IntegerNegationRemoval extends InstructionRemovalOperator<InsnNode> {

        public IntegerNegationRemoval() {
            super(InsnNode.class);
        }

        @Override
        public boolean canMutate(InsnNode instruction, MethodNode method, ClassNode owner) {
            return instruction.getOpcode() == Opcodes.INEG;
        }
    }

    public static class Target {
         public int opposite(int value) {
             return -value;
         }
    }

}