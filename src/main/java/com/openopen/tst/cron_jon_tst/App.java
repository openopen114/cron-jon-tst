package com.openopen.tst.cron_jon_tst;


import job.MyJob;
import job.TransJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;


/**
 * Hello world!
 * http://puremonkey2010.blogspot.com/2014/07/java-quartz-scheduler-22.html
 * http://tw.gitbook.net/quartz/quartz-helloworld.html
 */
public class App {
    public static void main( String[] args ) throws SchedulerException {
        System.out.println( "Hello World!" );



        // [0] Grab the Scheduler instance from the Factory
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        // and start it off
        scheduler.start();

        // [1] define the job
        JobDetail myJob = newJob(MyJob.class)
                .withIdentity("MyJob", "group1")
                .build();




        JobDetail transJob = newJob(TransJob.class)
                .withIdentity("TransJob", "group1")
                .build();




        // [2] define the Trigger
        Trigger triggerMy = newTrigger()
                .withIdentity("triggerMy", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
                .forJob(myJob)
                .build();


        Trigger triggerTransJob = newTrigger()
                .withIdentity("triggerTransJob", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule("0/10 * * * * ?"))
                .forJob(transJob)
                .build();



        // [3] define the scheduleJob
        scheduler.scheduleJob(myJob, triggerMy);
        scheduler.scheduleJob(transJob, triggerTransJob);



    }
}
