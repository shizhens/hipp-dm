package hiapp.modules.dmagent.data;

import hiapp.modules.dmagent.DeployPage;
import hiapp.modules.dmagent.DeployType;

import java.sql.Blob;

/**
 * 客户页面配置表HASYS_DM_CUPAGETEMPLATE
 * @author PengXiaoHong
 */
public class CuPageTemplate {
	private long id;//ID
	private long bizId;//业务ID
	private DeployPage deployPage;//配置页面
	private DeployType deployType;//配置类型
	private Blob Templatedata;//配置模板数据
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getBizId() {
		return bizId;
	}
	public void setBizId(long bizId) {
		this.bizId = bizId;
	}
	public DeployPage getDeployPage() {
		return deployPage;
	}
	public void setDeployPage(DeployPage deployPage) {
		this.deployPage = deployPage;
	}
	public DeployType getDeployType() {
		return deployType;
	}
	public void setDeployType(DeployType deployType) {
		this.deployType = deployType;
	}
	public Blob getTemplatedata() {
		return Templatedata;
	}
	public void setTemplatedata(Blob templatedata) {
		Templatedata = templatedata;
	}
	
}
