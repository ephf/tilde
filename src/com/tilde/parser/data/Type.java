package com.tilde.parser.data;

import com.tilde.parser.Context;
import com.tilde.parser.ParserException;
import com.tilde.tokenizer.Token;

import java.util.ArrayList;

public interface Type {

    boolean mutable();

    default void set_value(Type type, Context context) throws ParserException {
        throw new ParserException("Unable to set " + this);
    }

    default Type get_value(boolean require_mutable, Context context) throws ParserException {
        throw new ParserException("Unable to cast " + this + " to a value");
    }

    record UnknownIdentifier(Token.Identifier token) implements Type {
        public boolean mutable() {
            return false;
        }
    }

    interface Constant extends Type {
        record Function(String name, ArrayList<Type> arguments) implements Constant {
            public Function(String name, Context context) {
                this(context.module_name() + ":" + name, new ArrayList<>());
            }

            public String toString() {
                return name();
            }
        }
        record Integer(int value) implements Constant {}
        record Object(String data) implements Constant {}

        default boolean mutable() {
            return false;
        }
    }

    record Scoreboard(String name) implements Type {
        public Scoreboard(String name, Context context) {
            this(context.module_name() + "." + context.file_name() + "." + name);
        }

        public Scoreboard(Context context) {
            this(context.module_name() + "." + context.file_name());
        }

        public boolean mutable() {
            return false;
        }
    }

    record Score(boolean mutable, Scoreboard parent, Selector selector) implements Type {
        public Score(String name, Context context) {
            this(false, new Scoreboard(context), new Selector.Name(name));
        }

        public String toString() {
            return selector() + " " + parent().name();
        }

        public void set_value(Type type, Context context) throws ParserException {
            if(type instanceof Constant.Integer integer) {
                context.command("scoreboard players set " + this + " " + integer.value());
                return;
            }

            if(type instanceof Score) {
                context.command("scoreboard players operation " + this + " = " + type);
                return;
            }

            if(type instanceof Data) {
                context.command("execute store result score " + this + " run data get " + type);
                return;
            }

            throw new ParserException("Unable to set " + this + " to " + type);
        }

        public Type get_value(boolean require_mutable, Context context) throws ParserException {
            if(!require_mutable || mutable()) return this;

            if()
        }

        public static Score cast(Type type, boolean require_mutable, Context context) throws ParserException {
            if(type instanceof Score score && (!require_mutable || type.mutable())) return score;
            Score cast = new Score(true,
                    new Scoreboard("__tilde.process"),
                    new Selector.Name(require_mutable ? "left" : "right"));
            cast.set_value(type, context);
            return cast;
        }
    }

    interface Data extends Type {
        record Storage(boolean mutable, String name, String query) implements Data {
            public Storage(String name, Context context) {
                this(false, context.module_name() + ":" + context.file_name(), name);
            }

            public String toString() {
                return "storage " + name() + " " + query();
            }
        }

        default void set_value(Type type, Context context) throws ParserException {
            if(type instanceof Constant.Integer integer) {
                context.command("data modify " + this + " set value " + integer.value());
                return;
            }

            if(type instanceof Constant.Object object) {
                context.command("data modify " + this + " set value " + object.data());
                return;
            }

            if(type instanceof Score) {
                context.command("execute store result " + this + " int 1 run scoreboard players get " + type);
                return;
            }

            if(type instanceof Data) {
                context.command("data modify " + this + " set from " + type);
                return;
            }

            throw new ParserException("Unable to set " + this + " to " + type);
        }

        static Data cast(Type type, boolean require_mutable, Context context) throws ParserException {
            if(type instanceof Data data && (!require_mutable || type.mutable())) return data;
            Data cast = new Storage(true, "__tilde:process", require_mutable ? "left" : "right");
            cast.set_value(type, context);
            return cast;
        }
    }

}
