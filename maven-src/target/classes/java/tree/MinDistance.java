package tree;

public class MinDistance{//这个类用来保存在迭代查找的最小值信息
	 private double mindistance;
	 private int i;
	 private int j;
	public  MinDistance(double mindistance,int i,int j){
		 this.mindistance=mindistance; //保存最小值
		 this.i=i;//最小值的下标
		 this.j=j;
	 }
	 public void setDistance(double mindistance){
		 this.mindistance=mindistance;
	 }
	 public void seti(int i){
		 this.i=i;
	 }
	 public void setj(int j){
		 this.j=j;
	 }
	 public double getDistance(){
		 return mindistance;
	 }
	 public int geti(){
		 return i;
	 }
	 public int getj(){
		 return j;
	 }
}