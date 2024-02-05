package com.tilde.parser;

import com.tilde.parser.node.Scope;
import com.tilde.tokenizer.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Parser extends Tokenizer {

    public final HashMap<String, ArrayList<String>> output = new HashMap<>();

    public Parser(String data) throws TokenizerException {
        super(data);
    }

    public HashMap<String, ArrayList<String>> parse()  throws TokenizerException, ParserException {
        Context.Scope std = new Context.Scope();
        Context context = new Context("load");
        context.stack().add(std);

        context.commands().add("scoreboard objectives add __tilde.process dummy");
        context.commands().add("scoreboard objectives add __tilde.static dummy");
        context.commands().add("data modify storage __tilde:stack stack set value [{}]");
        context.commands().add("data modify storage __tilde:in_args stack set value []");

        Scope.parse(this, context);

        context.into(output);
        return output;
    }

}
