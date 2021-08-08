package job;


import cloud.sql.Druid;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyJob  implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//設定日期格式
        System.out.println(df.format(new Date()));// new Date()為獲取當前系統時間
        System.out.println("MyJob Quartz!!!");


        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;




        try {
            //取得連接
            conn = Druid.getConn();

			/*
			 *
			 *
				insert into tra01(date, ms,status)
                values(now(), '123','Y');
			 */

            //SQL
            String query = "   insert into tra01(date, ms,status) " +
                    "values(now(), ? , ?) " ;

            //預處理SQL
            preparedStatement = null;
            preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, String.valueOf(new Date().getTime()));
            preparedStatement.setString(2,null);

            //請求
            preparedStatement.executeUpdate();

            // Commit
            conn.commit();



        } catch (Throwable e) {
            if (conn != null) {
                try {
                    //Roll back
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    preparedStatement.close();
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }









        /*
        String LineNotifyAPI = "https://notify-api.line.me/api/notify";
        String Token = "n76OFn5y9GfEjnV00AxdQSI20WpG37IfLjGCF8NWILW";
        Http http = new Http();
        try {
            http.sendPost(LineNotifyAPI, Token, "MyJob line notify test測試" );

            System.out.println("MyJob send ok");
            System.out.println("");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */

    }
}
