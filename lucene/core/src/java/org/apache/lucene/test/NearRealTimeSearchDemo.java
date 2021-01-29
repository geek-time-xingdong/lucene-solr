package org.apache.lucene.test;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author chengzhengzheng
 * @date 2021/1/11
 */
public class NearRealTimeSearchDemo {
    public static void main(String[] args) throws IOException {
        ByteBuffersDirectory ramDirectory = new ByteBuffersDirectory();
        Document             document     = new Document();
        document.add(new TextField("title", "Doc1", Field.Store.YES));
        document.add(new IntPoint("ID", 1));
        IndexWriter indexWriter = new IndexWriter(ramDirectory, new IndexWriterConfig(new StandardAnalyzer()));
        indexWriter.addDocument(document);
        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(indexWriter));
        int           count         = indexSearcher.count(new MatchAllDocsQuery());
        if (count > 0) {
            TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), count);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                //即便没有commit，依然能看到Doc1
                System.out.println(indexSearcher.doc(scoreDoc.doc));
            }
        }
        System.out.println("=================================");
        document = new Document();
        document.add(new TextField("title", "Doc2", Field.Store.YES));
        document.add(new IntPoint("ID", 2));
        indexWriter.addDocument(document);
        indexWriter.commit();
        count = indexSearcher.count(new MatchAllDocsQuery());
        TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), count);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            //即便后来commit了，依然无法看到Doc2，因为没有重新打开IndexSearcher
            System.out.println(indexSearcher.doc(scoreDoc.doc));
        }
        System.out.println("=================================");
        indexSearcher.getIndexReader().close();
        indexSearcher = new IndexSearcher(DirectoryReader.open(indexWriter));
        count = indexSearcher.count(new MatchAllDocsQuery());
        topDocs = indexSearcher.search(new MatchAllDocsQuery(), count);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            //重新打开IndexSearcher，可以看到Doc1和Doc2
            System.out.println(indexSearcher.doc(scoreDoc.doc));
        }
        System.out.println("=================================");
    }
}
