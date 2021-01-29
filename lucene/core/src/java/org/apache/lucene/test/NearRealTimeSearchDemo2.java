package org.apache.lucene.test;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author chengzhengzheng
 * @date 2021/1/11
 */
public class NearRealTimeSearchDemo2 {
    public static void main(String[] args) throws IOException {
        ByteBuffersDirectory ramDirectory = new ByteBuffersDirectory();
        Document             document     = new Document();
        document.add(new TextField("title", "Doc1", Field.Store.YES));
        document.add(new IntPoint("ID", 1));
        IndexWriter indexWriter = new IndexWriter(ramDirectory, new IndexWriterConfig(new StandardAnalyzer()));
        indexWriter.addDocument(document);
        indexWriter.commit();
        SearcherManager searcherManager = new SearcherManager(indexWriter, null);
        //当没有调用者等待指定的generation的时候，必须要重新打开时间间隔5s，言外之意，如果有调用者在等待指定的generation，则只需等0.25s
        //防止不断的重新打开，严重消耗系统性能，设置最小重新打开时间间隔0.25s
        ControlledRealTimeReopenThread<IndexSearcher> controlledRealTimeReopenThread = new ControlledRealTimeReopenThread<>(indexWriter,
                searcherManager, 5, 0.25);
        //设置为后台线程
        controlledRealTimeReopenThread.setDaemon(true);
        controlledRealTimeReopenThread.setName("controlled reopen thread");
        controlledRealTimeReopenThread.start();
        int count = 0;
        IndexSearcher indexSearcher = searcherManager.acquire();
        try {
            //只能看到Doc1
            count = indexSearcher.count(new MatchAllDocsQuery());
            if (count > 0) {
                TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), count);
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    System.out.println(indexSearcher.doc(scoreDoc.doc));
                }
            }
            System.out.println("=================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            searcherManager.release(indexSearcher);
        }
        document = new Document();
        document.add(new TextField("title", "Doc2", Field.Store.YES));
        document.add(new IntPoint("ID", 2));
        indexWriter.addDocument(document);
        try {
            TimeUnit.SECONDS.sleep(6);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //休息6s之后，即使没有commit，依然可以搜索到Doc2，因为ControlledRealTimeReopenThread会刷新SearchManager
        indexSearcher = searcherManager.acquire();
        try {
            count = indexSearcher.count(new MatchAllDocsQuery());
            if (count > 0) {
                TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), count);
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    System.out.println(indexSearcher.doc(scoreDoc.doc));
                }
            }
            System.out.println("=================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            searcherManager.release(indexSearcher);
        }
        document = new Document();
        document.add(new TextField("title", "Doc3", Field.Store.YES));
        document.add(new IntPoint("ID", 3));
        long generation = indexWriter.addDocument(document);
        try {
            //当有调用者等待某个generation的时候，只需要0.25s即可重新打开
            controlledRealTimeReopenThread.waitForGeneration(generation);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        indexSearcher = searcherManager.acquire();
        try {
            count = indexSearcher.count(new MatchAllDocsQuery());
            if (count > 0) {
                TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), count);
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    System.out.println(indexSearcher.doc(scoreDoc.doc));
                }
            }
            System.out.println("=================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            searcherManager.release(indexSearcher);
        }
    }
}
