package tree;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class NeighbourJoiningSummary_Node{
	private NeighbourJoiningSummary_Node ParentNode;
	private NeighbourJoiningSummary_Node LNode;
	private NeighbourJoiningSummary_Node RNode;
	private String Tree;
	private int index; 
	
	public NeighbourJoiningSummary_Node(int index, NeighbourJoiningSummary_Node ParentNode, NeighbourJoiningSummary_Node LNode, NeighbourJoiningSummary_Node RNode , String Tree){
	 this.ParentNode = ParentNode;
	 this.LNode = LNode;
	 this.RNode = RNode;
    this.index = index;
    this.Tree = Tree;
	}
	public void Reset(int index, NeighbourJoiningSummary_Node ParentNode, NeighbourJoiningSummary_Node LNode, NeighbourJoiningSummary_Node RNode , String Tree){
		 this.ParentNode = ParentNode;
		 this.LNode = LNode;
		 this.RNode = RNode;
	     this.index = index;
	     this.Tree = Tree; 
	}
//reset function	
	public void Reset_ParentNode(NeighbourJoiningSummary_Node ParentNode){
		this.ParentNode = ParentNode;
	}
	public void Reset_LNode(NeighbourJoiningSummary_Node LNode){
		this.LNode = LNode;
	}
	public void Reset_RNode(NeighbourJoiningSummary_Node RNode){
		this.RNode = RNode;
	}
	public void Reset_index(int index){
		this.index = index;
	}
	public void Reset_Tree(String Tree){
		this.Tree = Tree;
	} 
// get function 	 
	public NeighbourJoiningSummary_Node Get_ParentNode(){
		return this.ParentNode;
	}
	public NeighbourJoiningSummary_Node Get_LNode(){
		return this.LNode;
	}
	public NeighbourJoiningSummary_Node Get_RNode(){
		return this.RNode;
	}
	public int Get_index(){
		return this.index;
	}
	public String Get_Tree(){
		return this.Tree;
	}
	 
}

public class NeighbourJoining_Summary {
	
   String input_Directory;
   String HPTree_OutPut;
   ArrayList<NeighbourJoiningSummary_Node> LeaveNode_list = new ArrayList<NeighbourJoiningSummary_Node>();
   //列表用于保存所有的子树
   ArrayList<String> seq = new ArrayList<String>();
 //列表用于保存所有的代表节点 
   
   public NeighbourJoining_Summary(String input_Directory , String HPTree_OutPut){
	   this.input_Directory = input_Directory;
	   this.HPTree_OutPut = HPTree_OutPut;
   }

   public void Merge() throws IOException{ //合并文件夹下的所有reduce输出结果
	     File NeighbourJoining_subTree_Directory = new File(input_Directory);//获取本地文件目录
		 if (!NeighbourJoining_subTree_Directory.isDirectory()){
			 System.out.println(NeighbourJoining_subTree_Directory + " is not a directory ");
			 System.exit(0);
		 }
		 else{//操作文件夹下的每个文件
			 String[] filelist = NeighbourJoining_subTree_Directory.list();
			
			 for(int i=0;i<filelist.length;i++){
				 File readfile = new File(input_Directory + "/" + filelist[i]);
				 if(readfile.isDirectory() || filelist[i].charAt(0)=='_' || filelist[i].charAt(0)=='.'){
					 continue;
				 }
				// System.out.println("The file name is :"+readfile.toString());
				 BufferedReader br = new BufferedReader(new FileReader(readfile.getPath()));
				 int leave_index = 0;
				 while(br.ready()){
					 String[] line = br.readLine().split("\t");
					 if (line.length < 2) continue;
					 LeaveNode_list.add(new NeighbourJoiningSummary_Node(leave_index , null , null , null , line[1]));
					 seq.add(line[0]);
				 }
				 br.close();
			 }//end for			
		 }//end else		 
   }
   
   public void Summary() throws IOException{//子树汇总
	   
	   int setsize = seq.size();
	   double [][]distance=new double[setsize][setsize]; //设置子集合的距离矩阵
	   double[] A=new double [setsize];//距离矩阵每行的和
	   CountTheDistance(seq , seq.size() , distance , A);//计算距离矩阵以及A 
	   MinDistance min_distance = new MinDistance(10000.0 , 0 ,0);
	   AmendTheDistance(distance , A , setsize, min_distance);//对距离矩阵进行修正
	   Set<Integer> Already_set = new HashSet();
	   
	   int iter=0;
	 	while(iter!=setsize-1){//依次合并两个节点 
	 		  iter++;
		 double Li=0.0 , Lj=0.0;
		 for(int z=0;z<setsize;z++){
			 Li = Li+distance[min_distance.geti()][z]-distance[min_distance.getj()][z]; 		
		 }
		 Lj = -Li;
		 Li = (min_distance.getDistance() + Li)/2;//分枝长度计算的时候是全总再加min_distance
		 Lj = (min_distance.getDistance() + Lj)/2; //获取了i , j 到父节点的分枝长度
		 
		 for(int k=0;k<setsize;k++){ //更新矩阵
			if(k!=min_distance.geti() && k!=min_distance.getj()){//当不是扫描到对正轴的时候
				if(distance[k][min_distance.geti()]==0){
					continue;
				}
	 			distance[min_distance.geti()][k]=(distance[min_distance.geti()][k]+distance[min_distance.getj()][k]
	 					-min_distance.getDistance())/2; 
	 			distance[min_distance.getj()][k]=0;//然后把下标大的横和列全部都变成0
	 			distance[k][min_distance.getj()]=0;
	 			distance[k][min_distance.geti()]=distance[min_distance.geti()][k];
	 			}
	 			else{   //要么k=i ,或者k=j
	 			 distance[min_distance.geti()][k]=0;
	 			 distance[min_distance.getj()][k]=0;
	 			 distance[k][min_distance.getj()]=0;
	 			 distance[k][min_distance.geti()]=0;
	 			}
		 }
		 
		//更新完矩阵后，合并这两个节点， 1、两个均未出现过 2、一个出现过一个未出现过
	     if(!Already_set.contains(min_distance.geti()) && !Already_set.contains(min_distance.getj())){
	    	 NeighbourJoiningSummary_Node InternalNode = new NeighbourJoiningSummary_Node(-1,//构建一个内部节点
	    			null,
	    			LeaveNode_list.get(min_distance.geti()),
	    			LeaveNode_list.get(min_distance.getj()),
	    			"("+LeaveNode_list.get(min_distance.geti()).Get_Tree()+":"+Li+","+LeaveNode_list.get(min_distance.getj()).Get_Tree()+":"+Lj+")"
	    			);
	    	LeaveNode_list.get(min_distance.geti()).Reset_ParentNode(InternalNode);//叶子节修改父节点
	    	LeaveNode_list.get(min_distance.getj()).Reset_ParentNode(InternalNode); 
	    	InternalNode.Get_LNode().Reset_Tree(null);// 设置左右子树节点为空，减小空间消耗
			InternalNode.Get_RNode().Reset_Tree(null);
			Already_set.add(min_distance.geti());// 记录已处理节点
			Already_set.add(min_distance.getj());
	     }
	     else if(Already_set.contains(min_distance.geti()) && !Already_set.contains(min_distance.getj())){ // i 节点已经被处理了
	  	  	NeighbourJoiningSummary_Node CurrentNode = LeaveNode_list.get(min_distance.geti());
   			while(CurrentNode.Get_ParentNode()!=null){
   				CurrentNode = CurrentNode.Get_ParentNode(); //回溯到i节点的根节点
   			}
   			NeighbourJoiningSummary_Node InternalNode = new NeighbourJoiningSummary_Node(-1,//构建一个内部节点
   	 	    			null,
   	 	    			CurrentNode,
   	 	    			LeaveNode_list.get(min_distance.getj()),
   	 	    			"("+CurrentNode.Get_Tree()+":"+Li+","+LeaveNode_list.get(min_distance.getj()).Get_Tree()+":"+Lj+")"
   	 	    			);
   			 CurrentNode.Reset_ParentNode(InternalNode);
   			 LeaveNode_list.get(min_distance.getj()).Reset_ParentNode(InternalNode); 
   			 InternalNode.Get_LNode().Reset_Tree(null);// 设置左右子树节点为空，减小空间消耗
   			 InternalNode.Get_RNode().Reset_Tree(null);
   			 Already_set.add(min_distance.getj());// 记录已处理节点
	    	 }//end if i contain
	     else if(Already_set.contains(min_distance.getj())&& !Already_set.contains(min_distance.geti())){ // j 节点已经被处理了
	     	     NeighbourJoiningSummary_Node CurrentNode = LeaveNode_list.get(min_distance.getj());
   			     while(CurrentNode.Get_ParentNode()!=null){
   				           CurrentNode = CurrentNode.Get_ParentNode(); //回溯到i节点的根节点
   			       } 
   			     NeighbourJoiningSummary_Node InternalNode = new NeighbourJoiningSummary_Node(-1,//构建一个内部节点
   	 	    			null,
   	 	    			CurrentNode,
   	 	    			LeaveNode_list.get(min_distance.geti()),
   	 	    			"("+LeaveNode_list.get(min_distance.geti()).Get_Tree()+":"+Li+","+CurrentNode.Get_Tree()+":"+Lj+")"
   	 	    			);
   			     CurrentNode.Reset_ParentNode(InternalNode);
   			     LeaveNode_list.get(min_distance.geti()).Reset_ParentNode(InternalNode); 
   			     InternalNode.Get_LNode().Reset_Tree(null);// 设置左右子树节点为空，减小空间消耗
   			     InternalNode.Get_RNode().Reset_Tree(null);
   			     Already_set.add(min_distance.geti());// 记录已处理节点
	    	     }//end if j contain
	     else if(Already_set.contains(min_distance.geti()) && Already_set.contains(min_distance.getj())){
	    	   NeighbourJoiningSummary_Node CurrentNodei = LeaveNode_list.get(min_distance.geti());
			   while(CurrentNodei.Get_ParentNode()!=null){
				  CurrentNodei = CurrentNodei.Get_ParentNode(); //回溯到i节点的根节点
			   }
			   NeighbourJoiningSummary_Node CurrentNodej = LeaveNode_list.get(min_distance.getj());
			   while(CurrentNodej.Get_ParentNode()!=null){
			    	CurrentNodej = CurrentNodej.Get_ParentNode(); //回溯到i节点的根节点
		       }
			   NeighbourJoiningSummary_Node InternalNode = new NeighbourJoiningSummary_Node(-1,//构建一个内部节点
	 	    			null,
	 	    			CurrentNodei,
	 	    			CurrentNodej,
	 	    			"("+CurrentNodei.Get_Tree()+":"+Li+","+CurrentNodej.Get_Tree()+":"+Lj+")"
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
		for(int i=0;i<setsize;i++){ // 重新修改净分枝度
			A[i]=0;
			for(int j=0;j<setsize;j++){
				A[i]=A[i]+distance[i][j]; 
			}
		}  
	    for(int i=0;i<setsize;i++){
	    	for(int j=i+1;j<setsize;j++){
	    		if(distance[i][j]==0 ){
	    			continue;
	    	     }
	    		distance[i][j]=distance[i][j]-(A[i]+A[j])/(setsize-2);//修正的时候根据公式要用到A 和distance
	    		distance[j][i]=distance[i][j];  
	    		if(distance[i][j]<min_distance.getDistance() && distance[i][j]!=0){//获取矩阵中最小的距离
	    			min_distance.setDistance(distance[i][j]);
	    			min_distance.seti(i);
	    			min_distance.setj(j);
	    		}// end if
	    	}//end for
	    } //end for 
	}//end while(Already_set.size()!=setsize)
	 
	 /*
	  *  wirte the final phylogenetic tree
	  */
	 	NeighbourJoiningSummary_Node CurrentNode = LeaveNode_list.get(0);
	 	while(CurrentNode.Get_ParentNode()!=null){
	 		CurrentNode = CurrentNode.Get_ParentNode(); //回溯到i节点的根节点
	 	}
	 	BufferedWriter bw = new BufferedWriter(new FileWriter(HPTree_OutPut));
	 	bw.write( CurrentNode.Get_Tree());
	 	bw.newLine();
	 	bw.flush(); 
   }//end Summary
   

public static void AmendTheDistance(double[][]distance, double[]A, int lineNumber, MinDistance min_distance){//在进化树构建的时候用来修正距离
    int i,j;
    for(i=0;i<lineNumber;i++){
    	for(j=i+1;j<lineNumber;j++){
    		distance[i][j]=distance[i][j]-(A[i]+A[j])/(lineNumber-2);//修正的时候根据公式要用到A 和distance
    		distance[j][i]=distance[i][j];  
    		if(distance[i][j]<min_distance.getDistance() && distance[i][j]!=0){//获取矩阵中最小的距离
    			min_distance.setDistance(distance[i][j]);
    			min_distance.seti(i);
    			min_distance.setj(j);
    		}
    	}
    } 
} 

public static void CountTheDistance(ArrayList<String>sequences,int lineNumber,double[][] distance,double[] A) throws IOException{//这个函数用来计算距离矩阵
		int i,j;
		double p;
		double temp;
		double distance_temp = 0;//这个在计算方差和均值的时候有用 
		
		for(i=0;i<lineNumber;i++){
			for(j=0;j<lineNumber;j++){
				
				if(i<j){
				p=JukeCantor((String)sequences.get(i),(String)sequences.get(j));
				p=1.0-0.75*p;					
				temp=Math.log(p);					
				distance_temp=(double)(-0.75*temp);   //根据公式计算距离
				distance[i][j]=distance_temp; 
				}
				else if(i==j){
					distance[i][j]=0;//对角线上为0
				}
				else{
					distance[i][j]=distance[j][i];
				} 
			//	System.out.print(distance_temp+"\t"); //这里打印出距离矩阵可以在屏幕上显示
				A[i]+=distance[i][j];//计算A数组
			} 				
		//	System.out.println();
		} 		
	}

public static double JukeCantor(String gene1,String gene2){ 
	int rate=0;
	int i;
	double result=0.0;
    int length=gene1.length();
	if(gene1.length()>gene2.length()){
        length=gene2.length();
	}
	for(i=0;i<length;i++){
		//System.out.print(gene1.charAt(i)+"  ");
		if(gene1.charAt(i)!=gene2.charAt(i)){
			rate++;
		}		
	}
	result=(double)rate/(double)gene1.length(); 
	//System.out.print(result+"   ");
	return result;
}
}
