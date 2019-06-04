package halign.kband;

public class KbandAlignTwo {
    private static int M=1, misM=-1, g=10, h=2, rate=2;
    private static int maxk;

    public void align(String s, String t){
        int mn = Math.abs(s.length()-t.length());
        int k = rate*mn+1;
        int pok, m=s.length(), n=t.length();//M为匹配积分，pok用于控制终止
        do{
            AffineGapPenalty aff = new AffineGapPenalty(m, n, g, h, k, mn, s, t, M, misM);
            maxk = aff.Init(); //必须先行调用此方法初始化数组

            //a[m,n]>=M*(n-k-1)-2*(k+1)*(h+g)控制终止
            //最坏情况估计至少k+1对空格，且不相邻

            pok = M*(n-k-1)-2*(k+1)*(h+g);
            if(maxk < pok) {
                k=k*2;
            }
            else break;
        }while(k<=pok);

        int ch = 1;//1.2.3对应a.b.c
        if(maxk == AffineGapPenalty.a[m][n+k-m]){ch=1;}
        else if(maxk == AffineGapPenalty.b[m][n+k-m]){ch=2;}
        else {ch=3;}//(k==AffineGapPenalty.c[m][n+k-m])

        KbandMake make = new KbandMake(g,h,m,n,k,mn,s,t,true);
        make.run(ch);
    }


}
