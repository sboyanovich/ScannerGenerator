package io.github.sboyanovich.scannergenerator.tests.mockjava2;

import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.scanner.*;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.tests.mockjava2.data.domains.SimpleDomains;
import io.github.sboyanovich.scannergenerator.tests.mockjava2.data.states.HelperTags;
import io.github.sboyanovich.scannergenerator.tests.mockjava2.data.states.OperatorsTags;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static io.github.sboyanovich.scannergenerator.tests.mockjava2.data.states.HelperTags.*;
import static io.github.sboyanovich.scannergenerator.tests.mockjava2.data.states.StateTags.*;
import static io.github.sboyanovich.scannergenerator.utility.Utility.NEWLINE;

public class MockJavaTest {
    public static void main(String[] args) {
        int alphabetSize = Character.MAX_CODE_POINT + 1;
        //alphabetSize = 256;

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

        NFA kw_abstractNFA = NFA.acceptsThisWord(alphabetSize, "abstract")
                .setAllFinalStatesTo(KEYWORD_ABSTRACT);
        NFA kw_assertNFA = NFA.acceptsThisWord(alphabetSize, "assert")
                .setAllFinalStatesTo(KEYWORD_ASSERT);
        NFA kw_booleanNFA = NFA.acceptsThisWord(alphabetSize, "boolean")
                .setAllFinalStatesTo(KEYWORD_BOOLEAN);
        NFA kw_breakNFA = NFA.acceptsThisWord(alphabetSize, "break")
                .setAllFinalStatesTo(KEYWORD_BREAK);
        NFA kw_byteNFA = NFA.acceptsThisWord(alphabetSize, "byte")
                .setAllFinalStatesTo(KEYWORD_BYTE);
        NFA kw_caseNFA = NFA.acceptsThisWord(alphabetSize, "case")
                .setAllFinalStatesTo(KEYWORD_CASE);
        NFA kw_catchNFA = NFA.acceptsThisWord(alphabetSize, "catch")
                .setAllFinalStatesTo(KEYWORD_CATCH);
        NFA kw_charNFA = NFA.acceptsThisWord(alphabetSize, "char")
                .setAllFinalStatesTo(KEYWORD_CHAR);
        NFA kw_classNFA = NFA.acceptsThisWord(alphabetSize, "class")
                .setAllFinalStatesTo(KEYWORD_CLASS);
        NFA kw_constNFA = NFA.acceptsThisWord(alphabetSize, "const")
                .setAllFinalStatesTo(KEYWORD_CONST);
        NFA kw_continueNFA = NFA.acceptsThisWord(alphabetSize, "continue")
                .setAllFinalStatesTo(KEYWORD_CONTINUE);
        NFA kw_defaultNFA = NFA.acceptsThisWord(alphabetSize, "default")
                .setAllFinalStatesTo(KEYWORD_DEFAULT);
        NFA kw_doNFA = NFA.acceptsThisWord(alphabetSize, "do")
                .setAllFinalStatesTo(KEYWORD_DO);
        NFA kw_doubleNFA = NFA.acceptsThisWord(alphabetSize, "double")
                .setAllFinalStatesTo(KEYWORD_DOUBLE);
        NFA kw_elseNFA = NFA.acceptsThisWord(alphabetSize, "else")
                .setAllFinalStatesTo(KEYWORD_ELSE);
        NFA kw_enumNFA = NFA.acceptsThisWord(alphabetSize, "enum")
                .setAllFinalStatesTo(KEYWORD_ENUM);
        NFA kw_extendsNFA = NFA.acceptsThisWord(alphabetSize, "extends")
                .setAllFinalStatesTo(KEYWORD_EXTENDS);
        NFA kw_finalNFA = NFA.acceptsThisWord(alphabetSize, "final")
                .setAllFinalStatesTo(KEYWORD_FINAL);
        NFA kw_finallyNFA = NFA.acceptsThisWord(alphabetSize, "finally")
                .setAllFinalStatesTo(KEYWORD_FINALLY);
        NFA kw_floatNFA = NFA.acceptsThisWord(alphabetSize, "float")
                .setAllFinalStatesTo(KEYWORD_FLOAT);
        NFA kw_forNFA = NFA.acceptsThisWord(alphabetSize, "for")
                .setAllFinalStatesTo(KEYWORD_FOR);
        NFA kw_ifNFA = NFA.acceptsThisWord(alphabetSize, "if")
                .setAllFinalStatesTo(KEYWORD_IF);
        NFA kw_gotoNFA = NFA.acceptsThisWord(alphabetSize, "goto")
                .setAllFinalStatesTo(KEYWORD_GOTO);
        NFA kw_implementsNFA = NFA.acceptsThisWord(alphabetSize, "implements")
                .setAllFinalStatesTo(KEYWORD_IMPLEMENTS);
        NFA kw_importNFA = NFA.acceptsThisWord(alphabetSize, "import")
                .setAllFinalStatesTo(KEYWORD_IMPORT);
        NFA kw_instanceofNFA = NFA.acceptsThisWord(alphabetSize, "instanceof")
                .setAllFinalStatesTo(KEYWORD_INSTANCEOF);
        NFA kw_intNFA = NFA.acceptsThisWord(alphabetSize, "int")
                .setAllFinalStatesTo(KEYWORD_INT);
        NFA kw_interfaceNFA = NFA.acceptsThisWord(alphabetSize, "interface")
                .setAllFinalStatesTo(KEYWORD_INTERFACE);
        NFA kw_longNFA = NFA.acceptsThisWord(alphabetSize, "long")
                .setAllFinalStatesTo(KEYWORD_LONG);
        NFA kw_nativeNFA = NFA.acceptsThisWord(alphabetSize, "native")
                .setAllFinalStatesTo(KEYWORD_NATIVE);
        NFA kw_newNFA = NFA.acceptsThisWord(alphabetSize, "new")
                .setAllFinalStatesTo(KEYWORD_NEW);
        NFA kw_packageNFA = NFA.acceptsThisWord(alphabetSize, "package")
                .setAllFinalStatesTo(KEYWORD_PACKAGE);
        NFA kw_privateNFA = NFA.acceptsThisWord(alphabetSize, "private")
                .setAllFinalStatesTo(KEYWORD_PRIVATE);
        NFA kw_protectedNFA = NFA.acceptsThisWord(alphabetSize, "protected")
                .setAllFinalStatesTo(KEYWORD_PROTECTED);
        NFA kw_publicNFA = NFA.acceptsThisWord(alphabetSize, "public")
                .setAllFinalStatesTo(KEYWORD_PUBLIC);
        NFA kw_returnNFA = NFA.acceptsThisWord(alphabetSize, "return")
                .setAllFinalStatesTo(KEYWORD_RETURN);
        NFA kw_shortNFA = NFA.acceptsThisWord(alphabetSize, "short")
                .setAllFinalStatesTo(KEYWORD_SHORT);
        NFA kw_staticNFA = NFA.acceptsThisWord(alphabetSize, "static")
                .setAllFinalStatesTo(KEYWORD_STATIC);
        NFA kw_strictfpNFA = NFA.acceptsThisWord(alphabetSize, "strictfp")
                .setAllFinalStatesTo(KEYWORD_STRICTFP);
        NFA kw_superNFA = NFA.acceptsThisWord(alphabetSize, "super")
                .setAllFinalStatesTo(KEYWORD_SUPER);
        NFA kw_switchNFA = NFA.acceptsThisWord(alphabetSize, "switch")
                .setAllFinalStatesTo(KEYWORD_SWITCH);
        NFA kw_synchronizedNFA = NFA.acceptsThisWord(alphabetSize, "synchronized")
                .setAllFinalStatesTo(KEYWORD_SYNCHRONIZED);
        NFA kw_thisNFA = NFA.acceptsThisWord(alphabetSize, "this")
                .setAllFinalStatesTo(KEYWORD_THIS);
        NFA kw_throwNFA = NFA.acceptsThisWord(alphabetSize, "throw")
                .setAllFinalStatesTo(KEYWORD_THROW);
        NFA kw_throwsNFA = NFA.acceptsThisWord(alphabetSize, "throws")
                .setAllFinalStatesTo(KEYWORD_THROWS);
        NFA kw_transientNFA = NFA.acceptsThisWord(alphabetSize, "transient")
                .setAllFinalStatesTo(KEYWORD_TRANSIENT);
        NFA kw_tryNFA = NFA.acceptsThisWord(alphabetSize, "try")
                .setAllFinalStatesTo(KEYWORD_TRY);
        NFA kw_voidNFA = NFA.acceptsThisWord(alphabetSize, "void")
                .setAllFinalStatesTo(KEYWORD_VOID);
        NFA kw_volatileNFA = NFA.acceptsThisWord(alphabetSize, "volatile")
                .setAllFinalStatesTo(KEYWORD_VOLATILE);
        NFA kw_whileNFA = NFA.acceptsThisWord(alphabetSize, "while")
                .setAllFinalStatesTo(KEYWORD_WHILE);

        NFA lit_trueNFA = NFA.acceptsThisWord(alphabetSize, "true")
                .setAllFinalStatesTo(TRUE);
        NFA lit_falseNFA = NFA.acceptsThisWord(alphabetSize, "false")
                .setAllFinalStatesTo(FALSE);
        NFA lit_nullNFA = NFA.acceptsThisWord(alphabetSize, "null")
                .setAllFinalStatesTo(NULL);

        Set<Integer> javaLetters = new HashSet<>();
        Set<Integer> javaLettersOrDigits = new HashSet<>();
        for (int i = 0; i < alphabetSize; i++) {
            if (Character.isJavaIdentifierStart(i)) {
                javaLetters.add(i);
            }
            if (Character.isJavaIdentifierPart(i)) {
                javaLettersOrDigits.add(i);
            }
        }
        NFA idenStartNFA = NFA.acceptsAllTheseCodePoints(alphabetSize, javaLetters);
        NFA idenPartNFA = NFA.acceptsAllTheseCodePoints(alphabetSize, javaLettersOrDigits);
        NFA identifierNFA = idenStartNFA.concatenation(idenPartNFA.iteration())
                .setAllFinalStatesTo(IDENTIFIER);

        NFA underscoreNFA = NFA.singleLetterLanguage(alphabetSize, "_");
        NFA underscoresNFA = underscoreNFA.positiveIteration();
        NFA zeroNFA = NFA.singleLetterLanguage(alphabetSize, "0");
        NFA nonZeroNFA = NFA.acceptsThisRange(alphabetSize, "1", "9");
        NFA digitNFA = zeroNFA.union(nonZeroNFA);
        NFA digitOrUnderscoreNFA = digitNFA.union(underscoreNFA);
        NFA digitsAndUnderscoresNFA = digitOrUnderscoreNFA.positiveIteration();
        NFA digitsNFA = digitNFA.concatenation(
                digitsAndUnderscoresNFA.optional().concatenation(digitNFA).optional());

        NFA decimalNumeralNFA = zeroNFA.union(nonZeroNFA.concatenation(digitsNFA.optional()))
                .union(nonZeroNFA.concatenation(underscoresNFA).concatenation(digitNFA));

        NFA integerTypeSuffixNFA = NFA.acceptsAllTheseSymbols(alphabetSize, Set.of("l", "L"));

        NFA decimalIntegerLiteralNFA = decimalNumeralNFA.concatenation(integerTypeSuffixNFA.optional());

        NFA integerLiteralNFA = decimalIntegerLiteralNFA
                .setAllFinalStatesTo(INTEGER_LITERAL); // for now

        NFA lparenNFA = NFA.singleLetterLanguage(alphabetSize, "(")
                .setAllFinalStatesTo(LPAREN);
        NFA rparenNFA = NFA.singleLetterLanguage(alphabetSize, ")")
                .setAllFinalStatesTo(RPAREN);

        NFA lbraceNFA = NFA.singleLetterLanguage(alphabetSize, "{")
                .setAllFinalStatesTo(LBRACE);
        NFA rbraceNFA = NFA.singleLetterLanguage(alphabetSize, "}")
                .setAllFinalStatesTo(RBRACE);

        NFA lsq_bracketNFA = NFA.singleLetterLanguage(alphabetSize, "[")
                .setAllFinalStatesTo(LSQ_BRACKET);
        NFA rsq_bracketNFA = NFA.singleLetterLanguage(alphabetSize, "]")
                .setAllFinalStatesTo(RSQ_BRACKET);

        NFA semicolonNFA = NFA.singleLetterLanguage(alphabetSize, ";")
                .setAllFinalStatesTo(SEMICOLON);
        NFA commaNFA = NFA.singleLetterLanguage(alphabetSize, ",")
                .setAllFinalStatesTo(COMMA);
        NFA dotNFA = NFA.singleLetterLanguage(alphabetSize, ".")
                .setAllFinalStatesTo(DOT);

        NFA ellipsisNFA = NFA.acceptsThisWord(alphabetSize, "...")
                .setAllFinalStatesTo(ELLIPSIS);
        NFA atNFA = NFA.singleLetterLanguage(alphabetSize, "@")
                .setAllFinalStatesTo(AT);
        NFA doubleColonNFA = NFA.acceptsThisWord(alphabetSize, "::")
                .setAllFinalStatesTo(DOUBLE_COLON);

        NFA operatorsNFA = NFA.emptyLanguage(alphabetSize)
                .union(NFA.acceptsThisWord(alphabetSize, "=").setAllFinalStatesTo(OperatorsTags.ASSIGNMENT))
                .union(NFA.acceptsThisWord(alphabetSize, ">").setAllFinalStatesTo(OperatorsTags.GREATER))
                .union(NFA.acceptsThisWord(alphabetSize, "<").setAllFinalStatesTo(OperatorsTags.LESS))
                .union(NFA.acceptsThisWord(alphabetSize, "!").setAllFinalStatesTo(OperatorsTags.NOT))
                .union(NFA.acceptsThisWord(alphabetSize, "~").setAllFinalStatesTo(OperatorsTags.COMPLEMENT))
                .union(NFA.acceptsThisWord(alphabetSize, "?").setAllFinalStatesTo(OperatorsTags.QUESTION_MARK))
                .union(NFA.acceptsThisWord(alphabetSize, ":").setAllFinalStatesTo(OperatorsTags.COLON))
                .union(NFA.acceptsThisWord(alphabetSize, "->").setAllFinalStatesTo(OperatorsTags.ARROW))
                .union(NFA.acceptsThisWord(alphabetSize, "==").setAllFinalStatesTo(OperatorsTags.EQUALS))
                .union(NFA.acceptsThisWord(alphabetSize, ">=").setAllFinalStatesTo(OperatorsTags.GREQ))
                .union(NFA.acceptsThisWord(alphabetSize, "<=").setAllFinalStatesTo(OperatorsTags.LEQ))
                .union(NFA.acceptsThisWord(alphabetSize, "!=").setAllFinalStatesTo(OperatorsTags.NEQ))
                .union(NFA.acceptsThisWord(alphabetSize, "&&").setAllFinalStatesTo(OperatorsTags.AND))
                .union(NFA.acceptsThisWord(alphabetSize, "||").setAllFinalStatesTo(OperatorsTags.OR))
                .union(NFA.acceptsThisWord(alphabetSize, "++").setAllFinalStatesTo(OperatorsTags.INC))
                .union(NFA.acceptsThisWord(alphabetSize, "--").setAllFinalStatesTo(OperatorsTags.DEC))
                .union(NFA.acceptsThisWord(alphabetSize, "+").setAllFinalStatesTo(OperatorsTags.PLUS))
                .union(NFA.acceptsThisWord(alphabetSize, "-").setAllFinalStatesTo(OperatorsTags.MINUS))
                .union(NFA.acceptsThisWord(alphabetSize, "*").setAllFinalStatesTo(OperatorsTags.MUL))
                .union(NFA.acceptsThisWord(alphabetSize, "/").setAllFinalStatesTo(OperatorsTags.DIV))
                .union(NFA.acceptsThisWord(alphabetSize, "&").setAllFinalStatesTo(OperatorsTags.BW_AND))
                .union(NFA.acceptsThisWord(alphabetSize, "|").setAllFinalStatesTo(OperatorsTags.BW_OR))
                .union(NFA.acceptsThisWord(alphabetSize, "^").setAllFinalStatesTo(OperatorsTags.BW_XOR))
                .union(NFA.acceptsThisWord(alphabetSize, "%").setAllFinalStatesTo(OperatorsTags.MOD))
                .union(NFA.acceptsThisWord(alphabetSize, "<<").setAllFinalStatesTo(OperatorsTags.LSHIFT))
                .union(NFA.acceptsThisWord(alphabetSize, ">>").setAllFinalStatesTo(OperatorsTags.RSHIFT))
                .union(NFA.acceptsThisWord(alphabetSize, ">>>").setAllFinalStatesTo(OperatorsTags.LOG_RSHIFT))
                .union(NFA.acceptsThisWord(alphabetSize, "+=").setAllFinalStatesTo(OperatorsTags.PLUS_EQ))
                .union(NFA.acceptsThisWord(alphabetSize, "-=").setAllFinalStatesTo(OperatorsTags.MINUS_EQ))
                .union(NFA.acceptsThisWord(alphabetSize, "*=").setAllFinalStatesTo(OperatorsTags.MUL_EQ))
                .union(NFA.acceptsThisWord(alphabetSize, "/=").setAllFinalStatesTo(OperatorsTags.DIV_EQ))
                .union(NFA.acceptsThisWord(alphabetSize, "&=").setAllFinalStatesTo(OperatorsTags.BW_AND_EQ))
                .union(NFA.acceptsThisWord(alphabetSize, "|=").setAllFinalStatesTo(OperatorsTags.BW_OR_EQ))
                .union(NFA.acceptsThisWord(alphabetSize, "^=").setAllFinalStatesTo(OperatorsTags.BQ_XOR_EQ))
                .union(NFA.acceptsThisWord(alphabetSize, "%=").setAllFinalStatesTo(OperatorsTags.MOD_EQ))
                .union(NFA.acceptsThisWord(alphabetSize, "<<=").setAllFinalStatesTo(OperatorsTags.LSHIFT_EQ))
                .union(NFA.acceptsThisWord(alphabetSize, ">>=").setAllFinalStatesTo(OperatorsTags.RSHIFT_EQ))
                .union(NFA.acceptsThisWord(alphabetSize, ">>>=").setAllFinalStatesTo(OperatorsTags.LOG_RSHIFT_EQ));

        NFA mlcStartNFA = NFA.acceptsThisWord(alphabetSize, "/*")
                .setAllFinalStatesTo(MLC_START);

        NFA stringLiteralStartNFA = NFA.acceptsThisWord(alphabetSize, "\"")
                .setAllFinalStatesTo(HelperTags.STRING_LITERAL_START);

        NFA slcStartNFA = NFA.acceptsThisWord(alphabetSize, "//")
                .setAllFinalStatesTo(HelperTags.SLC_START);

        List<StateTag> priorityList = new ArrayList<>(
                List.of(
                        WHITESPACE,
                        HelperTags.STRING_LITERAL_START,
                        HelperTags.MLC_ASTERISK,
                        HelperTags.MLC_NO_ASTERISK_SEQ,
                        COMMENT,
                        HelperTags.SLC_START,
                        MLC_START,
                        STRING_LITERAL,
                        ELLIPSIS,
                        AT,
                        DOUBLE_COLON,
                        LPAREN,
                        RPAREN,
                        LBRACE,
                        RBRACE,
                        LSQ_BRACKET,
                        RSQ_BRACKET,
                        SEMICOLON,
                        COMMA,
                        DOT,
                        IDENTIFIER,
                        INTEGER_LITERAL,
                        NULL,
                        FALSE,
                        TRUE,
                        KEYWORD_ABSTRACT,
                        KEYWORD_ASSERT,
                        KEYWORD_BOOLEAN,
                        KEYWORD_BREAK,
                        KEYWORD_BYTE,
                        KEYWORD_CASE,
                        KEYWORD_CATCH,
                        KEYWORD_CHAR,
                        KEYWORD_CLASS,
                        KEYWORD_CONST,
                        KEYWORD_CONTINUE,
                        KEYWORD_DEFAULT,
                        KEYWORD_DO,
                        KEYWORD_DOUBLE,
                        KEYWORD_ELSE,
                        KEYWORD_ENUM,
                        KEYWORD_EXTENDS,
                        KEYWORD_FINAL,
                        KEYWORD_FINALLY,
                        KEYWORD_FLOAT,
                        KEYWORD_FOR,
                        KEYWORD_IF,
                        KEYWORD_GOTO,
                        KEYWORD_IMPLEMENTS,
                        KEYWORD_IMPORT,
                        KEYWORD_INSTANCEOF,
                        KEYWORD_INT,
                        KEYWORD_INTERFACE,
                        KEYWORD_LONG,
                        KEYWORD_NATIVE,
                        KEYWORD_NEW,
                        KEYWORD_PACKAGE,
                        KEYWORD_PRIVATE,
                        KEYWORD_PROTECTED,
                        KEYWORD_PUBLIC,
                        KEYWORD_RETURN,
                        KEYWORD_SHORT,
                        KEYWORD_STATIC,
                        KEYWORD_STRICTFP,
                        KEYWORD_SUPER,
                        KEYWORD_SWITCH,
                        KEYWORD_SYNCHRONIZED,
                        KEYWORD_THIS,
                        KEYWORD_THROW,
                        KEYWORD_THROWS,
                        KEYWORD_TRANSIENT,
                        KEYWORD_TRY,
                        KEYWORD_VOID,
                        KEYWORD_VOLATILE,
                        KEYWORD_WHILE
                )
        );

        priorityList.addAll(Arrays.asList(OperatorsTags.values()));

        Map<StateTag, Integer> priorityMap = new HashMap<>();
        for (int i = 0; i < priorityList.size(); i++) {
            priorityMap.put(priorityList.get(i), i);
        }

        NFA mode0 = whitespaceNFA
                .union(kw_abstractNFA)
                .union(kw_assertNFA)
                .union(kw_booleanNFA)
                .union(kw_breakNFA)
                .union(kw_byteNFA)
                .union(kw_caseNFA)
                .union(kw_catchNFA)
                .union(kw_charNFA)
                .union(kw_classNFA)
                .union(kw_constNFA)
                .union(kw_continueNFA)
                .union(kw_defaultNFA)
                .union(kw_doNFA)
                .union(kw_doubleNFA)
                .union(kw_elseNFA)
                .union(kw_enumNFA)
                .union(kw_extendsNFA)
                .union(kw_finalNFA)
                .union(kw_finallyNFA)
                .union(kw_floatNFA)
                .union(kw_forNFA)
                .union(kw_ifNFA)
                .union(kw_gotoNFA)
                .union(kw_implementsNFA)
                .union(kw_importNFA)
                .union(kw_instanceofNFA)
                .union(kw_intNFA)
                .union(kw_interfaceNFA)
                .union(kw_longNFA)
                .union(kw_nativeNFA)
                .union(kw_newNFA)
                .union(kw_packageNFA)
                .union(kw_privateNFA)
                .union(kw_protectedNFA)
                .union(kw_publicNFA)
                .union(kw_returnNFA)
                .union(kw_shortNFA)
                .union(kw_staticNFA)
                .union(kw_strictfpNFA)
                .union(kw_superNFA)
                .union(kw_switchNFA)
                .union(kw_synchronizedNFA)
                .union(kw_thisNFA)
                .union(kw_throwNFA)
                .union(kw_throwsNFA)
                .union(kw_transientNFA)
                .union(kw_tryNFA)
                .union(kw_voidNFA)
                .union(kw_volatileNFA)
                .union(kw_whileNFA)
                .union(lit_nullNFA)
                .union(lit_falseNFA)
                .union(lit_trueNFA)
                .union(identifierNFA)
                .union(integerLiteralNFA)
                .union(lparenNFA)
                .union(rparenNFA)
                .union(lbraceNFA)
                .union(rbraceNFA)
                .union(lsq_bracketNFA)
                .union(rsq_bracketNFA)
                .union(semicolonNFA)
                .union(commaNFA)
                .union(dotNFA)
                .union(ellipsisNFA)
                .union(atNFA)
                .union(doubleColonNFA)
                .union(operatorsNFA)
                .union(mlcStartNFA)
                .union(stringLiteralStartNFA)
                .union(slcStartNFA);

        NFA mlcEndNFA = NFA.acceptsThisWord(alphabetSize, "*/")
                .setAllFinalStatesTo(COMMENT);
        NFA mlcNoAsteriskSeq = NFA.acceptsAllSymbolsButThese(alphabetSize, Set.of("*")).iteration()
                .setAllFinalStatesTo(HelperTags.MLC_NO_ASTERISK_SEQ);
        NFA mlcAsterisk = NFA.acceptsAllTheseSymbols(alphabetSize, Set.of("*"))
                .setAllFinalStatesTo(HelperTags.MLC_ASTERISK);

        NFA mlcModeNFA = mlcEndNFA
                .union(mlcNoAsteriskSeq)
                .union(mlcAsterisk);

        NFA escapeSequenceNFA = NFA.acceptsAllTheseWords(
                alphabetSize,
                Set.of(
                        "\\b", "\\t", "\\n", "\\f", "\\r", "\\\"", "\\'", "\\\\"
                )
        );

        NFA stringCharNFA = escapeSequenceNFA
                .union(NFA.acceptsAllSymbolsButThese(alphabetSize, Set.of("\\", "\"", "\n")))
                .setAllFinalStatesTo(HelperTags.STRING_LITERAL_ELEM);
        NFA stringLiteralNFA = NFA.acceptsAllTheseSymbols(alphabetSize, Set.of("\""))
                .setAllFinalStatesTo(STRING_LITERAL);

        NFA stringLiteralModeNFA = stringCharNFA.union(stringLiteralNFA);

        NFA slcModeNFA = NFA.acceptsAllSymbolsButThese(alphabetSize, Set.of("\n")).iteration()
                .concatenation(NFA.singleLetterLanguage(alphabetSize, "\n"))
                .setAllFinalStatesTo(COMMENT);
        // this will work because in both SLC and MLC we reset mode to 9

        System.out.println("Mode 0");
        LexicalRecognizer m0 = buildRecognizer(mode0, priorityMap);
        System.out.println();
        System.out.println("Mode MLC");
        LexicalRecognizer mlcMode = buildRecognizer(mlcModeNFA, priorityMap);
        System.out.println();
        System.out.println("Mode StringLiteralMode");
        LexicalRecognizer stringLiteralMode = buildRecognizer(stringLiteralModeNFA, priorityMap);
        System.out.println();
        System.out.println("Mode SLC");
        LexicalRecognizer slcMode = buildRecognizer(slcModeNFA, priorityMap);
        System.out.println();

        List<LexicalRecognizer> recognizers = List.of(m0, mlcMode, stringLiteralMode, slcMode);

        Map<StateTag, Integer> modeSwitches = Map.of(
                MLC_START, 1,
                COMMENT, 0,
                STRING_LITERAL_START, 2,
                STRING_LITERAL, 0,
                SLC_START, 3
        );

        String text = Utility.getText("MJTest2.txt");

        ProCompiler compiler = new ProCompiler(recognizers);
        ProScanner scanner = compiler.getScanner(text, modeSwitches);

        Set<Domain> ignoredTokenTypes = Set.of(
                SimpleDomains.WHITESPACE,
                Domain.END_OF_INPUT,
                Domain.ERROR
        );

        int errCount = 0;

        Token t = scanner.nextToken();
        while (t.getTag() != Domain.END_OF_INPUT) {
            if (!ignoredTokenTypes.contains(t.getTag())) {
                System.out.println(t);
            }
            if (t.getTag() == Domain.ERROR) {
                errCount++;
                System.out.println(t.getCoords());
            }
            t = scanner.nextToken();
        }

        System.out.println();
        System.out.println("Errors: " + errCount);
        System.out.println("Compiler messages: ");
        SortedMap<Position, Message> messages = compiler.getSortedMessages();
        for (Map.Entry<Position, Message> entry : messages.entrySet()) {
            System.out.println(entry.getValue() + " at " + entry.getKey());
        }
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
        System.out.println(NEWLINE + factorization + NEWLINE);

        return recognizer;
    }
}
