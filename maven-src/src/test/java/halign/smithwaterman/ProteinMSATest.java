package halign.smithwaterman;

public class ProteinMSATest {
    public static void main(String[] args) {
        String inputFile = "E:\\myProgram\\intellij\\HAlign-II\\example\\BB11002.tfa";
        String outputFile = "E:\\myProgram\\intellij\\HAlign-II\\example\\BB11002-protein.fasta";
        new LocalProteinMSA().start(inputFile, outputFile);
    }
}
