package io.softwarestrategies.printmgr.service;

import io.softwarestrategies.printmgr.data.PrinterInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaTray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PrinterService {

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
