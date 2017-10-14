package hiapp.modules.dm.multinumbermode;

import hiapp.modules.dm.Constants;
import hiapp.modules.dm.bo.ShareBatchItem;
import hiapp.modules.dm.dao.DMDAO;
import hiapp.modules.dm.multinumbermode.bo.*;
import hiapp.modules.dm.multinumbermode.dao.MultiNumberPredictModeDAO;
import hiapp.modules.dm.singlenumbermode.bo.*;
import hiapp.modules.dm.util.DateUtil;
import hiapp.modules.dmmanager.data.DataImportJdbc;
import hiapp.modules.dmsetting.DMBizPresetItem;
import hiapp.modules.dmsetting.DMPresetStateEnum;
import hiapp.modules.dmsetting.data.DmBizOutboundConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MultiNumberOutboundDataManage {

    @Autowired
    MultiNumberPredictModeDAO multiNumberPredictModeDAO;

    @Autowired
    DMDAO dmDAO;

    @Autowired
    MultiNumberPredictCustomerSharePool customerSharePool;

    @Autowired
    private DmBizOutboundConfigRepository dmBizOutboundConfig;

    @Autowired
    private DataImportJdbc dataImportJdbc;



    Map<Integer, EndCodeRedialStrategyM6> mapEndCodeRedialStrategy;

    Timer dailyTimer;
    TimerTask dailyTimerTask;

    Timer timeoutTimer;
    TimerTask timeoutTimerTask;


    public synchronized MultiNumberCustomer extractNextOutboundCustomer(String userId, int bizId) {
        return customerSharePool.extractCustomer(userId, bizId);
    }

    public String submitHiDialerOutboundResult(String userId, int bizId, String importBatchId, String customerId,
                                       String resultCodeType, String resultCode, Date presetTime, String resultData,
                                       String customerInfo) {
        return "";
    }

    public String submitOutboundResult(String userId, int bizId, String importBatchId, String customerId, int phoneType,
                                       String resultCodeType, String resultCode, Date presetTime, String resultData,
                                       String customerInfo) {
        String dialType = "xxx";
        String customerCallId = "xxx";
        Date dialTime = new Date();

        //presetTime = DateUtil.getNextXDay(5);
        //resultCodeType = "EndType-LostCall";
        //resultCode = "未接通拨打";

        MultiNumberCustomer originCustomerItem = customerSharePool.removeWaitCustomer(userId, bizId, importBatchId, customerId, phoneType);

        EndCodeRedialStrategyM6 endCodeRedialStrategy = mapEndCodeRedialStrategy.get(bizId);
        if (null == endCodeRedialStrategy) {
            endCodeRedialStrategy = getEndCodeRedialStrategyByBizId(bizId);
            mapEndCodeRedialStrategy.put(bizId, endCodeRedialStrategy);
        }

        // 经过 Outbound 策略处理器
        procEndcode(userId, originCustomerItem, endCodeRedialStrategy, resultCodeType, resultCode, presetTime, resultData);

        // 插入结果表
        //dataImportJdbc.insertDataToResultTable(bizId, shareBatchId, importBatchId, customerId, userId, resultData);
        dmDAO.updateDMResult(bizId, originCustomerItem.getShareBatchId(), importBatchId, customerId, originCustomerItem.getModifyId()); // MODIFYLAST 0
        dmDAO.insertDMResult(bizId, originCustomerItem.getShareBatchId(), importBatchId, customerId,
                originCustomerItem.getModifyId() + 1, userId, dialType, dialTime, customerCallId);

        // 插入导入客户表
        dataImportJdbc.insertDataToImPortTable(bizId, importBatchId, customerId, userId, customerInfo, originCustomerItem.getModifyId() + 1);
        return "";
    }

    /*
     * 重新初始化处理不适合，举例说明：系统运行中，进行启用共享批次操作，会导致获取外呼客户的功能暂停一段时间。
     *
     */
    public String startShareBatch(int bizId, List<String> shareBatchIds) {
        return "";
    }

    public void stopShareBatch(int bizId, List<String> shareBatchIds) {
    }

    public String appendCustomersToShareBatch(int bizId, List<String> shareBatchIds) {
        return "";
    }

    // 用户登录通知
    public void onLogin(String userId) {
    }


    public void initialize() {

    }

    public void dailyProc(List<ShareBatchItem> shareBatchItems) {
        customerSharePool.clear();

        loadCustomersDaily(shareBatchItems);
    }

    public void timeoutProc() {
        Date now =  new Date();
        Long curTimeSlot = now.getTime()/ Constants.timeSlotSpan;
        Long timeoutTimeSlot = curTimeSlot - Constants.timeoutThreshold/Constants.timeSlotSpan;

        /*
        while (earliestTimeSlot < timeoutTimeSlot) {
            Map<String, MultiNumberCustomer> mapTimeSlotWaitTimeOutPool;
            mapTimeSlotWaitTimeOutPool =  mapWaitTimeOutCustomerPool.get(earliestTimeSlot);

            for (MultiNumberCustomer customerItem : mapTimeSlotWaitTimeOutPool.values()) {
                // 放回客户共享池
                if (!customerItem.getInvalid()) {
                    addCustomerToSharePool(customerItem);
                }

                removeWaitResultCustome(customerItem.getUserId(), customerItem.getBizId(), customerItem.getImportBatchId(), customerItem.getCustomerId());

                removeWaitStopCustomer( customerItem.getBizId(), customerItem.getShareBatchId(), customerItem.getImportBatchId(),
                        customerItem.getCustomerId());
            }
        }*/
    }


    /////////////////////////////////////////////////////////////

    private void procEndcode(String userId, MultiNumberCustomer originCustomerItem,
                             EndCodeRedialStrategyM6 endCodeRedialStrategy,
                             String resultCodeType, String resultCode, Date presetTime, String resultData) {

        Date now = new Date();

        EndCodeRedialStrategyM6 endCodeRedialStrategyM6 = mapEndCodeRedialStrategy.get(originCustomerItem.getBizId());
        EndCodeRedialStrategyM6Item strategyItem = endCodeRedialStrategyM6.getEndCodeRedialStrategyItem(resultCodeType, resultCode);

        Boolean customerDialFinished;
        Boolean phoneTypeDialFinished;
        int redialDelayMinutes;
        int maxRedialNum;
        Boolean presetDial;


        MultiNumberCustomer item = new MultiNumberCustomer();
        item.setBizId(originCustomerItem.getBizId());
        item.setShareBatchId(originCustomerItem.getShareBatchId());
        item.setImportBatchId(originCustomerItem.getImportBatchId());
        item.setCustomerId(originCustomerItem.getCustomerId());
        item.setEndCodeType(resultCodeType);
        item.setEndCode(resultCode);
        item.setModifyUserId(userId);
        item.setModifyTime(now);
        item.setModifyId(originCustomerItem.getModifyId() + 1);
        item.setCurDialPhoneType(originCustomerItem.getCurDialPhoneType());
        item.setCurDialPhone(originCustomerItem.getCurDialPhone());

        PhoneDialInfo originPhoneDialInfo = originCustomerItem.getDialInfo(originCustomerItem.getCurDialPhoneType());
        originPhoneDialInfo.setDialCount( originPhoneDialInfo.getDialCount() + 1);
        originPhoneDialInfo.setLastDialTime(now);
        item.setDialInfo(originCustomerItem.getCurDialPhoneType(), originPhoneDialInfo);
        item.setCurPhoneDialSequence(customerSharePool.getPhoneDialSequence(originCustomerItem.getCurDialPhoneType()));

        item.setShareBatchStartTime(originCustomerItem.getShareBatchStartTime());

        if (strategyItem.getCustomerDialFinished()) {
            item.setState(MultiNumberPredictStateEnum.FINISHED);

            // 更新共享状态表
            multiNumberPredictModeDAO.updateCustomerShareState(item);

            // 插入共享历史表
            multiNumberPredictModeDAO.insertCustomerShareStateHistory(item);

        } else if (strategyItem.getPresetDial()) {
            item.setState(MultiNumberPredictStateEnum.PRESET_DIAL);

            // 更新共享状态表
            item.setCurPresetDialTime(presetTime);
            multiNumberPredictModeDAO.updateCustomerShareState(item);

            // 插入共享历史表
            multiNumberPredictModeDAO.insertCustomerShareStateHistory(item);

            // 插入预约表
            DMBizPresetItem presetItem = new DMBizPresetItem();
            presetItem.setSourceId(originCustomerItem.getShareBatchId());
            presetItem.setCustomerId(originCustomerItem.getCustomerId());
            presetItem.setImportId(originCustomerItem.getImportBatchId());
            presetItem.setPresetTime(presetTime);
            presetItem.setState(DMPresetStateEnum.InUse.getStateName());
            presetItem.setComment("xxx");
            presetItem.setModifyId(item.getModifyId());
            presetItem.setModifyLast(1);
            presetItem.setModifyUserId(userId);
            presetItem.setModifyTime(now);
            presetItem.setModifyDesc("xxx");
            presetItem.setPhoneType("xxx");
            dmDAO.insertPresetItem(originCustomerItem.getBizId(), presetItem);

            // 不要移出候选池，预约在今天
            if (DateUtil.isSameDay(now, presetTime)) {
                addCustomerToSharePool(item);
            }

        } else if (strategyItem.getPhoneTypeDialFinished()) {
            item.setState(MultiNumberPredictStateEnum.WAIT_DIAL);

            int nextDialPhoneType = customerSharePool.getNextDialPhoneType(item.getCurDialPhoneType());
            item.setNextDialPhoneType(nextDialPhoneType);

            multiNumberPredictModeDAO.updateCustomerShareState(item);

            // 插入共享历史表
            multiNumberPredictModeDAO.insertCustomerShareStateHistory(item);

        } else /*当前号码类型重拨*/ {

            PhoneDialInfo phoneDialInfo = item.getDialInfo(item.getCurDialPhoneType());

            if ((phoneDialInfo.getDialCount() - phoneDialInfo.getCausePresetDialCount()) >= strategyItem.getMaxRedialNum()) {
                item.setState(MultiNumberPredictStateEnum.WAIT_DIAL);
                int nextDialPhoneType = customerSharePool.getNextDialPhoneType(item.getCurDialPhoneType());
                item.setNextDialPhoneType(nextDialPhoneType);
            } else {
                item.setState(MultiNumberPredictStateEnum.WAIT_REDIAL);
                item.setCurPresetDialTime(DateUtil.getNextXMinute(strategyItem.getRedialDelayMinutes()));
            }

            addCustomerToSharePool(item);

            multiNumberPredictModeDAO.updateCustomerShareState(item);

            // 插入共享历史表
            multiNumberPredictModeDAO.insertCustomerShareStateHistory(item);
        }

        //若是当前是预约拨打，更新 预约状态 @ 预约表
        if (SingleNumberModeShareCustomerStateEnum.PRESET_DIAL.equals(originCustomerItem.getState())) {
            dmDAO.updatePresetState(item.getBizId(), item.getShareBatchId(), item.getImportBatchId(),
                    item.getCustomerId(), originCustomerItem.getModifyId(),
                    DMPresetStateEnum.FinishPreset.getStateName());
        }

    }


    //
    private void loadCustomersDaily(List<ShareBatchItem> shareBatchItems) {

        Date now = new Date();

        // 根据BizId, 归类共享客户数据
        Map<Integer, List<ShareBatchItem>> mapBizIdVsShareBatchItem = new HashMap<Integer, List<ShareBatchItem>>();
        for (ShareBatchItem shareBatchItem : shareBatchItems) {
            List<ShareBatchItem> shareBatchItemList = mapBizIdVsShareBatchItem.get(shareBatchItem.getBizId());
            if (null == shareBatchItemList) {
                shareBatchItemList = new ArrayList<ShareBatchItem>();
                mapBizIdVsShareBatchItem.put(shareBatchItem.getBizId(), shareBatchItemList);
            }
            shareBatchItemList.add(shareBatchItem);
        }

        List<MultiNumberPredictStateEnum> shareCustomerStateList = new ArrayList<MultiNumberPredictStateEnum>();
        shareCustomerStateList.add(MultiNumberPredictStateEnum.CREATED);
        shareCustomerStateList.add(MultiNumberPredictStateEnum.APPENDED);
        shareCustomerStateList.add(MultiNumberPredictStateEnum.WAIT_DIAL);
        shareCustomerStateList.add(MultiNumberPredictStateEnum.WAIT_REDIAL);
        shareCustomerStateList.add(MultiNumberPredictStateEnum.LOSS_WAIT_REDIAL);
        shareCustomerStateList.add(MultiNumberPredictStateEnum.REVERT);

        List<MultiNumberPredictStateEnum> shareCustomerStateList2 = new ArrayList<MultiNumberPredictStateEnum>();
        shareCustomerStateList2.add(MultiNumberPredictStateEnum.PRESET_DIAL);

        List<MultiNumberCustomer> shareDataItems = new ArrayList<MultiNumberCustomer>();
        for (Map.Entry<Integer, List<ShareBatchItem>> entry : mapBizIdVsShareBatchItem.entrySet()) {

            shareDataItems.clear();

            int bizId = entry.getKey();
            List<ShareBatchItem> givenBizShareBatchItems = entry.getValue();


            // 根据未接通拨打日期，决定是否清零<当日未接通重拨次数>
            //singleNumberModeDAO.clearPreviousDayLostCallCount(bizId);

            // 成批从DB取数据
            Boolean result = multiNumberPredictModeDAO.getGivenBizCustomersByState(
                    bizId, givenBizShareBatchItems, shareCustomerStateList, shareDataItems);

            // 成批从DB取数据, 根据nextDialTime
            result = multiNumberPredictModeDAO.getGivenBizCustomersByStateAndNextDialTime(
                    bizId, givenBizShareBatchItems, shareCustomerStateList2, shareDataItems);


            // 收集客户共享状态为 MultiNumberPredictStateEnum.APPENDED 的客户信息
            // 后续需要更改状态为 MultiNumberPredictStateEnum.CREATED
            List<String> appendedStateCustomerIdList = new ArrayList<String>();

            for (MultiNumberCustomer customerItem : shareDataItems) {
                addCustomerToSharePool(customerItem);

                if (MultiNumberPredictStateEnum.APPENDED.equals(customerItem.getState())) {
                    appendedStateCustomerIdList.add(customerItem.getShareBatchId());
                }
            }

            multiNumberPredictModeDAO.updateCustomerShareState(bizId, appendedStateCustomerIdList,
                                                    MultiNumberPredictStateEnum.CREATED);
        }
    }

    // 用于共享批次的启用，根据共享批次，不会导致重复加载
    private void loadCustomersIncremental(List<ShareBatchItem> shareBatchItems) {

        Date now = new Date();

        // 根据BizId, 归类共享客户数据
        Map<Integer, List<ShareBatchItem>> mapBizIdVsShareBatchItem = new HashMap<Integer, List<ShareBatchItem>>();
        for (ShareBatchItem shareBatchItem : shareBatchItems) {
            List<ShareBatchItem> shareBatchItemList = mapBizIdVsShareBatchItem.get(shareBatchItem.getBizId());
            if (null == shareBatchItemList) {
                shareBatchItemList = new ArrayList<ShareBatchItem>();
                mapBizIdVsShareBatchItem.put(shareBatchItem.getBizId(), shareBatchItemList);
            }
            shareBatchItemList.add(shareBatchItem);
        }

        List<MultiNumberPredictStateEnum> shareCustomerStateList = new ArrayList<MultiNumberPredictStateEnum>();
        shareCustomerStateList.add(MultiNumberPredictStateEnum.CREATED);
        //shareCustomerStateList.add(MultiNumberPredictStateEnum.APPENDED);
        shareCustomerStateList.add(MultiNumberPredictStateEnum.WAIT_DIAL);
        shareCustomerStateList.add(MultiNumberPredictStateEnum.WAIT_REDIAL);
        shareCustomerStateList.add(MultiNumberPredictStateEnum.LOSS_WAIT_REDIAL);
        shareCustomerStateList.add(MultiNumberPredictStateEnum.REVERT);

        List<MultiNumberPredictStateEnum> shareCustomerStateList2 = new ArrayList<MultiNumberPredictStateEnum>();
        shareCustomerStateList2.add(MultiNumberPredictStateEnum.PRESET_DIAL);

        List<MultiNumberCustomer> shareDataItems = new ArrayList<MultiNumberCustomer>();
        for (Map.Entry<Integer, List<ShareBatchItem>> entry : mapBizIdVsShareBatchItem.entrySet()) {

            shareDataItems.clear();

            int bizId = entry.getKey();
            List<ShareBatchItem> givenBizShareBatchItems = entry.getValue();


            // 成批从DB取数据
            Boolean result = multiNumberPredictModeDAO.getGivenBizCustomersByState(
                    bizId, givenBizShareBatchItems, shareCustomerStateList, shareDataItems);

            // 成批从DB取数据, 根据nextDialTime
            result = multiNumberPredictModeDAO.getGivenBizCustomersByStateAndNextDialTime(
                    bizId, givenBizShareBatchItems, shareCustomerStateList2, shareDataItems);

            for (MultiNumberCustomer customerItem : shareDataItems) {
                addCustomerToSharePool(customerItem);
            }

        }
    }

    private void addCustomerToSharePool(MultiNumberCustomer newCustomerItem) {
        customerSharePool.add(newCustomerItem);

        System.out.println("add multinumber customer: bizId[" + newCustomerItem.getBizId()
                + "] shareId[" + newCustomerItem.getShareBatchId() + "] IID[" + newCustomerItem.getImportBatchId()
                + "] CID[" + newCustomerItem.getCustomerId() + "]");
    }


    private EndCodeRedialStrategyM6 getEndCodeRedialStrategyByBizId(int bizId) {
        String jsonEndCodeRedialStrategy = dmBizOutboundConfig.dmGetAllBizOutboundSetting(bizId);

        //EndCodeRedialStrategyFromDB endCodeRedialStrategyFromDB = new Gson().fromJson(jsonEndCodeRedialStrategy,
        //        EndCodeRedialStrategyFromDB.class);

        //return hiapp.modules.dm.multinumbermode.bo.EndCodeRedialStrategyM6.getInstance(endCodeRedialStrategyFromDB);
        return null;
    }

}
