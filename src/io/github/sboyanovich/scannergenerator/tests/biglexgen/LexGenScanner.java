package io.github.sboyanovich.scannergenerator.tests.biglexgen;

import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.automata.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.*;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static io.github.sboyanovich.scannergenerator.tests.biglexgen.LexGenScanner.Mode.*;
import static io.github.sboyanovich.scannergenerator.tests.biglexgen.StateTags.*;

public class LexGenScanner implements Iterator<Token> {
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
    private MockCompiler compiler;

    public LexGenScanner(String inputText) {
        this.inputText = new Text(inputText);
        this.currentMode = INITIAL;
        this.currPos = new Position();
        this.start = this.currPos;
        this.hasNext = true;

        boolean hasFiles = false;

        if (hasFiles) {
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
            this.recognizers.put(INITIAL, new LexicalRecognizer("INITIAL.reco", finalTags));
            this.recognizers.put(REGEX, new LexicalRecognizer("REGEX.reco", finalTags));
            this.recognizers.put(CHAR_CLASS, new LexicalRecognizer("CHAR_CLASS.reco", finalTags));
            this.recognizers.put(COMMENT, new LexicalRecognizer("COMMENT.reco", finalTags));
        } else {

            int alphabetSize = Character.MAX_CODE_POINT + 1;

            NFA spaceNFA = NFA.singleLetterLanguage(alphabetSize, " ");
            NFA tabNFA = NFA.singleLetterLanguage(alphabetSize, "\t");
            NFA newlineNFA = NFA.singleLetterLanguage(alphabetSize, "\n");
            NFA carretNFA = NFA.singleLetterLanguage(alphabetSize, "\r");

            NFA whitespaceNFA = spaceNFA
                    .union(tabNFA)
                    .union(carretNFA.concatenation(newlineNFA))
                    .union(newlineNFA)
                    .positiveIteration()
                    .setAllFinalStatesTo(WHITESPACE);

            NFA whitespaceInRegexNFA = whitespaceNFA
                    .setAllFinalStatesTo(WHITESPACE_IN_REGEX);

            NFA classSingleCharNFA = NFA.acceptsAllSymbolsButThese(
                    alphabetSize, Set.of("\r", "\b", "\n", "\f", "\\", "-", "^", "]"));

            NFA decimalDigitsNFA = NFA.acceptsThisRange(alphabetSize, "0", "9");
            NFA hexDigitsNFA = decimalDigitsNFA
                    .union(
                            NFA.acceptsThisRange(alphabetSize, "A", "F")
                    );

            NFA decimalNumberNFA = decimalDigitsNFA.positiveIteration();
            NFA hexNumberNFA = hexDigitsNFA.positiveIteration();

            NFA decimalEscapeNFA = NFA.acceptsThisWord(alphabetSize, "\\U+#")
                    .concatenation(decimalNumberNFA);
            NFA hexEscapeNFA = NFA.acceptsThisWord(alphabetSize, "\\U+")
                    .concatenation(hexNumberNFA);

            NFA uEscapeNFA = decimalEscapeNFA.union(hexEscapeNFA);

            NFA classEscapeNFA = uEscapeNFA
                    .union(
                            NFA.acceptsAllTheseWords(
                                    alphabetSize,
                                    Set.of(
                                            "\\b", "\\t", "\\n", "\\f", "\\r", "\\\\",
                                            "\\-", "\\^", "\\]")
                            )
                    );

            NFA classCharNFA = classSingleCharNFA.union(classEscapeNFA)
                    .setAllFinalStatesTo(CLASS_CHAR);

            NFA escapeNFA = uEscapeNFA
                    .union(
                            NFA.acceptsAllTheseWords(
                                    alphabetSize,
                                    Set.of(
                                            "\\b", "\\t", "\\n", "\\f", "\\r", "\\\\",
                                            "\\*", "\\+", "\\|", "\\?",
                                            "\\.", "\\(", "\\)", "\\[", "\\{", "\\}"
                                    )
                            )
                    );

            NFA inputCharNFA = NFA.acceptsAllSymbolsButThese(
                    alphabetSize, Set.of("\r", "\n", "\\")
            );

            NFA underscoreNFA = NFA.singleLetterLanguage(alphabetSize, "_");
            NFA latinLettersNFA = NFA.acceptsThisRange(alphabetSize, "A", "Z")
                    .union(NFA.acceptsThisRange(alphabetSize, "a", "z"));
            NFA idenStartNFA = latinLettersNFA.union(underscoreNFA);
            NFA idenPartNFA = idenStartNFA.union(decimalDigitsNFA);
            NFA identifierNFA = idenStartNFA.concatenation(idenPartNFA.iteration())
                    .setAllFinalStatesTo(IDENTIFIER);

            NFA namedExprNFA = NFA.singleLetterLanguage(alphabetSize, "{")
                    .concatenation(identifierNFA)
                    .concatenation(NFA.singleLetterLanguage(alphabetSize, "}"))
                    .setAllFinalStatesTo(NAMED_EXPR);

            NFA charNFA = inputCharNFA.union(escapeNFA)
                    .setAllFinalStatesTo(CHAR);

            NFA charClassRangeOpNFA = NFA.singleLetterLanguage(alphabetSize, "-")
                    .setAllFinalStatesTo(CHAR_CLASS_RANGE_OP);

            NFA charClassOpenNFA = NFA.singleLetterLanguage(alphabetSize, "[")
                    .setAllFinalStatesTo(CHAR_CLASS_OPEN);

            NFA charClassCloseNFA = NFA.singleLetterLanguage(alphabetSize, "]")
                    .setAllFinalStatesTo(CHAR_CLASS_CLOSE);

            NFA charClassNegNFA = NFA.singleLetterLanguage(alphabetSize, "^")
                    .setAllFinalStatesTo(CHAR_CLASS_NEG);

            NFA dotNFA = NFA.singleLetterLanguage(alphabetSize, ".")
                    .setAllFinalStatesTo(DOT);
            NFA iterationOpNFA = NFA.singleLetterLanguage(alphabetSize, "*")
                    .setAllFinalStatesTo(ITERATION_OP);
            NFA posIterationOpNFA = NFA.singleLetterLanguage(alphabetSize, "+")
                    .setAllFinalStatesTo(POS_ITERATION_OP);
            NFA unionOpNFA = NFA.singleLetterLanguage(alphabetSize, "|")
                    .setAllFinalStatesTo(UNION_OP);
            NFA optionOpNFA = NFA.singleLetterLanguage(alphabetSize, "?")
                    .setAllFinalStatesTo(OPTION_OP);

            NFA lParenNFA = NFA.singleLetterLanguage(alphabetSize, "(")
                    .setAllFinalStatesTo(LPAREN);
            NFA rParenNFA = NFA.singleLetterLanguage(alphabetSize, ")")
                    .setAllFinalStatesTo(RPAREN);

            NFA classMinusOpNFA = NFA.acceptsThisWord(alphabetSize, "{-}")
                    .setAllFinalStatesTo(CLASS_MINUS_OP);

            NFA repetitionOpNFA = NFA.singleLetterLanguage(alphabetSize, "{")
                    .concatenation(decimalNumberNFA)
                    .concatenation(
                            NFA.singleLetterLanguage(alphabetSize, ",")
                                    .concatenation(decimalNumberNFA.optional()).optional()
                    )
                    .concatenation(NFA.singleLetterLanguage(alphabetSize, "}"))
                    .setAllFinalStatesTo(REPETITION_OP);

            NFA lAngleBracketNFA = NFA.singleLetterLanguage(alphabetSize, "<")
                    .setAllFinalStatesTo(L_ANGLE_BRACKET);

            NFA rAngleBracketNFA = NFA.singleLetterLanguage(alphabetSize, ">")
                    .setAllFinalStatesTo(R_ANGLE_BRACKET);

            NFA commaNFA = NFA.singleLetterLanguage(alphabetSize, ",")
                    .setAllFinalStatesTo(COMMA);

            NFA ruleEndNFA = NFA.singleLetterLanguage(alphabetSize, ";")
                    .setAllFinalStatesTo(RULE_END);
            NFA rulesSectionMarkerNFA = NFA.acceptsThisWord(alphabetSize, "%%")
                    .setAllFinalStatesTo(RULES_SECTION_MARKER);

            // DIRTY HACK TBH
            NFA definerNFA = NFA.acceptsThisWord(alphabetSize, ":=")
                    .concatenation(
                            NFA.acceptsAllTheseSymbols(alphabetSize, Set.of(" ", "\t"))
                                    .positiveIteration()
                    )
                    .setAllFinalStatesTo(DEFINER);
            NFA modesSectionMarkerNFA = NFA.acceptsThisWord(alphabetSize, "%MODES")
                    .setAllFinalStatesTo(MODES_SECTION_MARKER);
            NFA domainsGroupMarkerNFA = NFA.acceptsThisWord(alphabetSize, "%DOMAINS")
                    .concatenation(
                            NFA.singleLetterLanguage(alphabetSize, "[")
                                    .concatenation(identifierNFA)
                                    .concatenation(NFA.singleLetterLanguage(alphabetSize, "]"))
                                    .optional()
                    )
                    .setAllFinalStatesTo(DOMAINS_GROUP_MARKER);

            NFA commentStartNFA = NFA.acceptsThisWord(alphabetSize, "/*")
                    .setAllFinalStatesTo(COMMENT_START);
            NFA noAsteriskSeqNFA = NFA.acceptsAllSymbolsButThese(alphabetSize, Set.of("*")).iteration()
                    .setAllFinalStatesTo(NO_ASTERISK_SEQ);
            NFA commentCloseNFA = NFA.acceptsThisWord(alphabetSize, "*/")
                    .setAllFinalStatesTo(COMMENT_CLOSE);
            NFA asteriskNFA = NFA.singleLetterLanguage(alphabetSize, "*")
                    .setAllFinalStatesTo(ASTERISK);

            List<StateTag> priorityList = new ArrayList<>(
                    List.of(
                            ASTERISK,
                            COMMENT_CLOSE,
                            NO_ASTERISK_SEQ,
                            COMMENT_START,
                            CHAR,
                            CLASS_CHAR,
                            CHAR_CLASS_OPEN,
                            CHAR_CLASS_CLOSE,
                            CHAR_CLASS_NEG,
                            CHAR_CLASS_RANGE_OP,
                            DOT,
                            ITERATION_OP,
                            POS_ITERATION_OP,
                            UNION_OP,
                            OPTION_OP,
                            LPAREN,
                            RPAREN,
                            REPETITION_OP,
                            CLASS_MINUS_OP,
                            NAMED_EXPR,
                            IDENTIFIER,
                            DEFINER,
                            MODES_SECTION_MARKER,
                            DOMAINS_GROUP_MARKER,
                            RULES_SECTION_MARKER,
                            L_ANGLE_BRACKET,
                            R_ANGLE_BRACKET,
                            COMMA,
                            RULE_END,
                            WHITESPACE,
                            WHITESPACE_IN_REGEX
                    )
            );

            Map<StateTag, Integer> priorityMap = new HashMap<>();
            for (int i = 0; i < priorityList.size(); i++) {
                priorityMap.put(priorityList.get(i), i);
            }

            NFA mode0 = whitespaceNFA
                    .union(identifierNFA)
                    .union(definerNFA)
                    .union(modesSectionMarkerNFA)
                    .union(domainsGroupMarkerNFA)
                    .union(rulesSectionMarkerNFA)
                    .union(lAngleBracketNFA)
                    .union(rAngleBracketNFA)
                    .union(commaNFA)
                    .union(ruleEndNFA)
                    .union(commentStartNFA);

            NFA mode1 = whitespaceInRegexNFA
                    .union(charClassOpenNFA)
                    .union(dotNFA)
                    .union(iterationOpNFA)
                    .union(posIterationOpNFA)
                    .union(unionOpNFA)
                    .union(optionOpNFA)
                    .union(lParenNFA)
                    .union(rParenNFA)
                    .union(namedExprNFA)
                    .union(classMinusOpNFA)
                    .union(repetitionOpNFA)
                    .union(charNFA);

            NFA mode2 = classCharNFA
                    .union(charClassRangeOpNFA)
                    .union(charClassNegNFA)
                    .union(charClassCloseNFA);

            NFA mode3 = noAsteriskSeqNFA.union(commentCloseNFA).union(asteriskNFA);

            System.out.println("Mode 0");
            LexicalRecognizer m0 = buildRecognizer(mode0, priorityMap);
            System.out.println();
            System.out.println("Mode 1");
            LexicalRecognizer m1 = buildRecognizer(mode1, priorityMap);
            System.out.println();
            System.out.println("Mode 2");
            LexicalRecognizer m2 = buildRecognizer(mode2, priorityMap);
            System.out.println();
            System.out.println("Mode 3");
            LexicalRecognizer m3 = buildRecognizer(mode3, priorityMap);
            System.out.println();

            // Restoring recognizers from files.
            this.recognizers = new HashMap<>();
            this.recognizers.put(INITIAL, m0);
            this.recognizers.put(REGEX, m1);
            this.recognizers.put(CHAR_CLASS, m2);
            this.recognizers.put(COMMENT, m3);
        }
        // just in case
        resetCurrState();

        this.compiler = new MockCompiler();
    }

    static LexicalRecognizer buildRecognizer(NFA lang, Map<StateTag, Integer> priorityMap) {
        System.out.println(lang.getNumberOfStates());

        // This appears to be necessary for determinization to work properly. It shouldn't be.
        lang = lang.removeLambdaSteps();
        System.out.println("Lambda steps removed.");

        Instant start = Instant.now();
        DFA dfa = lang.determinize(priorityMap);
        Instant stop = Instant.now();
        long timeElapsed = Duration.between(start, stop).toMillis();

        System.out.println("Determinized!");
        System.out.println("\tin " + timeElapsed + "ms");
        System.out.println("States: " + dfa.getNumberOfStates());
        System.out.println("Classes: " + dfa.getTransitionTable().getEquivalenceMap().getEqClassDomain());

        start = Instant.now();
        LexicalRecognizer recognizer = new LexicalRecognizer(dfa);
        stop = Instant.now();
        timeElapsed = Duration.between(start, stop).toMillis();
        System.out.println("Recognizer built!");
        System.out.println("\tin " + timeElapsed + "ms");
        System.out.println("States: " + recognizer.getNumberOfStates());
        System.out.println("Classes: " + recognizer.getNumberOfColumns());

        String dot = recognizer.toGraphvizDotString(Object::toString, true);
        System.out.println(dot);
        String factorization = recognizer.displayEquivalenceMap(Utility::defaultUnicodeInterpretation);
        System.out.println("\n" + factorization + "\n");

        return recognizer;
    }

    public String getInputText() {
        return inputText.toString();
    }

    private void resetCurrState() {
        this.currState = getCurrentRecognizer().getInitialState();
    }

    protected void switchToMode(Mode mode) {
        this.currentMode = mode;
        resetCurrState();
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

    protected void setStartToCurrentPosition() {
        this.start = this.currPos;
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
                    this.compiler.addError(this.currPos, "Unexpected symbol encountered: " + currCodePoint);

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

                    /// for ignored expressions (e.g. whitespace) case should just reset start

                    switch (tag) {
                        case NAMED_EXPR:
                            optToken = handleNamedExpr(this.inputText, scannedFragment);
                            break;
                        case CLASS_MINUS_OP:
                            optToken = handleClassMinusOp(this.inputText, scannedFragment);
                            break;
                        case REPETITION_OP:
                            optToken = handleRepetitionOp(this.inputText, scannedFragment);
                            break;
                        case LPAREN:
                            optToken = handleLParen(this.inputText, scannedFragment);
                            break;
                        case RPAREN:
                            optToken = handleRParen(this.inputText, scannedFragment);
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
                        case IDENTIFIER:
                            optToken = handleIdentifier(this.inputText, scannedFragment);
                            break;
                        case DEFINER:
                            optToken = handleDefiner(this.inputText, scannedFragment);
                            break;
                        case MODES_SECTION_MARKER:
                            optToken = handleModesSectionMarker(this.inputText, scannedFragment);
                            break;
                        case DOMAINS_GROUP_MARKER:
                            optToken = handleDomainsGroupMarker(this.inputText, scannedFragment);
                            break;
                        case RULES_SECTION_MARKER:
                            optToken = handleRulesSectionMarker(this.inputText, scannedFragment);
                            break;
                        case L_ANGLE_BRACKET:
                            optToken = handleLAngleBracket(this.inputText, scannedFragment);
                            break;
                        case R_ANGLE_BRACKET:
                            optToken = handleRAngleBracket(this.inputText, scannedFragment);
                            break;
                        case COMMA:
                            optToken = handleComma(this.inputText, scannedFragment);
                            break;
                        case RULE_END:
                            optToken = handleRuleEnd(this.inputText, scannedFragment);
                            break;
                        case COMMENT_START:
                            optToken = handleCommentStart(this.inputText, scannedFragment);
                            break;
                        case NO_ASTERISK_SEQ:
                            optToken = handleNoAsteriskSeq(this.inputText, scannedFragment);
                            break;
                        case ASTERISK:
                            optToken = handleCommentAsterisk(this.inputText, scannedFragment);
                            break;
                        case COMMENT_CLOSE:
                            optToken = handleCommentClose(this.inputText, scannedFragment);
                            break;
                        case WHITESPACE:
                            setStartToCurrentPosition();
                            break;
                        case WHITESPACE_IN_REGEX:
                            optToken = handleWhitespaceInRegex(this.inputText, scannedFragment);
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

    protected Optional<Token> handleLParen(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.LPAREN.createToken(text, fragment));
    }

    protected Optional<Token> handleRParen(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.RPAREN.createToken(text, fragment));
    }

    protected Optional<Token> handleOptionOp(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.OPTION_OP.createToken(text, fragment));
    }

    protected Optional<Token> handleUnionOp(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.UNION_OP.createToken(text, fragment));
    }

    protected Optional<Token> handlePosIterationOp(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.POS_ITERATION_OP.createToken(text, fragment));
    }

    protected Optional<Token> handleIterationOp(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.ITERATION_OP.createToken(text, fragment));
    }

    protected Optional<Token> handleDot(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.DOT.createToken(text, fragment));
    }

    protected Optional<Token> handleCharClassRangeOp(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.CHAR_CLASS_RANGE_OP.createToken(text, fragment));
    }

    protected Optional<Token> handleCharClassNeg(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.CHAR_CLASS_NEG.createToken(text, fragment));
    }

    protected Optional<Token> handleCharClassOpen(Text text, Fragment fragment) {
        switchToMode(CHAR_CLASS);
        return Optional.of(SimpleDomains.CHAR_CLASS_OPEN.createToken(text, fragment));
    }

    protected Optional<Token> handleCharClassClose(Text text, Fragment fragment) {
        switchToMode(REGEX);
        return Optional.of(SimpleDomains.CHAR_CLASS_CLOSE.createToken(text, fragment));
    }

    protected Optional<Token> handleChar(Text text, Fragment fragment) {
        return Optional.of(DomainsWithIntegerAttribute.CHAR.createToken(text, fragment));
    }

    protected Optional<Token> handleClassChar(Text text, Fragment fragment) {
        return Optional.of(DomainsWithIntegerAttribute.CHAR.createToken(text, fragment));
    }

    protected Optional<Token> handleNamedExpr(Text text, Fragment fragment) {
        return Optional.of(DomainsWithStringAttribute.NAMED_EXPR.createToken(text, fragment));
    }

    protected Optional<Token> handleClassMinusOp(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.CLASS_MINUS_OP.createToken(text, fragment));
    }

    protected Optional<Token> handleRepetitionOp(Text text, Fragment fragment) {
        return Optional.of(DomainsWithIntPairAttribute.REPETITION_OP.createToken(text, fragment));
    }

    protected Optional<Token> handleLAngleBracket(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.L_ANGLE_BRACKET.createToken(text, fragment));
    }

    protected Optional<Token> handleRAngleBracket(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.R_ANGLE_BRACKET.createToken(text, fragment));
    }

    protected Optional<Token> handleDefiner(Text text, Fragment fragment) {
        switchToMode(REGEX);
        return Optional.of(SimpleDomains.DEFINER.createToken(text, fragment));
    }

    protected Optional<Token> handleRulesSectionMarker(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.RULES_SECTION_MARKER.createToken(text, fragment));
    }

    protected Optional<Token> handleRuleEnd(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.RULE_END.createToken(text, fragment));
    }

    protected Optional<Token> handleComma(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.COMMA.createToken(text, fragment));
    }

    protected Optional<Token> handleModesSectionMarker(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.MODES_SECTION_MARKER.createToken(text, fragment));
    }

    protected Optional<Token> handleIdentifier(Text text, Fragment fragment) {
        return Optional.of(DomainsWithStringAttribute.IDENTIFIER.createToken(text, fragment));
    }

    protected Optional<Token> handleDomainsGroupMarker(Text text, Fragment fragment) {
        return Optional.of(DomainsWithStringAttribute.DOMAINS_GROUP_MARKER.createToken(text, fragment));
    }

    protected Optional<Token> handleWhitespaceInRegex(Text text, Fragment fragment) {
        switchToMode(INITIAL);
        setStartToCurrentPosition();
        return Optional.empty();
    }

    protected Optional<Token> handleCommentStart(Text text, Fragment fragment) {
        switchToMode(COMMENT);
        return Optional.empty();
    }

    protected Optional<Token> handleNoAsteriskSeq(Text text, Fragment fragment) {
        return Optional.empty();
    }

    protected Optional<Token> handleCommentAsterisk(Text text, Fragment fragment) {
        return Optional.empty();
    }

    protected Optional<Token> handleCommentClose(Text text, Fragment fragment) {
        switchToMode(INITIAL);
        setStartToCurrentPosition();
        return Optional.empty();
    }

    public MockCompiler getCompiler() {
        return compiler;
    }
}
