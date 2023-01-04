package officebridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Excel {
	
	Workbook wb;
	String path;
	
	public Excel(String path) {
		this.path = path;
		try {
			wb = new XSSFWorkbook(new FileInputStream(new File(path)));
			System.out.println(wb + " get here");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public String getValueOfCell(int sheetnum, int rownum, int colnum) {
		System.out.println(sheetnum + " " + rownum + " " + colnum);
		System.out.println(wb);
		if (wb == null) {
			return "";
		}
		
		Sheet sheet = wb.getSheetAt(sheetnum);
		if (sheet == null) {
			return "";
		}
		Row row = sheet.getRow(rownum);
		if (row == null) {
			return "";
		}
		System.out.println(row);
		Cell cell = row.getCell(colnum);
		if (cell == null) {
			return "";
		}
		System.out.println(cell);
		//String str = cell.getStringCellValue();
		DataFormatter df = new DataFormatter();
		return df.formatCellValue(cell);
	}
	
	public void setValueOfCell(int sheetnum, int rownum, int colnum, String value) {
		if (wb == null) {
			return;
		}
		Sheet sheet = wb.getSheetAt(sheetnum);
		if (sheet == null) {
			return;
		}
		Row row = sheet.getRow(rownum);
		if (row == null) {
			row = sheet.createRow(rownum);
		}
		Cell cell = row.getCell(colnum);
		if (cell == null) {
			cell = row.createCell(colnum);
		}
		cell.setCellValue(value);
	}
	
	public void save() {
		try {
			FileOutputStream os = new FileOutputStream(path);
			wb.write(os);
			os.close();
			//wb.close();
			wb = new XSSFWorkbook(new FileInputStream(new File(path)));
			
			
		} catch (IOException e) {
			System.out.println("error " + e.getMessage());
		}
	}

	public String getPath() {
		return path;
	}

	public void saveAs(String fileName) {
		try {
			String path2 = path.substring(0, path.length() - 5) + " (1)" + path.substring(path.length() - 5, path.length());
			OutputStream os = new FileOutputStream(path2);
			wb.write(os);
			wb.close();
			Path p = Paths.get(new URI(path));
			Files.delete(p);
			Files.move(Paths.get(new URI(path2)), p, StandardCopyOption.REPLACE_EXISTING);
			Files.delete(Paths.get(new URI(path2)));
			
			try {
				File file = new File(path);
				wb = WorkbookFactory.create(file);
				System.out.println(wb + " get here");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public void closeStream() {
		save();
		try {
			wb.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
