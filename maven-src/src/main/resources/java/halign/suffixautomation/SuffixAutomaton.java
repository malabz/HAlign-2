package halign.suffixautomation;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Suffix Automaton
 * @see "http://e-maxx.ru/algo/suffix_automata"
 */
public class SuffixAutomaton {
    public int size;
    public int[] len; // 最短路径长度
    public int[] link; // 故障链接
    public int[][] next; // 下链接
    public int[] original; // 复制原始情形

    public SuffixAutomaton(int sz) {
        size = sz;
        len = new int[sz];
        link = new int[sz];
        next = new int[sz][];
        original = new int[sz];
    }

    public static int enc(char c) { return c - 'a'; }
    public static char dec(int n) { return (char)('a'+n); }

    /**
     * 创建后缀自动机，拓扑排序并返回。
     * 由于每次添加一个字符时只创建一个克隆，所以总共只有2个| a | -1个或更少的节点。
     * @param a
     * @return
     */
    public SuffixAutomaton buildSuffixAutomaton(char[] a) {
        int n = a.length;
        int[] len = new int[2*n];
        int[] link = new int[2*n];
        int[][] next = new int[2*n][26];
        int[] original = new int[2*n];
        Arrays.fill(link, -1);
        for(int i = 0;i < 2*n;i++){
            Arrays.fill(next[i], -1);
        }
        Arrays.fill(original, -1);

        len[0] = 0;
        link[0] = -1;
        int last = 0;
        int sz = 1;

        // extend
        for(char c : a){
            int v = enc(c);
            int cur = sz++;
            len[cur] = len[last] + 1;
            int p;
            for(p = last; p != -1 && next[p][v] == -1; p = link[p]){
                next[p][v] = cur;
            }
            if(p == -1){
                link[cur] = 0;
            }else{
                int q = next[p][v];
                if(len[p] + 1 == len[q]){
                    link[cur] = q;
                }else{
                    int clone = sz++;
                    original[clone] = original[q] != -1 ? original[q] : q;
                    len[clone] = len[p]+1;
                    System.arraycopy(next[q], 0, next[clone], 0, next[q].length);
                    link[clone] = link[q];
                    for(;p != -1 && next[p][v] == q; p = link[p]){
                        next[p][v] = clone;
                    }
                    link[q] = link[cur] = clone;
                }
            }
            last = cur;
        }

        // topological sort
        int[] nct = new int[sz];
        for(int i = 0;i < sz;i++){
            for(int e : next[i]){
                if(e != -1)nct[e]++;
            }
        }
        int[] ord = new int[sz];
        int p = 1;
        ord[0] = 0;
        for(int r = 0;r < p;r++){
            for(int e : next[ord[r]]){
                if(e != -1 && --nct[e] == 0)ord[p++] = e;
            }
        }
        int[] iord = new int[sz];
        for(int i = 0;i < sz;i++)iord[ord[i]] = i;

        SuffixAutomaton sa = new SuffixAutomaton(sz);
        for(int i = 0;i < sz;i++){
            sa.len[i] = len[ord[i]];
            sa.link[i] = link[ord[i]] != -1 ? iord[link[ord[i]]] : -1;
            sa.next[i] = next[ord[i]];
            for(int j = 0;j < sa.next[i].length;j++)sa.next[i][j] = sa.next[i][j] != -1 ? iord[sa.next[i][j]] : -1;
            sa.original[i] = original[ord[i]] != -1 ? iord[original[ord[i]]] : -1;
        }

        return sa;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i < size;i++){
            sb.append("{");
            sb.append(i).append("|");
            sb.append("len:").append(len[i]).append(", ");
            sb.append("link:").append(link[i]).append(", ");
            sb.append("original:").append(original[i]).append(", ");
            sb.append("next:{");
            for(int j = 0;j < 26;j++){
                if(next[i][j] != -1){
                    sb.append(dec(j)).append(":").append(next[i][j]).append(",");
                }
            }
            sb.append("}}\n");
        }
        return sb.toString();
    }

    /**
     * @param str
     * @return 1：sa包括str，-x：sa包括到第x个字符的str
     */
    public int contains(char[] str)
    {
        int cur = 0;
        for(int i = 0;i < str.length;i++){
            int nex = next[cur][enc(str[i])];
            if(nex == -1)return -i;
            cur = nex;
        }
        return 1;
    }

    /**
     * 不同连续子串的数量
     * @return
     */
    public long numberOfDistinctSubstrings()
    {
        long[] dp = new long[size];
        for(int i = size-1;i >= 0;i--){
            dp[i] = 1;
            for(int e : next[i]){
                if(e != -1)dp[i] += dp[e];
            }
        }
        return dp[0]-1; // remove empty
    }

    /**
     * 不同连续子串的长度之和
     * @return
     */
    public long totalLengthOfDistinctSubstrings()
    {
        long[] dp = new long[size];
        int[] count = new int[size];
        for(int i = size-1;i >= 0;i--){
            count[i] = 1;
            dp[i] = 0;
            for(int e : next[i]){
                if(e != -1){
                    count[i] += count[e];
                    dp[i] += dp[e] + count[e];
                }
            }
        }
        return dp[0];
    }

    /**
     * 以字典顺序返回K个连续较小的连续子字符串
     * @param K (1-indexed)
     * @return
     */
    public String kthDistinctSubstring(long K)
    {
        if(K <= 0)return null;
        long[] dp = new long[size];
        for(int i = size-1;i >= 0;i--){
            dp[i] = 1;
            for(int e : next[i]){
                if(e != -1)dp[i] += dp[e];
            }
        }
        if(K > dp[0]-1)return null;

        StringBuilder sb = new StringBuilder();
        int cur = 0;
        K++;
        while(true){
            if(K == 1)break;
            K--;
            for(int i = 0;i < next[cur].length;i++){
                int nex = next[cur][i];
                if(nex != -1){
                    long nct = dp[nex];
                    if(K <= nct){
                        sb.append(dec(i));
                        cur = nex;
                        break;
                    }else{
                        K -= nct;
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * 返回具有最小字典顺序的移位字符串。
     * 使用最小| s |字符构造SA对s + s (贪心)。
     * @param s
     * @return
     */
    public char[] smallestCyclicShift(char[] s)
    {
        char[] ss = new char[s.length*2];
        System.arraycopy(s, 0, ss, 0, s.length);
        System.arraycopy(s, 0, ss, s.length, s.length);
        SuffixAutomaton sa = buildSuffixAutomaton(ss);

        int cur = 0;
        char[] ret = new char[s.length];
        for(int i = 0;i < s.length;i++){
            for(int j = 0;j < sa.next[cur].length;j++){
                int nex = sa.next[cur][j];
                if(nex != -1){
                    ret[i] = dec(j);
                    cur = nex;
                    break;
                }
            }
        }

        return ret;
    }

    /**
     * 各nodeについて、initial->nodeの連続部分文字列が元の文字列に何回出現するかカウントした配列を返す。
     * cloneされていないノードに1をつけて、len降順に走査してlinkに加算していく。
     * 同じ文字が現れた時最初のcloneにlinkが集まる仕掛けになっている。あとはlen降順で走査すればオリジナル文字列上のカウントが集まってくる。
     * @return
     */
    public int[] enumNumberOfOccurs()
    {
        int n = size;
        int[] dp = new int[n];
        for(int i = n-1;i >= 1;i--){
            if(original[i] == -1)dp[i] += 1;
            dp[link[i]] += dp[i];
        }
        return dp;
    }

    public int track(char[] str)
    {
        int cur = 0;
        for(int i = 0;i < str.length;i++){
            int nex = next[cur][enc(str[i])];
            if(nex == -1)return -1;
            cur = nex;
        }
        return cur;
    }

    /**
     * 匹配子串第一次出现的位置
     * 用于非克隆的Len-1和用于克隆的原始的len-1是子串的结束位置。
     * @param q
     * @return
     */
    public int indexOf(char[] q)
    {
        int cur = 0;
        for(int i = 0;i < q.length;i++){
            int nex = next[cur][enc(q[i])];
            if(nex == -1)return -1;
            cur = nex;
        }
        if(original[cur] != -1)cur = original[cur];
        return len[cur]-1 - q.length + 1;
    }

    public int[][] ilinks()
    {
        int n = size;
        int[] ip = new int[n];
        for(int i = 1;i < n;i++)ip[link[i]]++;
        int[][] ret = new int[n][];
        for(int i = 0;i < n;i++)if(ip[i] > 0)ret[i] = new int[ip[i]];
        for(int i = 1;i < n;i++)ret[link[i]][--ip[link[i]]] = i;
        return ret;
    }

    /**
     * 匹配子串出现的所有位置
     * 按照故障链接进行反向操作，并为非克隆命令创建一个命中。
     * @param q
     * @param ilinks
     * @return
     */
    public BitSet indexOfAll(char[] q, int[][] ilinks)
    {
        BitSet ret = new BitSet();
        int cur = track(q);
        if(cur == -1)return ret;
        dfsIndexOfAll(cur, q.length, ilinks, ret);
        return ret;
    }

    public void dfsIndexOfAll(int cur, int qlen, int[][] ilinks, BitSet bs)
    {
        if(original[cur] == -1) bs.set(len[cur]-qlen);
        if(ilinks[cur] != null)for(int e : ilinks[cur]) dfsIndexOfAll(e, qlen, ilinks, bs);
    }

    /**
     * 不显示的最小长度 - 按字典顺序返回最小的字符串。
     * @return
     * NOT TESTED
     */
    public char[] nonOccuringString()
    {
        int n = size;
        int[] dp = new int[n];
        int[] prev = new int[n];
        Arrays.fill(prev, -1);
        for(int i = n-1;i >= 0;i--){
            dp[i] = n+1;
            for(int j = 0;j < next[i].length;j++){
                int e = next[i][j];
                int v = e != -1 ? dp[e] : 0;
                if(v < dp[i]){
                    dp[i] = v;
                    prev[i] = j;
                }
            }
            dp[i] = prev[i] == -1 ? 0 : dp[i]+1;
        }

        int cur = 0;
        char[] ret = new char[dp[0]];
        for(int i = 0;i < dp[0];i++, cur = next[cur][prev[cur]]){
            ret[i] = dec(prev[cur]);
        }
        return ret;
    }

    /**
     * 在t中连续字符串中，获取在T中最小位置的一个。
     * saをtrieとみなし、Tで検索している感じ。
     * @param t
     * @return
     */
    public char[] lcs(char[] t)
    {
        if(t.length == 0)return new char[0];
        int v = 0, l = 0, best = 0, bestPos = 0;
        for(int i = 0;i < t.length;i++){
            int e = enc(t[i]);
            while(v != 0 && next[v][e] == -1){
                v = link[v];
                l = len[v];
            }
            if(next[v][e] != -1){
                v = next[v][e];
                l++;
            }
            if(l > best){
                best = l; bestPos = i;
            }
        }
        return Arrays.copyOfRange(t, bestPos-best+1, bestPos+1);
    }

    /**
     * 多字符串连续字符串LCS。
     * O(sum |S|*W)
     * @param strs
     * @return
     * NOT TESTED
     */
    public char[] lcs(char[][] strs)
    {
        int m = strs.length;
        StringBuilder sb = new StringBuilder();
        char delim = (char)('z'-(m-1));
        for(char[] str : strs){
            sb.append(new String(str)).append(delim++);
        }
        SuffixAutomaton sa = buildSuffixAutomaton(sb.toString().toCharArray());

        int n = sa.size;
        int[] dp = new int[n];
        int besti = -1;
        for(int i = n-1;i >= 0;i--){
            for(int j = 0;j < sa.next[i].length;j++){
                int nex = sa.next[i][j];
                if(nex != -1){
                    if(j >= 25-(m-1) && j <= 25){
                        // delim
                        dp[i] |= 1<<j-(25-(m-1));
                    }else{
//						if(i >= nex){
//							U.tr(i, nex);
//						}
                        dp[i] |= dp[nex];
                    }
                }
            }
//			U.tr(i, dp[i]);
            if(dp[i] == (1<<m)-1){
                besti = i;
                break;
            }
        }
        if(besti == -1)return new char[0];

        char[] ret = new char[sa.len[besti]];
        int cur = besti;
        int pos = sa.len[besti]-1;
        for(int j = besti-1;j >= 0;j--){
            for(int k = 0;k < sa.next[j].length;k++){
                if(sa.next[j][k] == cur && sa.len[j] + 1 == sa.len[cur]){
                    ret[pos--] = dec(k);
                    cur = j;
                    break;
                }
            }
        }
        return ret;
    }
}
