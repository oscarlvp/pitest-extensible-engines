package io.pitex.examples;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

public class Stack<T> {

    public static final int DEFAULT_CAPACITY = 100;
    public static final double DEFAULT_GROWTH_RATE = 1.5;

    private T[] elements;
    private final double growthRate;
    private int size = 0;


    public Stack(Class<T> type) {
        this(type, DEFAULT_CAPACITY, DEFAULT_GROWTH_RATE);
    }

    public Stack(Class<T> type, int capacity, double growthRate) {
        Objects.requireNonNull(type);

        if(capacity < 1) throw new IllegalArgumentException("Initial capacity should be greater than 0");
        elements = (T[])(Array.newInstance(type, capacity));

        if(growthRate <= 1) throw new IllegalArgumentException("Growth rate should be greater than one");
        this.growthRate = growthRate;
    }

    private void increaseCapacity() {
        elements = Arrays.copyOf(elements, (int)Math.ceil(elements.length * growthRate));
    }

    public int capacity() {
        return elements.length;
    }

    public int size() {
        return size;
    }

    public boolean isFull() {
        return capacity() == size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void push(T element) {
        if(isFull()) increaseCapacity();
        elements[size] = element;
        size++;
    }

    public T peek() {
        if(isEmpty()) throw new IllegalStateException("Empty stack");
        return elements[size - 1];
    }

    public T pop() {
        T result = peek();
        size--;
        return result;
    }
}
