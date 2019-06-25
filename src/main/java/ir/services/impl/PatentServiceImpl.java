package ir.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ir.models.Patent;
import ir.repositories.PatentRepositoty;
import ir.services.PatentService;

/**
 * 提供通过专利ID查询专利细节的服务
 * 
 * @author 余定邦
 *
 */
//@Service
//public class PatentServiceImpl implements PatentService{
//	
//	@Autowired
//	private PatentRepositoty patentRepositoty;
//
//	@Override
//	public Patent getPatentDetail(String patentId) {
//		return patentRepositoty.getOne(patentId);
//	}
//
//}
