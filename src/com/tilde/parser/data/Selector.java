package com.tilde.parser.data;

public interface Selector {

    record Name(String name) implements Selector {
        public String toString() {
            return name();
        }
    }

}
