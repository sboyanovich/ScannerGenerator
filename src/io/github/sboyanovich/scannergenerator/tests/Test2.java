package io.github.sboyanovich.scannergenerator.tests;

import java.io.*;
import java.util.List;

public class Test2 {
    public static void main(String[] args) {
/*        int cntLetter = 0;
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

*/
        String filename = "someInts.reco";
        List<Integer> ints = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename))) {
            for (int x : ints) {
                dos.writeInt(x);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(DataInputStream dis = new DataInputStream(new FileInputStream(filename))) {
            while(dis.available() > 0) {
                int x = dis.readInt();
                System.out.println(x);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
