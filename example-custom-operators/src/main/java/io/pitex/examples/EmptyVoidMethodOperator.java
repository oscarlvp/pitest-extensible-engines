package io.pitex.examples;

import io.pitex.engines.amazo.operators.Description;
import io.pitex.engines.amazo.operators.MethodRewritingOperator;
import org.pitest.reloc.asm.Opcodes;
import org.pitest.reloc.asm.Type;
import org.pitest.reloc.asm.tree.ClassNode;
import org.pitest.reloc.asm.tree.InsnList;
import org.pitest.reloc.asm.tree.InsnNode;
import org.pitest.reloc.asm.tree.MethodNode;

@Description("Removes the body of void methods")
public class EmptyVoidMethodOperator extends MethodRewritingOperator {

    @Override
    public InsnList generateCode(MethodNode method, ClassNode owner) {
        InsnList code = new InsnList();
        code.add(new InsnNode(Opcodes.RETURN));
        return code;
    }

    @Override
    public boolean willMutateMethod(MethodNode method, ClassNode owner) {
        Type returnType = Type.getMethodType(method.desc).getReturnType();
        return Type.VOID_TYPE.equals(returnType) &&
                !(method.name.equals("<init>") || method.name.equals("<clinit>"));
    }

}
