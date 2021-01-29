package org.apache.lucene.marry;


/**
 * @author chengzhengzheng
 * @date 2021/1/25
 */
public class SearchContext {
    private Integer reqAge;
    private Double lat;
    private Double lon;

    public Integer getReqAge() {
        return reqAge;
    }

    public void setReqAge(Integer reqAge) {
        this.reqAge = reqAge;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }
}
