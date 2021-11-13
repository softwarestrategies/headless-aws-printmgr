package com.akatia.printmgr;

import com.akatia.printmgr.data.PrinterInfo;
import com.akatia.printmgr.service.PrinterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

import static java.lang.System.exit;

@SpringBootApplication
@Slf4j
public class PrintMgrApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(PrintMgrApplication.class, args);

            PrinterService printerService = new PrinterService();
            List<PrinterInfo> printerInfoList = printerService.getPrinterInfo();

            log.info(" ");
            log.info("Available Printers to Use:");

            for (PrinterInfo printerInfo : printerInfoList) {
                log.info(printerInfo.getName());
            }

            log.info(" ");
            log.info("Ready ....");
            log.info(" ");
        }
        catch (Exception e) {
            log.error("Unable to start application: " + e.getMessage());
            exit(0);
        }
    }
}
