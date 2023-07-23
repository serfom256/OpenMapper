package com.openmapper.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtil {

    private FileUtil() {
    }

    public static String readFile(final File file) {
        if (file == null) {
            throw new IllegalStateException("File is null!!!");
        }
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

}
