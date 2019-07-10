package ir.util.fieldDetection;

import org.springframework.stereotype.Service;

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
@Service
public class ApplicantDetector {
	
	public ApplicantDetector() {
		super();
		System.out.println("------------");
        System.out.println("加载多域查询申请人探测模块依赖文件");
        System.out.println("------------");
	}

	private final static Detector detectorForPeople=new Detector("inventor","field-detection-applicant-people-name-dict");
	
	private final static Detector detectorForCompany=new Detector("inventor","field-detection-applicant-company-name-dict");

	public boolean isPeopleApplicant(String word) {
		return detectorForPeople.detect(word);
	}
	
	public boolean isCompanyApplicant(String word) {
		return detectorForCompany.detect(word);
	}
	
	
}
