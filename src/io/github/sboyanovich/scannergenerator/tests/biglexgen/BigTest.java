package io.github.sboyanovich.scannergenerator.tests.biglexgen;

import io.github.sboyanovich.scannergenerator.scanner.Message;
import io.github.sboyanovich.scannergenerator.scanner.Position;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.*;

public class BigTest {
    public static void main(String[] args) {
        String text = Utility.getText("testInputM1.txt");

        LexGenScanner scanner = new LexGenScanner(text);
        MockCompiler compiler = scanner.getCompiler();

        Set<Domain> ignoredTokenTypes = Set.of(
                Domain.END_OF_PROGRAM,
                Domain.ERROR
        );

        List<Token> allTokens = new ArrayList<>();

        int errCount = 0;

        Token t = scanner.nextToken();
        allTokens.add(t);
        while (t.getTag() != Domain.END_OF_PROGRAM) {
            if (!ignoredTokenTypes.contains(t.getTag())) {
                System.out.println(t);
            }
            if (t.getTag() == Domain.ERROR) {
                errCount++;
                System.out.println(t.getCoords());
            }
            t = scanner.nextToken();
            allTokens.add(t);
        }

        System.out.println();
        System.out.println("Errors: " + errCount);
        System.out.println("Compiler messages: ");
        SortedMap<Position, Message> messages = compiler.getSortedMessages();
        for (Map.Entry<Position, Message> entry : messages.entrySet()) {
            System.out.println(entry.getValue() + " at " + entry.getKey());
        }

        if(errCount == 0) {
            AST ast = Parser.parse(allTokens.iterator());
            String dotAST = ast.toGraphVizDotString();
            System.out.println();
            System.out.println(dotAST);
        }

    }
}