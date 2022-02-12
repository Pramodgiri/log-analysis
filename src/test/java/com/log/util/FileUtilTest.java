package com.log.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class FileUtilTest {

    @InjectMocks
    FileUtil fileUtil;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void testStart(){
        String path = "src/main/resources/logfiletest.txt";
        FileUtil.readLogFile(path);
    }
}
