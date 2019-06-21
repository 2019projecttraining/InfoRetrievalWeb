package ir.services;

import org.springframework.stereotype.Service;

import ir.models.PatentsForView;

@Service
public interface SearchService {

	//基本查询
	public PatentsForView search(String keyWords);
	
	//TODO 其余的高级查询
	
}
