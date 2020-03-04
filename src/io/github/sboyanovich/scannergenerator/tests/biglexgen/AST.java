package io.github.sboyanovich.scannergenerator.tests.biglexgen;

import java.util.List;

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

        public static class Union extends Regex {
            List<Regex> operands;

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
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, String.valueOf(codePoint)));

                return result;
            }
        }

        public static class Dot extends Regex {
            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, "."));

                return result;
            }
        }

        public static class NamedExpr extends Regex {
            String name;

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
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                String label = "TheseChars";
                if (exclusive) {
                    label = "NotTheseChars";
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

            public static class ClassChar extends CharOrRange {
                int codePoint;

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

        public static class SimpleDomainGroup extends DomainGroup {

            @Override
            StringBuilder dotVisit() {
                StringBuilder result = new StringBuilder();

                result.append(AST.labelNode(number, "NoAttribute"));

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
            Identifier action;

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
