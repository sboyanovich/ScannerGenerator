package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.automata.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.LexicalRecognizer;
import io.github.sboyanovich.scannergenerator.scanner.Message;
import io.github.sboyanovich.scannergenerator.scanner.Position;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class RecognizerGenTest {

    public static final boolean DEBUG_PROFILE = false;
    static Instant startTotal, endTotal;
    static long totalTimeElapsed, timeNFA, timeGeneratingCode,
            timeBuildingRecognizers, timeWritingRecognizers, timeRemovingLambdas,
            timeComputingPivots, timeDeterminizing, timeCompressingAndMinimizing;
    public static long timeBuildingCharClasses = 0;

    static final String APP_NAME = "xscan";
    static final String USAGE_HINT =
            "USAGE: " + APP_NAME + " <input_file> [-r] [-d] [-p <package_name>] [-t]";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println(USAGE_HINT);
            System.exit(1);
        }

        String inputFile = args[0];

        /// PARAMS
        String recognizersDirName = "recognizers";
        String prefix = "generated/";
        String packageName = "io.github.sboyanovich.scannergenerator.generated";
        String stateTagsEnumName = "StateTags";
        String simpleDomainsEnumName = "SimpleDomains";
        String scannerClassName = "GeneratedScanner";
        boolean dumpDotDescriptions = false;
        boolean readRecognizersFromResources = false;
        boolean dumpAstDescription = false;

        /// PROCESSING COMMAND LINE ARGS
        boolean expectPackageName = false;
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (expectPackageName) {
                packageName = arg;
                expectPackageName = false;
            } else {
                switch (arg) {
                    case "-r":
                        readRecognizersFromResources = true;
                        break;
                    case "-d":
                        dumpDotDescriptions = true;
                        break;
                    case "-p":
                        expectPackageName = true;
                        if (i + 1 >= args.length) {
                            System.err.println("Package name parameter must be supplied!");
                            System.err.println(USAGE_HINT);
                            System.exit(1);
                        }
                        break;
                    case "-t":
                        dumpAstDescription = true;
                        break;
                    default:
                        System.err.println("Unrecognized arg: " + arg);
                        System.err.println(USAGE_HINT);
                        System.exit(1);
                }
            }
        }

        if (DEBUG_PROFILE) {
            startTotal = Instant.now();
        }

        /// READING TEXT FROM FILE
        String text = Utility.getText(inputFile);

        int maxCodePoint = Character.MAX_CODE_POINT;
        int aeoi = maxCodePoint + 1;
        int alphabetSize = maxCodePoint + 1 + 1;

        /// CREATING SCANNER
        MyScanner scanner = new MyScanner(text, alphabetSize);
        MockCompiler compiler = scanner.getCompiler();

        /// PARSING AND BUILDING AST
        AST ast = null;
        try {
            ast = Parser.parse(scanner, compiler);
        } catch (IllegalStateException e) {
            System.out.println("There are syntax errors in the input. Parsing cannot proceed.\n");
        }

        if (dumpAstDescription) {
            String dotAST = ast.toGraphVizDotString();
            System.out.println();
            System.out.println(dotAST);
            System.out.println();
        }

        int errors = compiler.getErrorCount();
        int warnings = compiler.getWarningCount();
        if (errors > 0) {
            System.out.println("Errors: " + errors);
        }
        if (warnings > 0) {
            System.out.println("Warnings: " + warnings);
        }
        /// DISPLAYING COMPILER MESSAGES
        System.out.println("Compiler messages:");
        SortedMap<Position, Message> messages = compiler.getSortedMessages();
        for (Map.Entry<Position, Message> entry : messages.entrySet()) {
            System.out.println("\tat " + entry.getKey() + " " + entry.getValue());
        }

        /// GENERATION PHASE
        if (compiler.getErrorCount() == 0) {

            AST.Spec spec = (AST.Spec) ast;

            Map<String, NFA> definitions = new HashMap<>();
            Map<String, Set<Integer>> defPivots = new HashMap<>();

            Instant start, end;

            /// BUILDING DEFINITIONS NFAs
            for (AST.Definitions.Def def : spec.definitions.definitions) {
                String name = def.identifier.identifier;

                if (DEBUG_PROFILE) {
                    start = Instant.now();
                }
                Set<Integer> pivots = def.regex.getPivots(defPivots, alphabetSize);
                defPivots.put(name, pivots);
                if (DEBUG_PROFILE) {
                    end = Instant.now();
                    timeComputingPivots += Duration.between(start, end).toNanos();
                }

                if (DEBUG_PROFILE) {
                    start = Instant.now();
                }
                NFA auto = buildNFAFromRegex(def.regex, definitions, alphabetSize);
                if (DEBUG_PROFILE) {
                    end = Instant.now();
                    timeNFA += Duration.between(start, end).toNanos();
                }

                definitions.put(name, auto);
            }

            Map<String, List<NFA>> modeNFALists = new HashMap<>();
            Map<String, NFA> modeNFAs = new HashMap<>();
            List<StateTag> priorityList = new ArrayList<>();
            Map<String, Set<Integer>> rulePivots = new HashMap<>();
            Map<String, Set<Integer>> modePivots = new HashMap<>();

            /// BUILDING RULE NFAs
            List<AST.Rules.Rule> rules = spec.rules.rules;
            for (AST.Rules.Rule rule : rules) {
                String stateName = rule.stateName;

                if (DEBUG_PROFILE) {
                    start = Instant.now();
                }
                Set<Integer> pivots = rule.regex.getPivots(defPivots, alphabetSize);
                rulePivots.put(stateName, pivots);
                if (DEBUG_PROFILE) {
                    end = Instant.now();
                    timeComputingPivots += Duration.between(start, end).toNanos();
                }

                if (DEBUG_PROFILE) {
                    start = Instant.now();
                }
                NFA nfa = buildNFAFromRegex(rule.regex, definitions, alphabetSize);
                if (DEBUG_PROFILE) {
                    end = Instant.now();
                    timeNFA += Duration.between(start, end).toNanos();
                }

                StateTag stateTag = new StateTag() {
                    String name = stateName;

                    @Override
                    public String toString() {
                        return name;
                    }
                };

                nfa = nfa.setAllFinalStatesTo(stateTag);

                priorityList.add(stateTag);

                /// ADDING RULES TO THEIR MODES
                List<AST.Identifier> modeNames = rule.modeList.modeNames;
                for (var mode : modeNames) {
                    String modeName = mode.identifier;
                    if (modeNFALists.containsKey(modeName)) {
                        modeNFALists.get(modeName).add(nfa);

                        if (DEBUG_PROFILE) {
                            start = Instant.now();
                        }
                        modePivots.get(modeName).addAll(rulePivots.get(stateName));
                        if (DEBUG_PROFILE) {
                            end = Instant.now();
                            timeComputingPivots += Duration.between(start, end).toNanos();
                        }
                    } else {
                        List<NFA> modeList = new ArrayList<>();
                        modeList.add(nfa);
                        modeNFALists.put(modeName, modeList);
                        if (DEBUG_PROFILE) {
                            start = Instant.now();
                        }
                        modePivots.put(modeName, new HashSet<>(rulePivots.get(stateName)));
                        if (DEBUG_PROFILE) {
                            end = Instant.now();
                            timeComputingPivots += Duration.between(start, end).toNanos();
                        }
                    }
                }
            }

            /// BUILDING MODE NFAs
            for (String modeName : modeNFALists.keySet()) {
                if (DEBUG_PROFILE) {
                    start = Instant.now();
                }
                NFA modeNFA = NFA.unionAll(modeNFALists.get(modeName));
                if (DEBUG_PROFILE) {
                    end = Instant.now();
                    timeNFA += Duration.between(start, end).toNanos();
                }
                modeNFAs.put(modeName, modeNFA);
            }

            Map<StateTag, Integer> priorityMap = new HashMap<>();

            /// BUILDING PRIORITY MAP
            for (int i = 0; i < priorityList.size(); i++) {
                priorityMap.put(priorityList.get(i), priorityList.size() - (i + 1));
            }

            Map<String, LexicalRecognizer> modes = new HashMap<>();

            /// BUILDING RECOGNIZERS FOR EVERY MODE
            if (DEBUG_PROFILE) {
                start = Instant.now();
            }
            for (String modeName : modeNFAs.keySet()) {
                NFA nfa = modeNFAs.get(modeName);
                if (DEBUG_PROFILE) {
                    System.out.println("Mode: " + modeName);
                }
                modes.put(modeName, buildRecognizer(nfa, priorityMap, modePivots.get(modeName)));
            }
            if (DEBUG_PROFILE) {
                end = Instant.now();
                timeBuildingRecognizers += Duration.between(start, end).toMillis();
            }

            /// WRITING RECOGNIZERS TO FILES
            if (DEBUG_PROFILE) {
                start = Instant.now();
            }
            for (String modeName : modes.keySet()) {
                LexicalRecognizer recognizer = modes.get(modeName);
                recognizer.writeToFile(
                        prefix + recognizersDirName + "/" + modeName + ".reco", priorityMap
                );
            }
            if (DEBUG_PROFILE) {
                end = Instant.now();
                timeWritingRecognizers += Duration.between(start, end).toMillis();
            }

            System.out.println();
            Collections.reverse(priorityList);

            /// CODE GENERATION SECTION

            List<String> stateNames = priorityList.stream().map(Objects::toString).collect(Collectors.toList());

            /// GENERATING STATE TAGS ENUM
            if (DEBUG_PROFILE) {
                start = Instant.now();
            }
            String stateTagsEnum = Utility.generateStateTagsEnum(stateNames, packageName);

            Utility.writeTextToFile(stateTagsEnum, prefix + stateTagsEnumName + ".java");

            List<AST.DomainGroup> domainGroupList = spec.domainGroups.domainGroups;

            final String SIMPLE_DOMAIN_KEY = "@SIMPLE";

            /// MERGING DOMAIN GROUPS BY ATTRIBUTE TYPE
            Map<String, List<String>> domainNamesMap = new HashMap<>();
            for (AST.DomainGroup domainGroup : domainGroupList) {
                String key = "";
                if (domainGroup instanceof AST.DomainGroup.SimpleDomainGroup) {
                    key = SIMPLE_DOMAIN_KEY;
                } else if (domainGroup instanceof AST.DomainGroup.DomainWithAttributeGroup) {
                    key = ((AST.DomainGroup.DomainWithAttributeGroup) domainGroup).attributeType;
                }
                List<String> names;
                if (domainNamesMap.containsKey(key)) {
                    names = domainNamesMap.get(key);
                } else {
                    names = new ArrayList<>();
                    domainNamesMap.put(key, names);
                }
                names.addAll(domainGroup.getDomainNames());
            }

            Map<String, String> domainEnums = new HashMap<>();

            /// GENERATING DOMAINS ENUMS
            for (String key : domainNamesMap.keySet()) {
                List<String> domainNames = domainNamesMap.get(key);
                String enumName = "";
                if (key.equals(SIMPLE_DOMAIN_KEY)) {
                    enumName = simpleDomainsEnumName;
                    String simpleDomainsEnum = Utility.generateSimpleDomainsEnum(
                            domainNames,
                            packageName,
                            enumName
                    );
                    Utility.writeTextToFile(simpleDomainsEnum, prefix + enumName + ".java");
                } else {
                    String type = key;
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

            /// GENERATING MAIN SCANNER CLASS
            StringBuilder scannerCode = new StringBuilder();
            scannerCode.append("package ").append(packageName).append(";\r\n\r\n")
                    .append("import io.github.sboyanovich.scannergenerator.automata.StateTag;\r\n" +
                            "import io.github.sboyanovich.scannergenerator.scanner.Fragment;\r\n" +
                            "import io.github.sboyanovich.scannergenerator.scanner.LexicalRecognizer;\r\n" +
                            "import io.github.sboyanovich.scannergenerator.scanner.Position;\r\n" +
                            "import io.github.sboyanovich.scannergenerator.scanner.Text;\r\n" +
                            "import io.github.sboyanovich.scannergenerator.scanner.token.Domain;\r\n" +
                            "import io.github.sboyanovich.scannergenerator.scanner.token.Token;\r\n" +
                            "import io.github.sboyanovich.scannergenerator.utility.Utility;\r\n" +
                            "\r\n" +
                            "import java.util.*;\r\n\r\n")
                    .append("import static ").append(packageName).append(".").append(scannerClassName)
                    .append(".Mode.*;\r\n")
                    .append("import static ").append(packageName).append(".").append(stateTagsEnumName)
                    .append(".*;\r\n\r\n")
                    .append("public abstract class ").append(scannerClassName)
                    .append(" implements Iterator<Token> {\r\n    protected enum Mode {\r\n");

            final String INDENT_4 = "    ";

            scannerCode.append(INDENT_4).append(INDENT_4).append("INITIAL");
            for (AST.Identifier mode : spec.modes.modeNames) {
                scannerCode.append(",\r\n").append(INDENT_4).append(INDENT_4).append(mode.identifier);
            }
            scannerCode.append("\r\n").append(INDENT_4).append("}\r\n\r\n")
                    .append("    private static final int NEWLINE = Utility.asCodePoint(\"\\n\");\r\n" +
                            "    private static final int CARRET = Utility.asCodePoint(\"\\r\");\r\n" +
                            "\r\n" +
                            "    private static Map<Mode, LexicalRecognizer> recognizers;\r\n\r\n" +
                            "    static {\r\n");
            scannerCode.append(
                    "        // Building tag list for correct restoring of recognizers from files.\r\n" +
                            "        List<StateTag> finalTags = new ArrayList<>();\r\n"
            );
            for (StateTag tag : priorityList) {
                scannerCode
                        .append(INDENT_4)
                        .append(INDENT_4)
                        .append("finalTags.add(").append(tag).append(");\r\n");
            }
            scannerCode.append("\r\n" +
                    "        // Restoring recognizers from files.\r\n" +
                    "        recognizers = new HashMap<>();\r\n");
            if (readRecognizersFromResources) {
                for (String modeName : modes.keySet()) {
                    String fileName = prefix + recognizersDirName + "/" + modeName + ".reco";
                    scannerCode
                            .append(INDENT_4)
                            .append(INDENT_4)
                            .append("recognizers.put(").append(modeName).append(", ")
                            .append("new LexicalRecognizer(\r\n")
                            .append(INDENT_4).append(INDENT_4).append(INDENT_4).append(INDENT_4)
                            .append("ClassLoader.getSystemClassLoader()\r\n")
                            .append(INDENT_4).append(INDENT_4).append(INDENT_4).append(INDENT_4)
                            .append(INDENT_4).append(INDENT_4)
                            .append(".getResourceAsStream(")
                            .append("\"")
                            .append(fileName)
                            .append("\"), finalTags));\r\n");
                }
            } else {
                for (String modeName : modes.keySet()) {
                    String fileName = prefix + recognizersDirName + "/" + modeName + ".reco";
                    scannerCode
                            .append(INDENT_4)
                            .append(INDENT_4)
                            .append("recognizers.put(").append(modeName).append(", ")
                            .append("new LexicalRecognizer(\"")
                            .append(fileName)
                            .append("\", finalTags));\r\n");
                }
            }
            scannerCode.append(INDENT_4).append("}\r\n\r\n");

            scannerCode.append(
                    "    private Position currPos;\r\n" +
                            "    private Position start;\r\n" +
                            "    private Text inputText;\r\n" +
                            "    private Mode currentMode;\r\n" +
                            "    private int currState;\r\n" +
                            "    private boolean hasNext;\r\n" +
                            "\r\n" +
                            "    public ")
                    .append(scannerClassName)
                    .append("(String inputText) {\r\n" +
                            "        // General purpose initialization.\r\n" +
                            "        this.inputText = new Text(inputText);\r\n" +
                            "        this.currentMode = INITIAL;\r\n" +
                            "        this.currPos = new Position();\r\n" +
                            "        this.start = this.currPos;\r\n" +
                            "        this.hasNext = true;\r\n"
                    );

            scannerCode.append("\r\n" +
                    "        // just in case\r\n" +
                    "        resetCurrState();\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    public String getInputText() {\r\n" +
                    "        return inputText.toString();\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    protected Position getStartPosition() {\r\n" +
                    "        return this.start;\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    protected Position getCurrentPosition() {\r\n" +
                    "        return this.currPos;\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    protected void resetCurrState() {\r\n" +
                    "        this.currState = getCurrentRecognizer().getInitialState();\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    protected void switchToMode(Mode mode) {\r\n" +
                    "        this.currentMode = mode;\r\n" +
                    "        resetCurrState();\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    protected void setStartToCurrentPosition() {\r\n" +
                    "        this.start = this.currPos;\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    protected void setCurrentPositionToStart() {\r\n" +
                    "        this.currPos = this.start;\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    protected void advanceCurrentPosition() {\r\n" +
                    "        int index = this.currPos.getIndex();\r\n" +
                    "        int codePoint = this.inputText.codePointAt(index);\r\n" +
                    "        int nextCodePoint = this.inputText.codePointAt(index + 1);\r\n" +
                    "\r\n" +
                    "        if (codePoint != Text.EOI) {\r\n" +
                    "            int line = this.currPos.getLine();\r\n" +
                    "            // CARRET not followed by NEWLINE will also count as line break\r\n" +
                    "            if ((codePoint == NEWLINE) || ((codePoint == CARRET) && (nextCodePoint != NEWLINE))) {\r\n" +
                    "                this.currPos = new Position(line + 1, 1, index + 1);\r\n" +
                    "            } else {\r\n" +
                    "                int pos = this.currPos.getPos();\r\n" +
                    "                this.currPos = new Position(line, pos + 1, index + 1);\r\n" +
                    "            }\r\n" +
                    "        }\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    private String getTextFragment(Fragment span) {\r\n" +
                    "        return Utility.getTextFragmentAsString(this.inputText, span);\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    protected int getCurrentCodePoint() {\r\n" +
                    "        int cp = this.currPos.getIndex();\r\n" +
                    "        return this.inputText.codePointAt(cp);\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    protected boolean atPotentialPatternStart() {\r\n" +
                    "        int currCodePoint = getCurrentCodePoint();\r\n" +
                    "        // assuming general use case that all token starts are recognized by default mode\r\n" +
                    "        LexicalRecognizer recognizer = recognizers.get(INITIAL);\r\n" +
                    "        int nextState = recognizer.transition(recognizer.getInitialState(), currCodePoint);\r\n" +
                    "        return nextState != LexicalRecognizer.DEAD_END_STATE;\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    private LexicalRecognizer getCurrentRecognizer() {\r\n" +
                    "        return recognizers.get(this.currentMode);\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    private boolean isFinal(int currState) {\r\n" +
                    "        return StateTag.isFinal(recognizers.get(this.currentMode).getStateTag(currState));\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    @Override\r\n" +
                    "    final public boolean hasNext() {\r\n" +
                    "        return this.hasNext;\r\n" +
                    "    }\r\n" +
                    "\r\n" +
                    "    @Override\r\n" +
                    "    final public Token next() {\r\n" +
                    "        return nextToken();\r\n" +
                    "    }\r\n"
            );
            scannerCode.append("\r\n" +
                    "    private Token nextToken() {\r\n" +
                    "        resetCurrState();\r\n" +
                    "        setStartToCurrentPosition();\r\n" +
                    "\r\n" +
                    "        // Save last final state encountered\r\n" +
                    "        OptionalInt lastFinalState = OptionalInt.empty();\r\n" +
                    "        Position lastInFinal = new Position(); // will be used only if lastFinalState is present\r\n" +
                    "\r\n" +
                    "        while (true) {\r\n" +
                    "            int currCodePoint = getCurrentCodePoint();\r\n" +
                    "            int nextState = getCurrentRecognizer().transition(this.currState, currCodePoint);\r\n" +
                    "\r\n" +
                    "            if (isFinal(this.currState)) {\r\n" +
                    "                lastFinalState = OptionalInt.of(this.currState);\r\n" +
                    "                lastInFinal = this.currPos;\r\n" +
                    "            }\r\n" +
                    "\r\n" +
                    "            if (nextState != LexicalRecognizer.DEAD_END_STATE) {\r\n" +
                    "                this.currState = nextState;\r\n" +
                    "                advanceCurrentPosition();\r\n" +
                    "            } else {\r\n" +
                    "                // it's time to stop\r\n" +
                    "\r\n" +
                    "                // nothing matched\r\n" +
                    "                if (!isFinal(this.currState) && !lastFinalState.isPresent()) {\r\n" +
                    "                    /// This guards against finding EOI while completing an earlier started token\r\n" +
                    "                    if (\r\n" +
                    "                            (currCodePoint == Text.EOI || currCodePoint == this.inputText.getAltEoi()) &&\r\n" +
                    "                                    this.currPos.equals(this.start)\r\n" +
                    "                    ) {\r\n" +
                    "                        this.hasNext = false;\r\n" +
                    "                        return Domain.END_OF_INPUT.createToken(this.inputText, new Fragment(currPos, currPos));\r\n" +
                    "                    }\r\n" +
                    "\r\n" +
                    "                    // we've found an error\r\n" +
                    "\r\n" +
                    "                    /// ERROR HANDLING CODE GOES HERE!\r\n" +
                    "                    Optional<Token> optToken = handleError(this.inputText, this.currentMode, this.start, this.currPos);\r\n" +
                    "\r\n" +
                    "                    if (optToken.isPresent()) {\r\n" +
                    "                        return optToken.get();\r\n" +
                    "                    } else {\r\n" +
                    "                        resetCurrState();\r\n" +
                    "                    }\r\n" +
                    "\r\n" +
                    "                } else {\r\n" +
                    "                    if (lastFinalState.isPresent()) {\r\n" +
                    "                        this.currPos = lastInFinal;\r\n" +
                    "                        this.currState = lastFinalState.getAsInt();\r\n" +
                    "                    }\r\n" +
                    "                    // now currState is certainly final\r\n" +
                    "\r\n" +
                    "                    lastFinalState = OptionalInt.empty(); // something matched, no reusing this!\r\n" +
                    "                    Fragment scannedFragment = new Fragment(this.start, this.currPos);\r\n" +
                    "\r\n" +
                    "                    // this addition ensures scannedFragment references exactly the recognized pattern\r\n" +
                    "                    setStartToCurrentPosition();\r\n" +
                    "\r\n" +
                    "                    Optional<Token> optToken = Optional.empty();\r\n" +
                    "\r\n" +
                    "                    // this cast should always work, provided all final ones are in one enum\r\n" +
                    "                    // alternative: switch vs instanceof\r\n" +
                    "                    "
            )
                    .append(stateTagsEnumName).append(" tag = (").append(stateTagsEnumName)
                    .append(") getCurrentRecognizer().getStateTag(this.currState);\r\n" +
                            "\r\n" +
                            "                    switch (tag) {\r\n"
                    );

            List<String> actionNames = new ArrayList<>();

            /// RULE ACTIONS
            for (AST.Rules.Rule rule : rules) {
                String stateName = rule.stateName;
                AST.Rules.Rule.Action action = rule.action;
                scannerCode.append("                        case ")
                        .append(stateName).append(":\r\n");
                if (action instanceof AST.Rules.Rule.Action.Call) {
                    String funcName = ((AST.Rules.Rule.Action.Call) action).funcName;
                    scannerCode.append(generateActionFuncCall(funcName));
                    actionNames.add(funcName);
                } else if (action instanceof AST.Rules.Rule.Action.Ignore) {
                    // scannerCode.append("                            setStartToCurrentPosition();\r\n");
                } else if (action instanceof AST.Rules.Rule.Action.Switch) {
                    String modeName = ((AST.Rules.Rule.Action.Switch) action).modeName;
                    scannerCode.append("                            ")
                            .append("switchToMode(").append(modeName).append(");\r\n");
                } else if (action instanceof AST.Rules.Rule.Action.Return) {
                    String domainName = ((AST.Rules.Rule.Action.Return) action).domainName;
                    String enumName = domainEnums.get(domainName);
                    scannerCode.append("                            ")
                            .append("optToken = Optional.of(\r\n")
                            .append("                                    ")
                            .append(enumName)
                            .append("\r\n                                            .")
                            .append(domainName)
                            .append(".createToken(this.inputText, scannedFragment)\r\n")
                            .append("                            );\r\n");
                } else if (action instanceof AST.Rules.Rule.Action.SwitchReturn) {
                    String modeName = ((AST.Rules.Rule.Action.SwitchReturn) action).modeName;
                    String domainName = ((AST.Rules.Rule.Action.SwitchReturn) action).domainName;
                    String enumName = domainEnums.get(domainName);

                    scannerCode.append("                            ")
                            .append("switchToMode(").append(modeName).append(");\r\n");
                    scannerCode.append("                            ")
                            .append("optToken = Optional.of(\r\n")
                            .append("                                    ")
                            .append(enumName)
                            .append("\r\n                                            .")
                            .append(domainName)
                            .append(".createToken(this.inputText, scannedFragment)\r\n")
                            .append("                            );\r\n");
                }
                scannerCode.append("                            break;\r\n");
            }
            scannerCode.append("                    }\r\n" +
                    "\r\n" +
                    "                    if (optToken.isPresent()) {\r\n" +
                    "                        return optToken.get();\r\n" +
                    "                    } else {\r\n" +
                    "                        resetCurrState();\r\n" +
                    "                    }\r\n" +
                    "                }\r\n" +
                    "            }\r\n" +
                    "        }\r\n" +
                    "    }\r\n\r\n"
            );

            scannerCode.append(
                    "    /// Default implementation, to ensure scanner doesn't get stuck\r\n" +
                            "    protected Optional<Token> handleError(Text text, Mode mode, Position start, Position follow) {\r\n" +
                            "        // recovery\r\n" +
                            "        // discard symbols from input until we find a potential pattern start\r\n" +
                            "        setCurrentPositionToStart();\r\n" +
                            "        advanceCurrentPosition();\r\n" +
                            "\r\n" +
                            "        while ((getCurrentCodePoint() != Text.EOI) && !atPotentialPatternStart()) {\r\n" +
                            "            advanceCurrentPosition();\r\n" +
                            "        }\r\n" +
                            "        switchToMode(INITIAL); // resetting to default mode after error recovery\r\n" +
                            "        Fragment invalidFragment = new Fragment(this.start, this.currPos);\r\n" +
                            "\r\n" +
                            "        /// HINT: If you wish to do the same, but not return any token, remember to call setStartToCurrentPosition()\r\n" +
                            "\r\n" +
                            "        return Optional.of(Domain.ERROR.createToken(this.inputText, invalidFragment));\r\n" +
                            "    }\r\n\r\n");

            for (String actionName : actionNames) {
                scannerCode.append(generateActionSignature(actionName));
            }
            scannerCode.append("}");

            Utility.writeTextToFile(scannerCode.toString(), prefix + scannerClassName + ".java");
            if (DEBUG_PROFILE) {
                end = Instant.now();
                timeGeneratingCode += Duration.between(start, end).toMillis();
            }

            if (DEBUG_PROFILE) {
                endTotal = Instant.now();
                totalTimeElapsed = Duration.between(startTotal, endTotal).toMillis();
            }

            if (DEBUG_PROFILE) {
                final int MILLION = 1_000_000;

                timeBuildingCharClasses /= MILLION;
                timeNFA /= MILLION;
            }

            if (dumpDotDescriptions) {
                System.out.println();
                for (String modeName : modes.keySet()) {
                    LexicalRecognizer recognizer = modes.get(modeName);
                    String factorization = recognizer.displayEquivalenceMap(Utility::defaultUnicodeInterpretation);
                    System.out.println("/*\n" + modeName + "\n" + factorization + "\n*/");
                    String dot = recognizer.toGraphvizDotString(Object::toString, true);
                    System.out.println(dot);
                }
                System.out.println();
            }

            if (DEBUG_PROFILE) {
                System.out.println("Time building NFAs: " + timeNFA + "ms");
                System.out.println("Time building char classes " + timeBuildingCharClasses + "ms");
                System.out.println("Time computing pivots: " + timeComputingPivots + "ns");
                System.out.println("Time building recognizers: " + timeBuildingRecognizers + "ms");
                System.out.println("Time removing lambdas: " + timeRemovingLambdas + "ms");
                System.out.println("Time determinizing: " + timeDeterminizing + "ms");
                System.out.println("Time compressing and minimizing: " + timeCompressingAndMinimizing + "ms");
                System.out.println("Time writing recognizers: " + timeWritingRecognizers + "ms");
                System.out.println("Time generating code: " + timeGeneratingCode + "ms");
                System.out.println("Total time: " + totalTimeElapsed + "ms");
            }
        }
    }

    static String generateActionFuncCall(String actionName) {
        return "                            optToken = " +
                actionName + "(this.inputText, scannedFragment);\r\n";
    }

    static String generateActionSignature(String actionName) {
        return "    protected abstract Optional<Token> " +
                actionName +
                "(Text text, Fragment fragment);\r\n\r\n";
    }

    static NFA buildNFAFromRegex(AST.Regex regex, Map<String, NFA> namedExpressions, int alphabetSize) {
        return regex.buildNFA(namedExpressions, alphabetSize);
    }

    private static final int A_LOT_OF_STATES = 250;

    static LexicalRecognizer buildRecognizer(NFA lang, Map<StateTag, Integer> priorityMap, Set<Integer> pivots) {
        Instant start, end;
        if (DEBUG_PROFILE) {
            start = Instant.now();
        }
        if (lang.getNumberOfStates() >= A_LOT_OF_STATES) {
            lang = lang.removeLambdaSteps();
        }
        if (DEBUG_PROFILE) {
            end = Instant.now();
            timeRemovingLambdas += Duration.between(start, end).toMillis();
        }

        List<Integer> pivotList = new ArrayList<>(pivots);

        if (DEBUG_PROFILE) {
            start = Instant.now();
        }
        DFA dfa = lang.determinize(priorityMap, pivotList);
        if (DEBUG_PROFILE) {
            end = Instant.now();
            timeDeterminizing += Duration.between(start, end).toMillis();
        }

        if (DEBUG_PROFILE) {
            start = Instant.now();
        }
        LexicalRecognizer recognizer = new LexicalRecognizer(dfa);
        if (DEBUG_PROFILE) {
            end = Instant.now();
            timeCompressingAndMinimizing += Duration.between(start, end).toMillis();
        }

        return recognizer;
    }
}
