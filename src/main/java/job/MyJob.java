package job;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyJob  implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//設定日期格式
        System.out.println(df.format(new Date()));// new Date()為獲取當前系統時間
        System.out.println("MyJob Quartz!");
    }
}
