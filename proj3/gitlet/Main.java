package gitlet;

import java.io.Serializable;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.io.BufferedWriter;
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.file.Files;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.DataOutputStream;

/**
 * @author JuKyung Choi
 */
@SuppressWarnings("serial")
public class Main implements Serializable {
    /** Tree containing the commit id and node. */
    private static HashMap<String, Node> tree = new
        HashMap<String, Node>();
    /** ID of the head of the node. */
    private static String head;
    /** String of SHA-1 to be included in next commit. */
    private static HashSet<String> sha1s = new HashSet<String>();
    /** Contains SHA-1 and name. */
    private static HashMap<String, String> rmv = new HashMap<String, String>();
    /** Keeps track of all branches. */
    private static HashMap<String, String> branches = new
        HashMap<String, String>();
    /** Contains SHA-1 and name. */
    private static HashMap<String, String> file = new
        HashMap<String, String>();
    /** Contains name and SHA-1. */
    private static HashMap<String, String> addTo = new
        HashMap<String, String>();
    /** Contains the commit ID history of a branch. */
    private static HashMap<String, ArrayList<String>> brnchHist = new
        HashMap<String, ArrayList<String>>();
    /** Keeps track of the current branch. */
    private static String curBrnch;

    public static void main(String[] args) {
        File check = new File(".gitlet/addTo");
        if (check.exists()) {
            deserObj();
        }
        switch (args[0]) {
        case "init":
            init();
            break;
        case "add":
            if (args.length < 2) {
                System.out.println("Incorrect operands.");
                break;
            } else {
                String file = args[1];
                add(file);
            }
            serObj();
            break;
        case "rm":
            if (args.length < 2) {
                System.out.println("Incorrect operands.");
            } else {
                remove(args[1]);
            }
            break;
        case "commit":
            if (args.length < 2 || args[1].length() == 0) {
                System.out.println("Please enter a commit message.");
            } else {
                commit(args[1]);
            }

            break;
        case "checkout":
            checkout(0, args);
            serObj();
            break;
        case "merge":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                break;
            } else {
                try {
                    merge(args[1]);
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
            }
            break;
        case "log":
            Node temp = null;
            while (!head.equals("pre-init")) {
                temp = tree.get(head);
                System.out.println("===");
                System.out.println("Commit " + head);
                System.out.println(temp._time);
                System.out.println(temp._msg);
                System.out.println();
                head = temp._parent;
            }
            break;
        case "global-log":
            globalLog();
            break;
        case "find":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                find(args[1]);
            }
            break;
        case "status":
            status();
            break;
        case "branch":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                if (branches.get(args[1]) != null) {
                    System.out.println("A branch with that"
                            + " name already exists.");
                } else {
                    branches.put(args[1], head);
                    brnchHist.put(args[1], new ArrayList<String>());
                    brnchHist.get(args[1]).add(head);
                }
            }
            serObj();
            break;
        case "rm-branch":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                if (branches.get(args[1]) == null) {
                    System.out.println("A branch with that"
                    + "name does not exist.");
                } else {
                    rmBranch(args[1]);
                    serObj();
                }
            }
            break;
        case "reset":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
                break;
            }
            if (args[1].length() < 40) {
                int count = 0;
                for (String s : tree.keySet()) {
                    if (args[1].equals(s.substring(0, args[1].length()))) {
                        args[1] = s;
                        count++;
                    }
                    if (count > 1) {
                        System.out.println("Multiple IDs exist.");
                        break;
                    }
                }
            }
            for (String s : tree.get(args[1])._sha.keySet()) {
                File copyFrom = new File(".gitlet/commit" + "/" + s + ".txt");
                File copyTo = new File(tree.get(args[1])._sha.get(s));
                try {
                    Files.copy(copyFrom.toPath(),
                        copyTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (String f : file.values()) {
                if (!tree.get(args[1])._sha.containsValue(f)) {
                    File rem = new File(f);
                    if (rem.exists()) {
                        rem.delete();
                    }
                }
            }
            head = args[1];
            addTo.clear();
            rmv.clear();
            serObj();
            break;
        default:
            System.out.println("Incorrect operands.");
            break;
        }
    }

    private static void checkout(int i, String[] args) {
        if (args.length < 2) {
            System.out.println("Incorrect operands.");
            return;
        } else if (args.length == 4 && args[2].equals("--")) {
            int count = 0;
            if (args[1].length() < 40) {
                for (String s : tree.keySet()) {
                    if (args[1].equals(s.substring(0, args[1].length()))) {
                        args[1] = s;
                        count++;
                    }
                    if (count > 1) {
                        System.out.println("Multiple IDs exist.");
                        return;
                    }
                }
            }
            checkout(args[1], args[3]);
        } else if (args.length == 3) {
            checkout(head, args[2]);
        } else if (args.length == 2) {
            if (!branches.containsKey(args[1])) {
                System.out.println("No such branch exists.");
                return;
            }
            if (head.equals(args[1])) {
                System.out.println("No need to"
                        + " checkout the current branch.");
            }
            curBrnch = args[1];
            checkout(args[1]);
        } else {
            System.out.println("Incorrect operands.");
            return;
        }
    }

    /** Status of gitlet. */
    private static void status() {
        Object[] keys = branches.keySet().toArray();
            Arrays.sort(keys);
            System.out.println("=== Branches ===");
            for (Object branch : keys) {
                if (curBrnch.equals(branch)) {
                    System.out.println("*" + branch);
                } else {
                    System.out.println(branch);
                }
            }
            System.out.println();
            System.out.println("=== Staged Files ===");
            Object[] staged = addTo.keySet().toArray();
            Arrays.sort(staged);
            for (Object stage : staged) {
                System.out.println(stage);
            }
            System.out.println();
            System.out.println("=== Removed Files ===");
            Object[] removed = rmv.values().toArray();
            Arrays.sort(removed);
            for (Object rmv : removed) {
                System.out.println(rmv);
            }
            System.out.println();
            System.out.println("=== Modifications Not Staged"
                + " For Commit ===");
            System.out.println();
            System.out.println("=== Untracked Files ===");
    }

    /** Adds file of NAME to staging area. */
    private static void add(String name) {
        if (!(new File(name)).exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String sha = sha1(new File(name));
        if (rmv.containsKey(sha)) {
            rmv.remove(sha);
            return;
        }
        for (String key : file.keySet()) {
            if (file.get(key).equals(name)) {
                sha1s.add(key);
            }
        }
        if (tree.get(head)._sha.get(sha) == null) {
            addTo.put(name, sha);
            serObj();
        } else {
            return;
        }
    }

    /** Merges branches. */
    private static void merge(String branch) throws
        FileNotFoundException, IOException {

        String split = splitNode(curBrnch, branch);
        String branchHead = branches.get(branch);
        if (branchHead.equals(split)) {
            return;
        } else if (head.equals(split)) {
            head = branchHead;
            return;
        }

        HashMap<String, String>[] curChan = branchFiles(head, split);
        HashMap<String, String>[] branChan =
            branchFiles(branchHead, split);

        for (String s : branChan[0].keySet()) {
            if (curChan[2].containsKey(s)) {
                checkout(branchHead, s);
                add(s);
            }
        }
        for (String s : branChan[1].keySet()) {
            if (!curChan[1].containsKey(s)) {
                checkout(branchHead, s);
                add(s);
            }
        }
        for (String s : curChan[2].keySet()) {
            if (!branChan[0].containsKey(s)
                && !branChan[1].containsKey(s) && !branChan[2].containsKey(s)) {
                remove(s);
            }
        }
        int conflict = 0;
        for (String s : curChan[0].keySet()) {
            if (branChan[0].containsKey(s) || branChan[3].containsKey(s)) {
                if (branChan[0].get(s) != null
                    && branChan[0].get(s).equals(curChan[0].get(s))) {
                    continue;
                }
                File to = new File(s);
                File cur = new File(curChan[0].get(s));
                String ext = getFileExtension(to);
                File curFile = new File(".gitlet/commit" + "/" + cur + "." + ext);

                to.delete();

                File newFile = new File(s);
                newFile.createNewFile();
                FileWriter fstream = new FileWriter("a.txt", true);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("<<<<<<< HEAD");
                appendFile.append(out, curFile);
                out.write("=======");

                if (branChan[0].containsKey(s)) {
                    File branFile = new File(".gitlet/commit/"
                        + branChan[0].get(s) + "." + ext);
                    appendFile.append(out, branFile);
                }
                out.write(">>>>>>>");
                out.close();
            }
            conflict++;
        }
        for (String s : curChan[3].keySet()) {
            if (branChan[0].containsKey(s) || branChan[3].containsKey(s)) {
                if (branChan[3].containsKey(s)) {
                    continue;
                }
                File to = new File(s);
                to.delete();
                File newFile = new File(s);
                String ext = getFileExtension(newFile);
                newFile.createNewFile();
                FileWriter fstream = new FileWriter("a.txt", true);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write("<<<<<<< HEAD");
                out.write("=======");

                if (branChan[0].containsKey(s)) {
                    File branFile = new File(".gitlet/commit/"
                        + branChan[0].get(s) + "." + ext);
                    appendFile.append(out, branFile);
                }
                out.write(">>>>>>>");
                out.close();
            }
            conflict++;
        }
        if (conflict != 0) {
            System.out.println("Encountered a merge conflict.");
        } else {
            commit("Merged " + branch + " with " + curBrnch);
            System.out.println("commited");
        }
    }

    /** Finds the changed, unchanged, and new files in COMMIT from SPLIT. */
    private static HashMap<String, String>[]
        branchFiles(String commit, String split) {
        @SuppressWarnings("unchecked")
        HashMap<String, String>[] arr = new HashMap[4];
        arr[0] = new HashMap<String, String>();
        arr[1] = new HashMap<String, String>();
        arr[2] = new HashMap<String, String>();
        arr[3] = new HashMap<String, String>();

        for (String s : tree.get(split)._sha.values()) {
            if (!tree.get(commit)._sha.containsValue(s))
                arr[3].put(s, s);
        }
        for (String s : tree.get(commit)._sha.keySet()) {
            String fileName = tree.get(split)._sha.get(s);
            if (fileName == null) {
                String inCom = tree.get(commit)._sha.get(s);
                if (tree.get(split)._sha.containsValue(inCom)) {
                    arr[0].put(inCom, s);
                } else {
                    arr[1].put(inCom, s);
                }

            } else {
                arr[2].put(tree.get(commit)._sha.get(s), s);
            }
        }
        return arr;
    }

    /** Return the ID of split node of CUR and branch BRN. */
    private static String splitNode(String cur, String brn) {
        ArrayList<String> copy = new ArrayList<String>(brnchHist.get(brn));
        copy.retainAll(brnchHist.get(cur));
        return copy.get(copy.size() - 1);
    }

    /** Removes branch BRANCH. */
    private static void rmBranch(String branch) {
        branches.remove(branch);
    }

    /** Finds MSG. */
    private static void find(String msg) {
        int count = 0;
        for (String id : tree.keySet()) {
            if (tree.get(id)._msg.equals(msg)) {
                System.out.println(id);
                count++;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** Log of all commits. */
    private static void globalLog() {
        for (String id : tree.keySet()) {
            System.out.println("===");
            System.out.println("Commit " + id);
            System.out.println(tree.get(id)._time);
            System.out.println(tree.get(id)._msg);
            System.out.println();
        }
    }

    /** Removes FILE. */
    private static void remove(String file) {
        HashMap<String, String> sha = tree.get(head)._sha;
        if (addTo.containsKey(file)) {
            addTo.remove(file);
            if (sha.containsValue(file)) {
                File f = new File(file);
                f.delete();
            }
            serObj();
            return;
        }

        if (!sha.containsValue(file)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        for (String s : sha.keySet()) {
            if (sha.get(s).equals(file)) {
                File f = new File(file);
                f.delete();
                rmv.put(s, sha.get(s));
            }
        }
        serObj();
    }

    /** Commits with string MSG. */
    private static void commit(String msg) {
        File copyFrom = null;
        File copyTo = null;
        if (addTo.isEmpty() && rmv.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (!addTo.isEmpty()) {
            for (String key : addTo.keySet()) {
                copyFrom = new File(key);
                String ext = getFileExtension(copyFrom);
                copyTo = new File(".gitlet/commit" + "/"
                    + addTo.get(key) + "." + ext);
                file.put(addTo.get(key), key);
                try {
                    Files.copy(copyFrom.toPath(), copyTo.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Node commitNode = new Node(head, msg);
        commitNode._sha = new HashMap<String, String>(tree.get(head)._sha);
        for (String s : sha1s) {
            commitNode._sha.remove(s);
        }
        for (String addN : addTo.keySet()) {
            commitNode._sha.put(addTo.get(addN), addN);
        }
        for (String rem : rmv.keySet()) {
            commitNode._sha.remove(rem);
        }

        addTo.clear();
        sha1s.clear();
        rmv.clear();
        String toHead = "";
        for (String s : commitNode._sha.keySet()) {
            toHead += sha1(s);
        }
        head = sha1(commitNode._time + toHead);
        tree.put(head, commitNode);
        brnchHist.get(curBrnch).add(head);
        branches.put(curBrnch, head);
        serObj();
    }

    /** Return list of files in directory. */
    private static HashSet<String> filesInDir() {
        File dir = new File(".");
        HashSet<String> has = new HashSet<String>();
        for (File f : dir.listFiles()) {
            has.add(f.getName());
        }
        return has;

    }

    /** Checks out FILENAME from commit COMMITID. */
    private static void checkout(String commitID, String fileName) {
        String fileSha = null;
        File copyFrom;
        File copyTo;
        if (tree.get(commitID) == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Node node = tree.get(commitID);
        for (String sha : node._sha.keySet()) {
            if (node._sha.get(sha).equals(fileName)) {
                fileSha = sha;
            }
        }

        if (fileSha == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        copyTo = new File(fileName);
        copyFrom = new File(".gitlet/commit" + "/" + fileSha + ".txt");
        try {
            Files.copy(copyFrom.toPath(), copyTo.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Checks out a branfh of name BRANCH. */
    private static void checkout(String branch) {
        String prev = head;
        head = branches.get(branch);
        for (String s : filesInDir()) {
            if (!tree.get(prev)._sha.containsValue(s)
                && tree.get(head)._sha.containsValue(s)) {
                System.out.println("There is an untracked file "
                    + "in the way; delete it or add it first.");
                return;
            }
        }
        for (String s : tree.get(head)._sha.keySet()) {
            File copyFrom = new File(".gitlet/commit" + "/" + s + ".txt");
            File copyTo = new File(tree.get(head)._sha.get(s));
            try {
                Files.copy(copyFrom.toPath(), copyTo.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (String s : tree.get(prev)._sha.values()) {
            if (!tree.get(head)._sha.containsValue(s)) {
                File f = new File(s);
                f.delete();
            }
        }
    }

    /** Initializes .gitlet directory. */
    private static void init() {
        File file = new File(".gitlet/");
        if (file.isDirectory()) {
            System.out.println("A gitlet version control "
                    + "system already exists in the current directory.");
        } else {
            file.mkdir();
            File commit = new File(".gitlet/commit");
            commit.mkdir();
            Node initNode = new Node("pre-init",
                new HashMap<String, String>(), "initial commit");
            head = sha1(initNode._time);
            tree.put(head, initNode);
            branches.put("master", "0");
        }
        curBrnch = "master";
        brnchHist.put(curBrnch, new ArrayList<String>());
        brnchHist.get(curBrnch).add(head);
        serObj();
    }

    /** Serializes all the objects. */
    private static void serObj() {
        serialize(sha1s, ".gitlet/sha1s");
        serialize(file, ".gitlet/file");
        serialize(addTo, ".gitlet/addTo");
        serialize(tree, ".gitlet/nodeTree");
        serialize(rmv, ".gitlet/removed");
        serialize(curBrnch, ".gitlet/curBranch");
        serialize(branches, ".gitlet/branches");
        serialize(brnchHist, ".gitlet/brnchHist");
        serialize(head, ".gitlet/head");
    }

    /** Serializes an objec OBJ with name NAME. */
    private static void serialize(Object obj, String name) {
        try {
            FileOutputStream outFile = new FileOutputStream(name);
            ObjectOutputStream outObject = new ObjectOutputStream(outFile);
            outObject.writeObject(obj);
            outFile.close();
            outObject.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Serializes a string. */
    @SuppressWarnings("unused")
    private static void serializeStr(String str, String name) {
        try {
            FileOutputStream outFile = new FileOutputStream(name);
            DataOutputStream outObject = new DataOutputStream(outFile);
            outObject.writeChars(str);
            outFile.close();
            outObject.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Deserializes all objects. */
    @SuppressWarnings("unchecked")
    private static void deserObj() {
        sha1s = (HashSet<String>) deser(".gitlet/sha1s");
        brnchHist = (HashMap<String, ArrayList<String>>)
            deser(".gitlet/brnchHist");
        curBrnch = (String) deser(".gitlet/curBranch");
        head = (String) deser(".gitlet/head");
        tree = (HashMap<String, Node>) deser(".gitlet/nodeTree");
        rmv = (HashMap<String, String>) deser(".gitlet/removed");
        file = (HashMap<String, String>) deser(".gitlet/file");
        addTo = (HashMap<String, String>) deser(".gitlet/addTo");
        branches = (HashMap<String, String>) deser(".gitlet/branches");

    }

    /** Return deserialized NAME. */
    private static Object deser(String name) {
        try {
            FileInputStream inFile = new FileInputStream(name);
            ObjectInputStream inObject = new ObjectInputStream(inFile);
            Object outObject = inObject.readObject();
            inObject.close();
            inFile.close();
            return outObject;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
            return null;
        }
    }

    /** Node that represents a commit. */
    private static class Node implements Serializable {
        private String _parent;
        private String _msg;
        private HashMap<String, String> _sha = new HashMap<String, String>();
        private String _time;

        /** Node with parent PAR and message MSG. */
        public Node(String par, String msg) {
            _parent = par;
            Date myDate = new Date();
            String date = new
                SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(myDate);
            _time = date;
            _msg = msg;

        }

        /** Node with parent PAR, message MSG and files SHA. */
        public Node(String par, HashMap<String, String> sha, String msg) {
            _parent = par;
            _sha = sha;
            _msg = msg;
            Date myDate = new Date();
            String date = new
                SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(myDate);
            _time = date;
        }
    }

    /** Return sha1 of a string. */
    public static String sha1(final String s) {
        MessageDigest messageDigest;
        byte[] b = s.getBytes();
        try {
            messageDigest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e1) {
            return null;
        }
        return byteArrayToHexString(messageDigest.digest(b));
    }
    /**
     * Return sha1 string of FILE after sifting through all files if it is a
     * directory. Adds the word "commit" if it is a commit directory. For a
     * file, simply returns blob sha-1.
     */
    public static String sha1(final File file) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e1) {
            return null;
        }
        if (!file.isDirectory()) {
            try (InputStream is = new BufferedInputStream(new
                FileInputStream(file))) {
                final byte[] buffer = new byte[1024];
                for (int read = 0; (read = is.read(buffer)) != -1;) {
                    messageDigest.update(buffer, 0, read);
                }
            } catch (FileNotFoundException e) {
                return null;
            } catch (IOException e) {
                return null;
            }
            messageDigest.update(file.getName().getBytes());
            byte[] output = messageDigest.digest();
            return byteArrayToHexString(output);
        } else {
            for (File fil : file.listFiles()) {
                try (InputStream is = new BufferedInputStream(new
                    FileInputStream(fil))) {
                    final byte[] buffer = new byte[1024];
                    for (int read = 0; (read = is.read(buffer)) != -1;) {
                        messageDigest.update(buffer, 0, read);
                    }
                } catch (FileNotFoundException e) {
                    return null;
                } catch (IOException e) {
                    return null;
                }
            }
            byte[] output = messageDigest.digest();
            return byteArrayToHexString(output);
        }
    }

    /**
     * Return the string version of a byte array B.
     */
    public static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    /** Return file extension of FILE. */
    private static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }
    
    /** Gets addTo. */
    static HashMap<String, String> getAddTo() {
        return addTo;
    }
    /** Gets rmv. */
    static HashMap<String, String> getRmv() {
        return rmv;
    }
    /** Gets branch. */
    static HashMap<String, String> getBranch() {
        return branches;
    }
}
