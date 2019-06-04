import halign.centerstar.MatrixMSA;
import halign.centerstar.TreeMSA;
import halign.kband.KbandMSA;
import halign.suffixtree.ExtremeMSA;
import halign.suffixtree.SparkDNAMSA;
import halign.smithwaterman.LocalProteinMSA;
import halign.smithwaterman.SparkProteinMSA;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import tree.MSATree;
import tree.SparkTree;

import java.util.Objects;

public class main {

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		String inputFile;
		String outputFile;
		String outputDFS;
		int method;
		try {
			if (args.length == 0) {
				errorNote();
				return;
			}
			if (Objects.equals(args[0], "-localMSA")) { // msa mode, Single mode
				inputFile = args[1];
				outputFile = args[2];
				method = Integer.parseInt(args[3]);
				switch (method) {
					case 0:
						// suffix tree algorithm, which is the fastest, but only for DNA/RNA
						System.out.println("Running suffix alignment as single mode");
						new ExtremeMSA().start(inputFile, outputFile, null);
						break;
					case 1:
						// kband algorithm based on BLOSUM62 scoring matrix, only for Protein
						System.out.println("Running smithwaterman alignment as single mode");
						new LocalProteinMSA().start(inputFile, outputFile);
						break;
					case 2:
						// kband algorithm based on affine gap penalty, only for DNA/RNA
						System.out.println("Running kband alignment as single mode");
						new KbandMSA().start(inputFile, outputFile, null);
						break;
					case 3:
						// trie tree alignment algorithm which is slower and only for DNA/RNA
						System.out.println("Running trie tree alignment as single mode");
						new TreeMSA().start(inputFile, outputFile, null);
						break;
					case 4:
						// basic algorithm based on the similarity matrix, the slowest and only for DNA/RNA
						// but it is the most accurate in the case of lower sequences similarity
						System.out.println("Running similarity matrix alignment as single mode");
						new MatrixMSA().start(inputFile, outputFile, null);
						break;
				}
				long end = System.currentTimeMillis();
				System.out.println("Cost time: " + (end - start) + "ms");
				System.out.println("Successfully! The result is saved as: " + outputFile);
			} else if (Objects.equals(args[0], "-hadoopMSA")) { //Hadoop mode
				inputFile = args[1];
				outputFile = args[2];
				outputDFS = args[3];
				method = Integer.parseInt(args[4]);
				switch (method) {
					case 0:
						System.out.println("Running suffix tree alignment as hadoop mode");
						new ExtremeMSA().start(inputFile, outputFile, outputDFS);
						break;
					case 1:
						System.out.println("Sorry, smithwaterman alignment as hadoop mode had been canceled. " +
								"You may use Spark mode instead.");
						break;
					case 2:
						System.out.println("Running kband alignment as hadoop mode");
						new KbandMSA().start(inputFile, outputFile, outputDFS);
						break;
					case 3:
						System.out.println("Running trie tree alignment as hadoop mode");
						new TreeMSA().start(inputFile, outputFile, outputDFS);
						break;
					case 4:
						System.out.println("Running similarity matrix alignment as hadoop mode");
						new MatrixMSA().start(inputFile, outputFile, outputDFS);
						break;
				}
				long end = System.currentTimeMillis();
				System.out.println("Cost time: " + (end - start) + "ms");
				System.out.println("Successfully! The result is saved as: " + outputFile);
			} else if (Objects.equals(args[0], "-sparkMSA")) {
				inputFile = args[1];
				outputFile = args[2];
				method = Integer.parseInt(args[3]);
				switch (method) {
					case 0:
						System.out.println("Running suffix tree alignment as spark mode");
						SparkConf conf = new SparkConf().setAppName("SparkSuffixAutomation");
						conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
						conf.set("spark.kryoserializer.buffer.max", "2000m");
						conf.registerKryoClasses(new Class[]{SparkDNAMSA.class});
						JavaSparkContext jsc = new JavaSparkContext(conf);
						new SparkDNAMSA().start(jsc, inputFile, outputFile);
						jsc.stop();
						break;
					case 1:
						System.out.println("Running smithwaterman alignment as spark mode");
						conf = new SparkConf().setAppName("SparkProteinMSA");
						conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
						conf.set("spark.kryoserializer.buffer.max", "2000m");
						conf.registerKryoClasses(new Class[]{SparkProteinMSA.class});
						jsc = new JavaSparkContext(conf);
						new SparkProteinMSA().start(jsc, inputFile, outputFile);
						jsc.stop();
						break;
				}
				long end = System.currentTimeMillis();
				System.out.println("Cost time: " + (end - start) + "ms");
				System.out.println("Successfully! The result is saved as: " + outputFile);
			} else if (Objects.equals(args[0], "-localTree")) {
				inputFile = args[1];
				outputFile = args[2];
				System.out.println("Generating phylogenetic trees as local mode");
				new MSATree().start(inputFile, outputFile);
				long end = System.currentTimeMillis();
				System.out.println("Cost time: " + (end - start) + "ms");
				System.out.println("Successfully! The result is saved as: " + outputFile);
			} else if (Objects.equals(args[0], "-sparkTree")) {
				inputFile = args[1];
				outputFile = args[2];
				System.out.println("Generating phylogenetic trees as spark mode");
				SparkConf conf = new SparkConf().setAppName("SparkMSATree");
				conf.setMaster("local[16]");
				conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
				conf.set("spark.kryoserializer.buffer.max", "2000m");
				conf.registerKryoClasses(new Class[]{SparkTree.class});
				JavaSparkContext jsc = new JavaSparkContext(conf);
				new SparkTree().GenerateTree(jsc, inputFile, outputFile);
				jsc.stop();
			} else
				errorNote();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void errorNote() {
		String version = "HAlign2.1.jar";
		String suffix = " <mode> <input-file> <output-file> <algorithm>";
		System.out.println("Kindly note: error params.");
		System.out.println();
		System.out.println("1. if you are a single core user, command is: java -jar " + version + suffix);
		System.out.println("mode: -localMSA, -localTree.");
		System.out.println("input-file: local fasta format file, required.");
		System.out.println("output-file: local fasta format file, just a file name, required.");
		System.out.println("algorithm: 0, 1, 2, 3, or 4, optional.");
		System.out.println();
		System.out.println("2. if you are a hadoop user, command is: hadoop jar " + version + suffix);
		System.out.println("mode: -hadoopMSA.");
		System.out.println("input-file: local fasta format file, required.");
		System.out.println("output-file: local fasta format file, just a file name, required.");
		System.out.println("algorithm: 0, 1, 2, 3, or 4, optional.");
		System.out.println();
		System.out.println("3. if you are a spark user, command is: spark-submit --class main " + version + suffix);
		System.out.println("mode: -sparkMSA, -sparkTree.");
		System.out.println("input-file: local fasta format file, required.");
		System.out.println("output-file: local fasta format file, just a file name, required.");
		System.out.println("algorithm: 0, 1, required for '-sparkMSA' mode.");
		System.out.println();
		System.out.println("Visit https://github.com/ShixiangWan/HAlign2.0 for detailed usages. Thanks.");
	}
}
