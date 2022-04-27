package io.softwarestrategies.printmgr.service;

import io.softwarestrategies.printmgr.data.PrintJobRequest;
import com.amazonaws.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Sides;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.InputStream;
import java.net.URL;

@Service
@Slf4j
public class PrintService {

    @Value("${default.printer.name}")
    private String defaultPrinterName;

    @Async("threadPoolTaskExecutor")
    public void printDocument(PrintJobRequest printJobRequest) {
        try (InputStream inputStream = new URL(printJobRequest.getFileUrl()).openStream()) {
            PDDocument pdf = PDDocument.load(inputStream);

            PrinterJob printerJob = PrinterJob.getPrinterJob();

            String printerNameToUse = (StringUtils.isNullOrEmpty(printJobRequest.getPrinterName())
                    ? defaultPrinterName : printJobRequest.getPrinterName());

            printerJob.setPrintService(findPrintService(printerNameToUse));
            printerJob.setPageable(new PDFPageable(pdf));

            PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
            printRequestAttributeSet.add(Sides.ONE_SIDED);

            printerJob.print(printRequestAttributeSet);

            log.info("Print Job Succeeded: {}", printJobRequest.getDescription());
        }
        catch (Exception e) {
            if (StringUtils.isNullOrEmpty(printJobRequest.getDescription())) {
                log.error("Print Job Failed: {}", e.getMessage());
                log.debug("Exception: ", e);
            }
            else {
                log.error("Print Job Failed: {} -- {}", printJobRequest.getDescription(), e.getMessage());
                log.debug("Exception: ", e);
            }
        }
    }

    private static javax.print.PrintService findPrintService(String printerName) throws PrinterException{
        javax.print.PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (javax.print.PrintService printService : printServices) {
            if (printService.getName().trim().equals(printerName)) {
                return printService;
            }
        }
        throw new PrinterException("Unable to find printer");
    }
}
