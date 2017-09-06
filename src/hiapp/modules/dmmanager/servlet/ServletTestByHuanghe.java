package hiapp.modules.dmmanager.servlet;

import hiapp.utils.serviceresult.RecordsetResult;
import hiapp.utils.serviceresult.ServiceResultCode;
import hiapp.system.buinfo.beanOld.User;
import hiapp.system.buinfo.beanOld.UserManager;
import hiapp.system.session.Tenant;
import hiapp.system.worksheet.dblayer.WorkSheetDataType;

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

/**
 * Servlet implementation class ServletTestByHuanghe
 */
@WebServlet("/ServletTestByHuanghe")
public class ServletTestByHuanghe extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServletTestByHuanghe() {
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
		try {
			int nCount=WorkSheetDataType.getCount();
			for(int ii=1;ii<4;ii++){
				String nameCh=WorkSheetDataType.getNameCh(ii);
				System.out.print(nameCh);
			}
			
			PrintWriter printWriter=null;
			printWriter = response.getWriter();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}