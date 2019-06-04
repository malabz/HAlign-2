package halign.centerstar;

import java.util.ArrayList;

public class NewRoot {
    /*如果两个划分存在相同前缀，因为该前缀中的符号在树中有唯一的位置，故用
             一个ArrayList的positions来记录该相同符号在P中所出现的位置*/
    ArrayList<Position> positions = new ArrayList<Position>();
    char v;    //结点所代表的符号
    NewRoot nv;  //失效链接中该结点的对应结点（地址）
    ArrayList<NewRoot> sons = new ArrayList<NewRoot>();	/*用来记录该节点所有的儿子结点（地址）*/
    int ID;	  /*为了方便人为判断关键字树的建立是否正确，为每个结点引入一个ID以助判断结点之间的关系。注：以后可以删除，对结果无甚影响*/
    int level;	/*结点所在的层测。为了在AC算法中能得到Pi在T中的开始位置而设立。该位置用当前T中位置号减去当前结点的层次即可得到。*/

    /**
     * 构造方法：初始化一个根结点，nv指向根结点本身
     */
    NewRoot() {
        v = 'r';    //结点的符号是跟
        nv = this;  //结点的失效链接是它本身
        level = 0;
    }

    /**
     * 构造方法：根据当前符号来初始化一个结点（通常是儿子）
     */
    NewRoot(char c) {
        v = c;  //设结点的符号为c
    }

    /**
     * 该方法把相同前缀中的符号在P中出现的新位置保存起来
     *
     * @param posit - 符号在P中的位置
     */
    public void setPos(int posit) {
        Position posrec = new Position(); //新建一个Position对象
        posrec.position = posit;    //该对象的设置为整数posit
        positions.add(posrec);    //将这个对象加入到positions中。
    }

    /**
     * 该方法为结点添加一个新儿子
     *
     * @param newSon - 新儿子
     */
    public void addSon(NewRoot newSon) {
        sons.add(newSon);   //add方法有ArrayList提供
    }

    /**
     * 该方法根据输入的符号在结点的所有儿子中查询是否已经有代表该符号的儿
     * 　子出现。有则返回该儿子（地址），无则返回空
     * @param c - 要查询的符号
     * @return son - 查询结果，查到则返回该儿子，无则为空
     */
    public NewRoot searchSon(char c) {
        NewRoot son = null;
        for (int i = 0; i < sons.size(); i++) {
            son = (NewRoot) sons.get(i);    //查询每一个儿子
            if (son.v == c)                //如果儿子的字符和c一样
                break;                    //中断循环
            else
                son = null;                //否则儿子为空
        }
        return son;                        //返回查询结果
    }

}
