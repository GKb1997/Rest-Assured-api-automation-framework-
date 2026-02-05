package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class ResultExcelWriter {

    private static final String RESULT_FILE =
            "src/test/resources/testdata/api_execution_result.xlsx";

    // Shared resources (must be protected in parallel runs)
    private static final Workbook workbook = new XSSFWorkbook();
    private static final Sheet sheet = workbook.createSheet("Results");
    private static int rowNum = 0;

    // Static header creation (runs once)
    static {
        Row header = sheet.createRow(rowNum++);
        header.createCell(0).setCellValue("TestCaseID");
        header.createCell(1).setCellValue("Method");
        header.createCell(2).setCellValue("URL");
        header.createCell(3).setCellValue("ExpectedStatus");
        header.createCell(4).setCellValue("ActualStatus");
        header.createCell(5).setCellValue("Result");
    }

    // 🔒 THREAD-SAFE WRITE METHOD
    public static synchronized void write(
            String tcId,
            String method,
            String url,
            int expected,
            int actual,
            String result) throws IOException {

        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(tcId);
        row.createCell(1).setCellValue(method);
        row.createCell(2).setCellValue(url);
        row.createCell(3).setCellValue(expected);
        row.createCell(4).setCellValue(actual);
        row.createCell(5).setCellValue(result);

        try (FileOutputStream fos = new FileOutputStream(RESULT_FILE)) {
            workbook.write(fos);
        }
    }
}
