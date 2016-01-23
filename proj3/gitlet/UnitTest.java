package gitlet;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;

import org.junit.Test;

public class UnitTest {
    /**
     * Unit test.
     * Make sure .gitlet direcoty does not exist in current direc.
     * Make sure f.txt is in the curr direc and z.txt is not.
     */
    @Test
    public void initAddRmBrnchCommitTest() {
        File file = new File(".gitlet");
        File f = new File("f.txt");
        if (file.exists()) {
            System.out.println("Remove the .gitlet directory first");
            return;
        }
        if (!f.exists()) {
            System.out.println("Make sure f.txt exists in current directory.");
            return;
        }
        String[] arg = new String[] {"init"};
        Main.main(arg);
        File fil = new File(".gitlet");
        assertEquals(true, fil.exists());
        String[] arg2 = new String[] {"add", "f.txt"};
        Main.main(arg2);
        HashMap add = Main.getAddTo();
        assertEquals(true, add.containsKey(arg2[1]));
        String[] arg3 = new String[] {"commit", "message"};
        Main.main(arg3);
        HashMap add3 = Main.getAddTo();
        assertEquals(true, add3.isEmpty());
        String[] args4 = new String[]{"rm", "f.txt"};
        Main.main(args4);
        HashMap rmv = Main.getRmv();
        assertEquals(false, rmv.isEmpty());
        String[] arg5 = new String[] {"add", "f.txt"};
        Main.main(arg5);
        String[] arg6 = new String[] {"commit", "message"};
        Main.main(arg6);
        String[] arg7 = new String[] {"branch", "bran"};
        Main.main(arg7);
        HashMap branch = Main.getBranch();
        assertEquals(true, branch.containsKey("master"));
        assertEquals(true, branch.containsKey("bran"));
        assertEquals(false, branch.containsKey("nope"));
        String[] arg8 = new String[] {"rm-branch", "master"};
        Main.main(arg8);
        HashMap branch2 = Main.getBranch();
        assertEquals(false, branch2.containsKey("master"));
        String[] arg9 = new String[] {"add", "z.txt"};
        Main.main(arg9);
        HashMap add2 = Main.getAddTo();
        assertEquals(true, add2.isEmpty());
        assertEquals(2, branch.size());
        assertEquals(1, branch2.size());
    }
    
    /** Main method. */
    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(UnitTest.class));
    }
}
