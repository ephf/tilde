package com.tilde.parser.node;

import com.tilde.parser.*;
import com.tilde.tokenizer.*;

import java.util.ArrayList;
import java.util.Objects;

public interface Statement {

    static void parse(Parser parser, Context context) throws TokenizerException, ParserException {

        Token.Identifier identifier = null;
        if(parser.has_type(Token.Identifier.class))
            identifier = (Token.Identifier) parser.peek().orElse(null);
        Literal literal = BinaryOperation.parse(parser, context);

        if(literal instanceof Literal.Command command) {
            context.commands().add(command.command());
            return;
        }

        if(identifier == null || !(literal instanceof Literal.Unknown))
            return;

        switch(Objects.requireNonNull(identifier).data()) {
            case "if" -> {
                Context if_context = new Context(context.stack(), context.filename() + Context.next_counter());
                Type.Score predicate = Type.Score.from(BinaryOperation.parse(parser, context), context, false);
                context.commands().push("scoreboard players set else __tilde.process 1");
                context.commands().push("execute unless score " + predicate + " matches 0 run function main:"
                        + context.filename() + Context.counter());

                Token.Identifier then = (Token.Identifier) parser.expect_type(Token.Identifier.class);
                if(!then.data().equals("then")) throw new ParserException("Expected then, but got " + then);


                Statement.parse(parser, if_context);
                if_context.commands().push("scoreboard players set else __tilde.process 0");
                if_context.into(parser.output);

                if(parser.has_type(Token.Identifier.class)
                && ((Token.Identifier) Objects.requireNonNull(parser.peek().orElse(null))).data().equals("else")) {
                    parser.next();
                    Context else_context = new Context(context.stack(), context.filename() + Context.next_counter());
                    context.commands().push("execute if score else __tilde.process matches 1 run function main:"
                            + context.filename() + Context.counter());

                    Statement.parse(parser, else_context);
                    else_context.into(parser.output);
                }

                return;
            }

            case "return" -> {
                new Type.Compound("storage", "__tilde:stack", "stack[-2].__return")
                        .set_value(BinaryOperation.parse(parser, context), context);
                return;
            }
        }

        boolean static_modifier = false;
        if(identifier.data().equals("static")) {
            static_modifier = true;
            identifier = (Token.Identifier) parser.expect_type(Token.Identifier.class);
        }

        Token.Misc declaration = (Token.Misc) parser.expect_type(Token.Misc.class);
        switch(declaration.data()) {
            case "::" -> {
                ArrayList<Type.Compound> args = new ArrayList<>();
                Context function_context = new Context(context.stack(), identifier.data());
                while(parser.has_type(Token.Identifier.class)) {
                    String arg_name = ((Token.Identifier) parser.next()).data();
                    args.add(new Type.Compound("storage", "__tilde:stack", "in_args[-1]." + arg_name));
                    function_context.stack().insert(arg_name, new Type.Compound("storage", "__tilde:stack", "stack[-1]." + arg_name));
                }

                context.stack().insert(identifier.data(), new Type.Function(identifier.data(), args));
                Token.Misc function_type = (Token.Misc) parser.expect_type(Token.Misc.class);
                switch(function_type.data()) {
                    case "->" -> new Type.Compound("storage", "__tilde:stack", "stack[-2].__return")
                            .set_value(BinaryOperation.parse(parser, function_context), function_context);
                    case "=>" -> Statement.parse(parser, function_context);
                    case "{" -> Scope.parse(parser, function_context);
                    default -> throw new ParserException("Expected '{' after function declaration");
                }
                function_context.into(parser.output);
                return;
            }

            case "=" -> {
                Literal value = BinaryOperation.parse(parser, context);
                Type type;

                if(value instanceof Literal.Constant.Integer
                || (value instanceof Literal.Value val && val.type() instanceof Type.Score)) {
                    type = new Type.Score(
                            new Type.Scoreboard("__tilde." + (static_modifier
                                    ? "static" : context.filename())),
                            new Type.Selector.Player(identifier.data())
                    );
                    type.set_value(value, context);
                } else if(value instanceof Literal.Constant.Compound
                || (value instanceof Literal.Value val && val.type() instanceof Type.Compound)) {
                    type = new Type.Compound("storage", "__tilde:stack",
                            (static_modifier ? "static." : "stack[-1].") + identifier.data());
                    type.set_value(value, context);
                } else throw new ParserException("Expected variable value, but got " + value);

                context.stack().insert(identifier.data(), type);
                return;
            }
        }

        if(literal instanceof Literal.Unknown)
            throw new ParserException("Unable to find identifier " + identifier);

    }

}
