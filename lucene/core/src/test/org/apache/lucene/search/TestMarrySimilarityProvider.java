/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.search;

import org.apache.lucene.analysis.MockAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.DoubleRange;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.IntRange;
import org.apache.lucene.document.IntRangeDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.similarities.PerFieldSimilarityWrapper;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.LuceneTestCase;

import java.util.Random;

public class TestMarrySimilarityProvider extends LuceneTestCase {
  private Directory       directory;
  private DirectoryReader reader;
  private IndexSearcher   searcher;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    directory = newDirectory();
    PerFieldSimilarityWrapper sim    = new ExampleSimilarityProvider();
    IndexWriterConfig         iwc    = new IndexWriterConfig().setSimilarity(sim);
    RandomIndexWriter         iw     = new RandomIndexWriter(random(), directory, iwc);

    Document document = buildRandomGuestDocument("1111", "cid", "0", "M");
    iw.addDocument(document);
    iw.commit();
    searcher = newSearcher(iw.getReader());
    searcher.setSimilarity(sim);
  }

  public void testBasics() throws Exception {

    BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
    booleanQueryBuilder.add(new TermQuery(new Term("avatar", "http://baidu.com.avatar.test.com")), BooleanClause.Occur.MUST);
    booleanQueryBuilder.add(new TermQuery(new Term("userId", "1111")), BooleanClause.Occur.MUST);
    Integer age = 30;
    int     minAge = Math.min(18, age - 5);
    int     maxAge = Math.min(80, age + 5);
    booleanQueryBuilder.add(IntPoint.newRangeQuery("age", minAge, maxAge), BooleanClause.Occur.MUST);

    TopDocs  foodocs  = searcher.search(booleanQueryBuilder.build(), 10);
    Document doc     = searcher.doc(foodocs.scoreDocs[0].doc);
    System.out.println(foodocs.scoreDocs[0].score);
    for (IndexableField indexableField : doc) {
      System.out.print(indexableField.name() + ":" + indexableField.stringValue() + "\t");
    }
    System.out.println();

    System.out.println(foodocs.scoreDocs[0].score);

  }

  private static class ExampleSimilarityProvider extends PerFieldSimilarityWrapper {
    private Similarity sim2 = new Sim2();
    private Similarity sim1 = new Sim1();

    @Override
    public Similarity get(String field) {
      System.out.println("ExampleSimilarityProvider : "+field);
      if(field.equals("avatar")){
        return sim1;
      }
      return sim2;
    }
  }

  private static class Sim1 extends Similarity {

    @Override
    public long computeNorm(FieldInvertState state) {
      return 111;
    }

    @Override
    public SimScorer scorer(
            float boost, CollectionStatistics collectionStats, TermStatistics... termStats) {
      return new SimScorer() {

        @Override
        public float score(float freq, long norm) {
          return 100;
        }
      };
    }
  }

  private static class Sim2 extends Similarity {

    @Override
    public long computeNorm(FieldInvertState state) {
      return 10;
    }

    @Override
    public SimScorer scorer(
            float boost, CollectionStatistics collectionStats, TermStatistics... termStats) {
      return new SimScorer() {
        @Override
        public float score(float freq, long norm) {
          return 10;
        }
      };
    }
  }


  public static Document buildRandomGuestDocument(String momoid, String cid, String seatId, String userSex) {
    Document document = new Document();
    Random   random   = new Random();


    document.add(new StringField("userId", momoid, Field.Store.YES));
    int age = random.nextInt(100);
    document.add(new IntPoint("age", 30));
    document.add(new StoredField("age", 30));

    document.add(new StringField("seat_id", seatId, Field.Store.YES));
    document.add(new StringField("roomId", cid, Field.Store.YES));
//        document.add(new SortedDocValuesField("cid", new BytesRef(cid)));

    document.add(new StringField("sex", userSex, Field.Store.YES));


    document.add(new StringField("avatar", getRandomAvatar(), Field.Store.YES));
    document.add(new StringField("cityCode", getRandomProCode(), Field.Store.YES));

    double faceScore = Math.random();
    document.add(new DoublePoint("faceScore", faceScore));
    document.add(new StoredField("faceScore", faceScore));

    String level = String.valueOf(random.nextInt(4));
    document.add(new StringField("level", level, Field.Store.YES));

    String s = String.valueOf(random.nextInt(100) > 50 ? Boolean.TRUE : Boolean.FALSE);
    document.add(new StringField("real_person", s, Field.Store.YES));

    return document;
  }

  private static String getRandomKeyWord(int minLength, int rangeLength) {
    final Random  random = new Random();
    int           i      = random.nextInt(rangeLength);
    StringBuilder momoid = new StringBuilder();
    for (int j = 0; j < minLength + i; j++) {
      momoid.append(random.nextInt(10));
    }
    return momoid.toString();
  }

  private static String getRandomAvatar() {
    return "http://baidu.com.avatar.test.com";
  }

  private static String getRandomProCode() {
    return "11";
  }

}
