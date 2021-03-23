package io.pitex.engines.amazo;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.reloc.asm.Opcodes;
import org.pitest.reloc.asm.tree.AbstractInsnNode;
import org.pitest.reloc.asm.tree.ClassNode;
import org.pitest.reloc.asm.tree.InsnNode;
import org.pitest.reloc.asm.tree.MethodNode;

import java.util.List;

import static io.pitex.engines.amazo.testutils.EngineHelper.findMutations;
import static io.pitex.engines.amazo.testutils.EngineHelper.mutate;
import static io.pitex.engines.amazo.testutils.ReflectionHelper.createInstance;
import static io.pitex.engines.amazo.testutils.ReflectionHelper.on;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

class InstructionReplacementOperatorTest {

    @Test
    void testInstructionReplacementFindsMutation() {
        List<MutationDetails> mutations = findMutations(Target.class, new AdditionReplacement());
        assertThat(mutations, hasSize(1));
    }

    @Test
    void testOperatorReplacedInMutation() {
        AdditionReplacement operator = new AdditionReplacement();
        Class<?> mutant = mutate(operator, new MutationIdentifier(
                Location.location(
                        ClassName.fromClass(Target.class),
                        MethodName.fromString("addition"),
                        "(II)I"),
                4,
                operator.identifier()
        ));

        Object instance = createInstance(mutant).usingDefaultConstructor();

        int first = 7, second = 4;
        Object result = on(instance).invoke("addition", int.class, int.class).with(first, second);
        assertThat(result, equalTo(first - second));
    }


    @Description("Replaces integer addition by integer subtraction")
    static class AdditionReplacement  extends InstructionReplacementOperator<InsnNode> {

        public AdditionReplacement() {
            super(InsnNode.class);
        }

        @Override
        public boolean canMutate(InsnNode instruction, MethodNode method, ClassNode owner) {
            return instruction.getOpcode() == Opcodes.IADD;
        }

        @Override
        public AbstractInsnNode replacementFor(InsnNode instruction, MethodNode method, ClassNode owner) {
            return new InsnNode(Opcodes.ISUB);
        }
    }

    public static class Target {
        public int addition(int x, int y) {
            return x + y;
        }
    }
}