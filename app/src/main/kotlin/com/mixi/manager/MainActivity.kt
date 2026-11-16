package com.mixi.manager

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import top.yukonga.miuix.kmp.anim.folmeSpring
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.focus.onFocusChanged
import top.yukonga.miuix.kmp.blur.*
import top.yukonga.miuix.kmp.nav.core.*
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.MiuixPopupHost
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.preference.*
import top.yukonga.miuix.kmp.theme.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Info

@Immutable
data class Option(val page:String,val key:String,val title:String,val kind:String,val initial:String,
                  val min:Int,val max:Int,val unit:String,val labels:List<String>,val values:List<String>,val secret:Boolean)

@Stable
class UiStore(private val context:Context,private val scope:CoroutineScope) {
    private val preferences=context.getSharedPreferences("manager_ui_preview",Context.MODE_PRIVATE)
    val values=mutableStateMapOf<String,String>().apply {
        preferences.all.forEach { (key,value)->if(value is String&&(!BuildConfig.HOST_INTEGRATED||key.startsWith("manager.")))put(key,value) }
    }
    val catalog:List<Option> = UiCatalog.options
    val byPage:Map<String,List<Option>> = catalog.groupBy{it.page}
    fun value(option:Option)=values[option.key]?:option.initial
    var connected by mutableStateOf(false);private set
    var previewAssets by mutableStateOf(android.os.Bundle());private set
    suspend fun loadPreview(){if(connected)mutex.withLock{try{previewAssets=hostRequest(context,"previewAssets")}catch(failure:Exception){if(failure is CancellationException&&failure !is TimeoutCancellationException)throw failure;previewAssets=android.os.Bundle()}}}
    var immersive by mutableStateOf(android.os.Bundle());private set
    private val mutex=Mutex()
    private val pending=linkedMapOf<String,String>()
    private val confirmed=mutableMapOf<String,String>()
    private var writing:Job?=null
    private fun accept(reply:android.os.Bundle){
        val state=reply.getBundle("values")?:throw IllegalStateException("Thiếu cấu hình Host")
        for(key in state.keySet())state.getString(key)?.let{confirmed[key]=it;if(!pending.containsKey(key))values[key]=it}
        if(reply.containsKey("immersive"))immersive=reply.getBundle("immersive")?:android.os.Bundle()
        connected=true
    }
    private var refreshing:Job?=null
    fun stopReads(){refreshing?.cancel()}
    fun refresh(){if(BuildConfig.HOST_INTEGRATED){refreshing?.cancel();refreshing=scope.launch{
        mutex.withLock{try{accept(hostRequest(context,"query"))}catch(failure:Exception){if(failure is CancellationException&&failure !is TimeoutCancellationException)throw failure;connected=false}}
    }}}
    fun preview(){if(connected)scope.launch{mutex.withLock{try{accept(hostRequest(context,"preview"))}catch(failure:Exception){if(failure is CancellationException&&failure !is TimeoutCancellationException)throw failure;android.widget.Toast.makeText(context,"Không mở được StandBy",android.widget.Toast.LENGTH_SHORT).show()}}}}
    suspend fun refreshImmersive(){if(connected)mutex.withLock{
        try{accept(hostRequest(context,"immersiveQuery"))}catch(failure:Exception){if(failure is CancellationException&&failure !is TimeoutCancellationException)throw failure;immersive=android.os.Bundle()}
    }}
    var applyingExtensions by mutableStateOf(false);private set
    fun applyExtensions(){if(!connected||applyingExtensions)return
        applyingExtensions=true
        scope.launch{try{mutex.withLock{accept(hostRequest(context,"applyExtensions"))}}
            catch(failure:Exception){if(failure is CancellationException&&failure !is TimeoutCancellationException)throw failure;android.widget.Toast.makeText(context,"Chưa áp dụng được. Thử lại khi kết nối Settings.",android.widget.Toast.LENGTH_LONG).show()}
            finally{applyingExtensions=false}}
    }
    fun set(key:String,value:String){
        if(!BuildConfig.HOST_INTEGRATED||key.startsWith("manager.")){
            values[key]=value;preferences.edit().putString(key,value).apply();return
        }
        if(!connected)return
        values[key]=value;pending[key]=value
        if(writing?.isActive==true)return
        writing=scope.launch{mutex.withLock{
            while(pending.isNotEmpty()){
                val (next,requested)=pending.entries.first().toPair()
                try{
                    val reply=hostRequest(context,"change",next,requested)
                    if(pending[next]==requested)pending.remove(next)
                    accept(reply)
                }catch(failure:Exception){
                    if(failure is CancellationException&&failure !is TimeoutCancellationException)throw failure
                    pending.clear();confirmed.forEach{(k,v)->values[k]=v}
                    connected=false
                }
            }
        }}
    }
}

private val pages=linkedMapOf(
    "cover" to "Ảnh bìa & Đồng hồ", "media.notification" to "Thông báo & Màn hình khóa",
    "media.island" to "Media · SuperIsland", "lyrics" to "Lyrics", "dictionaries" to "Từ điển phiên âm",
    "island" to "Lockscreen SuperIsland", "extras" to "Tùy chỉnh bổ sung", "immersive" to "HyperImmersive",
    "appearance" to "Tùy chỉnh Soft Glass", "extensions" to "Tiện ích mở rộng")

class MainActivity:ComponentActivity(){
    private lateinit var store:UiStore
    override fun getClassLoader():ClassLoader=javaClass.classLoader!!
    override fun attachBaseContext(base:Context){
        if(BuildConfig.HOST_INTEGRATED){
            val path=System.getProperty("mixi.ui.module.path")?:error("Host UI loader required")
            val loader=android.content.res.loader.ResourcesLoader()
            android.os.ParcelFileDescriptor.open(java.io.File(path),android.os.ParcelFileDescriptor.MODE_READ_ONLY).use{
                loader.addProvider(android.content.res.loader.ResourcesProvider.loadFromApk(it))
            }
            val isolated=base.createConfigurationContext(android.content.res.Configuration(base.resources.configuration))
            isolated.resources.addLoaders(loader)
            super.attachBaseContext(object:android.content.ContextWrapper(isolated){override fun getClassLoader()=this@MainActivity.classLoader})
        }else super.attachBaseContext(base)
    }
    override fun onCreate(savedInstanceState:Bundle?){
        if(BuildConfig.HOST_INTEGRATED)setTheme(R.style.Theme_Mixi)
        super.onCreate(savedInstanceState);enableEdgeToEdge()
        window.isNavigationBarContrastEnforced=false
        store=UiStore(applicationContext,lifecycleScope)
        setContent {
            val appearance=store.values["manager.theme"]?:"0"
            val dark=when(appearance){"1"->false;"2"->true;else->isSystemInDarkTheme()}
            SideEffect {
                WindowCompat.getInsetsController(window,window.decorView).apply {
                    isAppearanceLightStatusBars=!dark
                    isAppearanceLightNavigationBars=!dark
                }
            }
            MiuixTheme(colors=if(dark)darkColorScheme() else lightColorScheme()){Manager(store)}
        }
    }
    override fun onResume(){super.onResume();if(::store.isInitialized)store.refresh()}
    override fun onStop(){if(::store.isInitialized)store.stopReads();super.onStop()}
}

private data class Screen(val route:String):NavKey

@Composable private fun Manager(store:UiStore){
    var tab by rememberSaveable{mutableIntStateOf(0)}
    val rootPager=rememberPagerState(initialPage=tab,pageCount={3})
    val focus=LocalFocusManager.current
    val keyboard=LocalSoftwareKeyboardController.current
    LaunchedEffect(tab){
        focus.clearFocus(force=true);keyboard?.hide()
        rootPager.animateScrollToPage(tab,animationSpec=folmeSpring(damping=.95f,response=.4f,visibilityThreshold=.5f))
    }
    val stack=remember{navBackStackOf(Screen("root"))}
    fun open(route:String){if((stack.lastOrNull() as? Screen)?.route!=route)stack.add(Screen(route))}
    fun back(){if(stack.size>1)stack.removeAt(stack.lastIndex)}
    BackHandler(stack.size==1&&tab!=0){tab=0}
    NavDisplay(backStack=stack,onBack={back()}){
        entry<Screen>{screen->
    val pageStates=rememberSaveableStateHolder()
    val detail=screen.route.takeUnless{it=="root"}.orEmpty()
    val title=if(detail.isEmpty())listOf("Mixi Framework","Tính năng","Thông tin")[tab] else if(detail=="hmc")"HyperMusicCover Enhanced" else pages[detail]?:"Ghi nhận & Giấy phép"
    val surface=MiuixTheme.colorScheme.surface
    val backdrop=rememberLayerBackdrop{drawRect(surface);drawContent()}
    val tabScrolls=(0..2).map{index->key(screen,index){MiuixScrollBehavior(canScroll={detail.isEmpty()&&tab==index&&!rootPager.isScrollInProgress})}}
    val detailScroll=key(screen){MiuixScrollBehavior()}
    val scrollBehavior=if(detail.isEmpty())tabScrolls[tab] else detailScroll
    val barColors=BlurDefaults.blurColors(blendColors=listOf(BlendColorEntry(color=surface.copy(alpha=.38f))))
    Scaffold(popupHost={
        MiuixTheme(colors=MiuixTheme.colorScheme.copy(surfaceContainer=Color.Transparent)){MiuixPopupHost()}
    },topBar={
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
        val pageContent: @Composable (String)->Unit = {route->
            val pageScroll=when(route){"tab0"->tabScrolls[0];"tab1"->tabScrolls[1];"tab2"->tabScrolls[2];else->detailScroll}
            pageStates.SaveableStateProvider(route){Column(Modifier.fillMaxSize().nestedScroll(pageScroll.nestedScrollConnection)
                .verticalScroll(rememberScrollState()).padding(top=padding.calculateTopPadding(),bottom=padding.calculateBottomPadding())
                .padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
                CompositionLocalProvider(LocalMenuBackdrop provides backdrop){
                when(route){
                    "tab0"->{
                        FrameworkStatusCard(store)
                        Group("Tính năng"){
                            ArrowPreference(title="HyperMusicCover Enhanced",summary="Ảnh bìa, lời bài hát và Lockscreen Island",onClick={open("hmc")})
                            ArrowPreference(title="Tùy chỉnh Soft Glass",summary="Độ mờ, độ trong và phản chiếu",onClick={open("appearance")})
                            ArrowPreference(title="Tiện ích mở rộng",summary="Thanh cử chỉ, màn hình khóa và thanh trạng thái",onClick={open("extensions")})
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
                            Group("Âm nhạc & Màn hình khóa"){ArrowPreference(title="HyperMusicCover Enhanced",summary="8 nhóm tùy chỉnh",onClick={open("hmc")})}
                            Group("Giao diện hệ thống"){
                                ArrowPreference(title="Tùy chỉnh Soft Glass",onClick={open("appearance")})
                                ArrowPreference(title="Tiện ích mở rộng",onClick={open("extensions")})
                            }
                        }else{
                            val found=store.catalog.filter{it.title.contains(query,true)}
                            Group("${found.size} kết quả"){
                                if(found.isEmpty())BasicComponent(title="Không tìm thấy tùy chỉnh",summary="Thử một từ khóa khác.")
                                found.forEach{option->ArrowPreference(title=option.title,summary=pages[option.page],onClick={open(option.page)})}
                            }
                        }
                        PreviewNote(store)
                    }
                    "tab2"->{
                        val infoContext=LocalContext.current
                        val frameworkVersion by produceState<String?>(null,infoContext){
                            if(BuildConfig.HOST_INTEGRATED)value=withContext(Dispatchers.IO){
                                runCatching{infoContext.packageManager.getPackageInfo("com.mixi.host",0).versionName}.getOrNull()
                            }
                        }
                        Group("Mixi Framework"){
                            BasicComponent(title="Manager",summary=BuildConfig.VERSION_NAME)
                            BasicComponent(title="Framework",summary=frameworkVersion?:"Chưa xác nhận")
                            BasicComponent(title="ROM Maintainer",summary="Kien Vu")
                        }
                        Group("Giao diện"){
                            GlassDropdownPreference(items=listOf("Theo hệ thống","Sáng","Tối"),selectedIndex=(store.values["manager.theme"]?:"0").toInt(),title="Chế độ màu",onSelectedIndexChange={store.set("manager.theme",it.toString())})
                        }
                        Group("Ghi nhận"){
                            ArrowPreference(title="Ghi nhận & Giấy phép",onClick={open("credits")})
                        }
                    }
                    "hmc"->{
                        PreviewNote(store)
                        Card {SwitchPreference(title="HyperMusicCover Enhanced",summary="Giữ các tùy chỉnh khi tắt; dùng cơ chế tải lại hiện có",checked=store.values["enhanced.master"]!="false",onCheckedChange={store.set("enhanced.master",it.toString())})}
                        Group("Cá nhân hóa"){
                            pages.filterKeys{it !in listOf("appearance","extensions")}.forEach{(page,name)->
                                ArrowPreference(title=name,summary="${store.byPage[page].orEmpty().size} tùy chỉnh",onClick={open(page)})
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
                    "appearance"->SoftGlassPage(store)
                    "immersive"->HyperImmersivePage(store)
                    else->{
                        PreviewNote(store)
                        if(route=="island")IslandDemo(colorOs=(store.values["mini.pillStyle"]?:"0")=="0")
                        if(route=="extensions")Card{BasicComponent(title=if(store.applyingExtensions)"Đang áp dụng…" else "Áp dụng",summary="Khởi động lại SystemUI để nạp các tùy chọn pin. Giữ nguyên cấu hình đã lưu.",enabled=store.connected&&!store.applyingExtensions,onClick={store.applyExtensions()})}
                        val controls=store.byPage[route].orEmpty()
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
                }
            }}
        }
        if(detail.isEmpty()){
            HorizontalPager(state=rootPager,userScrollEnabled=false,beyondViewportPageCount=1,modifier=Modifier.fillMaxSize()){index->pageContent("tab$index")}
        }else pageContent(pageKey)
        }
    }
    }
    }
}

@Composable private fun FrameworkStatusCard(store:UiStore){
    val light=MiuixTheme.colorScheme.surface.luminance()>.5f
    val content=if(store.connected){if(light)Color(0xFF173E25) else Color(0xFFF0F8F1)} else MiuixTheme.colorScheme.onSurface
    val tint=content.copy(alpha=.65f)
    Box(Modifier.fillMaxWidth().heightIn(min=164.dp)
        .background(if(store.connected){if(light)Color(0xFFE0F1E4) else Color(0xFF203D2B)} else MiuixTheme.colorScheme.surfaceContainer,RoundedCornerShape(28.dp))){
        Canvas(Modifier.align(Alignment.CenterEnd).padding(end=16.dp).size(128.dp)){val c=if(store.connected){if(light)Color(0xFF278844) else Color(0xFF70D478)} else Color.Gray;drawCircle(c,radius=size.minDimension/2-5.dp.toPx(),style=androidx.compose.ui.graphics.drawscope.Stroke(9.dp.toPx()));if(store.connected){drawPath(androidx.compose.ui.graphics.Path().apply{moveTo(size.width*.26f,size.height*.51f);lineTo(size.width*.43f,size.height*.67f);lineTo(size.width*.77f,size.height*.33f)},c,style=androidx.compose.ui.graphics.drawscope.Stroke(10.dp.toPx(),cap=androidx.compose.ui.graphics.StrokeCap.Round,join=androidx.compose.ui.graphics.StrokeJoin.Round))}}
        Column(Modifier.padding(22.dp).padding(end=70.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Text("MIXI",fontSize=12.sp,color=content,modifier=Modifier.background(tint.copy(alpha=.12f),RoundedCornerShape(10.dp)).padding(horizontal=10.dp,vertical=4.dp))
            Text(if(store.connected)"Đã kết nối" else "Chưa kết nối",fontSize=26.sp,color=content)
            Text("Mixi Framework",fontSize=13.sp,color=tint)
        }
    }

}

@Composable private fun PreviewNote(store:UiStore){if(!store.connected)Card{BasicComponent(title="Chưa kết nối framework",summary="Mở lại từ Cài đặt → Mixi Framework")}}
@Composable internal fun Group(title:String,content:@Composable ColumnScope.()->Unit){
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text(title,modifier=Modifier.padding(start=12.dp,top=4.dp),fontSize=14.sp,color=MiuixTheme.colorScheme.onSurface.copy(alpha=.55f))
        Card(content=content)
    }
}
@Composable internal fun OptionRow(option:Option,store:UiStore,onPreviewChange:((String,String)->Unit)?=null){
    val current=store.value(option)
    when(option.kind){
        "bool"->SwitchPreference(title=option.title,summary=if(option.key=="appearance.gestureMotion")"Bật chức năng này có thể tăng nhẹ điện năng tiêu thụ" else null,checked=current=="true",onCheckedChange={store.set(option.key,it.toString())})
        "choice"->GlassDropdownPreference(items=option.labels,selectedIndex=option.values.indexOf(current).coerceAtLeast(0),title=option.title,onSelectedIndexChange={store.set(option.key,option.values[it])})
        "range","time"->{
            var draft by remember(option.key,current){mutableFloatStateOf(current.toFloat())}
            SliderPreference(title=option.title,value=draft,valueRange=option.min.toFloat()..option.max.toFloat(),valueText="${draft.toInt()}${option.unit}",onValueChange={draft=it;onPreviewChange?.invoke(option.key,it.toInt().toString())},onValueChangeFinished={
                val value=draft.toInt();store.set(option.key,(if(option.kind=="time"&&value in 1..9)10 else value).toString())
            })
        }
        else->Column(Modifier.padding(16.dp)){
            Text(option.title)
            if(option.secret&&!BuildConfig.HOST_INTEGRATED) Text("Khóa API chưa được lưu ở bản xem trước",fontSize=14.sp,color=MiuixTheme.colorScheme.onSurface.copy(alpha=.55f))
            else {
                var text by remember(option.key,current){mutableStateOf(current)}
                val focus=LocalFocusManager.current
                fun commit(){if(text!=current&&(!option.key.endsWith("waveCustom")||text.matches(Regex("#[0-9A-Fa-f]{8}"))))store.set(option.key,text)}
                TextField(value=text,onValueChange={text=it.take(4096)},label=option.title,singleLine=true,
                    visualTransformation=if(option.secret)androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                    keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(imeAction=androidx.compose.ui.text.input.ImeAction.Done,keyboardType=if(option.secret)androidx.compose.ui.text.input.KeyboardType.Password else androidx.compose.ui.text.input.KeyboardType.Text,autoCorrectEnabled=!option.secret),
                    keyboardActions=androidx.compose.foundation.text.KeyboardActions(onDone={commit();focus.clearFocus()}),
                    modifier=Modifier.fillMaxWidth().onFocusChanged{if(!it.hasFocus)commit()})
            }
        }
    }
}
