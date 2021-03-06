package org.smartregister.reporting.domain;

public class IndicatorQuery {
    private Long id;
    private String indicatorCode;
    private String query;
    private int dbVersion;

    public IndicatorQuery(Long id, String indicatorCode, String query, int dbVersion) {
        this.id = id;
        this.indicatorCode = indicatorCode;
        this.query = query;
        this.dbVersion = dbVersion;
    }

    public IndicatorQuery() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIndicatorCode() {
        return indicatorCode;
    }

    public void setIndicatorCode(String indicatorCode) {
        this.indicatorCode = indicatorCode;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }
}
