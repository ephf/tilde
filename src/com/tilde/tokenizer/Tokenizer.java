package com.tilde.tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class Tokenizer {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Token> cur_token;
    private final CharStepper stepper;

    public Tokenizer(String data) throws TokenizerException {
        stepper = new CharStepper(data);
        cur_token = generate_token(stepper);
    }

    private record MacroChunk(String data, int arg_index) {}
    private static final HashMap<String, ArrayList<MacroChunk>> macros = new HashMap<>();
    private static final ArrayList<CharStepper> macro_overrides = new ArrayList<>();

    private static Optional<Token> generate_token(CharStepper stepper) throws TokenizerException {
        CharStepper original_stepper = stepper;
        if(macro_overrides.size() > 0) {
            original_stepper = stepper;
            stepper = macro_overrides.get(macro_overrides.size() - 1);
        }

        while(Character.isWhitespace(stepper.peek().orElse('a')))
            stepper.next();

        if(stepper.peek().isEmpty()) {
            if(macro_overrides.size() > 0) {
                macro_overrides.remove(macro_overrides.size() - 1);
                return generate_token(original_stepper);
            }

            return Optional.empty();
        }

        char first_char = stepper.mark().next().orElse('\0');

        if(Character.isLetter(first_char) || first_char == '_') {
            while(Character.isLetterOrDigit(stepper.peek().orElse('\0')) || stepper.peek().orElse('\0') == '_')
                stepper.next();
            return Optional.of(new Token.Identifier(stepper.get_marker()));
        }

        if(Character.isDigit(first_char)) {
            while(Character.isDigit(stepper.peek().orElse('\0')))
                stepper.next();
            return Optional.of(new Token.Integer(Integer.parseInt(stepper.get_marker()), stepper.get_marker()));
        }

        switch(first_char) {
            case ':' -> {
                if (stepper.has_type(':')) {
                    stepper.next();
                    return Optional.of(new Token.Misc("::"));
                }
            }
            case '-' -> {
                if (stepper.has_type('>')) {
                    stepper.next();
                    return Optional.of(new Token.Misc("->"));
                }
            }
            case '=' -> {
                if (stepper.has_type('>')) {
                    stepper.next();
                    return Optional.of(new Token.Misc("=>"));
                }
            }
            case '/' -> {
                if (stepper.has_type('>')) {
                    stepper.next();
                    StringBuilder command = new StringBuilder();
                    while (stepper.peek().isPresent() && stepper.peek().get() != '/') {
                        char ch = stepper.next().orElse('\0');
                        if (Character.isWhitespace(ch)) {
                            while (stepper.peek().isPresent() && Character.isWhitespace(stepper.peek().get()))
                                stepper.next();
                            command.append(' ');
                            continue;
                        }
                        command.append(ch);
                    }
                    stepper.next();
                    return Optional.of(new Token.Command(command.toString()));
                }
            }
            case '"' -> {
                stepper.mark();
                while (stepper.peek().isPresent() && stepper.peek().get() != '"') {
                    if(stepper.next().orElse('\0') == '\\') stepper.next();
                }
                Token token = new Token.StringValue(stepper.get_marker());
                stepper.next();
                return Optional.of(token);
            }
        }

        return Optional.of(new Token.Misc(stepper.get_marker()));
    }

    public Optional<Token> peek() {
        return cur_token;
    }

    public Token next() throws TokenizerException {
        if(peek().isEmpty()) throw TokenizerException.expected_token();
        Token token = peek().get();
        cur_token = generate_token(stepper);
        System.out.println(token);
        return token;
    }

    public boolean has_type(Class<? extends Token> type) {
        return peek().isPresent() && peek().get().getClass().equals(type);
    }

    public boolean has_type(String type) {
        return peek().isPresent() && peek().get() instanceof Token.Misc
                && ((Token.Misc) peek().get()).data().equals(type);
    }

    public Token expect_type(Class<? extends Token> type) throws TokenizerException {
        Token token = next();
        if(token.getClass().equals(type))
            return token;
        throw TokenizerException.expected_type(type, token);
    }

    public Token expect_type(String type) throws TokenizerException {
        Token token = next();
        if(token instanceof Token.Misc && ((Token.Misc) token).data().equals(type))
            return token;
        throw TokenizerException.expected_type(type, token);
    }

}
