package org.apache.lucene.queries.marry;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author chengzhengzheng
 * @date 2021/1/18
 */
public class RecommendListSearchService {


    /**
     * 试用 CollectorManager 多个段并行查询
     *
     * @param request
     * @return
     */
    public static MatchSearchResponse collector(MatchSearchRequest request) {
        MatchSearchResponse response = new MatchSearchResponse();
        IndexSearcher       search   = LuceneMMapDirectory.getSearcher(request.getSex() == SexEnum.MALE.getSexKey() ? SexEnum.FEMALE.getSexKey() : SexEnum.MALE.getSexKey());
        try {
            SearchContext searchContext = new SearchContext();
            searchContext.setLat(request.getLat());
            searchContext.setLon(request.getLon());
            searchContext.setReqAge(request.getAge());
//            TopDocs topDocs = search.search(buildBoolQueryWithDivide(request), createSharedManager(searchContext, request));
            TopDocs topDocs = search.search(new MatchAllDocsQuery(), createSharedManager(searchContext, request));
            result(response, search, topDocs.scoreDocs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static CollectorManager<RecommendTopScoreCollector, TopDocs> createSharedManager(SearchContext searchContext, MatchSearchRequest request) {
        return new CollectorManager<>() {
            @Override
            public RecommendTopScoreCollector newCollector() {
                return RecommendTopScoreCollector.createWithContext(request.getResultSize(), searchContext);
            }


            @Override
            public TopDocs reduce(Collection<RecommendTopScoreCollector> collectors) {
                final TopDocs[] topDocs = new TopDocs[collectors.size()];
                int             i       = 0;
                for (RecommendTopScoreCollector collector : collectors) {
                    topDocs[i++] = collector.topDocs();
                }
                return TopDocs.merge(request.getResultSize(), topDocs);
            }
        };
    }


    public static Query buildBoolQueryWithDivide(MatchSearchRequest request) {
        Integer              age                 = request.getAge();
        int                  minAge              = Math.max(18, age - 5);
        int                  maxAge              = Math.min(80, age + 5);
        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        booleanQueryBuilder.add(new TermQuery(new Term(RoomIndexKeyWord.RECOMMEND_AVATAR, "")), BooleanClause.Occur.MUST_NOT);
//        booleanQueryBuilder.add(LatLonPoint.newDistanceQuery(RoomIndexKeyWord.RECOMMEND_LOC, request.getLat(), request.getLon(), 500000), BooleanClause.Occur.FILTER);
        booleanQueryBuilder.add(IntPoint.newRangeQuery(RoomIndexKeyWord.RECOMMEND_AGE, minAge, maxAge), BooleanClause.Occur.FILTER);
        booleanQueryBuilder.add(DoublePoint.newRangeQuery(RoomIndexKeyWord.RECOMMEND_FACE_SCORE, 0, 1), BooleanClause.Occur.FILTER);
        return booleanQueryBuilder.build();
    }

    /**
     * count
     *
     * @param sex
     * @return
     */
    public static int count(String sex) {
        IndexSearcher search = LuceneMMapDirectory.getSearcher(sex);
        try {
            return search.count(new MatchAllDocsQuery());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public static void result(MatchSearchResponse response, IndexSearcher search, ScoreDoc[] scoreDocs) throws IOException {
        final Set<CardCandidate> cardCandidateList = new LinkedHashSet<>();
        for (ScoreDoc scoreDoc : scoreDocs) {
            CardCandidate cardCandidate = getCardCandidate(search.doc(scoreDoc.doc));
            cardCandidate.setScore(scoreDoc.score);
            cardCandidateList.add(cardCandidate);
        }
        response.setResults(cardCandidateList);
    }


    private static CardCandidate getCardCandidate(Document doc) {
        String faceScore          = doc.get(RoomIndexKeyWord.RECOMMEND_FACE_SCORE);
        String id                 = doc.get(RoomIndexKeyWord.RECOMMEND_ID);
        String lastOnlineTime     = doc.get(RoomIndexKeyWord.RECOMMEND_LAST_ONLINE_TIME);
        String lastMaryOnlineTime = doc.get(RoomIndexKeyWord.RECOMMEND_LAST_MARRY_TIME);
        String realPerson         = doc.get(RoomIndexKeyWord.RECOMMEND_REAL_PERSON);

        String avatar = doc.get(RoomIndexKeyWord.RECOMMEND_AVATAR);
        String age    = doc.get(RoomIndexKeyWord.RECOMMEND_AGE);

        CardCandidate cardCandidate = new CardCandidate();
        cardCandidate.setAge(Integer.parseInt(age));
        cardCandidate.setFaceScore(Double.parseDouble(faceScore));
        cardCandidate.setMomoid(id);
        cardCandidate.setLastOnlineTime(Long.parseLong(lastOnlineTime));
        cardCandidate.setLastMarryOnlineTime(Long.parseLong(lastMaryOnlineTime));
        cardCandidate.setRealPerson(Boolean.parseBoolean(realPerson) ? 1 : 0);
        cardCandidate.setAvatar(avatar);
        cardCandidate.setLat(Double.parseDouble(doc.get(RoomIndexKeyWord.RECOMMEND_LAT)));
        cardCandidate.setLon(Double.parseDouble((doc.get(RoomIndexKeyWord.RECOMMEND_LON))));
        return cardCandidate;
    }

    public static final double TO_RADIANS = Math.PI / 180D;

    public static final double EARTH_MEAN_RADIUS = 6371008.7714D;      // meters (WGS 84)

    public static double esPlaneDistance(double lat1, double lon1, double lat2, double lon2) {
        double x = (lon2 - lon1) * TO_RADIANS * Math.cos((lat2 + lat1) / 2.0 * TO_RADIANS);
        double y = (lat2 - lat1) * TO_RADIANS;
        return Math.sqrt(x * x + y * y) * EARTH_MEAN_RADIUS;
    }

}
