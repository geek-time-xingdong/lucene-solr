package org.apache.lucene.queries.marry;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.search.HitQueue;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.lucene.queries.marry.RecommendListSearchService.esPlaneDistance;


/**
 * @author chengzhengzheng
 * @date 2021/1/21
 * <p>
 * <p>
 * def ageScore = 20 * Math.exp(-Math.abs(doc['age'].value-params.age)/2);def faceScore = 20 * doc['face_score'].value;
 * def distanceScore = 15 * Math.exp(-Math.abs(doc['location'].planeDistance(params.lat,params.lon))/100000);def onlineScore = 0;
 * if(params.c-doc['last_marry_online_time'].value  <= params.m){onlineScore = 0.5;}
 * else if( params.c - doc['last_app_online_time'].value <= params.m){onlineScore = 0.25;}
 * 20 * onlineScore + ageScore + distanceScore+faceScore;
 */
public abstract class RecommendTopScoreCollector extends TopDocsCollector<ScoreDoc> {

    private static final AtomicInteger topDocs = new AtomicInteger();


//    ChronicleMap<Long, String> userMap = ChronicleMap
//            .of(Long.class, String.class)
//            .name("user-map")
//            .entries(50)
//            .create();
    /**
     * Scorable leaf collector
     */
    public abstract static class ScorerLeafCollector implements LeafCollector {

        protected Scorable scorer;

        @Override
        public void setScorer(Scorable scorer) throws IOException {
            this.scorer = scorer;
        }
    }

    public static RecommendTopScoreCollector createWithContext(int numHits, SearchContext searchContext) {
        return create(numHits, searchContext);
    }

    private static float getScore(Document document, SearchContext searchContext) {
        //自定义算分逻辑
        String faceScore      = document.get(RoomIndexKeyWord.RECOMMEND_FACE_SCORE);
        int    age            = Integer.parseInt(document.get(RoomIndexKeyWord.RECOMMEND_AGE));
        String lon            = document.get(RoomIndexKeyWord.RECOMMEND_LON);
        String lat            = document.get(RoomIndexKeyWord.RECOMMEND_LAT);
        double ageScore       = 20 * Math.exp(-Math.abs(age - searchContext.getReqAge()) / 2);
        double faceScoreScore = 20 * Double.parseDouble(faceScore);
        double distance       = esPlaneDistance(searchContext.getLat(), searchContext.getLon(), Double.parseDouble(lat), Double.parseDouble(lon));
        double distanceScore  = 20 * Math.exp(-Math.abs(distance) / 100000);
        return (float) (ageScore + faceScoreScore + distanceScore);
    }


    private static class SimpleTopScoreDocCollector extends RecommendTopScoreCollector {

        SimpleTopScoreDocCollector(int numHits) {
            super(numHits);
        }

        private SearchContext searchContext;

        SimpleTopScoreDocCollector(int numHits,SearchContext searchContext) {
            super(numHits);
            this.searchContext = searchContext;
        }
        @Override
        public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
            // reset the minimum competitive score
            docBase = context.docBase;
            return new ScorerLeafCollector() {

                @Override
                public void setScorer(Scorable scorer) throws IOException {
                    super.setScorer(scorer);
                }

                @Override
                public void collect(int doc) throws IOException {
                    LeafReader reader   = context.reader();
                    Document   document = reader.document(doc);
//                    float      score    = getScore(document, searchContext);
                    SortedDocValues docValues = reader.getSortedDocValues("recommend_id");

                    float score = scorer.score();
                    totalHits++;


                    if (score <= pqTop.score) {
                        // Since docs are returned in-order (i.e., increasing doc Id), a document
                        // with equal score to pqTop.score cannot compete since HitQueue favors
                        // documents with lower doc Ids. Therefore reject those docs too.
                        return;
                    }
                    pqTop.doc   = doc + docBase;
                    pqTop.score = score;
                    pqTop       = pq.updateTop();
                }
            };
        }

        @Override
        public ScoreMode scoreMode() {
            return ScoreMode.TOP_DOCS;
        }
    }


    /**
     * Creates a new {@link TopScoreDocCollector} given the number of hits to collect and the number
     * of hits to count accurately.
     *
     * <p><b>NOTE</b>: If the total hit count of the top docs is less than or exactly {@code
     * totalHitsThreshold} then this value is accurate. On the other hand, if the {@link
     * TopDocs#totalHits} value is greater than {@code totalHitsThreshold} then its value is a lower
     * bound of the hit count. A value of {@link Integer#MAX_VALUE} will make the hit count accurate
     * but will also likely make query processing slower.
     *
     * <p><b>NOTE</b>: The instances returned by this method pre-allocate a full array of length
     * <code>numHits</code>, and fill the array with sentinel objects.
     */

    private static RecommendTopScoreCollector create(int numHits, SearchContext searchContext) {
        if (numHits <= 0) {
            throw new IllegalArgumentException("numHits must be > 0; please use TotalHitCountCollector if you just need the total hit count");
        }

        return new SimpleTopScoreDocCollector(numHits, searchContext);
    }


    int      docBase;
    ScoreDoc pqTop;

    RecommendTopScoreCollector(
            int numHits) {
        super(new HitQueue(numHits, true));
        // HitQueue implements getSentinelObject to return a ScoreDoc, so we know
        // that at this point top() is already initialized.
        pqTop = pq.top();
    }


    @Override
    protected TopDocs newTopDocs(ScoreDoc[] results, int start) {
        if (results == null) {
            return EMPTY_TOPDOCS;
        }

        return new TopDocs(new TotalHits(totalHits, totalHitsRelation), results);
    }

}
