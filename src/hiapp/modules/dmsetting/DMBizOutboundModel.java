package hiapp.modules.dmsetting;
/** 
 * @author liuhao 
 * @version ����ʱ�䣺2017��9��8�� ����02:15:42 
 * ��˵��  ���ڴ洢���ģʽ��Ϣ
 */
public enum DMBizOutboundModel {
			MODE1(1,1,"������","�ֶ�����"),
			MODE2(2,2,"������","hidialer�Զ����"),
			MODE3(3,3,"������","�������ز�"),
			MODE4(4,4,"������","������ز�"),
			MODE5(5,5,"������","������Ԥ���Ⲧ"),
			MODE6(6,6,"������","�����Ԥ���Ⲧ");
			
		private DMBizOutboundModel(int id,int outboundID,String outboundType,String outboundMode) {
			this.id=id;
			this.outboundID=outboundID;
			this.outboundType=outboundType;
	    	this.outboundMode=outboundMode;
	    	
	    }
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
