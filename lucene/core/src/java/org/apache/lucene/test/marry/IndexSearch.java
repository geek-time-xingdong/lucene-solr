package org.apache.lucene.test.marry;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.util.CollectionUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chengzhengzheng
 * @date 2021/1/12
 */
public class IndexSearch {
    final static List<String>             roomIds  = new CopyOnWriteArrayList<>();
    final static Map<String,List<String>> guestIds = new ConcurrentHashMap<>();

    static         ByteBuffersDirectory roomDir = new ByteBuffersDirectory();
    static         ByteBuffersDirectory userDir = new ByteBuffersDirectory();
    static         IndexWriter          roomW;
    static         IndexWriter          userW;
    private static Random               random  = new Random();

    private static SearcherManager roomSearchManager;
    private static SearcherManager userSearchManager;

    private AtomicBoolean hasStarted = new AtomicBoolean(false);
    public static void main(String[] args) throws IOException {
        roomW = new IndexWriter(roomDir, new IndexWriterConfig());
        userW = new IndexWriter(userDir, new IndexWriterConfig());

        //初始化100个房间
        for (int i = 0; i < 1; i++) {
            String s = masterOpenRoom();
            if (s != null) {
                String[] split        = s.split(":");
                String   roomId       = split[0];
                String   randomMaster = split[1];
                guestOnMic(roomId, randomMaster);
            }
        }


        roomSearchManager = new SearcherManager(userDir, null);
        userSearchManager = new SearcherManager(roomDir, null);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new RefreshThread(), 1, 1, TimeUnit.SECONDS);


        //上麦下麦(包括主持人)
        new Thread(() -> {
            try {
                while (true) {
                    onMicOrLeave();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


        new Thread(() -> {
            try {
                while (true) {
                    printAll();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


        new Thread(() -> {
//            try {
//                roomSearch();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }).start();


    }

    private static void roomSearch() throws IOException {
        IndexSearcher roomSearch = roomSearchManager.acquire();
        IndexSearcher userSearch = userSearchManager.acquire();
        for (String roomId : roomIds) {
            TopDocs roomId1 = roomSearch.search(new TermQuery(new Term("roomId", roomId)), 10);
            print(roomSearch, roomId1);

            BooleanQuery.Builder females = new BooleanQuery.Builder().
                    add(new TermQuery(new Term("roomId", roomId)), BooleanClause.Occur.MUST).
                    add(new TermQuery(new Term("sex", "F")), BooleanClause.Occur.MUST);
            TopDocs femalesDocs = userSearch.search(new ConstantScoreQuery(females.build()), 10);

            BooleanQuery.Builder maleQuery = new BooleanQuery.Builder().
                    add(new TermQuery(new Term("roomId", roomId)), BooleanClause.Occur.MUST).
                    add(new TermQuery(new Term("sex", "M")), BooleanClause.Occur.MUST);
            TopDocs maleDocs = userSearch.search(new ConstantScoreQuery(maleQuery.build()), 10);
            print(userSearch, femalesDocs);
            print(userSearch, maleDocs);
        }
        roomSearchManager.release(roomSearch);
        userSearchManager.release(userSearch);

    }

    private static void printAll() throws IOException {
        System.out.println("开始打印所有信息.....");
        IndexSearcher roomSearch  = roomSearchManager.acquire();
        TopDocs       roomTopDocs = roomSearch.search(new MatchAllDocsQuery(), 100);
        print(roomSearch, roomTopDocs);

        IndexSearcher userSearch = userSearchManager.acquire();
        TopDocs       search     = userSearch.search(new MatchAllDocsQuery(), 100);
        print(userSearch, search);
        roomSearchManager.release(roomSearch);
        userSearchManager.release(userSearch);
        System.out.println("结束打印所有信息.....");
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    static class RefreshThread implements Runnable {

        @Override
        public void run() {
            try {
                roomSearchManager.maybeRefresh();
                userSearchManager.maybeRefresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void print(IndexSearcher roomSearch, TopDocs roomTopDocs) throws IOException {
        ScoreDoc[] roomScores = roomTopDocs.scoreDocs;
        for (ScoreDoc scoreDoc : roomScores) {
            Document doc = roomSearch.doc(scoreDoc.doc);
            for (IndexableField indexableField : doc) {
                System.out.print(indexableField.name() + ":" + indexableField.stringValue() + "|");
            }
            System.out.println();
        }
    }

    private static String masterOpenRoom() throws IOException {
        String roomId       = getRandomKeyWord(5, 5);
        String randomMaster = getRandomKeyWord(6, 10);
        if (roomIds.contains(roomId) || guestIds.values().contains(randomMaster)) {
            return null;
        }
        roomIds.add(roomId);
        Document roomDoc = getRandomRoomId(roomId, randomMaster);
        roomW.addDocument(roomDoc);
        List<String> strings = guestIds.get(roomId);
        if(strings == null){
            strings = new ArrayList<>();
            guestIds.put(roomId,strings);
        }
        strings.add(randomMaster);
        roomW.commit();
        userW.commit();
        return roomId + ":" + randomMaster;
    }

    private static void guestOnMic(String roomId, String randomMaster) throws IOException {
        final List<Document> documentList = new ArrayList<>();
        Document             masterDoc    = buildRandomGuestDocument(randomMaster, roomId, "0", "F");
        documentList.add(masterDoc);
        int size = random.nextInt(3) + 1;
        for (int seatId = 1; seatId < size; seatId++) {
            String momoid = getRandomKeyWord(6, 10);
            if (!guestIds.values().contains(momoid)) {
                List<String> strings = guestIds.get(roomId);
                if(strings == null){
                    strings = new ArrayList<>();
                    guestIds.put(roomId,strings);
                }
                strings.add(randomMaster);

                Document document = buildRandomGuestDocument(momoid, roomId, String.valueOf(seatId), random.nextInt(10) > 5 ? "F" : "M");
                documentList.add(document);
            }
        }
        userW.addDocuments(documentList);
    }


    private static void onMicOrLeave() throws IOException {
        if(roomIds.size() > 0){
            int i = random.nextInt(100);
            if(i > 50){
                //删除房间已经用户信息
                String roomId = roomIds.get(random.nextInt(roomIds.size()));
                System.out.println("删除房间： "+roomId);

                roomW.deleteDocuments(new Term("roomId", roomId));
                userW.deleteDocuments(new Term("roomId", roomId));

                roomIds.remove(roomId);
                guestIds.remove(roomId);
            }
        }
        int i = random.nextInt(100);
        if(i > 50){
            String s = masterOpenRoom();

            if (s != null) {
                String[] split        = s.split(":");
                String   tempId       = split[0];
                System.out.println("开房： "+tempId);
                String   randomMaster = split[1];
                guestOnMic(tempId, randomMaster);
            }
        }

        roomW.commit();
        userW.commit();
    }

    public static Document getRandomRoomId(String cid, String master) {
        Document document = new Document();
        Random   random   = new Random();
        document.add(new StringField("roomId", cid, Field.Store.YES));
        document.add(new StringField("mode", String.valueOf(random.nextInt(7)), Field.Store.YES));
        document.add(new StringField("stage", "1", Field.Store.YES));
        document.add(new StringField("master", master, Field.Store.YES));
        document.add(new StringField("roomNotice", master + "的房间" + cid, Field.Store.YES));
        return document;
    }

    public static Document buildRandomGuestDocument(String momoid, String cid, String seatId, String userSex) {
        Document document = new Document();
        Random   random   = new Random();


        document.add(new StringField("userId", momoid, Field.Store.YES));
        int age = random.nextInt(100);
        document.add(new IntPoint("age", age));
        document.add(new StoredField("age", age));

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


    /**
     * private static final long serialVersionUID = -1L;
     *     private final String      momoid;
     *     private final String      age;
     *     private final String      userSex;
     *     private final String      name;
     *     private final String      avatar;
     *     private final Double  faceScore;
     *     private final String  seatId;
     *     private final String  cid;
     *     private final String  cityCode;
     *     private final Double  lng;
     *     private final Double  lat;
     *     private final Boolean isNew;
     *     private final Integer userRole;
     *     private final String  realPerson;
     *     private final long    becomeMatchMakerTime;
     *     private final int     onlineStatus = 1;
     */

}
