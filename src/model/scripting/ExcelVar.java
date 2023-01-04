package model.scripting;

import officebridge.Excel;

public class ExcelVar extends Variable {

	public ExcelVar(String key) {
		super(key);
	}
	
	private Excel excel;
	
	public void setExcel(String path) {
		this.excel = new Excel(path);
	}
	
	public Excel getExcel() {
		return excel;
	}
	
	public String toString() {
		return excel.getPath();
	}

}
