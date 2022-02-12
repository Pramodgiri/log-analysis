package com.log.service;

import com.log.dao.EventDBDetails;
import com.log.dao.LogAppServer;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.log.util.FileUtil;

public class LogService {

    private static Logger logger = LoggerFactory.getLogger(LogService.class);
    ExecutorService executorService;
    public void start(String path) {
        logger.info("Reading file from : ",path);
        Map<String, List<LogAppServer>> logsmap = FileUtil.readLogFile(path);
        List<EventDBDetails> eventDBDetails = new ArrayList<>();
        calculateEvent(logsmap, eventDBDetails);
        if (eventDBDetails.size() > 0) {
            executorService = Executors.newFixedThreadPool(eventDBDetails.size()/2);
            executorService.execute(()-> {
                try {
                    save(eventDBDetails);
                } catch (SQLException ex) {
                    logger.error("ERROR while saving logs event: "+ ex.getMessage());
                }
            });
            executorService.shutdown();
        }
        logger.info("Log analysis done : ");
    }

    private void save(List<EventDBDetails> eventDBDetails) throws SQLException {
        Connection con = null;
        try {
            Class.forName("org.hsqldb.jdbcDriver" );
            con = DriverManager.getConnection("jdbc:hsqldb:file:log_analysis_db", "sa", "");
            Statement statement = con.createStatement();
            statement.executeUpdate("Create table if not exists app_log (id varchar(500),host varchar(500),type varchar(500),duration bigint,alert varchar(10))");
            statement.close();
            for (EventDBDetails event : eventDBDetails) {
                String insertEvent = "INSERT INTO APP_LOG(id,host,type,duration,alert) "
                        + "VALUES(?,?,?,?,?)";
                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = con.prepareStatement(insertEvent);
                    preparedStatement.setString(1,event.getId());
                    preparedStatement.setString(2,event.getHost());
                    preparedStatement.setString(3,event.getType());
                    preparedStatement.setString(4,event.getDuration().toString());
                    preparedStatement.setBoolean(5,event.isAlert());
                    preparedStatement.execute();
                    preparedStatement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        } catch (Exception ex) {
            logger.error("ERROR while saving logs event: "+ ex.getMessage());
        }
        finally {
            if(con != null){
                con.close();
            }
        }
    }

    private void calculateEvent(Map<String, List<LogAppServer>> logsmap, List<EventDBDetails> eventDBDetails) {
        logger.info("calculation Events Details : ");
        try {
            logsmap.keySet().stream().forEach(log -> {
                List<LogAppServer> logAppServers = logsmap.get(log);
                LogAppServer logInfo = logAppServers.get(0);
                EventDBDetails event = new EventDBDetails();
                BigInteger duration;
                if (logInfo.getState().equalsIgnoreCase("STARTED")) {
                    duration = logAppServers.get(1).getTimestamp().subtract(logInfo.getTimestamp());
                } else {
                    duration = logInfo.getTimestamp().subtract(logAppServers.get(1).getTimestamp());
                }
                event.setId(logInfo.getId());
                event.setDuration(duration);
                event.setAlert(duration.intValue() > 4 ? true : false);
                if (logInfo.getType() != null) {
                    event.setType(logInfo.getType());
                    event.setHost(logInfo.getHost());
                }
                eventDBDetails.add(event);
            });
            logger.info("calculation Events Details DONE: ");
        } catch (Exception ex) {
            logger.error("Failed while calculating Events : "+ex.getMessage());
        }
    }
}
