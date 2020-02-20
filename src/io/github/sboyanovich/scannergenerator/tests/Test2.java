package io.github.sboyanovich.scannergenerator.tests;

import static io.github.sboyanovich.scannergenerator.utility.Utility.asString;

public class Test2 {
    public static void main(String[] args) {
        int cntLetter = 0;
        int cntLetterOrDigit = 0;
        for (int i = 0; i < Character.MAX_CODE_POINT + 1; i++) {
            if (Character.isJavaIdentifierStart(i)) {
                cntLetter++;
            }
            if (Character.isJavaIdentifierPart(i)) {
                cntLetterOrDigit++;
            }
            if (Character.isJavaIdentifierStart(i)) {
                System.out.println(i + ": " + asString(i));
            }
            int a = 0;
        }
        
        System.out.println("JavaLetters: " + cntLetter);
        System.out.println("JavaLetterOrDigits: " + cntLetterOrDigit);
    }
}
