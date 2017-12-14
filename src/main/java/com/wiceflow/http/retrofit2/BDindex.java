package com.wiceflow.http.retrofit2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wiceflow.frame.quartz2.RunJob;
import com.wiceflow.json.fastjson.po.BasicCTwo;
import com.wiceflow.json.fastjson.po.TableForJSON;
import com.wiceflow.json.fastjson.po.*;
import okhttp3.ResponseBody;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.Date;

import static com.wiceflow.json.fastjson.po.ArrayTurnPO.trun;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by BF on 2017/12/11.
 * 北斗--指数系统优化
 * 加入定时器
 */
public class BDindex {

    private static final Logger _LOG = LoggerFactory.getLogger(BDindex.class);

    static private General translateObject(String jsonString) {
        General basic = JSON.parseObject(jsonString, General.class);
        return basic;
    }

    static private Translationl getTranslation1(String jsonString) {
        Translationl t = JSON.parseObject(jsonString, Translationl.class);
        return t;
    }

    static public void getData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://202.104.124.214:13401/")
                .build();
        IndexSystemHttpJSON ishj = retrofit.create(IndexSystemHttpJSON.class);
        Call<ResponseBody> call = ishj.getJSON("20171212");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject json = JSON.parseObject(new String(response.body().bytes()));
                    JSONObject json1 = json.getJSONObject("table");
                    table table = JSON.parseObject(json1.toJSONString(), table.class);
                    TableForJSON t = trun(table);
                    System.out.println(t);
                    JSONObject json2 = json.getJSONObject("general");
                    General genera = JSON.parseObject(json2.toJSONString(), General.class);

                    BasicCTwo basic = new BasicCTwo();

                    basic.setGeneral(genera);
                    basic.setTfj(t);
                    basic.setPeriod(t.getPeriod());

                    saveDB(basic);
                } catch (Exception e) {
                    _LOG.error("newString转换字符串出错");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                _LOG.error("解析网络接口数据出错");
                throwable.getMessage();
            }
        });
    }

    static public void getData2() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://fanyi.youdao.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        IndexSystemHttpJSON ishj = retrofit.create(IndexSystemHttpJSON.class);
        final long startTime1 = System.currentTimeMillis();   // 获取开始时间
        Call<Translationl> call = ishj.getJSONByForm("I love you");
        call.enqueue(new Callback<Translationl>() {
            @Override
            public void onResponse(Call<Translationl> call, Response<Translationl> response) {
                Translationl t = response.body();
                System.out.println(t.toString());
                long endTime = System.currentTimeMillis();// 结束时间
                System.out.println("时间为： " + (endTime - startTime1));
                System.out.println(new Date());
            }

            @Override
            public void onFailure(Call<Translationl> call, Throwable throwable) {
                if (call.isCanceled()) {
                    System.out.println("请求取消");
                } else {
                    System.out.println("请求出错");
                    System.out.println(throwable.getMessage());
                }
            }
        });
    }

    static public void timer() throws SchedulerException {
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler scheduler = sf.getScheduler();
        // 通过过JobDetail封装HelloJob，同时指定Job在Scheduler中所属组及名称，这里，组名为group1，而名称为job1。
        JobDetail job = newJob(RunJob.class).withIdentity("job1", "group1").build();
        // 创建一个CronScheduleBuilder 实例，指定该Trigger在Scheduler中所属组及名称。
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0 0 22 ? * MON");
        // 接着设置调度的时间规则.当前时间运行
        Trigger trigger = newTrigger().withIdentity("trigger1", "group1").startNow()
                .withSchedule(cronScheduleBuilder).build();

//        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
//                .withIdentity("trigger1","group1").withSchedule(cronScheduleBuilder).build();


        // 注册并进行调度
        scheduler.scheduleJob(job, trigger);
        // 启动调度器
        scheduler.start();
    }


    static public void saveDB(BasicCTwo basic) {
        Configuration cfg = new Configuration().configure();
        SessionFactory sessionFactory = cfg.buildSessionFactory();
        Session session = null;
        try {
            // 开启hibernate session
            session = sessionFactory.openSession();
            // 开启事务
            session.beginTransaction();
            session.save(basic);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}