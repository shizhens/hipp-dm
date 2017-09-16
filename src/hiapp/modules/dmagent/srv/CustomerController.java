package hiapp.modules.dmagent.srv;

import hiapp.modules.dmagent.QueryRequest;
import hiapp.modules.dmagent.QueryTemplate;
import hiapp.modules.dmagent.data.CustomerRepository;
import hiapp.system.buinfo.Permission;
import hiapp.system.buinfo.RoleInGroupSet;
import hiapp.system.buinfo.User;
import hiapp.system.buinfo.data.PermissionRepository;
import hiapp.system.buinfo.data.UserRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

@RestController
public class CustomerController {
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PermissionRepository permissionRepository;

	/**
	 * 获取配置查询模板时需要使用的候选列
	 * 
	 * @param bizId
	 * @param configPage
	 * @return
	 * @throws SQLException
	 */
	@RequestMapping(value = "/srv/agent/getCandidadeColumn.srv", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public Map<String, Object> getCandidadeColumn(String bizId,
			String configPage) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> candidadeColumn = null;

		try {
			candidadeColumn = customerRepository.getCandidadeColumn(bizId,
					configPage);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", 1);
			result.put("reason", e.getMessage());
			return result;
		}

		result.put("data", candidadeColumn);
		result.put("result", 0);

		return result;
	}

	/**
	 * 保存配置好的查询模板
	 * 
	 * @param queryItem
	 * @return
	 */
	@RequestMapping(value = "/srv/agent/saveQueryTemplate.srv", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public Map<String, Object> saveQueryTemplate(QueryTemplate queryTemplate) {
		Map<String, Object> result = new HashMap<String, Object>();

		if (customerRepository.saveQueryTemplate(queryTemplate)) {
			result.put("result", 0);
		} else {
			result.put("result", 1);
			result.put("reason", "参数错误！");
			return result;
		}

		return result;
	}

	/**
	 * 保存配置好的查询模板
	 * 
	 * @param queryItem
	 * @return
	 */
	@RequestMapping(value = "/srv/agent/saveListTemplate.srv", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public Map<String, Object> saveListTemplate(QueryTemplate queryTemplate) {
		return saveQueryTemplate(queryTemplate);
	}

	/**
	 * 获取查询模板
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/srv/agent/getQueryTemplate.srv", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public Map<String, Object> getQueryTemplate(QueryTemplate queryTemplate) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		try {
			list = new Gson().fromJson(
					customerRepository.getQueryTemplate(queryTemplate),
					List.class);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", 1);
			result.put("reason", e.getMessage());
			return result;
		}

		result.put("data", list);
		result.put("result", 0);
		return result;
	}

	/**
	 * 获取列表模板
	 * 
	 * @return
	 */
	@RequestMapping(value = "/srv/agent/getListTemplate.srv", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public Map<String, Object> getListTemplate(QueryTemplate queryTemplate) {
		return getQueryTemplate(queryTemplate);
	}

	/**
	 * 讲数据注入模板
	 * 
	 * @return
	 */
	public String dataToListPattern(Map[][] data) {
		return "<table width=100% height=100% cellpadding=0 cellspacing=0>\n"
				+ "\t<tr height=18px>\n"
				+ "\t\t<th width=118px align=left style='font-size:8px;color: "
				+ data[0][0].get("fontColor")
				+ "'>"
				+ data[0][0].get("value")
				+ "</th>\n"
				+ "\t\t<th width=118px align=left style='font-size:8px;color: "
				+ data[0][1].get("fontColor")
				+ "'>"
				+ data[0][1].get("value")
				+ "</th>\n"
				+ "\t\t<th width=118px align=left style='font-size:8px;color: "
				+ data[0][2].get("fontColor")
				+ "'>"
				+ data[0][2].get("value")
				+ "</th>\n"
				+ "\t</tr>\n"
				+ "\t<tr height=18px>\n"
				+ "\t\t<th width=118px align=left style='font-size:8px;color: "
				+ data[1][0].get("fontColor")
				+ "'>"
				+ data[1][0].get("value")
				+ "</th>\n"
				+ "\t\t<th width=118px align=left style='font-size:8px;color: "
				+ data[1][1].get("fontColor")
				+ "'>"
				+ data[1][1].get("value")
				+ "</th>\n"
				+ "\t\t<th width=118px align=left style='font-size:8px;color: "
				+ data[1][2].get("fontColor")
				+ "'>"
				+ data[1][2].get("value")
				+ "</th>\n"
				+ "\t</tr>\n"
				+ "\t<tr height=18px>\n"
				+ "\t\t<th width=118px align=left style='font-size:8px;color: "
				+ data[2][0].get("fontColor")
				+ "'>"
				+ data[2][0].get("value")
				+ "</th>\n"
				+ "\t\t<th width=118px align=left style='font-size:8px;color: "
				+ data[2][1].get("fontColor")
				+ "'>"
				+ data[2][1].get("value")
				+ "</th>\n"
				+ "\t\t<th width=118px align=left style='font-size:8px;color: "
				+ data[2][2].get("fontColor")
				+ "'>"
				+ data[2][2].get("value")
				+ "</th>\n" + "\t</tr>\n" + "</table>";
	}

	/**
	 * 把要显示的数据拼接成html供前台显示
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<String> listToHtml(List<List<Map<String, Object>>> queryData) {
		List<String> result = new ArrayList<String>();
		for (List<Map<String, Object>> list : queryData) {
			Map[][] maps = new Map[3][3];
			// 设置默认值
			for (int i = 0; i < maps.length; i++) {
				for (int j = 0; j < maps[i].length; j++) {
					maps[i][j].put("value", "");
					maps[i][j].put("fontColor", "");
				}
			}
			// 匹配模板
			for (Map<String, Object> map : list) {
				maps[(int) map.get("rowNumber")][(int) map.get("colNumber")] = map;
			}
			result.add(dataToListPattern(maps));
		}
		return result;
	}

	/**
	 * 支持坐席根据不同业务和管理员自定义配置的查询条件查询所有客户数据.
	 * 
	 * @param queryRequest
	 * @return
	 */
	@RequestMapping(value = "/srv/agent/queryMyCustomers.srv", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public Map<String, Object> queryMyCustomers(QueryRequest queryRequest,
			HttpSession session) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<List<Map<String, Object>>> list = new ArrayList<List<Map<String, Object>>>();

		try {
			list = customerRepository.queryMyCustomers(queryRequest,
					((User) session.getAttribute("user")).getId());
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", 1);
			result.put("reason", e.getMessage());
			return result;
		}

		result.put("data", listToHtml(list));
		result.put("result", 0);
		return result;
	}

	/**
	 * 支持坐席根据不同业务和管理员自定义的查询条件查询预约或待跟进的客户列表
	 * 
	 * @param queryRequest
	 * @return
	 */
	@RequestMapping(value = "/srv/agent/queryMyPresetCustomers.srv", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public Map<String, Object> queryMyPresetCustomers(
			QueryRequest queryRequest, HttpSession session) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<List<Map<String, Object>>> list = new ArrayList<List<Map<String, Object>>>();

		try {
			list = customerRepository.queryMyPresetCustomers(queryRequest,
					((User) session.getAttribute("user")).getId());
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", 1);
			result.put("reason", e.getMessage());
			return result;
		}

		result.put("data", listToHtml(list));
		result.put("result", 0);
		return result;
	}

	/**
	 * 查询不同业务和管理员自定义的查询条件查询客户列表
	 * 
	 * @param queryRequest
	 * @return
	 */
	@RequestMapping(value = "/srv/agent/queryAllCustomers.srv", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public Map<String, Object> queryAllCustomers(QueryRequest queryRequest,
			HttpSession session) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<List<Map<String, Object>>> list = new ArrayList<List<Map<String, Object>>>();
		try {
			User user = (User) session.getAttribute("user");
			String userId = user.getId();
			RoleInGroupSet roleInGroupSet = userRepository
					.getRoleInGroupSetByUserId(userId);
			Permission permission = permissionRepository
					.getPermission(roleInGroupSet);
			int permissionId = permission.getId();

			list = customerRepository.queryAllCustomers(queryRequest,
					permissionId);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", 1);
			result.put("reason", e.getMessage());
			return result;
		}

		result.put("data", listToHtml(list));
		result.put("result", 0);
		return result;
	}

}
