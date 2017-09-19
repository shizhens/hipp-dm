package hiapp.modules.dmmanager.bean;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;


public class ExcelUtils {
	public  void exportExcel(List<String> excelHeader,List<Map<String,Object>> dataList,List<String> sheetCulomn,HttpServletRequest request,HttpServletResponse response){
		 HSSFWorkbook workbook = new HSSFWorkbook();                        // 创建工作簿对象
         HSSFSheet sheet = workbook.createSheet();                     // 创建工作表
         
         // 产生表格标题行
         HSSFRow rowm = sheet.createRow(0);
         HSSFCell cellTiltle = rowm.createCell(0);
         
         //sheet样式定义【getColumnTopStyle()/getStyle()均为自定义方法 - 在下面  - 可扩展】
         HSSFCellStyle columnTopStyle = this.getColumnTopStyle(workbook);//获取列头样式对象
         HSSFCellStyle style = this.getStyle(workbook);                    //单元格样式对象
         
         sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, (excelHeader.size()-1)));  
         cellTiltle.setCellStyle(columnTopStyle);
         cellTiltle.setCellValue("导出数据表");
         
         // 定义所需列数
         int columnNum = excelHeader.size();
         HSSFRow rowRowName = sheet.createRow(2);                // 在索引2的位置创建行(最顶端的行开始的第二行)
          
         // 将列头设置到sheet的单元格中
         for(int n=0;n<columnNum;n++){
             HSSFCell  cellRowName = rowRowName.createCell(n);                //创建列头对应个数的单元格
             cellRowName.setCellType(HSSFCell.CELL_TYPE_STRING);                //设置列头单元格的数据类型
             HSSFRichTextString text = new HSSFRichTextString(excelHeader.get(n));
             cellRowName.setCellValue(text);                                    //设置列头单元格的值
             cellRowName.setCellStyle(columnTopStyle);                        //设置列头单元格样式
         }
         //将查询出的数据设置到sheet对应的单元格中
         for (int i = 0; i < dataList.size(); i++) {
        	 HSSFRow row = sheet.createRow(i+3);//创建所需的行数
        	 for (int j = 0; j < sheetCulomn.size(); j++) {
        		 HSSFCell  cell = null;   //设置单元格的数据类型
        		 cell=row.createCell(j);
        		 cell.setCellValue(dataList.get(i).get(sheetCulomn.get(j)).toString());
        		 cell.setCellStyle(style);   
			}
        	 
		}
         
       //让列宽随着导出的列长自动适应
         for (int colNum = 0; colNum < columnNum; colNum++) {
             int columnWidth = sheet.getColumnWidth(colNum) / 256;
             for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
                 HSSFRow currentRow;
                 //当前行未被使用过
                 if (sheet.getRow(rowNum) == null) {
                     currentRow = sheet.createRow(rowNum);
                 } else {
                     currentRow = sheet.getRow(rowNum);
                 }
                 if (currentRow.getCell(colNum) != null) {
                     HSSFCell currentCell = currentRow.getCell(colNum);
                     if (currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                         int length = currentCell.getStringCellValue().getBytes().length;
                         if (columnWidth < length) {
                             columnWidth = length;
                         }
                     }
                 }
             }
             if(colNum == 0){
                 sheet.setColumnWidth(colNum, (columnWidth-2) * 256);
             }else{
                 sheet.setColumnWidth(colNum, (columnWidth+4) * 256);
             }
         }
         
       
          
		try {
			 String fileName = "Excel-" + String.valueOf(System.currentTimeMillis()).substring(4, 13) + ".xls";
	         String headStr = "attachment; filename=\"" + fileName + "\"";
	         //FileOutputStream out = new FileOutputStream("E://学生表.xls");
	         OutputStream out = response.getOutputStream();
	        response.setContentType("application/force-download");// 设置强制下载不打开
	         response.setContentType("APPLICATION/OCTET-STREAM");
	         response.setHeader("Content-Disposition", headStr);
			 workbook.write(out);
			 out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
         
	}
	
	
	 /*  
     * 列数据信息单元格样式
     */  
      public HSSFCellStyle getStyle(HSSFWorkbook workbook) {
            // 设置字体
            HSSFFont font = workbook.createFont();
            //设置字体大小
            //font.setFontHeightInPoints((short)10);
            //字体加粗
            //font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            //设置字体名字 
            font.setFontName("Courier New");
            //设置样式; 
            HSSFCellStyle style = workbook.createCellStyle();
            //设置底边框; 
            style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            //设置底边框颜色;  
            style.setBottomBorderColor(HSSFColor.BLACK.index);
            //设置左边框;   
            style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            //设置左边框颜色; 
            style.setLeftBorderColor(HSSFColor.BLACK.index);
            //设置右边框; 
            style.setBorderRight(HSSFCellStyle.BORDER_THIN);
            //设置右边框颜色; 
            style.setRightBorderColor(HSSFColor.BLACK.index);
            //设置顶边框; 
            style.setBorderTop(HSSFCellStyle.BORDER_THIN);
            //设置顶边框颜色;  
            style.setTopBorderColor(HSSFColor.BLACK.index);
            //在样式用应用设置的字体;  
            style.setFont(font);
            //设置自动换行; 
            style.setWrapText(false);
            //设置水平对齐的样式为居中对齐;  
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            //设置垂直对齐的样式为居中对齐; 
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
           
            return style;
      
      }
      
      /* 
       * 列头单元格样式
       */    
        public HSSFCellStyle getColumnTopStyle(HSSFWorkbook workbook) {
            
              // 设置字体
            HSSFFont font = workbook.createFont();
            //设置字体大小
            font.setFontHeightInPoints((short)11);
            //字体加粗
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            //设置字体名字 
            font.setFontName("Courier New");
            //设置样式; 
            HSSFCellStyle style = workbook.createCellStyle();
            //设置底边框; 
            style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            //设置底边框颜色;  
            style.setBottomBorderColor(HSSFColor.BLACK.index);
            //设置左边框;   
            style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            //设置左边框颜色; 
            style.setLeftBorderColor(HSSFColor.BLACK.index);
            //设置右边框; 
            style.setBorderRight(HSSFCellStyle.BORDER_THIN);
            //设置右边框颜色; 
            style.setRightBorderColor(HSSFColor.BLACK.index);
            //设置顶边框; 
            style.setBorderTop(HSSFCellStyle.BORDER_THIN);
            //设置顶边框颜色;  
            style.setTopBorderColor(HSSFColor.BLACK.index);
            //在样式用应用设置的字体;  
            style.setFont(font);
            //设置自动换行; 
            style.setWrapText(false);
            //设置水平对齐的样式为居中对齐;  
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            //设置垂直对齐的样式为居中对齐; 
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            
            return style;
            
        }
}