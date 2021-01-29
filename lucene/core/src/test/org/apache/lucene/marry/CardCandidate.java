package org.apache.lucene.marry;

import java.io.Serializable;
import java.util.Objects;

public class CardCandidate implements Serializable {
    private String       momoid;
    private String       avatar;
    private Integer      age;
    private Integer      realPerson = 0;

    private Double lat;
    private Double lon;
    private Double faceScore;
    private Long         lastOnlineTime;
    private Long         lastMarryOnlineTime;

    private double       score;
    private Integer      remaing;
    private int          total;

    public CardCandidate(String momoid) {
        this.momoid = momoid;
    }

    public CardCandidate() {
    }

    public CardCandidate(String momoid, String avatar) {
        this.momoid = momoid;
        this.avatar = avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CardCandidate that = (CardCandidate) o;
        return momoid.equals(that.momoid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(momoid);
    }

    @Override
    public String toString() {
        return score + ":" + momoid;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    public String getMomoid() {
        return momoid;
    }

    public void setMomoid(String momoid) {
        this.momoid = momoid;
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

    public Integer getRealPerson() {
        return realPerson;
    }

    public void setRealPerson(Integer realPerson) {
        this.realPerson = realPerson;
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

    public Long getLastMarryOnlineTime() {
        return lastMarryOnlineTime;
    }

    public void setLastMarryOnlineTime(Long lastMarryOnlineTime) {
        this.lastMarryOnlineTime = lastMarryOnlineTime;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Integer getRemaing() {
        return remaing;
    }

    public void setRemaing(Integer remaing) {
        this.remaing = remaing;
    }
}