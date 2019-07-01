package ir.services;

import org.apache.lucene.search.IndexSearcher;
import org.springframework.stereotype.Service;

import ir.enumDefine.FieldType;
import ir.enumDefine.FirstLetterOfNamePinyin;
import ir.enumDefine.IsGranted;
import ir.enumDefine.SortedType;
import ir.models.PatentsForView;

@Service
public interface SearchService {

	public PatentsForView search(FieldType field ,String keyWords,int page,FirstLetterOfNamePinyin letter,String timeFrom,//PatentsForView
			String timeTo,IsGranted isGranted,SortedType sortedType,IndexSearcher luceneIndex);
	
}
