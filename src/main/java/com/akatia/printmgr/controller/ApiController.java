package com.akatia.printmgr.controller;

import com.akatia.printmgr.data.PrinterInfoResponse;
import com.akatia.printmgr.service.PrinterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ApiController {

    private final PrinterService printerService;

    public ApiController(PrinterService printerService) {
        this.printerService = printerService;
    }

    @GetMapping("/printerinfo")
    public ResponseEntity<Object> getPrinterList() {
        try {
            PrinterInfoResponse printerInfoResponse = PrinterInfoResponse.builder()
                    .printers(printerService.getPrinterInfo())
                    .build();
            return new ResponseEntity<>(printerInfoResponse, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>("Unable to print: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
