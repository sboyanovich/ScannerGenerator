package io.github.sboyanovich.scannergenerator.tests.lexgen3;

import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.scanner.*;
import io.github.sboyanovich.scannergenerator.scanner.token.TEndOfProgram;
import io.github.sboyanovich.scannergenerator.scanner.token.TError;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static io.github.sboyanovich.scannergenerator.tests.lexgen3.StateTags.*;

public class LexGenScanner {
    private static final int NEWLINE = Utility.asCodePoint("\n");
    private static final int CARRET = Utility.asCodePoint("\r");

    private List<LexicalRecognizer> recognizers;
    private Position currPos;
    private Text inputText;
    private int currentMode;
    private int currState;
    private Map<String, Integer> modeNamesInv = Map.of("INITIAL", 0, "CHAR_CLASS", 1);

    private MockCompiler compiler;

    public LexGenScanner(String inputText) {
        this.inputText = new Text(inputText);
        this.currentMode = 0;
        this.currPos = new Position();

        int alphabetSize = Character.MAX_CODE_POINT + 1;
        //alphabetSize = 256;

        NFA classSingleCharNFA = NFA.acceptsAllSymbolsButThese(
                alphabetSize, Set.of("\r", "\b", "\t", "\n", "\f", "\\", "-", "^", "[", "]"));

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
                                        "\\-", "\\^", "\\[", "\\]")
                        )
                );

        NFA classCharNFA = classSingleCharNFA.union(classEscapeNFA)
                .setAllFinalStatesTo(CHAR);

        NFA escapeNFA = uEscapeNFA
                .union(
                        NFA.acceptsAllTheseWords(
                                alphabetSize,
                                Set.of(
                                        "\\b", "\\t", "\\n", "\\f", "\\r", "\\\\",
                                        "\\\"", "\\'", "\\*", "\\+", "\\|", "\\?",
                                        "\\.", "\\(", "\\)"
                                )
                        )
                );

        NFA inputCharNFA = NFA.acceptsAllSymbolsButThese(
                alphabetSize, Set.of("\r", "\n")
        );

        NFA underscoreNFA = NFA.singleLetterLanguage(alphabetSize, "_");
        NFA latinLettersNFA = NFA.acceptsThisRange(alphabetSize, "A", "Z")
                .union(NFA.acceptsThisRange(alphabetSize, "a", "z"));
        NFA idenStartNFA = latinLettersNFA.union(underscoreNFA);
        NFA idenPartNFA = idenStartNFA.union(decimalDigitsNFA);
        NFA identifierNFA = idenStartNFA.concatenation(idenPartNFA.iteration());

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

        List<StateTag> priorityList = new ArrayList<>(
                List.of(
                        CHAR,
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
                        NAMED_EXPR
                )
        );

        Map<StateTag, Integer> priorityMap = new HashMap<>();
        for (int i = 0; i < priorityList.size(); i++) {
            priorityMap.put(priorityList.get(i), i);
        }

        NFA mode0 = charNFA
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
                .union(repetitionOpNFA);

        NFA mode1 = classCharNFA
                .union(charClassRangeOpNFA)
                .union(charClassNegNFA)
                .union(charClassCloseNFA);

        System.out.println("Mode 0");
        LexicalRecognizer m0 = buildRecognizer(mode0, priorityMap);
        System.out.println();
        System.out.println("Mode 1");
        LexicalRecognizer m1 = buildRecognizer(mode1, priorityMap);
        System.out.println();

        this.recognizers = List.of(m0, m1);

        //not exactly necessary
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

    // should be protected probably
    void switchToMode(int mode) {
        this.currentMode = mode;
        resetCurrState();
    }

    void switchToMode(String mode) {
        switchToMode(modeNamesInv.get(mode));
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

    public Token nextToken() {
        if (getCurrentCodePoint() == Text.EOI) {
            return new TEndOfProgram(new Fragment(currPos, currPos));
        }

        resetCurrState();

        Position start = currPos;

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

                // we've found an error
                if (!isFinal(this.currState) && !lastFinalState.isPresent()) {

                    /// ERROR HANDLING CODE GOES HERE!
                    this.compiler.addError(this.currPos, "Unexpected symbol encountered.");

                    // recovery
                    // symbol we've stumbled upon might be the beginning of a new token

                    while ((getCurrentCodePoint() != Text.EOI) && !atPotentialTokenStart()) {
                        advanceCurrentPosition();
                    }
                    switchToMode(0); // resetting to default mode after error recovery
                    Fragment invalidFragment = new Fragment(start, this.currPos);
                    return new TError(invalidFragment, getTextFragment(invalidFragment));
                } else {
                    if (lastFinalState.isPresent()) {
                        this.currPos = lastInFinal;
                        this.currState = lastFinalState.getAsInt();
                    }
                    // now currState is certainly final

                    lastFinalState = OptionalInt.empty(); // something matched, no reusing this!
                    Fragment scannedFragment = new Fragment(start, this.currPos);
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
                    }

                    if (optToken.isPresent()) {
                        return optToken.get();
                    }
                }
            }
        }
    }

    Optional<Token> handleLParen(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.LPAREN.createToken(text, fragment));
    }

    Optional<Token> handleRParen(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.RPAREN.createToken(text, fragment));
    }

    Optional<Token> handleOptionOp(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.OPTION_OP.createToken(text, fragment));
    }

    Optional<Token> handleUnionOp(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.UNION_OP.createToken(text, fragment));
    }

    Optional<Token> handlePosIterationOp(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.POS_ITERATION_OP.createToken(text, fragment));
    }

    Optional<Token> handleIterationOp(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.ITERATION_OP.createToken(text, fragment));
    }

    Optional<Token> handleDot(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.DOT.createToken(text, fragment));
    }

    Optional<Token> handleCharClassRangeOp(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.CHAR_CLASS_RANGE_OP.createToken(text, fragment));
    }

    Optional<Token> handleCharClassNeg(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.CHAR_CLASS_NEG.createToken(text, fragment));
    }

    Optional<Token> handleCharClassOpen(Text text, Fragment fragment) {
        switchToMode("CHAR_CLASS");
        return Optional.of(SimpleDomains.CHAR_CLASS_OPEN.createToken(text, fragment));
    }

    Optional<Token> handleCharClassClose(Text text, Fragment fragment) {
        switchToMode("INITIAL");
        return Optional.of(SimpleDomains.CHAR_CLASS_CLOSE.createToken(text, fragment));
    }

    Optional<Token> handleChar(Text text, Fragment fragment) {
        return Optional.of(DomainsWithIntegerAttribute.CHAR.createToken(text, fragment));
    }

    Optional<Token> handleNamedExpr(Text text, Fragment fragment) {
        return Optional.of(DomainsWithStringAttribute.NAMED_EXPR.createToken(text, fragment));
    }

    Optional<Token> handleClassMinusOp(Text text, Fragment fragment) {
        return Optional.of(SimpleDomains.CLASS_MINUS_OP.createToken(text, fragment));
    }

    Optional<Token> handleRepetitionOp(Text text, Fragment fragment) {
        return Optional.of(DomainsWithIntPairAttribute.REPETITION_OP.createToken(text, fragment));
    }

    public MockCompiler getCompiler() {
        return compiler;
    }
}
