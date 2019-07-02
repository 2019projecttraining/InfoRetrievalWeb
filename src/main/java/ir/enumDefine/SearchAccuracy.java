package ir.enumDefine;

/**
 * 查询的粒度，根据这个粒度去查找不同的索引
 * 
 * @author 余定邦
 *
 */
public enum SearchAccuracy {

	FUZZY,//粗粒度
	ACCURATE,//精确
	SINGLE_WORD,
	DOUBLE_WORD;
}
