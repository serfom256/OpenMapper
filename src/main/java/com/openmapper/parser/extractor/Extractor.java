package com.openmapper.parser.extractor;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import com.openmapper.parser.model.SQLProcedure;

public interface Extractor {
    List<Future<List<SQLProcedure>>> extract(final File file);
}
