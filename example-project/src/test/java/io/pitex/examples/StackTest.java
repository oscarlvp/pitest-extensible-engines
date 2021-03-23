package io.pitex.examples;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StackTest {

    private static final int SEED = 1000;

    @Test
    public void createEmptyStack() {
        Stack<Integer> stack = new Stack<>(Integer.class);
        assertTrue(stack.isEmpty());
    }

    @Test
    public void sameElementPopAfterPush() {
        Stack<Integer> stack = new Stack<>(Integer.class);
        int element = new Random(SEED).nextInt();
        stack.push(element);
        assertEquals(element, stack.pop().intValue());
    }

}