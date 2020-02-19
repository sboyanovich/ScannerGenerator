package io.github.sboyanovich.scannergenerator.tests.mockjava;

import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.automata.NFAStateGraphBuilder;
import io.github.sboyanovich.scannergenerator.scanner.Compiler;
import io.github.sboyanovich.scannergenerator.scanner.*;
import io.github.sboyanovich.scannergenerator.scanner.Scanner;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.tests.mockjava.data.domains.SimpleDomains;
import io.github.sboyanovich.scannergenerator.utility.EquivalenceMap;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.*;

import static io.github.sboyanovich.scannergenerator.tests.mockjava.data.states.StateTags.*;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class MockJavaTest {
    public static void main(String[] args) {
        int alphabetSize = Character.MAX_CODE_POINT + 1;
        //alphabetSize = 256;

        NFA spaceNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint(" "));
        NFA tabNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("\t"));
        NFA newlineNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("\n"));
        NFA carretNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("\r"));

        NFA whitespaceNFA = spaceNFA
                .union(tabNFA)
                .union(carretNFA.concatenation(newlineNFA))
                .union(newlineNFA)
                .positiveIteration()
                .setAllFinalStatesTo(WHITESPACE);

        NFA kw_abstractNFA = Utility.acceptThisWord(alphabetSize, "abstract")
                .setAllFinalStatesTo(KEYWORD_ABSTRACT);
        NFA kw_assertNFA = Utility.acceptThisWord(alphabetSize, "assert")
                .setAllFinalStatesTo(KEYWORD_ASSERT);
        NFA kw_booleanNFA = Utility.acceptThisWord(alphabetSize, "boolean")
                .setAllFinalStatesTo(KEYWORD_BOOLEAN);
        NFA kw_breakNFA = Utility.acceptThisWord(alphabetSize, "break")
                .setAllFinalStatesTo(KEYWORD_BREAK);
        NFA kw_byteNFA = Utility.acceptThisWord(alphabetSize, "byte")
                .setAllFinalStatesTo(KEYWORD_BYTE);
        NFA kw_caseNFA = Utility.acceptThisWord(alphabetSize, "case")
                .setAllFinalStatesTo(KEYWORD_CASE);
        NFA kw_catchNFA = Utility.acceptThisWord(alphabetSize, "catch")
                .setAllFinalStatesTo(KEYWORD_CATCH);
        NFA kw_charNFA = Utility.acceptThisWord(alphabetSize, "char")
                .setAllFinalStatesTo(KEYWORD_CHAR);
        NFA kw_classNFA = Utility.acceptThisWord(alphabetSize, "class")
                .setAllFinalStatesTo(KEYWORD_CLASS);
        NFA kw_constNFA = Utility.acceptThisWord(alphabetSize, "const")
                .setAllFinalStatesTo(KEYWORD_CONST);
        NFA kw_continueNFA = Utility.acceptThisWord(alphabetSize, "continue")
                .setAllFinalStatesTo(KEYWORD_CONTINUE);
        NFA kw_defaultNFA = Utility.acceptThisWord(alphabetSize, "default")
                .setAllFinalStatesTo(KEYWORD_DEFAULT);
        NFA kw_doNFA = Utility.acceptThisWord(alphabetSize, "do")
                .setAllFinalStatesTo(KEYWORD_DO);
        NFA kw_doubleNFA = Utility.acceptThisWord(alphabetSize, "double")
                .setAllFinalStatesTo(KEYWORD_DOUBLE);
        NFA kw_elseNFA = Utility.acceptThisWord(alphabetSize, "else")
                .setAllFinalStatesTo(KEYWORD_ELSE);
        NFA kw_enumNFA = Utility.acceptThisWord(alphabetSize, "enum")
                .setAllFinalStatesTo(KEYWORD_ENUM);
        NFA kw_extendsNFA = Utility.acceptThisWord(alphabetSize, "extends")
                .setAllFinalStatesTo(KEYWORD_EXTENDS);
        NFA kw_finalNFA = Utility.acceptThisWord(alphabetSize, "final")
                .setAllFinalStatesTo(KEYWORD_FINAL);
        NFA kw_finallyNFA = Utility.acceptThisWord(alphabetSize, "finally")
                .setAllFinalStatesTo(KEYWORD_FINALLY);
        NFA kw_floatNFA = Utility.acceptThisWord(alphabetSize, "float")
                .setAllFinalStatesTo(KEYWORD_FLOAT);
        NFA kw_forNFA = Utility.acceptThisWord(alphabetSize, "for")
                .setAllFinalStatesTo(KEYWORD_FOR);
        NFA kw_ifNFA = Utility.acceptThisWord(alphabetSize, "if")
                .setAllFinalStatesTo(KEYWORD_IF);
        NFA kw_gotoNFA = Utility.acceptThisWord(alphabetSize, "goto")
                .setAllFinalStatesTo(KEYWORD_GOTO);
        NFA kw_implementsNFA = Utility.acceptThisWord(alphabetSize, "implements")
                .setAllFinalStatesTo(KEYWORD_IMPLEMENTS);
        NFA kw_importNFA = Utility.acceptThisWord(alphabetSize, "import")
                .setAllFinalStatesTo(KEYWORD_IMPORT);
        NFA kw_instanceofNFA = Utility.acceptThisWord(alphabetSize, "instanceof")
                .setAllFinalStatesTo(KEYWORD_INSTANCEOF);
        NFA kw_intNFA = Utility.acceptThisWord(alphabetSize, "int")
                .setAllFinalStatesTo(KEYWORD_INT);
        NFA kw_interfaceNFA = Utility.acceptThisWord(alphabetSize, "interface")
                .setAllFinalStatesTo(KEYWORD_INTERFACE);
        NFA kw_longNFA = Utility.acceptThisWord(alphabetSize, "long")
                .setAllFinalStatesTo(KEYWORD_LONG);
        NFA kw_nativeNFA = Utility.acceptThisWord(alphabetSize, "native")
                .setAllFinalStatesTo(KEYWORD_NATIVE);
        NFA kw_newNFA = Utility.acceptThisWord(alphabetSize, "new")
                .setAllFinalStatesTo(KEYWORD_NEW);
        NFA kw_packageNFA = Utility.acceptThisWord(alphabetSize, "package")
                .setAllFinalStatesTo(KEYWORD_PACKAGE);
        NFA kw_privateNFA = Utility.acceptThisWord(alphabetSize, "private")
                .setAllFinalStatesTo(KEYWORD_PRIVATE);
        NFA kw_protectedNFA = Utility.acceptThisWord(alphabetSize, "protected")
                .setAllFinalStatesTo(KEYWORD_PROTECTED);
        NFA kw_publicNFA = Utility.acceptThisWord(alphabetSize, "public")
                .setAllFinalStatesTo(KEYWORD_PUBLIC);
        NFA kw_returnNFA = Utility.acceptThisWord(alphabetSize, "return")
                .setAllFinalStatesTo(KEYWORD_RETURN);
        NFA kw_shortNFA = Utility.acceptThisWord(alphabetSize, "short")
                .setAllFinalStatesTo(KEYWORD_SHORT);
        NFA kw_staticNFA = Utility.acceptThisWord(alphabetSize, "static")
                .setAllFinalStatesTo(KEYWORD_STATIC);
        NFA kw_strictfpNFA = Utility.acceptThisWord(alphabetSize, "strictfp")
                .setAllFinalStatesTo(KEYWORD_STRICTFP);
        NFA kw_superNFA = Utility.acceptThisWord(alphabetSize, "super")
                .setAllFinalStatesTo(KEYWORD_SUPER);
        NFA kw_switchNFA = Utility.acceptThisWord(alphabetSize, "switch")
                .setAllFinalStatesTo(KEYWORD_SWITCH);
        NFA kw_synchronizedNFA = Utility.acceptThisWord(alphabetSize, "synchronized")
                .setAllFinalStatesTo(KEYWORD_SYNCHRONIZED);
        NFA kw_thisNFA = Utility.acceptThisWord(alphabetSize, "this")
                .setAllFinalStatesTo(KEYWORD_THIS);
        NFA kw_throwNFA = Utility.acceptThisWord(alphabetSize, "throw")
                .setAllFinalStatesTo(KEYWORD_THROW);
        NFA kw_throwsNFA = Utility.acceptThisWord(alphabetSize, "throws")
                .setAllFinalStatesTo(KEYWORD_THROWS);
        NFA kw_transientNFA = Utility.acceptThisWord(alphabetSize, "transient")
                .setAllFinalStatesTo(KEYWORD_TRANSIENT);
        NFA kw_tryNFA = Utility.acceptThisWord(alphabetSize, "try")
                .setAllFinalStatesTo(KEYWORD_TRY);
        NFA kw_voidNFA = Utility.acceptThisWord(alphabetSize, "void")
                .setAllFinalStatesTo(KEYWORD_VOID);
        NFA kw_volatileNFA = Utility.acceptThisWord(alphabetSize, "volatile")
                .setAllFinalStatesTo(KEYWORD_VOLATILE);
        NFA kw_whileNFA = Utility.acceptThisWord(alphabetSize, "while")
                .setAllFinalStatesTo(KEYWORD_WHILE);

        NFA lit_trueNFA = Utility.acceptThisWord(alphabetSize, "true")
                .setAllFinalStatesTo(TRUE);
        NFA lit_falseNFA = Utility.acceptThisWord(alphabetSize, "false")
                .setAllFinalStatesTo(FALSE);
        NFA lit_nullNFA = Utility.acceptThisWord(alphabetSize, "null")
                .setAllFinalStatesTo(NULL);

        NFAStateGraphBuilder commentNFAEdges = new NFAStateGraphBuilder(6, alphabetSize);
        addEdge(commentNFAEdges, 0, 1, Set.of("/"));
        addEdge(commentNFAEdges, 1, 2, Set.of("*"));
        addEdge(commentNFAEdges, 2, 4, Set.of("*"));
        addEdgeSubtractive(commentNFAEdges, 2, 3, Set.of("*"));
        addEdgeSubtractive(commentNFAEdges, 3, 3, Set.of("*"));
        addEdge(commentNFAEdges, 3, 4, Set.of("*"));
        addEdgeSubtractive(commentNFAEdges, 4, 3, Set.of("*", "/"));
        addEdge(commentNFAEdges, 4, 4, Set.of("*"));
        addEdge(commentNFAEdges, 4, 5, Set.of("/"));

        NFA commentNFA = new NFA(6, alphabetSize, 0, Map.of(5, COMMENT), commentNFAEdges.build());

        List<StateTag> priorityList = List.of(
                WHITESPACE,
                COMMENT,
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
        );

        Map<StateTag, Integer> priorityMap = new HashMap<>();
        for (int i = 0; i < priorityList.size(); i++) {
            priorityMap.put(priorityList.get(i), i);
        }

        NFA lang = whitespaceNFA
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
                .union(commentNFA)
                .union(lit_nullNFA)
                .union(lit_falseNFA)
                .union(lit_trueNFA);

        // TODO: OUT OF MEMORY!!!

        System.out.println(lang.getNumberOfStates());

        lang = lang.removeLambdaSteps();
        System.out.println("Lambda steps removed.");

        // EXPERIMENTAL
        List<Integer> mentioned = mentioned(lang);
        System.out.println(mentioned.size() + " mentioned symbols");
        EquivalenceMap hint = Utility.getCoarseSymbolClassMapExp(mentioned, alphabetSize);

        System.out.println(hint.getDomain() + " -> " + hint.getEqClassDomain());

        DFA dfa = lang.determinizeExp(priorityMap, hint);

        System.out.println("Determinized!");
        // Determinization needs to be optimized!
        System.out.println(dfa.getNumberOfStates());

        LexicalRecognizer recognizer = new LexicalRecognizer(hint, dfa);
        System.out.println("Recognizer built!");
        System.out.println("States: " + recognizer.getNumberOfStates());
        System.out.println("Classes: " + recognizer.getNumberOfColumns());

        String dot = recognizer.toGraphvizDotString(Object::toString, true);
        System.out.println(dot);
        String factorization = recognizer.displayEquivalenceMap(Utility::defaultUnicodeInterpretation);
        System.out.println(NEWLINE + factorization + NEWLINE);

        String text = Utility.getText("MJTest.txt");

        Compiler compiler = new Compiler(recognizer);
        Scanner scanner = compiler.getScanner(text);

        Set<Domain> ignoredTokenTypes = Set.of(
                SimpleDomains.WHITESPACE,
                Domain.END_OF_PROGRAM,
                Domain.ERROR
        );

        int errCount = 0;

        Token t = scanner.nextToken();
        while (t.getTag() != Domain.END_OF_PROGRAM) {
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
}
