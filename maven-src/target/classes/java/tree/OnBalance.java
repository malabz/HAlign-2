package tree;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class set_info {
    String label;
    int size;

    public set_info(String label, int size) {
        this.label = label;
        this.size = size;
    }

    public void Reset_size(int size) {
        this.size = size;
    }

    public int Get_size() {
        return this.size;
    }
}

public class OnBalance {

    String inputfile;

    public OnBalance(String inputfile) {
        this.inputfile = inputfile;
    }

    public void Balance(int cluster_num, String local_path) throws IOException {
        int flag = 0;
        int iteratorTime = 0;

        while (flag != 1) {
            BufferedReader br = new BufferedReader(new FileReader(inputfile));
            ArrayList<set_info> set_info_list = new ArrayList<>();
            for (int i = 0; i < cluster_num; i++) {
                set_info_list.add(new set_info(String.valueOf(i), 0));
            }

            while (br.ready()) {
                String[] line = br.readLine().split("\t");
                set_info_list.get(Integer.parseInt(line[0])).Reset_size(
                        set_info_list.get(Integer.parseInt(line[0])).Get_size() + 1);

            }//end while
            br.close();

            /*
             * 以上获取正确的集合类别信息,所有集合的信息被保存到list当中
             */
            int balance_value = 0;
            Comparator<set_info> comparator = (obj1, obj2) -> {
                set_info test1 = obj1;
                set_info test2 = obj2;
                if (test1.size > test2.size) {
                    return 1;
                } else if (test1.size < test2.size) {
                    return -1;
                } else {
                    return 0;
                }
            };

            Collections.sort(set_info_list, comparator);  //首先需要对set_info_list 根据size的大小进行排序

            ArrayList<Integer> new_list = new ArrayList<>();
            for (int i = 0; i < set_info_list.size() - 1; i++) { //排序后计算balance value
                balance_value = balance_value + (set_info_list.get(i).size - set_info_list.get(i + 1).size);
                new_list.add(set_info_list.get(i).size);
            }
            new_list.add(set_info_list.get(set_info_list.size() - 1).size);////把 set_info_list 中的size 复制给new_list
            new_list.set(1, new_list.get(0) + new_list.get(1));
            new_list.set(0, new_list.get(new_list.size() - 1) / 2);
            new_list.set(new_list.size() - 1, new_list.get(new_list.size() - 1) / 2);

            Collections.sort(new_list);  //对new_list 进行排序，并计算其balance value

            int new_balance_value = 0;
            for (int i = 0; i < set_info_list.size() - 1; i++) { //排序后计算 new balance value
                new_balance_value = new_balance_value + (new_list.get(i) - new_list.get(i + 1));
            }
            if (balance_value < 0) balance_value = -balance_value;
            if (new_balance_value < 0) new_balance_value = -new_balance_value;//由于是从小到大，所有balance value 的正负号需要调整


            if (new_balance_value < balance_value) {
                flag = 0;//flag等于0的时候表明有改进，需要继续迭代
                //重写数据
                String splitLabel = set_info_list.get(set_info_list.size() - 1).label;
                String splitToLable = set_info_list.get(0).label;
                int splitSize = set_info_list.get(set_info_list.size() - 1).size / 2;
                String combineLabel = set_info_list.get(0).label;
                String combineToLabel = set_info_list.get(1).label;
                iteratorTime++;
                inputfile = ReWriterFile(splitLabel, splitToLable, combineLabel, combineToLabel, splitSize, inputfile, iteratorTime);
            } else {
                flag = 1;//flag等于1的时候说明没有改进，退出while循环
                break;
            }
        }//end while

        BufferedReader br = new BufferedReader(new FileReader(inputfile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(local_path + "/OnBalance_OutPut"));
        while (br.ready()) {
            bw.write(br.readLine());
            bw.newLine();
            bw.flush();
        }
        br.close();
        bw.close();
    }// end Balance

    public String ReWriterFile(String splitLabel, String splitToLabel,
                               String combineLabel, String combineToLabel, int splitSize, String inputfile, int iteratorTime) throws IOException {
        String fw = "tmp-" + iteratorTime;
        BufferedReader br = new BufferedReader(new FileReader(inputfile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(fw));
        while (br.ready()) {
            String[] line = br.readLine().split("\t");
            if (line[0].equals(splitLabel) && splitSize > 0) {
                splitSize--;
                bw.write(line[0] + "\t" + line[1] + "\t" + line[2]);
                bw.newLine();
                bw.flush();
            } else if (line[0].equals(splitLabel) && splitSize <= 0) {
                bw.write(splitToLabel + "\t" + line[1] + "\t" + line[2]);
                bw.newLine();
                bw.flush();
            } else if (line[0].equals(combineLabel)) {
                bw.write(combineToLabel + "\t" + line[1] + "\t" + line[2]);
                bw.newLine();
                bw.flush();
            } else {
                bw.write(line[0] + "\t" + line[1] + "\t" + line[2]);
                bw.newLine();
                bw.flush();
            }
        }
        br.close();
        bw.close();
        return fw;
    }
}
