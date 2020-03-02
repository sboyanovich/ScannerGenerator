package io.github.sboyanovich.scannergenerator.scanner;

import io.github.sboyanovich.scannergenerator.scanner.token.TEndOfProgram;
import io.github.sboyanovich.scannergenerator.scanner.token.TError;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * ProScanner utilizes multiple recognizers (modes) to add additional power to scanning algorithm,
 * i.e. special modes for C-style multi-line comments or strings, or character class mode when scanning
 * flex-style regular expressions.
 */
public class ProScanner {
    private static final int NEWLINE = Utility.asCodePoint("\n");
    private static final int CARRET = Utility.asCodePoint("\r");
    private static final int KEEP_CURRENT = -1;

    private Text program;

    private ProCompiler compiler;

    private Position currPos;
    private List<LexicalRecognizer> recognizers;
    private Map<StateTag, Integer> modeSwitches;

    public int getCurrentMode() {
        return currentMode;
    }

    private int currentMode;

    ProScanner(
            String program,
            ProCompiler compiler,
            List<LexicalRecognizer> recognizers,
            Map<StateTag, Integer> modeSwitches // only non zero-ones are necessary
    ) {
        this.program = new Text(program);
        this.compiler = compiler;
        this.recognizers = recognizers;
        this.currPos = new Position();
        this.currentMode = 0;
        //for now without copying
        this.modeSwitches = modeSwitches;
    }

    public String getProgram() {
        return program.toString();
    }

    int modeSwitch(StateTag tag) {
        if (!modeSwitches.containsKey(tag)) {
            return KEEP_CURRENT;
        }
        return modeSwitches.get(tag);
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
        // assuming general use case that all token starts are recognized by default mode
        LexicalRecognizer recognizer = this.recognizers.get(0);
        int nextState = recognizer.transition(recognizer.getInitialState(), currCodePoint);
        return nextState != LexicalRecognizer.DEAD_END_STATE;
    }

    private LexicalRecognizer getCurrentRecognizer() {
        return this.recognizers.get(this.currentMode);
    }

    private boolean isFinal(int currState) {
        return StateTag.isFinal(this.recognizers.get(this.currentMode).getStateTag(currState));
    }

    public Token nextToken() {
        if (getCurrentCodePoint() == Text.EOI) {
            return new TEndOfProgram(new Fragment(currPos, currPos));
        }

        int currState = getCurrentRecognizer().getInitialState();

        Position start = currPos;

        // Save last final state encountered
        OptionalInt lastFinalState = OptionalInt.empty();
        Position lastInFinal = new Position(); // will be used only if lastFinalState is present

        while (true) {
            int currCodePoint = getCurrentCodePoint();
            int nextState = getCurrentRecognizer().transition(currState, currCodePoint);

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
                    this.currentMode = 0; // resetting to default mode after error recovery
                    Fragment invalidFragment = new Fragment(start, this.currPos);
                    return new TError(invalidFragment, getTextFragment(invalidFragment));
                } else {
                    if (lastFinalState.isPresent()) {
                        this.currPos = lastInFinal;
                        currState = lastFinalState.getAsInt();
                    }
                    // now currState is certainly final

                    lastFinalState = OptionalInt.empty(); // something matched, no reusing this!

                    StateTag tag = getCurrentRecognizer().getStateTag(currState);

                    int mode = modeSwitch(tag);

                    if (mode != KEEP_CURRENT) {
                        this.currentMode = mode;
                        // resetting to initial state is done later in the code
                    }

                    if (tag instanceof DomainTag) {
                        // we have a Token
                        DomainTag domainTag = (DomainTag) tag;
                        Fragment scannedFragment = new Fragment(start, this.currPos);
                        return domainTag.getDomain().createToken(this.program, scannedFragment);
                    } else {
                        // we must work further to complete our Token (maybe in new mode)
                        // don't forget to reset current recognizer to initial state
                        currState = getCurrentRecognizer().getInitialState();
                    }
                }
            }
        }
    }
}

