package com.log.main;

import com.log.service.LogService;
public class LogAnalysis {
    public static void main(String... args) {
        try {
            if(args.length>0) {
                LogService logService = new LogService();
                logService.start(args[0]);
            }
            else {
                throw new IllegalArgumentException("Please pass input file name");
            }
        } catch (Exception ex) {
            System.out.println("Error"+ex.getMessage());
        }
    }
}
