// SPDX-License-Identifier: AGPL-3.0-only
// Time-entry UX adapted from HMC-Enhanced ExtensionsPage.kt StandbyTimeRow.
package com.mixi.manager

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal fun standbySeconds(text:String,minutes:Boolean,maximum:Int):Int? {
    val number=text.toLongOrNull()?:return null
    if(number<0||number>maximum)return null
    val seconds=number*(if(minutes)60 else 1)
    return seconds.takeIf{it==0L||it in 10L..maximum.toLong()}?.toInt()
}

@Composable internal fun HyperImmersivePage(store:UiStore){
    val context=LocalContext.current
    val enabled=store.values["immersive.enabled"]=="true"
    val fields=store.byPage["immersive"].orEmpty()
    Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
        Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
            Text("HyperImmersive · Alpha",fontSize=15.sp)
            Text("Phiên bản runtime · Chưa xác nhận",fontSize=12.sp,color=MiuixTheme.colorScheme.onSurface.copy(alpha=.55f))
        }
        Text("Chưa kết nối",fontSize=11.sp,color=MiuixTheme.colorScheme.onSurface.copy(alpha=.55f))
    }
    Card{
        SwitchPreference(title="StandBy",summary="Đặt điện thoại nằm ngang khi đang sạc để hiển thị StandBy.",checked=enabled,onCheckedChange={store.set("immersive.enabled",it.toString())})
        SwitchPreference(title="Nền ảnh bìa làm mờ",summary="Khi phát nhạc, ảnh bìa được dùng làm nền.",checked=(store.values["immersive.blurBackdrop"]?:"true")=="true",enabled=enabled,onCheckedChange={store.set("immersive.blurBackdrop",it.toString())})
        fields.filter{it.kind=="time"}.forEach{field->
            StandbyTimeRow(field,store,enabled,
                if(field.key.endsWith("idleSeconds"))"Giảm sáng sau" else "Tắt màn hình sau",
                if(field.key.endsWith("idleSeconds"))"Không chạm trong thời gian này, StandBy giảm sáng và hạ tần số quét. 0 là không giảm sáng."
                else "StandBy đóng và tắt màn hình khi không có thao tác. Dựng điện thoại lên để dùng lại. 0 là không tắt.")
        }
        if(fields.filter{it.kind=="time"}.any{store.value(it)=="0"})Text("Đặt không giới hạn có thể làm máy nóng hơn và tăng nguy cơ lưu ảnh màn hình.",fontSize=12.sp,color=Color(0xFFD64545),modifier=Modifier.padding(horizontal=16.dp,vertical=10.dp))
        BasicComponent(title="Xem trước StandBy",summary="Chưa khả dụng · Manager chưa kết nối runtime",onClick={Toast.makeText(context,"Chức năng này sẽ có khi kết nối HyperImmersive.",Toast.LENGTH_SHORT).show()})
    }
    Text("Bản giao diện · Các lựa chọn hiện chỉ lưu trong Manager.",fontSize=12.sp,color=MiuixTheme.colorScheme.onSurface.copy(alpha=.55f),modifier=Modifier.padding(horizontal=12.dp))
}

@Composable private fun StandbyTimeRow(field:Option,store:UiStore,enabled:Boolean,title:String,summary:String){
    val stored=store.value(field).toIntOrNull()?:field.initial.toInt()
    var minutes by remember(field.key){mutableStateOf(stored%60==0)}
    var text by remember(field.key){mutableStateOf((if(minutes)stored/60 else stored).toString())}
    var focused by remember{mutableStateOf(false)}
    var showUnits by remember{mutableStateOf(false)}
    val focus=LocalFocusManager.current
    val colors=MiuixTheme.colorScheme
    val shape=RoundedCornerShape(10.dp)
    val parsed=standbySeconds(text,minutes,field.max)
    fun settle(){
        if(parsed!=null)store.set(field.key,parsed.toString())
        else text=(if(minutes)stored/60 else stored).toString()
    }
    LaunchedEffect(stored){if(!focused){minutes=stored%60==0;text=(if(minutes)stored/60 else stored).toString()}}
    BasicComponent(title=title,summary=summary,enabled=enabled,endActions={
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){
            BasicTextField(value=text,onValueChange={text=it.filter(Char::isDigit).take(5)},singleLine=true,enabled=enabled,
                textStyle=TextStyle(color=if(text.isNotEmpty()&&parsed==null)Color(0xFFD64545) else colors.onSurface,fontSize=15.sp,textAlign=TextAlign.Center),
                cursorBrush=SolidColor(colors.primary),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number,imeAction=ImeAction.Done),
                keyboardActions=KeyboardActions(onDone={settle();focus.clearFocus()}),
                modifier=Modifier.width(64.dp).clip(shape).background(colors.secondaryContainer).padding(vertical=10.dp)
                    .onFocusChanged{if(focused&&!it.isFocused)settle();focused=it.isFocused})
            Box{
                Text(if(minutes)"Phút" else "Giây",fontSize=14.sp,modifier=Modifier.clip(shape).background(colors.secondaryContainer)
                    .clickable(enabled=enabled){settle();focus.clearFocus();showUnits=true}.padding(horizontal=12.dp,vertical=10.dp))
                OverlayListPopup(show=showUnits,enableWindowDim=false,alignment=PopupPositionProvider.Align.End,onDismissRequest={showUnits=false}){
                    Column(Modifier.background(colors.surfaceContainer,RoundedCornerShape(20.dp)).padding(8.dp)){
                        listOf("Giây","Phút").forEachIndexed{index,label->
                            DropdownImpl(item=DropdownItem(text=label,selected=(index==1)==minutes,onClick={}),optionSize=2,isSelected=(index==1)==minutes,index=index,
                                onSelectedIndexChange={val nextMinutes=index==1;val next=standbySeconds(text,minutes,field.max)?:stored;minutes=nextMinutes;text=(if(minutes)(next+59)/60 else next).toString();standbySeconds(text,minutes,field.max)?.let{store.set(field.key,it.toString())};showUnits=false})
                        }
                    }
                }
            }
        }
    })
    if(focused&&text.isNotEmpty()&&parsed==null)Text("Nhập 0 hoặc ${if(minutes)"1–${field.max/60} phút" else "10–${field.max} giây"}.",fontSize=12.sp,color=Color(0xFFD64545),modifier=Modifier.padding(start=16.dp,bottom=8.dp))
}
