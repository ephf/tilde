package com.tilde.tokenizer;

public class Tokenizer {

    private Token cur_token;
    private final CharStepper stepper;

    public Tokenizer(String data) {
        stepper = new CharStepper(data);
        cur_token = generate_token(stepper);
    }

    private static Token generate_token(CharStepper stepper) {
        while(stepper.peek() != 0 && Character.isWhitespace(stepper.peek()))
            stepper.next();

        if(stepper.peek() == 0)
            return null;

        char first_char = stepper.mark().next();

        if(Character.isLetter(first_char) || first_char == '_') {
            while(Character.isLetterOrDigit(stepper.peek()) || stepper.peek() == '_')
                stepper.next();
            return new Token.Identifier(stepper.get_marker());
        }

        if(Character.isDigit(first_char)) {
            while(Character.isDigit(stepper.peek()))
                stepper.next();
            return new Token.Integer(Integer.parseInt(stepper.get_marker()), stepper.get_marker());
        }

        switch(first_char) {
            case ':' -> {
                if (stepper.has_type(':')) {
                    stepper.next();
                    return new Token.Misc("::");
                }
            }
            case '-' -> {
                if (stepper.has_type('>')) {
                    stepper.next();
                    return new Token.Misc("->");
                }
            }
        }

        return new Token.Misc(stepper.get_marker());
    }

    public Token peek() {
        return cur_token;
    }

    public Token next() throws TokenizerException {
        if(peek() == null) throw TokenizerException.expected_token();
        Token token = peek();
        cur_token = generate_token(stepper);
        // debug
        System.out.println(token);
        return token;
    }

    public boolean has_type(Class<? extends Token> type) {
        return peek() != null && peek().getClass().equals(type);
    }

    public boolean has_type(String type) {
        return peek() != null && peek() instanceof Token.Misc misc && misc.data().equals(type);
    }

    public Token expect_type(String type) throws TokenizerException {
        if(!has_type(type)) throw TokenizerException.expected_type(type, peek());
        return next();
    }

    @SuppressWarnings("unchecked")
    public <T extends Token> T expect_type(Class<T> type) throws TokenizerException {
        if(!has_type(type)) throw TokenizerException.expected_type(type, peek());
        return (T) next();
    }

}
