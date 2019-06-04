package halign.suffixtree;

import java.util.*;

/**
 * Implementation of suffix tree with Ukkonen construction method.</br></br>
 *
 * From: http://www.oschina.net/translate/ukkonens-suffix-tree-algorithm-in-plain-english.</br>
 * Edges are used to save char info in the paper, but we use nodes to save char info here.
 * There is no difference. Meanwhile, the construction of tree is save in child nodes and
 * brother nodes. Hence, if we want to traverse all child nodes of the node, we need to
 * find its direct child nodes, then traverse their brother nodes to find all child nodes.
 *
 * @author ShixiangWan
 */
public class SuffixTree {

    // Root node. Root node does not have "chars", so we do "new char[0]".
	private Node root = new Node(new char[0]);

	// Active point, as a triple: (active_node, active_edge, active_length),
	// - active_node: active_node, as a node.
    // - active_edge: active edge, but we use nodes to express edges.
    // - active_length: active length.
	private ActivePoint activePoint = new ActivePoint(root, null, 0);

    // Remainder, which expresses what numbers of suffixes needed to be inserted.
	private int reminder = 0;

    private int minMatchLen = 15;

	/**
	 * Build suffix tree of a word.
	 *
	 * @param word word for building suffix tree.
	 */
	public void build(String word) {
		int index = 0;
		char[] chars = word.toCharArray();
        // create suffix circularly
		while (index < chars.length) {
            // record current position. Note: "int a=b++;" is equal to "int a=b;b++;", NOT "a=b+1"!
			int currentIndex = index++;
            // current suffix char
			char w = chars[currentIndex];

            // Find if the node who saves current suffix char exits?
			if (find(w)) {
                // Exits, do "reminder+1，activePoint.length+1" and "continue".
				reminder++;
				continue;
			}

			// If no exits and "reminder==0", before the character is not left before the other
            // inserted with the suffix characters, so insert the suffix characters directly.
			if (reminder == 0) {
				// Directly in the current active node can be inserted into a node
				// Where the inserted node contains the character that is the last character of the
                // string from the current character, here is an optimization, and optimization
                // reference: http://blog.csdn.net/v_july_v/article/details/6897097 (3.6, induction,
                // reflection, optimization)
				Node node = new Node(Arrays.copyOfRange(chars, currentIndex, chars.length));
				node.label = currentIndex;

				// If there is no child node in the current activity point, the new node can be
                // used as its child node, otherwise it will traverse the child node (save through
                // the brother node).
				Node child = activePoint.point.child;
				if (null == child) {
					activePoint.point.child = node;
				} else {
					while (null != child.brother) {
						child = child.brother;
					}
					child.brother = node;
				}
				node.father=activePoint.point;
			} else if(activePoint.index==null) {
				// Just the former side of the reminder of those who, insert a new character,
                // the direct establishment of a new side of the storage of the characters, and
                // before the lack of supplement.
				Node node = new Node(Arrays.copyOfRange(chars, currentIndex, chars.length));

				node.label = currentIndex - reminder;

                // If there is no child node in the current activity point, the new node can be
                // used as its child node, otherwise it will traverse the child node (save through
                // the brother node).
				Node child = activePoint.point.child;
				if (null == child) {
					activePoint.point.child = node;
				} else {
					while (null != child.brother) {
						child = child.brother;
					}
					child.brother = node;
				}
				node.father = activePoint.point;

				// After division is completed, it is necessary to distinguish between rules 1 and 3.
				// According to rule 1 for processing.
                // The active node is the root node.
				if (root == activePoint.point) {
					// activePoint.point == root
					// According to rule 3 for processing.
				}
                // No suffix node, the active node is root
				else if (null == activePoint.point.suffixNode) {
					activePoint.point = root;
				}
                // Otherwise the active node becomes the suffix node of the current active node
				else {
					activePoint.point = activePoint.point.suffixNode;
				}
				// Both the active and active edges are reset
				activePoint.index = null;
				activePoint.length = 0;
				// Recursively handle the remaining suffixes to be inserted
				innerSplit(chars, currentIndex, activePoint.point);
			}
            // This can not be reminder, but the distance between reminder active point to the root.
			else if (reminder-getNodeString(activePoint.point).length() < activePoint.index.chars.length) {
				// If "reminder>0"，则说明该字符之前存在剩余字符，需要进行分割，然后插入新的后缀字符
				Node splitNode = activePoint.index;// 待分割的节点即为活动边(active_edge)
				// 创建切分后的节点，放到当前节点的子节点
				// 该节点继承了当前节点的子节点以及后缀节点信息
				//新建一个node当作index的儿子，index变成内部节点
				Node node = new Node(Arrays.copyOfRange(splitNode.chars, activePoint.length, splitNode.chars.length));// 从活动边长度开始截取剩余字符作为子节点
				node.child = splitNode.child;

				Node child = splitNode.child;
				while(null!=child){
					child.father=node;
					child=child.brother;
				}


				node.suffixNode = splitNode.suffixNode;
				splitNode.child = node;
				node.father = splitNode;
				splitNode.suffixNode = null;
				//node.position = (ArrayList<Integer>) splitNode.position.clone();
				if(splitNode.chars[splitNode.chars.length-1]=='$')
					node.label=splitNode.label;
				// 创建新插入的节点，放到当前节点的子节点(通过子节点的兄弟节点保存)
				Node newNode = new Node(Arrays.copyOfRange(chars, currentIndex, chars.length));// 插入新的后缀字符
				splitNode.child.brother = newNode;
				newNode.father=splitNode;
				splitNode.chars = Arrays.copyOfRange(splitNode.chars, 0, activePoint.length);// 修改当前节点的字符
				//newNode.position.add(currenctIndex-reminder);
				newNode.label = currentIndex-reminder;

				//Node fath = newNode.father;
				/***************
				 while(null!=fath && fath!=root){
				 fath.position.add(currenctIndex-reminder);
				 fath = fath.father;
				 }
				 ****************/

				// 分割完成之后需根据规则1和规则3进行区分对待
				// 按照规则1进行处理
				if (root == activePoint.point) {// 活动节点是根节点的情况
					// activePoint.point == root
					// 按照规则3进行处理
				} else if (null == activePoint.point.suffixNode) {// 无后缀节点，则活动节点变为root
					activePoint.point = root;
				} else {// 否则活动节点变为当前活动节点的后缀节点
					activePoint.point = activePoint.point.suffixNode;
				}
				// 活动边和活动边长度都重置
				activePoint.index = null;
				activePoint.length = 0;
				// 递归处理剩余的待插入后缀
				innerSplit(chars, currentIndex, splitNode);
			}
			else if(reminder-getNodeString(activePoint.point).length()==activePoint.index.chars.length){
				//直接在活动边的结点上插入一个新的儿子，不用插入内部节点了
				Node node = new Node(Arrays.copyOfRange(chars, currentIndex, chars.length));
				//node.position.add(currenctIndex-reminder);
				node.label = currentIndex-reminder;

				// 如果当前活动点无子节点，则将新建的节点作为其子节点即可，否则循环遍历子节点(通过兄弟节点进行保存)
				Node child = activePoint.index.child;
				if (null == child) {
					activePoint.index.child = node;
				} else {
					while (null != child.brother) {
						child = child.brother;
					}
					child.brother = node;
				}
				node.father = activePoint.index;
				/***********
				 Node fath = node.father;
				 while(null!=fath && fath!=root){
				 fath.position.add(currenctIndex-reminder);
				 fath = fath.father;
				 }
				 ************/

				Node ttmp=activePoint.index;
				if (root == activePoint.point) {// 活动节点是根节点的情况
					// activePoint.point == root
					// 按照规则3进行处理
				} else if (null == activePoint.point.suffixNode) {// 无后缀节点，则活动节点变为root
					activePoint.point = root;
				} else {// 否则活动节点变为当前活动节点的后缀节点
					activePoint.point = activePoint.point.suffixNode;
				}
				// 活动边和活动边长度都重置
				activePoint.index = null;
				activePoint.length = 0;
				// 递归处理剩余的待插入后缀
				innerSplit(chars, currentIndex, ttmp);
			}
			//***********
			if(index == chars.length&&reminder > 0){
				System.out.println("此处不应该出现！");
				index -= reminder;
				reminder = 0;
				activePoint.point = root;
				activePoint.index = null;
				activePoint.length = 0;
			}
			//***********
		}

	}

	/**
	 * 处理剩余的待插入后缀
	 * @param chars	构建后缀树的全部字符
	 * @param currenctIndex	当前已处理到的字符位置
	 * @param prefixNode 前继节点，即已经进行分割的节点，用于标识后缀节点
	 */
	private void innerSplit(char[] chars, int currenctIndex, Node prefixNode) {
		// 此处计算剩余待插入的后缀的开始位置，例如我们需要插入三个后缀(abx,bx,x)，已处理了abx，则还剩余bx和x，则下面计算的位置就是b的位置
		int start = currenctIndex - reminder + 1;

		//if(null!=root.child&&null!=root.child.suffixNode)
		//	  System.out.println("活动后缀是"+root.child.suffixNode);
		//	System.out.println("当前插入后缀：" + String.copyValueOf(chars, start, currenctIndex - start + 1) + "========");
		//	System.out.println("活动三元组"+activePoint.toString());
		//	System.out.println("reminder：" + String.valueOf(reminder));
		// dealStart表示本次插入我们需要进行查找的开始字符位置，因为由于规则2，可能出现通过后缀节点直接找到活动节点的情况
		// 如通过ab节点的后缀节点，直接找到节点b，那么此时的activePoint(node[b], null, 0)，我们需要从node[b]开始查找x，dealStart的位置就是x的位置

		//这里错了，不应该是activePoint.point.chars.length，而是活动点到根的所有字符个数！！！
		//int dealStart = start + activePoint.point.chars.length + activePoint.length;
		//************后面要修改，每个节点到根的字符个数应该存储*****
		int tmpp=0;
		Node fathh= activePoint.point;
		while(fathh!=root){
			tmpp+=fathh.chars.length;
			fathh=fathh.father;
		}

		int dealStart = start + tmpp + activePoint.length;
		//*********************************************************
		// 从dealStart开始查找所有后缀字符是否都存在(相对与活动点)


        //System.out.println("注意了~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		for (int index = dealStart; index <= currenctIndex; index++) {
			char w = chars[index];
			//		System.out.println(index);
			//		System.out.println(currenctIndex);
			//		System.out.print(w);

			if (find(w)) {// 存在，则查找下一个，activePoint.length+1，这里不增加reminder
				//		System.out.println("匹配上了");
				continue;
			}
			//	System.out.println("没匹配上：index："+index);
			Node splitNode = null;// 被分割的节点

			if(null==activePoint.index){// 如果activePoint.index==null，说明没有找到活动边，那么只需要在活动节点下插入一个节点即可
				splitNode=activePoint.point;//******我自己加的zouquan*******

				Node node = new Node(Arrays.copyOfRange(chars, index, chars.length));

				//node.position.add(start);
				node.label=start;
				Node child = activePoint.point.child;
				if(null==child){
					activePoint.point.child = node;
				}else{
					while (null != child.brother) {
						child = child.brother;
					}
					child.brother = node;
				}
				node.father = activePoint.point;
				//修改活动点！！
				//*******************************（我自己加的）
				// 分割完成之后需根据规则1和规则3进行区分对待
				// 按照规则1进行处理
				if (root == activePoint.point) {// 活动节点是根节点的情况
					// activePoint.point == root
					// 按照规则3进行处理
				} else if (null == activePoint.point.suffixNode) {// 无后缀节点，则活动节点变为root
					activePoint.point = root;
				} else {// 否则活动节点变为当前活动节点的后缀节点
					activePoint.point = activePoint.point.suffixNode;
				}
				// 活动边和活动边长度都重置
				activePoint.index = null;
				activePoint.length = 0;
				//*******************************
			}else{

				// 开始分割，分割部分同上面的分割
				splitNode = activePoint.index;//(活动边的儿子节点，不是父亲节点)
				// 创建切分后的节点，放到当前节点的子节点
				// 该节点继承了当前节点的子节点以及后缀节点信息
				Node node = new Node(Arrays.copyOfRange(splitNode.chars, activePoint.length, splitNode.chars.length));
				node.child = splitNode.child;

				Node child = splitNode.child;
				while(null!=child){
					child.father=node;
					child=child.brother;
				}

				node.suffixNode = splitNode.suffixNode;
				splitNode.child = node;
				node.father = splitNode;
				splitNode.suffixNode = null;
				//node.position = (ArrayList<Integer>) splitNode.position.clone();
				if(splitNode.chars[splitNode.chars.length-1]=='$')
					node.label=splitNode.label;
				// 创建新插入的节点，放到当前节点的子节点(通过子节点的兄弟节点保存)
				Node newNode = new Node(Arrays.copyOfRange(chars, index, chars.length));
				splitNode.child.brother = newNode;
				newNode.father = splitNode;
				// 修改当前节点的字符数
				splitNode.chars = Arrays.copyOfRange(splitNode.chars, 0, activePoint.length);
				// 规则2，连接后缀节点

				prefixNode.child.suffixNode = splitNode;//注意:前面原代码写的傻逼，明明应该新建一个splitNode,这样predixNode.suufixNode就应该是splitNode.可以作者傻逼，非要把原节点当成splitNode,新建一个节点node存放原来的节点(newNode是存放插入的新边的叶子)，这样prefixNode就变成了splitNode！

				//****计算splitNode到root的边上的字符串长度
				int k=0;
				Node tmp = splitNode;
				while(tmp!=root){
					k+=tmp.chars.length;
					tmp=tmp.father;
				}
				//***************************************
				//newNode.position.add(index-k);
				newNode.label=index-k;
			}

			reminder--;

			// 按照规则1进行处理
			if (root == activePoint.point) {// 活动节点是根节点的情况
				// activePoint.point == root

				// 按照规则3进行处理
			} else if (null == activePoint.point.suffixNode) {// 无后缀节点，则活动节点变为root
				activePoint.point = root;
			} else {
				activePoint.point = activePoint.point.suffixNode;
			}

			activePoint.index = null;
			activePoint.length = 0;
			if(reminder > 0){// 如果reminder==0则不需要继续递归插入后缀

				innerSplit(chars, currenctIndex, splitNode);
			}
		}

	}

	/**
	 * 寻找当前活动点的子节点中是否存在包含后缀字符的节点(边)
	 *
	 * @param w
	 * @return
	 */
	private boolean find(char w) {
		final Node start = activePoint.point;
		final Node current = activePoint.index;
		boolean exist = false;
//		System.out.println("find开始"+activePoint.toString());
		if (null == current) {// current==null 无活动边，则从活动点的子节点开始查找
			// 寻找子节点
			Node child = start.child;
			while (null != child) {
				if (child.chars[0] == w) {// 存在
					exist = true;
					if(child.chars.length>1){
						activePoint.index = child;
						activePoint.length++;// activePoint.length++
					}
					else if(child.chars.length==1){ //如果匹配边的字符串长度为1，活动点继续向下移动
						activePoint.point=child;
						activePoint.index = null;
						activePoint.length = 0;
					}
					break;
				} else {
					child = child.brother;
				}
			}
		}

		else if (current.chars.length>activePoint.length&&current.chars[activePoint.length] == w) {// 有活动边，则在活动边上查找
			activePoint.length++;
			exist = true;
			if (current.chars.length == activePoint.length) {
				// 如果活动边的长度已达到活动边的最后一个字符，则将活动点置为活动边，同时活动边置为null，长度置为0
				activePoint.point = current;
				activePoint.index = null;
				activePoint.length = 0;

			}

		}
		else {
			exist = false;
		}
//		System.out.println("find结束"+activePoint.toString());
		return exist;
	}

	/**
	 * 查找给定字符串是否是其子串
	 *
	 * @param word
	 * @return
	 */
	public boolean select(String word) {
		char[] chars = word.toCharArray();
		int index = 0;// 查找到的节点的匹配的位置
		// 查找从根节点开始，遍历子节点
		Node start = root;
		for (int i = 0; i < chars.length; i++) {
			if (start.chars.length < index + 1) {// 如果当前节点已匹配完，则从子节点开始，同时需重置index==0
				index = 0;
				start = start.child;
				while (null != start) {
					// 比较当前节点指定位置(index)的字符是否与待查找字符一致
					// 由于是遍历子节点，所以如果不匹配换个子节点继续
					if (start.chars[index] == chars[i]) {
						index++;
						break;
					} else {
						start = start.brother;
					}
				}
				if (null == start) {// 子节点遍历完都无匹配则返回false
					return false;
				}
			} else if (start.chars[index] == chars[i]) {
				// 如果当前查找到的节点的还有可比较字符，则进行比较，如果不同则直接返回false
				index++;
			} else {
				return false;
			}
		}
		return true;
	}
	/**
	 * 返回node节点的所有后代叶节点的label(position)
	 * @param node
	 * @return
	 */

	public static ArrayList <Integer> getNodeAllLeafSonLabel(Node node){
		ArrayList <Integer> result = new ArrayList<>();

		if(node.chars[node.chars.length-1]=='$'){
			result.add(node.label);
		}
		else{
			Node child = node.child;
			while(null!=child){
				result.addAll(getNodeAllLeafSonLabel(child));
				child=child.brother;
			}
		}
		return result;
	}

	/**
	 * 查找给定字符串的最长前缀，并返回其alignment效果最好的起始位置和长度，返回(pos,len)对
	 * wordstartpos是输入word在原始序列中开始的位置
     *
	 * @param word
	 * @return
	 */
	public int[] selectPrefixForAlignment(String word, int wordstartpos) {
		int[] back = new int[2];
		back[0]=-1;
		back[1]=0;
		word=word.substring(wordstartpos);

		char[] chars = word.toCharArray();
		int index = 0;// 查找到的节点的匹配的位置
		// 查找从根节点开始，遍历子节点
		Node start = root;

		for (int i = 0; i < chars.length; i++) {
			if (start.chars.length < index + 1) {// 如果当前节点已匹配完，则从子节点开始，同时需重置index==0
				index = 0;
				start = start.child;

				while (null != start) {
					// 比较当前节点指定位置(index)的字符是否与待查找字符一致
					// 由于是遍历子节点，所以如果不匹配换个子节点继续
					if (start.chars.length>index&&start.chars[index] == chars[i]) {
						index++;
						break;
					} else if(null != start.brother){
						start = start.brother;
					}
					else if (null == start.brother&&i>=minMatchLen){ //断在一个结点，每一个儿子都没能继续匹配上

						Integer[] startpos= (Integer[]) getNodeAllLeafSonLabel(start.father).toArray(new Integer[getNodeAllLeafSonLabel(start.father).size()]);
						int mindis=Integer.MAX_VALUE;
						int pos_j=-1;


						for(int j=0;j<startpos.length;j++){
							int tmp=Math.abs(startpos[j].intValue()-wordstartpos)-i;
							if(tmp<mindis){
								mindis=tmp;
								pos_j=startpos[j].intValue();
							}
						}

						if(mindis<-minMatchLen){
							back[0]=pos_j;
							back[1]=i;
						}
						else{
							back[0]=-1;
							back[1]=0;
						}

						return back;
					}else if(i<minMatchLen){
						back[0]=-1;
						back[1]=0;

						return back;

					}
				}

			} else if (start.chars[index] == chars[i]) {
				// 如果当前查找到的节点的还有可比较字符，则进行比较，如果不同则直接返回false
				index++;
			} else if (i>=minMatchLen){
				//断在一条边的中间，或叶节点，因为叶节点是$,肯定不同，所以在这结束

				Integer[] startpos= (Integer[]) getNodeAllLeafSonLabel(start).toArray(new Integer[getNodeAllLeafSonLabel(start).size()]);
				int mindis=Integer.MAX_VALUE;
				int pos_j=-1;

				for(int j=0;j<startpos.length;j++){
					int tmp=Math.abs(startpos[j].intValue()-wordstartpos)-i;
					if(tmp<mindis){
						mindis=tmp;
						pos_j=startpos[j].intValue();
					}
				}

				if(mindis<-minMatchLen){
					back[0]=pos_j;
					back[1]=i;
				}
				else{
					back[0]=-1;
					back[1]=0;
				}

				return back;
			} else if(i<minMatchLen){
				back[0]=-1;
				back[1]=0;

				return back;
			}
		}
		//i到头了


		Integer[] startpos = (Integer[]) getNodeAllLeafSonLabel(start).toArray(new Integer[getNodeAllLeafSonLabel(start).size()]);
		int mindis=Integer.MAX_VALUE;
		int pos_j=-1;
		for(int j=0;j<startpos.length;j++){
			int tmp=Math.abs(startpos[j].intValue()-wordstartpos);
			if(tmp<mindis){
				mindis=tmp;
				pos_j=startpos[j].intValue();
			}
		}

		back[0]=pos_j;
		back[1]=chars.length;
		return back;
	}

	/**
	 * 格式化打印出整个后缀树
	 */
	public void print() {
		Node child = root.child;
		System.out.println("[root] [activePoint:(" + activePoint.point + "," + activePoint.index + ","
				+ activePoint.length + ")], [reminder:" + reminder + "]");
		while (child != null) {
			System.out.print("|——");
			child.print("    ");
			child = child.brother;
		}
	}

	/**
	 * <p>
	 * 后缀树的节点，即边
	 * 每个节点的chars是其父亲到该点的边的字符串，root节点没父亲，所以chars=“”
	 * </p>
	 */
	private class Node {
		public char[] chars;
		public Node child;
		public Node brother;
		public Node father;
		public Node suffixNode;
		//public ArrayList <Integer> position;  //用来记录内部节点所有后代的叶节点代表的后缀的起始位置，如果是叶节点就只有一个整数值，就是它代表的后缀的起始位置
		public int label;//记录叶节点代表的后缀的起始位置，内部结点可能也有值，要通过chars的最后是否是$来判断
		public Node(char[] chars) {
			this.chars = chars;
			//position = new ArrayList();
		}

		@Override
		public String toString() {
			//return "Node [chars=" + String.valueOf(chars) + "]"+"position:"+String.valueOf(position);
			return "Node [chars=" + String.valueOf(chars) + "]";
		}

		public void print(String prefix) {
			System.out.print(String.valueOf(chars));
			if(chars[chars.length-1]=='$'){
				System.out.print(label);
			}
			if (null != this.suffixNode) {
				System.out.println("--" + String.valueOf(this.suffixNode.chars));
			} else {
				System.out.println();
			}
			Node child = this.child;
			while (null != child) {
				System.out.print(prefix + "|——");
				child.print(prefix + prefix);
				child = child.brother;
			}
		}
	}

	/**
	 * <p>
	 * 活动点(active point)，一个三元组：(active_node,active_edge,active_length)
	 * 活动边的父亲是point,儿子是index；活动点应该是point,提取存储的字符串要从儿子index的chars中找(0,length)
	 * </p>
	 */
	private class ActivePoint {
		public Node point;
		public Node index;
		public int length;

		public ActivePoint(Node point, Node index, int length) {
			this.point = point;
			this.index = index;
			this.length = length;
		}

		@Override
		public String toString() {
			return "ActivePoint [point=" + point + ", index=" + index + ", length=" + length + "]";
		}
	}
	/**
	 * 对String格式化,删除非法字符(只保留agctn,其余字符全部替换成n),全部转换成小写,u全部换成t
	 * @param s
	 * @return
	 */
	private static String format(String s){
		s=s.toLowerCase();
		s=s.replace('u', 't');
		StringBuffer sb = new StringBuffer(s);

		for(int i=0;i<sb.length();i++){
			switch(sb.charAt(i)){
				case 'a': break;
				case 'c': break;
				case 'g': break;
				case 't': break;
				case 'n':break;
				default: sb=sb.replace(i, i+1, "n");
			}
		}


		return(sb.toString());
	}

	/**
	 * 输入一个节点，输出从跟到该结点的边上的字符串
	 * @param start
	 * @return
	 */

	public static String getNodeString(Node start){
		String s="";

		while(start.chars.length>0){
			s=String.valueOf(start.chars)+s;
			start=start.father;
		}
		s=String.valueOf(start.chars)+s;
		return s;
	}


	public static void main(String[] args) {

		SuffixTree suffixTree = new SuffixTree();
		//String s1 ="ACACCGATGAGTCTGTCACGCGATAGCATGACGCTGCACCCTATGCTCGATAGCATTGCGAC";
		//String s1 ="ACACCGATGAGTCTGTCACGCGATAGCATGAC";
		//String s1 ="ACCACAACACCACAACACCACCACAACACCACCAACCACCT";
		String s1 ="ACTDGDG";
		//String s1="GGGAGCCATGCATT";
		s1 = format(s1)+"$";
		suffixTree.build(s1);
		//suffixTree.print();

		//System.out.println(String.valueOf(centerstar.root.child));

		//System.out.println(String.valueOf(centerstar.root.child.chars));

		/*Node start=centerstar.root.child.brother.child.child.child;
		Integer[] startpos= (Integer[]) getNodeAllLeafSonLabel(start).toArray(new Integer[getNodeAllLeafSonLabel(start).size()]);
		System.out.println(Arrays.toString(startpos));
		System.out.println(getNodeString(start));*/

		//System.out.println(centerstar.select("CACAAC"));
		String word = "AGTDGDG";
		word = word.toLowerCase();
		System.out.println(Arrays.toString(suffixTree.selectPrefixForAlignment(word, 0)));

        AlignSubstring alignSubstring = new AlignSubstring(suffixTree, word);
        System.out.println(Arrays.toString(suffixTree.selectPrefixForAlignment(word, 0)));
        int[][] findCommonSubstrings = alignSubstring.findCommonSubstrings();

		System.out.println("OK!");
	}
}
