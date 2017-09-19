package hiapp.modules.dmmanager.data;

import hiapp.modules.dm.singlenumbermode.bo.SingleNumberModeShareCustomerStateEnum;
import hiapp.modules.dmmanager.AreaCurEnum;
import hiapp.modules.dmmanager.AreaLastEnum;
import hiapp.modules.dmmanager.ImportBatchMassage;
import hiapp.modules.dmmanager.IsRecoverEnum;
import hiapp.modules.dmmanager.OperationNameEnum;
import hiapp.system.buinfo.User;
import hiapp.utils.DbUtil;
import hiapp.utils.database.BaseRepository;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;





/*import net.sf.json.JSONArray;
import net.sf.json.JSONObject;*/
import org.springframework.stereotype.Repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//数据共享db
@Repository
public class DMBizDataShare extends BaseRepository {

	//根据时间筛选导入批次号查询出没有被共享的客户批次数据
	@SuppressWarnings("resource")
	public List<Map<String, Object>> getNotShareDataByTimes(
			String StartTime,String EndTime,String businessId, String templateId) {
		String getXmlSql = "";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection dbConn = null;
		String jsonData=null;
		JsonArray dataArray=null;
		List<Map<String,Object>> dataList=new ArrayList<Map<String,Object>>();
		try {
			dbConn = this.getDbConnection();//select to_char(sysdate,'yy-mm-dd hh24:mi:ss')
			getXmlSql=String.format("SELECT XML FROM HASYS_DM_BIZTEMPLATEIMPORT WHERE TEMPLATEID='%s' AND BUSINESSID='%s'",templateId,businessId);
			stmt=dbConn.prepareStatement(getXmlSql);
			rs = stmt.executeQuery();
			while(rs.next()){
				//循环获取xml里面的数据
				jsonData=ClobToString(rs.getClob(1));	
			}
			//解析 通过查询获取xml，并把存储的json串解成json对象
			JsonObject jsonObject= new JsonParser().parse(jsonData).getAsJsonObject(); 
			//从对象中获取列名数组json集合
			dataArray=jsonObject.get("FieldMaps").getAsJsonArray();
			String sql="select ";
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < dataArray.size(); i++) {
				sb.append(dataArray.get(i).getAsJsonObject().get("FieldName").getAsString()+",");
			}
			sb.deleteCharAt(sb.length()-1);
			sql+=sb;
			
			sql=sql+" from HAU_DM_B"+businessId+"C_IMPORT where IID IN (select a.IID from HASYS_DM_IID a,HAU_DM_B1C_POOL b where a.IID=b.IID AND b.AREACUR='0' AND a.BUSINESSID=" + businessId + " OR a.IMPORTTIME BETWEEN to_date('"+StartTime+"','MM/dd/yyyy') AND to_date('"+ EndTime+"','MM/dd/yyyy'))";
			stmt=dbConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()){
				Map<String,Object> map=new HashMap<String, Object>();
				//列名不确定 需要循环 录入到 map集合中
				for (int i = 0; i < dataArray.size(); i++) {
					// 将循环出来的列名作为key
					String key=dataArray.get(i).getAsJsonObject().get("FieldName").getAsString();
					map.put(key,rs.getObject(i+1));
				}
				dataList.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			DbUtil.DbCloseQuery(rs,stmt);
			DbUtil.DbCloseConnection(dbConn);
		}
		return dataList;
	}
			/*//解析导入模板设置表
			*/
		
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
	
	

	// 2.1将共享的数据填入单号码重拨一份
	@SuppressWarnings("all")
	public String confirmShareData(String iId,String businessId,
			User user, String newId) {
		String insertsql = "";
		PreparedStatement stmt = null;
		Connection dbConn = null;
		try {
			dbConn = this.getDbConnection();
			insertsql =String.format("INSERT INTO HASYS_DM_B1C_DATAM3 (ID,BUSINESSID,SHAREID,IID,CID,STATE) VALUES(S_HASYS_DM_B1C_DATAM3.NEXTVAL,%s,'%s','%s','%s','%s')",businessId,newId,iId,user.getId(),SingleNumberModeShareCustomerStateEnum.CREATED) ;
			stmt = dbConn.prepareStatement(insertsql);
			stmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newId;
	}

	// 2.2将共享的数据填入单号码重拨共享历史表一份
	public void confirmShareDataOne(String iId, String bizid, User user, String newId) {
		String insertsql = "";
		PreparedStatement stmt = null;
		Connection dbConn = null;
		try {
			dbConn = this.getDbConnection();
			insertsql =String.format("INSERT INTO HASYS_DM_B1C_DATAM3_HIS (ID,BUSINESSID,SHAREID,IID,CID,STATE) VALUES(S_HASYS_DM_B1C_DATAM3_HIS.NEXTVAL,%s,'%s','%s','%s','%s')",bizid,newId,iId,user.getId(),SingleNumberModeShareCustomerStateEnum.CREATED);
			stmt = dbConn.prepareStatement(insertsql);
			stmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 查询当前的业务的数据池
	public int confirmShareDataTwo(String businessId) {
		String sql = "";
		PreparedStatement stmt = null;
		Connection dbConn = null;
		ResultSet rs = null;
		int s =0;
		int a=Integer.parseInt(businessId);
		try {
			dbConn = this.getDbConnection();
			sql ="SELECT DATAPOOLNAME FROM HASYS_DM_DATAPOOL WHERE BUSINESSID="+a+" AND DATAPOOLTYPE=2";
			stmt = dbConn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while(rs.next()){
			s = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	// 更改数据池记录表数据
	public void confirmShareDataThree(String iId,
			int dataPool, User user) {
		String updatesql = "";
		PreparedStatement stmt = null;
		Connection dbConn=null;
        try {
        	dbConn=this.getDbConnection();
        	updatesql=String.format("UPDATE HAU_DM_B1C_POOL SET CID='%s',DATAPOOLIDLAST=%s,DATAPOOLIDCUR=%s,AREALAST=%s,AREACUR=%s WHERE IID='%s'",user.getId(),dataPool,dataPool,1,2,iId);
        	stmt = dbConn.prepareStatement(updatesql);
			stmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//向数据池操作记录表添加数据
	public void confirmShareDataFree(String iId,
			User user, int dataPool) {
		String insertsql = "";
		PreparedStatement stmt = null;
		Connection dbConn = null;
		try {
			dbConn=this.getDbConnection();
			insertsql=String.format("INSERT INTO HAU_DM_B1C_POOL_ORE values(S_HAU_DM_B1C_POOL_ORE.NEXTVAL,'%s','%s','%s','%s',%s,%s,%s,%s,%s,'%s',to_date(sysdate,'yyyy/mm/dd hh24:mi'))",null,iId,user.getId(),OperationNameEnum.Sharing,dataPool,dataPool,1,2,0,user.getId());
			stmt = dbConn.prepareStatement(insertsql);
			stmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//向共享批次信息表添加数据
	public ServiceResultCode confirmShareDataFive(String businessId,
			String batherId, String shareName, String description,User user) {
		String insertsql = "";
		PreparedStatement stmt = null;
		Connection dbConn = null;
		try {
			dbConn=this.getDbConnection();
			insertsql = String.format("INSERT INTO HASYS_DM_SID (ID,BUSINESSID,SHAREID,SHARENAME,CREATEUSERID,CREATETIME,DESCRIPTION) VALUES(S_HASYS_DM_SID.NEXTVAL,%s,'%s','%s','%s',to_date(sysdate,'yyyy/mm/dd hh24:mi'),'%s')",businessId,batherId,shareName,user.getId(),description);
			stmt = dbConn.prepareStatement(insertsql);
			stmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ServiceResultCode.SUCCESS;
	}
}