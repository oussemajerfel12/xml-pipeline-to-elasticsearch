package org.example;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

    private static void parseXMLFiles() throws Exception {
        File folder = new File(UNZIP_FOLDER);
        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".xml"));

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize();

                NodeList products = doc.getElementsByTagName("product");
                for (int i = 0; i < products.getLength(); i++) {
                    String sku = doc.getElementsByTagName("article_sku").item(i).getTextContent();
                    String brand = doc.getElementsByTagName("brand").item(i).getTextContent();
                    String description = doc.getElementsByTagName("full_description").item(i).getTextContent();

                    System.out.println("SKU: " + sku);
                    System.out.println("Brand: " + brand);
                    System.out.println("Description: " + description);
                }
            }
        }
    }

    public static RestClient createElasticsearchClient() {
        return RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
    }

    public static void main(String[] args) {
        try {
            unzip(ZIP_FILE_PATH);
            parseXMLFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}