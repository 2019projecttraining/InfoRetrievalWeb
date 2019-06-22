package ir.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ir.models.Patent;
import ir.repositories.PatentRepositoty;
import ir.services.PatentService;

@Service
public class PatentServiceImpl implements PatentService{
	
	@Autowired
	private PatentRepositoty patentRepositoty;

	@Override
	public Patent getPatentDetail(String patentId) {
		return patentRepositoty.getOne(patentId);
	}

}
