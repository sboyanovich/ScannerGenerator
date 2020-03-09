package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.automata.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.LexicalRecognizer;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class RecognizerGenTest {
    public static void main(String[] args) {
        String appName = "xgen";
        if (args.length < 1) {
            System.err.println("USAGE: " + appName + " <input_file>");
            System.exit(1);
        }

        String inputFile = "res/testInputM1.txt";

        String text = Utility.getText(inputFile);

        int maxCodePoint = Character.MAX_CODE_POINT;
        int aeoi = maxCodePoint + 1;
        int alphabetSize = maxCodePoint + 1 + 1;

        MyScanner scanner = new MyScanner(text);
        MockCompiler compiler = scanner.getCompiler();

/*
        Set<Domain> ignoredTokenTypes = Set.of(
                Domain.END_OF_INPUT,
                Domain.ERROR
        );

        List<Token> allTokens = new ArrayList<>();

        int errCount = 0;

        Token t = scanner.nextToken();
        allTokens.add(t);
        while (t.getTag() != Domain.END_OF_INPUT) {
            if (!ignoredTokenTypes.contains(t.getTag())) {
                System.out.println(t);
            }
            if (t.getTag() == Domain.ERROR) {
                errCount++;
                System.out.println(t.getCoords());
            }
            t = scanner.nextToken();
            allTokens.add(t);
        }

        System.out.println();
        System.out.println("Errors: " + errCount);
        System.out.println("Compiler messages: ");
        SortedMap<Position, Message> messages = compiler.getSortedMessages();
        for (Map.Entry<Position, Message> entry : messages.entrySet()) {
            System.out.println(entry.getValue() + " at " + entry.getKey());
        }
*/

        if (true) {
            AST ast = Parser.parse(scanner);
            /*String dotAST = ast.toGraphVizDotString();
            System.out.println();
            System.out.println(dotAST);*/
            AST.Spec spec = (AST.Spec) ast;

            Map<String, NFA> definitions = new HashMap<>();
            Map<String, Set<Integer>> defPivots = new HashMap<>();

            Instant start, end;
            long timeElapsed = 0;

            for (AST.Definitions.Def def : spec.definitions.definitions) {
                String name = def.identifier.identifier;
                start = Instant.now();
                NFA auto = buildNFAFromRegex(def.regex, definitions, alphabetSize);
                end = Instant.now();
                timeElapsed += Duration.between(start, end).toMillis();
                definitions.put(name, auto);
                defPivots.put(name, def.regex.getPivots(defPivots, alphabetSize));
            }

/*            System.out.println();
            for(String key : definitions.keySet()) {
                NFA auto = definitions.get(key);
                System.out.println(key + ": ");
                String dot = auto.toGraphvizDotString(Utility::defaultUnicodeInterpretation, true);
                System.out.println(dot);
                System.out.println();
            }*/

            Map<String, NFA> modeNFAs = new HashMap<>();
            List<StateTag> priorityList = new ArrayList<>();
            Map<String, Set<Integer>> rulePivots = new HashMap<>();
            Map<String, Set<Integer>> modePivots = new HashMap<>();

            List<AST.Rules.Rule> rules = spec.rules.rules;
            for (AST.Rules.Rule rule : rules) {
                String stateName = rule.stateName;
                start = Instant.now();
                NFA nfa = buildNFAFromRegex(rule.regex, definitions, alphabetSize);
                end = Instant.now();
                timeElapsed += Duration.between(start, end).toMillis();

                rulePivots.put(stateName, rule.regex.getPivots(defPivots, alphabetSize));

                StateTag stateTag = new StateTag() {
                    String name = stateName;

                    @Override
                    public String toString() {
                        return name;
                    }
                };

                nfa = nfa.setAllFinalStatesTo(stateTag);

                priorityList.add(stateTag);

                List<AST.Identifier> modeNames = rule.modeList.modeNames;
                for (var mode : modeNames) {
                    String modeName = mode.identifier;
                    if (modeNFAs.containsKey(modeName)) {
                        NFA val = modeNFAs.get(modeName);
                        modeNFAs.put(modeName, val.union(nfa));
                        modePivots.get(modeName).addAll(rulePivots.get(stateName));
                    } else {
                        modeNFAs.put(modeName, nfa);
                        modePivots.put(modeName, new HashSet<>(rulePivots.get(stateName)));
                    }
                }
            }

            Map<StateTag, Integer> priorityMap = new HashMap<>();

            for (int i = 0; i < priorityList.size(); i++) {
                priorityMap.put(priorityList.get(i), priorityList.size() - (i + 1));
            }

            Map<String, LexicalRecognizer> modes = new HashMap<>();

            for (String modeName : modeNFAs.keySet()) {
                NFA nfa = modeNFAs.get(modeName);
                modes.put(modeName, buildRecognizer(nfa, priorityMap, modePivots.get(modeName)));
            }

            /// PARAMS
            String recognizersDirName = "recognizers";
            String prefix = "generated/";
            String packageName = "io.github.sboyanovich.scannergenerator.generated";
            String stateTagsEnumName = "StateTags";
            String simpleDomainsEnumName = "SimpleDomains";
            String scannerClassName = "GeneratedScanner";

            for (String modeName : modes.keySet()) {
                LexicalRecognizer recognizer = modes.get(modeName);
                /*String dot = recognizer.toGraphvizDotString(
                        Objects::toString, true
                );
                System.out.println();
                System.out.println(modeName + ": ");
                System.out.println(dot);
                System.out.println();*/
                recognizer.writeToFile(
                        prefix + recognizersDirName + "/" + modeName + ".reco", priorityMap
                );
            }

            System.out.println();
            Collections.reverse(priorityList);
/*
            StringBuilder finalTags = new StringBuilder();
            finalTags.append("List<StateTag> finalTags = new ArrayList<>();\n");
            for (int i = 0; i < priorityList.size(); i++) {
                StateTag tag = priorityList.get(i);
                finalTags.append("finalTags.add(").append(tag).append(");\n");
            }
            System.out.println(finalTags.toString());
*/
            List<String> stateNames = priorityList.stream().map(Objects::toString).collect(Collectors.toList());

            String stateTagsEnum = Utility.generateStateTagsEnum(stateNames, packageName);

            Utility.writeTextToFile(stateTagsEnum, prefix + stateTagsEnumName + ".java");

            List<AST.DomainGroup> domainGroupList = spec.domainGroups.domainGroups;

            Map<String, String> domainEnums = new HashMap<>();

            for (AST.DomainGroup domainGroup : domainGroupList) {
                List<String> domainNames = domainGroup.getDomainNames();
                String enumName = "";
                if (domainGroup instanceof AST.DomainGroup.SimpleDomainGroup) {
                    enumName = simpleDomainsEnumName;
                    String simpleDomainsEnum = Utility.generateSimpleDomainsEnum(
                            domainNames,
                            packageName,
                            enumName
                    );
                    Utility.writeTextToFile(simpleDomainsEnum, prefix + enumName + ".java");
                } else if (domainGroup instanceof AST.DomainGroup.DomainWithAttributeGroup) {
                    String type = ((AST.DomainGroup.DomainWithAttributeGroup) domainGroup).attributeType;
                    enumName = "DomainsWith" + type + "Attribute";
                    String domainEnum = Utility.generateDomainWithAttributeEnum(
                            type,
                            domainNames,
                            packageName,
                            enumName
                    );
                    Utility.writeTextToFile(domainEnum, prefix + enumName + ".java");
                }
                for (String domainName : domainNames) {
                    domainEnums.put(domainName, enumName);
                }
            }

            StringBuilder scannerCode = new StringBuilder();
            scannerCode.append("package ").append(packageName).append(";\n\n")
                    .append("import io.github.sboyanovich.scannergenerator.automata.StateTag;\n" +
                            "import io.github.sboyanovich.scannergenerator.scanner.Fragment;\n" +
                            "import io.github.sboyanovich.scannergenerator.scanner.LexicalRecognizer;\n" +
                            "import io.github.sboyanovich.scannergenerator.scanner.Position;\n" +
                            "import io.github.sboyanovich.scannergenerator.scanner.Text;\n" +
                            "import io.github.sboyanovich.scannergenerator.scanner.token.Domain;\n" +
                            "import io.github.sboyanovich.scannergenerator.scanner.token.Token;\n" +
                            "import io.github.sboyanovich.scannergenerator.utility.Utility;\n" +
                            "\n" +
                            "import java.util.*;\n\n")
                    .append("import static ").append(packageName).append(".").append(scannerClassName)
                    .append(".Mode.*;\n")
                    .append("import static ").append(packageName).append(".").append(stateTagsEnumName)
                    .append(".*;\n\n")
                    .append("public abstract class ").append(scannerClassName)
                    .append(" implements Iterator<Token> {\n    protected enum Mode {\n");

            final String INDENT_4 = "    ";

            scannerCode.append(INDENT_4).append(INDENT_4).append("INITIAL");
            for (AST.Identifier mode : spec.modes.modeNames) {
                scannerCode.append(",\n").append(INDENT_4).append(INDENT_4).append(mode.identifier);
            }
            scannerCode.append("\n").append(INDENT_4).append("}\n\n")
                    .append("    private static final int NEWLINE = Utility.asCodePoint(\"\\n\");\n" +
                            "    private static final int CARRET = Utility.asCodePoint(\"\\r\");\n" +
                            "\n" +
                            "    private Map<Mode, LexicalRecognizer> recognizers;\n" +
                            "    private Position currPos;\n" +
                            "    private Position start;\n" +
                            "    private Text inputText;\n" +
                            "    private Mode currentMode;\n" +
                            "    private int currState;\n" +
                            "    private boolean hasNext;\n" +
                            "\n" +
                            "    public ")
                    .append(scannerClassName)
                    .append("(String inputText) {\n" +
                            "        // General purpose initialization.\n" +
                            "        this.inputText = new Text(inputText);\n" +
                            "        this.currentMode = INITIAL;\n" +
                            "        this.currPos = new Position();\n" +
                            "        this.start = this.currPos;\n" +
                            "        this.hasNext = true;\n" +
                            "\n" +
                            "        // Building tag list for correct restoring of recognizers from files.\n" +
                            "        List<StateTag> finalTags = new ArrayList<>();\n");

            for (StateTag tag : priorityList) {
                scannerCode
                        .append(INDENT_4)
                        .append(INDENT_4)
                        .append("finalTags.add(").append(tag).append(");\n");
            }
            scannerCode.append("\n" +
                    "        // Restoring recognizers from files.\n" +
                    "        this.recognizers = new HashMap<>();\n");
            for (String modeName : modes.keySet()) {
                String fileName = prefix + recognizersDirName + "/" + modeName + ".reco";
                scannerCode
                        .append(INDENT_4)
                        .append(INDENT_4)
                        .append("this.recognizers.put(").append(modeName).append(", ")
                        .append("new LexicalRecognizer(\"")
                        .append(fileName)
                        .append("\", finalTags));\n");
            }
            scannerCode.append("\n" +
                    "        // just in case\n" +
                    "        resetCurrState();\n" +
                    "    }\n" +
                    "\n" +
                    "    public String getInputText() {\n" +
                    "        return inputText.toString();\n" +
                    "    }\n" +
                    "\n" +
                    "    protected void resetCurrState() {\n" +
                    "        this.currState = getCurrentRecognizer().getInitialState();\n" +
                    "    }\n" +
                    "\n" +
                    "    protected void switchToMode(Mode mode) {\n" +
                    "        this.currentMode = mode;\n" +
                    "        resetCurrState();\n" +
                    "    }\n" +
                    "\n" +
                    "    protected void setStartToCurrentPosition() {\n" +
                    "        this.start = this.currPos;\n" +
                    "    }\n" +
                    "\n" +
                    "    private void advanceCurrentPosition() {\n" +
                    "        int index = this.currPos.getIndex();\n" +
                    "        int codePoint = this.inputText.codePointAt(index);\n" +
                    "        int nextCodePoint = this.inputText.codePointAt(index + 1);\n" +
                    "\n" +
                    "        if (codePoint != Text.EOI) {\n" +
                    "            int line = this.currPos.getLine();\n" +
                    "            // CARRET not followed by NEWLINE will also count as line break\n" +
                    "            if ((codePoint == NEWLINE) || ((codePoint == CARRET) && (nextCodePoint != NEWLINE))) {\n" +
                    "                this.currPos = new Position(line + 1, 1, index + 1);\n" +
                    "            } else {\n" +
                    "                int pos = this.currPos.getPos();\n" +
                    "                this.currPos = new Position(line, pos + 1, index + 1);\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    private String getTextFragment(Fragment span) {\n" +
                    "        return Utility.getTextFragmentAsString(this.inputText, span);\n" +
                    "    }\n" +
                    "\n" +
                    "    private int getCurrentCodePoint() {\n" +
                    "        int cp = this.currPos.getIndex();\n" +
                    "        return this.inputText.codePointAt(cp);\n" +
                    "    }\n" +
                    "\n" +
                    "    private boolean atPotentialTokenStart() {\n" +
                    "        int currCodePoint = getCurrentCodePoint();\n" +
                    "        // assuming general use case that all token starts are recognized by default mode\n" +
                    "        LexicalRecognizer recognizer = this.recognizers.get(INITIAL);\n" +
                    "        int nextState = recognizer.transition(recognizer.getInitialState(), currCodePoint);\n" +
                    "        return nextState != LexicalRecognizer.DEAD_END_STATE;\n" +
                    "    }\n" +
                    "\n" +
                    "    private LexicalRecognizer getCurrentRecognizer() {\n" +
                    "        return this.recognizers.get(this.currentMode);\n" +
                    "    }\n" +
                    "\n" +
                    "    private boolean isFinal(int currState) {\n" +
                    "        return StateTag.isFinal(this.recognizers.get(this.currentMode).getStateTag(currState));\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    final public boolean hasNext() {\n" +
                    "        return this.hasNext;\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    final public Token next() {\n" +
                    "        return nextToken();\n" +
                    "    }\n"
            );
            scannerCode.append("\n" +
                    "    public Token nextToken() {\n" +
                    "        resetCurrState();\n" +
                    "        setStartToCurrentPosition();\n" +
                    "\n" +
                    "        // Save last final state encountered\n" +
                    "        OptionalInt lastFinalState = OptionalInt.empty();\n" +
                    "        Position lastInFinal = new Position(); // will be used only if lastFinalState is present\n" +
                    "\n" +
                    "        while (true) {\n" +
                    "            int currCodePoint = getCurrentCodePoint();\n" +
                    "            int nextState = getCurrentRecognizer().transition(this.currState, currCodePoint);\n" +
                    "\n" +
                    "            if (isFinal(this.currState)) {\n" +
                    "                lastFinalState = OptionalInt.of(this.currState);\n" +
                    "                lastInFinal = this.currPos;\n" +
                    "            }\n" +
                    "\n" +
                    "            if (nextState != LexicalRecognizer.DEAD_END_STATE) {\n" +
                    "                this.currState = nextState;\n" +
                    "                advanceCurrentPosition();\n" +
                    "            } else {\n" +
                    "                // it's time to stop\n" +
                    "\n" +
                    "                // nothing matched\n" +
                    "                if (!isFinal(this.currState) && !lastFinalState.isPresent()) {\n" +
                    "                    /// This guards against finding EOI while completing an earlier started token\n" +
                    "                    if (\n" +
                    "                            (currCodePoint == Text.EOI || currCodePoint == this.inputText.getAltEoi()) &&\n" +
                    "                                    this.currPos.equals(this.start)\n" +
                    "                    ) {\n" +
                    "                        this.hasNext = false;\n" +
                    "                        return Domain.END_OF_INPUT.createToken(this.inputText, new Fragment(currPos, currPos));\n" +
                    "                    }\n" +
                    "\n" +
                    "                    // we've found an error\n" +
                    "\n" +
                    "                    /// ERROR HANDLING CODE GOES HERE!\n" +
                    "                    handleError(currCodePoint, this.currentMode, this.currPos);\n" +
                    "\n" +
                    "                    // recovery\n" +
                    "                    // symbol we've stumbled upon might be the beginning of a new token\n" +
                    "                    while ((getCurrentCodePoint() != Text.EOI) && !atPotentialTokenStart()) {\n" +
                    "                        advanceCurrentPosition();\n" +
                    "                    }\n" +
                    "                    switchToMode(INITIAL); // resetting to default mode after error recovery\n" +
                    "                    Fragment invalidFragment = new Fragment(this.start, this.currPos);\n" +
                    "\n" +
                    "                    return Domain.ERROR.createToken(this.inputText, invalidFragment);\n" +
                    "                } else {\n" +
                    "                    if (lastFinalState.isPresent()) {\n" +
                    "                        this.currPos = lastInFinal;\n" +
                    "                        this.currState = lastFinalState.getAsInt();\n" +
                    "                    }\n" +
                    "                    // now currState is certainly final\n" +
                    "\n" +
                    "                    lastFinalState = OptionalInt.empty(); // something matched, no reusing this!\n" +
                    "                    Fragment scannedFragment = new Fragment(this.start, this.currPos);\n" +
                    "                    Optional<Token> optToken = Optional.empty();\n" +
                    "\n" +
                    "                    // this cast should always work, provided all final ones are in one enum\n" +
                    "                    // alternative: switch vs instanceof\n" +
                    "                    "
            )
                    .append(stateTagsEnumName).append(" tag = (").append(stateTagsEnumName)
                    .append(") getCurrentRecognizer().getStateTag(this.currState);\n" +
                            "\n" +
                            "                    /// TIP: for ignored expressions (e.g. whitespace) case should just" +
                            " reset start\n" +
                            "                    switch (tag) {\n"
                    );

            List<String> actionNames = new ArrayList<>();

            for (AST.Rules.Rule rule : rules) {
                String stateName = rule.stateName;
                AST.Rules.Rule.Action action = rule.action;
                scannerCode.append("                        case ")
                        .append(stateName).append(":\n");
                if (action instanceof AST.Rules.Rule.Action.Call) {
                    String funcName = ((AST.Rules.Rule.Action.Call) action).funcName;
                    scannerCode.append(generateActionFuncCall(funcName));
                    actionNames.add(funcName);
                } else if (action instanceof AST.Rules.Rule.Action.Ignore) {
                    scannerCode.append("                            setStartToCurrentPosition();\n");
                } else if (action instanceof AST.Rules.Rule.Action.Switch) {
                    String modeName = ((AST.Rules.Rule.Action.Switch) action).modeName;
                    scannerCode.append("                            ")
                            .append("switchToMode(").append(modeName).append(");\n");
                } else if (action instanceof AST.Rules.Rule.Action.Return) {
                    String domainName = ((AST.Rules.Rule.Action.Return) action).domainName;
                    String enumName = domainEnums.get(domainName);
                    scannerCode.append("                            ")
                            .append("optToken = Optional.of(\n")
                            .append("                                    ")
                            .append(enumName)
                            .append("\n                                            .")
                            .append(domainName)
                            .append(".createToken(this.inputText, scannedFragment)\n")
                            .append("                            );\n");
                } else if (action instanceof AST.Rules.Rule.Action.SwitchReturn) {
                    String modeName = ((AST.Rules.Rule.Action.SwitchReturn) action).modeName;
                    String domainName = ((AST.Rules.Rule.Action.SwitchReturn) action).domainName;
                    String enumName = domainEnums.get(domainName);

                    scannerCode.append("                            ")
                            .append("switchToMode(").append(modeName).append(");\n");
                    scannerCode.append("                            ")
                            .append("optToken = Optional.of(\n")
                            .append("                                    ")
                            .append(enumName)
                            .append("\n                                            .")
                            .append(domainName)
                            .append(".createToken(this.inputText, scannedFragment)\n")
                            .append("                            );\n");
                }
                scannerCode.append("                            break;\n");
            }
            scannerCode.append("                    }\n" +
                    "\n" +
                    "                    if (optToken.isPresent()) {\n" +
                    "                        return optToken.get();\n" +
                    "                    } else {\n" +
                    "                        resetCurrState();\n" +
                    "                    }\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n\n"
            );

            scannerCode.append("    protected abstract void " +
                    "handleError(int codePoint, Mode mode, Position errorAt);\n\n");

            for (String actionName : actionNames) {
                scannerCode.append(generateActionSignature(actionName));
            }
            scannerCode.append("}");

            Utility.writeTextToFile(scannerCode.toString(), prefix + scannerClassName + ".java");

            System.out.println("Time taken building NFAs: " + timeElapsed + "ms");
        }
    }

    static String generateActionFuncCall(String actionName) {
        return "                            optToken = " +
                actionName + "(this.inputText, scannedFragment);\n";
    }

    static String generateActionSignature(String actionName) {
        return "    protected abstract Optional<Token> " +
                actionName +
                "(Text text, Fragment fragment);\n\n";
    }

    static NFA buildNFAFromRegex(AST.Regex regex, Map<String, NFA> namedExpressions, int alphabetSize) {
        return regex.buildNFA(namedExpressions, alphabetSize);
    }

    static LexicalRecognizer buildRecognizer(NFA lang, Map<StateTag, Integer> priorityMap, Set<Integer> pivots) {
        //System.out.println(lang.getNumberOfStates());

        // This appears to be necessary for determinization to work properly. It shouldn't be.
        lang = lang.removeLambdaSteps();
        //System.out.println("Lambda steps removed.");

        List<Integer> pivotList = new ArrayList<>(pivots);

        /*       Instant start = Instant.now(); */
        DFA dfa = lang.determinize(priorityMap, pivotList);
/*        Instant stop = Instant.now();
        long timeElapsed = Duration.between(start, stop).toMillis();

        System.out.println("Determinized!");
        System.out.println("\tin " + timeElapsed + "ms");
        System.out.println("States: " + dfa.getNumberOfStates());
        System.out.println("Classes: " + dfa.getTransitionTable().getEquivalenceMap().getEqClassDomain());

        start = Instant.now();*/
        LexicalRecognizer recognizer = new LexicalRecognizer(dfa);
/*
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
*/

        return recognizer;
    }

}
