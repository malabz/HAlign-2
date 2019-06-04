package tree;

import utils.IOUtils;
import utils.HDFSUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MSATree {
    public static void main(String[] args) throws Exception {
        String inputFile = "/home/shixiang/out.fasta";
        String outputFile = "/home/shixiang/tree.tre";
        new MSATree().start(inputFile, outputFile);
    }

    public void start(String inputFile, String outputFile) throws Exception {

        String localPath = "";
        if (inputFile.contains("/")) {
            localPath = inputFile.substring(0, inputFile.lastIndexOf("/")+1);
            inputFile = inputFile.substring(inputFile.lastIndexOf("/")+1);
        }
        HDFSUtils utils = new HDFSUtils();
        utils.clear_local_path(new File(localPath + "HPTree_OutPut"));

        System.out.println(">>(Local mode for tree) loading data ...");
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
        clusterProcess.kvProcess(localPath + "inputKV", localPath + "Cluster_OutPut");
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
        BufferedReader bufferedReader = new BufferedReader(new FileReader(localPath + "OnBalance_OutPut"));
        String value;
        List<String> balanceKey = new ArrayList<>();
        List<String> balanceVal = new ArrayList<>();
        while (bufferedReader.ready()) {
            value = bufferedReader.readLine();
            String [] list = value.split("\t");// 把三个字段分离出来
            balanceKey.add(list[1]);
            balanceVal.add(list[2]);
        }
        bufferedReader.close();

        int loop;
        if (allNum < 100) loop = 4;
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
