package org.apache.lucene.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;

/**
 * @author chengzhengzheng
 * @date 2021/1/9
 */
public class TestNearRead {
    public static void main(String[] args) throws IOException {
        Directory   directory   = new ByteBuffersDirectory();
        IndexWriter indexWriter = new IndexWriter(directory,new IndexWriterConfig());
        Document    doc;
        doc = new Document();
        doc.add(new StringField("content", "a", Field.Store.YES));
        doc.add(new StringField("name", "chengzheng", Field.Store.YES));
        indexWriter.addDocument(doc);

        doc = new Document();
        doc.add(new StringField("content", "bcd", Field.Store.YES));
        doc.add(new StringField("name", "chengzheng2", Field.Store.YES));
        indexWriter.addDocument(doc);

        DirectoryReader reader = indexWriter.getReader();
        int             i      = reader.numDocs();
        System.out.println(i);

    }
}
