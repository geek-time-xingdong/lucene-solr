package org.apache.lucene.queries.marry;

import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.util.Random;

/**
 * @author chengzhengzheng
 * @date 2021/2/3
 */
public class TestByteBuffer {
    public static void main(String[] args) throws IOException {
        ByteBuffersDirectory buffersDirectory = new ByteBuffersDirectory();
        IndexWriter          indexWriter      = new IndexWriter(buffersDirectory, new IndexWriterConfig());


        for (int i = 0; i < 10; i++) {
            indexWriter.addDocument(TestCollector.buildRandomDocument());
            indexWriter.commit();
        }

        new Thread(() -> {
            while (true) {
                try {
                    int i = new Random().nextInt(100);
                    if(i > 70){
                        System.out.println("删除......");
                        indexWriter.deleteDocuments(IntPoint.newRangeQuery(RoomIndexKeyWord.RECOMMEND_AGE,50,1000));
                    }else{
                        System.out.println("添加......");
                        indexWriter.addDocument(TestCollector.buildRandomDocument());
                    }
                    indexWriter.commit();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            while (true) {
                IndexReader reader;
                try {
                    reader = DirectoryReader.open(buffersDirectory);
                    int i = reader.numDocs();
                    System.out.println(i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }
}
