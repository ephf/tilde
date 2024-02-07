package com.tilde.tokenizer;

public class CharStepper {

    private final String data;
    private int index = 0;
    private int marker = 0;

    public CharStepper(String data) {
        this.data = data;
    }

    public char peek() {
        if(index >= data.length()) return 0;
        return data.charAt(index);
    }

    public char next() {
        if(index >= data.length()) return 0;
        return data.charAt(index++);
    }

    public boolean has_type(char ch) {
        return peek() != 0 && peek() == ch;
    }

    public CharStepper mark() {
        marker = index;
        return this;
    }

    public String get_marker() {
        return data.substring(marker, index);
    }

}
