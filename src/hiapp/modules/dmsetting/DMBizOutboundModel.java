package hiapp.modules.dmsetting;
/** 
 * @author liuhao 
 * @version ����ʱ�䣺2017��9��8�� ����02:15:42 
 * ��˵��  ���ڴ洢��������Ϣ
 */
public class DMBizOutboundModel {
			//����id
			private int	id;
			//���ģʽid
			private int	outboundID;
			//�������
			private String	outboundType;
			//���ģʽ
			private String	outboundMode;
			
			public int getId() {
				return id;
			}
			public void setId(int id) {
				this.id = id;
			}
			public int getOutboundID() {
				return outboundID;
			}
			public void setNameCh(int outboundID) {
				this.outboundID = outboundID;
			}
			public String getOutboundType() {
				return outboundType;
			}
			public void setOutboundType(String outboundType) {
				this.outboundType = outboundType;
			}
			public String getOutboundMode() {
				return outboundMode;
			}
			public void setOutboundMode(String outboundMode) {
				this.outboundMode = outboundMode;
			}
			
			
}
