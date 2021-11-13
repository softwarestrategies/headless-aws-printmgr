package com.akatia.printmgr.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PrinterInfoResponse {

    private List<PrinterInfo> printers;
}
