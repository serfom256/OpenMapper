package io.github.serfom256.model.operations;

public enum DmlOperation {
    SELECT,//retrieve data from a database
    INSERT,//insert data into a table
    UPDATE, //updates existing data within a table
    DELETE, //Delete all records from a database table
    MERGE, //UPSERT operation (insert or update)
    ;

    /**
     * Whether this operation advances an
     * {@link io.github.serfom256.annotations.entity.OptimisticLockField}. Statements
     * that may overwrite an existing row do; a plain insert or delete does not.
     */
    public boolean advancesVersion() {
        return this == UPDATE || this == MERGE;
    }
}
