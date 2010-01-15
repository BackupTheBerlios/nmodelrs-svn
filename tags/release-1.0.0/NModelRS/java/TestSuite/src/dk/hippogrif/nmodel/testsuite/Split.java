package dk.hippogrif.nmodel.testsuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Splits NModel test suite file into files of one test case each.
 * Output files are named as input file with suffix _i starting from 1
 * and created in current directory.
 */
public class Split {

    static String[] names(File file) {
        String[] names = new String[2];
        String name = file.getName();
        int i = name.lastIndexOf('.');
        if (i == -1) { i = name.length(); }
        names[0] = name.substring(0, i);
        names[1] = name.substring(i);
        return names;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
        String[] names = names(file);
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        int state = 0;
        int no = 0;
        PrintWriter pw = null;
        for (;;) {
            String line = in.readLine();
            String tline = line.trim();
            if ("TestSuite(".equals(tline)) {
                state = 1;
            }
            else
            if ("TestCase(".equals(tline)) {
                state = 2;
                no++;
                String name = names[0] + "_" + no + names[1];
                pw = new PrintWriter(name);
                pw.println("TestSuite(");
                pw.println(line);
            }
            else
            if ("),".equals(tline) || ")".equals(tline)) {
                if (state == 2) {
                    state = 1;
                    pw.println("    )");
                    pw.println(")");
                    pw.close();
                }
                else
                if (state == 1) {
                    break;
                }
            }
            else {
                pw.println(line);
            }
        }
        System.out.println("Split into "+no+" test cases");
    }
}
