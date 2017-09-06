package hiapp.modules.dmmanager.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dm.setting.dbLayer.DMBizTemplateImport;
import dm.setting.dbLayer.DMBizWorkSheets;
import dm.setting.dbLayer.ImportMapColumn;
import dm.setting.dbLayer.WorkSheetTypeDm;
import hiapp.utils.serviceresult.ServiceResult;
import hiapp.modules.dmmanager.dbLayer.DMBizTaskManager;
import hiapp.modules.dmmanager.dbLayer.DMTask;
import hiapp.system.session.Tenant;
import hiapp.system.session.User;
import hiapp.system.worksheet.dblayer.WorkSheet;
import hiapp.system.worksheet.dblayer.WorkSheetColumn;
import hiapp.system.worksheet.dblayer.WorkSheetManager;
import hiapp.utils.ConstantResultType;
import hiapp.utils.UtilServlet;

/**
 * Servlet implementation class servletDistributeCust
 */
//@WebServlet("/servletDistributeCust")
public class servletDistributeCust2 extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public servletDistributeCust2() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
HttpSession session=request.getSession(false);
		
		Connection dbConn = null; 
		PrintWriter printWriter = response.getWriter();
		String userid="";
		try {
			Tenant tenant = (Tenant)session.getAttribute("tenant");
			dbConn= tenant.getDbConnectionConfig().getDbConnection();
			User user = (User)request.getSession(false).getAttribute("user");
			userid=user.getAgtId();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}catch(NullPointerException e)
		{
			JsonObject jsonObject =  new JsonObject();
			jsonObject.addProperty("returnCode", "2");
			jsonObject.addProperty("returnMessage","超时");
			printWriter.print(jsonObject.toString());
			return;
		}
		
		ServiceResult Result=new ServiceResult();
		String szActionValue = request.getParameter("action");
		
		if(szActionValue.equals("queryTask")){
			try {
				String bizId=request.getParameter("bizId");
				String szdateStart=request.getParameter("dateStart");
				String szdateEnd=request.getParameter("dateEnd");
				JsonObject jsonObject=queryTask(dbConn,Integer.parseInt(bizId),szdateStart,szdateEnd);
				printWriter.write(jsonObject.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(szActionValue.equals("getColumns")){
			try {
				String bizId=request.getParameter("bizId");
				JsonObject jsonObject=getColumns(dbConn,Integer.parseInt(bizId));
				printWriter.write(jsonObject.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(szActionValue.equals("queryDistributeData")){
			try {
				String bizId=request.getParameter("bizId");
				String taskList=request.getParameter("taskList");
				JsonParser jsonParser=new JsonParser();
				JsonArray jaTaskList=jsonParser.parse(taskList).getAsJsonArray();
				JsonObject jsonObject=queryDistributeData(dbConn,Integer.parseInt(bizId),jaTaskList);
				printWriter.write(jsonObject.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(szActionValue.equals("submitDistributeData")){
			try {
				String bizId=request.getParameter("bizId");
				String distributeData=request.getParameter("distributeData");
				JsonParser jsonParser=new JsonParser();
				JsonArray jaDistributeData=jsonParser.parse(distributeData).getAsJsonArray();
				submitDistributeData(dbConn,Integer.parseInt(bizId),jaDistributeData,userid);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
	}
	public static JsonObject queryTask(Connection dbConn,int bizId,String szdateStart,String szdateEnd) throws Exception{
		List<DMTask> listTask=new ArrayList<DMTask>();
		if(!DMBizTaskManager.getTaskList(dbConn, bizId, szdateStart, szdateEnd, listTask)){
			
		}
		
		//utilServlet.setResultType(ConstantResultType.RESULTTYPE_GRID);
		JsonObject jsonObject=new JsonObject();
		JsonArray jaRows=new JsonArray();
		int nRow=1;
		for(int ii=0;ii<listTask.size();ii++){
			DMTask dmTask=listTask.get(ii);
			JsonObject jsItem=new JsonObject();
			jsItem.addProperty("taskId", dmTask.getId());
			jsItem.addProperty("taskName", dmTask.getName());
			jsItem.addProperty("createUserId", dmTask.getCreateUserId());
			jsItem.addProperty("createTime", dmTask.getCreateTime());
			jsItem.addProperty("taskDescription", dmTask.getDescription());
			jaRows.add(jsItem);
		}		
		jsonObject.addProperty("total", nRow-1);
		jsonObject.add("rows", jaRows);

		return jsonObject;
	}
	public static JsonObject getColumns(Connection dbConn,int bizId) throws Exception{
		int workSheetId=DMBizWorkSheets.getWorkSheetId(dbConn, bizId, "客户任务工作表");
		List<WorkSheetColumn> listColumns=new ArrayList<WorkSheetColumn>();
		WorkSheet.getColumns(dbConn, workSheetId, listColumns);
		
		//utilServlet.setResultType(ConstantResultType.RESULTTYPE_QUERY);
		JsonObject jsonObject=new JsonObject();
		
		JsonArray jaColumns=new JsonArray();
        JsonObject jsCol=null; 
        jsCol=new JsonObject();
	    jsCol.addProperty("field", "distributedUserId");
	    jsCol.addProperty("title", "分配坐席ID");
	    jsCol.addProperty("width", 80);
        jaColumns.add(jsCol);
        
        jsCol=new JsonObject();
	    jsCol.addProperty("field", "distributedUserName");
	    jsCol.addProperty("title", "分配坐席名");
	    jsCol.addProperty("width", 80);
        jaColumns.add(jsCol);
        
	    for(int ii=0;ii<listColumns.size();ii++){
	    	WorkSheetColumn workSheetColumn=listColumns.get(ii);
	        jsCol=new JsonObject();
		    jsCol.addProperty("field", workSheetColumn.getName());
		    jsCol.addProperty("title", workSheetColumn.getNameCh());
		    jsCol.addProperty("width", 80);
	        jaColumns.add(jsCol);
		}
        jsonObject.add("columns", jaColumns);

		return jsonObject;
	}
	public static JsonObject queryDistributeData(Connection dbConn,int bizId,JsonArray jaTaskList) throws Exception{
		List<WorkSheetColumn> listColumn=new ArrayList<WorkSheetColumn>();
		ResultSet rs=null;
		PreparedStatement stmt = null;
		JsonObject jsonObject=new JsonObject();
		JsonArray jaRows=new JsonArray();
		try {
			int workSheetId=DMBizWorkSheets.getWorkSheetId(dbConn,bizId, "客户任务工作表");
			String szTableNameCustInTask=WorkSheetManager.getWorkSheetName(dbConn,workSheetId);
			WorkSheet.getColumns(dbConn, workSheetId, listColumn);
			String szSql;
			String szSqlPart1="Select ";
			String szSqlPart2="";
			String szSqlTaskFilter="taskid in (";
			for(int ii=0;ii<jaTaskList.size();ii++){
				JsonObject jsTask=jaTaskList.get(ii).getAsJsonObject();
				JsonElement jElement=jsTask.get("id");
				String taskId=jElement.getAsString();
				szSqlTaskFilter+="'";
				szSqlTaskFilter+=taskId;
				szSqlTaskFilter+="'";
				if(ii<jaTaskList.size()-1){
					szSqlTaskFilter+=",";
				}
			}		
			
			
			szSqlTaskFilter+=")";
			for(int nCol=0;nCol<listColumn.size();nCol++){
				WorkSheetColumn workSheetColumn=listColumn.get(nCol);
				String szItem=String.format("%s",workSheetColumn.getName());
				szSqlPart2+=szItem;
				if(nCol!=listColumn.size()-1){
					szSqlPart2+=",";
				}
			}
			String szSqlPart3=String.format("FROM %s ",szTableNameCustInTask);

			szSql=String.format("%s\r\n%s\r\n%s\r\n where %s  ORDER BY ID",
								szSqlPart1,szSqlPart2,szSqlPart3,szSqlTaskFilter);
		stmt = dbConn.prepareStatement(szSql);
		rs = stmt.executeQuery();
		
		//utilServlet.setResultType(ConstantResultType.RESULTTYPE_GRID);
		
		int nRow=1;
		while(rs.next()){
			JsonObject jsItem=new JsonObject();
			jsItem.addProperty("no", nRow);
			nRow++;
			for(int nCol=0;nCol<listColumn.size()-1;nCol++){
				WorkSheetColumn column=listColumn.get(nCol);
				String szFieldName=column.getName();
				String szFieldValue=rs.getString(nCol+1);
				jsItem.addProperty(szFieldName, szFieldValue);
			}		
			jaRows.add(jsItem);
		}
		jsonObject.addProperty("total", nRow-1);
		jsonObject.add("rows", jaRows);
	} catch (SQLException e) {
		e.printStackTrace();
		
	} finally {
		
	}
		return jsonObject;
	}
	
	public static boolean submitDistributeData(Connection dbConn,int bizId,JsonArray jaDistributeData,String userid) throws Exception{
		PreparedStatement stmt = null;
		String szTableNameCustDist="";
		String szTableNameCustDist_His="";
		String szSeqNameCustDist="";
		String szSeqNameCustDist_His="";
		int workSheetId=DMBizWorkSheets.getWorkSheetId(dbConn,bizId, WorkSheetTypeDm.WSTDM_CUSTDIST.getType());
		szTableNameCustDist=WorkSheetManager.getWorkSheetName(dbConn,workSheetId);
		szSeqNameCustDist=WorkSheetManager.getWorkSheetSeqName(dbConn, workSheetId);
		
		workSheetId=DMBizWorkSheets.getWorkSheetId(dbConn,bizId, WorkSheetTypeDm.WSTDM_CUSTDIST.getType());
		szTableNameCustDist_His=WorkSheetManager.getWorkSheetName(dbConn,workSheetId);
		szSeqNameCustDist_His=WorkSheetManager.getWorkSheetSeqName(dbConn, workSheetId);
		
		String szHeader=String.format("insert into %s ",szTableNameCustDist );
		String szHeader_his=String.format("insert into %s ",szTableNameCustDist_His );
		String szSqlFields=String.format("(id, iid, cid, taskid, modifyid, modifyuserid, modifytime, distuserid, disttime, userid, username )");
		String userIdModify=userid;
		String szSql;
		try {
			for(int ii=0;ii<jaDistributeData.size();ii++){
				JsonObject jsonObject=jaDistributeData.get(ii).getAsJsonObject();
				String userId=jsonObject.get("userId").getAsString();
				String userName=jsonObject.get("userName").getAsString();
				String taskId=jsonObject.get("taskId").getAsString();
				String IID=jsonObject.get("IID").getAsString();
				String CID=jsonObject.get("CID").getAsString();

				String szSqlValues=String.format("values (%s.nextval, '%s', '%s', '%s', 0, '%s', sysdate, '%s', sysdate, '%s', '%s')",
													szSeqNameCustDist,IID,CID,taskId,userIdModify,userIdModify,userId,userName);
				szSql=String.format("%s\r\n%s\r\n%s\r\n",szHeader,szSqlFields,szSqlValues);
				stmt = dbConn.prepareStatement(szSql);
				stmt.execute();
				
				
				String szSqlValues_His=String.format("values (%s.nextval, '%s', '%s', '%s', 0, '%s', sysdate, '%s', sysdate, '%s', '%s')",
														szSeqNameCustDist_His,IID,CID,taskId,userIdModify,userIdModify,userId,userName);
				szSql=String.format("%s\r\n%s\r\n%s\r\n",szHeader_his,szSqlFields,szSqlValues_His);
				stmt = dbConn.prepareStatement(szSql);
				stmt.execute();
				
			}
		
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
		}
		/*utilServlet.setResultType(ConstantResultType.RESULTTYPE_SUBMIT);
		utilServlet.setResultCode(0);
		utilServlet.setResultMessage("分配成功！");*/

		return true;
	}
}