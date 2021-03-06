package hiapp.modules.dmmanager.data;

import hiapp.modules.dm.singlenumbermode.SingleNumberOutboundDataManage;
import hiapp.modules.dm.singlenumbermode.bo.SingleNumberModeShareCustomerStateEnum;
import hiapp.modules.dmmanager.OperationNameEnum;
import hiapp.system.buinfo.User;
import hiapp.utils.DbUtil;
import hiapp.utils.database.BaseRepository;
import hiapp.utils.idfactory.IdFactory;
import hiapp.utils.serviceresult.ServiceResultCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;














import org.springframework.beans.factory.annotation.Autowired;
/*import net.sf.json.JSONArray;
import net.sf.json.JSONObject;*/
import org.springframework.stereotype.Repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//数据共享db
@Repository
public class DMBizDataShare extends BaseRepository {
	@Autowired
	private IdFactory idFactiory;
	@Autowired
	private SingleNumberOutboundDataManage singleNumberOutboundDataManage;
	//根据时间筛选导入批次号查询出没有被共享的客户批次数据
	@SuppressWarnings("resource")
	public Map<String,Object>  getNotShareDataByTimes(
			String StartTime,String EndTime,String businessId, String templateId,String sourceType,Integer num,Integer pageSize) {
		String getXmlSql = "";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection dbConn = null;
		String jsonData=null;
		JsonArray dataArray=null;
		String flag=null;
		Integer bizid = Integer.valueOf(businessId);
		Integer temp  =Integer.valueOf(templateId);
		Integer startNum=(num-1)*pageSize+1;
		Integer endNum=num*pageSize+1;
		Map<String,Object> resultMap=new HashMap<String, Object>();
		String workSheetImport="HAU_DM_B"+businessId+"C_IMPORT";
		String workSheetPool="HAU_DM_B"+businessId+"C_POOL";
		if(sourceType.equals("Excel")){
			flag="DbFieldName";
		}else{
			flag="FieldName";
		}
		List<Map<String,Object>> dataList=new ArrayList<Map<String,Object>>();
		try {
			dbConn = this.getDbConnection();
			getXmlSql="SELECT XML FROM HASYS_DM_BIZTEMPLATEIMPORT WHERE TEMPLATEID=? AND BUSINESSID=?";
			stmt=dbConn.prepareStatement(getXmlSql);
			stmt.setInt(1,temp);
			stmt.setInt(2,bizid);
			rs = stmt.executeQuery();
			while(rs.next()){
				//获取xml里面的数据
				jsonData=ClobToString(rs.getClob(1));	
			}
			//解析 通过查询获取xml，并把存储的json串解成json对象
			JsonObject jsonObject= new JsonParser().parse(jsonData).getAsJsonObject(); 
			//从对象中获取列名数组json集合
			dataArray=jsonObject.get("FieldMaps").getAsJsonArray();
			String sql="select iid,";
			String sql1="select iid,";
			for (int i = 0; i < dataArray.size(); i++) {
				String column=dataArray.get(i).getAsJsonObject().get(flag).getAsString();
				if(!"iid".equals(column.toLowerCase())){
					sql+=dataArray.get(i).getAsJsonObject().get(flag).getAsString()+",";
					sql1+=dataArray.get(i).getAsJsonObject().get(flag).getAsString()+",";
				}
				
			}
			sql=sql.substring(0,sql.length()-1);
			sql=sql+" from ("+sql1+"rownum rn"+" from "+workSheetImport+" where CID IN (select  b.CID from HASYS_DM_IID a,"+workSheetPool+" b where a.IID=b.IID AND b.AREACUR=0 AND a.BUSINESSID=? AND  a.IMPORTTIME >to_date('"+StartTime+"','yyyy-MM-dd hh24:mi:ss') and a.IMPORTTIME <to_date('"+EndTime+"','yyyy-MM-dd hh24:mi:ss')) and rownum<?) a where rn>=?";
			stmt=dbConn.prepareStatement(sql);
			stmt.setInt(1,bizid);
			stmt.setInt(2,endNum);
			stmt.setInt(3,startNum);
			rs = stmt.executeQuery();
			while(rs.next()){
				Map<String,Object> map=new HashMap<String, Object>();
				map.put("IID",rs.getString(1));
				//列名不确定 需要循环 录入到 map集合中
				for (int i = 0; i < dataArray.size(); i++) {
					// 将循环出来的列名作为key
					String key=dataArray.get(i).getAsJsonObject().get(flag).getAsString();
					map.put(key,rs.getObject(key));
				}
				dataList.add(map);
			}
			String countSql="select count(*) from "+workSheetImport+" where CID IN (select  b.CID from HASYS_DM_IID a,"+workSheetPool+" b where a.IID=b.IID AND b.AREACUR=0 AND a.BUSINESSID=? AND  a.IMPORTTIME >to_date('"+StartTime+"','yyyy-MM-dd hh24:mi:ss') and a.IMPORTTIME <to_date('"+EndTime+"','yyyy-MM-dd hh24:mi:ss'))";
			stmt=dbConn.prepareStatement(countSql);
			stmt.setInt(1,bizid);
			rs=stmt.executeQuery();
			Integer total=null;
			while(rs.next()){
				total=rs.getInt(1);
			}
			resultMap.put("total", total);
			resultMap.put("rows",dataList);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			DbUtil.DbCloseQuery(rs,stmt);
			DbUtil.DbCloseConnection(dbConn);
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
	//创建共享和追加共享
	@SuppressWarnings("resource")
	public ServiceResultCode addConfirmShareData(int businessId,String[] importIds,User user, String newShareId,String[] customerIds,String shareName,String description) throws Exception{
		String insertsql = "";
		String updatesql="";
		String sqlPool="";
		PreparedStatement stmt = null;
		Connection dbConn = null;
		ResultSet rs = null;
		
		//数据池id
		int poolID=0;
		//单号码重拨共享表
		String HASYS_DM_BC_DATAM3="HAU_DM_B"+businessId+"C_DATAM3";
		//单号码重拨共享历史表
		String HAU_DM_BC_DATAM3_HIS= "HAU_DM_B"+businessId+"C_DATAM3_HIS";
		//数据池记录表
		String HAU_DM_BC_POOL="HAU_DM_B"+businessId+"C_POOL";
		//数据池操作记录表
		String HAU_DM_BC_POOL_ORE="HAU_DM_B"+businessId+"C_POOL_ORE";
		try {
				dbConn = this.getDbConnection();
				//查找数据池id ok
				sqlPool =String.format("SELECT ID FROM HASYS_DM_DATAPOOL WHERE BUSINESSID=%s AND DATAPOOLTYPE=1",businessId); //更改
				stmt = dbConn.prepareStatement(sqlPool);
				rs = stmt.executeQuery();
				while(rs.next()){
				poolID = rs.getInt(1);
				}
				for (int i = 0; i < customerIds.length; i++) {
					String importId = importIds[i];
					String customerId=customerIds[i];
					//不自动提交数据
					dbConn.setAutoCommit(false);
					//单号码重拨共享表
					insertsql =String.format("INSERT INTO "+HASYS_DM_BC_DATAM3+" (ID,BUSINESSID,SHAREID,IID,CID,STATE,MODIFYID,MODIFYUSERID,MODIFYTIME) VALUES(S_HASYS_DM_B1C_DATAM3.NEXTVAL,%s,'%s','%s','%s','%s',%s,'%s',sysdate)",businessId,newShareId,importId,customerId,SingleNumberModeShareCustomerStateEnum.CREATED,0,user.getId()) ;
					stmt = dbConn.prepareStatement(insertsql);
					stmt.execute();
				    //单号码重拨共享历史表
					insertsql =String.format("INSERT INTO "+HAU_DM_BC_DATAM3_HIS+" (ID,BUSINESSID,SHAREID,IID,CID,STATE,MODIFYID,MODIFYUSERID,MODIFYTIME) VALUES(S_HASYS_DM_B1C_DATAM3_HIS.NEXTVAL,%s,'%s','%s','%s','%s',%s,'%s',sysdate)",businessId,newShareId,importId,customerId,SingleNumberModeShareCustomerStateEnum.CREATED,0,user.getId());
					stmt = dbConn.prepareStatement(insertsql);
					stmt.execute();
				    //更改数据池记录表数据
					updatesql=String.format("UPDATE "+HAU_DM_BC_POOL+" SET SOURCEID='%s',CID='%s',DATAPOOLIDLAST=%s,DATAPOOLIDCUR=%s,AREALAST=%s,AREACUR=%s,MODIFYTIME=sysdate WHERE IID='%s' AND CID='%s'",newShareId,customerId,poolID,poolID,0,1,importId,customerId);
		        	stmt = dbConn.prepareStatement(updatesql);
					stmt.execute();
					//数据池操作记录表
					insertsql=String.format("INSERT INTO "+HAU_DM_BC_POOL_ORE+" (ID,SOURCEID,IID,CID,OPERATIONNAME,DATAPOOLIDLAST,DATAPOOLIDCUR,AREALAST,AREACUR,ISRECOVER,MODIFYUSERID,MODIFYTIME) VALUES (S_HAU_DM_B1C_POOL_ORE.NEXTVAL,'%s','%s','%s','%s',%s,%s,%s,%s,%s,'%s',sysdate)",newShareId,importId,customerId,OperationNameEnum.Sharing,poolID,poolID,0,1,0,user.getId());
					stmt = dbConn.prepareStatement(insertsql);
					stmt.execute();
				}
				insertsql = String.format("INSERT INTO HASYS_DM_SID (ID,BUSINESSID,SHAREID,SHARENAME,CREATEUSERID,CREATETIME,DESCRIPTION) VALUES(S_HASYS_DM_SID.NEXTVAL,%s,'%s','%s','%s',sysdate,'%s')",businessId,newShareId,shareName,user.getId(),description);
				stmt = dbConn.prepareStatement(insertsql);
				stmt.execute();
				//无异常提交代码
				dbConn.commit();
		} catch (Exception e) {
			//有异常回滚
			dbConn.rollback();
			e.printStackTrace();
			return ServiceResultCode.INVALID_SESSION;
		}finally{
			DbUtil.DbCloseQuery(rs,stmt);
			DbUtil.DbCloseConnection(dbConn);
		}
		return ServiceResultCode.SUCCESS;
	}
	
	
	public  ServiceResultCode  appendShareData(int businessId,String[] importIds,User user, String newShareId,String[] customerIds,String appendName,String description){
		PreparedStatement pst = null;
		Connection conn = null;
		ResultSet rs = null;
		//单号码重拨共享表
		String HAU_DM_BC_DATAM3="HAU_DM_B"+businessId+"C_DATAM3";
		//单号码重拨共享历史表
		String HAU_DM_BC_DATAM3_HIS= "HAU_DM_B"+businessId+"C_DATAM3_HIS";
		//数据池记录表
		String HAU_DM_BC_POOL="HAU_DM_B"+businessId+"C_POOL";
		//数据池操作记录表
		String HAU_DM_BC_POOL_ORE="HAU_DM_B"+businessId+"C_POOL_ORE";
		String appendId=idFactiory.newId("DM_AID");
		List<String> shareIds=new ArrayList<String>();
		shareIds.add(newShareId);
		String insertSql="";
		String updateSql="";
		try {
			conn=this.getDbConnection();
			String getDataSourceSql="select a.id from HASYS_DM_DATAPOOL a where a.BusinessID=? and a.DataPoolName =?";
			pst=conn.prepareStatement(getDataSourceSql);
			pst.setInt(1,businessId);
			pst.setString(2,user.getId());
			rs=pst.executeQuery();
			Integer dataPoolNumber=null;
			while(rs.next()){
				dataPoolNumber=rs.getInt(1);
			}
			//不自动提交数据
			
			for (int i = 0; i < customerIds.length; i++) {
				conn.setAutoCommit(false);
				String customerId=customerIds[i];
				String importId=importIds[i];
				//单号码重拨共享表
				insertSql =String.format("INSERT INTO "+HAU_DM_BC_DATAM3+" (ID,BUSINESSID,SHAREID,IID,CID,STATE,MODIFYID,MODIFYUSERID,MODIFYTIME) VALUES(S_HASYS_DM_B1C_DATAM3.NEXTVAL,%s,'%s','%s','%s','%s',%s,'%s',sysdate)",businessId,newShareId,importId,customerId,SingleNumberModeShareCustomerStateEnum.APPENDED,0,user.getId()) ;
				pst=conn.prepareStatement(insertSql);
				pst.execute();
				//单号码重拨共享历史表
				insertSql =String.format("INSERT INTO "+HAU_DM_BC_DATAM3_HIS+" (ID,BUSINESSID,SHAREID,IID,CID,STATE,MODIFYID,MODIFYUSERID,MODIFYTIME) VALUES(S_HASYS_DM_B1C_DATAM3_HIS.NEXTVAL,%s,'%s','%s','%s','%s',%s,'%s',sysdate)",businessId,newShareId,importId,customerId,SingleNumberModeShareCustomerStateEnum.APPENDED,0,user.getId());
				pst = conn.prepareStatement(insertSql);
				pst.execute();
			    //更改数据池记录表数据
				updateSql=String.format("UPDATE "+HAU_DM_BC_POOL+" SET SOURCEID='%s',AREALAST=%s,AREACUR=%s,MODIFYTIME=sysdate WHERE IID='%s' AND CID='%s'",newShareId,0,1,importId,customerId);
	        	pst = conn.prepareStatement(updateSql);
				pst.execute();
				//数据池操作记录表
				insertSql=String.format("INSERT INTO "+HAU_DM_BC_POOL_ORE+" (ID,SOURCEID,IID,CID,OPERATIONNAME,DATAPOOLIDLAST,DATAPOOLIDCUR,AREALAST,AREACUR,ISRECOVER,MODIFYUSERID,MODIFYTIME) VALUES (S_HAU_DM_B1C_POOL_ORE.NEXTVAL,'%s','%s','%s','%s',%s,%s,%s,%s,%s,'%s',sysdate)",newShareId,importId,customerId,OperationNameEnum.Sharing,dataPoolNumber,dataPoolNumber,0,1,0,user.getId());
				pst = conn.prepareStatement(insertSql);
				pst.execute();
			}
			insertSql = String.format("INSERT INTO HASYS_DM_AID (ID,BUSINESSID,SHAREID,AdditionalID,AdditionalName,CreatUserID,CreateTime,Description,State) VALUES(S_HASYS_DM_AID.NEXTVAL,%s,'%s','%s','%s','%s',sysdate,'%s','%s')",businessId,newShareId,appendId,appendName,user.getId(),description,"入库成功");
			pst=conn.prepareStatement(insertSql);
			pst.execute();
			conn.commit();
			singleNumberOutboundDataManage.appendCustomersToShareBatch(businessId, shareIds);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ServiceResultCode.INVALID_SESSION;
		}
		return ServiceResultCode.SUCCESS;
	}
}
