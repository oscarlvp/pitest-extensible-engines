package io.pitex.engines.amazo.code;

import org.pitest.reloc.asm.tree.AbstractInsnNode;
import org.pitest.reloc.asm.tree.InsnList;
import org.pitest.reloc.asm.tree.LineNumberNode;

public class LineTracker {

    private int currentLine = -1;

    public int getCurrentLine() {
        return currentLine;
    }

    public void update(AbstractInsnNode instruction) {
        if(instruction instanceof LineNumberNode) {
            currentLine = ((LineNumberNode)instruction).line;
        }
    }

    public static int firstLineOf(InsnList instructions) {
        int result = Integer.MAX_VALUE;
        for (AbstractInsnNode ins : instructions) {
            if (ins instanceof LineNumberNode) {
                result = Math.min(result, ((LineNumberNode)ins).line);
            }
        }
        return result;
    }

}
