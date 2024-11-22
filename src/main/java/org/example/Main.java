package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
    private static final String ZIP_FILE_PATH = "C:\\Users\\oussemajerfel\\Desktop\\java_program\\xml.zip";
    private static final String UNZIP_FOLDER = "unzipped";

    private static void unzip(String filePath) throws IOException {
        File destDir = new File(UNZIP_FOLDER);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(filePath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                File file = new File(destDir, entry.getName());
                if (!entry.isDirectory()) {
                    Files.copy(zipIn, file.toPath());
                }
                zipIn.closeEntry();
            }
        }
    }

    public static void main(String[] args) {
        try {
            unzip(ZIP_FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}