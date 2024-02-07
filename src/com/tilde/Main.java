package com.tilde;

import com.tilde.parser.*;
import com.tilde.tokenizer.TokenizerException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static String directory;
    static String module_name;

    public static void main(String[] args) throws IOException, TokenizerException, ParserException {

        String[] paths = args[0].split("\\\\");

        module_name = paths[paths.length - 1].replace(".til", "");
        directory = args[1].endsWith("\\") ? args[1] : args[1] + "\\";

        String data = Files.readString(Path.of(args[0]));
        Parser parser = new Parser(data);

        mkdirs("data/" + module_name + "/functions/");
        mkdirs("data/minecraft/tags/functions/");
        write_file(
                "pack.mcmeta",
                """
                {
                \t"pack": {
                \t\t"pack_format": 20,
                \t\t"description": "Made with tilde"
                \t}
                }
                """ // I don't actually know the current pack_format
                );
        write_file("data/minecraft/tags/functions/load.json",
                """
                {
                \t"values": [\"""" + module_name + ":load\"]\n" +
                "}");

        for(Map.Entry<String, ArrayList<String>> entry : parser.parse(module_name).entrySet()) {
            write_file("data/" + module_name + "/functions/" + entry.getKey() + ".mcfunction",
                    entry.getValue().stream().reduce((prev, next) -> prev + "\n" + next)
                            .orElse(null));
        }

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void mkdirs(String path) {
        new File(directory + path)
                .mkdirs();
    }

    static void write_file(String filename, String data) throws IOException {
        Files.writeString(Path.of(directory + filename), data);
    }
}
