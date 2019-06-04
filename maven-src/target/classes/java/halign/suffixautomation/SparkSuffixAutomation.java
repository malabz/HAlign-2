package halign.suffixautomation;

import halign.suffixtree.DNAPairAlign;
import halign.suffixtree.GenAlignOut;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;
import utils.IOUtils;
import utils.HDFSUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Spark下实现后缀自动机（Suffix Automation, SAM）
 * */
public class SparkSuffixAutomation {

    public void start(JavaSparkContext jsc, String inputKVFile, String outputfile) {

        System.out.println("(Spark mode for DNA) Loading data ... " + inputKVFile);

        long start = System.currentTimeMillis();
        IOUtils formatUtils = new IOUtils();
        formatUtils.readFasta(inputKVFile, true);
        int allNum = formatUtils.getAllNum();
        int maxLength = formatUtils.getMaxLength();
        List<String> fastaKeyList = formatUtils.getS_key();
        List<String> fastaValList = formatUtils.getS_val();
        final String firstVal = fastaValList.get(0);

        System.out.println((System.currentTimeMillis() - start) + "ms");
        System.out.println("MultiThread MSA ... ");

        /*SuffixTree suffixTree = new SuffixTree();
        suffixTree.build(firstVal + "$");*/
        SuffixAutomaton suffixAutomaton = new SuffixAutomaton(maxLength).buildSuffixAutomaton(firstVal.toCharArray());
        List<int[][]> nameList = new ArrayList<>();
        for (int i = 0; i < allNum; i++) {
            /*AlignSubstring alignSubstring = new AlignSubstring(suffixTree, fastaValList.get(i));
            nameList.add(alignSubstring.findCommonSubstrings());*/
            char[] lcs = suffixAutomaton.lcs(fastaValList.get(i).toCharArray());
            int lcs_pos_S = suffixAutomaton.indexOf(lcs);
            int lcs_pos_T = new SuffixAutomaton(maxLength).buildSuffixAutomaton(fastaValList.get(i).toCharArray()).indexOf(lcs);
            int[][] matched_info = {{lcs_pos_S}, {lcs_pos_T}, {lcs.length}};
            nameList.add(matched_info);
        }
        System.out.println((System.currentTimeMillis() - start) + "ms");


        JavaRDD<String> fastaValRDD = jsc.parallelize(fastaValList).cache();
        final Broadcast<String> firstValBC = jsc.broadcast(firstVal);
        JavaPairRDD<int[], int[]> spacePairsRDD = fastaValRDD.zip(jsc.parallelize(nameList)).mapToPair(
                (PairFunction<Tuple2<String, int[][]>, int[], int[]>) tuple2 -> {
                    DNAPairAlign pairAlign = new DNAPairAlign(firstValBC.value(), tuple2._1, tuple2._2, maxLength);
                    pairAlign.pwa();
                    return new Tuple2(pairAlign.get_spaceevery(), pairAlign.get_spaceother());
                }).cache();

        int firstSpaceArray[] = spacePairsRDD.keys().reduce(
                (Function2<int[], int[], int[]>) (int1, int2) -> {
                    for (int i = 0; i < int2.length; i++)
                        if (int1[i] < int2[i]) int1[i] = int2[i];
                    return int1;
                });
        StringBuilder stringBuilder = new StringBuilder();
        String firstVal0 = firstVal;
        int firstValLen = firstVal0.length() + 1;
        for (int i = 0; i < firstValLen; i++) {
            for (int j = 0; j < firstSpaceArray[i]; j++) stringBuilder.append('-');
            if (i != firstValLen - 1) stringBuilder.append(firstVal0.charAt(i));
        }
        /* bug fixed.
        int lastNum = firstSpaceArray[firstSpaceArray.length - 1];
        for (int i = 0; i < lastNum; i++)
            stringBuilder = stringBuilder.append('-');*/
        firstVal0 = stringBuilder.toString();
        System.out.println((System.currentTimeMillis() - start) + "ms");

        System.out.println(">>Converting results ... ");
        final Broadcast<String> firstVal0BC = jsc.broadcast(firstVal0);
        final Broadcast<int[]> firstSpaceArrayBC = jsc.broadcast(firstSpaceArray);
        JavaRDD<String> fastaMSAOutJavaRDD = fastaValRDD.zip(spacePairsRDD).map(
                (Function<Tuple2<String, Tuple2<int[], int[]>>, String>) t -> {
                    String pi1 = t._1().trim();
                    int[] spaceEvery = t._2()._1();
                    int[] spaceOther = t._2()._2();
                    GenAlignOut genAlignOut = new GenAlignOut();
                    return genAlignOut.get_every_sequeces(firstVal0BC.value().trim(), pi1,
                            allNum, firstVal, firstVal.length(),
                            firstSpaceArrayBC.value(), spaceEvery, spaceOther);
                });

        List<String> fastaMSAOutList = fastaMSAOutJavaRDD.repartition(1).collect();
        System.out.println(">>" + (System.currentTimeMillis() - start) + "ms");


        System.out.println(">>Saving final results ... ");
        new HDFSUtils().clear_local_path(new File(outputfile));
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputfile));
            for (int i = 0; i < allNum; i++) {
                bw.write(fastaKeyList.get(i) + "\n");
                bw.write(fastaMSAOutList.get(i) + "\n");
            }
            bw.close();
        } catch (Exception ignored) {

        }
        System.out.println(">>" + (System.currentTimeMillis() - start) + "ms");
    }
}
