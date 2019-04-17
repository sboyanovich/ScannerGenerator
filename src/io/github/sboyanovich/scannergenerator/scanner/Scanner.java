package io.github.sboyanovich.scannergenerator.scanner;

import io.github.sboyanovich.scannergenerator.scanner.token.TEndOfProgram;
import io.github.sboyanovich.scannergenerator.scanner.token.TError;
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
            return new TEndOfProgram(new Fragment(currPos, currPos));
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
                    return new TError(invalidFragment, getTextFragment(invalidFragment));
                } else {
                    if (lastFinalState.isPresent()) {
                        this.currPos = lastInFinal;
                        currState = lastFinalState.getAsInt();
                    }
                    // now currState is certainly final => corresponds to some meaningful Domain

                    Fragment scannedFragment = new Fragment(start, this.currPos);

                    StateTag tag = this.recognizer.getStateTag(currState);

                    return tag.getDomain().createToken(this.program, scannedFragment);
                }
            }
        }
    }
}

