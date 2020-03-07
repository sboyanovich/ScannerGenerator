package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.scanner.Message;
import io.github.sboyanovich.scannergenerator.scanner.Position;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class BigTestGen {
    public static void main(String[] args) {
        String text = Utility.getTextFromResourceFile("testInputM1.txt");

        MyScanner scanner = new MyScanner(text);
        MockCompiler compiler = scanner.getCompiler();

        Set<Domain> ignoredTokenTypes = Set.of(
                Domain.END_OF_INPUT,
                Domain.ERROR
        );

        int errCount = 0;

        while (scanner.hasNext()) {
            Token t = scanner.next();

            if (!ignoredTokenTypes.contains(t.getTag())) {
                System.out.println(t);
            }
            if (t.getTag() == Domain.ERROR) {
                errCount++;
                System.out.println(t.getCoords());
            }
        }

        System.out.println();
        System.out.println("Errors: " + errCount);
        System.out.println("Compiler messages: ");
        SortedMap<Position, Message> messages = compiler.getSortedMessages();
        for (Map.Entry<Position, Message> entry : messages.entrySet()) {
            System.out.println(entry.getValue() + " at " + entry.getKey());
        }
    }
}
