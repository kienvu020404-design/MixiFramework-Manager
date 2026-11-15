package com.mixi.manager

import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.findRootCoordinates
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.awaitCancellation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text

@Composable internal fun SoftGlassPage(store:UiStore){
    val controls=store.byPage["appearance"].orEmpty()
    val drafts=remember{mutableStateMapOf<String,String>()}
    fun current(key:String):String= drafts[key]?:store.values[key]?:controls.first{it.key==key}.initial
    fun amount(key:String,max:Float)=current(key).toFloatOrNull()?.coerceIn(0f,max)?:0f
    val enabled=store.values["appearance.enabled"]=="true"
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(store.connected,lifecycle){if(store.connected)lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED){store.loadPreview();awaitCancellation()}}
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
    var visible by remember{mutableStateOf(true)}
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    var foreground by remember(lifecycle){mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))}
    DisposableEffect(lifecycle){
        val observer=LifecycleEventObserver{_,_->foreground=lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)}
        lifecycle.addObserver(observer)
        onDispose{lifecycle.removeObserver(observer)}
    }
    if(bitmap==null||recipe==null){
        Card{BasicComponent(title="Xem trước Soft Glass",summary="Chưa đọc được hình nền hoặc vật liệu gốc trên máy")}
        return
    }
    androidx.compose.ui.viewinterop.AndroidView(
        modifier=Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(24.dp)).onGloballyPositioned{coordinates->
            val bounds=coordinates.boundsInWindow();visible=bounds.height>0f&&bounds.bottom>0f&&bounds.top<coordinates.findRootCoordinates().size.height
        },
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
            val image=frame.getChildAt(0) as android.widget.ImageView
            if(image.tag !== bitmap){image.setImageBitmap(bitmap);image.tag=bitmap}
            val active=enabled&&visible&&foreground
            val panel=frame.getChildAt(1)
            val configuration=listOf(active,blur,background,opacity,reflection,recipe)
            if(panel.tag==configuration)return@AndroidView
            try{
                val native=recipe.clone();native[14]=opacity/100f;native[24]=reflection/100f
                android.view.View::class.java.getMethod("clearMiBackgroundBlendColor").invoke(panel)
                android.view.View::class.java.getMethod("setPassWindowBlurEnabled",Boolean::class.javaPrimitiveType).invoke(panel,active)
                android.view.View::class.java.getMethod("setMiViewBlurMode",Int::class.javaPrimitiveType).invoke(panel,if(active)1 else 0)
                android.view.View::class.java.getMethod("setMiBackgroundBlurMode",Int::class.javaPrimitiveType).invoke(panel,if(active)1 else 0)
                android.view.View::class.java.getMethod("setMiBackgroundBlurRadius",Int::class.javaPrimitiveType).invoke(panel,(80f*background/100f).toInt())
                if(active)android.view.View::class.java.getMethod("addMiBackgroundBlendColor",Int::class.javaPrimitiveType,Int::class.javaPrimitiveType).invoke(panel,0x1AFFFFFF,101)
                android.view.View::class.java.getMethod("setMiGlassBlurRadius",Int::class.javaPrimitiveType,Int::class.javaPrimitiveType).invoke(panel,blur.toInt(),(blur*2f).toInt())
                android.view.View::class.java.getMethod("setMiViewMaterialType",Int::class.javaPrimitiveType).invoke(panel,if(active)1 else 0)
                android.view.View::class.java.getMethod("setMiGlass",FloatArray::class.java).invoke(panel,if(active)native else null)
                panel.tag=configuration
                failed=false
            }catch(_:ReflectiveOperationException){failed=true}
        })
    if(failed)Text("ROM chưa cho phép vẽ Glass gốc trong cửa sổ này",fontSize=12.sp)
}
