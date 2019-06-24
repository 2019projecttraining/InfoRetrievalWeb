package ir.services;

import org.springframework.stereotype.Service;

import ir.models.Patent;
import ir.models.PatentsForView;

@Service
public interface PatentService {

	public Patent getPatentDetail(String patentId);

}
