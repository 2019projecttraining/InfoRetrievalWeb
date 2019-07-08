package ir.services;

import org.springframework.stereotype.Service;

import ir.models.Patent;

@Service
public interface PatentService {

	public Patent getPatentDetail(String patentId);

}
