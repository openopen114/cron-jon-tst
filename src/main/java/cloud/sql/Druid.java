package cloud.sql;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * https://blog.csdn.net/LxxImagine/article/details/81604408
 */

public class Druid {


    // druid 連接池
    private static DruidDataSource pool;
    
    
    
    /** APIENV **/
    private static String APIENV;

    /** 資料庫  URL **/
    private static String url;
    /**帳號**/
    private static String username;
    /**密碼**/
    private static String password;
    /**socketFactory**/
    private static String socketFactory;
    /**cloudSqlInstance**/
    private static String cloudSqlInstance;
    /**初始連接數**/
    private static int initialSize;
    /**最大活動連接數**/
    private static int maxActive;
    /**最小閒置連接數**/
    private static int minIdle;
    /**連接timeout**/
    private static long maxWait;

    private static String fileName = "/DB.properties";

    static {
        init();
    }

    /**
     * 加载属性文件并读取属性文件中的内容将其设置给连接信息
     * @param propName
     */
    private static void loadProp(String propName) {
        fileName = propName;
        try {
            InputStream is = Druid.class.getResourceAsStream(fileName);
            Properties p = new Properties();
            p.load(is);
            
            
            
            APIENV = p.getProperty("APIENV");
            
            
            if(APIENV.equals("dev")) {
            	//開發環境
            	String dbHost = p.getProperty("jdbc.dbHost");
                String dbPort = p.getProperty("jdbc.dbPort");
                String dbSID = p.getProperty("jdbc.dbSID");
                
                // Set up URL parameters
                String connectionString = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbSID;
                url = connectionString;
            }else {
            	//cloud 環境
            	// Set up URL parameters
                String connectionString = "jdbc:postgresql:///postgres";
                url = connectionString;
            }
 
 
            
            System.out.println("==> url : [" + APIENV + "] " + url );
            
            //設定其他參數
            username = p.getProperty("jdbc.username");
            password = p.getProperty("jdbc.password"); 
            socketFactory = p.getProperty("jdbc.socketFactory");
            cloudSqlInstance = p.getProperty("jdbc.cloudSqlInstance");
            initialSize = Integer.parseInt(p.getProperty("initialSize"));
            maxActive = Integer.parseInt(p.getProperty("maxActive"));
            maxWait = Integer.parseInt(p.getProperty("maxWait"));
            minIdle = Integer.parseInt(p.getProperty("minIdle"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void init() {
        pool = new DruidDataSource();
        //加载属性文件,初始化配置
        loadProp(fileName); 

               
        if(APIENV.equals("dev")) {
        	//開發環境
        	 pool.setUrl(url);
        	 pool.setUsername(username);
        	 pool.setPassword(password); 
        }else {
        	//cloud 環境 
        	Properties connProps = new Properties();
            connProps.setProperty("user", "postgres");
            connProps.setProperty("password", "postgres");
            connProps.setProperty("sslmode", "disable");
            connProps.setProperty("socketFactory", socketFactory);
            connProps.setProperty("cloudSqlInstance", cloudSqlInstance);
            connProps.setProperty("connectTimeout","60");
            connProps.setProperty("socketTimeout", "60");
            connProps.setProperty("loginTimeout", "60");
            pool.setUrl(url);
            pool.setConnectProperties(connProps);
        }




        

        //设置连接池中初始连接数
        pool.setInitialSize(initialSize);
        //设置最大连接数
        pool.setMaxActive(maxActive);
        //设置最小的闲置链接数
        pool.setMinIdle(minIdle);
        //设置最大的等待时间(等待获取链接的时间)
        pool.setMaxWait(maxWait);
    }

    /**
     * 链接获取
     * @return
     */
    public static Connection getConn() {
        try {
            //如果连接池为空或者被异常关闭,则重新初始化一个
            if(pool == null || pool.isClosed()) {
                init();
            }
            return pool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }



    /**
     * 资源关闭
     *
     * @param stmt
     * @param conn
     */
    public static void close(Statement stmt, Connection conn) {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 連接資訊
     * @return
     */
    public static String getConnectInfo() {

        JsonObject obj = new JsonObject();
        obj.addProperty("getActiveCount", pool.getActiveCount());
        obj.addProperty("getConnectCount", pool.getConnectCount());
        obj.addProperty("getMinIdle", pool.getMaxIdle());
        obj.addProperty("getConnectErrorCount", pool.getConnectErrorCount());
        obj.addProperty("getActivePeak", pool.getActivePeak());
        obj.addProperty("getMaxActive", pool.getMaxActive());
        obj.addProperty("getCreateCount", pool.getCreateCount());
        obj.addProperty("getCloseCount", pool.getCloseCount());
        obj.addProperty("getUrl", pool.getUrl());


        String json = new Gson().toJson(obj);
        return json;

    }



}
