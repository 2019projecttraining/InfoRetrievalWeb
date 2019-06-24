package ir.util.w2v;

/**
 * 词及其相似度
 * 用来作为返回结果集合中的单个对象。
 * @author 杨涛
 *
 */
public class WordEntry implements Comparable<WordEntry> {
    public String name;
    public float score;

    public WordEntry(String name, float score) {
        this.name = name;
        this.score = score;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return this.name + "\t" + score;
    }

    @Override
    public int compareTo(WordEntry o) {
        // TODO Auto-generated method stub
        if (this.score < o.score) {
            return 1;
        } else {
            return -1;
        }
    }

}