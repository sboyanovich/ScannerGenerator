package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.automata.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.LexicalRecognizer;
import io.github.sboyanovich.scannergenerator.scanner.Position;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.*;

import static io.github.sboyanovich.scannergenerator.generated.GeneratedScanner.Mode.*;
import static io.github.sboyanovich.scannergenerator.generated.StateTags.*;

public abstract class GeneratedScanner implements Iterator<Token> {
    protected enum Mode {
        INITIAL,
        REGEX,
        CHAR_CLASS,
        COMMENT,
        SL_COMMENT
    }

    private static final int NEWLINE = Utility.asCodePoint("\n");
    private static final int CARRET = Utility.asCodePoint("\r");

    private static Map<Mode, LexicalRecognizer> recognizers;

    static {
        // Building tag list for correct restoring of recognizers from files.
        List<StateTag> finalTags = new ArrayList<>();
        finalTags.add(SLC_REG);
        finalTags.add(SLC_CLOSE);
        finalTags.add(SLC_START);
        finalTags.add(ASTERISK);
        finalTags.add(COMMENT_CLOSE);
        finalTags.add(NO_ASTERISK_SEQ);
        finalTags.add(COMMENT_START);
        finalTags.add(CLASS_CHAR);
        finalTags.add(CHAR);
        finalTags.add(CHAR_CLASS_CLOSE);
        finalTags.add(CHAR_CLASS_OPEN);
        finalTags.add(CHAR_CLASS_NEG);
        finalTags.add(CHAR_CLASS_RANGE_OP);
        finalTags.add(EOF);
        finalTags.add(DOT);
        finalTags.add(ITERATION_OP);
        finalTags.add(POS_ITERATION_OP);
        finalTags.add(UNION_OP);
        finalTags.add(OPTION_OP);
        finalTags.add(REPETITION_OP);
        finalTags.add(CLASS_MINUS_OP);
        finalTags.add(RPAREN);
        finalTags.add(LPAREN);
        finalTags.add(NAMED_EXPR);
        finalTags.add(IDENTIFIER);
        finalTags.add(DEFINER);
        finalTags.add(MODES_SECTION_MARKER);
        finalTags.add(DOMAINS_GROUP_MARKER);
        finalTags.add(RULES_SECTION_MARKER);
        finalTags.add(R_ANGLE_BRACKET);
        finalTags.add(L_ANGLE_BRACKET);
        finalTags.add(COMMA);
        finalTags.add(RULE_END);
        finalTags.add(ACTION_SWITCH_RETURN);
        finalTags.add(ACTION_RETURN);
        finalTags.add(ACTION_SWITCH);
        finalTags.add(WHITESPACE);
        finalTags.add(WHITESPACE_IN_REGEX);

        // Restoring recognizers from files.
        recognizers = new HashMap<>();
        recognizers.put(REGEX, new LexicalRecognizer(
                ClassLoader.getSystemClassLoader()
                        .getResourceAsStream("generated/recognizers/REGEX.reco"), finalTags));
        recognizers.put(INITIAL, new LexicalRecognizer(
                ClassLoader.getSystemClassLoader()
                        .getResourceAsStream("generated/recognizers/INITIAL.reco"), finalTags));
        recognizers.put(SL_COMMENT, new LexicalRecognizer(
                ClassLoader.getSystemClassLoader()
                        .getResourceAsStream("generated/recognizers/SL_COMMENT.reco"), finalTags));
        recognizers.put(CHAR_CLASS, new LexicalRecognizer(
                ClassLoader.getSystemClassLoader()
                        .getResourceAsStream("generated/recognizers/CHAR_CLASS.reco"), finalTags));
        recognizers.put(COMMENT, new LexicalRecognizer(
                ClassLoader.getSystemClassLoader()
                        .getResourceAsStream("generated/recognizers/COMMENT.reco"), finalTags));
    }

    private Position currPos;
    private Position start;
    private Text inputText;
    private Mode currentMode;
    private int currState;
    private boolean hasNext;

    public GeneratedScanner(String inputText) {
        // General purpose initialization.
        this.inputText = new Text(inputText);
        this.currentMode = INITIAL;
        this.currPos = new Position();
        this.start = this.currPos;
        this.hasNext = true;

        // just in case
        resetCurrState();
    }

    public String getInputText() {
        return inputText.toString();
    }

    protected Position getStartPosition() {
        return this.start;
    }

    protected Position getCurrentPosition() {
        return this.currPos;
    }

    protected void resetCurrState() {
        this.currState = getCurrentRecognizer().getInitialState();
    }

    protected void switchToMode(Mode mode) {
        this.currentMode = mode;
        resetCurrState();
    }

    protected void setStartToCurrentPosition() {
        this.start = this.currPos;
    }

    protected void setCurrentPositionToStart() {
        this.currPos = this.start;
    }

    protected void advanceCurrentPosition() {
        int index = this.currPos.getIndex();
        int codePoint = this.inputText.codePointAt(index);
        int nextCodePoint = this.inputText.codePointAt(index + 1);

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
        return Utility.getTextFragmentAsString(this.inputText, span);
    }

    protected int getCurrentCodePoint() {
        int cp = this.currPos.getIndex();
        return this.inputText.codePointAt(cp);
    }

    protected boolean atPotentialPatternStart() {
        int currCodePoint = getCurrentCodePoint();
        // assuming general use case that all token starts are recognized by default mode
        LexicalRecognizer recognizer = recognizers.get(INITIAL);
        int nextState = recognizer.transition(recognizer.getInitialState(), currCodePoint);
        return nextState != LexicalRecognizer.DEAD_END_STATE;
    }

    private LexicalRecognizer getCurrentRecognizer() {
        return recognizers.get(this.currentMode);
    }

    private boolean isFinal(int currState) {
        return StateTag.isFinal(recognizers.get(this.currentMode).getStateTag(currState));
    }

    @Override
    final public boolean hasNext() {
        return this.hasNext;
    }

    @Override
    final public Token next() {
        return nextToken();
    }

    private Token nextToken() {
        resetCurrState();
        setStartToCurrentPosition();

        // Save last final state encountered
        OptionalInt lastFinalState = OptionalInt.empty();
        Position lastInFinal = new Position(); // will be used only if lastFinalState is present

        while (true) {
            int currCodePoint = getCurrentCodePoint();
            int nextState = getCurrentRecognizer().transition(this.currState, currCodePoint);

            if (isFinal(this.currState)) {
                lastFinalState = OptionalInt.of(this.currState);
                lastInFinal = this.currPos;
            }

            if (nextState != LexicalRecognizer.DEAD_END_STATE) {
                this.currState = nextState;
                advanceCurrentPosition();
            } else {
                // it's time to stop

                // nothing matched
                if (!isFinal(this.currState) && !lastFinalState.isPresent()) {
                    /// This guards against finding EOI while completing an earlier started token
                    if (
                            (currCodePoint == Text.EOI || currCodePoint == this.inputText.getAltEoi()) &&
                                    this.currPos.equals(this.start)
                    ) {
                        this.hasNext = false;
                        return Domain.END_OF_INPUT.createToken(this.inputText, new Fragment(currPos, currPos));
                    }

                    // we've found an error

                    /// ERROR HANDLING CODE GOES HERE!
                    Optional<Token> optToken = handleError(this.inputText, this.currentMode, this.start, this.currPos);

                    if (optToken.isPresent()) {
                        return optToken.get();
                    } else {
                        resetCurrState();
                    }

                } else {
                    if (lastFinalState.isPresent()) {
                        this.currPos = lastInFinal;
                        this.currState = lastFinalState.getAsInt();
                    }
                    // now currState is certainly final

                    lastFinalState = OptionalInt.empty(); // something matched, no reusing this!
                    Fragment scannedFragment = new Fragment(this.start, this.currPos);
                    Optional<Token> optToken = Optional.empty();

                    // this cast should always work, provided all final ones are in one enum
                    // alternative: switch vs instanceof
                    StateTags tag = (StateTags) getCurrentRecognizer().getStateTag(this.currState);

                    /// TIP: for ignored expressions (e.g. whitespace) case should just reset start
                    switch (tag) {
                        case WHITESPACE_IN_REGEX:
                            optToken = handleWhitespaceInRegex(this.inputText, scannedFragment);
                            break;
                        case WHITESPACE:
                            setStartToCurrentPosition();
                            break;
                        case ACTION_SWITCH:
                            optToken = Optional.of(
                                    DomainsWithStringAttribute
                                            .ACTION_SWITCH.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case ACTION_RETURN:
                            optToken = Optional.of(
                                    DomainsWithStringAttribute
                                            .ACTION_RETURN.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case ACTION_SWITCH_RETURN:
                            optToken = Optional.of(
                                    DomainsWithStringPairAttribute
                                            .ACTION_SWITCH_RETURN.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case RULE_END:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .RULE_END.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case COMMA:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .COMMA.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case L_ANGLE_BRACKET:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .L_ANGLE_BRACKET.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case R_ANGLE_BRACKET:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .R_ANGLE_BRACKET.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case RULES_SECTION_MARKER:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .RULES_SECTION_MARKER.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case DOMAINS_GROUP_MARKER:
                            optToken = Optional.of(
                                    DomainsWithStringAttribute
                                            .DOMAINS_GROUP_MARKER.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case MODES_SECTION_MARKER:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .MODES_SECTION_MARKER.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case DEFINER:
                            switchToMode(REGEX);
                            optToken = Optional.of(
                                    SimpleDomains
                                            .DEFINER.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case IDENTIFIER:
                            optToken = Optional.of(
                                    DomainsWithStringAttribute
                                            .IDENTIFIER.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case NAMED_EXPR:
                            optToken = Optional.of(
                                    DomainsWithStringAttribute
                                            .NAMED_EXPR.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case LPAREN:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .LPAREN.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case RPAREN:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .RPAREN.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case CLASS_MINUS_OP:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .CLASS_MINUS_OP.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case REPETITION_OP:
                            optToken = Optional.of(
                                    DomainsWithIntPairAttribute
                                            .REPETITION_OP.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case OPTION_OP:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .OPTION_OP.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case UNION_OP:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .UNION_OP.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case POS_ITERATION_OP:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .POS_ITERATION_OP.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case ITERATION_OP:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .ITERATION_OP.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case DOT:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .DOT.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case EOF:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .EOF.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case CHAR_CLASS_RANGE_OP:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .CHAR_CLASS_RANGE_OP.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case CHAR_CLASS_NEG:
                            optToken = Optional.of(
                                    SimpleDomains
                                            .CHAR_CLASS_NEG.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case CHAR_CLASS_OPEN:
                            switchToMode(CHAR_CLASS);
                            optToken = Optional.of(
                                    SimpleDomains
                                            .CHAR_CLASS_OPEN.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case CHAR_CLASS_CLOSE:
                            switchToMode(REGEX);
                            optToken = Optional.of(
                                    SimpleDomains
                                            .CHAR_CLASS_CLOSE.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case CHAR:
                            optToken = Optional.of(
                                    DomainsWithIntegerAttribute
                                            .CHAR.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case CLASS_CHAR:
                            optToken = Optional.of(
                                    DomainsWithIntegerAttribute
                                            .CHAR.createToken(this.inputText, scannedFragment)
                            );
                            break;
                        case COMMENT_START:
                            switchToMode(COMMENT);
                            break;
                        case NO_ASTERISK_SEQ:
                            optToken = handleNoAsteriskSeq(this.inputText, scannedFragment);
                            break;
                        case COMMENT_CLOSE:
                            optToken = handleCommentClose(this.inputText, scannedFragment);
                            break;
                        case ASTERISK:
                            optToken = handleCommentAsterisk(this.inputText, scannedFragment);
                            break;
                        case SLC_START:
                            switchToMode(SL_COMMENT);
                            break;
                        case SLC_CLOSE:
                            optToken = handleSlcClose(this.inputText, scannedFragment);
                            break;
                        case SLC_REG:
                            optToken = handleSlcReg(this.inputText, scannedFragment);
                            break;
                    }

                    if (optToken.isPresent()) {
                        return optToken.get();
                    } else {
                        resetCurrState();
                    }
                }
            }
        }
    }

    /// Default implementation, to ensure scanner doesn't get stuck
    protected Optional<Token> handleError(Text text, Mode mode, Position start, Position follow) {
        // recovery
        // discard symbols from input until we find a potential pattern start
        setCurrentPositionToStart();
        advanceCurrentPosition();

        while ((getCurrentCodePoint() != Text.EOI) && !atPotentialPatternStart()) {
            advanceCurrentPosition();
        }
        switchToMode(INITIAL); // resetting to default mode after error recovery
        Fragment invalidFragment = new Fragment(this.start, this.currPos);

        /// HINT: If you wish to do the same, but not return any token, remember to call setStartToCurrentPosition()

        return Optional.of(Domain.ERROR.createToken(this.inputText, invalidFragment));
    }

    protected abstract Optional<Token> handleWhitespaceInRegex(Text text, Fragment fragment);

    protected abstract Optional<Token> handleNoAsteriskSeq(Text text, Fragment fragment);

    protected abstract Optional<Token> handleCommentClose(Text text, Fragment fragment);

    protected abstract Optional<Token> handleCommentAsterisk(Text text, Fragment fragment);

    protected abstract Optional<Token> handleSlcClose(Text text, Fragment fragment);

    protected abstract Optional<Token> handleSlcReg(Text text, Fragment fragment);

}