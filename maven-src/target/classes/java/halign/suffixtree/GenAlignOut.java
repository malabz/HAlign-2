package halign.suffixtree;

public class GenAlignOut {
	public String get_every_sequeces(String center_re,String pi,int n,String center,int center_len,int[] Space,int[] Spaceevery,int[]Spaceother){
		int j;
		String tmp="";
		for (j = 0; j < pi.length(); j++) {
			String kong = "";
			for (int k = 0; k < Spaceother[j]; k++)
				kong = kong.concat("-");
			tmp=tmp.concat(kong).concat(pi.substring(j,j+1));
		}
		String kong = "";
		int l;
		if(pi.length()>(Spaceother.length-1)){
			l=Spaceother.length-1;
		}
		else{
			l=pi.length();
		}
		for (j = 0; j < Spaceother[l]; j++)
			kong = kong.concat("-");
		tmp=tmp.concat(kong);
		int Cha[] = new int[center.length() + 1];
		int position = 0; // 用来记录插入差异空格的位置
		for (j = 0; j < center.length() + 1; j++) {
			Cha[j] = 0;
			if (Space[j] - Spaceevery[j] > 0)
				Cha[j] = Space[j] - Spaceevery[j];
			position = position + Spaceevery[j];
			if (Cha[j] > 0) { // 在位置position处插入Cha[j]个空格
				kong = "";
				for (int k = 0; k < Cha[j]; k++)
					kong = kong.concat("-");
				tmp=tmp.substring(0,position).concat(kong).concat(tmp.substring(position));
			}
			position = position + Cha[j] + 1;
		}
		return tmp;
	}
}
