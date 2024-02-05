package com.tilde.tokenizer;

import java.util.Optional;

public class CharStepper {

    private final String data;
    private int index = 0;
    private int marker = 0;

    public CharStepper(String data) {
        this.data = data;
    }

    public Optional<Character> peek() {
        if(index >= data.length()) return Optional.empty();
        return Optional.of(data.charAt(index));
    }

    public Optional<Character> next() {
        if(index >= data.length()) return Optional.empty();
        return Optional.of(data.charAt(index++));
    }

    public boolean has_type(char ch) {
        return peek().isPresent() && peek().get() == ch;
    }

    public CharStepper mark() {
        marker = index;
        return this;
    }

    public String get_marker() {
        return data.substring(marker, index);
    }

}
