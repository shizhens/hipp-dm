package hiapp.modules.dmsetting.result;
/** 
 * @author yangwentian 
 * @version 创建时间：2017年9月12日 下午7:12:43 
 * 类说明 
 */
public class DMBizAutomaticColumns {
	private String worksheetId;
	private String worksheetName;
	private String worksheetNameCh;
	private String columnName;
	private String columnNameCh;
	public	String		fixedColumn;

	public String getWorksheetName() {
		return worksheetName;
	}
	public void setWorksheetName(String worksheetName) {
		this.worksheetName = worksheetName;
	}
	public String getWorksheetNameCh() {
		return worksheetNameCh;
	}
	public void setWorksheetNameCh(String worksheetNameCh) {
		this.worksheetNameCh = worksheetNameCh;
	}
	public String getColumnNameCh() {
		return columnNameCh;
	}
	public void setColumnNameCh(String columnNameCh) {
		this.columnNameCh = columnNameCh;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getWorksheetId() {
		return worksheetId;
	}
	public void setWorksheetId(String worksheetId) {
		this.worksheetId = worksheetId;
	}
	public String getFixedColumn() { return fixedColumn; }
	public void setFixedColumn(String fixedColumn) { this.fixedColumn = fixedColumn; }
}
