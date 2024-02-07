package com.tilde.parser;

import com.tilde.parser.node.Scope;
import com.tilde.tokenizer.Tokenizer;
import com.tilde.tokenizer.TokenizerException;

import java.util.ArrayList;
import java.util.HashMap;

public class Parser extends Tokenizer {

    public Parser(String data) {
        super(data);
    }

    public HashMap<String, ArrayList<String>> parse(String module_name) throws TokenizerException, ParserException {
        HashMap<String, ArrayList<String>> files = new HashMap<>();
        Context context = new Context(module_name, files);

        context.command("scoreboard objectives add __tilde.process dummy");
        Scope.parse(this, context);
        context.command("tellraw @a [{\"text\":\"DEBUG\\n\",\"bold\":true,\"color\":\"blue\"}," +
                "{\"nbt\":\"{}\",\"storage\":\"lang:load\"}]");

        context.close();
        return files;
    }

}
