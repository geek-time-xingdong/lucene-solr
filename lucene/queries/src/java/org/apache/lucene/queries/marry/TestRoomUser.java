package org.apache.lucene.queries.marry;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author chengzhengzheng
 * @date 2021/3/2
 */
public class TestRoomUser {
    final static Random random = new Random();
    private static final List<String> cityCodes = new ArrayList<>();

    private static final List<String> allUserIds = new ArrayList<>();
    private static SearcherManager searcherManager = null;

    public static void main(String[] args) throws IOException, InterruptedException {

        ByteBuffersDirectory buffersDirectory = new ByteBuffersDirectory();
        IndexWriter          indexWriter      = new IndexWriter(buffersDirectory, new IndexWriterConfig());
        setUp();
        //insert
        indexWriter.addDocuments(buildRandom(20000));
        indexWriter.commit();

        searcherManager = new SearcherManager(indexWriter, false, false, new SearcherFactory());
        ControlledRealTimeReopenThread cRTReopenThead = new ControlledRealTimeReopenThread(indexWriter, searcherManager,
                5.0, 0.025);
        cRTReopenThead.setDaemon(true);
        cRTReopenThead.setName("searchManagerUpdate");
        // 开启线程
        cRTReopenThead.start();
        IndexSearcher searcher = searcherManager.acquire();
        int           count    = searcher.count(new MatchAllDocsQuery());
        System.out.println(count);

        //searcher
        search();
    }

    private static void search() throws IOException, InterruptedException {
        String[] strings = randomLonLat(-180, 180, -90, 90);

        for (int i = 0;i < 10000;i++){
            List<String> hasReceivedRecommend = getHasReceivedRecommend();
            final List<Map<String, Integer>> ageRecommend = getAgeRecommend();
            long startTime = System.currentTimeMillis();
            String sex = random.nextInt(100) > 50 ? "F" : "M";
            Document document = doSearcher(sex, ageRecommend, strings, hasReceivedRecommend);
            System.out.println(System.currentTimeMillis() - startTime);
            Thread.sleep(1000);
        }


    }

    private static List<String> getHasReceivedRecommend() {
        List<String> hasReceivedRecommend = new ArrayList<>();
        int available = random.nextInt(50);
        for (int i = 0;i < available;i++){
            hasReceivedRecommend.add(allUserIds.get(random.nextInt(allUserIds.size())));
        }
        return hasReceivedRecommend;
    }

    private static List<Map<String, Integer>> getAgeRecommend() {
        final List<Map<String, Integer>> ageRecommend = new ArrayList<>();
        int level = random.nextInt(3);
        int min = 0;
        for (int i = 0;i < level + 2;i++){
            final Map<String, Integer> cap = new HashMap<>();
            if (min == 0){
                int                 low  = random.nextInt(100) - min;
                min = low;
                cap.put("low",low);
                cap.put("high",low + 5);
                ageRecommend.add(cap);
            }else{
                int low = min - 5 * i;
                cap.put("low", low);
                cap.put("high", low + 5 * i);
                ageRecommend.add(cap);
            }
        }
        return ageRecommend;
    }

    private static Document doSearcher(String opSex, List<Map<String, Integer>> ageRecommend, String[] strings, List<String> hasReceivedRecommend) throws IOException {
        IndexSearcher searcher = searcherManager.acquire();
        BooleanQuery.Builder boolBuilder = new BooleanQuery.Builder()
                //过滤性别
                .add(new TermQuery(new Term(RoomIndexKeyWord.USER_SEX, opSex)), BooleanClause.Occur.FILTER)
                //提升颜值 5分
                .add(new BoostQuery(DoublePoint.newRangeQuery(RoomIndexKeyWord.USER_FACE_SCORE, 0.5, 1), 5), BooleanClause.Occur.SHOULD)
                //200km 以内提升 1分
                .add(new BoostQuery(LatLonPoint.newDistanceQuery(RoomIndexKeyWord.USER_LOCATION, Double.parseDouble(strings[0]), Double.parseDouble(strings[1]), 200_000), 2), BooleanClause.Occur.SHOULD);

        //过滤已经推荐过的用户
        Set<BytesRef> blacks = new HashSet<>();
        for (String s : hasReceivedRecommend) {
            blacks.add(new BytesRef(s));
        }
        if (!blacks.isEmpty()) {
            boolBuilder.add(new TermInSetQuery(RoomIndexKeyWord.USER_MOMOID, blacks), BooleanClause.Occur.MUST_NOT);
        }

        //年龄提升  4分
        if (!ageRecommend.isEmpty()) {
            Map<String, Integer> range  = ageRecommend.get(ageRecommend.size() - 1);
            Integer              low    = range.get("low");
            Integer              higher = range.get("high");
            boolBuilder.add(new BoostQuery(IntPoint.newRangeQuery(RoomIndexKeyWord.USER_AGE, low, higher), 4), BooleanClause.Occur.SHOULD);
        }
        //新用户提升  3分
        boolBuilder.add(new BoostQuery(new TermQuery(new Term(RoomIndexKeyWord.USER_IS_NEW, String.valueOf(Boolean.TRUE))), 3), BooleanClause.Occur.SHOULD);

        //提升房间内嘉宾人数质量 (只有一个异性在里面) 提升 2分
        BooleanQuery.Builder roomTypeQuery  = new BooleanQuery.Builder();
        BooleanQuery.Builder roomTypeQuery2 = new BooleanQuery.Builder();
        if (SexEnum.FEMALE.getSexKey().equals(opSex)) {
            //只有女的
            roomTypeQuery.add(IntPoint.newRangeQuery(RoomIndexKeyWord.ROOM_MALE_GUEST_SIZE, 0, 0), BooleanClause.Occur.FILTER);
            roomTypeQuery.add(IntPoint.newRangeQuery(RoomIndexKeyWord.ROOM_FEMALE_GUEST_SIZE, 1, 3), BooleanClause.Occur.FILTER);
            //男女都有
            roomTypeQuery2.add(IntPoint.newRangeQuery(RoomIndexKeyWord.ROOM_MALE_GUEST_SIZE, 1, 3), BooleanClause.Occur.FILTER);
            roomTypeQuery2.add(IntPoint.newRangeQuery(RoomIndexKeyWord.ROOM_FEMALE_GUEST_SIZE, 1, 3), BooleanClause.Occur.FILTER);
        } else {
            //只有男的
            roomTypeQuery.add(IntPoint.newRangeQuery(RoomIndexKeyWord.ROOM_FEMALE_GUEST_SIZE, 0, 0), BooleanClause.Occur.FILTER);
            roomTypeQuery.add(IntPoint.newRangeQuery(RoomIndexKeyWord.ROOM_MALE_GUEST_SIZE, 1, 3), BooleanClause.Occur.FILTER);
            //男女都有
            roomTypeQuery2.add(IntPoint.newRangeQuery(RoomIndexKeyWord.ROOM_FEMALE_GUEST_SIZE, 1, 3), BooleanClause.Occur.FILTER);
            roomTypeQuery2.add(IntPoint.newRangeQuery(RoomIndexKeyWord.ROOM_MALE_GUEST_SIZE, 1, 3), BooleanClause.Occur.FILTER);
        }

        boolBuilder.add(new BoostQuery(roomTypeQuery.build(), 1), BooleanClause.Occur.SHOULD);
        boolBuilder.add(new BoostQuery(roomTypeQuery2.build(), 0.9f), BooleanClause.Occur.SHOULD);
        BooleanQuery query     = boolBuilder.build();
        TopDocs      search1   = searcher.search(query, 1);
        ScoreDoc[]   scoreDocs = search1.scoreDocs;
        if (scoreDocs.length > 0) {
            Document doc = searcher.doc(scoreDocs[0].doc);
            return doc;
        }
        return null;

    }



    private static List<Document> buildRandom(int roomSize) {
        final List<Document> documentList = new ArrayList<>();
        for (int i = 0; i < roomSize; i++) {
            String             cid          = randomStr(5);
            int                mode         = random.nextInt(8);
            String             owner        = randomStr(8);
            allUserIds.add(owner);
            int                randomManGuestSize           = 1 + random.nextInt(10);
            int                randomFemaleGuestSize           = 1 + random.nextInt(10);
            Document masterDoc = getUserDoc(cid, mode, owner,"-1", randomManGuestSize, randomFemaleGuestSize, 0);
            documentList.add(masterDoc);
            for (int j = 1; j < 1+ randomManGuestSize; j++) {
                Document userDoc = getUserDoc(cid, mode, owner,"-1", randomManGuestSize, randomFemaleGuestSize, j);

                documentList.add(userDoc);
            }
            for (int j = 0; j < 1 + randomFemaleGuestSize; j++) {
                Document userDoc = getUserDoc(cid, mode, owner,"-1", randomManGuestSize, randomFemaleGuestSize, j);
                documentList.add(userDoc);
            }
        }
        return documentList;
    }

    private static Document getUserDoc(String cid, int mode, String owner, String role,int randomManGuestSize, int randomFemaleGuestSize, int j) {
        String userId = randomStr(8);
        allUserIds.add(userId);
        Document document = new Document();
        //id
        document.add(new StringField(RoomIndexKeyWord.USER_MOMOID, userId, Field.Store.YES));
        //age
        String age = random.nextInt(100) + "";
        document.add(new IntPoint(RoomIndexKeyWord.USER_AGE, Integer.parseInt(age)));
        document.add(new StoredField(RoomIndexKeyWord.USER_AGE, Integer.parseInt(age)));
        //sex
        document.add(new StringField(RoomIndexKeyWord.USER_SEX, "M", Field.Store.YES));
        //name
        document.add(new StringField(RoomIndexKeyWord.USER_NAME, randomStr(3), Field.Store.YES));
        //avatar
        document.add(new StringField(RoomIndexKeyWord.USER_AVATAR, randomStr(10), Field.Store.YES));

        //faceScore
        double faceScore = random.nextDouble();
        document.add(new DoublePoint(RoomIndexKeyWord.USER_FACE_SCORE, faceScore));
        document.add(new StoredField(RoomIndexKeyWord.USER_FACE_SCORE, faceScore));
        document.add(new DoubleDocValuesField(RoomIndexKeyWord.USER_FACE_SCORE, faceScore));
        //seatId
        document.add(new StringField(RoomIndexKeyWord.USER_SEAT_ID, String.valueOf(j), Field.Store.YES));
        //cid
        document.add(new StringField(RoomIndexKeyWord.USER_CID, cid, Field.Store.YES));
        //cityCode
        document.add(new StringField(RoomIndexKeyWord.USER_PROV_CODE, cityCodes.get(random.nextInt(cityCodes.size())), Field.Store.YES));
        //地理位置
        String[] strings = randomLonLat(-180, 180, -90, 90);
        document.add(new LatLonPoint(RoomIndexKeyWord.USER_LOCATION, Double.parseDouble(strings[0]), Double.parseDouble(strings[1])));
        document.add(new StoredField(RoomIndexKeyWord.USER_LAT, Double.parseDouble(strings[0])));
        document.add(new StoredField(RoomIndexKeyWord.USER_LON, Double.parseDouble(strings[1])));
        //user role
        document.add(new StringField(RoomIndexKeyWord.USER_ROLE, role, Field.Store.YES));
        document.add(new StringField(RoomIndexKeyWord.USER_REAL_PERSON, role, Field.Store.YES));
        //become time
        document.add(new LongPoint(RoomIndexKeyWord.USER_BECOME_MATCHMAKER_TIME, System.currentTimeMillis()));
        document.add(new StoredField(RoomIndexKeyWord.USER_BECOME_MATCHMAKER_TIME, System.currentTimeMillis()));
        //online
        document.add(new StringField(RoomIndexKeyWord.USER_ONLINE_STATUS, "1", Field.Store.YES));
        //isNew
        document.add(new StringField(RoomIndexKeyWord.USER_IS_NEW, random.nextInt(100) > 50 ? "1" : "0", Field.Store.YES));


        document.add(new StringField(RoomIndexKeyWord.ROOM_CID, cid, Field.Store.YES));
        document.add(new StringField(RoomIndexKeyWord.ROOM_MODE, String.valueOf(mode), Field.Store.YES));
        document.add(new StringField(RoomIndexKeyWord.ROOM_STAGE, "1", Field.Store.YES));
        document.add(new StringField(RoomIndexKeyWord.ROOM_SERVER_TYPE, "2", Field.Store.YES));
        document.add(new StringField(RoomIndexKeyWord.ROOM_OWNER, owner, Field.Store.YES));
        document.add(new StringField(RoomIndexKeyWord.ROOM_NOTICE, randomStr(10), Field.Store.YES));
        document.add(new StringField(RoomIndexKeyWord.ROOM_WHITE_ROOM, "0", Field.Store.YES));
        document.add(new IntPoint(RoomIndexKeyWord.ROOM_MALE_GUEST_SIZE, randomManGuestSize));
        document.add(new StoredField(RoomIndexKeyWord.ROOM_MALE_GUEST_SIZE, randomManGuestSize));
        document.add(new IntPoint(RoomIndexKeyWord.ROOM_FEMALE_GUEST_SIZE, randomFemaleGuestSize));
        document.add(new StoredField(RoomIndexKeyWord.ROOM_FEMALE_GUEST_SIZE, randomFemaleGuestSize));
        return document;
    }

    public static String[] randomLonLat(double MinLon, double MaxLon, double MinLat, double MaxLat) {
        BigDecimal db  = new BigDecimal(Math.random() * (MaxLon - MinLon) + MinLon);
        String     lon = db.setScale(6, BigDecimal.ROUND_HALF_UP).toString();// 小数后6位
        db = new BigDecimal(Math.random() * (MaxLat - MinLat) + MinLat);
        String lat = db.setScale(6, BigDecimal.ROUND_HALF_UP).toString();
        return new String[]{lat, lon};
    }


    private static String randomStr(int len) {
        int           i  = random.nextInt(5);
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < len + i; j++) {
            sb.append(random.nextInt(10));
        }
        String cid = sb.toString();
        return cid;
    }

    private static void setUp() {
        cityCodes.add("22");
        cityCodes.add("44");
        cityCodes.add("23");
        cityCodes.add("45");
        cityCodes.add("46");
        cityCodes.add("50");
        cityCodes.add("51");
        cityCodes.add("52");
        cityCodes.add("31");
        cityCodes.add("53");
        cityCodes.add("32");
        cityCodes.add("54");
        cityCodes.add("11");
        cityCodes.add("33");
        cityCodes.add("34");
        cityCodes.add("12");
        cityCodes.add("35");
        cityCodes.add("13");
        cityCodes.add("34");
        cityCodes.add("36");
        cityCodes.add("37");
        cityCodes.add("37");
        cityCodes.add("15");
        cityCodes.add("61");
        cityCodes.add("62");
        cityCodes.add("41");
        cityCodes.add("63");
        cityCodes.add("42");
        cityCodes.add("64");
        cityCodes.add("65");
        cityCodes.add("21");
        cityCodes.add("43");
    }
}
