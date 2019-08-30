package com.zxl.demo.controller;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jiguang.common.resp.DefaultResult;
import cn.jpush.api.JPushClient;
import cn.jpush.api.device.TagAliasResult;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.*;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import cn.jpush.api.report.ReceivedsResult;
import cn.jpush.api.schedule.ScheduleResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.jpush.api.push.model.notification.PlatformNotification.ALERT;

@RequestMapping("push")
@RestController
public class JPushController {
    private static final String masterSecret = "fd897251925a1baac5e6ec43";
    private static final String appKey = "56b0d8e4946272b51d850dad";
    @RequestMapping("/spush")
    public String push(String content) {                               //
        JPushClient jpushClient = new JPushClient(masterSecret, appKey, null, ClientConfig.getInstance());
        // For push, all you need do is to build PushPayload object.
        PushPayload payload = buildPushObject_android_tag_alertWithTitle();

        try {
            PushResult result = jpushClient.sendPush(payload);
//            LOG.info("Got result - " + result);
            return content;
        } catch (APIConnectionException e) {
            // Connection error, should retry later
//            LOG.error("Connection error, should retry later", e);
            return content;
        } catch (APIRequestException e) {
            // Should review the error, and fix the request
//            LOG.error("Should review the error, and fix the request", e);
//            LOG.info("HTTP Status: " + e.getStatus());
//            LOG.info("Error Code: " + e.getErrorCode());
//            LOG.info("Error Message: " + e.getErrorMessage());
            return content;
        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//            return content;
//        }
    }
    public static PushPayload buildPushObject_all_all_alert() {
        return PushPayload.alertAll(ALERT);
    }

    public static PushPayload buildPushObject_all_alias_alert() {
        return PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias("123456"))
                .setNotification(Notification.alert(ALERT))
                .build();
    }

    // notice 1: 构建推送对象：平台是 Android，目标是 tag 为 "tag1" 的设备，内容是 Android 通知 ALERT，并且标题为 TITLE
    public static PushPayload buildPushObject_android_tag_alertWithTitle() {
        return PushPayload.newBuilder()
                .setPlatform(Platform.android())
                .setAudience(Audience.alias("123456"))
                .setNotification(Notification.android(ALERT, "TiTle", null))
                .build();
    }

    // notice 2: 构建推送对象：平台是 iOS，推送目标是 "tag1", "tag_all" 的交集，推送内容同时包括通知与消息
    //  - 通知信息是 ALERT，角标数字为 5，通知声音为 "happy"，并且附加字段 from = "JPush"；消息内容是 MSG_CONTENT。
    //  通知是 APNs 推送通道的，消息是 JPush 应用内消息通道的。APNs 的推送环境是“生产”（如果不显式设置的话，Library 会默认指定为开发）
    public static PushPayload buildPushObject_ios_tagAnd_alertWithExtrasAndMessage() {
        return PushPayload.newBuilder()
                .setPlatform(Platform.ios())
                .setAudience(Audience.tag_and("tag1", "tag_all"))
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(IosNotification.newBuilder()
                                .setAlert(ALERT)
                                .setBadge(5)
                                .setSound("happy")
                                .addExtra("from", "JPush")
                                .build())
                        .build())
                .setMessage(Message.content("MSG_CONTENT"))
                .setOptions(Options.newBuilder()
                        .setApnsProduction(true)
                        .build())
                .build();
    }

    // notice 3: 构建推送对象：平台是 Andorid 与 iOS，推送目标是 （"tag1" 与 "tag2" 的并集）交（"alias1" 与 "alias2" 的并集），
    //  推送内容是 - 内容为 MSG_CONTENT 的消息，并且附加字段 from = JPush。
    public static PushPayload buildPushObject_ios_audienceMore_messageWithExtras() {
        return PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.newBuilder()
                        .addAudienceTarget(AudienceTarget.tag("tag1", "tag2"))
                        .addAudienceTarget(AudienceTarget.alias("alias1", "alias2"))
                        .build())
                .setMessage(Message.newBuilder()
                        .setMsgContent("MSG_CONTENT")
                        .addExtra("from", "JPush")
                        .build())
                .build();
    }

    // notice 4: 构建推送对象：推送内容包含SMS信息
    public static void testSendWithSMS() {
        JPushClient jpushClient = new JPushClient(masterSecret, appKey);
        try {
            SMS sms = SMS.newBuilder()
                    .setDelayTime(1000)
                    .setTempID(2000)
                    .addPara("Test", 1)
                    .build();
            PushResult result = jpushClient.sendAndroidMessageWithAlias("Test SMS", "test sms", sms, "alias1");
//            LOG.info("Got result - " + result);
        } catch (APIConnectionException e) {
//            LOG.error("Connection error. Should retry later. ", e);
        } catch (APIRequestException e) {
//            LOG.error("Error response from JPush server. Should review and fix it. ", e);
//            LOG.info("HTTP Status: " + e.getStatus());
//            LOG.info("Error Code: " + e.getErrorCode());
//            LOG.info("Error Message: " + e.getErrorMessage());
        }
    }

    // notice 5: 统计获取样例
    public void getModel() {
        JPushClient jpushClient = new JPushClient(masterSecret, appKey);
        try {
            ReceivedsResult result = jpushClient.getReportReceiveds("1942377665");
//            LOG.debug("Got result - " + result);

        } catch (APIConnectionException e) {
            // Connection error, should retry later
//            LOG.error("Connection error, should retry later", e);

        } catch (APIRequestException e) {
            // Should review the error, and fix the request
//            LOG.error("Should review the error, and fix the request", e);
//            LOG.info("HTTP Status: " + e.getStatus());
//            LOG.info("Error Code: " + e.getErrorCode());
//            LOG.info("Error Message: " + e.getErrorMessage());
        }
    }

    // notice: Tag/Alias 样例
    public void getTagAlis(String REGISTRATION_ID1) {
        JPushClient jpushClient = new JPushClient(masterSecret, appKey);
        try {
            TagAliasResult result = jpushClient.getDeviceTagAlias(REGISTRATION_ID1);

            System.out.println(result.alias);
            System.out.println(result.tags.toString());
        } catch (APIConnectionException e) {
//            LOG.error("Connection error. Should retry later. ", e);
        } catch (APIRequestException e) {
//            LOG.error("Error response from JPush server. Should review and fix it. ", e);
//            LOG.info("HTTP Status: " + e.getStatus());
//            LOG.info("Error Code: " + e.getErrorCode());
//            LOG.info("Error Message: " + e.getErrorMessage());
        }
    }

    // notice: 绑定手机号
    public void bindPhone(String REGISTRATION_ID1){
        JPushClient jpushClient = new JPushClient(masterSecret, appKey);
        try {
            DefaultResult result =  jpushClient.bindMobile(REGISTRATION_ID1, "13000000000");
//            LOG.info("Got result " + result);
        } catch (APIConnectionException e) {
//            LOG.error("Connection error. Should retry later. ", e);
        } catch (APIRequestException e) {
//            LOG.error("Error response from JPush server. Should review and fix it. ", e);
//            LOG.info("HTTP Status: " + e.getStatus());
//            LOG.info("Error Code: " + e.getErrorCode());
//            LOG.info("Error Message: " + e.getErrorMessage());
        }
    }

    // notice: Schedule 样例
    public void schedule(){
        JPushClient jpushClient = new JPushClient(masterSecret, appKey);
        String name = "test_schedule_example";
        String time = "2016-07-30 12:30:25";
        PushPayload push = PushPayload.alertAll("test schedule example.");
        try {
            ScheduleResult result = jpushClient.createSingleSchedule(name, time, push);
//            LOG.info("schedule result is " + result);
        } catch (APIConnectionException e) {
//            LOG.error("Connection error. Should retry later. ", e);
        } catch (APIRequestException e) {
//            LOG.error("Error response from JPush server. Should review and fix it. ", e);
//            LOG.info("HTTP Status: " + e.getStatus());
//            LOG.info("Error Code: " + e.getErrorCode());
//            LOG.info("Error Message: " + e.getErrorMessage());
        }
    }

    // notice: Custom Client 样例  配置的SSLVersion表示指定至少支持的协议版本，也可能支持其他多个协议版本，
    //  最终支持的协议版本列表取决于JRE和运行环境
    public static void testCustomClient() {
        ClientConfig config = ClientConfig.getInstance();
        config.setMaxRetryTimes(5);
        config.setConnectionTimeout(10 * 1000);	// 10 seconds
        config.setSSLVersion("TLSv1.1");		// JPush server supports SSLv3, TLSv1, TLSv1.1, TLSv1.2

        JPushClient jPushClient = new JPushClient(masterSecret, appKey, null, config);
    }

    public static void testCustomPushClient() {
        ClientConfig config = ClientConfig.getInstance();
        config.setApnsProduction(false); 	// development env
        config.setTimeToLive(60 * 60 * 24); // one day

        //	config.setGlobalPushSetting(false, 60 * 60 * 24); // development env, one day

        JPushClient jPushClient = new JPushClient(masterSecret, appKey, null, config); 	// JPush client

        //	PushClient pushClient = new PushClient(masterSecret, appKey, null, config); 	// push client only

    }

}
