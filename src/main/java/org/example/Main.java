package org.example;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.xcontent.XContentType;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


public class Main {
    public static final String ZIP_FILE_PATH = "C:\\Users\\oussemajerfel\\Desktop\\java_program\\xml.zip";
    public static final String UNZIP_FOLDER = "unzipped";


    public static void unzip(String filePath) throws IOException {
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

    private static RestHighLevelClient createElasticsearchClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(5000)
                        .setSocketTimeout(60000));
        return new RestHighLevelClient(builder);
    }

    private static void parseXMLFiles() throws Exception {
        File folder = new File(UNZIP_FOLDER);
        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".xml"));
        if (listOfFiles != null) {
            BulkRequest bulkRequest = new BulkRequest();
            try (RestHighLevelClient client = createElasticsearchClient()) {
                ensureIndexExists(client);
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

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("sku", sku);
                        jsonObject.put("brand", brand);
                        jsonObject.put("description", description);

                        IndexRequest request = new IndexRequest("products")
                                .id(sku)
                                .source(jsonObject.toString(), XContentType.JSON);

                        bulkRequest.add(request);

                        if (bulkRequest.numberOfActions() == 1000) {
                            indexToElasticsearch(client, bulkRequest);
                            bulkRequest = new BulkRequest();
                        }
                    }
                }
                if (bulkRequest.numberOfActions() > 0) {
                    indexToElasticsearch(client, bulkRequest);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void indexToElasticsearch(RestHighLevelClient client, BulkRequest bulkRequest) throws IOException {
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (bulkResponse.hasFailures()) {
            System.out.println("Bulk indexing failed!");
        } else {
            System.out.println("Bulk indexing successful!");
        }
    }

    private static void ensureIndexExists(RestHighLevelClient client) throws IOException {
        GetIndexRequest request = new GetIndexRequest("products");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        if (!exists) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest("products");
            client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            System.out.println("Index 'products' created.");
        } else {
            System.out.println("Index 'products' already exists.");
        }
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