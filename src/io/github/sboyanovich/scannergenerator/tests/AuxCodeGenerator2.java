package io.github.sboyanovich.scannergenerator.tests;

import java.util.List;

public class AuxCodeGenerator2 {
    public static void main(String[] args) {
        String all = "=   >   <   !   ~   ?   :   -> " +
                "==  >=  <=  !=  &&  ||  ++  -- " +
                "+   -   *   /   &   |   ^   %   <<   >>   >>> " +
                "+=  -=  *=  /=  &=  |=  ^=  %=  <<=  >>=  >>>=";
        String[] operators = all.split("[ ]+");

        List<String> names = List.of(
                "ASSIGNMENT",
                "GREATER",
                "LESS",
                "NOT",
                "COMPLEMENT",
                "QUESTION_MARK",
                "COLON",
                "ARROW",
                "EQUALS",
                "GREQ",
                "LEQ",
                "NEQ",
                "AND",
                "OR",
                "INC",
                "DEC",
                "PLUS",
                "MINUS",
                "MUL",
                "DIV",
                "BW_AND",
                "BW_OR",
                "BW_XOR",
                "MOD",
                "LSHIFT",
                "RSHIFT",
                "LOG_RSHIFT",
                "PLUS_EQ",
                "MINUS_EQ",
                "MUL_EQ",
                "DIV_EQ",
                "BW_AND_EQ",
                "BW_OR_EQ",
                "BQ_XOR_EQ",
                "MOD_EQ",
                "LSHIFT_EQ",
                "RSHIFT_EQ",
                "LOG_RSHIFT_EQ"
        );
/*
        for (int i = 0; i < operators.length; i++) {
            System.out.println(names.get(i) + " " + operators[i]);
        }*/

/*        for (int i = 0; i < operators.length; i++) {
            String name = names.get(i);
            String s = name + " {\n" +
                    "        @Override\n" +
                    "        public Token createToken(Text text, Fragment fragment) {\n" +
                    "            return new BasicToken(fragment, " + name + ");\n" +
                    "        }\n" +
                    "    },";
            System.out.println(s);
        }*/
/*        for (int i = 0; i < operators.length; i++) {
            String name = names.get(i);
            String s = name + " {\n" +
                    "        @Override\n" +
                    "        public Domain getDomain() {\n" +
                    "            return Operators." + name + ";\n" +
                    "        }\n" +
                    "    },";
            System.out.println(s);
        }*/

        for (int i = 0; i < operators.length; i++) {
            String name = names.get(i);
            String operator = operators[i];
            String s = ".union(acceptsThisWord(alphabetSize, \"" + operator + "\").setAllFinalStatesTo(OperatorsTags." + name + "))";
            System.out.println(s);
        }

    }
}
