package com.tilde.tokenizer;

public class TokenizerException extends Exception {
    public TokenizerException(String message) {
        super(message);
    }

    public static TokenizerException expected_token() {
        return new TokenizerException("Expected a token");
    }

    public static TokenizerException expected_type(Class<? extends Token> exp, Token token) {
        return new TokenizerException("Expected token type " + exp + ", but got " + token);
    }

    public static TokenizerException expected_type(String exp, Token token) {
        return new TokenizerException("Expected token type " + exp + ", but got " + token);
    }
}