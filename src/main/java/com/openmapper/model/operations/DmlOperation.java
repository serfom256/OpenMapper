package com.openmapper.model.operations;

public enum DmlOperation {
    SELECT,//retrieve data from a database
    INSERT,//insert data into a table
    UPDATE, //updates existing data within a table
    DELETE, //Delete all records from a database table
    MERGE, //UPSERT operation (insert or update)
}
