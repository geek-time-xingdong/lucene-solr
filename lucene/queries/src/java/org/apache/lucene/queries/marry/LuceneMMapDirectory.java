package org.apache.lucene.queries.marry;

import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LatLonDocValuesField;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author chengzhengzheng
 * @date 2021/1/12
 */
public final class LuceneMMapDirectory {
    private static Directory maleDir;
    private static Directory     femaleDir;

    private static IndexWriter maleIW;
    private static IndexWriter femaleIW;

    private static SearcherManager maleManager;
    private static SearcherManager femaleManager;


    private final static ExecutorService executorService = new ThreadPoolExecutor(8, 8, 5,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(1000));

    public static File getFile(final String... names) {
        if (names == null) {
            throw new NullPointerException("names must not be null");
        }
        File file = null;
        for (final String name : names) {
            if (file == null) {
                file = new File(name);
            } else {
                file = new File(file, name);
            }
        }
        return file;
    }

    static {
        try {
            maleDir     = new NIOFSDirectory(getFile("_MALE").toPath());
//            maleDir.setPreload(true);
//            maleDir = new ByteBuffersDirectory();
            femaleDir     = new NIOFSDirectory(getFile("_FEMALE").toPath());
//            femaleDir.setPreload(true);
            femaleDir = new ByteBuffersDirectory();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig();
            indexWriterConfig.setUseCompoundFile(false);
            indexWriterConfig.setCodec(new SimpleTextCodec());
            maleIW = new IndexWriter(maleDir, indexWriterConfig);
            IndexWriterConfig indexWriterConfig2 = new IndexWriterConfig();
            indexWriterConfig2.setUseCompoundFile(false);
            indexWriterConfig.setCodec(new SimpleTextCodec());
            femaleIW = new IndexWriter(femaleDir, indexWriterConfig2);

            maleManager   = new SearcherManager(maleIW, new SearcherFactory() {
                @Override
                public IndexSearcher newSearcher(IndexReader reader, IndexReader previousReader) throws IOException {
                    return new MyIndexSearcher(reader, executorService);
                }
            });
            femaleManager = new SearcherManager(femaleIW,new SearcherFactory(){
                @Override
                public IndexSearcher newSearcher(IndexReader reader, IndexReader previousReader) throws IOException {
                    return new MyIndexSearcher(reader, executorService);
                }
            });


            RefreshThread.addSearchManager(new RefreshThread.SearcherManagerWrapper(maleManager,"recommendListMale",-1));
            RefreshThread.addSearchManager(new RefreshThread.SearcherManagerWrapper(femaleManager,"recommendListFemale",-1));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    public static void updateDocumentById(String id,Document document, String sexName){
        IndexWriter indexWriter = getIndexWriter(sexName);
        try {
            indexWriter.updateDocument(new Term(RoomIndexKeyWord.RECOMMEND_ID,id),document);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadDoc(List<Document> documentList,String sexName){
        IndexWriter indexWriter = getIndexWriter(sexName);
        try {
            indexWriter.addDocuments(documentList);
            indexWriter.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void load(List<RecommendListUser> recommendListUserList, String sexName) {
        IndexWriter indexWriter = getIndexWriter(sexName);
        final List<Document> documentList = new ArrayList<>();
        for (RecommendListUser recommendListUser : recommendListUserList) {
            final Document document = new Document();
            //momoid
            document.add(new StringField(RoomIndexKeyWord.RECOMMEND_ID, recommendListUser.getId(), Field.Store.YES));
            //faceScore
            document.add(new DoublePoint(RoomIndexKeyWord.RECOMMEND_FACE_SCORE, recommendListUser.getFaceScore()));
            document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_FACE_SCORE, recommendListUser.getFaceScore()));
            //lastOnlineTime
            document.add(new LongPoint(RoomIndexKeyWord.RECOMMEND_LAST_ONLINE_TIME, recommendListUser.getLastOnlineTime()));
            document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_LAST_ONLINE_TIME, recommendListUser.getLastOnlineTime()));
            //lastMarryOnlineTime
            document.add(new LongPoint(RoomIndexKeyWord.RECOMMEND_LAST_MARRY_TIME, recommendListUser.getLastMarryOnline()));
            document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_LAST_MARRY_TIME, recommendListUser.getLastMarryOnline()));
            //realPerson
            document.add(new StringField(RoomIndexKeyWord.RECOMMEND_REAL_PERSON,recommendListUser.getRealPerson() , Field.Store.YES));
            //level
            document.add(new StringField(RoomIndexKeyWord.RECOMMEND_LEVEL,recommendListUser.getLevel() , Field.Store.YES));
            //avatar
            document.add(new StringField(RoomIndexKeyWord.RECOMMEND_AVATAR,recommendListUser.getAvatar() , Field.Store.YES));
            //location
            document.add(new LatLonPoint(RoomIndexKeyWord.RECOMMEND_LOC,recommendListUser.getLat() ,recommendListUser.getLon()));
            document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_LAT,recommendListUser.getLat()));
            document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_LON,recommendListUser.getLon()));

            document.add(new LatLonDocValuesField(RoomIndexKeyWord.RECOMMEND_LON,recommendListUser.getLat(),recommendListUser.getLon()));
            //age
            document.add(new IntPoint(RoomIndexKeyWord.RECOMMEND_AGE,recommendListUser.getAge() ));
            document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_AGE,recommendListUser.getAge()));
            documentList.add(document);
        }
        try {
            indexWriter.addDocuments(documentList);
            indexWriter.commit();
//            maleManager.maybeRefresh(); //TODO
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static IndexWriter getIndexWriter(String sexName) {
        return SexEnum.MALE.getSexKey().equals(sexName) ? maleIW : femaleIW;
    }

    public static IndexSearcher getSearcher(String sexName){
        try {
            return sexName.equals(SexEnum.MALE.getSexKey()) ? maleManager.acquire() : femaleManager.acquire();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        final List<Document> documentList = new ArrayList<>();
        for (int i = 0; i <100;i++) {
            Document document = buildRandomDocument();
            documentList.add(document);
        }
        IndexWriter m = getIndexWriter("M");
        m.addDocuments(documentList);

        final List<Document> femaleList = new ArrayList<>();
        for (int i = 0; i <100;i++) {
            Document document = buildRandomDocument();
            femaleList.add(document);
        }
        IndexWriter f = getIndexWriter("F");
        f.addDocuments(femaleList);
        f.commit();
        m.commit();
    }

    public static Document buildRandomDocument() {
        Document document = new Document();
        Random   random   = new Random();
        int      length   = 6;
        int           i        = random.nextInt(5);
        StringBuilder sb       = new StringBuilder();
        for (int j = 0; j < length + i; j++) {
            sb.append(random.nextInt(10));
        }

        document.add(new StringField(RoomIndexKeyWord.RECOMMEND_ID, sb.toString(), Field.Store.YES));
        document.add(new StringField(RoomIndexKeyWord.RECOMMEND_AVATAR, "http://baidu.avatar.com", Field.Store.YES));
        int age = random.nextInt(100);
        document.add(new IntPoint(RoomIndexKeyWord.RECOMMEND_AGE, age));
        document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_AGE, age));

        double faceScore = Math.random();
        document.add(new DoublePoint(RoomIndexKeyWord.RECOMMEND_FACE_SCORE, faceScore));
        document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_FACE_SCORE, faceScore));

        long l = System.currentTimeMillis() - random.nextInt(1000 * 100000);
        document.add(new LongPoint(RoomIndexKeyWord.RECOMMEND_LAST_ONLINE_TIME, l));
        document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_LAST_ONLINE_TIME, l));

        long l1 = System.currentTimeMillis() - random.nextInt(1000 * 100000);
        document.add(new LongPoint(RoomIndexKeyWord.RECOMMEND_LAST_MARRY_TIME, l1));
        document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_LAST_MARRY_TIME, l1));

        String level = String.valueOf(random.nextInt(4));
        document.add(new StringField(RoomIndexKeyWord.RECOMMEND_LEVEL, level, Field.Store.YES));

        String s = String.valueOf(random.nextInt(100) > 50 ? Boolean.TRUE : Boolean.FALSE);
        document.add(new StringField(RoomIndexKeyWord.RECOMMEND_REAL_PERSON, s, Field.Store.YES));


        String[] strings = randomLonLat(73.807171, 134.495648, 20.287309, 51.251166);
        document.add(new LatLonPoint(RoomIndexKeyWord.RECOMMEND_LOC, Double.parseDouble(strings[0]), Double.parseDouble(strings[1])));
        document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_LON, Double.parseDouble(strings[0])));
        document.add(new StoredField(RoomIndexKeyWord.RECOMMEND_LAT,  Double.parseDouble(strings[1])));
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


    public static void releaseSearch(IndexSearcher searcher,String sex) {
        try {
            if(sex.equals(SexEnum.MALE.getSexKey())) {
                maleManager.release(searcher);
            }else{
                femaleManager.release(searcher);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
