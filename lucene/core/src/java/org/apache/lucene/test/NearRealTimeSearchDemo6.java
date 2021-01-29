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
import org.apache.lucene.search.SearcherLifetimeManager;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chengzhengzheng
 * @date 2021/1/11
 */
public class NearRealTimeSearchDemo6 {
    private static SearcherManager searcherManager;

    public static void main(String[] args) throws IOException {
        SearcherLifetimeManager searcherLifetimeManager = new SearcherLifetimeManager();
        ByteBuffersDirectory ramDirectory = new ByteBuffersDirectory();
        Document document = new Document();
        document.add(new TextField("title", "Doc1", Field.Store.YES));
        document.add(new IntPoint("ID", 1));
        IndexWriter indexWriter = new IndexWriter(ramDirectory, new IndexWriterConfig(new StandardAnalyzer()));
        indexWriter.addDocument(document);
        indexWriter.commit();

        document = new Document();
        document.add(new TextField("title", "Doc2", Field.Store.YES));
        document.add(new IntPoint("ID", 2));
        indexWriter.addDocument(document);
        indexWriter.commit();

        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(ramDirectory));
        //记录当前的searcher，保存token，当有后续的搜索请求到来，例如用户翻页，那么用这个token去获取对应的那个searcher
        long record = searcherLifetimeManager.record(indexSearcher);
        indexSearcher = searcherLifetimeManager.acquire(record);
        if (indexSearcher != null) {
            // Searcher is still here
            try {
                // do searching...
            } finally {
                searcherLifetimeManager.release(indexSearcher);
                // Do not use searcher after this!
                indexSearcher = null;
            }
        } else {
            // Searcher was pruned -- notify user session timed out, or, pull fresh searcher again
        }
        //由于保留许多的searcher是非常耗系统资源的，包括打开发files和RAM，所以最好在一个单独的线程中，定期的重新打开searcher和定时的去清理旧的searcher
        //丢弃所有比指定的时间都老的searcher
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new NearRealTimeSearchDemo6.RefreshThread(searcherLifetimeManager), 1, 1, TimeUnit.SECONDS);
    }

    static class RefreshThread implements Runnable {
        private final SearcherLifetimeManager searcherLifetimeManager;
        public RefreshThread(SearcherLifetimeManager searcherLifetimeManager) {
            this.searcherLifetimeManager = searcherLifetimeManager;
        }

        @Override
        public void run() {
            try {
                searcherManager.maybeRefresh();
                searcherLifetimeManager.prune(new SearcherLifetimeManager.PruneByAge(600.0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
