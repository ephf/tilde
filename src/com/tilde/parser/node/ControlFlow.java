package com.tilde.parser.node;

import com.tilde.parser.*;
import com.tilde.parser.data.Type;
import com.tilde.tokenizer.*;

public interface ControlFlow {

    static Type parse(Parser parser, Context context) throws TokenizerException, ParserException {
        Type type = BinaryOperation.parse(parser, context);
        if(!(type instanceof Type.UnknownIdentifier unknown)) return type;

        switch(unknown.token().data()) {
            case "if" -> {


            }
        }
    }

}
