package ir.services;

import org.springframework.stereotype.Service;

import ir.models.PatentsForView;

@Service
public interface SearchService {

	//������ѯ
	public PatentsForView search(String keyWords);
	
	//TODO ����ĸ߼���ѯ
	
}
