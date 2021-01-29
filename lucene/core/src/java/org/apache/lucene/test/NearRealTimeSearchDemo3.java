package org.apache.lucene.test;

import org.apache.lucene.document.Document;
import org.apache.lucene.store.ByteBuffersDirectory;
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
public class NearRealTimeSearchDemo3 {
    public static void main(String[] args) throws IOException {
        ByteBuffersDirectory ramDirectory = new ByteBuffersDirectory();
        Document             document     = new Document();
        document.add(new TextField("title", "Doc1", Field.Store.YES));
        document.add(new IntPoint("ID", 1));
        IndexWriter indexWriter = new IndexWriter(ramDirectory, new IndexWriterConfig(new StandardAnalyzer()));
        indexWriter.addDocument(document);
        indexWriter.commit();
        DirectoryReader directoryReader = DirectoryReader.open(ramDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
        document = new Document();
        document.add(new TextField("title", "Doc2", Field.Store.YES));
        document.add(new IntPoint("ID", 2));
        indexWriter.addDocument(document);
        //即使commit，搜索依然不可见，需要重新打开reader
        indexWriter.commit();
        //只能看到Doc1
        int count = indexSearcher.count(new MatchAllDocsQuery());
        TopDocs search = indexSearcher.search(new MatchAllDocsQuery(), 10);

        if (count > 0) {
            TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), count);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                System.out.println(indexSearcher.doc(scoreDoc.doc));
            }
        }
        System.out.println("=================================");
        //如果发现有新数据更新，则会返回一个新的reader
        DirectoryReader newReader = DirectoryReader.openIfChanged(directoryReader);
        if (newReader != null) {
            indexSearcher = new IndexSearcher(newReader);
            directoryReader.close();
        }
        count = indexSearcher.count(new MatchAllDocsQuery());
        if (count > 0) {
            TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), count);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                System.out.println(indexSearcher.doc(scoreDoc.doc));
            }
        }
        System.out.println("=================================");
    }
}
