package ir.darkdeveloper.anbarinoo.controller;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Controller
@RequestMapping("/api/export/excel")
public class ExportExcelController {

    // under development

    @GetMapping("/products")
    @PreAuthorize("authentication.name.equals('anonymousUser')")
    public ResponseEntity<Resource> productsExcel(HttpServletRequest req) throws IOException {

        var workbook = new XSSFWorkbook();

        var sheet = workbook.createSheet("Persons");
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4000);

        var header = sheet.createRow(0);

        var headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        var font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        headerStyle.setFont(font);

        var headerCell = header.createCell(0);
        headerCell.setCellValue("Name");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(1);
        headerCell.setCellValue("Age");
        headerCell.setCellStyle(headerStyle);

        var style = workbook.createCellStyle();
        style.setWrapText(true);

        var row = sheet.createRow(2);
        var cell = row.createCell(0);
        cell.setCellValue("John Smith");
        cell.setCellStyle(style);

        cell = row.createCell(1);
        cell.setCellValue(20);
        cell.setCellStyle(style);

        var currDir = new File("./temp.xlsx");
        if (currDir.exists())
            currDir.createNewFile();
        var path = currDir.getAbsolutePath();
        var outputStream = new FileOutputStream(path);
        workbook.write(outputStream);
        workbook.close();

        var file = new File(path);

        var resource = new InputStreamResource(new FileInputStream(file));


        return ResponseEntity.ok()
                .header("Content-disposition", "attachment; filename=" + file.getName())
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


}
