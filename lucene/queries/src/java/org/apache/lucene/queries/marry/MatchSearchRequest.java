package org.apache.lucene.queries.marry;

public class MatchSearchRequest {
    private String                 sex;
    private Boolean                isNew;
    private Integer                age;
    private Integer                resultSize;
    private String                 scriptSource;
    private Integer                offset;
    private String                 momoId;
    private String  nextScrollId;
    private boolean esAutoFill;
    private boolean isTest;
    private double lat;
    private double lon;

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Boolean getNew() {
        return isNew;
    }

    public void setNew(Boolean aNew) {
        isNew = aNew;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getResultSize() {
        return resultSize;
    }

    public void setResultSize(Integer resultSize) {
        this.resultSize = resultSize;
    }

    public String getScriptSource() {
        return scriptSource;
    }

    public void setScriptSource(String scriptSource) {
        this.scriptSource = scriptSource;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getNextScrollId() {
        return nextScrollId;
    }

    public void setNextScrollId(String nextScrollId) {
        this.nextScrollId = nextScrollId;
    }

    public boolean isEsAutoFill() {
        return esAutoFill;
    }

    public void setEsAutoFill(boolean esAutoFill) {
        this.esAutoFill = esAutoFill;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }

    public String getMomoId() {
        return momoId;
    }

    public void setMomoId(String momoId) {
        this.momoId = momoId;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}