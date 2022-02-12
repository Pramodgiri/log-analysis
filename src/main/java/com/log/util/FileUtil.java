package com.log.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.log.dao.LogAppServer;
import com.log.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);
    public static Map<String,List<LogAppServer>> readLogFile(String path) {
        logger.info("Parsing file starts");
        Path logFile = Paths.get(path);
        Map<String,List<LogAppServer>> logsMap = new HashMap<>();
        try (Stream<String> lines = Files.lines(logFile)) {
            lines.forEach(line -> {
                try {
                    LogAppServer logAppServer = new ObjectMapper().readValue(line, LogAppServer.class);
                    if(logsMap.containsKey(logAppServer.getId())) {
                        List<LogAppServer> logAppServers = logsMap.get(logAppServer.getId());
                        logAppServers.add(logAppServer);
                        logsMap.put(logAppServer.getId(),logAppServers);
                    }
                    else {
                        List<LogAppServer> logAppServers = new ArrayList<>();
                        logAppServers.add(logAppServer);
                        logsMap.put(logAppServer.getId(),logAppServers);
                    }
                } catch (JsonProcessingException ex) {
                    logger.error("Error while parsing file: "+ex.getMessage());

                }
            });
            logger.info("Parsing file end");
        } catch (IOException ex) {
            logger.error("Error while reading file from disk: "+ex.getMessage());
        }
        return logsMap;
    }
}
