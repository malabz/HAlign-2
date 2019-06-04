package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IOUtils {
	private List<String> s_key = new ArrayList<>();
	private List<String> s_val = new ArrayList<>();
	private int maxLength;
	private int allNum;

	public int getMaxLength() {
		return maxLength;
	}

	public int getAllNum() {
		return allNum;
	}

	public List<String> getS_key() {
		return s_key;
	}

	public List<String> getS_val() {
		return s_val;
	}

	public void readFasta(String input, boolean isDNA) {
		try {
			BufferedReader brReader = new BufferedReader(new FileReader(input));
			StringBuilder stringBuilder = new StringBuilder();
			int max = 0;
			int all = 0;
			while(brReader.ready()) {
				String line = brReader.readLine();
				if (line.equals("")) continue;
				line = line.trim();
				if (line.charAt(0) == '>') {
					s_key.add(line);
					all++;
					if (stringBuilder.length() != 0) {
                        String sbString;
                        if (isDNA) sbString = formatDNA(stringBuilder.toString());
                        else sbString = stringBuilder.toString();
						int len = sbString.length();
						if (max < len) max = len;
						s_val.add(sbString);
						stringBuilder.setLength(0);
					}
				} else {
					stringBuilder.append(line);
				}
			}
			if (stringBuilder.length() != 0) {
				String sbString;
				if (isDNA) sbString = formatDNA(stringBuilder.toString());
				else sbString = stringBuilder.toString();
				int len = sbString.length();
				if (max < len) max = len;
				s_val.add(sbString);
			}
			maxLength = max;
			allNum = all;
			brReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Integer> deleteSimilarityOutput(List<String> in_key, List<String> in_val,
                                                List<String> out_key, List<String> out_val) {
        List<Integer> deleteIndex = new ArrayList<>();
        for (int i=0; i<in_key.size(); i++) {
            String outVal = out_val.get(i).replace("-", "");
            if (in_key.get(i).equals(out_key.get(i)) && !in_val.get(i).equals(outVal)) {
                deleteIndex.add(i);
            }
        }
		return deleteIndex;
	}

    public void formatKVFasta(String input, String output) {
        try {
            readFasta(input, false);
            BufferedWriter bwWriter = new BufferedWriter(new FileWriter(output));
            int total = s_key.size();
            for (int i=0; i<total; i++) {
                bwWriter.write(s_key.get(i)+"\t"+s_val.get(i)+"\n");
            }
            bwWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public String formatDNA(String s) {
		s = s.toLowerCase();
		s = s.replace('u', 't');
		StringBuffer sb = new StringBuffer(s);

		for (int i = 0; i < sb.length(); i++) {
			switch (sb.charAt(i)) {
				case 'a':
					break;
				case 'c':
					break;
				case 'g':
					break;
				case 't':
					break;
				case 'n':
					break;
				default:
					sb = sb.replace(i, i + 1, "n");
			}
		}
		return (sb.toString());
	}
}
