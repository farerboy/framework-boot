package com.farerboy.framework.boot.core.util;

import com.farerboy.framework.boot.util.excel.ExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 *
 * @author linjb
 */
public class ReportExcelUtil {
    /**
     * 功能: Excel导出公共方法
     * 记录条数大于50000时 导出.xlsx文件(excel07+)  小于等于50000时导出 .xls文件(excel97-03)
     */
    public void excelExport(List list, String title, Class className, Integer exportType, HttpServletResponse response, HttpServletRequest request) throws IOException {
        OutputStream out = response.getOutputStream();
        try {
            ExcelUtil excel = new ExcelUtil();
            if(list!=null && list.size()>ExcelUtil.EXPORT_07_LEAST_SIZE){
                dealBigNumber(list, title, className, exportType, response, request, out, excel);
            }else{
                HSSFWorkbook hss = new HSSFWorkbook();
                if(exportType==null){
                    hss = excel.exportExcel(list,title,className,0);
                }else{
                    hss = excel.exportExcel(list, title, className, exportType);
                }
                String disposition = "attachment;filename=";
                if(request!=null&&request.getHeader("USER-AGENT")!=null&& StringUtils.contains(request.getHeader("USER-AGENT"), "Firefox")){
                    disposition += new String((title+".xls").getBytes(),"ISO8859-1");
                }else{
                    disposition += URLEncoder.encode(title+".xls", "UTF-8");
                }

                response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                response.setHeader("Content-disposition", disposition);
                hss.write(out);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            out.close();
        }
    }

    private void dealBigNumber(List list, String title, Class className, Integer exportType,
                               HttpServletResponse response, HttpServletRequest request, OutputStream out, ExcelUtil excel)
            throws Exception{
        SXSSFWorkbook hss;
        if(exportType==null){
            hss = excel.exportExcel07S(list,title,className,0);
        }else{
            hss = excel.exportExcel07S(list, title, className, exportType);
        }

        String disposition = "attachment;filename=";
        if(request!=null && request.getHeader("USER-AGENT") != null && StringUtils.contains(request.getHeader("USER-AGENT"), "Firefox")){
            disposition += new String((title+".xlsx").getBytes(),"ISO8859-1");
        }else{
            disposition += URLEncoder.encode(title+".xlsx", "UTF-8");
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        response.setHeader("Content-disposition", disposition);
        hss.write(out);
    }
}
