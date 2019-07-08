package ir.enumDefine;

public enum PatentTypeCode {

//	A01, A21, A22, A23, A24, A41, A42, A43, A44, A45, A46, A47, A61, A62, A63, 
//	
//	B01, B02, B03, B05, B06, B07, B08, B09, B21, B22, B23, B24, B25, B26, B27, B28, B29, B30, B31, 
//	B32, B33, B41, B42, B43, B44, B60, B61, B62, B63, B64, B65, B66, B67, B81, B82, 
//	
//	C01, C02, C03, C04, C05, C06, C07, C08, C09, C10, C11, C12, C14, C21, C22, C23, C25, C30, C40, 
//	
//	D01, D02, D03, D04, D05, D06, D21, 
//	
//	E01, E02, E03, E04, E05, E06, E21, 
//	
//	F01, F02, F03, F04, F15, F16, F17, F21, F22, F23, F24, F25, F26, F27, F28, F41, F42, 
//	
//	G01, G02, G03, G04, G05, G06, G07, G08, G09, G10, G11, G12, G16, G21, 
//	
//	H01, H02, H03, H04, H05;
	ALL("全部类别"),
	A("人类生活必需"),
	B("作业；运输"),
	C("化学；冶金"),
	D("纺织；造纸"),
	E("固定建筑物"),
	F("机械工程；照明；加热；武器；爆破"),
	G("物理"),
	H("电学");
	
	public final String description;

	private PatentTypeCode(String description) {
		this.description = description;
	}
	
}
