package ir.models;

import ir.enumDefine.BoolOptionSymbol;
import ir.enumDefine.FieldType;

/**
 *此类用来代表一行高级搜索框的值
 * @author 杨涛
 *
 */
public class BoolExpression {

	public BoolOptionSymbol symbol;
	public String keyWords;
	public FieldType field;
	
	public BoolExpression(String keyWords,FieldType field,BoolOptionSymbol symbol) {
		this.keyWords=keyWords;
		this.field=field;
		this.symbol=symbol;
	}
	
}
