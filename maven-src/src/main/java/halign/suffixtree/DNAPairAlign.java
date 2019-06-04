package halign.suffixtree;

public class DNAPairAlign {
    String center;
    String si;
    int[][] name;
    /*spaceevery,spaceother拆开*/
    int[] Spaceevery;
    int[] Spaceother;
    //除中心序列以外的最大长度
    int Spaceother_len;
    // ###########定义匹配，不匹配和空格的罚分###############
    int spacescore = -1, matchscore = 0, mismatchscore = -1;
    /*===========================================*/
    public String get_center(){
        return center;
    }
    public String get_si(){
        return si;
    }
    public int[][] get_name(){
        return name;
    }
    public int[] get_spaceevery(){
        return Spaceevery;
    }
    public int[] get_spaceother(){
        return Spaceother;
    }
    /*===========================================*/
    public DNAPairAlign(String center, String si, int[][] name, int Spaceother_len){
        this.center = center;
        this.si = si;
        this.name = name;
        this.Spaceother_len = Spaceother_len;
        this.Spaceevery = new int[center.length() + 1];// 存放中心序列分别与其他序列比对时增加的空格的位置
        this.Spaceother = new int[Spaceother_len + 1];// 存放其他序列与中心序列比对时增加的空格的位置
    }

    public void pwa() {
        prealign();
        for (int j = 1; j < name[0].length; j++) {
            midalign(j);
        }
        postalign();
    }
    public void prealign() {
        String strC = center.substring(0, name[0][0]);
        String stri = si.substring(0, name[1][0]);
        int M[][] = computeScoreMatrixForDynamicProgram(stri, strC);// 动态规划矩阵计算完毕
        traceBackForDynamicProgram(M, stri.length(), strC.length(), 0, 0);// 回溯，更改空格数组
    }
    public void midalign(int j) {
        int lamda = Math.max(name[1][j - 1] + name[2][j - 1] - name[1][j],
                name[0][j - 1] + name[2][j - 1] - name[0][j]);
        if (lamda > 0) {
            name[0][j] += lamda;
            name[1][j] += lamda;
            name[2][j] -= lamda;
        }
        if (name[2][j] < 0 ){
            //System.out.println("此处有错误！！！！");
            name[1][j]=si.length();
        }
        if (name[1][j]>si.length()){
            name[1][j]=si.length();
        }
        String strC = center.substring(name[0][j - 1] + name[2][j - 1],name[0][j]);// 此处有漏洞，如果Name[centerstar][i][0][0]=0，会抱错
        String stri = si.substring(name[1][j - 1] + name[2][j - 1], name[1][j]);
        int M[][] = computeScoreMatrixForDynamicProgram(stri, strC);// 动态规划矩阵计算完毕
        traceBackForDynamicProgram(M, stri.length(), strC.length(),
                name[1][j - 1] + name[2][j - 1],
                name[0][j - 1] + name[2][j - 1]);
    }

    public void postalign() {
        int j = name[0].length;
        if (j > 0) {
            int cstart = name[0][j - 1] + name[2][j - 1];
            if (cstart > center.length())
                cstart--;// 别忘了，建后缀树时加了个$
            int istart = name[1][j - 1] + name[2][j - 1];
            if (istart > si.length())
                istart--;
            String strC = center.substring(cstart);
            String stri = si.substring(name[1][j - 1] + name[2][j - 1]);
            int M[][] = computeScoreMatrixForDynamicProgram(stri, strC);// 动态规划矩阵计算完毕
            traceBackForDynamicProgram(M, stri.length(), strC.length(), istart,cstart);
        } else {
            String strC = center;
            String stri = si;
            int M[][] = computeScoreMatrixForDynamicProgram(stri, strC);// 动态规划矩阵计算完毕
            traceBackForDynamicProgram(M, stri.length(), strC.length(), 0, 0);
        }
    }
    /*功能：计算两序列比对过程中的得分矩阵
     * 输入：两个字符串
     * 输出：得分矩阵
     * */
    public int[][] computeScoreMatrixForDynamicProgram(String stri, String strC) {
        int len1 = stri.length() + 1;
        int len2 = strC.length() + 1;
        int M[][] = new int[len1][len2]; // 定义动态规划矩阵
        // ---初始化动态规划矩阵-----------
        int p, q;
        for (p = 0; p < len1; p++)
            M[p][0] = spacescore * p;
        for (q = 0; q < len2; q++)
            M[0][q] = spacescore * q;
        // ---初始化结束----------
        // ----计算矩阵的值------------
        for (p = 1; p < len1; p++)
            for (q = 1; q < len2; q++) {// M[p][q]=max(M[p-1][q]-1,M[p][q-1]-1,M[p-1][q-1]+h)
                int h;
                if (stri.charAt(p - 1) == strC.charAt(q - 1))
                    h = matchscore;
                else
                    h = mismatchscore;
                M[p][q] = Math.max(M[p - 1][q - 1] + h, Math.max(M[p - 1][q] + spacescore, M[p][q - 1] + spacescore));
            }
        return (M);
    }

    /*函数功能：回溯得分矩阵并填写空格记录表
     * 输入：得分矩阵，两序列长度，两序列比对的开始位置
     * 输出：无输出，但填写了全局变量spaceevery,spaceother。
     * */
    public void traceBackForDynamicProgram(int[][] M, int p, int q, int k1, int k2) {
        while (p > 0 && q > 0) {
            if (M[p][q] == M[p][q - 1] + spacescore) {
                Spaceother[p + k1]++;
                //System.out.println(Spaceother[p+k1]);
                q--;
            } else if (M[p][q] == M[p - 1][q] + spacescore) {
                Spaceevery[q + k2]++;
                p--;
            } else {
                p--;
                q--;
            }
        }
        if (p == 0){
            while (q > 0) {
                Spaceother[k1]++;
                q--;
            }
        }
        if (q == 0){
            while (p > 0) {
                Spaceevery[k2]++;
                p--;
            }
        }
    }
}
