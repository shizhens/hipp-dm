package hiapp.modules.dmsetting;
/** 
 * @author liuhao 
 * @version ����ʱ�䣺2017��9��8�� ����02:15:42 
 * ��˵��  ���ڴ洢��������Ϣ
 */
public class DMBizPhoneType {
		//������������
		private String	name;
		//����������������
		private String	nameCh;
		//����
		private String	description;
		//����˳��
		private int	dialOrder;
		//��Ӧ�����ֶ�
		private String customerColumnMap;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getNameCh() {
			return nameCh;
		}
		public void setNameCh(String nameCh) {
			this.nameCh = nameCh;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public int getDialOrder() {
			return dialOrder;
		}
		public void setDialOrder(int dialOrder) {
			this.dialOrder = dialOrder;
		}
		public String getCustomerColumnMap() {
			return customerColumnMap;
		}
		public void setCustomerColumnMap(String customerColumnMap) {
			this.customerColumnMap = customerColumnMap;
		}
}
