package org.apache.lucene.queries.function;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TopScoreDocCollectorV2;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author chengzhengzheng
 * @date 2021/1/20
 */
public class TestCollector {
    public static void main(String[] args) throws Exception {
        ByteBuffersDirectory buffersDirectory = new ByteBuffersDirectory();
        IndexWriter indexWriter = new IndexWriter(buffersDirectory, new IndexWriterConfig());

        ArrayList<Integer> list = new ArrayList();

        for (int i = 0; i < 1000;i++){
            Document document = new Document();
            int      popularity       = new Random().nextInt(1000);
            list.add(popularity);
            document.add(new LongPoint("popularity",popularity));
            document.add(new StoredField("popularity",popularity));
            indexWriter.addDocument(document);
        }
        list.sort((o1, o2) -> o2 - o1);
        System.out.println(list);

        indexWriter.commit();
        IndexReader reader = DirectoryReader.open(buffersDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);

        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        booleanQueryBuilder.add(LongPoint.newRangeQuery("popularity",50,80),BooleanClause.Occur.FILTER);


        TopScoreDocCollector collector = TopScoreDocCollector.create(20, null, 10);
        searcher.search(booleanQueryBuilder.build(),collector);
        TopDocs topDocs = collector.topDocs();
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            for (IndexableField indexableField : doc) {
                System.out.print(indexableField.name()+":"+indexableField.stringValue()+"\t");
            }
            System.out.println();
        }


        System.out.println("=============");
        TopScoreDocCollectorV2 topScoreDocCollector = TopScoreDocCollectorV2.create(3, scoreDocs[scoreDocs.length-2], 10);
        searcher.search(booleanQueryBuilder.build(),topScoreDocCollector);
        topDocs = topScoreDocCollector.topDocs();
        scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            for (IndexableField indexableField : doc) {
                System.out.print(indexableField.name()+":"+indexableField.stringValue()+"\t");
            }
            System.out.println();
        }


    }



}
