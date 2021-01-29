package org.apache.lucene.test;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;

/**
 * @author chengzhengzheng
 * @date 2021/1/11
 */
public class NearRealTimeSearchDemo4 {
    public static void main(String[] args) throws IOException {
        ByteBuffersDirectory ramDirectory = new ByteBuffersDirectory();
        Document             document     = new Document();
        document.add(new TextField("title", "Doc1", Field.Store.YES));
        document.add(new IntPoint("ID", 1));
        IndexWriter indexWriter = new IndexWriter(ramDirectory, new IndexWriterConfig(new StandardAnalyzer()));
        indexWriter.addDocument(document);
        indexWriter.commit();
        SearcherManager searcherManager = new SearcherManager(ramDirectory, null);
        document = new Document();
        document.add(new TextField("title", "Doc2", Field.Store.YES));
        document.add(new IntPoint("ID", 2));
        indexWriter.addDocument(document);
        //即使commit，搜索依然不可见，需要重新打开reader
        indexWriter.commit();
        //只能看到Doc1
        IndexSearcher indexSearcher = searcherManager.acquire();
        int           count         = indexSearcher.count(new MatchAllDocsQuery());
        if (count > 0) {
            TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), count);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                System.out.println(indexSearcher.doc(scoreDoc.doc));
            }
        }
        System.out.println("=================================");
        //最好放到finally语句块中
        searcherManager.release(indexSearcher);
        if (!searcherManager.isSearcherCurrent()) {
            //说明有新数据，需要刷新
            searcherManager.maybeRefresh();
        }
        indexSearcher = searcherManager.acquire();
        count         = indexSearcher.count(new MatchAllDocsQuery());
        if (count > 0) {
            TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), count);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                System.out.println(indexSearcher.doc(scoreDoc.doc));
            }
        }
        System.out.println("=================================");
        searcherManager.release(indexSearcher);
    }
}
