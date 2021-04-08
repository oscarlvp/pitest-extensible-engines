package io.pitex.engines.amazo.code;

import org.pitest.coverage.analysis.Block;
import org.pitest.coverage.analysis.ControlFlowAnalyser;
import org.pitest.reloc.asm.tree.AbstractInsnNode;
import org.pitest.reloc.asm.tree.LabelNode;
import org.pitest.reloc.asm.tree.MethodNode;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockTracker {

    private final List<Block> blocks;
    private final Set<LabelNode> handlers;
    private int currentBlockIndex = 0;
    private int currentInstructionIndex = -1;
    private boolean inFinallyBlock = false;

    public BlockTracker(MethodNode method) {
        this.blocks = ControlFlowAnalyser.analyze(method);
        this.handlers = method.tryCatchBlocks.stream().map((block) -> block.handler).collect(Collectors.toSet());
    }

    public void update(AbstractInsnNode instruction) {
        currentInstructionIndex++;
        if(shouldChangeBlock()) {
            currentBlockIndex++;
            inFinallyBlock = (instruction instanceof LabelNode) && handlers.contains(instruction);
        }
    }

    private boolean shouldChangeBlock() {
        Block block = blocks.get(currentBlockIndex);
        return currentInstructionIndex < block.getFirstInstruction() ||
                currentInstructionIndex > block.getLastInstruction();
    }

    public int getCurrentBlockIndex() {
        return currentBlockIndex;
    }

    public boolean isInFinallyBlock() {
        return inFinallyBlock;
    }

}
