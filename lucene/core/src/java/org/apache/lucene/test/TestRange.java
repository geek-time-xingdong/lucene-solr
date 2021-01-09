package org.apache.lucene.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author chengzhengzheng
 * @date 2021/1/6
 */
public class TestRange {
    public static void main(String[] args) throws IOException {
        ByteBuffersDirectory directory = new ByteBuffersDirectory();

        IndexWriterConfig    conf        = new IndexWriterConfig();
        IndexWriter          indexWriter = new IndexWriter(directory, conf);
        Document             doc;
        doc = new Document();
        doc.add(new StringField("content", "a", Field.Store.YES));
        doc.add(new StringField("name", "chengzheng", Field.Store.YES));
        indexWriter.addDocument(doc);

        doc = new Document();
        doc.add(new StringField("content", "bcd", Field.Store.YES));
        doc.add(new StringField("name", "chengzheng2", Field.Store.YES));
        indexWriter.addDocument(doc);

        doc = new Document();
        doc.add(new StringField("content", "ga", Field.Store.YES));
        doc.add(new StringField("name", "chengzheng3", Field.Store.YES));
        indexWriter.addDocument(doc);

        doc = new Document();
        doc.add(new StringField("content", "gc", Field.Store.YES));
        doc.add(new StringField("name", "chengzheng4", Field.Store.YES));
        indexWriter.addDocument(doc);

        indexWriter.commit();

        DirectoryReader reader        = DirectoryReader.open(directory);
        IndexSearcher   indexSearcher = new IndexSearcher(reader);
        TermRangeQuery  query         = new TermRangeQuery("content", new BytesRef("bc"), new BytesRef("gch"), true, true);
        TopDocs         results       = indexSearcher.search(query, 100);
        ScoreDoc[]      scoreDocs     = results.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document res = indexSearcher.doc(scoreDoc.doc);
            for (IndexableField rw : res) {
                System.out.print(rw.name() + ":" + rw.stringValue()+"\t");
            }
            System.out.println();
        }
    }
}
