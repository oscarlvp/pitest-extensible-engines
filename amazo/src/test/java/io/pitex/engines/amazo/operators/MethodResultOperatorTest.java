package io.pitex.engines.amazo.operators;

import io.pitex.engines.amazo.testutils.DynamicClassLoader;
import org.junit.jupiter.api.Test;
import org.pitest.reloc.asm.Label;
import org.pitest.reloc.asm.Opcodes;
import org.pitest.reloc.asm.Type;
import org.pitest.reloc.asm.tree.*;

import static io.pitex.engines.amazo.testutils.EngineHelper.mutate;
import static io.pitex.engines.amazo.testutils.MutationLocation.in;
import static io.pitex.engines.amazo.testutils.ReflectionHelper.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class MethodResultOperatorTest {

    @Test
    void shouldMutateMethodReturningPrimitiveType() {
        NegateBooleanResult operator = new NegateBooleanResult();
        Class<?> mutant = mutate(in(Target.class).method("odd", int.class), operator);
        Object instance = createInstance(mutant).usingDefaultConstructor();
        boolean result = (boolean)on(instance).invoke("odd", int.class).with(1);
        assertFalse(result, "Mutated method should have returned false");
    }

    @Test
    void shouldMutateMethodReturningString() {
        RemoveFirstChar operator = new RemoveFirstChar();
        Class<?> mutant = mutate(in(Target.class).method("representation", int.class), operator);
        Object instance = createInstance(mutant).usingDefaultConstructor();
        String result = (String)on(instance).invoke("representation", int.class).with(1);
        assertThat(result, emptyString());
    }


    @Test
    void shouldMutateMethodWithNoParameters() {
        DuplicateIntegerResult operator = new DuplicateIntegerResult();
        Class<?> mutant = mutate(in(Target.class).method("constant"), operator);
        Object instance = createInstance(mutant).usingDefaultConstructor();
        int result = (int)on(instance).invoke("constant");
        assertEquals(84, result, "Operator should have duplicated the constant");
    }

    @Test
    void shouldMutateMethodWithMoreThanOneParameter() {
        DuplicateIntegerResult operator = new DuplicateIntegerResult();
        Class<?> mutant = mutate(in(Target.class).method("add", int.class, int.class), operator);
        Object instance = createInstance(mutant).usingDefaultConstructor();
        int result = (int)on(instance).invoke("add", int.class, int.class).with(1, 2);
        assertEquals(6, result, "Operator should have duplicated the addition");
    }


    @Test
    void shouldMutateStaticMethodInInterface() {
        DuplicateIntegerResult operator = new DuplicateIntegerResult();
        Class<?> mutant = mutate(in(ITarget.class).method("plusTwo", int.class), operator);
        int operand = 2;
        int result = (int)onClass(mutant).invoke("plusTwo", int.class).with(operand);
        int expected = ITarget.plusTwo(operand) * 2;
        assertEquals(expected, result, "Mutated method should return original result duplicated");
    }

    @Test
    void shouldMutateStaticMethodWithNoParameters() {
        DuplicateIntegerResult operator = new DuplicateIntegerResult();
        Class<?> mutant = mutate(in(Target.class).method("one"), operator);
        int result = (int)onClass(mutant).invoke("one");
        assertEquals(2, result, "Operator should have duplicated the result");
    }

    @Test
    void shouldMutateInterfaceMethod() {
        DuplicateIntegerResult operator = new DuplicateIntegerResult();
        Class<?> mutant = mutate(in(ITarget.class).method("plusThree", int.class), operator);
        Class<?> mutantImplementation = ((DynamicClassLoader)mutant.getClassLoader()).load(ITargetImplementation.class);
        Object instance = createInstance(mutantImplementation).usingDefaultConstructor();
        int operand = 2;
        int result = (int)on(instance).invoke("plusThree", int.class).with(operand);
        int expected =  new ITargetImplementation().plusThree(operand) * 2;
        assertEquals(expected, result, "Mutated method should return original result duplicated");
    }


    // Mutation operators

    @Description("Negates a boolean result")
    public static class NegateBooleanResult extends MethodResultOperator {

        @Override
        public InsnList generateCode(MethodNode method, ClassNode owner) {
            InsnList code = new InsnList();
            LabelNode falseBranch = new LabelNode(new Label());
            LabelNode end = new LabelNode(new Label());
            code.add(new JumpInsnNode(Opcodes.IFNE, falseBranch));
            code.add(new InsnNode(Opcodes.ICONST_0));
            code.add(new JumpInsnNode(Opcodes.GOTO, end));
            code.add(falseBranch);
            code.add(new InsnNode(Opcodes.ICONST_1));
            code.add(end);
            return code;
        }

        @Override
        public boolean willMutateMethod(MethodNode method, ClassNode owner) {
            return  Type.BOOLEAN_TYPE.equals(Type.getMethodType(method.desc).getReturnType());
        }
    }


    @Description("Duplicates integer results")
    public static class DuplicateIntegerResult extends MethodResultOperator {

        @Override
        public InsnList generateCode(MethodNode method, ClassNode owner) {
            InsnList code = new InsnList();
            code.add(new InsnNode(Opcodes.DUP));
            code.add(new InsnNode(Opcodes.IADD));
            return code;
        }

        @Override
        public boolean willMutateMethod(MethodNode method, ClassNode owner) {
            return Type.getReturnType(method.desc).equals(Type.INT_TYPE);
        }
    }

    @Description("Removes the first character of any string result")
    public static class RemoveFirstChar extends MethodResultOperator {

        @Override
        public InsnList generateCode(MethodNode method, ClassNode owner) {

            InsnList code = new InsnList();
            final String STRING = Type.getType(String.class).getInternalName();
            LabelNode end = new LabelNode(new Label());

            // Original result value on top of the stack
            // Duplicate for check
            code.add(new InsnNode(Opcodes.DUP));
            // If null, go to end
            code.add(new JumpInsnNode(Opcodes.IFNULL, end));

            // If empty, go to end
            code.add(new InsnNode(Opcodes.DUP));
            // Duplicate again for empty check
            code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, STRING, "isEmpty", "()Z"));
            code.add(new JumpInsnNode(Opcodes.IFNE, end));

            // Remove the first character
            code.add(new InsnNode(Opcodes.ICONST_1));
            code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, STRING, "substring", "(I)Ljava/lang/String;"));
            code.add(end);

            return code;
        }

        @Override
        public boolean willMutateMethod(MethodNode method, ClassNode owner) {
            return Type.getReturnType(method.desc).equals(Type.getType(String.class));
        }

    }

    // Mutation targets

    public interface ITarget {
        static int plusTwo(int value) { return 2 + value; }

        default int plusThree(int value) { return 3 + value; }
    }

    public static class ITargetImplementation implements ITarget {

    }

    public static class Target {

        public int duplicate(int value) {
            return 2 * value;
        }

        public static int triplicate(int value) { return 3 * value; }

        public int constant() { return 42; }

        public static int one() { return 1;  }

        public int add(int a, int b) { return a + b; }

        public boolean odd(int value) { return value %2 == 0; }

        public String representation(int value)  { return Integer.toString(value, 2); }

    }

}