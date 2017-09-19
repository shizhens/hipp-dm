package hiapp.modules.dmsetting.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import hiapp.modules.dmsetting.result.*;
import hiapp.system.worksheet.bean.WorkSheet;
import hiapp.system.worksheet.bean.WorkSheetColumn;
import hiapp.system.worksheet.data.WorkSheetRepository;
import hiapp.utils.DbUtil;
import hiapp.utils.database.BaseRepository;
@Repository
public class DmBizAutomaticRepository extends BaseRepository {
	
	@Autowired
	 private WorkSheet workSheet;
	@Autowired
	private WorkSheetRepository workSheetRepository;
	//获取客户导入表列
	public List<DMBizAutomaticColumns> dmGetBizCustomerColumns(int bizId)
	{
		Connection dbConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<WorkSheetColumn> listColumns=new ArrayList<WorkSheetColumn>();
		List<DMBizAutomaticColumns> listDmBizAutomaticColums=new ArrayList<DMBizAutomaticColumns>();
		try {
			//根据表名查询中文名称接worksheetid
			dbConn =this.getDbConnection();
			String szSelectSql="select ID,NameCh from HASYS_WORKSHEET where NAME='HAU_DM_B"+bizId+"C_IMPORT'";
			stmt = dbConn.prepareStatement(szSelectSql);
			rs = stmt.executeQuery();
			String workSheetId="";
			String worksheetName="";
			while(rs.next()){
				workSheetId=rs.getString(1);
				worksheetName=rs.getString(2);
			}
			
			//根据worksheetid获取该工作表下所有列信息
			workSheet.getColumns(dbConn, workSheetId, listColumns);
			//将列值绑定到列表中
			for (int i = 0; i < listColumns.size(); i++) {
				WorkSheetColumn workSheetColumn=listColumns.get(i);
				//剔除掉不需要显示的列信息
				if (!workSheetColumn.getColumnName().equals("ID")&&!workSheetColumn.getColumnName().equals("MODIFYCLASS")&&
						!workSheetColumn.getColumnName().equals("MODIFYID")&&!workSheetColumn.getColumnName().equals("MODIFYUSERID")) {
					DMBizAutomaticColumns dmBizAutomaticColumns=new DMBizAutomaticColumns();
					dmBizAutomaticColumns.setWorksheetName("HAU_DM_B"+bizId+"C_IMPORT");
					dmBizAutomaticColumns.setWorksheetNameCh(worksheetName);
					dmBizAutomaticColumns.setColumnName(workSheetColumn.getColumnName());
					dmBizAutomaticColumns.setColumnNameCh(workSheetColumn.getColumnNameCh());
					listDmBizAutomaticColums.add(dmBizAutomaticColumns);
				}
				
			}
			
		
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.DbCloseConnection(dbConn);
			DbUtil.DbCloseExecute(stmt);
		}
		return listDmBizAutomaticColums;
	}
	//获取结果表列
	public boolean getResultColumns(
			List<DMBizAutomaticColumns> listDMBizAutomaticColumns, String bizId) {
		Connection dbConn = null;
		try {
			dbConn = this.getDbConnection();
			List<WorkSheetColumn> listColumns = new ArrayList<WorkSheetColumn>();
			//根据表名获取worksheetid
			String szWorkSheetName="HAU_DM_B"+bizId+"C_RESULT";
			String workSheetId = this.getWorksheetIdByName(szWorkSheetName);
			//根据worksheetid获取该表下所有列信息
			workSheet.getColumns(dbConn, workSheetId, listColumns);
			//将列绑定到列表中
			for (WorkSheetColumn workSheetColumn : listColumns) {
				//剔除掉不需要显示的列信息
				if (!workSheetColumn.getColumnName().equals("ID")&&!workSheetColumn.getColumnName().equals("MODIFYID")&&!workSheetColumn.getColumnName().equals("MODIFYLAST")) {
					DMBizAutomaticColumns dmBizAutomaticColumns = new DMBizAutomaticColumns();
					dmBizAutomaticColumns.setWorksheetName(szWorkSheetName);
					dmBizAutomaticColumns.setWorksheetNameCh(workSheetRepository.getWorkSheetNameCh(workSheetId));
					dmBizAutomaticColumns.setColumnName(workSheetColumn.getColumnName());
					dmBizAutomaticColumns.setColumnNameCh(workSheetColumn.getColumnNameCh());
					listDMBizAutomaticColumns.add(dmBizAutomaticColumns);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			DbUtil.DbCloseConnection(dbConn);
		}
		return true;
	}
	//根据结果名获取工作表id
	private String getWorksheetIdByName(String szWorkSheetName) {
		String workSheetId = "";
		Connection dbConn = null;
		String szSql = "";
		PreparedStatement stmt = null;	
		ResultSet rs = null;	
		try {
			dbConn = this.getDbConnection();
			szSql =String.format("select ID from HASYS_WORKSHEET where NAME='%s' ", szWorkSheetName) ;
			stmt = dbConn.prepareStatement(szSql);
			rs = stmt.executeQuery();
			if (rs.next()) {
				workSheetId=rs.getString(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			DbUtil.DbCloseConnection(dbConn);
			DbUtil.DbCloseQuery(rs, stmt);
		}
		return workSheetId;
	}
	
	
	//根据cid，iid获取客户信息
	public JsonObject dmGetBizCustomer(int bizId,String Cid,String IID,String columns)
	{
		Connection dbConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JsonObject jsonObject=new JsonObject();
		try {
			dbConn =this.getDbConnection();
			//查询客户信息
			String szSelectSql="select "+columns+" from HAU_DM_B"+bizId+"C_IMPORT where Cid='"+Cid+"' and IID='"+IID+"'";
			stmt = dbConn.prepareStatement(szSelectSql);
			rs = stmt.executeQuery();
			String[] column=columns.split(",");
			while(rs.next()){
				for(int i=0;i<column.length-1;i++)
				{
					jsonObject.addProperty(column[i], rs.getString(column[i]));
				}
			}
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.DbCloseConnection(dbConn);
			DbUtil.DbCloseExecute(stmt);
		}
		return jsonObject;
	}
	//根据cid查询前台所需信息
	public JsonObject dmGetBizCustomerHis(int bizId,String Cid,String columns)
	{
		Connection dbConn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		JsonObject jsonObject=new JsonObject();
		JsonArray jsonArray=new JsonArray();
		try {
			dbConn =this.getDbConnection();
			String szSelectSql="select "+columns+" from HAU_DM_B"+bizId+"C_IMPORT,HASYS_DM_B"+bizId+"C_PresetTime,HAU_DM_B"+bizId+"C_RESULT "
					+ " where HAU_DM_B"+bizId+"C_IMPORT.Cid=HAU_DM_B"+bizId+"C_RESULT.CID and HAU_DM_B"+bizId+"C_IMPORT.Cid=HASYS_DM_B"+bizId+"C_PresetTime.CID Cid='"+Cid+"'";
			stmt = dbConn.prepareStatement(szSelectSql);
			rs = stmt.executeQuery();
			String[] column=columns.split(",");
			while(rs.next()){
				JsonObject jsonObject_row=new JsonObject();
				for(int i=0;i<column.length-1;i++)
				{
					String[] workColumn=column[i].split(".");
					jsonObject_row.addProperty(column[i], rs.getString(workColumn[1]));
				}
				jsonArray.add(jsonObject_row);
			}
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.DbCloseConnection(dbConn);
			DbUtil.DbCloseExecute(stmt);
		}
		jsonObject.add("rows", jsonArray);
		return jsonObject;
	}
	
	
	public boolean getPresetColumns(List<DMBizAutomaticColumns> listDMBizAutomaticColumns, String bizId) {
		Connection dbConn = null;
		try {
			dbConn = this.getDbConnection();
			List<WorkSheetColumn> listColumns = new ArrayList<WorkSheetColumn>();
			//拼接工作表名称
			String szWorkSheetName="HASYS_DM_B"+bizId+"C_PRESETTIME";
			//根据预约表名获取工作表id
			String workSheetId = this.getWorksheetIdByName(szWorkSheetName);
			//根据工作表id获取该工作表下面所有的列信息
			workSheet.getColumns(dbConn, workSheetId, listColumns);
			for (WorkSheetColumn workSheetColumn : listColumns) {
				//剔除掉不需要显示的列信息
				if (!workSheetColumn.getColumnName().equals("ID")
						&&!workSheetColumn.getColumnName().equals("SOURCEID")
						&&!workSheetColumn.getColumnName().equals("IID")
						&&!workSheetColumn.getColumnName().equals("CID")
						&&!workSheetColumn.getColumnName().equals("MODIFYID")
						&&!workSheetColumn.getColumnName().equals("MODIFYLAST")
						&&!workSheetColumn.getColumnName().equals("MODIFYUSERID")
						&&!workSheetColumn.getColumnName().equals("MODIFYTIME")) {
					DMBizAutomaticColumns dmBizAutomaticColumns = new DMBizAutomaticColumns();
					dmBizAutomaticColumns.setWorksheetName(szWorkSheetName);
					dmBizAutomaticColumns.setWorksheetNameCh(workSheetRepository.getWorkSheetNameCh(workSheetId));
					dmBizAutomaticColumns.setColumnName(workSheetColumn.getColumnName());
					dmBizAutomaticColumns.setColumnNameCh(workSheetColumn.getColumnNameCh());
					listDMBizAutomaticColumns.add(dmBizAutomaticColumns);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			DbUtil.DbCloseConnection(dbConn);
		}
		return true;
	}
	//获取该业务下所有工作表列
	public List<DMBizAutomaticColumns> getAllBizColumns(String bizId) {
		//获取导入表列信息
		
		List<DMBizAutomaticColumns> listDMBizAutomaticColumns = this.dmGetBizCustomerColumns(Integer.parseInt(bizId));
		List<DMBizAutomaticColumns> listResultColumns = new ArrayList<DMBizAutomaticColumns>();
		//获取结果表列信息
		this.getResultColumns(listResultColumns,bizId);
		for (DMBizAutomaticColumns dmBizAutomaticColumns : listResultColumns) {
			if (!dmBizAutomaticColumns.getColumnName().equals("IID")&&
					!dmBizAutomaticColumns.getColumnName().equals("CID")&&
					!dmBizAutomaticColumns.getColumnName().equals("MODIFYTIME")) {
				listDMBizAutomaticColumns.add(dmBizAutomaticColumns);
			}
		}
		List<DMBizAutomaticColumns> listPresetColumns = new ArrayList<DMBizAutomaticColumns>();
		//获取预约表列信息
		this.getPresetColumns(listPresetColumns,bizId);
		listDMBizAutomaticColumns.addAll(listPresetColumns);
		return listDMBizAutomaticColumns;
	}
	
	/*//根据结果名获取工作表id
		private String getAutomaticPageUrl(int bizId) {
			 
			Connection dbConn = null;
			String url="";
			PreparedStatement stmt = null;	
			ResultSet rs = null;	
			try {
				dbConn = this.getDbConnection();
				String szSql =String.format("select ID from HASYS_WORKSHEET where NAME='%s' ", szWorkSheetName) ;
				stmt = dbConn.prepareStatement(szSql);
				rs = stmt.executeQuery();
				if (rs.next()) {
					url=rs.getString(1)
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				DbUtil.DbCloseConnection(dbConn);
				DbUtil.DbCloseQuery(rs, stmt);
			}
			return workSheetId;
		}*/
	
}
