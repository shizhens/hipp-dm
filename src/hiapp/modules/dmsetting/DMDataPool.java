package hiapp.modules.dmsetting;
/** 
 * @author liuhao 
 * @version ����ʱ�䣺2017��9��8�� ����02:15:42 
 * ��˵��  ���ڴ洢���ݳ���Ϣ
 */
public class DMDataPool {
	//���ݳر��
	private int id;
	//���ݳ�����
	private String dataPoolName;
	//���ݳ�����
	private String dataPoolType;
	//���ݳ�����
	private String dataPoolDes;
	//���ڵ�
	private int pid;
	//��������
	private int areaType;
	//���ݳ�����
	private int poolTopLimit;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDataPoolName() {
		return dataPoolName;
	}
	public void setDataPoolName(String dataPoolName) {
		this.dataPoolName = dataPoolName;
	}
	public String getDataPoolType() {
		return dataPoolType;
	}
	public void setDataPoolType(String dataPoolType) {
		this.dataPoolType = dataPoolType;
	}
	public String getDataPoolDes() {
		return dataPoolDes;
	}
	public void setDataPoolDes(String dataPoolDes) {
		this.dataPoolDes = dataPoolDes;
	}
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}
	public int getAreaType() {
		return areaType;
	}
	public void setAreaType(int areaType) {
		this.areaType = areaType;
	}
	public int getPoolTopLimit() {
		return poolTopLimit;
	}
	public void setModeId(int poolTopLimit) {
		this.poolTopLimit = poolTopLimit;
	}
	
}
