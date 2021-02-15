package com.olegdvd.contentgenerator;

import com.olegdvd.grabber.domain.GatheredData;
import com.olegdvd.grabber.harvester.Harvester;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

public class ExcelFileUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(com.olegdvd.grabber.ExcelFileUpdater.class);
    //private final UrlContentGrabber grabber;
    private final Harvester grabber;

    ExcelFileUpdater(Harvester grabber) {
        this.grabber = grabber;
    }

    boolean updateExcelFile(XSSFWorkbook wb, String sheetName, OutputStream fOP) {

        Sheet sheet = wb.getSheet(sheetName);
        if (Objects.isNull(sheet)) {
            throw new IllegalArgumentException(String.format("Sheet with name %s not found in workbook %s", sheetName, wb.toString()));
        }

        int lastUsedRow = sheet.getLastRowNum();
        lastUsedRow =8460;
        int pos = 1;
        int dataColumn = this.grabber.getDataColumnNumber();
        int step = 10;
        long start = 0;
        long finish = 0;
        LOG.info("Available cores number: {}", Runtime.getRuntime().availableProcessors());
        while (pos <= lastUsedRow) {
            start = Instant.now().toEpochMilli();
            IntStream.range(pos, pos + step).parallel()
                    .boxed()
                    .forEach(rowNumber -> getArticleAndProcess(sheet, rowNumber, dataColumn));
            pos += step;
            finish = Instant.now().toEpochMilli();
            LOG.info("{} rows processed, {} steps takes {}sec", pos, step, (float) (finish - start) / 1000);
        }
        try {
            wb.write(fOP);
            LOG.info("File was updated successfully. {} cells processed. {}", pos, fOP.toString());
        } catch (IOException e) {
            LOG.warn("Failed to open/write to file {}", fOP);
            return false;
        }

        try {
            fOP.close();
        } catch (IOException e) {
            LOG.warn("Failed to close the file {}", fOP);
        }
        return true;
    }

    private void getArticleAndProcess(Sheet sheet, int idx, int dataColumn) {
        Row row = sheet.getRow(idx);
        String article;
        try {
            article = row.getCell(dataColumn, CREATE_NULL_AS_BLANK).getStringCellValue();
            GatheredData grabbedValue = grabber.request(article);
            fullfillCells(grabbedValue, row);
        } catch (Exception e) {
            LOG.warn("Empty article with row index: {}", idx);
        }

    }

    private void fullfillCells(GatheredData gatheredData, Row row) {
        gatheredData.gatheringTemplate().entrySet().stream()
                .filter(entry -> Objects.nonNull(entry.getKey()))
                .forEach(entry -> setCellsValue(row, entry.getValue(), gatheredData.columnIndexes().get(entry.getKey())));
    }

    private void setCellsValue(Row row, String value, Integer index) {
        row.getCell(index, CREATE_NULL_AS_BLANK).setCellValue(value);
    }
}
