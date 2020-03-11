package io.github.sboyanovich.scannergenerator.tests;

import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.HashSet;
import java.util.Set;

public class Test2 {
    public static void main(String[] args) {

        Set<Integer> jis = new HashSet<>();
        Set<Integer> jip = new HashSet<>();

        int cntLetter = 0;
        int cntLetterOrDigit = 0;
        for (int i = 0; i < Character.MAX_CODE_POINT + 1; i++) {
            if (Character.isJavaIdentifierStart(i)) {
                jis.add(i);
            }
            if (Character.isJavaIdentifierPart(i)) {
                jip.add(i);
            }
        }

        var jiss = Utility.compressIntoSegments(jis);
        var jips = Utility.compressIntoSegments(jip);

        StringBuilder s = new StringBuilder();
        s.append("[");
        for(var segment : jips) {
            int a = segment.getFirst();
            int b = segment.getSecond();
            s.append("\\U+#").append(a);
            if(a != b) {
                s.append("-").append("\\U+#").append(b);
            }
        }
        s.append("]");

        System.out.println(s.toString());

/*        String filename = "someInts.reco";
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
        }*/
    }
}
