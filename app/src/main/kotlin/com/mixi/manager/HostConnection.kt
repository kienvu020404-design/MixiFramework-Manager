package com.mixi.manager

import android.app.BroadcastOptions
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun hostRequest(context:Context,op:String,key:String?=null,value:String?=null):Bundle = withTimeout(9000){
    suspendCancellableCoroutine { continuation ->
        val options=BroadcastOptions.makeBasic().apply{setShareIdentityEnabled(true)}
        val intent=Intent("com.mixi.host.SETTINGS_REQUEST").setPackage("com.android.settings")
            .addFlags(Intent.FLAG_RECEIVER_FOREGROUND).putExtra("op",op).putExtra("key",key).putExtra("value",value)
        context.sendOrderedBroadcast(intent,null,options.toBundle(),object:BroadcastReceiver(){
            override fun onReceive(context:Context,intent:Intent){
                if(!continuation.isActive)return
                val reply=getResultExtras(false)
                if(resultCode==1&&reply!=null)continuation.resume(Bundle(reply))
                else continuation.resumeWithException(IllegalStateException("Settings chưa kết nối hoặc chưa lưu được cấu hình"))
            }
        },Handler(Looper.getMainLooper()),0,null,null)
    }
}
