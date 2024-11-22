package org.example;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class MainTest {
    @Mock
    private RestHighLevelClient mockClient;

    @InjectMocks
    private Main main;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testUnzip() throws IOException {
        File tempFile = new File("unzipped/xml.xml");
        main.unzip(tempFile.getPath());
        File unzippedDir = new File(Main.UNZIP_FOLDER);
        assertTrue(unzippedDir.exists());
    }


}
