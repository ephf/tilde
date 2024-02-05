package com.tilde.parser;

import com.tilde.parser.node.Literal;

import java.util.ArrayList;
import java.util.HashMap;

public interface Type {

    default void set_value(Literal literal, Context context) throws ParserException {
        throw new ParserException("Unable to modify " + this);
    }

    interface Selector extends Type {
        enum Base { a, e, r, p }

        record Query(Base base, HashMap<String, String> query) implements Selector {}
        record Player(String name) implements Selector {
            public String toString() {
                return name();
            }
        }
    }

    record Scoreboard(String name) implements Type {}
    record Score(Scoreboard parent, Selector target) implements Type {
        public String toString() {
            return target() + " " + parent().name();
        }

        public void set_value(Literal literal, Context context) throws ParserException {
            if(literal instanceof Literal.Constant constant) {
                if(constant instanceof Literal.Constant.Integer integer)
                    context.commands().push("scoreboard players set " + this + " " + integer.value());
                else if(constant instanceof Literal.Constant.Compound compound)
                    throw ParserException.type_mismatch(this, null);
                return;
            }

            if(literal instanceof Literal.Value value) {
                if(value.type() instanceof Compound compound)
                    context.commands().push("execute store result score " + this + " run data get " + compound);
                else if(value.type() instanceof Score score)
                    context.commands().push("scoreboard players operation " + this + " = " + score);
                else
                    throw ParserException.type_mismatch(this, value.type());
                return;
            }

            throw new ParserException("UNREACHABLE");
        }

        public static Score from(Literal literal, Context context, boolean mutable) throws ParserException {
            if(!mutable && (literal instanceof Literal.Value value && value.type() instanceof Type.Score))
                return (Score) value.type();

            Score score = new Score(new Scoreboard("__tilde.process"),
                    new Selector.Player(mutable ? "left" : "right"));
            score.set_value(literal, context);
            return score;
        }
    }

    record Function(String name, ArrayList<Compound> args) implements Type {}

    record Compound(String type, String parent, String path) implements Type {
        public void set_value(Literal literal, Context context) throws ParserException {
            if(literal instanceof Literal.Constant constant) {
                if(constant instanceof Literal.Constant.Integer integer)
                    context.commands().push("data modify " + this + " set value " + integer.value());
                else if(constant instanceof Literal.Constant.Compound compound)
                    context.commands().push("data modify " + this + " set value " + compound.value());
                return;
            }

            if(literal instanceof Literal.Value value) {
                if(value.type() instanceof Compound compound)
                    context.commands().push("data modify " + this + " set from " + compound);
                else if(value.type() instanceof Score score)
                    context.commands().push("execute store result " + this + " int 1 run scoreboard players get " + score);
                else
                    throw ParserException.type_mismatch(this, value.type());
                return;
            }

            throw new ParserException("UNREACHABLE");
        }

        public String toString() {
            return type() + " " + parent() + " " + path();
        }
    }

}
