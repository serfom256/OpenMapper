package com.openmapper.common.extractor;

import com.openmapper.common.entity.SQLProcedure;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

public interface Extractor {
    List<Future<List<SQLProcedure>>> extract(final File file);
}
