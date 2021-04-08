package io.pitex.engines.amazo.operators;

import io.pitex.engines.amazo.code.BlockTracker;
import io.pitex.engines.amazo.code.LineTracker;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.*;
import org.pitest.reloc.asm.tree.AbstractInsnNode;
import org.pitest.reloc.asm.tree.ClassNode;
import org.pitest.reloc.asm.tree.InsnList;
import org.pitest.reloc.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public abstract class InstructionOperator<T extends AbstractInsnNode> extends MutationOperator {

    protected final Class<T> type;

    public InstructionOperator(Class<T> type) {
        Objects.requireNonNull(type, "Instruction node type for instruction replacement can not be null");
        this.type = type;
    }

    @Override
    public List<MutationDetails> findMutations(MethodNode method, ClassNode owner) {

        ArrayList<MutationDetails> result = new ArrayList<>();
        ListIterator<AbstractInsnNode> it = method.instructions.iterator();
        final Location location = Location.location(
                ClassName.fromString(owner.name),
                MethodName.fromString(method.name),
                method.desc
        );
        final String opId = identifier();
        final String opDesc = description();

        BlockTracker blockTracker = new BlockTracker(method);
        LineTracker lineTracker = new LineTracker();

        while (it.hasNext()) {
            int index = it.nextIndex();
            AbstractInsnNode instruction = it.next();
            blockTracker.update(instruction);
            lineTracker.update(instruction);
            if (type.isAssignableFrom(instruction.getClass())) {
                T target = (T) instruction;
                if (canMutate(target, method, owner)) {
                    result.add(new MutationDetails(
                            new MutationIdentifier(location, index, opId),
                            owner.sourceFile, opDesc,
                            lineTracker.getCurrentLine(),
                            blockTracker.getCurrentBlockIndex(),
                            blockTracker.isInFinallyBlock(),
                            getPoisonStatus(target, method, owner))
                    );
                }
            }
        }
        return result;
    }

    public PoisonStatus getPoisonStatus(T instruction, MethodNode method, ClassNode owner) {
        if(method.name.equals("<clinit>"))
            return PoisonStatus.IS_STATIC_INITIALIZER_CODE;
        if(mutationMayPoisonJVM(instruction, method, owner))
            return PoisonStatus.MAY_POISON_JVM;
        return PoisonStatus.NORMAL;
    }

    public boolean mutationMayPoisonJVM(T instruction, MethodNode method, ClassNode owner) {
        return false;
    }

    @Override
    public void mutate(MutationIdentifier id, MethodNode method, ClassNode owner) {

        assert id.getFirstIndex() >= 0 && id.getFirstIndex() < method.instructions.size()
                : "Wrong index  for mutation identifier"
                ;

        InsnList body = method.instructions;
        AbstractInsnNode instruction = body.get(id.getFirstIndex());

        assert type.isAssignableFrom(instruction.getClass())
                : "Wrong type for target instruction. Expecting: " + type.getName() + " got " + instruction.getClass().getName()
                ;

        body.insertBefore(instruction, replacementFor((T)instruction, method, owner));
        body.remove(instruction);

    }

    public abstract InsnList replacementFor(T instruction, MethodNode method, ClassNode owner);

    public abstract boolean canMutate(T instruction, MethodNode method, ClassNode owner);

}
