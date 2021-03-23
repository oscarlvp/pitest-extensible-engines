package io.pitex.engines.amazo;

import org.junit.jupiter.api.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.reloc.asm.Opcodes;
import org.pitest.reloc.asm.Type;
import org.pitest.reloc.asm.tree.*;
import java.util.List;
import static io.pitex.engines.amazo.testutils.EngineHelper.findMutations;
import static io.pitex.engines.amazo.testutils.EngineHelper.mutate;
import static io.pitex.engines.amazo.testutils.ReflectionHelper.createInstance;
import static io.pitex.engines.amazo.testutils.ReflectionHelper.on;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;


class MethodRewritingOperatorTest {

    @Test
    void shouldFindMutation() {
        List<MutationDetails> mutations = findMutations(Target.class, new ReturnFalseOperator());
        assertThat(mutations, hasSize(1));
        MutationDetails details = mutations.get(0);
        assertThat(details.getMethod(), equalTo(MethodName.fromString("lessThan")));
    }

    @Test
    void shouldPerformMutation() {
        ReturnFalseOperator operator = new ReturnFalseOperator();
             Class<?> mutant = mutate(operator, new MutationIdentifier(
                Location.location(
                        ClassName.fromClass(Target.class),
                        MethodName.fromString("lessThan"),
                        "(II)Z"),
                1,
                operator.identifier()
        ));
        Object instance = createInstance(mutant).usingDefaultConstructor();
        int first = 4, second = 7;
        Object result = on(instance).invoke("lessThan", int.class, int.class).with(first, second);
        assertFalse((boolean) result);
    }

    @Description("Replaces the code of a boolean method by return false")
    public static class ReturnFalseOperator extends MethodRewritingOperator {

        @Override
        public boolean canMutate(MethodNode method, ClassNode owner) {
            return Type.getReturnType(method.desc).equals(Type.BOOLEAN_TYPE);
        }

        @Override
        public InsnList generateCode(MethodNode method, ClassNode owner) {
            InsnList list = new InsnList();
            list.add(new LdcInsnNode(false));
            list.add(new InsnNode( Type.BOOLEAN_TYPE.getOpcode(Opcodes.IRETURN)));
            return list;
        }
    }

    public static class Target {

        public boolean lessThan(int a, int b) {
            return a < b;
        }

        public int multiply(int a, int b) {
            return a * b;
        }

    }

}