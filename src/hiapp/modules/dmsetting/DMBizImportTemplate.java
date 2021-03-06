package hiapp.modules.dmsetting;


/** 
 * @author yangwentian 
 * @version 创建时间：2017年9月8日 下午3:16:43 
 * 类说明  业务导入模板配置信息
 */
public class DMBizImportTemplate {
	private int templateId;		//模板ID，自动生成
	private int bizId;	//业务ID
	private String templateName;//模板名称
	private String desc;	//描述
	private int isDefault;		//是否是缺省模板
	private String dataSourceType;	//数据来源类型
	private String configJson; 	//json格式详细配置数据
	public int getTemplateId() {
		return templateId;
	}
	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}
		public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(int isDefault) {
		this.isDefault = isDefault;
	}
	
	public String getConfigJson() {
		return configJson;
	}
	public void setConfigJson(String configJson) {
		this.configJson = configJson;
	}
	public String getDataSourceType() {
		return dataSourceType;
	}
	public void setDataSourceType(String dataSourceType) {
		this.dataSourceType = dataSourceType;
	}
	public int getBizId() {
		return bizId;
	}
	public void setBizId(int bizId) {
		this.bizId = bizId;
	}
}
