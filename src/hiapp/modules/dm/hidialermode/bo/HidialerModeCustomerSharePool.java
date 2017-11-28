package hiapp.modules.dm.hidialermode.bo;

import hiapp.modules.dm.multinumbermode.bo.MultiNumberCustomer;
import hiapp.modules.dm.multinumbermode.bo.MultiNumberPredictStateEnum;
import hiapp.modules.dmmanager.data.DMBizMangeShare;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

@Component
public class HidialerModeCustomerSharePool {

    @Autowired
    private DMBizMangeShare dmBizMangeShare;

    // 客户共享池
    // BizId <==> PriorityBlockingQueue<HidialerModeCustomer>
    Map<Integer, PriorityBlockingQueue<HidialerModeCustomer>> mapPreseCustomerSharePool;
    Map<Integer, PriorityBlockingQueue<HidialerModeCustomer>> mapCustomerSharePool;

    // 用于处理共享停止的客户池，共享批次维度，用于标注已经停止共享的客户
    // ShareBatchId <==> {BizId + ImportId + CustomerId <==> MultiNumberCustomer}
    Map<String, Map<String, HidialerModeCustomer>> mapShareBatchWaitStopCustomerPool;

    public HidialerModeCustomerSharePool() {

        mapPreseCustomerSharePool = new HashMap<Integer, PriorityBlockingQueue<HidialerModeCustomer>>();
        mapCustomerSharePool = new HashMap<Integer, PriorityBlockingQueue<HidialerModeCustomer>>();

        //mapPreseCustomerSharePool = new PriorityBlockingQueue<HidialerModeCustomer>(1, nextDialTimeComparator);
        //mapCustomerSharePool = new PriorityBlockingQueue<HidialerModeCustomer>(1, shareBatchBeginTimeComparator);

        mapShareBatchWaitStopCustomerPool = new HashMap<String, Map<String, HidialerModeCustomer>>();
    }

    public void initialize() {

    }

    public void clear() {

    }

    public void timeoutProc() {
        //customerWaitPool.timeoutProc();
    }

    public HidialerModeCustomer extractCustomer(String userId, Integer bizId) {
        Date now = new Date();
        HidialerModeCustomer shareDataItem = null;

        PriorityBlockingQueue<HidialerModeCustomer> oneBizPresetCustomerPool = mapPreseCustomerSharePool.get(bizId);

        while (true) {
            shareDataItem = oneBizPresetCustomerPool.peek();
            if (null == shareDataItem)
                break;

            if (shareDataItem.getInvalid()) {
                oneBizPresetCustomerPool.poll();  // 扔掉已经停止共享批次的客户
                removeFromShareBatchStopWaitPool(shareDataItem);
                continue;
            }

            if (null != shareDataItem && shareDataItem.getNextDialTime().before(now)) {
                shareDataItem = oneBizPresetCustomerPool.poll();
                removeFromShareBatchStopWaitPool(shareDataItem);
                return shareDataItem;
            }

            break;   //NOTE: 取不到合适的，就跳出
        }

        PriorityBlockingQueue<HidialerModeCustomer> oneBizCustomerPool = mapCustomerSharePool.get(bizId);

        while (true) {
            shareDataItem = oneBizCustomerPool.poll();
            if (null == shareDataItem)
                break;

            removeFromShareBatchStopWaitPool(shareDataItem);

            if (shareDataItem.getInvalid())
                continue;

            return shareDataItem;
        }

        return shareDataItem;
    }

    /*
    public void add(MultiNumberCustomer customer) {
        PriorityBlockingQueue<MultiNumberCustomer> queue;
        if (MultiNumberPredictStateEnum.PRESET_DIAL.equals(customer.getState())
            || MultiNumberPredictStateEnum.WAIT_REDIAL.equals(customer.getState()) ) {
            queue = mapPreseCustomerSharePool.get(customer.getShareBatchId());
            if (null == queue) {
                queue = new PriorityBlockingQueue<MultiNumberCustomer>(1, nextDialTimeComparator);
                mapPreseCustomerSharePool.put(customer.getShareBatchId(), queue);
            }

        } else {
            queue = mapCustomerSharePool.get(customer.getShareBatchId());
            if (null == queue) {
                queue = new PriorityBlockingQueue<MultiNumberCustomer>(1, shareBatchBeginTimeComparator);
                mapCustomerSharePool.put(customer.getShareBatchId(), queue);
            }
        }

        queue.put(customer);
    }
    */

    public void add(HidialerModeCustomer customer) {
        PriorityBlockingQueue<HidialerModeCustomer> queue;
        if (HidialerModeCustomerStateEnum.WAIT_REDIAL.equals(customer.getState()) ) {
            mapPreseCustomerSharePool.get(customer.getBizId()).put(customer);
        } else {
            mapCustomerSharePool.get(customer.getBizId()).put(customer);
        }

        Map<String, HidialerModeCustomer> mapWaitStopPool = mapShareBatchWaitStopCustomerPool.get(customer.getShareBatchId());
        if (null == mapWaitStopPool) {
            mapWaitStopPool = new HashMap<String, HidialerModeCustomer>();
            mapShareBatchWaitStopCustomerPool.put(customer.getShareBatchId(), mapWaitStopPool);
        }
        mapWaitStopPool.put(customer.getBizId() + customer.getImportBatchId() + customer.getCustomerId(), customer);
    }

    /**
     * 仅标注已经停止共享，由于没办法直接从PriorityBlockingQueue里面移除。
     * @param shareBatchIds
     */
    public void stopShareBatch(int bizId, List<String> shareBatchIds) {
        for (String shareBatchId : shareBatchIds) {
            Map<String, HidialerModeCustomer> mapWaitStopPool;
            mapWaitStopPool = mapShareBatchWaitStopCustomerPool.remove(shareBatchId);
            for (HidialerModeCustomer item : mapWaitStopPool.values()) {
                item.setInvalid(true);
            }
        }
    }

    /*
    public void stopShareBatch(List<String> shareBatchIds) {
        removeFromCustomerSharePool(shareBatchIds, mapPreseCustomerSharePool);
        removeFromCustomerSharePool(shareBatchIds, mapCustomerSharePool);
    }

    private void removeFromCustomerSharePool(List<String> shareBatchIds,
                                             Map<String, PriorityBlockingQueue<MultiNumberCustomer>> shareBatchIdVsCustomerMap)  {
        PriorityBlockingQueue<MultiNumberCustomer> queue;
        if (null != shareBatchIdVsCustomerMap) {
            for (String shareBatchId : shareBatchIds) {
                queue = shareBatchIdVsCustomerMap.remove(shareBatchId);
            }
        }
    }*/

    public HidialerModeCustomer removeWaitCustomer(String userId, int bizId, String importBatchId, String customerId) {
        //return customerWaitPool.removeWaitCustomer(userId, bizId, importBatchId, customerId);
        return null;
    }

    public HidialerModeCustomer getWaitCustomer(String userId, int bizId, String importBatchId, String customerId) {
        //TODO
        //return customerWaitPool.getWaitCustome(userId, bizId, importBatchId, customerId);
        return null;
    }

    public void hidialerPhoneConnect(HidialerModeCustomer customer, Date originModifyTime) {
        //customerWaitPool.hidialerPhoneConnect(customer, originModifyTime);
    }

    public void agentScreenPopUp(HidialerModeCustomer customer, Date originModifyTime) {
        //customerWaitPool.agentScreenPopUp(customer, originModifyTime);
    }

    public void markShareBatchStopFromCustomerWaitPool(int bizId, List<String> shareBatchIds) {
        // TODO
        //customerWaitPool.markShareBatchStopFromCustomerWaitPool(bizId, shareBatchIds);
    }


    //////////////////////////////////////////////////////////

    private void removeFromShareBatchStopWaitPool(HidialerModeCustomer customer) {
        Map<String, HidialerModeCustomer> oneShareBatchPool = mapShareBatchWaitStopCustomerPool.get(customer.getShareBatchId());
        oneShareBatchPool.remove(customer.getImportBatchId() + customer.getCustomerId());
        if (oneShareBatchPool.isEmpty())
            mapShareBatchWaitStopCustomerPool.remove(customer.getShareBatchId());
    }

    //匿名Comparator实现
    private static Comparator<HidialerModeCustomer> nextDialTimeComparator = new Comparator<HidialerModeCustomer>() {

        @Override
        public int compare(HidialerModeCustomer c1, HidialerModeCustomer c2) {
            return (c1.getNextDialTime().before(c2.getNextDialTime())) ? 1 : -1;
        }
    };

    //匿名Comparator实现
    private static Comparator<HidialerModeCustomer> shareBatchBeginTimeComparator = new Comparator<HidialerModeCustomer>() {

        @Override
        public int compare(HidialerModeCustomer c1, HidialerModeCustomer c2) {
            return (c1.getShareBatchStartTime().before(c2.getShareBatchStartTime())) ? 1 : -1;
        }
    };

}