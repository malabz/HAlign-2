package tree;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import utils.IOUtils;
import utils.HDFSUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SparkTree {
    public static void main(String[] args) throws Exception {
        String filename = "/home/shixiang/out.fasta";
//        filename = "/home/shixiang/genome-out2.fasta";
        String outputFile = "/home/shixiang/tree.tre";

        SparkConf conf = new SparkConf().setAppName("SparkMSATree");
        conf.setMaster("local[16]");
        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        conf.set("spark.kryoserializer.buffer.max", "2000m");
        conf.registerKryoClasses(new Class[]{SparkTree.class});
        JavaSparkContext jsc = new JavaSparkContext(conf);
        new SparkTree().GenerateTree(jsc, filename, outputFile);
        jsc.stop();
    }

    public void GenerateTree(JavaSparkContext jsc, String inputFile, String outputFile) throws Exception {

        String localPath = "";
        if (inputFile.contains("/")) {
            localPath = inputFile.substring(0, inputFile.lastIndexOf("/")+1);
            inputFile = inputFile.substring(inputFile.lastIndexOf("/")+1);
        }

        HDFSUtils utils = new HDFSUtils();
        utils.clear_local_path(new File(localPath + "HPTree_OutPut"));

        System.out.println(">> (Spark mode for tree) loading data ...");
        long startTime = System.currentTimeMillis();
        IOUtils formatUtils = new IOUtils();
        formatUtils.formatKVFasta(localPath + inputFile, localPath + "inputKV");
        int allNum = formatUtils.getAllNum();
        System.out.println("total number: " + allNum);
        System.out.println((System.currentTimeMillis() - startTime) + "ms");

        System.out.println(">>clustering process ...");
        String reduce_number = "2";
        Pre_Cluster_process cluster = new Pre_Cluster_process(localPath + "inputKV", reduce_number);
        cluster.Get_Cluster(localPath + "single_cluster_output", allNum);

        ////////////////////////////////////////////////////////////////////////////////////////
        ClusterProcess clusterProcess = new ClusterProcess();
        clusterProcess.preProcess(localPath + "single_cluster_output");
        new File(localPath + "single_cluster_output").delete();
        //clusterProcess.kvProcess(local_path + "inputKV", local_path + "Cluster_OutPut");

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(localPath + "Cluster_OutPut"));
        jsc.textFile("file://"+localPath + "inputKV").map(
                (Function<String, String>) f -> {
                    String value = f.trim();
                    String[] value_temp = value.split("\t");
                    String name = value_temp[0];
                    String sequence = value_temp[1];
                    String label = "";
                    double min_distance = 100000;
                    for (Set<ClusterProcess.cluster_data> set : ClusterProcess.sum_set) {
                        Iterator iter = set.iterator();
                        ClusterProcess.cluster_data data = (ClusterProcess.cluster_data) iter.next();
                        double p = ClusterProcess.JukeCantor(sequence, data.sequence);
                        p = 1.0 - 0.75 * p;
                        double distance = (double) (int) (-0.75 * Math.log(p) * 1000) / 1000;
                        if (distance < min_distance) {
                            min_distance = distance;
                            label = data.label;
                        }
                    }
                    return label + "\t" + name + "\t" + sequence+"\n";
                }).collect().forEach(l -> {
                try {
                    bufferedWriter.write(l);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        });
        bufferedWriter.close();
        new File(localPath + "inputKV").delete();
        System.out.println((System.currentTimeMillis() - startTime) + "ms");

        /*local_path: Cluster_OutPut -> OnBalance_OutPut*/
        int cluster_number = 3;
        OnBalance onbalance = new OnBalance(localPath + "Cluster_OutPut");
        onbalance.Balance(cluster_number, localPath);
        new File(localPath + "Cluster_OutPut").delete();

        System.out.println(">>construct phylogenetic tree ...");
        ////////////////////////////////////////////////////////////////////////////////////////
        new File(localPath + "HPTree_OutPut").mkdir();
        JavaPairRDD<String, String> balancePairRDD = jsc.textFile("file://"+localPath + "OnBalance_OutPut").mapToPair(
                (PairFunction<String, String, String>) s -> {
                    String [] list = s.split("\t");
                    return new Tuple2(list[1], list[2]);
                });
        List<String> balanceKey = balancePairRDD.keys().collect();
        List<String> balanceVal = balancePairRDD.values().collect();

        int loop;
        if (allNum < 100) loop = 8;
        else loop = 32;
        int loopNum = allNum/loop;
        int position = 0;
        ExecutorService pool = Executors.newFixedThreadPool(loop);
        for (int i = 0; i < loop; i++) {
            int position2 = position+loopNum;
            if (i == loop-1) position2 = allNum;
            List<String> key = balanceKey.subList(position, position2);
            List<String> val = balanceVal.subList(position, position2);
            GetSubTreeThread getSubTreeThread = new GetSubTreeThread(key, val, localPath + "HPTree_OutPut/"+i+".txt");
            pool.execute(new Thread(getSubTreeThread));
            position = position2;
        }
        pool.shutdown();
        while (!pool.isTerminated());
        new File(localPath + "OnBalance_OutPut").delete();
        System.out.println((System.currentTimeMillis() - startTime) + "ms");

        System.out.println(">>merge and save phylogenetic tree ...");
        NeighbourJoining_Summary nj_summary = new NeighbourJoining_Summary(localPath+"HPTree_OutPut", outputFile);
        nj_summary.Merge();
        nj_summary.Summary();

        utils.clear_local_path(new File(localPath + "HPTree_OutPut"));
        System.out.println(">>success! time cost: " + (System.currentTimeMillis() - startTime) + "ms");
    }

    public class GetSubTreeThread implements Runnable {
        private List<String> Key;
        private List<String> Val;
        private String outputFile;
        GetSubTreeThread(List<String> Key, List<String> Val, String outputFile) {
            this.Key = Key;
            this.Val = Val;
            this.outputFile = outputFile;
        }
        @Override
        public void run() {
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
                NeighbourJoining nj = new NeighbourJoining();//开启进化树构建算法
                nj.construct(Key, Val);
                String sub_tree = nj.getSubTree();
                String present_seq = nj.getPresentNode();
                bufferedWriter.write(present_seq+"\t"+sub_tree);
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
