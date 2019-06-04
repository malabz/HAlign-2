package halign.suffixtree;

import halign.kband.KbandMSA;

import java.io.IOException;

public class ExtremeMSATest {

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        String inputFile = "D:\\Applications\\myProgram\\intellij\\HAlign-II\\example\\protein.fasta";
        String outputFile = "D:\\Applications\\myProgram\\intellij\\HAlign-II\\example\\in-out.fa";
        String outputDFS = "/shixiangwan";
        new ExtremeMSA().start(inputFile, outputFile, null);

        //new ExtremeMSA().start(inputFile, outputFile, outputDFS);
    }
}
