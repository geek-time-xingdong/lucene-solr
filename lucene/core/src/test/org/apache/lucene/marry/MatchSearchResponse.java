package org.apache.lucene.marry;

import java.util.LinkedHashSet;
import java.util.Set;

public class MatchSearchResponse {
    private Set<CardCandidate> results = new LinkedHashSet<>();

    public Set<CardCandidate> getResults() {
        return results;
    }

    public void setResults(Set<CardCandidate> results) {
        this.results = results;
    }
}
