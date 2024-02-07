package com.tilde.parser.node;

import com.tilde.parser.*;
import com.tilde.parser.data.Type;
import com.tilde.tokenizer.*;

import java.util.ArrayList;

public interface Statement {

    static void parse(Parser parser, Context context) throws TokenizerException, ParserException {

        if(parser.has_type(Token.Identifier.class) && context.find_stack(parser.peek().data()) == null) {
            Token.Identifier identifier = parser.expect_type(Token.Identifier.class);

            if(parser.has_type("::")) {
                parser.next();

                Type.Constant.Function function = new Type.Constant.Function(identifier.data(), context);
                context.insert_stack(identifier.data(), function);
                Context function_context = context.open(identifier.data());
                while(parser.has_type(Token.Identifier.class)) {
                    Token argument_token = parser.next();
                    Type argument_type = new Type.Data.Storage(argument_token.data(), function_context);
                    function.arguments().add(argument_type);
                    function_context.insert_stack(argument_token.data(), argument_type);
                }

                switch(parser.next().data()) {
                    case "->" -> return_statement(
                            BinaryOperation.parse(parser, function_context), function_context);
                    case "{" -> Scope.parse(parser, function_context);
                    default -> throw new ParserException("Expected a '{' after argument list");
                }

                function_context.close();

                return;
            }

            if(parser.has_type("=")) {
                parser.next();
                Type right_type = BinaryOperation.parse(parser, context);

                if(right_type instanceof Type.Constant.Integer || right_type instanceof Type.Score) {
                    Type left_type = new Type.Score(identifier.data(), context);
                    left_type.set_value(right_type, context);
                    context.insert_stack(identifier.data(), left_type);
                    return;
                }

                if(right_type instanceof Type.Constant.Object || right_type instanceof Type.Data) {
                    Type left_type = new Type.Data.Storage(identifier.data(), context);
                    left_type.set_value(right_type, context);
                    context.insert_stack(identifier.data(), left_type);
                    return;
                }

                if(right_type instanceof Type.Constant.Function) {
                    context.insert_stack(identifier.data(), right_type);
                    return;
                }

                throw new ParserException("Unable to put " + right_type + " into a variable");
            }

            throw new ParserException("Unable to find identifier '" + identifier.data() + "'");
        }

        BinaryOperation.parse(parser, context);

    }

    private static void return_statement(Type return_type, Context context) throws ParserException {
        if(return_type instanceof Type.Constant.Integer integer) {
            context.command("return " + integer.value());
            return;
        }

        if(return_type instanceof Type.Score) {
            context.command("return run scoreboard players get " + return_type);
            return;
        }

        if(return_type instanceof Type.Data) {
            context.command("return run data get " + return_type);
            return;
        }

        throw new ParserException("You can only return type integer, not " + return_type);
    }

}
