package io.github.sboyanovich.scannergenerator.tests;

public class AuxCodeGenerator {
    public static void main(String[] args) {
        String[] keywords = new String[]{
                "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
                "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
                "for", "if", "goto", "implements", "import", "instanceof", "int", "interface", "long", "native",
                "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
                "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
        };

        /*
        for (String kw : keywords) {
            String kwu = kw.toUpperCase();
            String en = "KEYWORD_" + kwu + " {\n" +
                    "        @Override\n" +
                    "        public Domain getDomain() {\n" +
                    "            return KeywordDomains.KEYWORD_" + kwu + ";\n" +
                    "        }\n" +
                    "    },\n";
            System.out.println(en);
        }
        */

        /*
        for (String kw : keywords) {
            String kwu = kw.toUpperCase();
            String en = "NFA kw_" + kw + "NFA = Utility.acceptsThisWord(alphabetSize, \"" + kw + "\")\n" +
                    "                .setAllFinalStatesTo(KEYWORD_" + kwu + ");";
            System.out.println(en);
        }
        */


        /*
        for (String kw : keywords) {
            String kwu = kw.toUpperCase();
            String en = "KEYWORD_" + kwu + ",";
            System.out.println(en);
        }
        */

        for (String kw : keywords) {
            String en = ".union(kw_" + kw + "NFA)";
            System.out.println(en);
        }

    }
}
