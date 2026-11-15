package com.mixi.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import top.yukonga.miuix.kmp.blur.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.preference.*
import top.yukonga.miuix.kmp.theme.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Info

data class Option(val page:String,val key:String,val title:String,val kind:String,val initial:String,
                  val min:Int,val max:Int,val unit:String,val labels:List<String>,val values:List<String>,val secret:Boolean)

class UiStore(context:Context) {
    private val preferences=context.getSharedPreferences("manager_ui_preview",Context.MODE_PRIVATE)
    val values=mutableStateMapOf<String,String>().apply {
        preferences.all.forEach { (key,value)->if(value is String)put(key,value) }
    }
    val catalog:List<Option> = JSONArray(context.assets.open("catalog.json").bufferedReader().use { it.readText() }).let { array ->
        List(array.length()) { index -> array.getJSONObject(index).let { o ->
            fun strings(key:String)=o.getJSONArray(key).let { a->List(a.length()){a.getString(it)} }
            Option(o.getString("page"),o.getString("key"),o.getString("title"),o.getString("kind"),o.getString("initial"),o.getInt("min"),o.getInt("max"),o.getString("unit"),strings("labels"),strings("values"),o.getBoolean("secret"))
        } }
    }
    fun value(option:Option)=values[option.key]?:option.initial
    fun set(key:String,value:String){values[key]=value;preferences.edit().putString(key,value).apply()}
}

private val pages=linkedMapOf(
    "cover" to "Ảnh bìa & Đồng hồ", "media.notification" to "Thông báo & Màn hình khóa",
    "media.island" to "Media · SuperIsland", "lyrics" to "Lyrics", "dictionaries" to "Từ điển phiên âm",
    "island" to "Lockscreen SuperIsland", "extras" to "Tùy chỉnh bổ sung", "immersive" to "HyperImmersive",
    "appearance" to "Tùy chỉnh Soft Glass", "extensions" to "Tiện ích mở rộng")

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState);enableEdgeToEdge()
        window.isNavigationBarContrastEnforced=false
        val store=UiStore(applicationContext)
        setContent {
            val appearance=store.values["manager.theme"]?:"0"
            val dark=when(appearance){"1"->false;"2"->true;else->isSystemInDarkTheme()}
            SideEffect {
                WindowCompat.getInsetsController(window,window.decorView).apply {
                    isAppearanceLightStatusBars=!dark
                    isAppearanceLightNavigationBars=!dark
                }
            }
            MiuixTheme(colors=if(dark)darkColorScheme() else lightColorScheme()){Manager(store,this)}
        }
    }
}

@Composable private fun Manager(store:UiStore,context:Context){
    val pageStates=rememberSaveableStateHolder()
    var tab by rememberSaveable{mutableIntStateOf(0)}
    var detail by rememberSaveable{mutableStateOf("")}
    fun back(){detail=if(detail in pages && detail !in listOf("appearance","extensions"))"hmc" else ""}
    BackHandler(detail.isNotEmpty()||tab!=0){if(detail.isNotEmpty())back() else tab=0}
    val title=if(detail.isEmpty())listOf("Mixi Framework","Tính năng","Thông tin")[tab] else if(detail=="hmc")"HyperMusicCover Enhanced" else pages[detail]?:"Mã nguồn & Ghi nhận"
    val surface=MiuixTheme.colorScheme.surface
    val backdrop=rememberLayerBackdrop{drawRect(surface);drawContent()}
    val scrollBehavior=key(detail,tab){MiuixScrollBehavior()}
    val barColors=BlurDefaults.blurColors(blendColors=listOf(BlendColorEntry(color=surface.copy(alpha=.38f))))
    Scaffold(topBar={
        Box {
            Box(Modifier.matchParentSize().graphicsLayer{
                alpha=(-scrollBehavior.state.contentOffset/48.dp.toPx()).coerceIn(0f,1f)
            }.progressiveTextureBlur(backdrop=backdrop,shape=RectangleShape,gradient=ProgressiveBlur.Top.copy(curve=2.2f),blurRadius=10f,colors=barColors))
            TopAppBar(title=title,color=Color.Transparent,scrollBehavior=scrollBehavior,navigationIcon={
                if(detail.isNotEmpty())IconButton(onClick={back()},modifier=Modifier.size(48.dp)){Icon(MiuixIcons.Back,contentDescription="Quay lại")}
            })
        }
    },bottomBar={if(detail.isEmpty()){
        FloatingNavigationBar(
            color=Color.Transparent,
            modifier=Modifier.textureBlur(backdrop=backdrop,shape=RoundedCornerShape(32.dp),blurRadius=25f,
                colors=BlurDefaults.blurColors(blendColors=listOf(BlendColorEntry(color=MiuixTheme.colorScheme.surfaceContainer.copy(alpha=.65f)))))){
            FloatingNavigationBarItem(tab==0,{tab=0},MiuixIcons.Home,"Trang chủ")
            FloatingNavigationBarItem(tab==1,{tab=1},MiuixIcons.Settings,"Tính năng")
            FloatingNavigationBarItem(tab==2,{tab=2},MiuixIcons.Info,"Thông tin")
        }
    }}){padding->
        Box(Modifier.fillMaxSize().layerBackdrop(backdrop)){
        val pageKey=detail.ifEmpty{"tab$tab"}
        AnimatedContent(targetState=pageKey,modifier=Modifier.fillMaxSize(),label="page",transitionSpec={
            val forward=targetState !in listOf("tab0","tab1","tab2","hmc") || initialState.startsWith("tab") && !targetState.startsWith("tab")
            val direction=if(forward)1 else -1
            if(initialState.startsWith("tab")&&targetState.startsWith("tab")){
                (fadeIn(tween(150)) togetherWith fadeOut(tween(100))).using(null)
            }else{
                ((slideInHorizontally(tween(280,easing=FastOutSlowInEasing)){it*direction/5}+fadeIn(tween(180))) togetherWith
                    (slideOutHorizontally(tween(280,easing=FastOutSlowInEasing)){-it*direction/10}+fadeOut(tween(120)))).using(null)
            }
        }){route->
            pageStates.SaveableStateProvider(route){Column(Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState()).padding(top=padding.calculateTopPadding(),bottom=padding.calculateBottomPadding())
                .padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
                when(route){
                    "tab0"->{
                        FrameworkStatusCard()
                        Group("Tính năng"){
                            ArrowPreference(title="HyperMusicCover Enhanced",summary="Ảnh bìa, lời bài hát và Lockscreen Island",onClick={detail="hmc"})
                            ArrowPreference(title="Tùy chỉnh Soft Glass",summary="Độ mờ, độ trong và phản chiếu",onClick={detail="appearance"})
                            ArrowPreference(title="Tiện ích mở rộng",summary="Thanh cử chỉ, màn hình khóa và thanh trạng thái",onClick={detail="extensions"})
                        }
                        Group("Thông tin ROM"){
                            BasicComponent(title="Phiên bản ROM",summary=Build.VERSION.INCREMENTAL)
                            BasicComponent(title="Codename",summary=Build.DEVICE)
                            BasicComponent(title="Android",summary="${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}")
                            BasicComponent(title="Kernel",summary=System.getProperty("os.version")?:"Không có dữ liệu")
                        }
                    }
                    "tab1"->{
                        var query by rememberSaveable{mutableStateOf("")}
                        TextField(value=query,onValueChange={query=it},label="Tìm tùy chỉnh",singleLine=true,modifier=Modifier.fillMaxWidth())
                        if(query.isBlank()){
                            Group("Âm nhạc & Màn hình khóa"){ArrowPreference(title="HyperMusicCover Enhanced",summary="8 nhóm tùy chỉnh",onClick={detail="hmc"})}
                            Group("Giao diện hệ thống"){
                                ArrowPreference(title="Tùy chỉnh Soft Glass",onClick={detail="appearance"})
                                ArrowPreference(title="Tiện ích mở rộng",onClick={detail="extensions"})
                            }
                        }else{
                            val found=store.catalog.filter{it.title.contains(query,true)}
                            Group("${found.size} kết quả"){
                                if(found.isEmpty())BasicComponent(title="Không tìm thấy tùy chỉnh",summary="Thử một từ khóa khác.")
                                found.forEach{option->ArrowPreference(title=option.title,summary=pages[option.page],onClick={detail=option.page})}
                            }
                        }
                        PreviewNote()
                    }
                    "tab2"->{
                        Group("Mixi Framework"){
                            BasicComponent(title="Manager",summary=BuildConfig.VERSION_NAME)
                            BasicComponent(title="Framework",summary="Chưa kết nối")
                            BasicComponent(title="ROM Maintainer",summary="Kien Vu")
                        }
                        Group("Giao diện"){
                            OverlayDropdownPreference(items=listOf("Theo hệ thống","Sáng","Tối"),selectedIndex=(store.values["manager.theme"]?:"0").toInt(),title="Chế độ màu",onSelectedIndexChange={store.set("manager.theme",it.toString())})
                        }
                        Group("Dự án"){
                            ArrowPreference(title="Mã nguồn Manager",summary="GitHub · Kien Vu",onClick={context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/kienvu020404-design/MixiFramework-Manager")))})
                            ArrowPreference(title="Mã nguồn mở & Ghi nhận",onClick={detail="credits"})
                        }
                    }
                    "hmc"->{
                        PreviewNote()
                        Card {SwitchPreference(title="HyperMusicCover Enhanced",summary="Lựa chọn xem trước trên Manager",checked=store.values["preview.hmc"]!="false",onCheckedChange={store.set("preview.hmc",it.toString())})}
                        Group("Cá nhân hóa"){
                            pages.filterKeys{it !in listOf("appearance","extensions")}.forEach{(page,name)->
                                ArrowPreference(title=name,summary="${store.catalog.count{it.page==page}} tùy chỉnh",onClick={detail=page})
                            }
                        }
                    }
                    "credits"->Group("Cảm ơn các dự án nguồn mở"){
                        BasicComponent(title="Miuix · YuKongA",summary="0.9.4 · Apache-2.0")
                        BasicComponent(title="HyperMusicCover",summary="zyl6932 · AGPL-3.0")
                        BasicComponent(title="HyperMusicCover Enhanced",summary="Lewewe · AGPL-3.0")
                        BasicComponent(title="HyperModifier",summary="AritxOnly")
                        BasicComponent(title="Kotlin & Jetpack Compose",summary="Apache-2.0")
                    }
                    else->{
                        PreviewNote()
                        val controls=store.catalog.filter{it.page==route}
                        controls.groupBy { option ->
                            when(route){
                                "appearance" -> when(option.key){"appearance.enabled"->"Soft Glass";"appearance.blur","appearance.background"->"Độ mờ";else->"Vật liệu"}
                                "cover" -> if(option.key in listOf("clockSize","forceColon"))"Đồng hồ" else if(option.key in listOf("coverStyle","coverOnTap","mini.mediaCollapsedDefault"))"Ảnh bìa" else "Nền & Chuyển động"
                                "lyrics" -> if(option.key.startsWith("provider")||option.key.startsWith("spicy")||option.key=="lyricsSearchMode")"Nguồn lời bài hát" else if(option.key in listOf("lyricsAlign","lyricsRoma","onDeviceTransliteration"))"Hiển thị" else "Hiệu ứng & Phát nhạc"
                                "extensions" -> if(option.key in listOf("appearance.gestureMotion","appearance.pinGlass"))"Điều hướng & Màn hình khóa" else "Thanh trạng thái"
                                "media.notification","media.island" -> if(option.key.substringAfterLast('.') in listOf("background","theme","flow","tone","animate","blur","invert"))"Nền & Hiệu ứng" else "Nội dung thẻ"
                                else -> pages[route]?:title
                            }
                        }.forEach{(section,options)->Group(section){options.forEach{OptionRow(it,store)}}}
                    }
                }
                Spacer(Modifier.height(24.dp))
            }}
        }
        }
    }
}

@Composable private fun FrameworkStatusCard(){
    val tint=MiuixTheme.colorScheme.onSurface.copy(alpha=.55f)
    Box(Modifier.fillMaxWidth().padding(horizontal=8.dp).heightIn(min=156.dp)
        .background(MiuixTheme.colorScheme.surfaceContainer,RoundedCornerShape(28.dp))){
        Icon(MiuixIcons.Info,contentDescription=null,tint=tint.copy(alpha=.12f),modifier=Modifier.align(Alignment.CenterEnd).padding(end=16.dp).size(112.dp))
        Column(Modifier.padding(22.dp).padding(end=70.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Text("MIXI",fontSize=12.sp,modifier=Modifier.background(tint.copy(alpha=.12f),RoundedCornerShape(10.dp)).padding(horizontal=10.dp,vertical=4.dp))
            Text("Chưa kết nối",fontSize=26.sp)
            Text("Framework · Chưa xác nhận",fontSize=13.sp,color=tint)
        }
    }
    Text("Bản giao diện · Các lựa chọn chưa áp dụng lên hệ thống",fontSize=12.sp,color=tint,modifier=Modifier.padding(horizontal=12.dp))
}

@Composable private fun PreviewNote(){Card{BasicComponent(title="Chưa kết nối framework",summary="Các lựa chọn hiện chỉ lưu trong Manager, chưa áp dụng lên hệ thống.")}}
@Composable private fun Group(title:String,content:@Composable ColumnScope.()->Unit){
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text(title,modifier=Modifier.padding(start=12.dp,top=4.dp),fontSize=14.sp,color=MiuixTheme.colorScheme.onSurface.copy(alpha=.55f))
        Card(content=content)
    }
}
@Composable private fun OptionRow(option:Option,store:UiStore){
    val current=store.value(option)
    when(option.kind){
        "bool"->SwitchPreference(title=option.title,checked=current=="true",onCheckedChange={store.set(option.key,it.toString())})
        "choice"->OverlayDropdownPreference(items=option.labels,selectedIndex=option.values.indexOf(current).coerceAtLeast(0),title=option.title,onSelectedIndexChange={store.set(option.key,option.values[it])})
        "range","time"->{
            var draft by remember(option.key,current){mutableFloatStateOf(current.toFloat())}
            SliderPreference(title=option.title,value=draft,valueRange=option.min.toFloat()..option.max.toFloat(),valueText="${draft.toInt()}${option.unit}",onValueChange={draft=it},onValueChangeFinished={
                val value=draft.toInt();store.set(option.key,(if(option.kind=="time"&&value in 1..9)10 else value).toString())
            })
        }
        else->Column(Modifier.padding(16.dp)){
            Text(option.title)
            if(option.secret) Text("Khóa API chưa được lưu ở bản xem trước",fontSize=14.sp,color=MiuixTheme.colorScheme.onSurface.copy(alpha=.55f))
            else TextField(value=current,onValueChange={store.set(option.key,it.take(4096))},label=option.title,singleLine=true,modifier=Modifier.fillMaxWidth())
        }
    }
}
