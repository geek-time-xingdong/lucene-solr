package org.apache.lucene.test;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;

/**
 * @author chengzhengzheng
 * @date 2021/1/12
 */
public class TestMultiIndexSearch {
    public static void main(String[] args) throws IOException {
        ByteBuffersDirectory buffersDirectory1 = new ByteBuffersDirectory();
        IndexWriter writer1 = new IndexWriter(buffersDirectory1,new IndexWriterConfig());

        ByteBuffersDirectory buffersDirectory2 = new ByteBuffersDirectory();
        IndexWriter writer2 = new IndexWriter(buffersDirectory2,new IndexWriterConfig());

        Document document = new Document();
        document.add(new StringField("id","1", Field.Store.YES));
        writer1.addDocument(document);
        writer1.commit();

        Document document2 = new Document();
        document2.add(new StringField("id","1", Field.Store.YES));
        writer2.addDocument(document);
        writer2.commit();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(buffersDirectory1));
        TopDocs       topDocs   = searcher.search(new MatchAllDocsQuery(), 10);
        System.out.println(topDocs.totalHits);

        IndexSearcher searcher2 = new IndexSearcher(DirectoryReader.open(buffersDirectory2));
        TopDocs       topDocs2   = searcher2.search(new MatchAllDocsQuery(), 10);
        System.out.println(topDocs2.totalHits);



    }
}
