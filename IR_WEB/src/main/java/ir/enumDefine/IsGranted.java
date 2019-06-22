package ir.enumDefine;

/**
 * 对专利是否授权的限制
 * 
 * @author 余定邦
 */
public enum IsGranted {

	NO_LIMIT,//对专利是否授权没有限制
	GRANTED,//只查询已经授权的专利
	NOT_GRANTED;//只查询没有授权的专利
	
}
