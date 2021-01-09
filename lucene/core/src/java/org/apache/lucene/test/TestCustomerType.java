package org.apache.lucene.test;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * @author chengzhengzheng
 * @date 2021/1/8
 */
public class TestCustomerType {
    public static void main(String[] args) throws IOException {
        ByteBuffersDirectory directory = new ByteBuffersDirectory();

        IndexWriterConfig conf        = new IndexWriterConfig();
        IndexWriter       indexWriter = new IndexWriter(directory, conf);
        Document document = new Document();
//        document.add(new Field("real", ));
    }
}
