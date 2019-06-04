package halign.smithwaterman;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class SparkProteinMSATest {
    public static void main(String[] args) {
        String inputFile = "E:\\myProgram\\intellij\\HAlign-II\\example\\BB11001.tfa";
        String outputFile = "E:\\myProgram\\intellij\\HAlign-II\\example\\BB11001-protein.fasta";

        SparkConf conf = new SparkConf().setAppName("SparkProteinMSA");
        conf.setMaster("local[16]");
        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        conf.set("spark.kryoserializer.buffer.max", "2000m");
        conf.registerKryoClasses(new Class[]{SparkProteinMSA.class});
        JavaSparkContext jsc = new JavaSparkContext(conf);
        new SparkProteinMSA().start(jsc, inputFile, outputFile);
        jsc.stop();
    }
}
