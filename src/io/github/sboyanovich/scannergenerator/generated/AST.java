package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.automata.NFA;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public abstract class AST {

    // sealing class
    private AST() {

    }

    int number;

    abstract StringBuilder dotVisit();

    static StringBuilder labelNode(int number, String label) {
        StringBuilder result = new StringBuilder();
        result.append(TAB).append(number).append(SPACE + "[label=\"").append(label).append("\"]" + NEWLINE);
        return result;
    }

    static StringBuilder edgeString(int from, int to) {
        StringBuilder result = new StringBuilder();
        result.append(TAB)
                .append(from)
                .append(SPACE + DOT_ARROW + SPACE)
                .append(to)
                .append(NEWLINE);
        return result;
    }

    public final int getNumber() {
        return this.number;
    }

    final String toGraphVizDotString() {
        return ("digraph specification {" + NEWLINE +
                TAB + "rankdir=LR;" + NEWLINE) +
                dotVisit() +
                "}" + NEWLINE;
    }

    public static class Spec extends AST {
        Definitions definitions;
        Modes modes;
        DomainGroups domainGroups;
        Rules rules;

        @Override
        StringBuilder dotVisit() {
            StringBuilder result = new StringBuilder();

            result.append(AST.labelNode(number, "Specification"));

            result.append(AST.edgeString(number, definitions.number));
            result.append(AST.edgeString(number, modes.number));
            result.append(AST.edgeString(number, domainGroups.number));
            result.append(AST.edgeString(number, rules.number));

            result.append(definitions.dotVisit());
            result.append(modes.dotVisit());
            result.append(domainGroups.dotVisit());
            result.append(rules.dotVisit());

            return result;
        }
    }

    public static class Identifier extends AST {
        String identifier;

        @Override
        StringBuilder dotVisit() {
            StringBuilder result = new StringBuilder();

            result.append(AST.labelNode(number, identifier));

            return result;
        }
    }

    public static class Definitions extends AST {

        List<Def> definitions;

        @Override
        StringBuilder dotVisit() {
            StringBuilder result = new StringBuilder();

            result.append(AST.labelNode(number, "Definitions"));

            for (Def definition : definitions) {
                result.append(AST.edgeString(number, definition.number));
            }

            for (Def definition : definitions) {
                result.append(definition.dotVisit());
            }

            return result;
        }

        public static class Def extends AST {
            Identifier identifier;
            Regex regex;

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, "Def"));

                result.append(AST.edgeString(number, identifier.number));
                result.append(AST.edgeString(number, regex.number));

                result.append(identifier.dotVisit());
                result.append(regex.dotVisit());

                return result;
            }

        }
    }

    public abstract static class Regex extends AST {
        // sealed
        private Regex() {
        }

        public Set<Integer> getPivots(Map<String, Set<Integer>> defPivots, int alphabetSize) {
            HashSet<Integer> pivots = new HashSet<>();
            collectPivots(defPivots, alphabetSize, pivots);
            return pivots;
        }

        abstract void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots);

        abstract NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize);

        public static class Union extends Regex {
            List<Regex> operands;

            @Override
            void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots) {
                for (Regex regex : operands) {
                    regex.collectPivots(defPivots, alphabetSize, pivots);
                }
            }

            @Override
            NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize) {
                List<NFA> nfas = new ArrayList<>();
                for (Regex operand : operands) {
                    nfas.add(operand.buildNFA(namedExpressions, alphabetSize));
                }
                return NFA.unionAll(nfas);
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, "|"));

                for (Regex operand : operands) {
                    result.append(AST.edgeString(number, operand.number));
                }

                for (Regex operand : operands) {
                    result.append(operand.dotVisit());
                }

                return result;
            }
        }

        public static class Concatenation extends Regex {
            List<Regex> operands;

            @Override
            void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots) {
                for (Regex regex : operands) {
                    regex.collectPivots(defPivots, alphabetSize, pivots);
                }
            }

            @Override
            NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize) {
                List<NFA> nfas = new ArrayList<>();
                List<Integer> chars = new ArrayList<>();

                int state = 0;
                for (Regex operand : operands) {
                    if (state == 0) {
                        if (operand instanceof Char) {
                            chars.clear();
                            chars.add(((Char) operand).codePoint);
                            state = 1;
                        } else {
                            nfas.add(operand.buildNFA(namedExpressions, alphabetSize));
                        }
                    } else {
                        if (operand instanceof Char) {
                            chars.add(((Char) operand).codePoint);
                        } else {
                            nfas.add(NFA.acceptsThisWord(alphabetSize, chars));
                            nfas.add(operand.buildNFA(namedExpressions, alphabetSize));
                            state = 0;
                        }
                    }
                }
                if (state == 1) {
                    nfas.add(NFA.acceptsThisWord(alphabetSize, chars));
                }
                return NFA.concatenationAll(nfas);
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, ":"));

                for (Regex operand : operands) {
                    result.append(AST.edgeString(number, operand.number));
                }

                for (Regex operand : operands) {
                    result.append(operand.dotVisit());
                }

                return result;
            }

        }

        public static class Iteration extends Regex {
            Regex a;

            @Override
            void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots) {
                a.collectPivots(defPivots, alphabetSize, pivots);
            }

            @Override
            NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize) {
                return a.buildNFA(namedExpressions, alphabetSize).iteration();
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, "*"));

                result.append(AST.edgeString(number, a.number));

                result.append(a.dotVisit());

                return result;
            }
        }

        public static class PosIteration extends Regex {
            Regex a;

            @Override
            void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots) {
                a.collectPivots(defPivots, alphabetSize, pivots);
            }

            @Override
            NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize) {
                return a.buildNFA(namedExpressions, alphabetSize).positiveIteration();
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, "+"));

                result.append(AST.edgeString(number, a.number));

                result.append(a.dotVisit());

                return result;
            }
        }

        public static class Option extends Regex {
            Regex a;

            @Override
            void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots) {
                a.collectPivots(defPivots, alphabetSize, pivots);
            }

            @Override
            NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize) {
                return a.buildNFA(namedExpressions, alphabetSize).optional();
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, "?"));

                result.append(AST.edgeString(number, a.number));

                result.append(a.dotVisit());

                return result;
            }
        }

        public static class Repetition extends Regex {
            Regex a;
            int from;
            int to;

            @Override
            void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots) {
                a.collectPivots(defPivots, alphabetSize, pivots);
            }

            @Override
            NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize) {
                NFA aNFA = a.buildNFA(namedExpressions, alphabetSize);
                NFA result = aNFA.power(from);

                if (to == -1) {
                    result = result.concatenation(aNFA.iteration());
                } else if (to > from) {
                    List<NFA> degrees = new ArrayList<>();
                    for (int i = 0; i <= to - from; i++) {
                        degrees.add(aNFA.power(i));
                    }
                    result = result.concatenation(NFA.unionAll(degrees));
                }
                return result;
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                String s;

                if (to == -1) {
                    s = from + "+";
                } else {
                    if (from == to) {
                        s = from + "";
                    } else {
                        s = from + "-" + to;
                    }
                }

                result.append(AST.labelNode(number, "Repeat " + s));

                result.append(AST.edgeString(number, a.number));

                result.append(a.dotVisit());

                return result;
            }
        }

        public static class Char extends Regex {
            int codePoint;

            @Override
            void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots) {
                pivots.add(codePoint);
            }

            @Override
            NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize) {
                return NFA.singleLetterLanguage(alphabetSize, codePoint);
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, String.valueOf(codePoint)));

                return result;
            }
        }

        public static class Dot extends Regex {

            @Override
            void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots) {
                // we need to include EOF here as well, as DOT is essentially a symbol class
                // prohibiting \n and EOF
                pivots.add(asCodePoint("\n"));
                pivots.add(alphabetSize - 1);
            }

            @Override
            NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize) {
                // fixed the logic here, EOF must not be matched
                return NFA.acceptsAllCodePointsButThese(alphabetSize, Set.of(asCodePoint("\n"), alphabetSize - 1));
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, "."));

                return result;
            }
        }

        public static class Eof extends Regex {
            @Override
            void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots) {
                pivots.add(alphabetSize - 1);
            }

            @Override
            StringBuilder dotVisit() {
                return AST.labelNode(number, "<<EOF>>");
            }

            @Override
            NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize) {
                return NFA.singleLetterLanguage(alphabetSize, alphabetSize - 1);
            }
        }

        public static class NamedExpr extends Regex {
            String name;

            @Override
            void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots) {
                pivots.addAll(defPivots.get(name));
            }

            @Override
            NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize) {
                return namedExpressions.get(name);
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, name));

                return result;
            }
        }

        public static class CharClass extends Regex {
            boolean exclusive;
            List<CharOrRange> charsOrRanges;

            @Override
            void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots) {
                for (CharOrRange cor : charsOrRanges) {
                    if (cor instanceof CharOrRange.ClassChar) {
                        pivots.add(((CharOrRange.ClassChar) cor).codePoint);
                    } else if (cor instanceof CharOrRange.Range) {
                        CharOrRange.Range range = (CharOrRange.Range) cor;
                        pivots.add(range.cpa);
                        pivots.add(range.cpb);
                    }
                }
                if (exclusive) {
                    pivots.add(alphabetSize - 1); // exclusive class implicitly prohibits EOF/AEOI
                }
            }

            boolean containsCodePoint(int codePoint, int alphabetSize) {
                if (codePoint == alphabetSize - 1) {
                    return false;
                }
                if (exclusive) {
                    for (var cor : charsOrRanges) {
                        if (cor.containsCodePoint(codePoint)) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    for (var cor : charsOrRanges) {
                        if (cor.containsCodePoint(codePoint)) {
                            return true;
                        }
                    }
                    return false;
                }
            }

            @Override
            NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize) {
                Instant start = null, end = null;
                if (RecognizerGenTest.DEBUG_PROFILE) {
                    start = Instant.now();
                }
                NFA result;
                Set<Integer> codePoints = new HashSet<>();
                for (var cor : charsOrRanges) {
                    codePoints.addAll(cor.getCodePoints());
                }

                if (exclusive) {
                    int aeoi = alphabetSize - 1;
                    codePoints.add(aeoi); // to disallow EOF (AEOI)
                    result = NFA.acceptsAllCodePointsButThese(alphabetSize, codePoints);
                } else {
                    result = NFA.acceptsAllTheseCodePoints(alphabetSize, codePoints);
                }
                if (RecognizerGenTest.DEBUG_PROFILE) {
                    end = Instant.now();
                    RecognizerGenTest.timeBuildingCharClasses += Duration.between(start, end).toNanos();
                }
                return result;
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                String label = "[]";
                if (exclusive) {
                    label = "[^]";
                }

                result.append(AST.labelNode(number, label));

                for (CharOrRange cr : charsOrRanges) {
                    result.append(AST.edgeString(number, cr.number));
                }

                for (CharOrRange cr : charsOrRanges) {
                    result.append(cr.dotVisit());
                }

                return result;
            }
        }

        public abstract static class CharOrRange extends AST {
            // sealed
            private CharOrRange() {
            }

            abstract Set<Integer> getCodePoints();

            abstract boolean containsCodePoint(int codePoint);

            public static class ClassChar extends CharOrRange {
                int codePoint;

                @Override
                boolean containsCodePoint(int codePoint) {
                    return this.codePoint == codePoint;
                }

                @Override
                Set<Integer> getCodePoints() {
                    return Set.of(codePoint);
                }

                @Override
                StringBuilder dotVisit() {
                    StringBuilder result = new StringBuilder();

                    result.append(AST.labelNode(number, String.valueOf(codePoint)));

                    return result;
                }
            }

            public static class Range extends CharOrRange {
                int cpa;
                int cpb;

                @Override
                boolean containsCodePoint(int codePoint) {
                    return cpa <= codePoint && codePoint <= cpb;
                }

                @Override
                Set<Integer> getCodePoints() {
                    Set<Integer> result = new HashSet<>();
                    for (int i = cpa; i <= cpb; i++) {
                        result.add(i);
                    }
                    return result;
                }

                @Override
                StringBuilder dotVisit() {
                    StringBuilder result = new StringBuilder();

                    result.append(AST.labelNode(number, cpa + "-" + cpb));

                    return result;
                }
            }
        }

        public static class CharClassDiff extends Regex {
            CharClass a;
            CharClass b;

            @Override
            void collectPivots(Map<String, Set<Integer>> defPivots, int alphabetSize, HashSet<Integer> pivots) {
                a.collectPivots(defPivots, alphabetSize, pivots);
                b.collectPivots(defPivots, alphabetSize, pivots);
            }

            @Override
            NFA buildNFA(Map<String, NFA> namedExpressions, int alphabetSize) {
                Set<Integer> codePoints = new HashSet<>();
                for (int i = 0; i < alphabetSize; i++) {
                    if (a.containsCodePoint(i, alphabetSize) && !b.containsCodePoint(i, alphabetSize)) {
                        codePoints.add(i);
                    }
                }
                return NFA.acceptsAllTheseCodePoints(alphabetSize, codePoints);
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, "{-}"));

                result.append(AST.edgeString(number, a.number));
                result.append(AST.edgeString(number, b.number));

                result.append(a.dotVisit());
                result.append(b.dotVisit());

                return result;
            }
        }

    }

    public static class Modes extends AST {
        List<Identifier> modeNames;

        @Override
        StringBuilder dotVisit() {
            StringBuilder result = new StringBuilder();

            result.append(AST.labelNode(number, "Modes"));

            for (Identifier id : modeNames) {
                result.append(AST.edgeString(number, id.number));
            }

            for (Identifier id : modeNames) {
                result.append(id.dotVisit());
            }

            return result;
        }

    }

    public abstract static class DomainGroup extends AST {
        // sealed
        private DomainGroup() {
        }

        List<Identifier> domainNames;

        List<String> getDomainNames() {
            List<String> result = new ArrayList<>();
            for (Identifier id : domainNames) {
                result.add(id.identifier);
            }
            return result;
        }

        public static class SimpleDomainGroup extends DomainGroup {

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, "#NoAttribute"));

                for (Identifier name : domainNames) {
                    result.append(AST.edgeString(number, name.number));
                }

                for (Identifier name : domainNames) {
                    result.append(name.dotVisit());
                }

                return result;
            }
        }

        public static class DomainWithAttributeGroup extends DomainGroup {
            String attributeType;

            public DomainWithAttributeGroup(String attributeType) {
                this.attributeType = attributeType;
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, attributeType));

                for (Identifier name : domainNames) {
                    result.append(AST.edgeString(number, name.number));
                }

                for (Identifier name : domainNames) {
                    result.append(name.dotVisit());
                }

                return result;
            }
        }
    }

    public static class DomainGroups extends AST {
        List<DomainGroup> domainGroups;

        @Override
        StringBuilder dotVisit() {
            StringBuilder result = new StringBuilder();

            result.append(AST.labelNode(number, "DomainGroups"));

            for (DomainGroup group : domainGroups) {
                result.append(AST.edgeString(number, group.number));
            }

            for (DomainGroup group : domainGroups) {
                result.append(group.dotVisit());
            }

            return result;
        }
    }

    public static class Rules extends AST {

        List<Rule> rules;

        @Override
        StringBuilder dotVisit() {
            StringBuilder result = new StringBuilder();

            result.append(AST.labelNode(number, "Rules"));

            for (Rule rule : rules) {
                result.append(AST.edgeString(number, rule.number));
            }

            for (Rule rule : rules) {
                result.append(rule.dotVisit());
            }

            return result;
        }

        public static class ModeList extends AST {
            List<Identifier> modeNames;

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, "ModeList"));

                for (Identifier name : modeNames) {
                    result.append(AST.edgeString(number, name.number));
                }

                for (Identifier name : modeNames) {
                    result.append(name.dotVisit());
                }

                return result;
            }

        }

        public static class Rule extends AST {
            ModeList modeList;
            String stateName;
            Regex regex;
            Action action;

            public static abstract class Action extends AST {
                private Action() {
                }

                public static class Ignore extends Action {
                    public Ignore(int number) {
                        this.number = number;
                    }

                    @Override
                    StringBuilder dotVisit() {
                        return new StringBuilder(AST.labelNode(number, "@@Ignore"));
                    }
                }

                public static class Switch extends Action {
                    String modeName;

                    public Switch(int number, String modeName) {
                        this.number = number;
                        this.modeName = modeName;
                    }

                    @Override
                    StringBuilder dotVisit() {
                        return new StringBuilder(AST.labelNode(number, "@" + modeName));
                    }
                }

                public static class Return extends Action {
                    String domainName;

                    public Return(int number, String domainName) {
                        this.number = number;
                        this.domainName = domainName;
                    }

                    @Override
                    StringBuilder dotVisit() {
                        return new StringBuilder(AST.labelNode(number, "#" + domainName));
                    }
                }

                public static class SwitchReturn extends Action {
                    String modeName;
                    String domainName;

                    public SwitchReturn(int number, String modeName, String domainName) {
                        this.number = number;
                        this.modeName = modeName;
                        this.domainName = domainName;
                    }

                    @Override
                    StringBuilder dotVisit() {
                        return new StringBuilder(AST.labelNode(number, "@" + modeName + "#" + domainName));
                    }
                }

                public static class Call extends Action {
                    String funcName;

                    public Call(int number, String funcName) {
                        this.number = number;
                        this.funcName = funcName;
                    }

                    @Override
                    StringBuilder dotVisit() {
                        return new StringBuilder(AST.labelNode(number, "@@Call " + funcName));
                    }
                }
            }

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, stateName));

                result.append(AST.edgeString(number, modeList.number));
                result.append(AST.edgeString(number, regex.number));
                result.append(AST.edgeString(number, action.number));

                result.append(modeList.dotVisit());
                result.append(regex.dotVisit());
                result.append(action.dotVisit());

                return result;
            }
        }
    }
}
