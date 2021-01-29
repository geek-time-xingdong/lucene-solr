package org.apache.lucene.marry;


/**
 * @author chengzhengzheng
 * @date 2021/1/12
 */
public class RecommendListUser {
    private String id;
    private Double faceScore;
    private Long lastOnlineTime;
    private String realPerson;
    private String level;
    private Long lastMarryOnline;
    private Double lat = -1D;
    private Double lon = - 1D;
    private String avatar;
    private Integer age;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getFaceScore() {
        return faceScore;
    }

    public void setFaceScore(Double faceScore) {
        this.faceScore = faceScore;
    }

    public Long getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(Long lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
    }

    public String getRealPerson() {
        return realPerson;
    }

    public void setRealPerson(String realPerson) {
        this.realPerson = realPerson;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getLastMarryOnline() {
        return lastMarryOnline;
    }

    public void setLastMarryOnline(Long lastMarryOnline) {
        this.lastMarryOnline = lastMarryOnline;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
