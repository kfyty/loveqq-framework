package com.kfyty.excel;

import com.kfyty.excel.processor.TemplateExcelParallelExport;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/7/3 18:19
 * @email kfyty725@hotmail.com
 */
public class ExportTest {

    @Test
    public void test() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TemplateExcelParallelExport export = new TemplateExcelParallelExport(out, ExportEntity.class);
        export.start();
        export.write(new ExportEntity(1L, "test"));
        export.end();
        export.close();
    }
}
