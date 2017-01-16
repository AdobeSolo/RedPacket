package com.strive.red.tools;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with Android Studio
 * <p/>
 * Author:zhaibin
 * <p/>
 * Date : 2016/1/10.
 */
public class StriveQQRedService extends AccessibilityService{

    private final String TAG = StriveQQRedService.class.getName();

    /** 微信的包名*/
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    /** 红包消息的关键字*/
    static final String HONGBAO_TEXT_KEY = "[QQ红包]";

    private static List<Long> mHasClickNormalSourceNodeId = new ArrayList<>();
    private static List<Long> mHasClickCommandSourceNodeId = new ArrayList<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        Log.d(TAG, "事件---->" + event);

        //通知栏事件
        if(eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> texts = event.getText();
            if(!texts.isEmpty()) {
                for(CharSequence t : texts) {
                    String text = String.valueOf(t);
                    if(text.contains(HONGBAO_TEXT_KEY)) {
                        openNotify(event);
                        break;
                    }
                }
            }
        } else if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            openHongBao(event);
        }

    }

    @Override
    public void onInterrupt()  {
        Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    /** 打开通知栏消息*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotify(AccessibilityEvent event) {
        if(event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        //以下是精华，将微信的通知栏消息打开
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
        if("com.tencent.mobileqq.activity.SplashActivity".equals(event.getClassName())) {
            //在聊天界面,去点中红包
            checkKey2();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey1() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("拆红包");
        for(AccessibilityNodeInfo n : list) {
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey2() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> normalList = nodeInfo.findAccessibilityNodeInfosByText("点击拆开");
        List<AccessibilityNodeInfo> commandList = nodeInfo.findAccessibilityNodeInfosByText("口令红包");

        if(normalList.isEmpty()) {
            normalList = nodeInfo.findAccessibilityNodeInfosByText(HONGBAO_TEXT_KEY);
            for(AccessibilityNodeInfo n : normalList) {
                Log.i(TAG, "-->QQ红包:" + n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else {
            //最新的红包领起
            for(int i = normalList.size() - 1; i >= 0; i --) {
                AccessibilityNodeInfo parent = normalList.get(i).getParent();
                Log.i(TAG, "-->点击拆开:" + parent);
                if(parent != null) {
                    try {
                        Field sourcenodeId = AccessibilityNodeInfo.class.getDeclaredField("mSourceNodeId");
                        Field parentNodeId = AccessibilityNodeInfo.class.getDeclaredField("mParentNodeId");
                        sourcenodeId.setAccessible(true);
                        parentNodeId.setAccessible(true);
                        long msoucrenodeId = (Long) sourcenodeId.get(parent);
                        long mparentNodeId = (Long) parentNodeId.get(parent);
                        if (mHasClickNormalSourceNodeId != null && mHasClickNormalSourceNodeId.size() > 0){
                            if (!mHasClickNormalSourceNodeId.contains(msoucrenodeId)) {
                                mHasClickNormalSourceNodeId.add(msoucrenodeId);
                                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                break;
                            }
                        }else {
                            mHasClickNormalSourceNodeId = new ArrayList<>();
                            mHasClickNormalSourceNodeId.add(msoucrenodeId);
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        if(commandList.isEmpty()) {
            commandList = nodeInfo.findAccessibilityNodeInfosByText(HONGBAO_TEXT_KEY);
            for(AccessibilityNodeInfo n : commandList) {
                Log.i(TAG, "-->QQ红包:" + n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else {
            //最新的红包领起
            for(int i = commandList.size() - 1; i >= 0; i --) {
                AccessibilityNodeInfo parent = commandList.get(i).getParent();
                Log.i(TAG, "-->点击拆开:" + parent);
                if(parent != null) {
                    try {
                        Field sourcenodeId = AccessibilityNodeInfo.class.getDeclaredField("mSourceNodeId");
                        Field parentNodeId = AccessibilityNodeInfo.class.getDeclaredField("mParentNodeId");
                        sourcenodeId.setAccessible(true);
                        parentNodeId.setAccessible(true);
                        long msoucrenodeId = (Long) sourcenodeId.get(parent);
                        long mparentNodeId = (Long) parentNodeId.get(parent);
                        if (mHasClickCommandSourceNodeId != null && mHasClickCommandSourceNodeId.size() > 0){
                            if (!mHasClickCommandSourceNodeId.contains(msoucrenodeId)) {
                                mHasClickCommandSourceNodeId.add(msoucrenodeId);
                                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                if (inputCommandRed(nodeInfo)) {
                                    break;
                                }
                            }
                        }else {
                            mHasClickCommandSourceNodeId = new ArrayList<>();
                            mHasClickCommandSourceNodeId.add(msoucrenodeId);
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            if (inputCommandRed(nodeInfo)) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    private boolean inputCommandRed(AccessibilityNodeInfo nodeInfo){
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return false;
        }
        List<AccessibilityNodeInfo> inputCommandlList =  nodeInfo.findAccessibilityNodeInfosByText("点击输入口令");
        if(!inputCommandlList.isEmpty()) {
            //最新的红包领起
            for(int i = inputCommandlList.size() - 1; i >= 0; i --) {
                AccessibilityNodeInfo parent = inputCommandlList.get(i).getParent();
                Log.i(TAG, "-->点击拆开:" + parent);
                if(parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    sendCommandRed(nodeInfo);
                    return true;
                }
            }
        }
        return false;
    }

    private void sendCommandRed(AccessibilityNodeInfo nodeInfo){
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> inputCommandlList =  nodeInfo.findAccessibilityNodeInfosByText("发送");
        if(!inputCommandlList.isEmpty()) {
            //最新的红包领起
            for(int i = inputCommandlList.size() - 1; i >= 0; i --) {
                inputCommandlList.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        }
    }
}
