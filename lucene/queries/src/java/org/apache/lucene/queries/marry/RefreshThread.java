package org.apache.lucene.queries.marry;

import org.apache.lucene.search.SearcherManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chengzhengzheng
 * @date 2021/1/12
 */
public class RefreshThread {
    final static         List<SearcherManagerWrapper> searcherManagerList = new ArrayList<>();
    private static final ScheduledExecutorService     executorService     = Executors.newSingleThreadScheduledExecutor();
    private static       Object                       lock                = new Object();


    public static void addSearchManager(SearcherManagerWrapper searcherManagerWrapper) {
        synchronized (lock) {
            searcherManagerList.add(searcherManagerWrapper);
        }
    }

    static class SearcherManagerWrapper {
        SearcherManager searcherManager;
        String          tag;
        Integer         score;

        public SearcherManagerWrapper(SearcherManager searcherManager, String tag, Integer score) {
            this.searcherManager = searcherManager;
            this.tag             = tag;
            this.score           = score;
        }
    }

    static {
        RefreshThread.start();
    }

    private static void start() {
        executorService.scheduleAtFixedRate(() -> {
            synchronized (lock) {
                searcherManagerList.sort(Comparator.comparingInt(o -> o.score));
                for (SearcherManagerWrapper searcherManager : searcherManagerList) {
                    try {
                        searcherManager.searcherManager.maybeRefresh();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

}
