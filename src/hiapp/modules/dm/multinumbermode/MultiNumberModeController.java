package hiapp.modules.dm.multinumbermode;

import com.google.gson.Gson;
import hiapp.modules.dm.Constants;
import hiapp.modules.dm.bo.CustomerBasic;
import hiapp.modules.dm.multinumbermode.bo.BizConfig;
import hiapp.modules.dm.multinumbermode.bo.MultiNumberCustomer;
import hiapp.modules.dm.multinumbermode.bo.PhoneDialInfo;
import hiapp.modules.dm.singlenumbermode.bo.NextOutboundCustomerResult;
import hiapp.modules.dm.util.DateUtil;
import hiapp.modules.dm.util.XMLUtil;
import hiapp.modules.dmsetting.DMBusiness;
import hiapp.system.buinfo.User;
import hiapp.utils.serviceresult.ServiceResult;
import hiapp.utils.serviceresult.ServiceResultCode;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class MultiNumberModeController {

    @Autowired
    MultiNumberOutboundDataManage multiNumberOutboundDataManage;


    /**
     * @param bizId
     * @param count
     * @return
     *
     * <Msg Result="1" CustomerCount="1">
     *    <CustList>
     *    <Item DMBusinessId="24" IID="20170309P_0001" CID="2398636" TaskID="20170309T0001" PhoneType="1" PhoneNum="15777731180"/>
     *    </CustList>
     * </Msg>
     *
     */
    public String hiDialerGetCustList(int bizId, int count) {

        List<MultiNumberCustomer> customerList = multiNumberOutboundDataManage.extractNextOutboundCustomer(
                Constants.HiDialerUserId, bizId, count);

        /*if (null == customerList) {
            Document doc = new Document();
            Element root = new Element("Msg");
            root.setAttribute("Result", "0");
            root.setAttribute("CustomerCount", "0");
            doc.setRootElement(root);
            return XMLUtil.outputDocumentToString(doc);
        }*/

        Document doc = new Document();
        Element root = new Element("Msg");
        root.setAttribute("Result", "1");
        doc.setRootElement(root);
        Element custList = new Element("CustList");

        for (MultiNumberCustomer customer : customerList) {
            Element item = new Element("Item");
            item.setAttribute("DMBusinessId", String.valueOf(customer.getBizId()));
            item.setAttribute("IID", customer.getImportBatchId());
            item.setAttribute("CID", customer.getCustomerId());
            item.setAttribute("TaskID", customer.getShareBatchId());
            item.setAttribute("PhoneType", String.valueOf(customer.getCurDialPhoneType()));
            item.setAttribute("PhoneNum", customer.getCurDialPhone());

            custList.addContent(item);
        }

        root.addContent(custList);

        return XMLUtil.outputDocumentToString(doc);
    }

    public String hiDialerDialResultNotify(int bizId, String importBatchId, String customerId, String shareBatchId,
                                           String phoneType, String resultCode, String customerCallID)
    {
        multiNumberOutboundDataManage.hiDialerDialResultNotify(Constants.HiDialerUserId, bizId, importBatchId,
                customerId, Integer.valueOf(phoneType), resultCode, resultCode, customerCallID);

        Document doc = new Document();
        Element root = new Element("Msg");
        root.setAttribute("Result", "1");
        root.setAttribute("Description", "");
        doc.setRootElement(root);
        return XMLUtil.outputDocumentToString(doc);
    }

    public String submitScreenPopUp(String userId, String strBizId, String importBatchId, String customerId, String strPhoneType) {

        ServiceResult serviceresult = new ServiceResult();

        multiNumberOutboundDataManage.submitAgentScreenPopUp(userId, Integer.parseInt(strBizId), importBatchId,
                customerId, Integer.valueOf(strPhoneType));

        serviceresult.setResultCode(ServiceResultCode.SUCCESS);
        return serviceresult.toJson();
    }

    public String submitOutboundResult(String userId, int bizId, String shareBatchId, String importBatchId,
                       String customerId, String strPhoneType, String resultCodeType, String resultCode,
                       Boolean isPreset, Date presetTime, String dialType, Date dialTime, String customerCallId,
                       Map<String, String> mapCustomizedResultColumn, String jsonCustomerInfo) {

        ServiceResult serviceresult = new ServiceResult();

        multiNumberOutboundDataManage.submitOutboundResult(userId, bizId, importBatchId, customerId,
                Integer.valueOf(strPhoneType), resultCodeType, resultCode, isPreset, presetTime,
                dialType, dialTime, customerCallId, mapCustomizedResultColumn, jsonCustomerInfo);

        serviceresult.setResultCode(ServiceResultCode.SUCCESS);
        return serviceresult.toJson();
    }

    public String startShareBatch( int bizId, String strShareBatchIds) {

        ServiceResult serviceresult = new ServiceResult();

        //List<String> shareBatchIds = new Gson().fromJson(jsonShareBatchIds, List.class);
        List<String> shareBatchIds = new ArrayList<String>();

        String[] arrayShareBatchId = strShareBatchIds.split(",");
        for (String shareBatchId : arrayShareBatchId)
            shareBatchIds.add(shareBatchId);

        multiNumberOutboundDataManage.startShareBatch(bizId, shareBatchIds);

        serviceresult.setResultCode(ServiceResultCode.SUCCESS);
        return serviceresult.toJson();
    }

    public String stopShareBatch( int bizId, String strShareBatchIds) {

        ServiceResult serviceresult = new ServiceResult();

        //List<String> shareBatchIds = new Gson().fromJson(strShareBatchIds, List.class);
        List<String> shareBatchIds = new ArrayList<String>();

        String[] arrayShareBatchId = strShareBatchIds.split(",");
        for (String shareBatchId : arrayShareBatchId)
            shareBatchIds.add(shareBatchId);

        multiNumberOutboundDataManage.stopShareBatch(bizId, shareBatchIds);

        serviceresult.setResultCode(ServiceResultCode.SUCCESS);
        return serviceresult.toJson();
    }

    public void cancelOutboundTask(int bizId, List<CustomerBasic> customerBasicList) {
        multiNumberOutboundDataManage.cancelOutboundTask(bizId, customerBasicList);
    }

}

