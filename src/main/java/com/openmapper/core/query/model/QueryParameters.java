package com.openmapper.core.query.model;

import com.openmapper.parser.model.SQLRecord;

public class QueryParameters {
    String query;
    QuerySpecifications querySpecifications;
    SQLRecord source;

    public QueryParameters(String query, QuerySpecifications querySpecifications, SQLRecord source) {
        this.query = query;
        this.querySpecifications = querySpecifications;
        this.source = source;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public QuerySpecifications getQuerySpecifications() {
        return querySpecifications;
    }

    public void setQuerySpecifications(QuerySpecifications querySpecifications) {
        this.querySpecifications = querySpecifications;
    }

    public SQLRecord getSource() {
        return source;
    }

    public void setSource(SQLRecord source) {
        this.source = source;
    }
}
