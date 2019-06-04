package halign.suffixtree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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

public class ExtremeMSA {
	private String filePath = ""; // 文件名
	private static String[] sequence; // 序列
	private static String[] sequenceName; // 序列名
	private int[][] spaceCentre; // 中心序列增加空格的位置，最终被space数组取代
	private int[][] spaceOthers; // 其它序列增加空格的位置
	private int n; // 序列数
	private int c; // 中心序列编号
	private int spaceScore = -1; // 可以设置为final吗
	private int[][][][] name;

	public boolean start(String inputFile, String outputFile, String outputDFS)
			throws IOException, ClassNotFoundException, InterruptedException {
		filePath = inputFile;
		n = count();
		if (n < 2)
			return false;
		if (outputDFS == null)
			input();
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
			Job job = Job.getInstance(conf, "msa_extreme");
			job.setJarByClass(ExtremeMSA.class);
			job.setInputFormatClass(TextInputFormat.class);
			job.setMapperClass(ExtremeMapper.class);
			job.setMapOutputKeyClass(NullWritable.class);
			job.setMapOutputValueClass(Text.class);
			FileInputFormat.addInputPath(job, new Path(outputDFS + "/input/input.txt"));
			FileOutputFormat.setOutputPath(job, new Path(outputDFS + "/output"));
			job.setNumReduceTasks(1);
			job.waitForCompletion(true);
		}
		c = centreAlignEachOne(); // 中心序列选择方法待改，注意在此之前centre都没有赋值
		deduplication();
		// 长度为n的字符串有n+1个位置可以插入空格
		spaceCentre = new int[n][sequence[c].length() + 1]; // 存放中心序列增加空格的位置
		spaceOthers = new int[n][computeMaxLength() + 1]; // 存放其它序列增加空格的位置
		for (int i = 0; i < n; ++i)
			if (i != c) {
				if (name[c][i][0].length > 0)
					preAlign(i);
				if (name[c][i][0].length > 1)
					for (int j = 1; j < name[c][i][0].length; ++j)
						midAlign(i, j);
				postAlign(i);
			}
		output(combine(), outputFile);
		return true;
	}

	// 处理子串包含和重叠问题
	private void deduplication() {
		for (int i = 0; i < n; i++)
			if (i != c && name[c][i][0].length > 1) {
				int len = name[c][i][0].length;
				for (int j = 1; j < name[c][i][0].length; ++j) {
					// 如果前一子串和当前子串首尾重叠，后移当前子串头指针，后移的距离为中心序列和外围序列中子串重叠字符数中的较大者
					// 如果任一序列中前一子串包含当前子串，则将当前子串长度置0
					// 如果非负，lambda是重叠字符数，没有+1, -1问题；如果非正，lambda是间隔段的长度
					int lambda = Math.max(name[c][i][1][j - 1] + name[c][i][2][j - 1] - name[c][i][1][j],
							name[c][i][0][j - 1] + name[c][i][2][j - 1] - name[c][i][0][j]); // 从j = 1开始，不会越界
					if (lambda > 0) {
						if (name[c][i][2][j] > lambda) {
							name[c][i][0][j] += lambda;
							name[c][i][1][j] += lambda;
							name[c][i][2][j] -= lambda;
						} else {
							name[c][i][2][j] = 0;
							--len;
						}
					}
				}
				int[][] tmp = new int[3][len];
				for (int j = 0, k = 0; j < len; ++j, ++k) {
					while (name[c][i][2][k] == 0)
						++k;
					for (int l = 0; l < 3; ++l)
						tmp[l][j] = name[c][i][l][k];
				}
				name[c][i] = tmp;
			}
	}

	// 比对其它序列与中心序列有重合的前端
	private void preAlign(int i) {
		String strCentre = sequence[c].substring(0, name[c][i][0][0]);
		String strOthers = sequence[i].substring(0, name[c][i][1][0]);
		int[][] dp = scoreMatrix(strOthers, strCentre);
		traceBack(dp, strOthers.length(), strCentre.length(), i, 0, 0);
	}

	// 比对其它序列与中心序列有重合的中间部分，j从1开始，
	private void midAlign(int i, int j) {
		String strCentre = sequence[c].substring(name[c][i][0][j - 1] + name[c][i][2][j - 1], name[c][i][0][j]);
		String strOthers = sequence[i].substring(name[c][i][1][j - 1] + name[c][i][2][j - 1], name[c][i][1][j]);
		int[][] dp = scoreMatrix(strOthers, strCentre);
		traceBack(dp, strOthers.length(), strCentre.length(), i,
				name[c][i][1][j - 1] + name[c][i][2][j - 1],
				name[c][i][0][j - 1] + name[c][i][2][j - 1]);
	}

	// 比对其它序列与中心序列有重合的后端
	private void postAlign(int i) {
		int tmp = name[c][i][0].length;
		if (tmp > 0) {
			int cStart = name[c][i][0][tmp - 1] + name[c][i][2][tmp - 1];
			if (cStart > sequence[c].length()) // 别忘了，建后缀树时在串尾增加了'$'
				--cStart;
			int iStart = name[c][i][1][tmp - 1] + name[c][i][2][tmp - 1];
			String strCentre = sequence[c].substring(cStart);
			String strOthers = sequence[i].substring(iStart);
			int[][] dp = scoreMatrix(strOthers, strCentre);
			traceBack(dp, strOthers.length(), strCentre.length(), i, iStart, cStart);
		} else {
			String strCentre = sequence[c];
			String strOthers = sequence[i];
			int[][] M = scoreMatrix(strOthers, strCentre);
			traceBack(M, strOthers.length(), strCentre.length(), i, 0, 0);
		}
	}

	// 返回序列数量
	private int count() {
		int n = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String s;
			while (br.ready()) {
				s = br.readLine();
				if (s.length() > 0 && s.trim().charAt(0) == '>') // 有空行报错的bug找了两天，字符之前需要判断字符串长度
					++n;
			}
			br.close();
		} catch (Exception ex) {
			ex.getStackTrace();
		}
		if (n == 0) {
			System.out.println("no sequence found");
			System.out.println("no need to align");
		} else if (n == 1) {
			System.out.println("only one sequence found");
			System.out.println("please try another file");
		} else
			System.out.println(n + " sequences found");
		return n;
	}

	// 将序列及对应序列名读入相应数组
	private void input() {
		sequence = new String[n];
		sequenceName = new String[n];
		int i = -1;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
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
		StringBuilder sb = new StringBuilder(s);
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

	// 总之，这个函数每一趟选出分别存在于中心序列和一个其它序列的符合某一条件的所有子串对
	// 暂时认为该函数和后缀树不存在bug
	private int centreAlignEachOne() {
		// name，四维分别表示：序列一；序列二；0为一号子串起始位置，1为二号子串起始位置，2为长度；子串对序号
		name = new int[1][n][3][]; // 暂时把n改为1，减少内存占用
		SuffixTree st = new SuffixTree();
		st.build(sequence[0] + "$"); // 末尾加'$'是为了不出现重复后缀，具体见后缀树
		for (int i = 0; i < n; ++i) {
			if (i == 0)
				continue;
			int index = 0;
			ArrayList<Integer> result = new ArrayList<>();
			while (index < sequence[i].length()) {
				// 在j序列中，将从index位置开始的序列段与中心序列进行匹配，返回中心序列中从a[0]开始的长度为a[1]的序列索引
				// 总之，a[0]为中心序列上子串的起始位置，a[1]为其长度
				int[] a = st.selectPrefixForAlignment(sequence[i], index);
				// 子串长度大于等于起始位置差，且在外围序列中，当前子串起始位置必大于前一子串起始位置
				if (a[1] > Math.abs(a[0] - index)) {
					result.add(a[0]);
					result.add(index);
					result.add(a[1]);
					index += a[1];
				} else
					index += a[1] > 0 ? a[1] : 1; // 可能是剪枝
			}
			int[][] substrings = new int[3][result.size() / 3];
			for (int j = 0, tmp = 0; j < result.size(); ++tmp)
				for (int k = 0; k < 3; ++k)
					substrings[k][tmp] = result.get(j++);
			name[0][i] = substrings;
		}
		return 0;
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

	// 计算除中心序列以外的其它序列的最大长度
	private int computeMaxLength() {
		int maxLength = 0;
		for (int i = 0; i < n; ++i)
			if (i != c && sequence[i].length() > maxLength)
				maxLength = sequence[i].length();
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

    public static class ExtremeMapper extends Mapper<Object, Text, NullWritable, Text> {
        int count = -1;
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
            if (value.charAt(0) == '>') {
                count++;
                sequenceName[count] = value.toString();
            } else {
                sequence[count] = sequence[count] == null ? value.toString() : sequence[count] + value.toString();
            }
            context.write(NullWritable.get(), value);
        }
    }

//	public static class ExtremeMapper extends Mapper<Object, Text, NullWritable, Text> {
//		int count = 0;
//		public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
//			if (value.charAt(0) == '>') {
//				Piname[count] = value.toString();
//			} else {
//				System.out.println("value:  "+value);
//				Pi[count] = value.toString();
//				count++;
//			}
//			context.write(NullWritable.get(), value);
//		}
//	}
}