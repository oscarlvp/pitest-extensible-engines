package io.pitex.examples;

public class A {
    public int m(int x) {
        int a = 1;
        if(x == 0) return a + 0;
        if(x == 1) return a + 1;
        if(x == 2) return a + 2;
        if(x == 3) return a + 3;
        return a + 9;
    }
}
