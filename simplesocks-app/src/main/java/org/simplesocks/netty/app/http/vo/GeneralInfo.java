package org.simplesocks.netty.app.http.vo;

import com.sun.management.OperatingSystemMXBean;
import lombok.Data;
import org.simplesocks.netty.app.AppManager;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

@Data
public class GeneralInfo {
    private String operatingSystemName;
    private String totalPhysicalMemory;
    private String freePhysicalMemory;
    private String vmUsed;
    private String vmFree;
    private String vmTotal;
    private String vmMax;
    private int totalActiveThread;  //用户线程
    private String version;
    private String startTime;

    static final BigDecimal MB = BigDecimal.valueOf(1024*1024);
    static final int MB_I =  1024*1024;


    public static GeneralInfo snapshot(){
        GeneralInfo i = new GeneralInfo();
        i.operatingSystemName = currentOperatingSystemName();
        i.totalPhysicalMemory = getTotalMemoryString();
        i.freePhysicalMemory = getFreeTotalMemoryString();
        Runtime rt = Runtime.getRuntime();
        i.vmTotal = toMB(rt.totalMemory());
        i.vmFree = toMB(rt.freeMemory());
        i.vmMax = toMB(rt.maxMemory());
        long used = rt.totalMemory() - rt.freeMemory();
        i.vmUsed = toMB(used);
        i.totalActiveThread = getTotalActiveThreadNumber();
        i.version = AppManager.VERSION;
        i.startTime = AppManager.START_TIME.toString().replace("T"," ").substring(0,"2019-00-00 11:11:11".length());
        return i;
    }

    private static String toMB(long total){
        BigDecimal r = BigDecimal.valueOf(total).divide(MB, 2, RoundingMode.HALF_UP);
        return r.toString()+" MB";
    }



    public static String currentOperatingSystemName(){
        return System.getProperty("os.name");
    }

    public static String getTotalMemoryString(){
        return getMemoryString(OperatingSystemMXBean::getTotalPhysicalMemorySize);
    }

    public static String getFreeTotalMemoryString(){
        return getMemoryString(OperatingSystemMXBean::getFreePhysicalMemorySize);
    }

    private static String getMemoryString(Function<OperatingSystemMXBean,Long> function){
        OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        Long size = function.apply(bean);
        long mb = size/MB_I;
        if(mb<1024){
            return mb+" MB";
        }
        BigDecimal gb = BigDecimal.valueOf(mb).divide(BigDecimal.valueOf(1024), 2, RoundingMode.HALF_UP);
        return gb.toString()+" GB";
    }

    private static int getTotalActiveThreadNumber(){
        ThreadGroup parentThread;
        int totalThread = 0;
        for (parentThread = Thread.currentThread().getThreadGroup(); parentThread
                .getParent() != null; parentThread = parentThread.getParent()) {
            totalThread = parentThread.activeCount();
        }
        return totalThread;
    }

}
