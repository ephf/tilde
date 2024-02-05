package com.tilde.parser.node;

import com.tilde.parser.*;
import com.tilde.tokenizer.*;

public interface BinaryOperation {

    static Literal parse(Parser parser, Context context) throws TokenizerException, ParserException {
        return expression(parser, context);
    }

    private static Literal expression(Parser parser, Context context) throws TokenizerException, ParserException {
        Literal literal = term(parser, context);
        while(parser.has_type("+") || parser.has_type("-")) {
            Token.Misc operator = (Token.Misc) parser.next();
            Literal right_literal = term(parser, context);

            if(literal instanceof Literal.Constant.Integer left
            && right_literal instanceof Literal.Constant.Integer right) {
                literal = new Literal.Constant.Integer(switch (operator.data()) {
                    case "+" -> left.value() + right.value();
                    case "-" -> left.value() - right.value();
                    default -> 0;
                });
                continue;
            }

            Type left = Type.Score.from(literal, context, true);

            if(right_literal instanceof Literal.Constant.Integer right) {
                context.commands().push("scoreboard players " + (switch(operator.data()) {
                    case "+" -> "add ";
                    case "-" -> "remove ";
                    default -> "";
                }) + left + " " + right.value());
            } else {
                Type right = Type.Score.from(right_literal, context, false);
                context.commands().push("scoreboard players operation " + left + " " + operator.data() + "= " + right);
            }

            literal = new Literal.Value(left);
        }
        return literal;
    }

    private static Literal term(Parser parser, Context context) throws TokenizerException, ParserException {
        Literal literal = Literal.parse(parser, context);
        while(parser.has_type("*") || parser.has_type("/") || parser.has_type("%")) {
            Token.Misc operator = (Token.Misc) parser.next();
            Literal right_literal = Literal.parse(parser, context);

            if(literal instanceof Literal.Constant.Integer left
            && right_literal instanceof Literal.Constant.Integer right) {
                literal = new Literal.Constant.Integer(switch (operator.data()) {
                    case "*" -> left.value() * right.value();
                    case "/" -> left.value() / right.value();
                    case "%" -> left.value() % right.value();
                    default -> 0;
                });
                continue;
            }


            Type left = Type.Score.from(literal, context, true);
            Type right = Type.Score.from(right_literal, context, false);

            context.commands().push("scoreboard players operation " + left + " " + operator.data() + "= " + right);
            literal = new Literal.Value(left);
        }
        return literal;
    }

}
