package io.pitex.engines.amazo.operators;

import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.reloc.asm.Opcodes;
import org.pitest.reloc.asm.Type;
import org.pitest.reloc.asm.tree.*;

import java.lang.reflect.Modifier;

public abstract class MethodResultOperator extends MethodOperator {

    private static final String PREFIX = "__$amazo_delegate_$_";

    @Override
    public void mutate(MutationIdentifier id, MethodNode method, ClassNode owner) {

        InsnList delegation = new InsnList();
        // Push all parameters
        Type[] parameters = getParameters(method, owner);
        for(int index = 0; index < parameters.length; index++) {
            delegation.add(new IntInsnNode(parameters[index].getOpcode(Opcodes.ILOAD), index));
        }

        // Invoke the renamed method
        final String NEW_METHOD_NAME = PREFIX + method.name;
        delegation.add(
                new MethodInsnNode(
                getInvocationOpcode(method, owner),
                owner.name,
                NEW_METHOD_NAME, method.desc, Modifier.isInterface(owner.access))
        );

        // Add the code modifying the result
        delegation.add(generateCode(method, owner));

        // Return the new result
        delegation.add(new InsnNode(getReturnOpcode(method)));

        // Add a new method with the same metadata
        // and the delegation code
        MethodNode newMethod = new MethodNode(
                method.access,
                method.name,
                method.desc,
                method.signature,
                method.exceptions.toArray(String[]::new)
        );
        newMethod.instructions = delegation;
        owner.methods.add(newMethod);

        // Rename the original method
        method.name = NEW_METHOD_NAME;
    }

    private int getInvocationOpcode(MethodNode method, ClassNode owner) {
        if(Modifier.isStatic(method.access))
            return Opcodes.INVOKESTATIC;
        if(Modifier.isInterface(owner.access))
            return Opcodes.INVOKEINTERFACE;
        return Opcodes.INVOKEVIRTUAL;
    }

    private int getReturnOpcode(MethodNode method) {
        return Type.getReturnType(method.desc).getOpcode(Opcodes.IRETURN);
    }

    private Type[] getParameters(MethodNode method, ClassNode owner) {
        Type[] argumentTypes = Type.getMethodType(method.desc).getArgumentTypes();
        if(Modifier.isStatic(method.access)) {
            return argumentTypes;
        }
        Type[] result = new Type[1 + argumentTypes.length];
        System.arraycopy(argumentTypes, 0, result, 1, argumentTypes.length);
        result[0] = Type.getObjectType(owner.name);
        return result;
    }

    public abstract InsnList generateCode(MethodNode method, ClassNode owner);
}
