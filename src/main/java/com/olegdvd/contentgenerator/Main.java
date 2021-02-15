package com.olegdvd.contentgenerator;

import com.olegdvd.grabber.domain.WebClient;
import com.olegdvd.grabber.harvester.DanfossHarvester;
import com.olegdvd.grabber.harvester.SchneiderHarvester;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Objects;


public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String DANFOSS = "danfoss";
    private static final String SCHNEIDER = "schneider";
    private static String sheetName = "";
    private final ExcelFileUpdater fileUpdater;
    private final SourceFileStreamProvider fileProvider;

    private static String inputFileName;
    private static String outputFileName;

    private static void fileNameProvider(String fileName) {
        String out = "_out";
        String fileSeparator = ".";
        String extension = "xlsx";
        inputFileName = fileName + fileSeparator + extension;
        outputFileName = fileName + out + fileSeparator + extension;
    }


    private Main(ExcelFileUpdater fileUpdater, SourceFileStreamProvider fileProvider1) {
        this.fileUpdater = fileUpdater;
        this.fileProvider = fileProvider1;
    }

    public static void main(String[] args) throws Exception {
        Main main = null;
        String fileName = "";
        if (args.length > 1) {
            if (DANFOSS.equalsIgnoreCase(args[0])) {
                fileNameProvider("Danfoss_Price_Ukraine_03.02.2020_v01");
                main = new Main(new ExcelFileUpdater(new DanfossHarvester(new WebClient())), new SourceFileStreamProvider());
                sheetName = "Лист1";

            } else if (SCHNEIDER.equalsIgnoreCase(args[0])) {
                fileNameProvider("Schneider_Electric_out");
                main = new Main(new ExcelFileUpdater(new SchneiderHarvester(new WebClient())), new SourceFileStreamProvider());
                sheetName = "Прайс-лист";

            } else {
                //TODO react to wrong ARGUMENT
            }
        } else {
            //TODO react to no ARGUMENT
            throw new IllegalArgumentException("Please provide \"Danfoss\" or \"Schneider\" as first parameter, and sheet name as second parameter.");
        }


        //TODO react to NPE from FileProvider

        FileInputStream fIP = main.getFileProvider().getInputSourceStream(inputFileName);
        FileOutputStream fileOutputStream = main.getFileProvider().getOutputSourceStream(outputFileName);

        XSSFWorkbook wb = null;
        if (Objects.nonNull(fIP)) {
            wb = new XSSFWorkbook(fIP);
            main.getFileUpdater().updateExcelFile(wb, sheetName, fileOutputStream);

        } else {
            LOG.info(" either not exist or cannot open stream {}", fIP);
        }
    }

    private SourceFileStreamProvider getFileProvider() {
        return fileProvider;
    }

    private ExcelFileUpdater getFileUpdater() {
        return fileUpdater;
    }
}
