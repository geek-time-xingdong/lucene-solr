package org.apache.lucene.queries.marry;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.IndexSearcher;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author chengzhengzheng
 * @date 2021/1/29
 */
public class MyIndexSearcher extends IndexSearcher {
    public MyIndexSearcher(IndexReader r) {
        super(r);
    }



    public MyIndexSearcher(IndexReader r, Executor executor) {
        super(r, executor);
    }

    public MyIndexSearcher(IndexReaderContext context, Executor executor) {
        super(context, executor);
    }

    public MyIndexSearcher(IndexReaderContext context) {
        super(context);
    }

    @Override
    protected LeafSlice[] slices(List<LeafReaderContext> leaves) {
        //lucene处理一个段只会给一个线程处理
        return slices(leaves, 10000, 1);
    }
}
