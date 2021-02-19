package org.apache.lucene.queries.marry;

import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

import static org.apache.lucene.queries.marry.RecommendListSearchService.buildBoolQueryWithDivide;
import static org.apache.lucene.queries.marry.RecommendListSearchService.result;

/**
 * @author chengzhengzheng
 * @date 2021/2/7
 */
public class FunctionScoreSearchService {

    public static MatchSearchResponse collector(MatchSearchRequest request)  {
        MatchSearchResponse response = new MatchSearchResponse();
        IndexSearcher search = LuceneMMapDirectory.getSearcher(request.getSex() == SexEnum.MALE.getSexKey() ? SexEnum.FEMALE.getSexKey() : SexEnum.MALE.getSexKey());
        //这里的RECOMMEND_AGE是从docValue中获取的、列式数据库中获取的、性能是否比StoreField正排表快？
        FunctionScoreQuery functionScoreQuery = new FunctionScoreQuery(buildBoolQueryWithDivide(request), DoubleValuesSource.fromField(RoomIndexKeyWord.RECOMMEND_AGE, value -> value));
        TopDocs topDocs;
        try {
            topDocs = search.search(functionScoreQuery, request.getResultSize());
            result(response, search, topDocs.scoreDocs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;

    }

}
