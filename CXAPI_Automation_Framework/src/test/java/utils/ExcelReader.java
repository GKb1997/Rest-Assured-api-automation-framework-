    package utils;

    import org.apache.poi.ss.usermodel.*;
    import java.io.FileInputStream;
    import java.util.*;

    public class ExcelReader {

        public static List<Map<String, String>> readExcel(String sheetName) throws Exception {

            List<Map<String, String>> dataList = new ArrayList<>();

            FileInputStream fis =
                    new FileInputStream("src/test/resources/testdata/api_test_data.xlsx");

            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new RuntimeException("Sheet not found: " + sheetName);
            }

            Row headerRow = sheet.getRow(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row currentRow = sheet.getRow(i);
                if (currentRow == null) continue;

                Map<String, String> rowData = new HashMap<>();

                for (int j = 0; j < headerRow.getLastCellNum(); j++) {

                    Cell headerCell = headerRow.getCell(j);
                    Cell valueCell = currentRow.getCell(j);

                    String header = formatter.formatCellValue(headerCell);
                    String value = formatter.formatCellValue(valueCell);

                    rowData.put(header, value);
                }

                rowData.put("_SHEET_NAME", sheetName);
                dataList.add(rowData);
            }

            workbook.close();
            fis.close();

            return dataList;
        }
    }
