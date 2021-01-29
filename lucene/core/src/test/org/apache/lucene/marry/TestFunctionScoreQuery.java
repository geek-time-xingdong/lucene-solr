package org.apache.lucene.marry;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

/**
 * @author chengzhengzheng
 * @date 2021/1/18
 */
public class TestFunctionScoreQuery extends BaseSearch{
    public static void main(String[] args) throws IOException {
        setUp();

        DirectoryReader reader        = DirectoryReader.open(directory);
        IndexSearcher   indexSearcher = new IndexSearcher(reader);


    }
}
