package org.apache.lucene.marry;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LatLonDocValuesField;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chengzhengzheng
 * @date 2021/1/25
 */
public class TestCollector {
    public static void main(String[] args) throws IOException, InterruptedException {


//        insertData();
        LuceneMMapDirectory.loadDoc(new ArrayList<>(), "M");
        warmUp(Runtime.getRuntime().availableProcessors());
        search(Runtime.getRuntime().availableProcessors());
//        warmUp(1);
    }

    private static void warmUp(int times) {
        for (int i = 0; i < times; i++) {
            doSearch();
        }
    }


    private static void insertData() {
        final List<Document> documentList = new ArrayList<>();
        for (int j = 0; j < 299493; j++) {
            documentList.add(buildRandomDocument());
        }
        LuceneMMapDirectory.loadDoc(documentList, "M");
        System.out.println("insert over....");
    }

    private static void search(int concurrency) {
        System.out.println("=========================");
        int count = RecommendListSearchService.count("M");
        System.out.println("count---->" + count);

        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(600);
        for (int i = 0; i < concurrency; i++) {
            poolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        long beginTime = System.currentTimeMillis();
                        doSearch();
                        System.out.println("cost: " + (System.currentTimeMillis() - beginTime));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }
    }

    private static void doSearch() {
        int                age                = 18 + new Random().nextInt(100);
        String[]           strings            = randomLonLat(-180, 180, -90, 90);
        MatchSearchRequest matchSearchRequest = new MatchSearchRequest();
        matchSearchRequest.setAge(age);
        matchSearchRequest.setLat(Double.parseDouble(strings[0]));
        matchSearchRequest.setLon(Double.parseDouble(strings[1]));
        matchSearchRequest.setResultSize(60);
        matchSearchRequest.setSex("F");
        RecommendListSearchService.collector(matchSearchRequest);
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

        document.add(new StringField(RoomIndexKeyWord.RECOMMEND_ID, sb.toString(), Field.Store.YES));

        int age = random.nextInt(100);
        document.add(new IntPoint(RoomIndexKeyWord.RECOMMEND_AGE, age));
        document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_AGE, age));
        document.add(new DoubleDocValuesField(RoomIndexKeyWord.RECOMMEND_AGE, age));


        StringBuilder avatars = new StringBuilder();
        for (int j = 0; j < length + i; j++) {
            avatars.append(random.nextInt(10));
        }
        document.add(new StringField(RoomIndexKeyWord.RECOMMEND_AVATAR, avatars.toString(), Field.Store.YES));

        double faceScore = Math.random();
        document.add(new DoublePoint(RoomIndexKeyWord.RECOMMEND_FACE_SCORE, faceScore));
        document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_FACE_SCORE, faceScore));
        document.add(new DoubleDocValuesField(RoomIndexKeyWord.RECOMMEND_FACE_SCORE, faceScore));

        long l = System.currentTimeMillis() - random.nextInt(1000 * 100000);
        document.add(new LongPoint(RoomIndexKeyWord.RECOMMEND_LAST_ONLINE_TIME, l));
        document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_LAST_ONLINE_TIME, l));

        long l1 = System.currentTimeMillis() - random.nextInt(1000 * 100000);
        document.add(new LongPoint(RoomIndexKeyWord.RECOMMEND_LAST_MARRY_TIME, l1));
        document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_LAST_MARRY_TIME, l1));

        String level = String.valueOf(random.nextInt(4));
        document.add(new StringField(RoomIndexKeyWord.RECOMMEND_LEVEL, level, Field.Store.YES));
        document.add(new DoubleDocValuesField(RoomIndexKeyWord.RECOMMEND_LEVEL, Double.parseDouble(level)));

        String s = String.valueOf(random.nextInt(100) > 50 ? Boolean.TRUE : Boolean.FALSE);
        document.add(new StringField(RoomIndexKeyWord.RECOMMEND_REAL_PERSON, s, Field.Store.YES));


        String[] strings = randomLonLat(-180, 180, -90, 90);
        document.add(new LatLonPoint(RoomIndexKeyWord.RECOMMEND_LOC, Double.parseDouble(strings[0]), Double.parseDouble(strings[1])));

        document.add(new LatLonDocValuesField(RoomIndexKeyWord.RECOMMEND_LOC, Double.parseDouble(strings[0]), Double.parseDouble(strings[1])));
        document.add(new DoubleDocValuesField(RoomIndexKeyWord.RECOMMEND_LAT, Double.parseDouble(strings[0])));
        document.add(new DoubleDocValuesField(RoomIndexKeyWord.RECOMMEND_LON, Double.parseDouble(strings[1])));

        document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_LAT, Double.parseDouble(strings[0])));
        document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_LON, Double.parseDouble(strings[1])));
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
