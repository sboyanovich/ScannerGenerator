package io.github.sboyanovich.scannergenerator.lex;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.Position;
import io.github.sboyanovich.scannergenerator.token.TEndOfProgram;
import io.github.sboyanovich.scannergenerator.token.TError;
import io.github.sboyanovich.scannergenerator.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.OptionalInt;

public class Scanner {
    private static final int NEWLINE = Utility.asCodePoint("\n");

    private Text program;

    private Compiler compiler;

    private Position currPos;
    private LexicalRecognizer dfa;

    Scanner(String program, Compiler compiler, LexicalRecognizer dfa) {
        this.program = new Text(program);
        this.compiler = compiler;
        this.dfa = dfa;
        this.currPos = new Position();
    }

    public String getProgram() {
        return program.toString();
    }

    // TODO: Might add \r as line break support
    private void advanceCurrentPosition() {
        int index = this.currPos.getIndex();
        int codePoint = this.program.codePointAt(index);

        if (codePoint != Text.EOI) {
            int line = this.currPos.getLine();
            if (codePoint == NEWLINE) {
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
        int nextState = this.dfa.transition(0, currCodePoint);
        return nextState != LexicalRecognizer.DEAD_END_STATE;
    }

    private boolean isFinal(int currState) {
        return StateTag.isFinal(this.dfa.getStateTag(currState));
    }

    public Token nextToken() {
        if (getCurrentCodePoint() == Text.EOI) {
            return new TEndOfProgram(new Fragment(currPos, currPos));
        }
        // state 0 initial by default
        int currState = 0;

        Position start = currPos;

        // Save last final state encountered
        OptionalInt lastFinalState = OptionalInt.empty();
        Position lastInFinal = new Position(); // will be used only if lastFinalState is present

        while (true) {
            int currCodePoint = getCurrentCodePoint();
            int nextState = this.dfa.transition(currState, currCodePoint);

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

                    StateTag tag = this.dfa.getStateTag(currState);

                    return tag.getDomain().createToken(this.program, scannedFragment);
                }
            }
        }
    }
}

