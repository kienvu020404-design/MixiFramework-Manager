package com.mixi.manager

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable internal fun SoftGlassPage(store:UiStore){
    val controls=store.byPage["appearance"].orEmpty()
    val drafts=remember{mutableStateMapOf<String,String>()}
    fun current(key:String):String= drafts[key]?:store.values[key]?:controls.first{it.key==key}.initial
    fun amount(key:String,max:Float)=current(key).toFloatOrNull()?.coerceIn(0f,max)?:0f
    val enabled=store.values["appearance.enabled"]=="true"
    LaunchedEffect(store.connected){if(store.connected)store.loadPreview()}
    Group("Soft Glass"){controls.filter{it.kind=="bool"}.forEach{OptionRow(it,store)}}
    GlassSample(store,enabled,amount("appearance.blur",100f),amount("appearance.background",200f),
        amount("appearance.opacity",100f),amount("appearance.reflection",200f))
    Group("Độ mờ"){
        controls.filter{it.key in listOf("appearance.blur","appearance.background")}.forEach{
            OptionRow(it,store){key,value->drafts[key]=value}
        }
    }
    Group("Vật liệu"){
        controls.filter{it.key in listOf("appearance.opacity","appearance.reflection")}.forEach{
            OptionRow(it,store){key,value->drafts[key]=value}
        }
    }
}
@Composable private fun GlassSample(store:UiStore,enabled:Boolean,blur:Float,background:Float,opacity:Float,reflection:Float){
    val bytes=store.previewAssets.getByteArray("wallpaper")
    val recipe=store.previewAssets.getFloatArray("recipe")
    val bitmap by produceState<android.graphics.Bitmap?>(null,bytes){
        value=kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default){bytes?.let{android.graphics.BitmapFactory.decodeByteArray(it,0,it.size)}}
    }
    var failed by remember{mutableStateOf(false)}
    if(bitmap==null||recipe==null){
        Card{BasicComponent(title="Xem trước Soft Glass",summary="Chưa đọc được hình nền hoặc vật liệu gốc trên máy")}
        return
    }
    androidx.compose.ui.viewinterop.AndroidView(
        modifier=Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(24.dp)),
        factory={context->android.widget.FrameLayout(context).apply{
            addView(android.widget.ImageView(context).apply{scaleType=android.widget.ImageView.ScaleType.CENTER_CROP},android.widget.FrameLayout.LayoutParams(-1,-1))
            addView(android.widget.TextView(context).apply{
                text="Soft Glass";textSize=22f;setTextColor(android.graphics.Color.WHITE)
                val d=resources.displayMetrics.density
                setPadding((24*d).toInt(),(24*d).toInt(),(24*d).toInt(),(24*d).toInt())
                this.background=android.graphics.drawable.GradientDrawable().apply{setColor(android.graphics.Color.TRANSPARENT);cornerRadius=28*d};clipToOutline=true
            },android.widget.FrameLayout.LayoutParams(-1,-2,android.view.Gravity.BOTTOM).apply{setMargins(20,20,20,20)})
        }},onReset=null,onRelease={frame->
            try{android.view.View::class.java.getMethod("setMiGlass",FloatArray::class.java).invoke(frame.getChildAt(1),*arrayOfNulls<Any>(1))}catch(_:ReflectiveOperationException){}
        },update={frame->
            (frame.getChildAt(0) as android.widget.ImageView).setImageBitmap(bitmap)
            val panel=frame.getChildAt(1)
            try{
                val native=recipe.clone();native[14]=opacity/100f;native[24]=reflection/100f
                android.view.View::class.java.getMethod("clearMiBackgroundBlendColor").invoke(panel)
                android.view.View::class.java.getMethod("setPassWindowBlurEnabled",Boolean::class.javaPrimitiveType).invoke(panel,enabled)
                android.view.View::class.java.getMethod("setMiViewBlurMode",Int::class.javaPrimitiveType).invoke(panel,if(enabled)1 else 0)
                android.view.View::class.java.getMethod("setMiBackgroundBlurMode",Int::class.javaPrimitiveType).invoke(panel,if(enabled)1 else 0)
                android.view.View::class.java.getMethod("setMiBackgroundBlurRadius",Int::class.javaPrimitiveType).invoke(panel,(80f*background/100f).toInt())
                if(enabled)android.view.View::class.java.getMethod("addMiBackgroundBlendColor",Int::class.javaPrimitiveType,Int::class.javaPrimitiveType).invoke(panel,0x1AFFFFFF,101)
                android.view.View::class.java.getMethod("setMiGlassBlurRadius",Int::class.javaPrimitiveType,Int::class.javaPrimitiveType).invoke(panel,blur.toInt(),(blur*2f).toInt())
                android.view.View::class.java.getMethod("setMiViewMaterialType",Int::class.javaPrimitiveType).invoke(panel,if(enabled)1 else 0)
                android.view.View::class.java.getMethod("setMiGlass",FloatArray::class.java).invoke(panel,if(enabled)native else null)
                failed=false
            }catch(_:ReflectiveOperationException){failed=true}
        })
    if(failed)Text("ROM chưa cho phép vẽ Glass gốc trong cửa sổ này",fontSize=12.sp)
}
