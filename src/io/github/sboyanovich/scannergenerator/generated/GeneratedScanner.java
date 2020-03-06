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
    enum Mode {
        INITIAL,
        REGEX,
        CHAR_CLASS,
        COMMENT
    }

    private static final int NEWLINE = Utility.asCodePoint("\n");
    private static final int CARRET = Utility.asCodePoint("\r");

    private Map<Mode, LexicalRecognizer> recognizers;
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

        // Building tag list for correct restoring of recognizers from files.
        List<StateTag> finalTags = new ArrayList<>();
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
        finalTags.add(WHITESPACE);
        finalTags.add(WHITESPACE_IN_REGEX);

        // Restoring recognizers from files.
        this.recognizers = new HashMap<>();
        this.recognizers.put(REGEX, new LexicalRecognizer("res/generated/recognizers/REGEX.reco", finalTags));
        this.recognizers.put(INITIAL, new LexicalRecognizer("res/generated/recognizers/INITIAL.reco", finalTags));
        this.recognizers.put(CHAR_CLASS, new LexicalRecognizer("res/generated/recognizers/CHAR_CLASS.reco", finalTags));
        this.recognizers.put(COMMENT, new LexicalRecognizer("res/generated/recognizers/COMMENT.reco", finalTags));

        // just in case
        resetCurrState();
    }

    public String getInputText() {
        return inputText.toString();
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

    private void advanceCurrentPosition() {
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

    private int getCurrentCodePoint() {
        int cp = this.currPos.getIndex();
        return this.inputText.codePointAt(cp);
    }

    private boolean atPotentialTokenStart() {
        int currCodePoint = getCurrentCodePoint();
        // assuming general use case that all token starts are recognized by default mode
        LexicalRecognizer recognizer = this.recognizers.get(INITIAL);
        int nextState = recognizer.transition(recognizer.getInitialState(), currCodePoint);
        return nextState != LexicalRecognizer.DEAD_END_STATE;
    }

    private LexicalRecognizer getCurrentRecognizer() {
        return this.recognizers.get(this.currentMode);
    }

    private boolean isFinal(int currState) {
        return StateTag.isFinal(this.recognizers.get(this.currentMode).getStateTag(currState));
    }

    @Override
    final public boolean hasNext() {
        return this.hasNext;
    }

    @Override
    final public Token next() {
        return nextToken();
    }

    public Token nextToken() {
        resetCurrState();
        setStartToCurrentPosition();

        // Save last final state encountered
        OptionalInt lastFinalState = OptionalInt.empty();
        Position lastInFinal = new Position(); // will be used only if lastFinalState is present

        while (true) {
            int currCodePoint = getCurrentCodePoint();

            /// This guards against finding EOI while completing an earlier started token
            if (currCodePoint == Text.EOI && this.currPos.equals(this.start)) {
                this.hasNext = false;
                return Domain.END_OF_INPUT.createToken(this.inputText, new Fragment(currPos, currPos));
            }

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

                // we've found an error
                if (!isFinal(this.currState) && !lastFinalState.isPresent()) {

                    /// ERROR HANDLING CODE GOES HERE!
                    handleError(currCodePoint, this.currentMode, this.currPos);

                    // recovery
                    // symbol we've stumbled upon might be the beginning of a new token
                    while ((getCurrentCodePoint() != Text.EOI) && !atPotentialTokenStart()) {
                        advanceCurrentPosition();
                    }
                    switchToMode(INITIAL); // resetting to default mode after error recovery
                    Fragment invalidFragment = new Fragment(this.start, this.currPos);

                    return Domain.ERROR.createToken(this.inputText, invalidFragment);
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
                        case RULE_END:
                            optToken = handleRuleEnd(this.inputText, scannedFragment);
                            break;
                        case COMMA:
                            optToken = handleComma(this.inputText, scannedFragment);
                            break;
                        case L_ANGLE_BRACKET:
                            optToken = handleLAngleBracket(this.inputText, scannedFragment);
                            break;
                        case R_ANGLE_BRACKET:
                            optToken = handleRAngleBracket(this.inputText, scannedFragment);
                            break;
                        case RULES_SECTION_MARKER:
                            optToken = handleRulesSectionMarker(this.inputText, scannedFragment);
                            break;
                        case DOMAINS_GROUP_MARKER:
                            optToken = handleDomainsGroupMarker(this.inputText, scannedFragment);
                            break;
                        case MODES_SECTION_MARKER:
                            optToken = handleModesSectionMarker(this.inputText, scannedFragment);
                            break;
                        case DEFINER:
                            optToken = handleDefiner(this.inputText, scannedFragment);
                            break;
                        case IDENTIFIER:
                            optToken = handleIdentifier(this.inputText, scannedFragment);
                            break;
                        case NAMED_EXPR:
                            optToken = handleNamedExpr(this.inputText, scannedFragment);
                            break;
                        case LPAREN:
                            optToken = handleLParen(this.inputText, scannedFragment);
                            break;
                        case RPAREN:
                            optToken = handleRParen(this.inputText, scannedFragment);
                            break;
                        case CLASS_MINUS_OP:
                            optToken = handleClassMinusOp(this.inputText, scannedFragment);
                            break;
                        case REPETITION_OP:
                            optToken = handleRepetitionOp(this.inputText, scannedFragment);
                            break;
                        case OPTION_OP:
                            optToken = handleOptionOp(this.inputText, scannedFragment);
                            break;
                        case UNION_OP:
                            optToken = handleUnionOp(this.inputText, scannedFragment);
                            break;
                        case POS_ITERATION_OP:
                            optToken = handlePosIterationOp(this.inputText, scannedFragment);
                            break;
                        case ITERATION_OP:
                            optToken = handleIterationOp(this.inputText, scannedFragment);
                            break;
                        case DOT:
                            optToken = handleDot(this.inputText, scannedFragment);
                            break;
                        case CHAR_CLASS_RANGE_OP:
                            optToken = handleCharClassRangeOp(this.inputText, scannedFragment);
                            break;
                        case CHAR_CLASS_NEG:
                            optToken = handleCharClassNeg(this.inputText, scannedFragment);
                            break;
                        case CHAR_CLASS_OPEN:
                            optToken = handleCharClassOpen(this.inputText, scannedFragment);
                            break;
                        case CHAR_CLASS_CLOSE:
                            optToken = handleCharClassClose(this.inputText, scannedFragment);
                            break;
                        case CHAR:
                            optToken = handleChar(this.inputText, scannedFragment);
                            break;
                        case CLASS_CHAR:
                            optToken = handleClassChar(this.inputText, scannedFragment);
                            break;
                        case COMMENT_START:
                            optToken = handleCommentStart(this.inputText, scannedFragment);
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

    protected abstract void handleError(int codePoint, Mode mode, Position errorAt);

    protected abstract Optional<Token> handleWhitespaceInRegex(Text text, Fragment fragment);

    protected abstract Optional<Token> handleRuleEnd(Text text, Fragment fragment);

    protected abstract Optional<Token> handleComma(Text text, Fragment fragment);

    protected abstract Optional<Token> handleLAngleBracket(Text text, Fragment fragment);

    protected abstract Optional<Token> handleRAngleBracket(Text text, Fragment fragment);

    protected abstract Optional<Token> handleRulesSectionMarker(Text text, Fragment fragment);

    protected abstract Optional<Token> handleDomainsGroupMarker(Text text, Fragment fragment);

    protected abstract Optional<Token> handleModesSectionMarker(Text text, Fragment fragment);

    protected abstract Optional<Token> handleDefiner(Text text, Fragment fragment);

    protected abstract Optional<Token> handleIdentifier(Text text, Fragment fragment);

    protected abstract Optional<Token> handleNamedExpr(Text text, Fragment fragment);

    protected abstract Optional<Token> handleLParen(Text text, Fragment fragment);

    protected abstract Optional<Token> handleRParen(Text text, Fragment fragment);

    protected abstract Optional<Token> handleClassMinusOp(Text text, Fragment fragment);

    protected abstract Optional<Token> handleRepetitionOp(Text text, Fragment fragment);

    protected abstract Optional<Token> handleOptionOp(Text text, Fragment fragment);

    protected abstract Optional<Token> handleUnionOp(Text text, Fragment fragment);

    protected abstract Optional<Token> handlePosIterationOp(Text text, Fragment fragment);

    protected abstract Optional<Token> handleIterationOp(Text text, Fragment fragment);

    protected abstract Optional<Token> handleDot(Text text, Fragment fragment);

    protected abstract Optional<Token> handleCharClassRangeOp(Text text, Fragment fragment);

    protected abstract Optional<Token> handleCharClassNeg(Text text, Fragment fragment);

    protected abstract Optional<Token> handleCharClassOpen(Text text, Fragment fragment);

    protected abstract Optional<Token> handleCharClassClose(Text text, Fragment fragment);

    protected abstract Optional<Token> handleChar(Text text, Fragment fragment);

    protected abstract Optional<Token> handleClassChar(Text text, Fragment fragment);

    protected abstract Optional<Token> handleCommentStart(Text text, Fragment fragment);

    protected abstract Optional<Token> handleNoAsteriskSeq(Text text, Fragment fragment);

    protected abstract Optional<Token> handleCommentClose(Text text, Fragment fragment);

    protected abstract Optional<Token> handleCommentAsterisk(Text text, Fragment fragment);

}