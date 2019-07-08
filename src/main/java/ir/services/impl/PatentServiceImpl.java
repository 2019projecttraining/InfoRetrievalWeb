package ir.services.impl;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ir.enumDefine.SearchAccuracy;
import ir.luceneIndex.LuceneSearcher;
import ir.models.Patent;
import ir.repositories.PatentRepositoty;
import ir.services.PatentService;

/**
 * 提供通过专利ID查询专利细节的服务
 * 
 * @author 余定邦、杨涛
 *
 */
@Service
public class PatentServiceImpl implements PatentService{
	
	

	@Override
	public Patent getPatentDetail(String patentId) {
		IndexSearcher luceneIndex = LuceneSearcher.indexes.get(SearchAccuracy.ACCURATE);
		Query q=new TermQuery(new Term("application_publish_number", patentId));
		TopDocs topDocs=null;
		try {
			topDocs=luceneIndex.search(q,1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		Patent p=null;
		try {
			Document doc = luceneIndex.doc(scoreDocs[0].doc);
			p=new Patent();
			p.setId(doc.get("id"));
			p.setPatent_Abstract(doc.get("abstract"));
			p.setAddress(doc.get("address"));
			p.setApplicant(doc.get("applicant"));
			p.setApplication_date(doc.get("application_date"));
			p.setApplication_number(doc.get("application_number"));
			p.setApplication_publish_number(doc.get("application_publish_number"));
			p.setClassification_number(doc.get("classification_number"));
			p.setFilling_date(doc.get("filing_date"));
			p.setGrant_status(doc.get("grant_status").equals("1")?"已授权":"未授权");
			String inventors="";
			for(String s:doc.getValues("inventor")) {
				inventors+=(s+";");
			}
			p.setInventor(inventors);
			p.setTitle(doc.get("title"));
			p.setYear(Integer.parseInt(doc.get("year")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return p;
	}

}
