package org.apache.lucene.queries.marry;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.store.Directory;

import java.io.IOException;

/**
 * @author chengzhengzheng
 * @date 2021/1/29
 * <p>
 * <p>
 * 由于压测结果表明、多个reader查询, 性能有提升、故此SearchManager 提升点是维护了多个 IndexSearcher
 * 预先创建多个 IndexSearch
 * https://zzzoot.blogspot.com/2008/06/lucene-concurrent-search-performance.html
 */
public class MySearcherManager extends MyReferenceManager<IndexSearcher> {

    private final SearcherFactory searcherFactory;

    /**
     * Creates and returns a new SearcherManager from the given {@link IndexWriter}.
     *
     * @param writer          the IndexWriter to open the IndexReader from.
     * @param searcherFactory An optional {@link SearcherFactory}. Pass <code>null</code> if you don't
     *                        require the searcher to be warmed before going live or other custom behavior.
     * @throws IOException if there is a low-level I/O error
     */
    public MySearcherManager(IndexWriter writer, SearcherFactory searcherFactory) throws IOException {
        this(writer, true, false, searcherFactory);
    }

    /**
     * Expert: creates and returns a new SearcherManager from the given {@link IndexWriter},
     * controlling whether past deletions should be applied.
     *
     * @param writer          the IndexWriter to open the IndexReader from.
     * @param applyAllDeletes If <code>true</code>, all buffered deletes will be applied (made
     *                        visible) in the {@link IndexSearcher} / {@link DirectoryReader}. If <code>false</code>, the
     *                        deletes may or may not be applied, but remain buffered (in IndexWriter) so that they will
     *                        be applied in the future. Applying deletes can be costly, so if your app can tolerate
     *                        deleted documents being returned you might gain some performance by passing <code>false
     *                        </code>. See {@link DirectoryReader#openIfChanged(DirectoryReader, IndexWriter, boolean)}.
     * @param writeAllDeletes If <code>true</code>, new deletes will be forcefully written to index
     *                        files.
     * @param searcherFactory An optional {@link SearcherFactory}. Pass <code>null</code> if you don't
     *                        require the searcher to be warmed before going live or other custom behavior.
     * @throws IOException if there is a low-level I/O error
     */
    public MySearcherManager(
            IndexWriter writer,
            boolean applyAllDeletes,
            boolean writeAllDeletes,
            SearcherFactory searcherFactory)
            throws IOException {
        if (searcherFactory == null) {
            searcherFactory = new SearcherFactory();
        }
        this.searcherFactory = searcherFactory;
        current              =
                getSearcher(
                        searcherFactory, DirectoryReader.open(writer, applyAllDeletes, writeAllDeletes), null);
    }

    /**
     * Creates and returns a new SearcherManager from the given {@link Directory}.
     *
     * @param dir             the directory to open the DirectoryReader on.
     * @param searcherFactory An optional {@link SearcherFactory}. Pass <code>null</code> if you don't
     *                        require the searcher to be warmed before going live or other custom behavior.
     * @throws IOException if there is a low-level I/O error
     */
    public MySearcherManager(Directory dir, SearcherFactory searcherFactory) throws IOException {
        if (searcherFactory == null) {
            searcherFactory = new SearcherFactory();
        }
        this.searcherFactory = searcherFactory;
        current              = getSearcher(searcherFactory, DirectoryReader.open(dir), null);
    }

    /**
     * Creates and returns a new SearcherManager from an existing {@link DirectoryReader}. Note that
     * this steals the incoming reference.
     *
     * @param reader          the DirectoryReader.
     * @param searcherFactory An optional {@link SearcherFactory}. Pass <code>null</code> if you don't
     *                        require the searcher to be warmed before going live or other custom behavior.
     * @throws IOException if there is a low-level I/O error
     */
    public MySearcherManager(DirectoryReader reader, SearcherFactory searcherFactory)
            throws IOException {
        if (searcherFactory == null) {
            searcherFactory = new SearcherFactory();
        }
        this.searcherFactory = searcherFactory;
        this.current         = getSearcher(searcherFactory, reader, null);
    }

    @Override
    protected void decRef(IndexSearcher reference) throws IOException {
        reference.getIndexReader().decRef();
    }

    @Override
    protected IndexSearcher refreshIfNeeded(IndexSearcher referenceToRefresh) throws IOException {
        final IndexReader r = referenceToRefresh.getIndexReader();
        assert r instanceof DirectoryReader
                : "searcher's IndexReader should be a DirectoryReader, but got " + r;
        final IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) r);
        if (newReader == null) {
            return null;
        } else {
            return getSearcher(searcherFactory, newReader, r);
        }
    }

    @Override
    protected boolean tryIncRef(IndexSearcher reference) {
        return reference.getIndexReader().tryIncRef();
    }

    @Override
    protected int getRefCount(IndexSearcher reference) {
        return reference.getIndexReader().getRefCount();
    }

    /**
     * Returns <code>true</code> if no changes have occurred since this searcher ie. reader was
     * opened, otherwise <code>false</code>.
     *
     * @see DirectoryReader#isCurrent()
     */
    public boolean isSearcherCurrent() throws IOException {
        final IndexSearcher searcher = acquire();
        try {
            final IndexReader r = searcher.getIndexReader();
            assert r instanceof DirectoryReader
                    : "searcher's IndexReader should be a DirectoryReader, but got " + r;
            return ((DirectoryReader) r).isCurrent();
        } finally {
            release(searcher);
        }
    }

    /**
     * Expert: creates a searcher from the provided {@link IndexReader} using the provided {@link
     * SearcherFactory}. NOTE: this decRefs incoming reader on throwing an exception.
     */
    public static IndexSearcher getSearcher(
            SearcherFactory searcherFactory, IndexReader reader, IndexReader previousReader)
            throws IOException {
        boolean             success = false;
        final IndexSearcher searcher;
        try {
            searcher = searcherFactory.newSearcher(reader, previousReader);
            if (searcher.getIndexReader() != reader) {
                throw new IllegalStateException(
                        "SearcherFactory must wrap exactly the provided reader (got "
                                + searcher.getIndexReader()
                                + " but expected "
                                + reader
                                + ")");
            }
            success = true;
        } finally {
            if (!success) {
                reader.decRef();
            }
        }
        return searcher;
    }
}
