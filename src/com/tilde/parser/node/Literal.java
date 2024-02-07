package com.tilde.parser.node;

import com.tilde.parser.*;
import com.tilde.parser.data.Selector;
import com.tilde.parser.data.Type;
import com.tilde.tokenizer.*;

public interface Literal {

    static Type parse(Parser parser, Context context) throws TokenizerException, ParserException {

        Token token = parser.next();

        if(token instanceof Token.Integer integer)
            return new Type.Constant.Integer(integer.value());

        if(token instanceof Token.Identifier identifier) {
            Type type = context.find_stack(identifier.data());
            if(type == null) return new Type.UnknownIdentifier(identifier);

            if(type instanceof Type.Constant.Function function) {
                for (Type argument : function.arguments())
                    argument.set_value(BinaryOperation.parse(parser, context), context);
                context.command("execute store result score return __tilde.process run function " + function.name());
                return new Type.Score(false,
                        new Type.Scoreboard("__tilde.process"),
                        new Selector.Name("return"));
            }

            return type;
        }

        throw new ParserException("Unexpected token '" + token.data() + "', expected literal");

    }

}
