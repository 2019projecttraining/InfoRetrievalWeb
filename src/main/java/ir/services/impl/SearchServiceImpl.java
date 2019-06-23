package ir.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ir.luceneIndex.LuceneSearcher;
import ir.models.PatentsForView;
import ir.services.SearchService;

@Service
public class SearchServiceImpl implements SearchService{

	@Autowired
	private LuceneSearcher luceneSearcher;
	
	@Override
	public PatentsForView search(String keyWords) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
