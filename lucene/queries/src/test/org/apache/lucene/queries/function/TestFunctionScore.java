package org.apache.lucene.queries.function;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chengzhengzheng
 * @date 2021/1/20
 */
public class TestFunctionScore {
    public static void main(String[] args) throws Exception {
        ByteBuffersDirectory buffersDirectory = new ByteBuffersDirectory();
        IndexWriter indexWriter = new IndexWriter(buffersDirectory, new IndexWriterConfig());
        Document document = new Document();
        document.add(new NumericDocValuesField("popularity",10));
        document.add(new StoredField("popularity",10));
        document.add(new NumericDocValuesField("count",20));
        document.add(new StoredField("count",20));
        indexWriter.addDocument(document);


        document = new Document();
        document.add(new NumericDocValuesField("popularity",15));
        document.add(new StoredField("popularity",15));
        document.add(new NumericDocValuesField("count",20));
        document.add(new StoredField("count",20));
        indexWriter.addDocument(document);

        document = new Document();
        document.add(new NumericDocValuesField("popularity",1));
        document.add(new StoredField("popularity",1));
        document.add(new NumericDocValuesField("count",3));
        document.add(new StoredField("count",3));
        indexWriter.addDocument(document);

        indexWriter.commit();
        IndexReader reader = DirectoryReader.open(buffersDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs search = searcher.search(new FunctionScoreQuery(new MatchAllDocsQuery(), getDoubleValues()), 10);
        ScoreDoc[] scoreDocs = search.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            for (IndexableField indexableField : doc) {
                System.out.println(indexableField.name()+":"+indexableField.stringValue());
            }
        }
        System.out.println(search);
    }


    public static double oneArgMethod(double arg){
        return arg;
    }
    public static DoubleValuesSource getDoubleValues() throws Exception {

        Map<String, Method> functions = new HashMap<>();
        functions.put("foo", TestFunctionScore.class.getMethod("oneArgMethod", double.class));
        functions.put("foo2", TestFunctionScore.class.getMethod("oneArgMethod", double.class));
        functions.put("foo3", TestFunctionScore.class.getMethod("oneArgMethod", double.class));
        functions.put("foo4", TestFunctionScore.class.getMethod("oneArgMethod", double.class));
        functions.put("foo5", TestFunctionScore.class.getMethod("oneArgMethod", double.class));

        Expression     expr     = JavascriptCompiler.compile("foo(popularity)+foo2(count)+foo3(faceScore)+foo4(distance)+foo5(age)", functions, TestFunctionScore.class.getClassLoader());
        SimpleBindings bindings = new SimpleBindings();
        bindings.add("popularity", DoubleValuesSource.fromIntField("popularity"));
        bindings.add("count", DoubleValuesSource.fromIntField("count"));

        bindings.add("count", DoubleValuesSource.fromIntField("realPerson"));
        bindings.add("count", DoubleValuesSource.fromIntField("count"));
        bindings.add("count", DoubleValuesSource.fromIntField("count"));
        DoubleValuesSource vs = expr.getDoubleValuesSource(bindings);

        return vs;
    }
}
