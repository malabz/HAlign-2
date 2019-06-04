package halign.centerstar;

import java.util.ArrayList;

/**
 * 队列类型，用来进行层次遍历时保存访问过的结点（地址）。
 *
 * @author 李慧妍
 * @version 2006年3月27日
 */
public class Queue {

    ArrayList<Object> Q = new ArrayList<Object>();     //声明并初始化一个ArrayList

    /**
     * 队列的插入方法：即在队尾插入一个树结点
     *
     * @param node - 树结点
     */
    public void insert(NewRoot node) {
        Q.add(node);                  //add是ArrayList提供的
    }

    /**
     * 队列的插入方法：即在队尾插入一个记录AC算法处理信息的结点
     *
     * @param I - Record类型的结点
     */
    public void insert(Record I) {
        Q.add(I);
    }

    /**
     * 队列的弹出方法，即从队首读取并删除一个树结点
     *
     * @return 队首的树结点
     */
    public NewRoot popfront() {
        NewRoot node;              //用一个数结点类型的变量node
        if (Q.size() == 0)          //size是ArrayList提供的.如果Q为空则
            node = null;              //node为空值
        else {                          //否则
            node = (NewRoot) Q.get(0);      //node等于队首节点
            Q.remove(0);              //并将队首结点删除。
        }
        return node;                  //返回node
    }

    /**
     * 队列的弹出方法：从队首读取并删除一个Record类型的节点。
     *
     * @return 队首的Record类对象
     */
    public Record popI() {
        Record I;                //用一个Record类型的变量I
        /*因为调用时已经保证队列不为空，因此在这里就不进行判断直接处理了*/
        I = (Record) Q.get(0);    //I等于队首结点
        Q.remove(0);            //将队首节点删除
        return I;               //返回I
    }

    /**
     * 队列的长度方法，利用ArrayList提供的size()方法返回队列长度
     *
     * @return 队列长度
     */
    public int size() {
        return Q.size();
    }

    /**
     * 队列的清除方法，利用ArrayList提供的clear()方法清空队列
     */
    public void clear() {
        Q.clear();
    }
}
