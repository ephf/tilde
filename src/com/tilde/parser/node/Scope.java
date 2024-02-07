package com.tilde.parser.node;

import com.tilde.parser.*;
import com.tilde.tokenizer.*;

public interface Scope {

    static void parse(Parser parser, Context context) throws TokenizerException, ParserException {

        while (parser.peek() != null && !parser.has_type("}"))
            Statement.parse(parser, context);
        if(parser.peek() != null) parser.next();

    }

}
