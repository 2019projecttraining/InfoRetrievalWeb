package ir.services;

import org.springframework.stereotype.Service;

import ir.models.PatentsForView;

@Service
public interface SearchService {

	//基础搜索
	public PatentsForView search(String keyWords);
	
	//TODO 高级搜索
	
}
