package io.github.sboyanovich.scannergenerator.tests.automata;

import io.github.sboyanovich.scannergenerator.automata.NFA;

import java.util.Map;

public class NFATest1 {
    public static void main(String[] args) {
        Map<Integer, String> interpretation = Map.of(
                0, "e",
                1, "l",
                2, "i",
                3, "f"
        );

        int alphabetSize = interpretation.size();
        NFA elif = NFA.singleLetterLanguage(4, 0)
                .concatenation(NFA.singleLetterLanguage(4, 1))
                .concatenation(NFA.singleLetterLanguage(4, 2))
                .concatenation(NFA.singleLetterLanguage(4, 3))
                .removeLambdaSteps();
        String dot = elif.toGraphvizDotString(interpretation::get);
        System.out.println(dot);
    }
}
