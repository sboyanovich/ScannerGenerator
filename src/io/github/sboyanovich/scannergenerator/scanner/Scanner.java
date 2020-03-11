package io.github.sboyanovich.scannergenerator.scanner;

import io.github.sboyanovich.scannergenerator.automata.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.OptionalInt;

public class Scanner {
    private static final int NEWLINE = Utility.asCodePoint("\n");
    private static final int CARRET = Utility.asCodePoint("\r");

    private Text program;

    private Compiler compiler;

    private Position currPos;
    private LexicalRecognizer recognizer;

    Scanner(String program, Compiler compiler, LexicalRecognizer recognizer) {
        this.program = new Text(program);
        this.compiler = compiler;
        this.recognizer = recognizer;
        this.currPos = new Position();
    }

    public String getProgram() {
        return program.toString();
    }

    private void advanceCurrentPosition() {
        int index = this.currPos.getIndex();
        int codePoint = this.program.codePointAt(index);
        int nextCodePoint = this.program.codePointAt(index + 1);

        if (codePoint != Text.EOI) {
            int line = this.currPos.getLine();
            // CARRET not followed by NEWLINE will also count as line break
            if ((codePoint == NEWLINE) || ((codePoint == CARRET) && (nextCodePoint != NEWLINE))) {
                this.currPos = new Position(line + 1, 1, index + 1);
            } else {
                int pos = this.currPos.getPos();
                this.currPos = new Position(line, pos + 1, index + 1);
            }
        }
    }

    private String getTextFragment(Fragment span) {
        return Utility.getTextFragmentAsString(this.program, span);
    }

    private int getCurrentCodePoint() {
        int cp = this.currPos.getIndex();
        return this.program.codePointAt(cp);
    }

    /**
     * assumed EOI not reached yet
     */
    private boolean atPotentialTokenStart() {
        int currCodePoint = getCurrentCodePoint();
        int nextState = this.recognizer.transition(this.recognizer.getInitialState(), currCodePoint);
        return nextState != LexicalRecognizer.DEAD_END_STATE;
    }

    private boolean isFinal(int currState) {
        return StateTag.isFinal(this.recognizer.getStateTag(currState));
    }

    public Token nextToken() {
        if (getCurrentCodePoint() == Text.EOI) {
            return Domain.END_OF_INPUT.createToken(this.program, new Fragment(currPos, currPos));
        }

        int currState = this.recognizer.getInitialState();

        Position start = currPos;

        // Save last final state encountered
        OptionalInt lastFinalState = OptionalInt.empty();
        Position lastInFinal = new Position(); // will be used only if lastFinalState is present

        while (true) {
            int currCodePoint = getCurrentCodePoint();
            int nextState = this.recognizer.transition(currState, currCodePoint);

            if (isFinal(currState)) {
                lastFinalState = OptionalInt.of(currState);
                lastInFinal = this.currPos;
            }

            if (nextState != LexicalRecognizer.DEAD_END_STATE) {
                currState = nextState;
                advanceCurrentPosition();
            } else {
                // it's time to stop

                if (!isFinal(currState) && !lastFinalState.isPresent()) {
                    this.compiler.addError(this.currPos, "Unexpected symbol encountered.");

                    // recovery
                    // symbol we've stumbled upon might be the beginning of a new token

                    while ((getCurrentCodePoint() != Text.EOI) && !atPotentialTokenStart()) {
                        advanceCurrentPosition();
                    }
                    Fragment invalidFragment = new Fragment(start, this.currPos);
                    return Domain.ERROR.createToken(this.program, invalidFragment);
                } else {
                    if (lastFinalState.isPresent()) {
                        this.currPos = lastInFinal;
                        currState = lastFinalState.getAsInt();
                    }
                    // now currState is certainly final => corresponds to some meaningful Domain

                    Fragment scannedFragment = new Fragment(start, this.currPos);

                    StateTag tag = this.recognizer.getStateTag(currState);

                    if (tag instanceof DomainTag) {
                        DomainTag domainTag = (DomainTag) tag;
                        return domainTag.getDomain().createToken(this.program, scannedFragment);
                    } else {
                        // FOR NOW, THIS CHECK SHOULD PROBABLY BE PERFORMED WHEN BUILDING A RECOGNIZER/SCANNER
                        throw new RuntimeException("Only DomainTag StateTags allowed as final states in a scanner!");
                    }
                }
            }
        }
    }
}

