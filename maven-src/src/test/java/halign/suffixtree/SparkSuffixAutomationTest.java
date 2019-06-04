package halign.suffixtree;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import halign.suffixautomation.SparkSuffixAutomation;

public class SparkSuffixAutomationTest {
    public static void main(String[] args) {

        String inputKVFile = "E:\\myProgram\\intellij\\HAlign-II\\example\\BB11001.tfa";
        String outputFile = "E:\\myProgram\\intellij\\HAlign-II\\example\\BB11001";
        //String outputFile2 = "E:\\myProgram\\intellij\\HAlign-II\\example\\genomeSpark2.fasta";

        SparkConf conf = new SparkConf().setAppName("SparkSuffixAutomation");
        conf.setMaster("local[16]");
        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        conf.set("spark.kryoserializer.buffer.max", "2000m");
        conf.registerKryoClasses(new Class[]{SparkSuffixAutomationTest.class});
        conf.registerKryoClasses(new Class[]{DNAPairAlign.class});
        conf.registerKryoClasses(new Class[]{GenAlignOut.class});
        JavaSparkContext jsc = new JavaSparkContext(conf);

        new SparkSuffixAutomation().start(jsc, inputKVFile, outputFile);

//        new SparkDNAMSA().start(jsc, inputKVFile, outputFile2);

        jsc.stop();

        /*try {
            new MatrixMSA().start(inputKVFile, outputFile2, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/


    }
}
