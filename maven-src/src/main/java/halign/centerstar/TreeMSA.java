package halign.centerstar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import utils.HDFSUtils;

public class TreeMSA {
	private static String[] sequence; // 序列
	private static String[] sequenceName; // 序列名
	private int[][] spaceCentre; // 中心序列增加空格的位置
	private int[][] spaceOthers; // 其它序列增加空格的位置
	private int r; // 被划分的小段的长度
	private int n; // 序列数量
	private int c; // 中心序列索引
	private int spaceScore = -1; // 动态规划
	private int[][][][] name;

	public boolean start(String inputFile, String outputFile, String outputDFS)
			throws IOException, ClassNotFoundException, InterruptedException {
		n = count(inputFile);
		if (n < 2)
			return false;
		if (outputDFS == null)
			input(inputFile);
		else {
			sequence = new String[n];
			sequenceName = new String[n];
			System.out.println(">>Clearing HDFS Path & uploading ...");
			HDFSUtils MSAFileUtils = new HDFSUtils();
			MSAFileUtils.clear_dfs_path(outputDFS);
			MSAFileUtils.local_to_dfs(inputFile, outputDFS + "/input/input.txt");
			System.out.println(">>Map reducing ...");
			Configuration conf = new Configuration();
			conf.set("mapred.task.timeout", "0");
			Job job = Job.getInstance(conf, "msa_tree");
			job.setJarByClass(TreeMSA.class);
			job.setInputFormatClass(TextInputFormat.class);
			job.setMapperClass(TreeMapper.class);
			job.setMapOutputKeyClass(NullWritable.class);
			job.setMapOutputValueClass(Text.class);
			FileInputFormat.addInputPath(job, new Path(outputDFS + "/input/input.txt"));
			FileOutputFormat.setOutputPath(job, new Path(outputDFS + "/output"));
			job.setNumReduceTasks(1);
			job.waitForCompletion(true);
		}
		// 计算划分的序列长度，待改进
		r = sequence[1].length() < 300 ? sequence[1].length() : sequence[1].length() / 30;
		System.out.printf("the length of substrings is %d\n", r);
		c = findCentre();
		System.out.printf("centre sequence is sequence[%d]\n", c);
		// 多序列比对部分
		spaceCentre = new int[n][sequence[c].length() + 1]; // 存放中心序列增加空格的位置
		spaceOthers = new int[n][maxLength() + 1]; // 存放其它序列增加空格的位置
		for (int i = 0; i < n; ++i) {
			if (i == c)
				continue;
			// 比对其它序列与中心序列有重合的前端
			if (name[c][i][0][0] != 0 && name[c][i][1][0] != 0) {
				System.out.printf("preAligning sequence[%d]\n", i);
				// 此处有漏洞，如果name[c][i][0][0] == 0，会报错
				String strCentre = sequence[c].substring(0, (name[c][i][0][0] - 1) * r);
				String strOthers = sequence[i].substring(0, name[c][i][1][0] - 1); // 第二个参数-1
				System.out.printf("\tcentre: %s\n", strCentre);
				System.out.printf("\tothers: %s\n", strOthers);
				int[][] dp = scoreMatrix(strOthers, strCentre);
				traceBack(dp, strOthers.length(), strCentre.length(), i, 0, 0);
			}
			// 比对其它序列与中心序列有重合的中间部分
			System.out.printf("midAligning sequence[%d]\n", i);
			for (int j = 1; j < name[c][i][0].length - 1; ++j) // 判断条件-1
				if (name[c][i][0][j] != 0 && name[c][i][1][j] >= name[c][i][1][j - 1] + r && name[c][i][0][j] > name[c][i][0][j - 1]) { // 去掉一个不等于0的检查
					// 此处有漏洞，如果name[c][i][0][0] == 0，会报错
					System.out.printf("%d:\n", j);
					String strCentre = sequence[c].substring(name[c][i][0][j - 1] * r, (name[c][i][0][j] - 1) * r);
					String strOthers = sequence[i].substring(name[c][i][1][j - 1] + r - 1, name[c][i][1][j] - 1); // 各-1
					System.out.printf("\tcentre: %s\n", strCentre);
					System.out.printf("\tothers: %s\n", strOthers);
					int[][] dp = scoreMatrix(strOthers, strCentre);
					traceBack(dp, strOthers.length(), strCentre.length(), i,
							name[c][i][1][j - 1] + r - 1, name[c][i][0][j - 1] * r);
				}
			// 比对其它序列与中心序列有重合的后端
			int j = name[c][i][0].length - 1;
			System.out.printf("postAligning sequence[%d]\n", i);
			if (j != 0) {
				String strCentre = sequence[c].substring(r * name[c][i][0][j - 1]);
				String strOthers = sequence[i].substring(name[c][i][1][j - 1] + r - 1); // -1
				System.out.printf("\tcentre: %s\n", strCentre);
				System.out.printf("\tothers: %s\n", strOthers);
				int[][] dp = scoreMatrix(strOthers, strCentre);
				traceBack(dp, strOthers.length(), strCentre.length(), i, name[c][i][1][j - 1] + r - 1, name[c][i][0][j - 1] * r);
			} else {
				String strCentre = sequence[c];
				String strOthers = sequence[i];
				System.out.printf("\tcentre: %s\n", strCentre);
				System.out.printf("\tothers: %s\n", strOthers);
				int[][] dp = scoreMatrix(strOthers, strCentre);
				traceBack(dp, strOthers.length(), strCentre.length(), i, 0, 0);
			}
		}
		output(combine(), outputFile);
		return true;
	}

	// 读入
	private void input(String inputFile) {
		sequence = new String[n];
		sequenceName = new String[n];
		int i = -1;
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line;
			while (br.ready()) {
				line = br.readLine();
				if (line.length() > 0 && line.charAt(0) == '>') {
					sequenceName[++i] = line;
					sequence[i] = "";
					break;
				}
			}
			while (br.ready()) {
				line = br.readLine();
				if (line.length() > 0 && line.charAt(0) == '>') {
					sequenceName[++i] = line;
					sequence[i] = "";
				} else
					sequence[i] += format(line);
			}
			br.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			System.exit(0);
		}
	}

	// 格式化String，转为大写字母，保留A, G, C, T, N，替换U为T，忽略空格，其它字符为N
	private String format(String s) {
		s = s.toUpperCase();
		s = s.replaceAll("\\s*", "");
		s = s.replace('U', 'T');
		StringBuilder sb = new StringBuilder(s); // 可能待改，线程安全警告
		for (int i = 0; i < sb.length(); ++i) {
			switch (sb.charAt(i)) {
				case 'A':
				case 'C':
				case 'G':
				case 'T':
				case 'N':
					break;
				default:
					sb.replace(i, i + 1, "n");
			}
		}
		return sb.toString();
	}

	// 建立关键字树
	private NewRoot buildTrieTree(int i, BuildTree buildTree) {
		NewRoot trie = new NewRoot(); // 创建一个新关键字树（树根）
		trie.ID = 0; // 树根的ID为0
		for (int j = 0; j < sequence[i].length() / r; ++j)  // 建树。对于每一个长度为r的划分子串
			buildTree.build(sequence[i], j * r, r, j + 1, trie); // 根据这个子串建树
		buildTree.failLink(trie); // 建立整个树的失效链接
		return trie;
	}

	// 找出num[][]数组中和最大的那一行，作为中心序列
	private int findNumMax(int[][] num) {
		int[] sum = new int[n];
		for (int i = 0; i < n; i++) {
			sum[i] = 0;
			for (int j = 0; j < n; j++)
				sum[i] += num[i][j];
		}
		int max = 0;
		for (int i = 1; i < n; i++)
			max = sum[max] > sum[i] ? max : i;
		return max;
	}

	// 找出中心序列
	private int findCentre() {
		int[][] num = new int[n][n]; // 用来记录出现的个数
		name = new int[n][n][2][]; // 用来存放出现的名字
		BuildTree buildTree = new BuildTree();
		for (int i = 0; i < n; ++i) {
			NewRoot trieTree = buildTrieTree(i, buildTree);
			for (int j = 0; j < n; j++)
				if (i != j) {
					buildTree.ACsearch(sequence[j], trieTree);
					name[i][j] = buildTree.out();
					num[i][j] = name[i][j][0].length - 1;
				} else
					num[i][j] = 0;
		}
		return findNumMax(num);
	}

	// 动态规划矩阵
	private int[][] scoreMatrix(String strOthers, String strCentre) {
		int matchScore = 0, mismatchScore = -1;
		int len1 = strOthers.length() + 1, len2 = strCentre.length() + 1;
		int[][] dp = new int[len1][len2]; // 定义动态规划矩阵
		// 初始化
		int p, q;
		for (p = 0; p < len1; ++p)
			dp[p][0] = spaceScore * p;
		for (q = 1; q < len2; ++q)
			dp[0][q] = spaceScore * q;
		// 矩阵每一个元素依赖于其左上角元素及其对应字符是否相等，上边的元素和左边的元素
		// dp[p][q]=max(dp[p-1][q]-1,dp[p][q-1]-1,dp[p-1][q-1]+h)
		for (p = 1; p < len1; ++p)
			for (q = 1; q < len2; ++q) {
				int h;
				h = strOthers.charAt(p - 1) == strCentre.charAt(q - 1) ? matchScore : mismatchScore;
				dp[p][q] = Math.max(dp[p - 1][q - 1] + h, Math.max(dp[p - 1][q] + spaceScore, dp[p][q - 1] + spaceScore));
			}
		return dp;
	}

	// 回溯。p, q为间隔字符串长度，k1, k2为字串起始位置
	private void traceBack(int[][] dp, int p, int q, int i, int k1, int k2) {
		while (p > 0 && q > 0)
			if (dp[p][q] == dp[p][q - 1] + spaceScore) {
				// 可能来自上边就向上走
				++spaceOthers[i][p + k1];
				--q;
			} else if (dp[p][q] == dp[p - 1][q] + spaceScore) {
				// 否则，可能来自左边就向左走
				++spaceCentre[i][q + k2];
				--p;
			} else {
				// 否则，向左上走
				--p;
				--q;
			}
		while (q > 0) {
			++spaceOthers[i][k1];
			--q;
		}
		while (p > 0) {
			++spaceCentre[i][k2];
			--p;
		}
	}

	private int[] combine() {
		int[] space = new int[sequence[c].length() + 1];// 记录中心序列中空格的添加位置和数量
		int i, j, max;
		for (i = 0; i < sequence[c].length() + 1; ++i) {
			max = 0;
			for (j = 0; j < n; ++j)
				max = spaceCentre[j][i] > max ? spaceCentre[j][i] : max;
			space[i] = max;
		}
		return space;
	}

	// 返回序列数量
	private int count(String filePath) {
		int num = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String s;
			while (br.ready()) {
				s = br.readLine();
				if (s.length() > 0 && s.charAt(0) == '>')
					++num;
			}
			br.close();
		} catch (Exception ex) {
			ex.getStackTrace();
		}
		if (num == 0) {
			System.out.println("no sequence found");
			System.out.println("no need to align");
		} else if (num == 1) {
			System.out.println("only one sequence found");
			System.out.println("please try another file");
		} else
			System.out.println(num + " sequences found");
		return num;
	}

	// 计算除中心序列以外的其他序列的最大长度
	private int maxLength() {
		int maxLength = 0;
		for (int i = 0; i < n; ++i)
			if (i != c)
				maxLength = sequence[i].length() > maxLength ? sequence[i].length() : maxLength;
		return maxLength;
	}

	private void output(int[] space, String outputFile) {
		String[] output = new String[n];
		// 输出中心序列
		output[c] = "";
		for (int i = 0; i < sequence[c].length(); ++i) {
			for (int j = 0; j < space[i]; ++j)
				output[c] = output[c].concat("-");
			output[c] = output[c].concat(sequence[c].substring(i, i + 1));
		}
		for (int j = 0; j < space[sequence[c].length()]; ++j)
			output[c] = output[c].concat("-");
		// 输出其它序列
		for (int i = 0; i < n; ++i)
			if (i != c) {
				output[i] = "";
				for (int j = 0; j < sequence[i].length(); ++j) {
					for (int k = 0; k < spaceOthers[i][j]; ++k)
						output[i] = output[i].concat("-");
					output[i] = output[i].concat(sequence[i].substring(j, j + 1));
				}
				for (int j = 0; j < spaceOthers[i][sequence[i].length()]; ++j)
					output[i] = output[i].concat("-");
				// 计算差异数组
				int[] dis = new int[sequence[c].length() + 1];
				int position = 0; // 插入差异空格的位置
				for (int j = 0; j < sequence[c].length() + 1; ++j) {
					int tmp1 = space[j] - spaceCentre[i][j];
					dis[j] = tmp1 > 0 ? tmp1 : 0;
					position = position + spaceCentre[i][j];
					if (dis[j] > 0) { // 在位置position处插入空格
						String tmp2 = "";
						for (int k = 0; k < dis[j]; ++k)
							tmp2 = tmp2.concat("-");
						output[i] = output[i].substring(0, position).concat(tmp2).concat(output[i].substring(position));
					}
					position += dis[j] + 1;
				}
			}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			for (int i = 0; i < n; ++i) {
				bw.write(sequenceName[i]);
				bw.newLine();
				bw.flush();
				bw.write(output[i].toUpperCase());
				if (i != n - 1)
					bw.newLine();
				bw.flush();
			}
			bw.close();
		} catch (Exception ignored) {
		}
	}

	public static class TreeMapper extends Mapper<Object, Text, NullWritable, Text> {
		int count = 0;

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			if (value.charAt(0) == '>') {
				sequenceName[count] = value.toString();
			} else {
				sequence[count] = value.toString();
				++count;
			}
			context.write(NullWritable.get(), value);
		}
	}
}