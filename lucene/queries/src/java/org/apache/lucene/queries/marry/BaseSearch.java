package org.apache.lucene.queries.marry;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.ByteBuffersDirectory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Random;

/**
 * @author chengzhengzheng
 * @date 2021/1/8
 */
public class BaseSearch {
    protected static IndexWriter       indexWriter;
    protected static ByteBuffersDirectory directory;
    public static void setUp() throws IOException {
        directory  = new ByteBuffersDirectory();
        IndexWriterConfig conf        = new IndexWriterConfig();
        indexWriter  = new IndexWriter(directory, conf);

        for (int i = 1; i < 10000; i++) {
            try {
                indexWriter.addDocument(buildRandomDocument());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        indexWriter.commit();
    }
    public static void main(String[] args) throws Exception {
//        MMapDirectory directory = new MMapDirectory(Path.of("/Users/chengzheng/lucenedir/"));

        for (int i = 0;i <100;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        try {
                            int age = new Random().nextInt(100);
                            indexWriter.updateDocument(new Term("age", String.valueOf(age)),buildRandomDocument());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }).start();
        }



//        DirectoryReader reader        = DirectoryReader.open(directory);
//        IndexSearcher   indexSearcher = new IndexSearcher(reader);
//
//        TopDocs    res       = indexSearcher.search(new MatchAllDocsQuery(), 1000);
//        ScoreDoc[] scoreDocs = res.scoreDocs;
//        for (ScoreDoc scoreDoc : scoreDocs) {
//            Document info = reader.document(scoreDoc.doc);
//            for (IndexableField rw : info) {
//                System.out.print(rw.name() + ":" + rw.stringValue() + "\t");
//            }
//            System.out.println();
//        }

        System.in.read();

    }

    public static Document buildRandomDocument() {
        Document      document = new Document();
        Random        random   = new Random();
        int           length   = 6;
        int           i        = random.nextInt(5);
        StringBuilder sb       = new StringBuilder();
        for (int j = 0; j < length + i; j++) {
            sb.append(random.nextInt(10));
        }

        document.add(new StringField("id", sb.toString(), Field.Store.YES));

        int age = random.nextInt(100);
        document.add(new IntPoint("age", age));
        document.add(new StoredField("age", age));

        double faceScore = Math.random();
        document.add(new DoublePoint("faceScore", faceScore));
        document.add(new StoredField("faceScore", faceScore));

        long l = System.currentTimeMillis() - random.nextInt(1000 * 100000);
        document.add(new LongPoint("last_app_online_time", l));
        document.add(new StoredField("last_app_online_time", l));

        long l1 = System.currentTimeMillis() - random.nextInt(1000 * 100000);
        document.add(new LongPoint("last_marry_online_time", l1));
        document.add(new StoredField("last_marry_online_time", l1));

        String level = String.valueOf(random.nextInt(4));
        document.add(new StringField("level", level, Field.Store.YES));

        String s = String.valueOf(random.nextInt(100) > 50 ? Boolean.TRUE : Boolean.FALSE);
        document.add(new StringField("real_person", s, Field.Store.YES));


        String[] strings = randomLonLat(73.807171, 134.495648, 20.287309, 51.251166);
        document.add(new LatLonPoint("location", Double.parseDouble(strings[0]), Double.parseDouble(strings[1])));
        return document;
    }

    /**
     * @param MinLon ：最小经度 MaxLon： 最大经度
     *               MinLat：最小纬度
     *               MaxLat：最大纬度
     * @return @throws
     * @Description: 在矩形内随机生成经纬度
     */
    public static String[] randomLonLat(double MinLon, double MaxLon, double MinLat, double MaxLat) {
        BigDecimal db  = new BigDecimal(Math.random() * (MaxLon - MinLon) + MinLon);
        String     lon = db.setScale(6, BigDecimal.ROUND_HALF_UP).toString();// 小数后6位
        db = new BigDecimal(Math.random() * (MaxLat - MinLat) + MinLat);
        String lat = db.setScale(6, BigDecimal.ROUND_HALF_UP).toString();
        return new String[]{lat, lon};
    }

}
