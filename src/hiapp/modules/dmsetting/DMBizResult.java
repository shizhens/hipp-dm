package hiapp.modules.dmsetting;

import org.springframework.http.StreamingHttpOutputMessage;

/** 
 * @author liuhao 
 * @version ����ʱ�䣺2017��9��8�� ����02:15:42 
 * ��˵��  ���ڴ洢�������Ϣ
 */

public class DMBizResult {
		//����id
		private int	id;
		//��Դ���
		private String sourceId;
		//��������id
		private String	iid;
		//�ͻ�id
		private String	cid;
		//�Ƿ�Ϊ���һ���޸�
		private String	modifyLast;
		//�޸�id
		private String modifyId;
		//�޸��˹���
		private String modifyUserid;
		//�޸�ʱ��
		private String modifyTime;
		//��������
		private String optrType;
		//����ʱ��
		private String dialTime;
		//������ˮ��
		private String customerCallId;
		
		
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getSourceId() {
			return sourceId;
		}
		public void setSourceId(String sourceId) {
			this.sourceId = sourceId;
		}
		public String getIid() {
			return iid;
		}
		public void setIid(String iid) {
			this.iid = iid;
		}
		public String getCid() {
			return cid;
		}
		public void setCid(String cid) {
			this.cid = cid;
		}
		public String getModifyLast() {
			return modifyLast;
		}
		public void setModifyLast(String modifyLast) {
			this.modifyLast = modifyLast;
		}
		public String getModifyId() {
			return modifyId;
		}
		public void setModifyId(String modifyId) {
			this.modifyId = modifyId;
		}
		public String getModifyUserid() {
			return modifyUserid;
		}
		public void setModifyUserid(String modifyUserid) {
			this.modifyUserid = modifyUserid;
		}
		public String getModifyTime() {
			return modifyTime;
		}
		public void setModifyTime(String modifyTime) {
			this.modifyTime = modifyTime;
		}
		public String getOptrType() {
			return optrType;
		}
		public void setOptrType(String optrType) {
			this.optrType = optrType;
		}
		public String getDialTime() {
			return dialTime;
		}
		public void setDialTime(String dialTime) {
			this.dialTime = dialTime;
		}
		public String getCustomerCallId() {
			return customerCallId;
		}
		public void setCustomerCallId(String customerCallId) {
			this.customerCallId = customerCallId;
		}
}
