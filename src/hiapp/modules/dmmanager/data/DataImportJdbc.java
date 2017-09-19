package hiapp.modules.dmmanager.data;

import hiapp.modules.dmmanager.bean.Business;
import hiapp.modules.dmmanager.bean.ImportTemplate;
import hiapp.modules.dmmanager.bean.WorkSheet;
import hiapp.modules.dmmanager.bean.WorkSheetColumn;
import hiapp.utils.DbUtil;
import hiapp.utils.database.BaseRepository;
import hiapp.utils.idfactory.IdFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
@Repository
public class DataImportJdbc extends BaseRepository{
	@Autowired
	private IdFactory idfactory;
	/**
	 * 获取所有业务
	 * @param userId
	 * @return
	 * @throws IOException 
	 */
	public List<Business> getBusinessData(String userId) throws IOException{
		List<Business> businessList=new ArrayList<Business>();
		List<Integer> ornizeIdList=new ArrayList<Integer>();
		Connection conn=null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		
		try {
			conn= this.getDbConnection();
			String getOrgnizeSql="select b.businessId,b.name,b.DESCRIPTION,b.OWNERGROUPID,b.outboundmddeId,b.configJson from BU_MAP_USERORGROLE a,HASYS_DM_Business b  where a.GROUPID=b.OWNERGROUPID and a.USERID=?";
			pst=conn.prepareStatement(getOrgnizeSql);
			pst.setString(1,userId);
			rs = pst.executeQuery();
			while (rs.next()) {
				Business bus=new Business();
				bus.setId(rs.getInt(1));
				bus.setName(rs.getString(2));
				bus.setDescription(rs.getString(3));
				bus.setOwnergroupId(rs.getString(4));
				bus.setOutboundmddeId(rs.getInt(5));
				bus.setConfigJson(rs.getString(6));
				businessList.add(bus);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DbUtil.DbCloseQuery(rs,pst);
			DbUtil.DbCloseConnection(conn);
		}
		return businessList;
	}
	/**
	 * 获取所有导入模板
	 * @param businessId
	 * @return
	 */
	public List<ImportTemplate> getAllTemplates(Integer businessId){
		List<ImportTemplate> temList=new ArrayList<ImportTemplate>();
		Connection conn=null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			conn= this.getDbConnection();
			String sql="select id,TEMPLATEID,BUSINESSID,NAME,DESCRIPTION,ISDEFAULT,SOURCETYPE,XML from HASYS_DM_BIZTEMPLATEIMPORT where BUSINESSID=?";
			pst=conn.prepareStatement(sql);
			pst.setInt(1,businessId);
			//ִ�и���
			rs = pst.executeQuery();
			while (rs.next()) {
				ImportTemplate temPlate=new ImportTemplate();
				temPlate.setId(rs.getInt(1));
				temPlate.setTemPlateId(rs.getInt(2));
				temPlate.setBussinesID(rs.getInt(3));
				temPlate.setName(rs.getString(4));
				temPlate.setDescription(rs.getString(5));
				temPlate.setIsDefault(rs.getInt(6));
				temPlate.setSourceType(rs.getString(7));
				temPlate.setXml(rs.getString(8));
				temList.add(temPlate);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DbUtil.DbCloseQuery(rs,pst);
			DbUtil.DbCloseConnection(conn);
		}
		return temList;
	}
	/**
	 * 获取workSheetId
	 * @param bizId
	 * @return
	 */
	public String getWookSeetId(Integer bizId){
		Connection conn=null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String wookSheetId=null;
		try {
			conn= this.getDbConnection();
			String sql="select WORKSHEETID from Hasys_Dm_Bizworksheet where BIZID=?";
			pst=conn.prepareStatement(sql);
			pst.setInt(1, bizId);
			rs = pst.executeQuery();
		
			while(rs.next()){
				wookSheetId=rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DbUtil.DbCloseQuery(rs,pst);
			DbUtil.DbCloseConnection(conn);
		}
		return wookSheetId;
	}
	/**
	 * 根据WORKSHEETID获取表信息
	 * @param workSheetId
	 * @return
	 */
	public WorkSheet getWorkSheet(String workSheetId){
		WorkSheet workSheet=new WorkSheet();
		Connection conn=null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			conn= this.getDbConnection();
			String sql="SELECT id,name,namech,description,isowner FROM HASYS_WORKSHEET where id=?";
			pst=conn.prepareStatement(sql);
			pst.setString(1, workSheetId);
			rs = pst.executeQuery();
			while(rs.next()){
				workSheet.setId(rs.getString(1));
				workSheet.setName(rs.getString(2));
				workSheet.setNameCh(rs.getString(3));
				workSheet.setDescription(rs.getString(4));
				workSheet.setIsOwner(rs.getInt(5));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DbUtil.DbCloseQuery(rs,pst);
			DbUtil.DbCloseConnection(conn);
		}
		return workSheet;
	}
	/**
	 * 获取导入表字段
	 * @param workSheetId
	 * @return
	 */
	public List<WorkSheetColumn> getWorkSeetColumnList(String workSheetId){
		 List<WorkSheetColumn>  columnList=new ArrayList<WorkSheetColumn>();
		 Connection conn=null;
		 PreparedStatement pst = null;
		 ResultSet rs = null;
		 try {
			conn= this.getDbConnection();
			String sql="SELECT id,ColumnName,ColumnNameCh,ColumnDescription,DataType,Length,DictionaryName,DictionaryLevel,IsSysColumn,IsIdentitySquence,workSheetId FROM HASYS_WORKSHEETCOLUMN	 WHERE WorkSheetId=? ORDER BY ID";
			pst=conn.prepareStatement(sql);
			pst.setString(1, workSheetId);
			rs = pst.executeQuery();
			while(rs.next()){
				WorkSheetColumn sheetColumn=new WorkSheetColumn();
				sheetColumn.setId(rs.getInt(1));
				sheetColumn.setField(rs.getString(2));
				sheetColumn.setTitle(rs.getString(3));
				sheetColumn.setDescription(rs.getString(4));
				sheetColumn.setDataType(rs.getString(5));
				sheetColumn.setLength(rs.getInt(6));
				sheetColumn.setDicName(rs.getString(7));
				sheetColumn.setDicLevel(rs.getInt(8));
				sheetColumn.setIsSysColumn(rs.getInt(9));
				sheetColumn.setIsIdentitySquence(rs.getInt(10));
				sheetColumn.setWorkSheetId(rs.getString(11));
				columnList.add(sheetColumn);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DbUtil.DbCloseQuery(rs,pst);
			DbUtil.DbCloseConnection(conn);
		}
		 return columnList;
	}
	
	
	public Map<String,Object> getExcelData(Integer temPlateId,Integer bizId) throws IOException{
		Map<String,String> map1=new HashMap<String, String>();
		Map<String,Object> map=new HashMap<String, Object>();
		List<String> excelList=new ArrayList<String>();		
		Connection conn=null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String jsonData=null;
		try {
			conn= this.getDbConnection();
			String getXmlSql="select xml from HASYS_DM_BIZTEMPLATEIMPORT where TEMPLATEID=? and BUSINESSID=?";
			pst=conn.prepareStatement(getXmlSql);
			pst.setInt(1, temPlateId);
			pst.setInt(2,bizId);
			rs = pst.executeQuery();
			while(rs.next()){
				jsonData=ClobToString(rs.getClob(1));	
			}
			JsonObject jsonObject= new JsonParser().parse(jsonData).getAsJsonObject();
			JsonArray dataArray=jsonObject.get("FieldMaps").getAsJsonArray();
			for (int i = 0; i < dataArray.size(); i++) {
				String key=dataArray.get(i).getAsJsonObject().get("DbFieldName").getAsString();
				String value=dataArray.get(i).getAsJsonObject().get("ExcelHeader").getAsString();
				map1.put(key,value );
				excelList.add(value);
			}
			
			map.put("exMap", map1);
			map.put("exList",excelList);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	/**
	 * 获取要导入的数据
	 * @param temPlateId
	 * @param bizId
	 * @return
	 * @throws IOException
	 */
	public List<Map<String,Object>> getDbData(Integer temPlateId,Integer bizId) throws IOException{
		Connection conn=null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String jsonData=null;
		String getDbDataSql="select ";
		List<Map<String,Object>> dataList=new ArrayList<Map<String,Object>>();
		List<String> sourceColumns=new ArrayList<String>();
		try {
			conn= this.getDbConnection();
			String getXmlSql="select xml from HASYS_DM_BIZTEMPLATEIMPORT where TEMPLATEID=? and BUSINESSID=?";
			pst=conn.prepareStatement(getXmlSql);
			pst.setInt(1, temPlateId);
			pst.setInt(2,bizId);
			rs = pst.executeQuery();
			while(rs.next()){
				jsonData=ClobToString(rs.getClob(1));	
			}
			JsonObject jsonObject= new JsonParser().parse(jsonData).getAsJsonObject();
			JsonObject excelTemplate=jsonObject.get("ImportExcelTemplate").getAsJsonObject();
			JsonArray dataArray=jsonObject.get("FieldMaps").getAsJsonArray();
			String sourceTableName=excelTemplate.get("SourceTableName").getAsString();
			for (int i = 0; i < dataArray.size(); i++) {
				String key=dataArray.get(i).getAsJsonObject().get("FieldName").getAsString();
				String value=dataArray.get(i).getAsJsonObject().get("FieldNameSource").getAsString();
				sourceColumns.add(value);
			}
			List<String> newList=new ArrayList<String>(new HashSet(sourceColumns));
			for (int i = 0; i < newList.size(); i++) {
				getDbDataSql+=newList.get(i)+",";
			}
			getDbDataSql=getDbDataSql.substring(0, getDbDataSql.length()-1)+" from "+sourceTableName;
			pst=conn.prepareStatement(getDbDataSql);
			rs = pst.executeQuery();
			ResultSetMetaData md = rs.getMetaData();//获得结果集结构信息,元数据 
			
			while(rs.next()){
				Map<String,Object> map=new HashMap<String, Object>();
				for (int i = 0; i < dataArray.size(); i++) {
					String sourceName=dataArray.get(i).getAsJsonObject().get("FieldNameSource").getAsString();
					String key=dataArray.get(i).getAsJsonObject().get("FieldName").getAsString();
					for (int j= 0; j < newList.size(); j++) {
						if(sourceName.equals(newList.get(j))){
							map.put(key,rs.getObject(j+1));
						}
						
					}
				}
			
				
				dataList.add(map);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			DbUtil.DbCloseQuery(rs,pst);
			DbUtil.DbCloseConnection(conn);
		}
		return dataList;
	}
	/**
	 * 保存导入数据
	 * @param tempId
	 * @param bizId
	 * @param sheetColumnList
	 * @param isnertData
	 * @param tableName
	 * @param userId
	 * @throws IOException
	 */
	@SuppressWarnings({ "resource", "unused" })
	public Map<String,Object> insertImportData(Integer tempId,Integer bizId,String workSheetId,List<WorkSheetColumn> sheetColumnList,List<Map<String,Object>> isnertData,String tableName,String userId,String operationName) throws IOException{
		Connection conn=null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String jsonData=null;
		Statement statement=null;
		Map<String,Object> resultMap=null;
		List<String> stringList=new ArrayList<String>();
		try {
			conn= this.getDbConnection();
			String getXmlSql="select xml from HASYS_DM_BIZTEMPLATEIMPORT where TEMPLATEID=? and BUSINESSID=?";
			pst=conn.prepareStatement(getXmlSql);
			pst.setInt(1, tempId);
			pst.setInt(2,bizId);
			rs = pst.executeQuery();
			while(rs.next()){
				jsonData=ClobToString(rs.getClob(1));	
			}
			String importBatchId=idfactory.newId("DM_IID");//饶茹批次号
			String disBatchId=idfactory.newId("DM_DID");//分配号
			String getDataSourceSql="select id from HASYS_DM_DATAPOOL where DataPoolName ='数据源池'";
			pst=conn.prepareStatement(getDataSourceSql);
			rs=pst.executeQuery();
			Integer dataPoolNumber=null;
			while(rs.next()){
				dataPoolNumber=rs.getInt(1);
			}
			if("Excel导入".equals(operationName)){
				resultMap=insertExcelData(jsonData,workSheetId,tableName,isnertData,bizId,userId,importBatchId,dataPoolNumber,operationName,disBatchId);
			}else{
				resultMap=insertDbData(jsonData,workSheetId,tableName,isnertData,userId,importBatchId,dataPoolNumber,operationName,disBatchId);
			}
			//导入批次表里面插数据
			String insertImportBatchSql="insert into HASYS_DM_IID(id,iid,BusinessId,ImportTime,UserID,Name,Description,ImportType) values(SEQ_HASYS_DM_IID.nextval,?,?,sysdate,?,?,?,?)";
			pst=conn.prepareStatement(insertImportBatchSql);
			pst.setString(1, importBatchId);
			pst.setInt(2, bizId);
			pst.setString(3, userId);
			pst.setString(4, "导入批次");
			pst.setString(5, "导入批次");
			pst.setString(6, operationName);
			pst.executeUpdate();
			resultMap.put("result", true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultMap.put("result", false);
		}finally{
			DbUtil.DbCloseQuery(rs,pst);
			DbUtil.DbCloseConnection(conn);
		}
		return resultMap;
	}
	
    // CLOB转换成String
    public String ClobToString(Clob sc) throws SQLException, IOException {  
        String reString = "";  
        Reader is = sc.getCharacterStream();
        BufferedReader br = new BufferedReader(is);  
        String s = br.readLine();  
        StringBuffer sb = new StringBuffer();  
        while (s != null) {
            sb.append(s);  
            s = br.readLine();  
        }  
        reString = sb.toString();  
        return reString;  
    }  
    
    /**
     * 导入Excel数据
     * @param jsonData
     * @param workSheetId
     * @param tableName
     * @param isnertData
     * @param bizId
     * @param uId
     */
    public Map<String,Object> insertExcelData(String jsonData,String workSheetId,String tableName,List<Map<String,Object>> isnertData,Integer bizId,String userId,String importBatchId,Integer dataPoolNumber,String operationName,String disBatchId){
    	Connection conn=null;
		PreparedStatement pst = null;
		PreparedStatement pst1 = null;
		ResultSet rs = null;
		Statement statement=null;
		List<String> stringList=new ArrayList<String>();
		List<String> dataTypeList=new ArrayList<String>();
		List<String> columList=new ArrayList<String>();
		Map<String,Object> resultMap=new HashMap<String, Object>();//返回结果集 
		List<String> repeatColumns=new ArrayList<String>();//重复字段的集合
		try {
			conn=this.getDbConnection();
	    	//解析JSON RepetitionExcludeType
			JsonObject jsonObject= new JsonParser().parse(jsonData).getAsJsonObject();
			JsonObject excelTemplate=jsonObject.get("ImportExcelTemplate").getAsJsonObject();
			String repetitionExcludeType=excelTemplate.get("RepetitionExcludeType").getAsString();
			String RepetitionColumn=excelTemplate.get("RepetitionExcludeWorkSheetColumn").getAsString();
			String RepetitionColumnCh=excelTemplate.get("RepetitionExcludeWorkSheetColumnCh").getAsString();
		    Integer RepetitionCount=Integer.valueOf(excelTemplate.get("RepetitionExcludeDayCount").getAsString());
		    //排重字段
		    resultMap.put("column",RepetitionColumnCh);
			JsonArray dataArray=jsonObject.get("FieldMaps").getAsJsonArray();
			//获取导入表字段所属类型
			getDataType(dataTypeList,columList,dataArray,workSheetId,1);
			//获取排重字段类型
			String getTypeSql="select dataType from HASYS_WORKSHEETCOLUMN	where COLUMNNAME=? and  workSheetId=?";
			pst=conn.prepareStatement(getTypeSql);
			pst.setString(1, RepetitionColumn);
			pst.setString(2, workSheetId);
			rs=pst.executeQuery();
			String type=null;
			while(rs.next()){
				type=rs.getString(1);
			}
			String distinctSql=null;
			String resultTableName="HAU_DM_B"+bizId+"C_Result";
			//查询数据
			if("按导入时间排重".equals(repetitionExcludeType)){
				if(type.startsWith("datetime")){
					 distinctSql="select to_char("+RepetitionColumn+",'yyyy-mm-dd') from "+tableName+" where modifytime <sysdate and modifytime>sysdate-"+RepetitionCount;
				}else{
					 distinctSql="select "+RepetitionColumn+" from "+tableName+" where modifytime <sysdate and modifytime>sysdate-"+RepetitionCount;
				}
			}else{
				if(type.startsWith("datetime")){
					 distinctSql="select to_char(a."+RepetitionColumn+",'yyyy-mm-dd') from "+tableName+" a,"+resultTableName+" b where b.DialTime <sysdate and b.DialTime>sysdate-"+RepetitionCount;
				}else{
					 distinctSql="select a."+RepetitionColumn+" from "+tableName+" a,"+resultTableName+" b where b.DialTime <sysdate and b.DialTime>sysdate-"+RepetitionCount;
				}
			}
			
			pst=conn.prepareStatement(distinctSql);
			rs=pst.executeQuery();
			while(rs.next()){
				if(type.toLowerCase().startsWith("varchar")||type.toLowerCase().startsWith("datetime")){
					stringList.add(rs.getString(1));
				}else if(type.toLowerCase().startsWith("int")){
					stringList.add(String.valueOf(rs.getInt(1)));
				}
			}
			//向导入表插数据
			statement=conn.createStatement();
			for (int i = 0; i < isnertData.size(); i++) {
				String data=(String) isnertData.get(i).get(RepetitionColumn);
				Boolean ifRepeat=true;
				for(int h=0;h<stringList.size();h++){
					if(data.equals(stringList.get(h))){
						repeatColumns.add(data);
						ifRepeat=false;
						break;
					}
				}
				if(ifRepeat){
					String customerBatchId=idfactory.newId("DM_CID");//客户号
					String insertImportDataSql="insert into "+tableName+"(ID,IID,CID,modifylast,modifyid,modifyuserid,modifytime,";
					//数据池记录表里面插数据
					String isnertDataPoolSql="insert into HAU_DM_B1C_POOL(ID,DID,IID,CID,DataPoolIDLast,DataPoolIDCur,AreaLast,AreaCur,ISRecover,ModifyUserID,ModifyTime) "
											+" values(S_HAU_DM_B1C_POOL.nextval,?,?,?,?,?,?,?,?,?,sysdate)";
					pst=conn.prepareStatement(isnertDataPoolSql);
					pst.setString(1,disBatchId);
					pst.setString(2, importBatchId);
					pst.setString(3,customerBatchId);
					pst.setInt(4, dataPoolNumber);
					pst.setInt(5, dataPoolNumber);
					pst.setInt(6, 0);
					pst.setInt(7, 0);
					pst.setInt(8, 0);
					pst.setString(9, userId);
					//数据池操作记录表里面插数据
					String dataPoolOperationSql="insert into HAU_DM_B1C_POOL_ORE(ID,DID,IID,CID,OperationName,DataPoolIDLast,DataPoolIDCur,AreaLast,AreaCur,ISRecover,ModifyUserID,ModifyTime)"
												+" values(S_HAU_DM_B1C_POOL_ORE.nextval,?,?,?,?,?,?,?,?,?,?,sysdate)";
					pst1=conn.prepareStatement(dataPoolOperationSql);
					pst1.setString(1,disBatchId);
					pst1.setString(2, importBatchId);
					pst1.setString(3,customerBatchId);
					pst1.setString(4,operationName);
					pst1.setInt(5, dataPoolNumber);
					pst1.setInt(6, dataPoolNumber);
					pst1.setInt(7, 0);
					pst1.setInt(8, 0);
					pst1.setInt(9, 0);
					pst1.setString(10, userId);
					
					for (int k = 0; k < dataArray.size(); k++) {
						insertImportDataSql+=dataArray.get(k).getAsJsonObject().get("DbFieldName").getAsString()+",";
					}
					insertImportDataSql=insertImportDataSql.substring(0,insertImportDataSql.length()-1)+") values(S_HAU_DM_B101C_IMPORT.nextval,'"+importBatchId+"','"+customerBatchId+"',1,0,'"+userId+"',sysdate,";
					for (int j = 0; j < dataArray.size(); j++) {
						String cName=dataArray.get(j).getAsJsonObject().get("DbFieldName").getAsString();
						for (int k = 0; k < columList.size(); k++) {
							String cType =dataTypeList.get(k);
							if(cName.equals(columList.get(k))){
								if(cType.toLowerCase().startsWith("varchar")){
									insertImportDataSql+="'"+isnertData.get(i).get(cName)+"',";
								}else if(cType.toLowerCase().startsWith("int")){
									insertImportDataSql+=isnertData.get(i).get(cName)+",";
								}else if(cType.toLowerCase().startsWith("datetime")){
									insertImportDataSql+="to_date('"+isnertData.get(i).get(cName)+"','yyyy-mm-dd'),";
								}
							}
						}
					
					}
					insertImportDataSql=insertImportDataSql.substring(0,insertImportDataSql.length()-1)+")";
					statement.addBatch(insertImportDataSql);
					pst.executeUpdate();
					pst1.executeUpdate();
				}
				
				
			}
			resultMap.put("repeatColumn", repeatColumns);
			statement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return resultMap;
    }
    /**
     * 将数据库来源数据插入到导入表中
     */
    public Map<String,Object> insertDbData(String jsonData,String workSheetId,String tableName,List<Map<String,Object>> isnertData,String userId,String importBatchId,Integer dataPoolNumber,String operationName,String disBatchId){
    	Connection conn=null;
		Statement statement=null;
		PreparedStatement pst = null;
		PreparedStatement pst1 = null;
		ResultSet rs = null;
		List<String> dataTypeList=new ArrayList<String>();
		List<String> columList=new ArrayList<String>();
		Map<String,Object> resultMap=new HashMap<String, Object>();//返回结果集 
		try {
			conn=this.getDbConnection();
			JsonObject jsonObject= new JsonParser().parse(jsonData).getAsJsonObject();
			JsonArray dataArray=jsonObject.get("FieldMaps").getAsJsonArray();
			//获取导入表字段所属类型
			getDataType(dataTypeList,columList,dataArray,workSheetId,2);
			//向导入表插数据
			statement=conn.createStatement();
			for (int i = 0; i < isnertData.size(); i++) {
				String customerBatchId=idfactory.newId("DM_CID");//客户号
				String insertImportDataSql="insert into "+tableName+"(ID,IID,CID,modifylast,modifyid,modifyuserid,modifytime,";
				//数据池记录表里面插数据
				String isnertDataPoolSql="insert into HAU_DM_B1C_POOL(ID,DID,IID,CID,DataPoolIDLast,DataPoolIDCur,AreaLast,AreaCur,ISRecover,ModifyUserID,ModifyTime) "
										+" values(S_HAU_DM_B1C_POOL.nextval,?,?,?,?,?,?,?,?,?,sysdate)";
				pst=conn.prepareStatement(isnertDataPoolSql);
				pst.setString(1,disBatchId);
				pst.setString(2, importBatchId);
				pst.setString(3,customerBatchId);
				pst.setInt(4, dataPoolNumber);
				pst.setInt(5, dataPoolNumber);
				pst.setInt(6, 0);
				pst.setInt(7, 0);
				pst.setInt(8, 0);
				pst.setString(9, userId);
				//数据池操作记录表里面插数据
				String dataPoolOperationSql="insert into HAU_DM_B1C_POOL_ORE(ID,DID,IID,CID,OperationName,DataPoolIDLast,DataPoolIDCur,AreaLast,AreaCur,ISRecover,ModifyUserID,ModifyTime)"
											+" values(S_HAU_DM_B1C_POOL_ORE.nextval,?,?,?,?,?,?,?,?,?,?,sysdate)";
				pst1=conn.prepareStatement(dataPoolOperationSql);
				pst1.setString(1,disBatchId);
				pst1.setString(2, importBatchId);
				pst1.setString(3,customerBatchId);
				pst1.setString(4,operationName);
				pst1.setInt(5, dataPoolNumber);
				pst1.setInt(6, dataPoolNumber);
				pst1.setInt(7, 0);
				pst1.setInt(8, 0);
				pst1.setInt(9, 0);
				pst1.setString(10, userId);
				for (int k = 0; k < dataArray.size(); k++) {
					insertImportDataSql+=dataArray.get(k).getAsJsonObject().get("FieldName").getAsString()+",";
				}
				insertImportDataSql=insertImportDataSql.substring(0,insertImportDataSql.length()-1)+") values(S_HAU_DM_B101C_IMPORT.nextval,'"+importBatchId+"','"+customerBatchId+"',1,0,'"+userId+"',sysdate,";
				for (int j = 0; j < dataArray.size(); j++) {
					String cName=dataArray.get(j).getAsJsonObject().get("FieldName").getAsString();
					for (int k = 0; k < columList.size(); k++) {
						String type =dataTypeList.get(k);
						if(cName.equals(columList.get(k))){
							if(type.toLowerCase().startsWith("varchar")){
								insertImportDataSql+="'"+isnertData.get(i).get(cName)+"',";
							}else if(type.toLowerCase().startsWith("int")){
								insertImportDataSql+=isnertData.get(i).get(cName)+",";
							}else if(type.toLowerCase().startsWith("datetime")){
								insertImportDataSql+="to_date('"+isnertData.get(i).get(cName)+"','yyyy-mm-dd'),";
							}
						}
					}
				
				}
				insertImportDataSql=insertImportDataSql.substring(0,insertImportDataSql.length()-1)+")";
				statement.addBatch(insertImportDataSql);
				pst.executeUpdate();
				pst1.executeUpdate();
			}
			statement.executeBatch();
			resultMap.put("result", true);
			resultMap.put("repeatColumn",null);
			resultMap.put("column",null);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultMap.put("result", false);
		}
		return resultMap;
    }
    
    /**
     * 获取字段类型
     * @param dataTypeList
     * @param columList
     * @param dataArray
     * @param workSheetId
     * @param action
     */
    public void getDataType(List<String> dataTypeList,List<String> columList,JsonArray dataArray,String workSheetId,Integer action){
    	Connection conn=null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String actionName=null;
		try {
			conn=this.getDbConnection();
			//获取导入表字段所属类型和
			String getDataTypeSql="select datatype,columnname from hasys_worksheetcolumn  where workSheetId=? and columnname in(";
			if(action==1){
				actionName="DbFieldName";
			}else{
				actionName="FieldName";
			}
			for (int i = 0; i < dataArray.size(); i++) {
				String name=dataArray.get(i).getAsJsonObject().get(actionName).getAsString();
				getDataTypeSql+="'"+name+"',";
			}
			getDataTypeSql=getDataTypeSql.substring(0,getDataTypeSql.length()-1)+")";
			pst=conn.prepareStatement(getDataTypeSql);
			pst.setString(1, workSheetId);;
			rs=pst.executeQuery();
			while(rs.next()){
					dataTypeList.add(rs.getString(1));
					columList.add(rs.getString(2));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
}