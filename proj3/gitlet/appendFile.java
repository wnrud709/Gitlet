package gitlet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
 
public class appendFile {
    /** appends FROM to TO. */
    public static void main(String[] args) throws IOException {
        String to = "a.txt";
        String from = "b.txt";
        File fromF = new File(from);
 

        
        FileWriter fstream = new FileWriter(to, true);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(">>>>>>>");
 
        try (BufferedReader br = new BufferedReader(new FileReader(fromF))) {
            String line;
            while ((line = br.readLine()) != null) {
                out.newLine();
               out.write(line);
            }
        }
        out.newLine();
        out.close();
    }
    public static void append(BufferedWriter out, File take) throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(take))) {
            String line;
            while ((line = br.readLine()) != null) {
                out.newLine();
               out.write(line);
            }
        }
        
    }
}