package com.traker.shared;

/**
 * Created by Daniel on 15.6.2017 г..
 */

public class test {
    private static final test ourInstance = new test();

    public static test getInstance() {
        return ourInstance;
    }

    private test() {
    }
}
