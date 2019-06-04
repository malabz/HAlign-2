package halign.smithwaterman;

import jaligner.Alignment;
import jaligner.Sequence;
import jaligner.SmithWatermanGotoh;
import jaligner.matrix.MatrixLoader;
import jaligner.matrix.MatrixLoaderException;
import jaligner.util.SequenceParser;
import jaligner.util.SequenceParserException;
import utils.IOUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of Protein Multiple Sequence Alignment based on Smith Waterman Algorithm.
 *
 * @author ShixiangWan
 * */
public class LocalProteinMSA {

    private static List<String> s_out1 = new ArrayList<>();
    private static List<String> s_out2 = new ArrayList<>();

    /**
     * Run smithwaterman multiple sequence alignment.
     *
     * @param inputFile input file "path+name", fasta format.
     * @param outputFile output file "path+name", fasta format.
     * */
    public void start(String inputFile, String outputFile) {
        System.out.println("Loading data ... " + inputFile);
        long startTime = System.currentTimeMillis();

        IOUtils formatUtils = new IOUtils();
        formatUtils.readFasta(inputFile, false);
        List<String> s_key = formatUtils.getS_key();
        List<String> s_val = formatUtils.getS_val();

        //The first sequence is used as the centerstar sequence.
        String sequence1 = s_val.get(0);
        int sequenceLen1 = sequence1.length();
        int sequence1_id = 0;

        // The "sequence1" is used as the "root", which compared with other sequences.
        // The first alignment is preserved as "s_out1".
        int total_num = s_val.size();
        for (String ignored : s_val) {
            s_out1.add("");
            s_out2.add("");
        }
        System.out.println("MultiThread MSA ... ");
        int taskSize = 16;
        ExecutorService pool = Executors.newFixedThreadPool(taskSize);
        for (int i = 0; i < total_num; i++) {
            String line = s_val.get(i);
            AlignThread alignThread = new AlignThread(i, sequence1, line);
            pool.execute(new Thread(alignThread));
        }
        pool.shutdown();
        while(!pool.isTerminated());
        System.out.println("Aligned, " + (System.currentTimeMillis() - startTime) + "ms");

        // Statistic the alignments with centerstar sequence, and get the merge results:
        // - centerSpaces: record the spaces in each alignment.
        // - oneSpace[]: record the spaces in centerstar alignment.
        int index;
        int oneSpaceLen = sequenceLen1 + 1;
        int oneSpace[] = new int[oneSpaceLen];
        int centerSpaces[][] = new int[total_num][oneSpaceLen];
        for (int i = 0; i < total_num; i++) {
            String line = s_out1.get(i);
            index = 0;
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == '-') {
                    centerSpaces[i][index]++;
                } else {
                    index++;
                }
                // bug fixed.
                if (oneSpace[index] < centerSpaces[i][index]) {
                    oneSpace[index] = centerSpaces[i][index];
                }
            }
        }

        // Get the centerstar alignment "sequence1".
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < oneSpaceLen; i++) {
            for (int j = 0; j < oneSpace[i]; j++) stringBuilder.append('-');
            if (i != oneSpaceLen - 1) stringBuilder.append(sequence1.charAt(i));
        }
        sequence1 = stringBuilder.toString();

        // Merge array based on "centerSpace", "oneSpace" and "s_out2",
        // and align all sequences.
        System.out.println("Merging sequences ... ");
        for (int i = 0; i < total_num; i++) {
            String line = s_out2.get(i);
            // Record the position of inserted spaces.
            int position = 0;
            for (int j = 0; j < oneSpaceLen; j++) {
                int gap = oneSpace[j] - centerSpaces[i][j];
                // bug fixed.
                //position += centerSpaces[i][j];
                if (gap > 0) {
                    if (position < line.length()) {
                        // Insert spaces.
                        line = line.substring(0, position).concat(insert(gap)).concat(line.substring(position));
                    } else {
                        // For some special sequences out of limited length, which need to
                        // be cut off and broken off insertion.
                        if (line.length() > sequence1.length()) {
                            line = line.substring(0, sequence1.length());
                        }
                        break;
                    }
                }
                position += gap + 1;
            }

            // Some special sequences is not equal to others after inserting spaces,
            // which need to add some extra spaces.
            if (line.length() < sequence1.length()) {
                line = line.concat(insert(sequence1.length() - line.length()));
            }
            s_out2.set(i, line);
        }

        // delete inconsistent results.
        List<Integer> deleteIndex = new IOUtils().deleteSimilarityOutput(s_key, s_val, s_key, s_out2);
        if (deleteIndex.size() > 0)
            System.out.println("There are "+deleteIndex.size()+" low similarity sequences (deleted):");
        for (int deleted : deleteIndex) {
            System.out.println("deleted sequences: "+s_key.get(deleted));
            s_key.set(deleted, "");
        }

        // Save alignment to hard disk.
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            for (int i = 0; i < s_key.size(); i++) {
                if (s_key.get(i).equals("")) continue;
                bufferedWriter.write(s_key.get(i)+ "\n");
                bufferedWriter.write(s_out2.get(i)+ "\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("Saved, " + (System.currentTimeMillis() - startTime) + "ms");
    }


    private class AlignThread implements Runnable {
        private int id;
        private String sequence1;
        private String sequence2;

        AlignThread(int id, String sequence1, String sequence2) {
            this.id = id;
            this.sequence1 = sequence1;
            this.sequence2 = sequence2;
        }

        @Override
        public void run() {
            try {
                Sequence s1 = SequenceParser.parse("A" + sequence1 + "A");
                Sequence s2 = SequenceParser.parse("A" + sequence2 + "A");
                Alignment align = SmithWatermanGotoh.align(s1, s2, MatrixLoader.load("BLOSUM80"), 0.0f, 0.0f);
                String o1 = new String(align.getSequence1());
                String o2 = new String(align.getSequence2());
                o1 = o1.substring(1, o1.length()-1);
                o2 = o2.substring(1, o2.length()-1);
                s_out1.set(id, o1);
                s_out2.set(id, o2);
//                Sequence s1 = SequenceParser.parse(sequence1);
//                Sequence s2 = SequenceParser.parse(sequence2);
//                Alignment align = SmithWatermanGotoh.align(s1, s2, MatrixLoader.load("BLOSUM80"), 0f, 0.5f);
//                sequence1 = new String(align.getSequence1());
//                sequence2 = new String(align.getSequence2());
//                s_out1.set(id, sequence1);
//                s_out2.set(id, sequence2);
            } catch (SequenceParserException | MatrixLoaderException e) {
                e.printStackTrace();
            }
        }
    }


    private String insert(int num) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int k = 0; k < num; k++) {
            stringBuilder.append('-');
        }
        return stringBuilder.toString();
    }
}
