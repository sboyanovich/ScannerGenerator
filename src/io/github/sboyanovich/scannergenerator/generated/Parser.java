package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.scanner.Position;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.*;

import static io.github.sboyanovich.scannergenerator.generated.DomainsWithIntPairAttribute.REPETITION_OP;
import static io.github.sboyanovich.scannergenerator.generated.DomainsWithIntegerAttribute.CHAR;
import static io.github.sboyanovich.scannergenerator.generated.DomainsWithStringAttribute.*;
import static io.github.sboyanovich.scannergenerator.generated.DomainsWithStringPairAttribute.ACTION_SWITCH_RETURN;
import static io.github.sboyanovich.scannergenerator.generated.SimpleDomains.*;

public class Parser {
    private Token sym;
    private Iterator<Token> tokens;
    private int nodeNames;

    private int stateNames;

    private MockCompiler compiler;

    private Map<String, Position> defNamesMap;
    private Map<String, Position> modeNamesMap;
    private Map<String, Position> domainNamesMap;
    private Map<String, Position> ruleNamesMap;

    private Set<Domain> ruleFirst = Set.of(
            L_ANGLE_BRACKET,
            IDENTIFIER,
            DEFINER
    );

    private Set<Domain> fRegexFirst = Set.of(
            NAMED_EXPR,
            CHAR_CLASS_OPEN,
            CHAR,
            DOT,
            LPAREN
    );

    private Set<Domain> unaryOperators = Set.of(
            POS_ITERATION_OP,
            ITERATION_OP,
            OPTION_OP,
            REPETITION_OP
    );

    private Parser(Iterator<Token> tokens, MockCompiler compiler) {
        this.tokens = tokens;
        this.sym = tokens.next();
        this.nodeNames = 0;
        this.stateNames = 0;
        this.compiler = compiler;
        this.defNamesMap = new HashMap<>();
        this.modeNamesMap = new HashMap<>();
        this.domainNamesMap = new HashMap<>();
        this.ruleNamesMap = new HashMap<>();
    }

    static AST parse(Iterator<Token> tokens, MockCompiler compiler) {
        return new Parser(tokens, compiler).parse();
    }

    private AST parse() {
        AST ast = spec();
        expect(Domain.END_OF_INPUT);
        return ast;
    }

    private AST.Spec spec() {
        AST.Spec spec = new AST.Spec();
        spec.number = getNodeName();

        AST.Definitions definitions = new AST.Definitions();
        definitions.number = getNodeName();

        List<AST.Definitions.Def> defs = new ArrayList<>();

        while (sym.getTag().equals(DomainsWithStringAttribute.IDENTIFIER)) {
            defs.add(definition());
        }
        definitions.definitions = defs;

        AST.Modes modes;

        if (sym.getTag().equals(MODES_SECTION_MARKER)) {
            modes = modesSection();
        } else {
            modes = new AST.Modes();
            modes.number = getNodeName();
            modes.modeNames = new ArrayList<>();
        }

        AST.DomainGroups domainGroups;

        if (sym.getTag().equals(DOMAINS_GROUP_MARKER)) {
            domainGroups = domainsSection();
        } else {
            domainGroups = new AST.DomainGroups();
            domainGroups.number = getNodeName();
            domainGroups.domainGroups = new ArrayList<>();
        }

        expect(SimpleDomains.RULES_SECTION_MARKER);

        AST.Rules rules = new AST.Rules();
        rules.number = getNodeName();
        List<AST.Rules.Rule> ruleList = new ArrayList<>();

        while (ruleFirst.contains(sym.getTag())) {
            ruleList.add(rule());
        }

        rules.rules = ruleList;

        spec.definitions = definitions;
        spec.modes = modes;
        spec.domainGroups = domainGroups;
        spec.rules = rules;

        return spec;
    }

    private AST.Definitions.Def definition() {
        AST.Definitions.Def definition = new AST.Definitions.Def();
        definition.number = getNodeName();

        Token t = sym;
        expect(IDENTIFIER);

        AST.Identifier identifier = new AST.Identifier();
        identifier.number = getNodeName();
        String id = ((TokenWithAttribute<String>) t).getAttribute();
        identifier.identifier = id;

        expect(DEFINER);
        AST.Regex regex = regex();

        Position idPos = t.getCoords().getStarting();
        if (this.defNamesMap.containsKey(id)) {
            Position pos = this.defNamesMap.get(id);
            this.compiler.addError(
                    idPos,
                    "Redefinition! Named expression " + id + " is already defined at " + pos + ".");
        } else {
            this.defNamesMap.put(id, idPos);
        }

        definition.identifier = identifier;
        definition.regex = regex;

        return definition;
    }

    private AST.Modes modesSection() {
        AST.Modes modes = new AST.Modes();
        modes.number = getNodeName();

        expect(MODES_SECTION_MARKER);

        List<AST.Identifier> modeNames = new ArrayList<>();

        while (sym.getTag().equals(IDENTIFIER)) {
            AST.Identifier identifier = new AST.Identifier();
            identifier.number = getNodeName();
            String id = ((TokenWithAttribute<String>) sym).getAttribute();
            identifier.identifier = id;

            Position idPos = sym.getCoords().getStarting();
            if (id.equals("INITIAL")) {
                this.compiler.addWarning(idPos, "INITIAL is default mode's name. No need to explicitly declare it.");
            } else {
                if (this.modeNamesMap.containsKey(id)) {
                    Position declPos = this.modeNamesMap.get(id);
                    this.compiler.addWarning(idPos, "Mode " + id + " has already been declared at " + declPos + ".");
                } else {
                    this.modeNamesMap.put(id, idPos);
                }
            }

            modeNames.add(identifier);
            nextToken();
        }

        modes.modeNames = modeNames;

        return modes;
    }

    private AST.DomainGroups domainsSection() {
        AST.DomainGroups domainGroups = new AST.DomainGroups();
        domainGroups.number = getNodeName();

        List<AST.DomainGroup> domainGroupList = new ArrayList<>();

        do {
            Token t = sym;
            expect(DOMAINS_GROUP_MARKER);
            String type = ((TokenWithAttribute<String>) t).getAttribute();

            AST.DomainGroup domainGroup;
            List<AST.Identifier> identifiers = new ArrayList<>();
            if (type.isEmpty()) {
                domainGroup = new AST.DomainGroup.SimpleDomainGroup();
                domainGroup.number = getNodeName();
            } else {
                domainGroup = new AST.DomainGroup.DomainWithAttributeGroup(type);
                domainGroup.number = getNodeName();
            }

            while (sym.getTag().equals(IDENTIFIER)) {
                AST.Identifier identifier = new AST.Identifier();
                identifier.number = getNodeName();
                String id = ((TokenWithAttribute<String>) sym).getAttribute();
                identifier.identifier = id;
                identifiers.add(identifier);

                Position idPos = sym.getCoords().getStarting();

                if (this.domainNamesMap.containsKey(id)) {
                    Position declPos = this.domainNamesMap.get(id);
                    this.compiler.addError(
                            idPos,
                            "Redeclaration! Domain " + id + " has already been declared at " + declPos + " ."
                    );
                } else {
                    this.domainNamesMap.put(id, idPos);
                }

                nextToken();
            }

            domainGroup.domainNames = identifiers;
            domainGroupList.add(domainGroup);
        } while (sym.getTag().equals(DOMAINS_GROUP_MARKER));

        domainGroups.domainGroups = domainGroupList;
        return domainGroups;
    }

    private AST.Rules.Rule rule() {
        AST.Rules.Rule rule = new AST.Rules.Rule();
        rule.number = getNodeName();

        AST.Rules.ModeList modeList;
        if (sym.getTag().equals(L_ANGLE_BRACKET)) {
            modeList = modeList();
        } else {
            modeList = new AST.Rules.ModeList();
            modeList.number = getNodeName();
            AST.Identifier identifier = new AST.Identifier();
            identifier.number = getNodeName();
            identifier.identifier = "INITIAL";
            modeList.modeNames = new ArrayList<>();
            modeList.modeNames.add(identifier);
        }

        String stateName;
        if (sym.getTag().equals(IDENTIFIER)) {
            String id = ((TokenWithAttribute<String>) sym).getAttribute();
            stateName = id;

            Position idPos = sym.getCoords().getStarting();

            if (id.startsWith("STATE_")) {
                this.compiler.addError(idPos, "Rule names, starting with \"STATE_\" are reserved for the generator.");
            }

            if (this.ruleNamesMap.containsKey(id)) {
                Position declPos = this.ruleNamesMap.get(id);
                this.compiler.addError(idPos, "Rule names must be distinct! Rule name " + id
                        + " already used at " + declPos + ".");
            } else {
                this.ruleNamesMap.put(id, idPos);
            }

            nextToken();
        } else {
            stateName = "STATE_" + stateNames++; // generating name
        }

        expect(DEFINER);

        AST.Regex regex = regex();

        AST.Rules.Rule.Action action;

        if (sym.getTag().equals(ACTION_SWITCH)) {
            String id = ((TokenWithAttribute<String>) sym).getAttribute();
            Position idPos = sym.getCoords().getStarting();
            handleUndeclared(id, idPos, true);

            String modeName = id;
            action = new AST.Rules.Rule.Action.Switch(getNodeName(), modeName);
            nextToken();
        } else if (sym.getTag().equals(ACTION_RETURN)) {
            String id = ((TokenWithAttribute<String>) sym).getAttribute();
            Position idPos = sym.getCoords().getStarting();
            handleUndeclared(id, idPos, false);

            String domainName = id;
            action = new AST.Rules.Rule.Action.Return(getNodeName(), domainName);
            nextToken();
        } else if (sym.getTag().equals(ACTION_SWITCH_RETURN)) {
            StringPair modeDomain = ((TokenWithAttribute<StringPair>) sym).getAttribute();
            String modeName = modeDomain.getFirst();
            String domainName = modeDomain.getSecond();

            int offset = modeName.length() + 1;

            Position idPos = sym.getCoords().getStarting();
            Position idPos2 = new Position(idPos.getLine(), idPos.getPos() + offset, idPos.getIndex() + offset);
            handleUndeclared(modeName, idPos, true);
            handleUndeclared(domainName, idPos2, false);

            action = new AST.Rules.Rule.Action.SwitchReturn(getNodeName(), modeName, domainName);
            nextToken();
        } else if (sym.getTag().equals(IDENTIFIER)) {
            String actionFuncName = ((TokenWithAttribute<String>) sym).getAttribute();
            action = new AST.Rules.Rule.Action.Call(getNodeName(), actionFuncName);
            nextToken();
        } else {
            action = new AST.Rules.Rule.Action.Ignore(getNodeName());
        }

        expect(RULE_END);

        rule.modeList = modeList;
        rule.stateName = stateName;
        rule.regex = regex;
        rule.action = action;

        return rule;
    }

    private AST.Rules.ModeList modeList() {
        AST.Rules.ModeList modeList = new AST.Rules.ModeList();
        modeList.number = getNodeName();

        List<AST.Identifier> modeNames = new ArrayList<>();

        expect(L_ANGLE_BRACKET);

        Token t = sym;
        expect(IDENTIFIER);
        String id = ((TokenWithAttribute<String>) t).getAttribute();
        String modeName = id;

        Position idPos = t.getCoords().getStarting();
        handleUndeclared(id, idPos, true);

        AST.Identifier identifier = new AST.Identifier();
        identifier.number = getNodeName();
        identifier.identifier = modeName;
        modeNames.add(identifier);

        while (sym.getTag().equals(COMMA)) {
            nextToken();
            t = sym;
            expect(IDENTIFIER);
            modeName = ((TokenWithAttribute<String>) t).getAttribute();
            identifier = new AST.Identifier();
            identifier.number = getNodeName();
            identifier.identifier = modeName;
            modeNames.add(identifier);
        }

        expect(R_ANGLE_BRACKET);

        modeList.modeNames = modeNames;

        return modeList;
    }

    private AST.Regex regex() {
        AST.Regex cRegex = cRegex();

        if (sym.getTag().equals(UNION_OP)) {
            List<AST.Regex> regexes = new ArrayList<>();
            regexes.add(cRegex);
            while (sym.getTag().equals(UNION_OP)) {
                nextToken();
                regexes.add(cRegex());
            }
            AST.Regex.Union union = new AST.Regex.Union();
            union.number = getNodeName();
            union.operands = regexes;
            return union;
        } else {
            return cRegex;
        }
    }

    private AST.Regex cRegex() {
        AST.Regex fRegex = fRegex();

        if (fRegexFirst.contains(sym.getTag())) {
            List<AST.Regex> regexes = new ArrayList<>();
            regexes.add(fRegex);
            while (fRegexFirst.contains(sym.getTag())) {
                regexes.add(fRegex());
            }
            AST.Regex.Concatenation concatenation = new AST.Regex.Concatenation();
            concatenation.number = getNodeName();
            concatenation.operands = regexes;
            return concatenation;
        } else {
            return fRegex;
        }
    }

    private AST.Regex fRegex() {
        AST.Regex lRegex = lRegex();

        if (unaryOperators.contains(sym.getTag())) {
            if (sym.getTag().equals(POS_ITERATION_OP)) {
                nextToken();

                AST.Regex.PosIteration result = new AST.Regex.PosIteration();
                result.number = getNodeName();
                result.a = lRegex;
                return result;
            } else if (sym.getTag().equals(ITERATION_OP)) {
                nextToken();

                AST.Regex.Iteration result = new AST.Regex.Iteration();
                result.number = getNodeName();
                result.a = lRegex;
                return result;
            } else if (sym.getTag().equals(OPTION_OP)) {
                nextToken();

                AST.Regex.Option result = new AST.Regex.Option();
                result.number = getNodeName();
                result.a = lRegex;
                return result;
            } else {
                IntPair attribute = ((TokenWithAttribute<IntPair>) sym).getAttribute();
                Position pos = sym.getCoords().getStarting();
                nextToken();

                AST.Regex.Repetition result = new AST.Regex.Repetition();
                result.number = getNodeName();
                result.a = lRegex;
                int from = attribute.getFirst();
                int to = attribute.getSecond();

                if (to >= 0) {
                    if (from > to) {
                        this.compiler.addError(
                                pos,
                                "Repetition lower bound (" + from + ") greater than upper bound (" + to + ")!");
                    }
                }

                result.from = from;
                result.to = to;

                return result;
            }
        } else {
            return lRegex;
        }
    }

    private AST.Regex lRegex() {
        if (sym.getTag().equals(CHAR)) {
            AST.Regex.Char result = new AST.Regex.Char();
            result.number = getNodeName();

            int c = ((TokenWithAttribute<Integer>) sym).getAttribute();
            Position cPos = sym.getCoords().getStarting();
            handleChar(c, cPos);

            result.codePoint = c;
            nextToken();
            return result;
        } else if (sym.getTag().equals(DOT)) {
            AST.Regex.Dot result = new AST.Regex.Dot();
            result.number = getNodeName();
            nextToken();
            return result;
        } else if (sym.getTag().equals(EOF)) {
            AST.Regex.Eof result = new AST.Regex.Eof();
            result.number = getNodeName();
            nextToken();
            return result;
        } else if (sym.getTag().equals(NAMED_EXPR)) {
            AST.Regex.NamedExpr result = new AST.Regex.NamedExpr();
            result.number = getNodeName();
            String id = ((TokenWithAttribute<String>) sym).getAttribute();
            result.name = id;

            if (!this.defNamesMap.containsKey(id)) {
                this.compiler.addError(sym.getCoords().getStarting(), "Named expression " + id + " is undefined.");
            }

            nextToken();
            return result;
        } else if (sym.getTag().equals(CHAR_CLASS_OPEN)) {
            return charClassExpression();
        } else {
            expect(LPAREN);

            AST.Regex regex = regex();

            expect(RPAREN);

            return regex;
        }
    }

    private AST.Regex charClassExpression() {
        AST.Regex.CharClass charClass = charClass();

        if (sym.getTag().equals(CLASS_MINUS_OP)) {
            nextToken();
            AST.Regex.CharClass charClass2 = charClass();
            AST.Regex.CharClassDiff result = new AST.Regex.CharClassDiff();
            result.number = getNodeName();
            result.a = charClass;
            result.b = charClass2;
            return result;
        } else {
            return charClass;
        }
    }

    private AST.Regex.CharClass charClass() {
        AST.Regex.CharClass result = new AST.Regex.CharClass();
        result.number = getNodeName();

        expect(CHAR_CLASS_OPEN);

        boolean exclusive = false;
        if (sym.getTag().equals(CHAR_CLASS_NEG)) {
            nextToken();
            exclusive = true;
        }
        result.exclusive = exclusive;

        List<AST.Regex.CharOrRange> charOrRangeList = new ArrayList<>();

        do {
            Token t = sym;
            expect(CHAR);
            int cpa = ((TokenWithAttribute<Integer>) t).getAttribute();

            Position cPosA = t.getCoords().getStarting();
            handleChar(cpa, cPosA);

            if (sym.getTag().equals(CHAR_CLASS_RANGE_OP)) {
                nextToken();
                t = sym;
                expect(CHAR);
                int cpb = ((TokenWithAttribute<Integer>) t).getAttribute();

                Position cPosB = t.getCoords().getStarting();
                handleChar(cpb, cPosB);

                if (cpa > cpb) {
                    this.compiler.addError(cPosA, "Impossible range! Range start greater than end.");
                } else if (cpa == cpb) {
                    this.compiler.addWarning(cPosA, "Range contains only one code point. " +
                            "There's a shorter way to write it :)");
                }

                AST.Regex.CharClass.CharOrRange.Range range = new AST.Regex.CharOrRange.Range();
                range.number = getNodeName();
                range.cpa = cpa;
                range.cpb = cpb;

                charOrRangeList.add(range);
            } else {
                AST.Regex.CharClass.CharOrRange.ClassChar c = new AST.Regex.CharOrRange.ClassChar();
                c.number = getNodeName();
                c.codePoint = cpa;
                charOrRangeList.add(c);
            }
        } while (sym.getTag().equals(CHAR));

        result.charsOrRanges = charOrRangeList;

        expect(CHAR_CLASS_CLOSE);

        return result;
    }

    private void nextToken() {
        if (this.tokens.hasNext()) {
            this.sym = this.tokens.next();
        }
    }

    private int getNodeName() {
        return nodeNames++;
    }

    private void handleUndeclared(String id, Position idPos, boolean isMode) {
        Map<String, Position> declMap = this.domainNamesMap;
        String type = "Domain";
        boolean extra = true;
        if (isMode) {
            declMap = this.modeNamesMap;
            type = "Mode";
            extra = !id.equals("INITIAL");
        }
        if (!declMap.containsKey(id)) {
            if (extra) {
                this.compiler.addError(idPos, type + " " + id
                        + " has not been declared. Is there a spelling error?");
            }
        }
    }

    private void handleChar(int c, Position cPos) {
        int maxCodePoint = this.compiler.getAlphabetSize() - 2;
        if (!Utility.isInRange(c, 0, maxCodePoint)) {
            this.compiler.addError(cPos,
                    "Invalid code point value for char: " + c + ". Valid range is [0," + maxCodePoint + "].");
        }
    }

    private void expect(Domain tag) {
        if (this.sym.getTag() == tag) {
            nextToken();
        } else {
            String message = "Syntax error at token " + this.sym +
                    ". Expected " + tag + ", got " + this.sym.getTag() + "!";
            compiler.addError(this.sym.getCoords().getStarting(), message);
            throw new IllegalStateException(message);
        }
    }
}
