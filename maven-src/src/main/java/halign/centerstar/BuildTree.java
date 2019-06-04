package halign.centerstar;

/**
 * Build Tree.
 *
 * @author Huiyan Li, Quan Zou
 */
public class BuildTree {

    private Queue Q = new Queue();             //常用变量：队列Q
    private int idCounter = 1;                         //常用变量：结点的ID值加法器

    /*public String readStr() throws IOException {
        BufferedReader sb = new BufferedReader(new InputStreamReader(System.in));
        return sb.readLine();
    }

    public int readInt() throws IOException {
        String s = readStr();
        return Integer.parseInt(s);
    }*/

    /**
     * 输出方法：用于输出Aho-Corasick算法的结果
     * 处理的是本类中的Q对列，其中保存了记录Aho-Corasick处理结果信息的
     * Record类对象I（详见本类方法ACsearch()）。
     */
    public int[][] out() {
        Position p;          //Position类的变量，用于读取保存的位置
        Record I;            //Record类的变量，用于读取记录的信息
        int i = 0;               //整数i用户循环计数
        int[][] all = new int[2][Q.size() + 1];    //#############有待修改############################33

        while (Q.size() != 0) {                 //当队列不空时，循环
            I = Q.popI();                    //读取队首的记录
            p = (Position) I.name.get(0); //读取该模式号。
            all[0][i] = p.position;
            all[1][i] = I.site;
            i++;
        }
        all[0][i] = 0;
        all[1][i] = 0;
        return (all);
    }

    /**
     * 建树方法：根据字符串P中每段划分的起始位置和长度，在关键字树trie上
     * （可能是子树）递归地建立相应的分支。
     *
     * @param P    - 从键盘读入的字符串
     * @param i    - 准备读入的子串的起始位置（在某一段划分中）
     * @param r    - 未读的子串的长度
     * @param trie - 已经部分建立的关键字树（子树树根地址）
     * @param pid  - 表示本次正在处理的是P中的哪段，编号从1开始。该参数
     *             纯粹为了输出方便，其实只要划分结束，在任何时候都可
     *             以计算出来。
     */
    public void build(String P, int i, int r, int pid, NewRoot trie) {
        if (r > 0) {           //如果还有没有读入的字符，则执行下面的语句
            NewRoot son = trie.searchSon(P.charAt(i)); /*在trie的儿子中查找是否有P中位置i的字符出现*/
            if (son == null) {                  //如果没有的话
                son = new NewRoot(P.charAt(i));    /*根据位置和字符建立一个新儿子结点*/
                /*注意，下面这两句和ID相关的话可以删除。*/
                son.ID = idCounter;            //新结点的ID为id
                idCounter++;           //然后id+1以产生下一个ID号
                trie.addSon(son);     //将新结点加入trie的儿子当中
            }
            if (r == 1) {             //如果处理到本段的最后一个字符，则
                son.setPos(pid);    //在该结点中保存本段的段号。
            }
            /*将son作为新子树树根，并根据P中下一个位置的字符继续建树，未读子串长度-1*/
            build(P, i + 1, r - 1, pid, son);
        }
    }

    /**
     * 建立失效链接的方法：根据队列Q中的内容和树根trie层次地建立起每个结
     * 点的失效链接
     *
     * @param trie - 关键字树（树根地址）
     */
    public void failLink(NewRoot trie) {
        int i;                      //整数i用于以后的循环计数
        int level = 1;              /*整数level用于计算结点层次。
                                                  同时，它还永远等于即将遍历的
                                                  层次。这里是第一层。*/
        Q.clear();                    //系统队列清零备用

        NewRoot son;                    //建立一个树结点类型的变量son
        for (i = 0; i < trie.sons.size(); i++) {   //初始化第一层结点
            son = (NewRoot) trie.sons.get(i);  /*每一个结点，即
                                                              根的儿子*/
            son.nv = trie;             //其失效链接指向树根
            son.level = level;       //其层次为1（因为是第一层）
            Q.insert(son);           //将该儿子插入队列。
        }
        level++;                     //层次level加1，备用。

        NewRoot NewRoot= new NewRoot();         //新建一个树结点
        NewRoot.v = 'L';                /*该结点的字符为‘L’，即
                                               “分层符”表示树的一层。
                                                    该符号用于判断每一层
                                                    遍历是否结束以便计算
                                                level的值。*/
        Q.insert(NewRoot);              //将该节点插入队列。
        NewRoot w;                      //建立一个树结点类型的变量w

        while (Q.size() != 0) {         //当队列不为空时
            NewRoot= Q.popfront();     //读出队列的队首结点
            if (NewRoot.v != 'L') {      //如果该结点不是“分层符”则，
                for (i = 0; i < NewRoot.sons.size(); i++) {//对该结点的
                    son = (NewRoot) NewRoot.sons.get(i);/*每一个儿子
                                                                         进行如下操作*/
                    w = NewRoot.nv;     /*看这个儿子父亲（即队首结点）的失效链接*/
/*（这是下面这句的注释）如果该链接不是树根且没有标定该儿子字符的儿子，则*/
                    while (w != trie && w.searchSon(son.v) == null)
                        w = w.nv;      //接着看这个失效链接的失效链接
/*可以看出，循环停止条件是失效链接指向树根，或存在标定该儿子字符的儿子*/
                    if (w.searchSon(son.v) != null) {/*如果w存在
                                                                   标定该son字符的儿子*/
                        son.nv = w.searchSon(son.v); /*该son
                                                                         的失效链接指向标定该
                                                                      son字符的w的儿子*/
                    } else         //否则，即w的儿子中不存在这样的标定
                        son.nv = trie; //则该son的失效链接指向树根
/*（这是下面if语句的注释）如果该son不是每个分段的结束结点，但其失效链接最终
   会指向某个Pi的末端结点（不一定直接指向），则该son的positions中插入-1，
   表示通过这个son结点找失效链接的话最终能够找到一个代表某个模式的结点，无论
   这个节点或模式的尾节点是否是树叶。*/
                    if (son.positions.size() == 0 && son.nv.positions.size() != 0)
                        son.setPos(-1);

                    son.level = level; //将level值保存到son中去。
                    Q.insert(son);	  /*将该儿子保存在队列中，以便
                                                               下一个层测的层次遍历*/
                }
            } else { //否则，即队列的首结点是“分层符”L的话，
                if (Q.size() != 0) { /*看队列是否为空，即刚弹出的队首
                                                         是否是最后一个结点，如果不是*/
                    level++;        //level加1，即该遍历下一层了
                    Q.insert(NewRoot); /*将这个有“分层符”的结点再插到
                                                             队尾，这样队尾前的都是同以层
                                                             的树结点，层号都是刚才加1后
                                                             的level*/
                }
            }
        }
    }

    /**
     * Aho-Corasick搜索方法：根据关键字树trie对字符串T进行处理
     *
     * @param T    - 读入的字符串
     * @param trie - 建立的关键字树
     */
    public void ACsearch(String T, NewRoot trie) {
        int i;                                //整数i用于循环计数
        Position p;                   //Position类的p用于暂存位置
        NewRoot NewRoot= trie;            //树结点Nodea先等于树根trie
        NewRoot son;                    //树结点变量son以后用。
        Record I;                    //Record类型变量用于保存结果
        Q.clear();                   //队列清零备用
        for (i = 0; i < T.length(); i++) { //对于T中的每一个字符T(i)
            son = NewRoot.searchSon(T.charAt(i)); /*看看Nodea的儿子
                                                             里有没有标定T(i)的*/
            while (son == null) {      //如果没有的话
                if (NewRoot == trie)       //如果Nodea是树根，
                    break;             //则跳出。
                NewRoot= NewRoot.nv;        //否则Nodea等于它的失效链接
                son = NewRoot.searchSon(T.charAt(i)); /*继续在它的
                                                                    儿子中寻找T(i)*/
            }

            if (NewRoot == trie && son == null) /*如果Nodea是树根，而且
                                                   刚才跳出了（这样son才会为空）*/
                continue;         /*则结束外层循环，继续看T中下一个
                                                   字符*/
            NewRoot= son;          //Nodea等于它那个标定T(i)的儿子
            if (NewRoot.positions.size() != 0) { /*如果该结点可能代表
                                                               一个模式Pi*/
                p = (Position) NewRoot.positions.get(0); /*检查
                                                                  positions的
                                                                       首位以判断这个
                                                                       结点是否直接代
                                                                       表着某个模式*/
                if (p.position != -1) { /*如果positions的首位不是
                                                       -1，即意味着该结点直接代表着
                                                           某个模式Pi的话*/
/*（这是下面这句的注释）将该结点所代表的模式在T中的起始位置计算并记录在I中，
   同时将该结点所代表的模式号也记录下来。*/
                    I = new Record(i + 2 - NewRoot.level, NewRoot.positions);
                    Q.insert(I);             //将I保存在队列中。
                }
                son = NewRoot.nv;            //继续看Nodea的失效链接
/*（这是下面这句的注释）如果该链接不为空而且不指向根节点，而且可能代表着某个
   模式的话，则进行循环，该循环找到这个结点所连接到的所有可能模式*/
                while (son != null && son != trie && son.positions.size() != 0) {
                    p = (Position) son.positions.get(0);/*看该
                                                                  链接的positions首位*/
                    if (p.position != -1) { /*如果首位不是-1，即该
                                                             失效链接直接代表着某个模式，则*/
/*（这是下面这句的注释）将该结点所代表的模式在T中的起始位置计算并记录在I中，
   同时将该结点所代表的模式号也记录下来。*/
                        I = new Record(i + 2 - son.level, son.positions);
                        Q.insert(I);     //将I保存在队列中。
                        son = son.nv;    //继续看它的失效链接
                    }
                }
            }
        }
    }
}
