package ir.services;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.springframework.stereotype.Service;

import ir.models.BoolExpression;
import ir.models.PatentsForView;

@Service
public interface AdvancedSearchService {

	public PatentsForView search(BoolExpression[] expressions,int page,String timeFrom
			,String timeTo,IndexSearcher luceneIndex,Analyzer analyzer);
}
