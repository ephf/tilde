package com.tilde.parser;

import java.util.ArrayList;
import java.util.HashMap;

public record Context(CommandCollection commands, Stack stack, String filename) {

    private static int counter = 0;

    public static class CommandCollection extends ArrayList<String> {
        private final ArrayList<String> prefix = new ArrayList<>();

        public void push_prefix(String prefix) {
            this.prefix.add(prefix);
        }

        public void pop_prefix() {
            prefix.remove(prefix.size() - 1);
        }

        public void push(String command) {
            StringBuilder prefix = new StringBuilder();
            for(String pref : this.prefix) prefix.append(pref).append(" ");
            if(prefix.toString().equals("")) add(command);
            else add("execute " + prefix + "run " + command);
        }
    }

    public static class Scope extends HashMap<String, Type> {}

    public static class Stack extends ArrayList<HashMap<String, Type>> {

        public Type find(String key) {
            for(int i = size() - 1; i >= 0; i--) {
                Type type = get(i).get(key);
                if(type != null) return type;
            }
            return null;
        }

        public void insert(String key, Type type) {
            get(size() - 1).put(key, type);
        }

    }

    public Context(Stack parent, String filename) {
        this(new CommandCollection(), (Stack) parent.clone(), filename);
        commands().push("scoreboard objectives add __tilde." + filename() + " dummy");
    }

    public Context(String filename) {
        this(new Stack(), filename);
    }

    public void into(HashMap<String, ArrayList<String>> output) {
        output.put(filename(), commands());
    }

    public static int next_counter() {
        return counter++;
    }

    public static int counter() {
        return counter - 1;
    }

}
