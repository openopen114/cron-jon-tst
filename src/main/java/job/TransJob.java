package job;


import cloud.sql.Druid;
import http.Http;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TransJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//設定日期格式
        System.out.println(df.format(new Date()));// new Date()為獲取當前系統時間
        System.out.println("TranJob Quartz!!!");


        Connection conn = null;
        PreparedStatement psmt = null;
        ResultSet resultSet = null;





        /******** 1. 標註處理中 ********/
        updateStatus("NULL_TO_X", "ALL");
        System.out.println("====> 標註處理中 NULL_TO_X ");







        try {
            //取得連接
            conn = Druid.getConn();
            conn.setAutoCommit(false);


            /******** 2. 取得處理中 X 清單 ********/

			/*
			 *
			 * 
                SELECT
                    *
                FROM
                    TRA01
                WHERE
                    STATUS = 'X'
                ORDER BY
                    DATE
                ;
			 */

            //SQL
            String query = "    " +
                    "SELECT " +
                    "  * " +
                    "FROM " +
                    "  TRA01 " +
                    "WHERE " +
                    "  STATUS = 'X' " +
                    "ORDER BY " +
                    "  DATE " ;

            //預處理SQL
            psmt = null;
            psmt = conn.prepareStatement(query);

            //請求
            resultSet = psmt.executeQuery();

            //結果集
            /******** 3. for each 轉檔 ********/
            while (resultSet.next()) {
                doTrans(resultSet.getString("ms"));

            }


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
                    psmt.close();
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }





        //發送 Line notify
        sendLineNotify("===> OK");














    }






    //更新處理狀態
    // NULL_TO_X:處理中
    // X_TO_Y:處理成功
    // X_TO_N:處理失敗
    public void updateStatus(String _action, String _id){
        Connection conn = null;
        PreparedStatement  psmt = null;
        ResultSet resultSet = null;

        //Table Name
        String table = "tra01";
        try {
            //取得連接
            conn = Druid.getConn();
            conn.setAutoCommit(false);

			/*
			 *
			 *
                update tra01
                set status = 'X'
                where status is null;
			 */

            //SQL
            String query = "update " + table;


            // NULL_TO_X:處理中
            // X_TO_Y:處理成功
            // X_TO_N:處理失敗
            switch (_action) {

                case "NULL_TO_X":
                    query += " set  status = 'X'  where status is null ";
                    break;
                case "X_TO_Y":
                    query += " set  status = 'Y'  where ms = '" + _id + "' ";
                    break;
                case "X_TO_N":
                    query += " set  status = 'N'  where ms = '" + _id + "' ";
                    break;
            }


            System.out.println(query);

            //預處理SQL
             psmt = null;
             psmt = conn.prepareStatement(query);

            //請求
             psmt.executeUpdate();

            // Commit
            conn.commit();



        } catch (Throwable e) {
            e.printStackTrace();
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
                    psmt.close();
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }





    //發送 Line notify
    public void sendLineNotify(String _msg){
        String LineNotifyAPI = "https://notify-api.line.me/api/notify";
        String Token = "n76OFn5y9GfEjnV00AxdQSI20WpG37IfLjGCF8NWILW";
        Http http = new Http();
        try {
            http.sendPost(LineNotifyAPI, Token, _msg );
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // 轉檔
    public void doTrans(String _ms){

        System.out.println("===> do trans "+ _ms);

        Connection conn = null;
        PreparedStatement  psmt = null;
        ResultSet resultSet = null;


        String transResult ="";

        try {
            //取得連接
            conn = Druid.getConn();
            conn.setAutoCommit(false);

			/*
			 *
			 *
               update tra01 
                set ms = '### ' ||  ms,
                status = 'X'
                where ms  = '123';
			 */

            //SQL
            String query = "update tra01  " +
                    "set ms = '### ' ||  ms, " +
                    "status = 'X' " +
                    "where ms  =  ? ";


            //預處理SQL
            psmt = null;
            psmt = conn.prepareStatement(query);
            psmt.setString(1, _ms);

            //請求
            psmt.executeUpdate();

            // Commit
            conn.commit();


            transResult ="X_TO_Y";


            System.out.println("====> do thans :" + _ms + " OK ");

        } catch (Throwable e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    //Roll back
                    conn.rollback();
                    //標註處理失敗
                    transResult ="X_TO_N";

                    //發送 Line notify
                    sendLineNotify("標註處理失敗:" + _ms);

                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    psmt.close();
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


        updateStatus(transResult, "### " + _ms);





    }
}

