package com.tilde.parser.node;

import com.tilde.parser.*;
import com.tilde.tokenizer.*;

public interface Literal {

    static Literal parse(Parser parser, Context context) throws TokenizerException, ParserException {
        Token token = parser.next();

        if(token instanceof Token.Identifier identifier) {
            Type type = context.stack().find(identifier.data());
            if(type == null)
                return new Unknown(identifier);

            if(type instanceof Type.Function function) {
                if(function.args().size() > 0) {
                    context.commands().push("data modify storage __tilde:stack in_args append value {}");
                    for (Type.Compound arg : function.args())
                        arg.set_value(BinaryOperation.parse(parser, context), context);
                    context.commands().push("data modify storage __tilde:stack stack append from storage "
                            + "__tilde:stack in_args[-1]");
                    context.commands().push("data remove storage __tilde:stack in_args[-1]");
                } else {
                    context.commands().push("data modify storage __tilde:stack stack append value {}");
                }
                context.commands().push("function main:" + function.name());
                context.commands().push("data remove storage __tilde:stack stack[-1]");
                return new Literal.Value(new Type.Compound("storage", "__tilde:stack", "stack[-1].__return"));
            }

            return new Value(type);
        }

        if(token instanceof Token.Integer integer)
            return new Literal.Constant.Integer(integer.value());

        if(token instanceof Token.StringValue string)
            return new Literal.Constant.Compound("\"" + string.data() + "\"");

        if(token instanceof Token.Misc misc) {
            if(misc.data().equals("(")) {
                Literal literal = BinaryOperation.parse(parser, context);
                parser.expect_type(")");
                return literal;
            }
        }

        if(token instanceof Token.Command command)
            return new Literal.Command(command.data());

        throw new ParserException("Unexpected token '" + token + "'");
    }

    interface Constant extends Literal {
        record Integer(int value) implements Constant {}
        record Compound(String value) implements Constant {}
    }

    record Value(Type type) implements Literal {}

    record Unknown(Token.Identifier identifier) implements Literal {}

    record Command(String command) implements Literal {}

}
