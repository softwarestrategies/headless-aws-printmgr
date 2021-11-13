package com.akatia.printmgr.data;

import lombok.Data;

import java.util.List;

@Data
public class PrinterInfo {

    @Data
    public static class Tray {
        private String name;
        private Integer id;
    }

    private String name;
    private List<Tray> trays;
}
