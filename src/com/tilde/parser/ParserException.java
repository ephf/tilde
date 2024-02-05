package com.tilde.parser;

public class ParserException extends Exception {

    public ParserException(String message) {
        super(message);
    }

    public static ParserException type_mismatch(Type a, Type b) {
        return new ParserException("Type mismatch: " + a + " and " + b);
    }

}
