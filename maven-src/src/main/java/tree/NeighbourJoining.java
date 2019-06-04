package tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class NeighbourJoining_Node {
    private NeighbourJoining_Node ParentNode;
    private NeighbourJoining_Node LNode;
    private NeighbourJoining_Node RNode;
    private String Tree;
    private int index;

    public NeighbourJoining_Node(int index, NeighbourJoining_Node ParentNode, NeighbourJoining_Node LNode, NeighbourJoining_Node RNode, String Tree) {
        this.ParentNode = ParentNode;
        this.LNode = LNode;
        this.RNode = RNode;
        this.index = index;
        this.Tree = Tree;
    }

    public void Reset(int index, NeighbourJoining_Node ParentNode, NeighbourJoining_Node LNode, NeighbourJoining_Node RNode, String Tree) {
        this.ParentNode = ParentNode;
        this.LNode = LNode;
        this.RNode = RNode;
        this.index = index;
        this.Tree = Tree;
    }

    // reset function
    public void Reset_ParentNode(NeighbourJoining_Node ParentNode) {
        this.ParentNode = ParentNode;
    }

    public void Reset_LNode(NeighbourJoining_Node LNode) {
        this.LNode = LNode;
    }

    public void Reset_RNode(NeighbourJoining_Node RNode) {
        this.RNode = RNode;
    }

    public void Reset_index(int index) {
        this.index = index;
    }

    public void Reset_Tree(String Tree) {
        this.Tree = Tree;
    }

    // get function
    public NeighbourJoining_Node Get_ParentNode() {
        return this.ParentNode;
    }

    public NeighbourJoining_Node Get_LNode() {
        return this.LNode;
    }

    public NeighbourJoining_Node Get_RNode() {
        return this.RNode;
    }

    public int Get_index() {
        return this.index;
    }

    public String Get_Tree() {
        return this.Tree;
    }

}

public class NeighbourJoining {

    String subTree;//  返回进化子树
    String present_seq;

    public void construct(List<String> seq_name, List<String> seq) throws IOException {//把子集合的序列名和序列数组作为参数传进来

        int setsize = seq.size();//获取序列的条数
        double[][] distance = new double[setsize][setsize]; //设置子集合的距离矩阵
        double[] A = new double[setsize];//距离矩阵每行的和
        ArrayList<NeighbourJoining_Node> LeaveNode_list = new ArrayList<>();
        for (int i = 0; i < setsize; i++) {//构建叶子节点列表
            LeaveNode_list.add(new NeighbourJoining_Node(i, null, null, null, ""));
        }

        CountTheDistance(seq, seq.size(), distance, A);//计算距离矩阵以及A
        MinDistance min_distance = new MinDistance(10000.0, 0, 0);
        AmendTheDistance(distance, A, setsize, min_distance);//对距离矩阵进行修正
        Set<Integer> Already_set = new HashSet();
        int iter = 0;
        while (iter != setsize - 1) {//依次合并两个节点
            //System.out.println("iter time is:" + iter);
            iter++;
            double Li = 0.0, Lj = 0.0;
            for (int z = 0; z < setsize; z++) {
                Li = Li + distance[min_distance.geti()][z] - distance[min_distance.getj()][z];
            }
            Lj = -Li;
            Li = (min_distance.getDistance() + Li) / 2;//分枝长度计算的时候是全总再加min_distance
            Lj = (min_distance.getDistance() + Lj) / 2; //获取了i , j 到父节点的分枝长度

            for (int k = 0; k < setsize; k++) { //更新矩阵
                if (k != min_distance.geti() && k != min_distance.getj()) {//当不是扫描到对正轴的时候
                    if (distance[k][min_distance.geti()] == 0) {
                        continue;
                    }
                    distance[min_distance.geti()][k] = (distance[min_distance.geti()][k] + distance[min_distance.getj()][k]
                            - min_distance.getDistance()) / 2;
                    distance[min_distance.getj()][k] = 0;//然后把下标大的横和列全部都变成0
                    distance[k][min_distance.getj()] = 0;
                    distance[k][min_distance.geti()] = distance[min_distance.geti()][k];
                } else {   //要么k=i ,或者k=j
                    distance[min_distance.geti()][k] = 0;
                    distance[min_distance.getj()][k] = 0;
                    distance[k][min_distance.getj()] = 0;
                    distance[k][min_distance.geti()] = 0;
                }
            }

            //更新完矩阵后，合并这两个节点， 1、两个均未出现过 2、一个出现过一个未出现过
            if (!Already_set.contains(min_distance.geti()) && !Already_set.contains(min_distance.getj())) {
                LeaveNode_list.get(min_distance.geti()).Reset_Tree(seq_name.get(min_distance.geti()));//叶子节点添加子结构
                LeaveNode_list.get(min_distance.getj()).Reset_Tree(seq_name.get(min_distance.getj()));
                NeighbourJoining_Node InternalNode = new NeighbourJoining_Node(-1,//构建一个内部节点
                        null,
                        LeaveNode_list.get(min_distance.geti()),
                        LeaveNode_list.get(min_distance.getj()),
                        "(" + LeaveNode_list.get(min_distance.geti()).Get_Tree() + ":" + Li + "," + LeaveNode_list.get(min_distance.getj()).Get_Tree() + ":" + Lj + ")"
                );
                LeaveNode_list.get(min_distance.geti()).Reset_ParentNode(InternalNode);//叶子节修改父节点
                LeaveNode_list.get(min_distance.getj()).Reset_ParentNode(InternalNode);
                InternalNode.Get_LNode().Reset_Tree(null);// 设置左右子树节点为空，减小空间消耗
                InternalNode.Get_RNode().Reset_Tree(null);
                Already_set.add(min_distance.geti());// 记录已处理节点
                Already_set.add(min_distance.getj());
            } else if (Already_set.contains(min_distance.geti()) && !Already_set.contains(min_distance.getj())) { // i 节点已经被处理了
                NeighbourJoining_Node CurrentNode = LeaveNode_list.get(min_distance.geti());
                while (CurrentNode.Get_ParentNode() != null) {
                    CurrentNode = CurrentNode.Get_ParentNode(); //回溯到i节点的根节点
                }
                LeaveNode_list.get(min_distance.getj()).Reset_Tree(seq_name.get(min_distance.getj()));
                NeighbourJoining_Node InternalNode = new NeighbourJoining_Node(-1,//构建一个内部节点
                        null,
                        CurrentNode,
                        LeaveNode_list.get(min_distance.getj()),
                        "(" + CurrentNode.Get_Tree() + ":" + Li + "," + LeaveNode_list.get(min_distance.getj()).Get_Tree() + ":" + Lj + ")"
                );
                CurrentNode.Reset_ParentNode(InternalNode);
                LeaveNode_list.get(min_distance.getj()).Reset_ParentNode(InternalNode);
                InternalNode.Get_LNode().Reset_Tree(null);// 设置左右子树节点为空，减小空间消耗
                InternalNode.Get_RNode().Reset_Tree(null);
                Already_set.add(min_distance.getj());// 记录已处理节点
            }//end if i contain
            else if (Already_set.contains(min_distance.getj()) && !Already_set.contains(min_distance.geti())) { // j 节点已经被处理了
                NeighbourJoining_Node CurrentNode = LeaveNode_list.get(min_distance.getj());
                while (CurrentNode.Get_ParentNode() != null) {
                    CurrentNode = CurrentNode.Get_ParentNode(); //回溯到i节点的根节点
                }
                LeaveNode_list.get(min_distance.geti()).Reset_Tree(seq_name.get(min_distance.geti()));//叶子节点添加子结构
                NeighbourJoining_Node InternalNode = new NeighbourJoining_Node(-1,//构建一个内部节点
                        null,
                        CurrentNode,
                        LeaveNode_list.get(min_distance.geti()),
                        "(" + LeaveNode_list.get(min_distance.geti()).Get_Tree() + ":" + Li + "," + CurrentNode.Get_Tree() + ":" + Lj + ")"
                );
                CurrentNode.Reset_ParentNode(InternalNode);
                LeaveNode_list.get(min_distance.geti()).Reset_ParentNode(InternalNode);
                InternalNode.Get_LNode().Reset_Tree(null);// 设置左右子树节点为空，减小空间消耗
                InternalNode.Get_RNode().Reset_Tree(null);
                Already_set.add(min_distance.geti());// 记录已处理节点
            }//end if j contain
            else if (Already_set.contains(min_distance.geti()) && Already_set.contains(min_distance.getj())) {
                NeighbourJoining_Node CurrentNodei = LeaveNode_list.get(min_distance.geti());
                while (CurrentNodei.Get_ParentNode() != null) {
                    CurrentNodei = CurrentNodei.Get_ParentNode(); //回溯到i节点的根节点
                }
                NeighbourJoining_Node CurrentNodej = LeaveNode_list.get(min_distance.getj());
                while (CurrentNodej.Get_ParentNode() != null) {
                    CurrentNodej = CurrentNodej.Get_ParentNode(); //回溯到i节点的根节点
                }
                NeighbourJoining_Node InternalNode = new NeighbourJoining_Node(-1,//构建一个内部节点
                        null,
                        CurrentNodei,
                        CurrentNodej,
                        "(" + CurrentNodei.Get_Tree() + ":" + Li + "," + CurrentNodej.Get_Tree() + ":" + Lj + ")"
                );
                CurrentNodei.Reset_ParentNode(InternalNode);
                CurrentNodej.Reset_ParentNode(InternalNode);
                CurrentNodei.Reset_Tree(null);
                CurrentNodej.Reset_Tree(null);
            }
            // 合并节点过程完成

            //  再次寻找最小值
            min_distance.setDistance(10000.0);//重新设置最小值的下标，后面的代码用来找新构建的最小值,因为扫描是从上到下从左到右，所以x一定是比y更小
            min_distance.seti(0);
            min_distance.setj(0);
            for (int i = 0; i < setsize; i++) { // 重新修改净分枝度
                A[i] = 0;
                for (int j = 0; j < setsize; j++) {
                    A[i] = A[i] + distance[i][j];
                }
            }
            for (int i = 0; i < setsize; i++) {
                for (int j = i + 1; j < setsize; j++) {
                    if (distance[i][j] == 0) {
                        continue;
                    }
                    distance[i][j] = distance[i][j] - (A[i] + A[j]) / (setsize - 2);//修正的时候根据公式要用到A 和distance
                    distance[j][i] = distance[i][j];
                    if (distance[i][j] < min_distance.getDistance() && distance[i][j] != 0) {//获取矩阵中最小的距离
                        min_distance.setDistance(distance[i][j]);
                        min_distance.seti(i);
                        min_distance.setj(j);
                    }// end if
                }//end for
            } //end for
        }//end while(Already_set.size()!=setsize)

        // 获取子树
        NeighbourJoining_Node CurrentNode = LeaveNode_list.get(0);
        while (CurrentNode.Get_ParentNode() != null) {
            CurrentNode = CurrentNode.Get_ParentNode(); //回溯到i节点的根节点
        }
        subTree = CurrentNode.Get_Tree();
        // 获取代表序列
        while (CurrentNode.Get_LNode() != null) {
            CurrentNode = CurrentNode.Get_LNode();
        }
        present_seq = seq.get(CurrentNode.Get_index());
    }

    public String getSubTree() {//用来获取子树
        return subTree;
    }

    public String getPresentNode() {//获取代表序列
        return present_seq;
    }

    public static void AmendTheDistance(double[][] distance, double[] A, int lineNumber, MinDistance min_distance) {//在进化树构建的时候用来修正距离
        int i, j;
        for (i = 0; i < lineNumber; i++) {
            for (j = i + 1; j < lineNumber; j++) {
                distance[i][j] = distance[i][j] - (A[i] + A[j]) / (lineNumber - 2);//修正的时候根据公式要用到A 和distance
                distance[j][i] = distance[i][j];
                if (distance[i][j] < min_distance.getDistance() && distance[i][j] != 0) {//获取矩阵中最小的距离
                    min_distance.setDistance(distance[i][j]);
                    min_distance.seti(i);
                    min_distance.setj(j);
                }
            }
        }
    }

    public static void CountTheDistance(List<String> sequences, int lineNumber, double[][] distance, double[] A) throws IOException {//这个函数用来计算距离矩阵
        int i, j;
        double p;
        double temp;
        double distance_temp = 0;//这个在计算方差和均值的时候有用

        for (i = 0; i < lineNumber; i++) {
            for (j = 0; j < lineNumber; j++) {

                if (i < j) {
                    p = JukeCantor(sequences.get(i), sequences.get(j));
                    p = 1.0 - 0.75 * p;
                    temp = Math.log(p);
                    distance_temp = -0.75 * temp;   //根据公式计算距离
                    distance[i][j] = distance_temp;
                } else if (i == j) {
                    distance[i][j] = 0;//对角线上为0
                } else {
                    distance[i][j] = distance[j][i];
                }
                //	System.out.print(distance_temp+"\t"); //这里打印出距离矩阵可以在屏幕上显示
                A[i] += distance[i][j];//计算A数组
            }
            //	System.out.println();
        }
    }

    public static double JukeCantor(String gene1, String gene2) {
        int rate = 0;
        int i;
        double result = 0.0;
        int length = gene1.length();
        if (gene1.length() > gene2.length()) {
            length = gene2.length();
        }
        for (i = 0; i < length; i++) {
            //System.out.print(gene1.charAt(i)+"  ");
            if (gene1.charAt(i) != gene2.charAt(i)) {
                rate++;
            }
        }
        result = (double) rate / (double) gene1.length();
        //System.out.print(result+"   ");
        return result;
    }


}
 