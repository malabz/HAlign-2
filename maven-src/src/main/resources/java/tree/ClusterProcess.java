package tree;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ClusterProcess {

    public static Set<Set> sum_set = new HashSet<>();

    class cluster_data {
        String label;
        String name;
        String sequence;
    }

    public void preProcess(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        int first_set_flag = 0;
        String pre_label = "";
        String cur_label;
        Set<cluster_data> pre_set = new HashSet<>();
        while (br.ready()) {
            String str[] = br.readLine().split("\t");
            cur_label = str[0];
            if (first_set_flag == 0) {  // add the first data
                first_set_flag = 1;
                pre_label = cur_label;
                cluster_data clusterdata = new cluster_data();
                clusterdata.label = str[0];
                clusterdata.name = str[1];
                clusterdata.sequence = str[2];
                pre_set.add(clusterdata);
                continue;
            }
            // if the current label no equal to the pre label add the set to sum-set
            if (!cur_label.equals(pre_label)) {
                sum_set.add(pre_set);
                pre_label = cur_label;
                pre_set = new HashSet<>();
                cluster_data clusterdata = new cluster_data();
                clusterdata.label = str[0];
                clusterdata.name = str[1];
                clusterdata.sequence = str[2];
                pre_set.add(clusterdata);
            } else {
                cluster_data clusterdata = new cluster_data();
                clusterdata.label = str[0];
                clusterdata.name = str[1];
                clusterdata.sequence = str[2];
                pre_set.add(clusterdata);
            }
        }// end while
        sum_set.add(pre_set);
        br.close();
    }

    public void kvProcess(String inputKVFile, String outputClusterFile) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputKVFile));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputClusterFile));

        String value;
        while (bufferedReader.ready()) {
            value = bufferedReader.readLine().trim();
            String[] value_temp = value.split("\t");
            String name = value_temp[0];
            String sequence = value_temp[1];
            String label = "";
            double min_distance = 100000;
            for (Set<cluster_data> set : sum_set) {
                Iterator iter = set.iterator();
                cluster_data clusterdata = (cluster_data) iter.next(); // random select one sample to calculate the distance
                double p = JukeCantor(sequence, clusterdata.sequence);
                p = 1.0 - 0.75 * p;
                double distance = (double) (int) (-0.75 * Math.log(p) * 1000) / 1000;  //根据公式计算距离
                if (distance < min_distance) {
                    min_distance = distance;
                    label = clusterdata.label;
                }
            }
            bufferedWriter.write(label + "\t" + name + "\t" + sequence+"\n");
        }
        bufferedReader.close();
        bufferedWriter.close();
    }

    /*input two sequences, return differ parts rate of them.*/
    public static double JukeCantor(String gene1, String gene2) {
        int rate = 0;
        int i;
        double result;
        int length = gene1.length();
        if (gene1.length() > gene2.length()) {
            length = gene2.length();
        }
        for (i = 0; i < length; i++) {
            if (gene1.charAt(i) != gene2.charAt(i)) {
                rate++;
            }
        }
        result = (double) rate / (double) gene1.length();
        return result;
    }

}
