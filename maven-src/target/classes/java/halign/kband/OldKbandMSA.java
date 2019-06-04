//package halign.kband;
//
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.io.NullWritable;
//import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapreduce.Job;
//import org.apache.hadoop.mapreduce.Mapper;
//import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
//import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
//import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
//import utils.HDFSUtils;
//import utils.IOUtils;
//
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class OldKbandMSA {
//
//	static List<String> s_key = new ArrayList<>();
//	static List<String> s_val = new ArrayList<>();
//
//    public void start(String inputFile, String outputFile, String outputDFS)
//    		throws IOException, ClassNotFoundException, InterruptedException {
//    	if (outputDFS == null) {
//			IOUtils formatUtils = new IOUtils();
//			formatUtils.readFasta(inputFile, true);
//			s_key = formatUtils.getS_key();
//			s_val = formatUtils.getS_val();
//    	} else {
//        	System.out.println(">>Clearing HDFS Path & uploading ...");
//			HDFSUtils MSAFileUtils = new HDFSUtils();
//			MSAFileUtils.clear_dfs_path(outputDFS);
//    		MSAFileUtils.local_to_dfs(inputFile, outputDFS + "/input/input.txt");
//
//    		System.out.println(">>Map reducing ...");
//    		Configuration conf = new Configuration();
//    		conf.set("mapred.task.timeout", "0");
//			Job job = Job.getInstance(conf, "msa_kband");
//    		job.setJarByClass(OldKbandMSA.class);
//    		job.setInputFormatClass(TextInputFormat.class);
//    		job.setMapperClass(KbandMapper.class);
//    		job.setMapOutputKeyClass(NullWritable.class);
//    		job.setMapOutputValueClass(Text.class);
//    		FileInputFormat.addInputPath(job, new Path(outputDFS + "/input/input.txt"));
//    		FileOutputFormat.setOutputPath(job, new Path(outputDFS + "/output"));
//    		job.setNumReduceTasks(1);
//    		job.waitForCompletion(true);
//    	}
//
//        /*将第一个序列作为根，与其他每个序列进行双序列比对，保留第一个序列的比对结果s_out1*/
//        ArrayList<String> s_out1 = new ArrayList<>();
//        ArrayList<String> s_out2 = new ArrayList<>();
//        String sequence1 = s_val.get(0);
//        int sequenceLen1 = sequence1.length();
//        KbandAlignTwo kbandAlignTwo = new KbandAlignTwo();
//        for (int i=1; i<s_val.size(); i++) {
//            kbandAlignTwo.align(sequence1, s_val.get(i));
//            s_out1.add(new String(KbandMake.aligns));
//            s_out2.add(new String(KbandMake.alignt));
//        }
//
//        /*统计第一个序列的比对结果，得到它的归总比对结果insertSpace1[]*/
//        int index;
//        int insertSpace1[] = new int[sequenceLen1 + 1];
//        for (String line : s_out1) {
//            int tempSpace1[] = new int[sequenceLen1 + 1];
//            index = 0;
//            for (int j=0; j < line.length(); j++) {
//                if (line.charAt(j) == '-') {
//                    tempSpace1[index]++;
//                } else {
//                    if (insertSpace1[index] < tempSpace1[index]) {
//                        insertSpace1[index]=tempSpace1[index];
//                    }
//                    index++;
//                }
//            }
//        }
//
//        /*以第一条序列为中心序列，计算中心序列sequence1*/
//        StringBuilder stringBuilder = new StringBuilder();
//        int insertSpaceLen1 = insertSpace1.length;
//        for(int i=0; i<insertSpaceLen1; i++){
//            for(int j=0; j<insertSpace1[i]; j++) {
//                stringBuilder.append('-');
//            }
//            if(i != insertSpaceLen1-1) {
//                stringBuilder.append(sequence1.charAt(i));
//            }
//        }
//        sequence1 = stringBuilder.toString();
//
//        /*将归纳得到的sequence1再次与第一次比对结果比对，得到最终各序列比对结果存入文件*/
//        try {
//            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
//            for (int i=0; i<s_key.size(); i++) {
//                bw.write(s_key.get(i));
//                bw.newLine();
//                if (i == 0) {
//                    bw.write(sequence1);
//                } else {
//                	kbandAlignTwo.align(sequence1, s_out2.get(i-1));
//                    bw.write(new String(KbandMake.alignt));
//                }
//                bw.newLine();
//                bw.flush();
//            }
//            bw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//	public static class KbandMapper extends Mapper<Object, Text, NullWritable, Text> {
//		public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
//			if (value.charAt(0) == '>') {
//				s_key.add(value.toString());
//			} else {
//				s_val.add(value.toString());
//			}
//			context.write(NullWritable.get(), value);
//		}
//	}
//}
