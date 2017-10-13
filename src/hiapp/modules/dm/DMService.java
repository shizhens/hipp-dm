package hiapp.modules.dm;

import hiapp.modules.dm.bo.ShareBatchItem;
import hiapp.modules.dm.dao.DMDAO;
import hiapp.modules.dm.multinumbermode.MultiNumberOutboundDataManage;
import hiapp.modules.dm.singlenumbermode.SingleNumberOutboundDataManage;
import hiapp.utils.database.DBConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DMService {

    @Autowired
    SingleNumberOutboundDataManage singleNumberOutboundDataManage;

    @Autowired
    MultiNumberOutboundDataManage multiNumberOutboundDataManage;

    @Autowired
    DMDAO dmDAO;



    Timer dailyTimer;
    TimerTask dailyTimerTask;

    Timer timeoutTimer;
    TimerTask timeoutTimerTask;


    @Autowired
    @Qualifier("tenantDBConnectionPool")
    public void setDBConnectionPool(DBConnectionPool dbConnectionPool) {
        //this.dbConnectionPool = dbConnectionPool;


        singleNumberOutboundDataManage.initialize();
        multiNumberOutboundDataManage.initialize();

        setDailyRoutine();
        setTimeOutRoutine(Constants.timeSlotSpan);

        dailyProc();
    }

    private Boolean shareBatchDailyProc(/*OUT*/List<ShareBatchItem> shareBatchItems) {
        //List<String> expiredShareBatchIds = new ArrayList<String>();
        dmDAO.expireShareBatchsByEndTime(/*expiredShareBatchIds*/);

        dmDAO.activateShareBatchByStartTime();

        Boolean result = dmDAO.getAllActiveShareBatchItems(shareBatchItems);
        return  result;
    }

    private void dailyProc() {
        List<ShareBatchItem> shareBatchItems = new ArrayList<ShareBatchItem>();
        Boolean ret = shareBatchDailyProc(shareBatchItems);

        Map<Integer, List<ShareBatchItem>> mapOutboundModeIdVsShareBatchs;
        mapOutboundModeIdVsShareBatchs = new HashMap<Integer, List<ShareBatchItem>>();

        List<ShareBatchItem> oneModeShareBatchItems;

        // TODO 根据业务类型分类
        for (ShareBatchItem shareBatchItem : shareBatchItems) {
            int outboundModeId = shareBatchItem.getOutboundModeId();
            oneModeShareBatchItems = mapOutboundModeIdVsShareBatchs.get(outboundModeId);
            if (null == oneModeShareBatchItems) {
                oneModeShareBatchItems = new ArrayList<ShareBatchItem>();
                mapOutboundModeIdVsShareBatchs.put(outboundModeId, oneModeShareBatchItems);
            }
            oneModeShareBatchItems.add(shareBatchItem);
        }

        for (Map.Entry<Integer, List<ShareBatchItem>> entry : mapOutboundModeIdVsShareBatchs.entrySet()) {
            Integer modeId = entry.getKey();
            oneModeShareBatchItems = entry.getValue();

            if (3 == modeId)
                singleNumberOutboundDataManage.dailyProc(oneModeShareBatchItems);
            else if (6 == modeId)
                multiNumberOutboundDataManage.dailyProc(oneModeShareBatchItems);

        }
    }

    private void setDailyRoutine() {
        dailyTimer = new Timer();
        dailyTimerTask = new TimerTask() {
            @Override
            public void run() {
                dailyProc();
                //loadCustomersDaily(shareBatchItems);
            }
        };

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 2); // 控制时
        calendar.set(Calendar.MINUTE, 0);      // 控制分
        calendar.set(Calendar.SECOND, 0);      // 控制秒

        dailyTimer.scheduleAtFixedRate(dailyTimerTask, calendar.getTime(), 1000 * 60 * 60 * 24);
    }

    private void setTimeOutRoutine(Long timeSlotSpan) {
        timeoutTimer = new Timer();
        timeoutTimerTask = new TimerTask() {
            @Override
            public void run() {
                singleNumberOutboundDataManage.timeoutProc();
                multiNumberOutboundDataManage.timeoutProc();
            }
        };

        dailyTimer.scheduleAtFixedRate(timeoutTimerTask, timeSlotSpan, timeSlotSpan);
    }

}