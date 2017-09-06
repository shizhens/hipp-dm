package hiapp.modules.dmsetting.servlet;

import hiapp.utils.serviceresult.RecordsetResult;
import hiapp.utils.serviceresult.ServiceResultCode;
import hiapp.system.session.Tenant;
import hiapp.utils.DbUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dmNew.setting.dbLayer.DMBusiness;
import dmNew.setting.dbLayer.DMBusinessManager;

/**
 * Servlet implementation class ServletDMBusinessQuery
 */
@WebServlet("/ServletDMBusinessQuery")
public class ServletDMBusinessQuery extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServletDMBusinessQuery() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session=request.getSession(false);
		Tenant tenant = (Tenant)session.getAttribute("tenant");
		Connection dbConn = null;
		try {
			dbConn= tenant.getDbConnectionConfig().getDbConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		String searchKeyString=request.getParameter("searchKey");
		RecordsetResult recordsetResult=new RecordsetResult();
		List<DMBusiness> listDMBusiness=new ArrayList<DMBusiness>();
		try {
			if (!DMBusinessManager.DMBusinessQuery(dbConn,searchKeyString,listDMBusiness)) {
				return;
			}
			recordsetResult.setResultCode(ServiceResultCode.SUCCESS);
			recordsetResult.setPage(0);	
			recordsetResult.setTotal(listDMBusiness.size());
			recordsetResult.setPageSize(listDMBusiness.size());
			recordsetResult.setRows(listDMBusiness);
			PrintWriter printWriter=null;
			printWriter = response.getWriter();
			printWriter.write(recordsetResult.toJson());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtil.DbCloseConnection(dbConn);
		}
	}
}