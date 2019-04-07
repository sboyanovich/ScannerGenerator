package io.github.sboyanovich.scannergenerator.tests.automata;

import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.lex.StateTag;
import io.github.sboyanovich.scannergenerator.token.Domain;

import java.util.Map;

public class NFATest1 {

    enum Keyword implements StateTag {
        ELIF {
            @Override
            public Domain getDomain() {
                return null;
            }
        }
    }

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
                .removeLambdaSteps()
                .relabelStates(
                        Map.of(
                                0, StateTag.NOT_FINAL,
                                1, StateTag.NOT_FINAL,
                                2, StateTag.NOT_FINAL,
                                3, StateTag.NOT_FINAL,
                                4, Keyword.ELIF
                        )
                )
                .iteration()
                .removeLambdaSteps();

                // removeLambdaSteps() doesn't work correctly (messes up iteration)
                // UPDATE: Fixed

                // might want to write constructor validation for NFA sooner than expected
        String dot = elif.toGraphvizDotString(interpretation::get, true);
        System.out.println(dot);
    }
}
