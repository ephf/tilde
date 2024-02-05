package com.tilde;

import com.tilde.parser.Parser;
import com.tilde.parser.ParserException;
import com.tilde.tokenizer.TokenizerException;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException, TokenizerException, ParserException {

        String data = Files.readString(Path.of(args[0]));
        Parser parser = new Parser(data);

        String path = "C:\\Users\\Grant\\AppData\\Roaming\\.minecraft\\saves\\tilde testing\\datapacks\\tilde\\data\\main\\functions";

        parser.parse().forEach((filename, commands) -> {
            try(FileWriter writer = new FileWriter(path + "\\" + filename + ".mcfunction")) {
                for(String command : commands) writer.write(command + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }
}
