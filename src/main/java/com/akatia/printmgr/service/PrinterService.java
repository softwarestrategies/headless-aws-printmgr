package com.akatia.printmgr.service;

import com.akatia.printmgr.data.PrintJobRequest;
import com.akatia.printmgr.data.PrinterInfo;
import com.amazonaws.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.Sides;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PrinterService {

    @Value("${default.printer.name}")
    private String defaultPrinterName;

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
        }
        catch (PrinterException pe) {
            log.error("PrinterException", pe);
            throw new RuntimeException("PrinterException", pe);
        }
        catch (IOException ioe) {
            log.error("Unable to process file", ioe);
            throw new RuntimeException("Unable to process file", ioe);
        }
    }

    private static PrintService findPrintService(String printerName) throws PrinterException{
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().trim().equals(printerName)) {
                return printService;
            }
        }
        throw new PrinterException("Unable to find printer");
    }

    public List<PrinterInfo> getPrinterInfo() {
        List<PrinterInfo> printerInfoList = new ArrayList<>();

        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        for (PrintService printService : printServices) {
            PrinterInfo printerInfo = new PrinterInfo();
            printerInfo.setName(printService.getName());

            // we store all the tray in a hashmap
            Map<Integer, Media> trayMap = new HashMap<Integer, Media>(10);

            // we chose something compatible with the printable interface
            DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;

            List<PrinterInfo.Tray> trayList = new ArrayList<>();

            Object o = printService.getSupportedAttributeValues(Media.class, flavor, null);
            if (o != null && o.getClass().isArray()) {
                for (Media media : (Media[]) o) {

                    // we collect the MediaTray available
                    if (media instanceof MediaTray) {
                        //log.info(media.getClass().getName());

                        MediaTray mediaTray = (MediaTray)media;
                        PrinterInfo.Tray printerInfoTray = new PrinterInfo.Tray();
                        printerInfoTray.setName(media.toString());
                        printerInfoTray.setId(mediaTray.getValue());

                        trayList.add(printerInfoTray);
                    }
                }
            }

            printerInfo.setTrays(trayList);

            printerInfoList.add(printerInfo);
        }

        return printerInfoList;
    }
}
