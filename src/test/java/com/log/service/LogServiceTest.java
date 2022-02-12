package com.log.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class LogServiceTest {

    @InjectMocks
    LogService logService;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void testStart(){
        String path = "src/main/resources/logfiletest.txt";
        logService.start(path);
    }
}
