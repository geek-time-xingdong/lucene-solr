package org.apache.lucene.queries.marry;

public enum SexEnum {

    FEMALE("F", "female", "M"),
    MALE("M", "male", "F"),
    SEX_UNKNOWN("Sex_Unknown", "Sex_Unknown", "Sex_Unknown");


    private final String sexKey;
    private final String sexVal;
    private String oppositeSexKey;

    SexEnum(String sexKey, String sexVal) {
        this.sexKey = sexKey;
        this.sexVal = sexVal;
    }

    SexEnum(String sexKey, String sexVal, String oppositeSexKey) {
        this.sexKey = sexKey;
        this.sexVal = sexVal;
        this.oppositeSexKey = oppositeSexKey;
    }


    public String getSexKey() {
        return sexKey;
    }

    public String getSexVal() {
        return sexVal;
    }

    public SexEnum getOppositeSex() {
        return SexEnum.enumOf(oppositeSexKey);
    }

    public static SexEnum enumOf(final String sexStr) {

        for (final SexEnum filterSexEnum : values()) {
            if (filterSexEnum.getSexKey().equals(sexStr)) {
                return filterSexEnum;
            }
        }
        return SexEnum.SEX_UNKNOWN;
    }


}