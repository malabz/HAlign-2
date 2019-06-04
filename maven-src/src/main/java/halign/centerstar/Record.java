package halign.centerstar;

import java.util.ArrayList;

/**
 * 记录类型，用于保存Aho-Corasick算法搜索时找到的匹配信息。
 *
 * @author 李慧妍
 * @version 2006年3月27日
 */
public class Record {

    int site;	/*用于记录该匹配模式在T中出现的起始位置。因为
                      Aho-Corasick算法是针对每个位置的，因此每个记录只
                        保存一个位置。*/
    ArrayList<Position> name = new ArrayList<Position>(); /*用于记录该记录保存的位置
                                                 所对应的所有模式*/
        /*请注意：
         *从上面的信息域可以看出，尽管每个记录可以对应多个模式，但每个记录却
          只保存一个位置。因此会存在多个记录（位置）对应一个模式的情况。*/

    /**
     * 记录的构造方法：根据匹配字符串在T中的起始位置和对应的所有模式建立
     * 一个新记录。
     *
     * @param st  - 本次匹配的模式的在T中的起始位置。
     * @param pos - 本次匹配的所有模式在P中的编号。
     */
    Record(int st, ArrayList<?> pos) {
        site = st;            //让site等于st。
        Position p;         //用一个Position变量p来遍历pos。
        for (int i = 0; i < pos.size(); i++) { //对于pos中的每一个
            p = (Position) pos.get(i); //Position类对象，
            name.add(p);               //将这个对象添加到name里
        }
    }
}
