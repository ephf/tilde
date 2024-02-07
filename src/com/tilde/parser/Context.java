package com.tilde.parser;

import com.tilde.parser.data.Type;

import java.util.ArrayList;
import java.util.HashMap;

public record Context(ArrayList<String> output_commands, String module_name, String file_name,
                      ArrayList<HashMap<String, Type>> stack, Context parent, HashMap<String, ArrayList<String>> files) {

    private static int counter = 0;

    public static int counter() {
        return counter++;
    }

    public Context(String module_name, HashMap<String, ArrayList<String>> files) {
        this(new ArrayList<>(), module_name, "load", new ArrayList<>(), null, files);
        stack().add(new HashMap<>());
        command("scoreboard objectives add " + module_name() + "." + file_name() + " dummy");
    }

    public Context command(String command) {
        output_commands().add(command);
        return this;
    }

    public Context open(String file_name) {
        Context context = new Context(new ArrayList<>(), module_name(), file_name, stack(), null, files());
        context.stack().add(new HashMap<>());
        context.command("scoreboard objectives add " + module_name() + "." + file_name() + " dummy");
        return context;
    }

    public Context open_block() {
        if(parent() != null) return parent().open_block();
        Context context = new Context(new ArrayList<>(), module_name(), "block/" + file_name + counter(),
                stack(), this, files());
        context.stack().add(new HashMap<>());
        return context;
    }

    public void close() {
        files().put(file_name(), output_commands());
        stack().remove(stack().size() - 1);
    }

    public Type find_stack(String key) {
        for(int i = stack().size() - 1; i >= 0; i--) {
            Type type = stack().get(i).get(key);
            if(type != null) return type;
        }
        return null;
    }

    public void insert_stack(String key, Type type) {
        stack().get(stack().size() - 1).put(key, type);
    }

}
