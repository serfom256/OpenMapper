package io.github.serfom256.core.query.model;

import io.github.serfom256.parser.model.SQLRecord;

public class QueryParameters {
    BoundSql boundSql;
    QuerySpecifications querySpecifications;
    SQLRecord source;

    public QueryParameters(BoundSql boundSql, QuerySpecifications querySpecifications, SQLRecord source) {
        this.boundSql = boundSql;
        this.querySpecifications = querySpecifications;
        this.source = source;
    }

    public BoundSql getBoundSql() {
        return boundSql;
    }

    public void setBoundSql(BoundSql boundSql) {
        this.boundSql = boundSql;
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
