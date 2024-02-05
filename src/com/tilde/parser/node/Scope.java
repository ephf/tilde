package com.tilde.parser.node;

import com.tilde.parser.*;
import com.tilde.tokenizer.TokenizerException;

public interface Scope {

    static void parse(Parser parser, Context context) throws TokenizerException, ParserException {
        while(parser.peek().isPresent() && !parser.has_type("}"))
            Statement.parse(parser, context);
        if(parser.has_type("}")) parser.next();
        context.stack().remove(context.stack().size() - 1);
    }

}
