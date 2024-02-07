package com.tilde.parser.node;

import com.tilde.parser.*;
import com.tilde.parser.data.Type;
import com.tilde.tokenizer.*;

public interface BinaryOperation {

    static Type parse(Parser parser, Context context) throws TokenizerException, ParserException {
        return declaration(parser, context);
    }

    private static Type expression(Parser parser, Context context) throws TokenizerException, ParserException {

        Type left_type = Literal.parse(parser, context);

        while(parser.has_type("+") || parser.has_type("-")) {
            Token.Misc operator = parser.expect_type(Token.Misc.class);
            Type right_type = Literal.parse(parser, context);

            if(left_type instanceof Type.Constant.Integer left && right_type instanceof Type.Constant.Integer right) {
                left_type = new Type.Constant.Integer(switch(operator.data()) {
                    case "+" -> left.value() + right.value();
                    case "-" -> left.value() - right.value();
                    default -> 0;
                });
                continue;
            }

            Type.Score left_score = Type.Score.cast(left_type, true, context);

            if(right_type instanceof Type.Constant.Integer right) {
                context.command("scoreboard players " + (operator.data().equals("+") ? "add" : "remove") +
                        " " + left_score + " " + right.value());
                left_type = left_score;
                continue;
            }

            context.command("scoreboard players operation " + left_score + " " + operator.data() + "= " +
                    Type.Score.cast(right_type, false, context));
            left_type = left_score;
        }

        return left_type;

    }

    private static Type declaration(Parser parser, Context context) throws TokenizerException, ParserException {

        Type left_type = expression(parser, context);

        if(parser.has_type("=")) {
            parser.next();
            left_type.set_value(expression(parser, context), context);
        }

        return left_type;

    }

}
