package halign.smithwaterman;

import jaligner.Alignment;
import jaligner.Sequence;
import jaligner.SmithWatermanGotoh;
import jaligner.matrix.MatrixLoader;
import jaligner.matrix.MatrixLoaderException;
import jaligner.util.SequenceParser;
import jaligner.util.SequenceParserException;

public class SmithWatermanTest {

    public static void main(String[] args) throws MatrixLoaderException, SequenceParserException {
//        String sequence1 = "MAGKR";
//        String sequence2 = "MAGK";
        String sequence1 = "GKGDPKKPRGKMSSYAFFVQTSREEHKKKHPDASVNFSEFSKKCSERWKTMSAKEKGKFEDMAKADKARYEREMKTYIPPKGE";
        String sequence2 = "MQDRVKRPMNAFIVWSRDQRRKMALENPRMRNSEISKQLGYQWKMLTEAEKWPFFQEAQKLQAMHREKYPNYKYRPRRKAKMLPK";
        Sequence s1 = SequenceParser.parse("A" + sequence1 + "A");
        Sequence s2 = SequenceParser.parse("A" + sequence2 + "A");
        Alignment align = SmithWatermanGotoh.align(s1, s2, MatrixLoader.load("BLOSUM80"), 0f, 0f);
        sequence1 = new String(align.getSequence1());
        sequence2 = new String(align.getSequence2());
        System.out.println(sequence1.substring(1, sequence1.length()-1));
        System.out.println(sequence2.substring(1, sequence2.length()-1));
//
//        Sequence s1 = SequenceParser.parse(sequence1);
//        Sequence s2 = SequenceParser.parse(sequence2);
//        Alignment align = SmithWatermanGotoh.align(s1, s2, MatrixLoader.load("BLOSUM80"), 0f, 0.5f);
//        sequence1 = new String(align.getSequence1());
//        sequence2 = new String(align.getSequence2());
//        System.out.println(sequence1);
//        System.out.println(sequence2);
    }
}
