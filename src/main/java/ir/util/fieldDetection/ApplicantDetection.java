package ir.util.fieldDetection;

/**
 * 申请人探测
 * 
 * 申请人有可能是一个普通的人或者公司
 * 
 * 对于人名，使用精确的比对，
 * 对于公司名，则进行了分词，因为一般不会有人搜索某个公司的全名
 * 
 * @author 余定邦
 *
 */
public class ApplicantDetection {
	
	private final static Detector detectorForPeople=new Detector("inventor","field-detection-applicant-people-name-dict");
	
	private final static Detector detectorForCompany=new Detector("inventor","field-detection-applicant-company-name-dict");

	public static boolean isPeopleApplicant(String word) {
		return detectorForPeople.detect(word);
	}
	
	public static boolean isCompanyApplicant(String word) {
		return detectorForCompany.detect(word);
	}
	
	
}
