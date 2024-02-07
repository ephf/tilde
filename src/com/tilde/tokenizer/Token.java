package com.tilde.tokenizer;

public interface Token {

    String data();

    record Identifier(String data) implements Token {}
    record Integer(int value, String data) implements Token {}
    record Misc(String data) implements Token {}

}
