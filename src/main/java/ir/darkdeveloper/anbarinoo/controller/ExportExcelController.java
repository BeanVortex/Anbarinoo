package ir.darkdeveloper.anbarinoo.controller;

import ir.darkdeveloper.anbarinoo.dto.mapper.ProductMapper;
import ir.darkdeveloper.anbarinoo.model.ProductModel;
import ir.darkdeveloper.anbarinoo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Controller
@RequestMapping("/api/export/excel")
@RequiredArgsConstructor
public class ExportExcelController {


    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping(value = "/products")
    @PreAuthorize("hasAnyAuthority('OP_ACCESS_USER', 'OP_ACCESS_ADMIN')")
    public ResponseEntity<Resource> productsExcel(HttpServletRequest req) throws IOException {

        var workBook = new XSSFWorkbook();
        var sheet = workBook.createSheet("products");
        int rowNum = 0;

        var products = productService.getAllProducts(req);
        var row = sheet.createRow(rowNum++);
        createColumns(row, new String[]
                {"ردیف", "نام", "توضیحات", "قیمت", "تعداد", "مالیات", "تاریخ خرید", "تاریخ ویرایش"});
        for (var pr : products) {
            row = sheet.createRow(rowNum++);
            createList(pr, row, rowNum - 1);
        }

        var file = new File("ProductsReport.xlsx");
        var out = new FileOutputStream(file);
        workBook.write(out);
        out.close();

        var resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header("Content-disposition", "attachment; filename=" + file.getName())
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private void createList(ProductModel productModel, Row row, int rowNum) {

        var product = productMapper.productToDto(productModel);

        var cell = row.createCell(0);
        cell.setCellValue(rowNum);

        cell = row.createCell(1);
        cell.setCellValue(product.name());

        cell = row.createCell(2);
        cell.setCellValue(product.description());

        cell = row.createCell(3);
        cell.setCellValue(product.price().toString());

        cell = row.createCell(4);
        cell.setCellValue(product.totalCount().toString());

        cell = row.createCell(5);
        cell.setCellValue(product.tax().toString());

        cell = row.createCell(6);
        cell.setCellValue(product.createdAt());

        cell = row.createCell(7);
        cell.setCellValue(product.updatedAt());

    }

    private void createColumns(Row row, String[] columns) {
        Cell cell;
        for (int i = 0; i < columns.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(columns[i]);
        }
    }


}
