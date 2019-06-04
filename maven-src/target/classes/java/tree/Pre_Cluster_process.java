package tree;

import java.io.*;
import java.util.*;

class Point {
    double meature;
    int i;
    int j;

    Point(double meature, int i, int j) {
        this.meature = meature;
        this.i = i;
        this.j = j;
    }
}

class Node {
    private String ID;
    private int Lnum;
    private int Rnum;
    private Node ParentNode;
    private Node LNode;
    private Node RNode;
    private int index;
    private Boolean HaveClass;

    public Node(String ID, int Lnum, int Rnum, Node ParentNode, Node LNode, Node RNode, int index, Boolean HaveClass) {
        this.ID = ID;
        this.Lnum = Lnum;
        this.Rnum = Rnum;
        this.ParentNode = ParentNode;
        this.LNode = LNode;
        this.RNode = RNode;
        this.index = index;
        this.HaveClass = HaveClass;
    }

    public void Reset(String ID, int Lnum, int Rnum, Node ParentNode, Node LNode, Node RNode, int index, Boolean HaveClass) {
        this.ID = ID;
        this.Lnum = Lnum;
        this.Rnum = Rnum;
        this.ParentNode = ParentNode;
        this.LNode = LNode;
        this.RNode = RNode;
        this.index = index;
        this.HaveClass = HaveClass;
    }

    // reset function
    public void Reset_Lnum(int Lnum) {
        this.Lnum = Lnum;
    }

    public void Reset_Rnum(int Rnum) {
        this.Rnum = Rnum;
    }

    public void Reset_ParentNode(Node ParentNode) {
        this.ParentNode = ParentNode;
    }

    public void Reset_LNode(Node LNode) {
        this.LNode = LNode;
    }

    public void Reset_RNode(Node RNode) {
        this.RNode = RNode;
    }

    public void Reset_index(int index) {
        this.index = index;
    }

    public void Reset_HaveClass(Boolean HaveClass) {
        this.HaveClass = HaveClass;
    }

    // get function
    public int Get_Lnum() {
        return this.Lnum;
    }

    public int Get_Rnum() {
        return this.Rnum;
    }

    public Node Get_ParentNode() {
        return this.ParentNode;
    }

    public Node Get_LNode() {
        return this.LNode;
    }

    public Node Get_RNode() {
        return this.RNode;
    }

    public int Get_index() {
        return this.index;
    }

    public Boolean Get_HaveClass() {
        return this.HaveClass;
    }
}

public class Pre_Cluster_process {
    private String inputFile = new String();
    private int rdNum = 0;
    private static int cluster_num;

    public Pre_Cluster_process(String inputFile, String reduceNumber) {
        // TODO Auto-generated constructor stub
        this.inputFile = inputFile;
        this.rdNum = Integer.parseInt(reduceNumber);
    }

    public static String ClusterFromMatrix(double[][] distance, String output, ArrayList<String> cluster_list, int cluster_list_size, int k) {
        try {
            Comparator<Point> comparator = (obj1, obj2) -> {  // for the sort
                Point point = obj2;
                if (point.meature > point.meature) {
                    return 1;
                } else if (point.meature < point.meature) {
                    return -1;
                } else {
                    return 0;
                }
            };
            ArrayList<Point> distance_list = new ArrayList<>();
            Set<Integer> Already_set = new HashSet();  // this set save the whole index of data
            for (int i = 0; i < cluster_list_size; i++) {  // translate the matrix to the list
                for (int j = i + 1; j < cluster_list_size; j++) {
                    Point point = new Point(distance[i][j], i, j);
                    distance_list.add(point);
                }
            }
            Collections.sort(distance_list, comparator);  // sort the list
            Set<Node> Root_set = new HashSet();
            Set<Set> Sum_set = new HashSet();
            ArrayList<Node> LeaveNode_list = new ArrayList<Node>();//用于保存叶子节点的数组
            for (int i = 0; i < cluster_list_size; i++) {
                LeaveNode_list.add(new Node("leave", 0, 0, null, null, null, i, null));
            }
            int Index = 0;
            while (Already_set.size() != cluster_list_size) {
                //i和j都未被处理过
                if (!Already_set.contains(distance_list.get(Index).i) && !Already_set.contains(distance_list.get(Index).j)) {
                    Set<Integer> new_set = new HashSet();// 申请新的集合用于存放i,j
                    new_set.add(distance_list.get(Index).i); //新集合添加i ,j
                    new_set.add(distance_list.get(Index).j);
                    Sum_set.add(new_set);   //全总集合添加新集合
                    Already_set.add(distance_list.get(Index).i);
                    Already_set.add(distance_list.get(Index).j);
                    Node InternalNode = new Node("Internal", 1, 1, null, LeaveNode_list.get(distance_list.get(Index).i),
                            LeaveNode_list.get(distance_list.get(Index).j), -1, null);   //申请一个内部节点，左右节点指向叶子节点，Parent节点为null
                    LeaveNode_list.get(distance_list.get(Index).i).Reset("leave", 0, 0, InternalNode,
                            null, null, distance_list.get(Index).i, true);         //修改叶子节点的信息
                    LeaveNode_list.get(distance_list.get(Index).j).Reset("leave", 0, 0, InternalNode,
                            null, null, distance_list.get(Index).j, true);
                    Root_set.add(InternalNode);        //将当前的根节点添加到根节点集合
                }// end if    //i和j有一个被处理过
                else if (Already_set.contains(distance_list.get(Index).i) && !Already_set.contains(distance_list.get(Index).j)) {
                    for (Set<Integer> seti : Sum_set) {
                        if (seti.contains(distance_list.get(Index).i)) {
                            seti.add(distance_list.get(Index).j);
                            Already_set.add(distance_list.get(Index).j);
                            break;
                        }
                    }// end for
                    Node CurrentNode = LeaveNode_list.get(distance_list.get(Index).i);  // current node
                    while (CurrentNode.Get_ParentNode() != null) {
                        CurrentNode = CurrentNode.Get_ParentNode(); //回溯到i节点的根节点
                    }
                    // now the CurrentNode is the root
                    Node InternalNode = new Node("Internal", CurrentNode.Get_Lnum() + CurrentNode.Get_Rnum(), 1, null, CurrentNode,
                            LeaveNode_list.get(distance_list.get(Index).j), -1, null);   //申请一个内部节点，
                    LeaveNode_list.get(distance_list.get(Index).j).Reset("leave", 0, 0, InternalNode,
                            null, null, distance_list.get(Index).j, true); //设置右子节点
                    CurrentNode.Reset_ParentNode(InternalNode); //修改左节点的父节点
                    Root_set.add(InternalNode);   //添加新的根节点
                    Root_set.remove(CurrentNode); //移除旧的根节点
                }// end if    //i和j有一个被处理过
                else if (!Already_set.contains(distance_list.get(Index).i) && Already_set.contains(distance_list.get(Index).j)) {
                    for (Set<Integer> setj : Sum_set) {
                        if (setj.contains(distance_list.get(Index).j)) {
                            setj.add(distance_list.get(Index).i);
                            Already_set.add(distance_list.get(Index).i);
                            break;
                        }
                    }//end  for
                    Node CurrentNode = LeaveNode_list.get(distance_list.get(Index).j);  // current node
                    while (CurrentNode.Get_ParentNode() != null) {
                        CurrentNode = CurrentNode.Get_ParentNode(); //回溯到i节点的根节点
                    }
                    // now the CurrentNode is the root
                    Node InternalNode = new Node("Internal", CurrentNode.Get_Lnum() + CurrentNode.Get_Rnum(), 1, null, CurrentNode,
                            LeaveNode_list.get(distance_list.get(Index).i), -1, null);   //申请一个内部节点，
                    LeaveNode_list.get(distance_list.get(Index).i).Reset("leave", 0, 0, InternalNode,
                            null, null, distance_list.get(Index).i, true); //设置右子节点
                    CurrentNode.Reset_ParentNode(InternalNode); //修改左节点的父节点
                    Root_set.add(InternalNode);   //添加新的根节点
                    Root_set.remove(CurrentNode); //移除旧的根节点
                }// end if
                else {   // in this part the Already_set no add element
                    Set<Integer> seti = new HashSet();
                    Set<Integer> setj = new HashSet();
                    for (Set<Integer> set : Sum_set) {
                        if (set.contains(distance_list.get(Index).i)) {
                            seti = set;
                            break;
                        }
                    }
                    for (Set<Integer> set : Sum_set) {
                        if (set.contains(distance_list.get(Index).j)) {
                            setj = set;
                            break;
                        }
                    }
                    if (seti.equals(setj)) { // if i and j in the same set
                        Index++;
                        continue;
                    }
                    Set<Integer> combine_set = new HashSet(); // combine the two set
                    for (Integer item : seti) {  // get the set's elements
                        combine_set.add(item);
                    }
                    for (Integer item : setj) {
                        combine_set.add(item);
                    }

                    Set<Set> tmp_set = new HashSet();////为了确保能够删除seti和setj
                    tmp_set.add(combine_set);
                    for (Set<Integer> set : Sum_set) {
                        if (set.equals(seti)) {
                            continue;
                        }
                        if (set.equals(setj)) {
                            continue;
                        }
                        tmp_set.add(set);
                    }
                    Sum_set = null;
                    Sum_set = tmp_set;

                    Node CurrentNodei = LeaveNode_list.get(distance_list.get(Index).i);  // current node i
                    Node CurrentNodej = LeaveNode_list.get(distance_list.get(Index).j);  // current node i
                    while (CurrentNodei.Get_ParentNode() != null) {
                        CurrentNodei = CurrentNodei.Get_ParentNode(); //回溯到i节点的根节点
                    }
                    while (CurrentNodej.Get_ParentNode() != null) {
                        CurrentNodej = CurrentNodej.Get_ParentNode(); //回溯到i节点的根节点
                    }
                    Node InternalNode = new Node("Internal", CurrentNodei.Get_Lnum() + CurrentNodei.Get_Rnum(),
                            CurrentNodej.Get_Lnum() + CurrentNodej.Get_Rnum(), null, CurrentNodei,
                            CurrentNodej, -1, null);   //申请一个内部节点，
                    CurrentNodei.Reset_ParentNode(InternalNode); //修改 父节点
                    CurrentNodej.Reset_ParentNode(InternalNode); //修改 父节点
                    Root_set.add(InternalNode);   //添加新的根节点
                    Root_set.remove(CurrentNodei); //移除旧的根节点
                    Root_set.remove(CurrentNodej); //移除旧的根节点
                }  // end else
                Index++;
            } // end while
            // now the Sum_set contains the class of the data

    	/*
    	 * This should have the on load balance to the sum_set to make the set size==rdNum
    	 * Root_set.size() == Sum_set.size()
    	 */
            if (Sum_set.size() > 3 * k) {
                while (Sum_set.size() > 3 * k) {
                    ArrayList<Set> set_list = new ArrayList<Set>(); //用于存储集合的列表
                    for (Set<Set> set : Sum_set) {
                        set_list.add(set); //向列表中添加元素
                    }
                    Comparator<Set> comparator2 = new Comparator<Set>() {   //比较函数
                        public int compare(Set obj1, Set obj2) {  // for the sort
                            Set test1 = (Set) obj1;
                            Set test2 = (Set) obj2;
                            if (test1.size() > test2.size()) {
                                return 1;
                            } else if (test1.size() < test2.size()) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                    };
                    Collections.sort(set_list, comparator2);//对集合列表进行排序
                    Set tmp = set_list.get(0);
                    set_list.get(0).addAll(set_list.get(1));//合并最小的两个集合
                    Sum_set.remove(tmp);//删除总集合中的最小两个
                    Sum_set.remove(set_list.get(1));//并添加新的集合进总集合中
                }
                int label = 0;
                BufferedWriter bw = new BufferedWriter(new FileWriter(output));  // write the class the the output file
                for (Set<Integer> set : Sum_set) {
                    for (int item : set) {
                        bw.write(label + "\t" + cluster_list.get(item));  // output is   label \t  name  \t  seq
                        bw.newLine();
                        bw.flush();
                    }
                    label++;
                }//end for
                cluster_num = Sum_set.size();
                //System.out.println("Sum_set.size()>3k the class number is " + Sum_set.size());
            }// end   Sum_set.size()>3*rdNum
            else if (Sum_set.size() >= k && Sum_set.size() <= 3 * k) {
                int label = 0;
                BufferedWriter bw = new BufferedWriter(new FileWriter(output));  // write the class the the output file
                for (Set<Integer> set : Sum_set) {
                    for (int item : set) {
                        bw.write(label + "\t" + cluster_list.get(item));  // output is   label \t  name  \t  seq
                        bw.newLine();
                        bw.flush();
                    }
                    label++;
                }//end for

                //System.out.println("rdNum<Sum_set.size()<3k the class number is " + Sum_set.size());
                cluster_num = Sum_set.size();
            }// end   rdNum<Sum_set.size()<=3*rdNum
            else if (Sum_set.size() < k) {
                while (Root_set.size() < k) {  //Set<Node> Root_set = new HashSet();
                    int max_size = 0;  //记录最大树
                    Node max_node = null;
                    for (Node root : Root_set) {//遍历所有的根节点获取节点数目最多的树
                        int node_number = root.Get_Lnum() + root.Get_Rnum();
                        if (node_number > max_size) {
                            max_size = node_number;
                            max_node = root;
                        }
                    }//end for
                    double LRrate = (double) max_node.Get_Lnum() / (double) max_node.Get_Rnum();
                    if (LRrate >= 0.5 && LRrate <= 2) {//左右子分支个数满足拆分条件，直接拆分根节点
                        max_node.Get_LNode().Reset_ParentNode(null);
                        max_node.Get_RNode().Reset_ParentNode(null);
                        Root_set.remove(max_node);
                        Root_set.add(max_node.Get_LNode());
                        Root_set.add(max_node.Get_RNode());
                    } else if (LRrate > 2) {
                        while (LRrate > 2) {
                            max_node = max_node.Get_LNode(); //修改节点及其分支数目
                            max_node.Reset_Rnum(max_node.Get_ParentNode().Get_Rnum() + max_node.Get_Rnum());
                            LRrate = (double) max_node.Get_Lnum() / (double) max_node.Get_Rnum();
                        }
                        max_node.Get_LNode().Reset_ParentNode(null);
                        max_node.Get_ParentNode().Reset_LNode(max_node.Get_RNode());
                        max_node.Get_RNode().Reset_ParentNode(max_node.Get_ParentNode());
                        Root_set.add(max_node.Get_LNode());//设置当前节点的左分支为根节点
                        while (max_node.Get_ParentNode() != null) {
                            max_node = max_node.Get_ParentNode();//向根节点回溯
                            max_node.Reset_Lnum(max_node.Get_LNode().Get_Lnum() + max_node.Get_LNode().Get_Rnum());
                            max_node.Reset_Rnum(max_node.Get_RNode().Get_Lnum() + max_node.Get_RNode().Get_Rnum());
                        }
                    }//end else
                    else if (LRrate < 0.5) {
                        while (LRrate < 0.5) {
                            max_node = max_node.Get_RNode();
                            max_node.Reset_Lnum(max_node.Get_ParentNode().Get_Lnum() + max_node.Get_Lnum());
                            LRrate = (double) max_node.Get_Lnum() / (double) max_node.Get_Rnum();
                        }
                        max_node.Get_RNode().Reset_ParentNode(null);
                        max_node.Get_ParentNode().Reset_RNode(max_node.Get_LNode());
                        max_node.Get_LNode().Reset_ParentNode(max_node.Get_ParentNode());
                        Root_set.add(max_node.Get_RNode());
                        while (max_node.Get_ParentNode() != null) {
                            max_node = max_node.Get_ParentNode();
                            max_node.Reset_Lnum(max_node.Get_LNode().Get_Lnum() + max_node.Get_LNode().Get_Rnum());
                            max_node.Reset_Rnum(max_node.Get_RNode().Get_Lnum() + max_node.Get_RNode().Get_Rnum());
                        }
                    } //end else
                }// end while
                // now the Root_set.size > rdNum 遍历每棵子树，写出聚类信息
                int label = 0;
                BufferedWriter bw = new BufferedWriter(new FileWriter(output));
                for (Node root : Root_set) {
                    Queue<Node> queue = new LinkedList<Node>(); //用队列实现层序遍历
                    queue.add(root);
                    Node current_node;
                    while (!queue.isEmpty()) {
                        current_node = queue.poll();
                        if (current_node.Get_LNode().Get_index() != -1) { //不等于-1为叶子节点
                            bw.write(label + "\t" + cluster_list.get(current_node.Get_LNode().Get_index()));  // output is   label \t  name  \t  seq
                            bw.newLine();
                            bw.flush();
                        } else {
                            queue.add(current_node.Get_LNode());
                        }// end if
                        if (current_node.Get_RNode().Get_index() != -1) { //不等于-1为叶子节点
                            bw.write(label + "\t" + cluster_list.get(current_node.Get_RNode().Get_index()));  // output is   label \t  name  \t  seq
                            bw.newLine();
                            bw.flush();
                        } else {
                            queue.add(current_node.Get_RNode());
                        }//end if
                    }// end while
                    label++;
                }
                //System.out.println("Sum_set.size()<rdNum the class number is " + Root_set.size());
                cluster_num = Root_set.size();
            }// end   rdNum.Sum_set.size()
            return "";
        } catch (IOException | IndexOutOfBoundsException | NullPointerException e) {
            return null;
        }
    }// end ClusterFromMatrix

    public int get_cluster_number() {
        return cluster_num;
    }


    public void CountTheDistance(ArrayList<String> sequences, int lineNumber, double[][] distance, double[] A) {//这个函数用来计算距离矩阵
        int i, j;
        double p;
        double temp;
        double distance_temp = 0;//这个在计算方差和均值的时候有用

        for (i = 0; i < lineNumber; i++) {
            for (j = 0; j < lineNumber; j++) {

                if (i < j) {
                    p = JukeCantor(sequences.get(i).split("\t")[1], sequences.get(j).split("\t")[1]);
                    p = 1.0 - 0.75 * p;
                    temp = Math.log(p);
                    distance_temp = -0.75 * temp;  //根据公式计算距离
                    distance[i][j] = distance_temp;
                } else if (i == j) {
                    distance[i][j] = 0;//对角线上为0
                } else {
                    distance[i][j] = distance[j][i];
                }
                //System.out.print(distance_temp+"\t"); //这里打印出距离矩阵可以在屏幕上显示
                A[i] += distance[i][j];//计算A数组
            }
        }
    }

    public double JukeCantor(String gene1, String gene2) {
        int rate = 0;
        int i;
        double result = 0.0;
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

    public static void AmendTheDistance(double[][] distance, double[] A, int lineNumber) {//在进化树构建的时候用来修正距离
        int i, j;
        for (i = 0; i < lineNumber; i++) {
            for (j = i + 1; j < lineNumber; j++) {
                distance[i][j] = distance[i][j] - (A[i] + A[j]) / (lineNumber - 2);//修正的时候根据公式要用到A 和distance
                distance[j][i] = distance[i][j];
            }
        }
    }


    public void Get_Cluster(String output, int line_number) throws IOException {
        int select_number = 0;
        if (line_number < 100) { // if the totle number is bigger than 100000 , we random select 10000 sequences
            select_number = line_number / 2;
        } else if (line_number < 10000) { // if the totle number is bigger than 100000 , we random select 10000 sequences
            select_number = line_number / 10;
        } else if (10000 <= line_number && line_number <= 100000) {    // if the number is less than 100000 , we random select 1 of the 10 number sequences
            select_number = 1000;
        } else if (line_number > 100000) {
            select_number = 3000;
        }

        ArrayList<String> cluster_list = new ArrayList<>();  //用于保存样本序列
        ArrayList<Integer> random_list = new ArrayList<>();  // the index of the samples
        Set<Integer> random_set = new HashSet();
        Random random = new Random();
        int count = 0;
        while (count < select_number) {
            int ran_num = random.nextInt(line_number);
            if (!random_set.contains(ran_num)) {
                count++;
                random_set.add(ran_num);
                random_list.add(ran_num);
            }
        }
        Collections.sort(random_list);

        // get the random data to cluster
        BufferedReader br = new BufferedReader(new FileReader(inputFile));   // the input data is key value format
        count = 0;
        int index_random_list = 0;
        String current_line;
        while (br.ready() && index_random_list < random_list.size()) {
            current_line = br.readLine();
            if (count == random_list.get(index_random_list)) {
                cluster_list.add(current_line);   // the current_line is key value format
                index_random_list++;
            }
            count++;
        }
        br.close();

        // the cluster_list is the key value format
        int cluster_list_size = cluster_list.size();
        double[][] distance = new double[cluster_list_size][cluster_list_size];
//        double[] A = new double[cluster_list_size];
//        CountTheDistance(cluster_list, cluster_list_size, distance, A);//计算距离矩阵以及A
//        AmendTheDistance(distance, A, cluster_list_size);//对距离矩阵进行修正
        String flag;
        flag = ClusterFromMatrix(distance, output, cluster_list, cluster_list_size, rdNum);
//        do {
//        } while (flag.equals(null));
    }
}
