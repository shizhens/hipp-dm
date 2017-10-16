package hiapp.modules.dm.multinumbermode.bo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MultiNumberPredictCustomerSharePool {

    @Autowired
    PhoneTypeDialSequence phoneTypeDialSequence;
    
    // bizId <==> {号码类型 <==> 相应拨打号码的客户池}
    Map<Integer, Map<Integer, OnePhoneTypeCustomerPool>> mapCustomerManage;

    public MultiNumberCustomer extractCustomer(String userId, int bizId) {
        MultiNumberCustomer customer;

        for (int dialIndex = 1; dialIndex <= phoneTypeDialSequence.getPhoneTypeNum(bizId); dialIndex++) {
            int phoneType = phoneTypeDialSequence.getPhoneType(bizId, dialIndex);

            Map<Integer, OnePhoneTypeCustomerPool> oneBizCustomerSharePool = mapCustomerManage.get(bizId);
            OnePhoneTypeCustomerPool onePhoneTypeCustomerPool = oneBizCustomerSharePool.get(phoneType);
            customer = onePhoneTypeCustomerPool.extractCustomer(userId);
            if (null != customer)
                return customer;
        }

        return null;
    }

    public void add(MultiNumberCustomer customer) {

        Map<Integer, OnePhoneTypeCustomerPool> oneBizCustomerSharePool = mapCustomerManage.get(customer.getBizId());
        if (null == oneBizCustomerSharePool) {
            oneBizCustomerSharePool = new HashMap<Integer, OnePhoneTypeCustomerPool>();
            mapCustomerManage.put(customer.getBizId(), oneBizCustomerSharePool);
        }

        OnePhoneTypeCustomerPool onePhoneTypeCustomerPool = oneBizCustomerSharePool.get(customer.getNextDialPhoneType());
        if (null == onePhoneTypeCustomerPool) {
            onePhoneTypeCustomerPool = new OnePhoneTypeCustomerPool(customer.getBizId(), customer.getNextDialPhoneType());
            oneBizCustomerSharePool.put(customer.getNextDialPhoneType(), onePhoneTypeCustomerPool);
        }

        onePhoneTypeCustomerPool.add(customer);
    }

    public void addWaitResultCustomer(MultiNumberCustomer customer) {
        Map<Integer, OnePhoneTypeCustomerPool> oneBizCustomerSharePool = mapCustomerManage.get(customer.getBizId());
        if (null == oneBizCustomerSharePool)
            return;

        OnePhoneTypeCustomerPool onePhoneTypeCustomerPool = oneBizCustomerSharePool.get(customer.getCurDialPhoneType());
        if (null == onePhoneTypeCustomerPool)
            return;

        onePhoneTypeCustomerPool.addWaitResultCustomer(customer);
    }


    public void clear() {
        mapCustomerManage.clear();
    }

    public MultiNumberCustomer removeWaitCustomer(String userId, int bizId, String importBatchId, String customerId, int phoneType) {
        Map<Integer, OnePhoneTypeCustomerPool> oneBizCustomerSharePool = mapCustomerManage.get(bizId);

        OnePhoneTypeCustomerPool onePhoneTypeCustomerPool = oneBizCustomerSharePool.get(phoneType);

        onePhoneTypeCustomerPool.removeWaitCustomer(userId, bizId, importBatchId, customerId);
        return null;
    }

    public MultiNumberCustomer getWaitCustomer(String userId, int bizId, String importBatchId, String customerId, int phoneType) {
        Map<Integer, OnePhoneTypeCustomerPool> oneBizCustomerSharePool = mapCustomerManage.get(bizId);

        OnePhoneTypeCustomerPool onePhoneTypeCustomerPool = oneBizCustomerSharePool.get(phoneType);

        onePhoneTypeCustomerPool.getWaitCustomer(userId, bizId, importBatchId, customerId);
        return null;
    }

    void initialize() {

        /*
        for (int dialIndex = 1; dialIndex <= phoneTypeDialSequence.size(); dialIndex++) {
            OnePhoneTypeCustomerPool onePhoneTypeCustomerPool = new OnePhoneTypeCustomerPool(bizId, dialIndex);
            mapCustomerManage.put(dialIndex, onePhoneTypeCustomerPool);

            //bizCustomerSharePool;
        }*/

    }

}
