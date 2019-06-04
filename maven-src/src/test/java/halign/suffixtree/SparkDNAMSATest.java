package halign.suffixtree;

import java.util.ArrayList;
import java.util.List;

public class SparkDNAMSATest {
    public static void main(String[] args) {

        String word1 = "ACTACTDGDACTDGD";
        String word2 = "ACTDGD";
        String word3 = "GDACGD";

        List<String> fastaValList = new ArrayList<>();
        fastaValList.add(word1);
        fastaValList.add(word2);
        fastaValList.add(word3);

        SuffixTree suffixTree = new SuffixTree();
        suffixTree.build(word1 + "$");

        int maxLength = word1.length();

        List<int[]> spaceEvery = new ArrayList<>();
        List<int[]> spaceOther = new ArrayList<>();
        for (int i = 0; i < fastaValList.size(); i++) {
            AlignSubstring alignSubstring = new AlignSubstring(suffixTree, fastaValList.get(i));
            int[][] subStrings = alignSubstring.findCommonSubstrings();

            DNAPairAlign pairAlign = new DNAPairAlign(word1, fastaValList.get(i), subStrings, maxLength);
            pairAlign.pwa();
            spaceEvery.add(pairAlign.get_spaceevery());
            spaceOther.add(pairAlign.get_spaceother());
        }

        /*String inputFile = "E:\\myProgram\\intellij\\HAlign-II\\example\\special.fasta";
        String outputFile = "E:\\myProgram\\intellij\\HAlign-II\\example\\special-out.fasta";
        try {
            new ExtremeMSA().start(inputFile, outputFile, null);
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }*/

        System.out.println("OK");

    }

    /*public static void main(String[] args) {

        String inputKVFile = "D:\\MASTER2016\\1.MSA2.0\\data\\genome0.fasta";
        String outputfile = "D:\\MASTER2016\\1.MSA2.0\\data\\genomeSpark.fasta";

        SparkConf conf = new SparkConf().setAppName("SparkSuffixAutomation");
        conf.setMaster("local[16]");
        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        conf.set("spark.kryoserializer.buffer.max", "2000m");
        conf.registerKryoClasses(new Class[]{SparkSuffixAutomation.class});
        conf.registerKryoClasses(new Class[]{DNAPairAlign.class});
        conf.registerKryoClasses(new Class[]{GenAlignOut.class});
        JavaSparkContext jsc = new JavaSparkContext(conf);
        new SparkSuffixAutomation().start(jsc, inputKVFile, outputfile);
        jsc.stop();
    }*/

}
