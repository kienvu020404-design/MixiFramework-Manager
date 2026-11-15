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
    Card{BasicComponent(title="Xem trước Soft Glass",summary="Minh họa bố cục và hiệu ứng trong app. Vật liệu HyperOS chính xác và kết nối hệ thống sẽ được hoàn thiện sau.")}
    Group("Soft Glass"){controls.filter{it.kind=="bool"}.forEach{OptionRow(it,store)}}
    GlassSample(enabled,amount("appearance.blur",100f),amount("appearance.background",200f),
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
    Text("Kéo để xem minh họa ngay · Thả tay để lưu lựa chọn trên Manager",fontSize=13.sp,
        color=MiuixTheme.colorScheme.onSurface.copy(alpha=.55f),modifier=Modifier.padding(horizontal=12.dp))
}

@Composable private fun GlassSample(enabled:Boolean,blur:Float,background:Float,opacity:Float,reflection:Float){
    val backdrop=rememberLayerBackdrop{drawContent()}
    val surface=MiuixTheme.colorScheme.surfaceContainer
    val material=MiuixTheme.colorScheme.onSurface
    Card{
        Row(Modifier.fillMaxWidth().padding(18.dp),horizontalArrangement=Arrangement.SpaceBetween){
            Text("Mẫu vật liệu",fontSize=17.sp)
            Text(if(enabled)"Đang minh họa" else "Tùy chỉnh đang tắt",fontSize=12.sp,color=material.copy(alpha=.55f))
        }
        Box(Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(24.dp))){
            // Static locally drawn scene; no network, frame clock or continuously animated wallpaper.
            Canvas(Modifier.matchParentSize().layerBackdrop(backdrop)){
                drawRect(Brush.verticalGradient(listOf(Color(0xFF68A9D1),Color(0xFFE9D4AF),Color(0xFF41778C))))
                drawCircle(Color(0xFFFCE9B0),size.width*.1f,Offset(size.width*.76f,size.height*.24f))
                val hills=Path().apply{moveTo(0f,size.height*.57f);cubicTo(size.width*.25f,size.height*.28f,size.width*.34f,size.height*.75f,size.width*.7f,size.height*.44f);lineTo(size.width,size.height*.52f);lineTo(size.width,size.height);lineTo(0f,size.height);close()}
                drawPath(hills,Color(0xFF698C82))
                drawRect(Brush.verticalGradient(listOf(Color(0xFF77A6B1),Color(0xFF264E69)),startY=size.height*.62f),topLeft=Offset(0f,size.height*.62f),size=Size(size.width,size.height*.38f))
                drawRect(Color(0xFFEBDBBB),Offset(size.width*.22f,size.height*.41f),Size(size.width*.21f,size.height*.18f))
                val roof=Path().apply{moveTo(size.width*.18f,size.height*.42f);lineTo(size.width*.32f,size.height*.29f);lineTo(size.width*.47f,size.height*.42f);close()}
                drawPath(roof,Color(0xFF9C6D56))
                drawRect(Color(0xFF527B8F),Offset(size.width*.29f,size.height*.48f),Size(size.width*.05f,size.height*.11f))
                repeat(9){index->val y=size.height*(.65f+index*.035f);drawLine(Color.White.copy(alpha=.15f),Offset(size.width*(index%3)*.13f,y),Offset(size.width*(.45f+(index%4)*.15f),y),1.5f)}
            }
            val shape=RoundedCornerShape(28.dp)
            val tint=if(enabled) .18f+opacity/100f*.6f else .85f
            val panel=if(enabled)Modifier.textureBlur(backdrop=backdrop,shape=shape,
                blurRadius=blur*.35f+background*.08f,
                colors=BlurDefaults.blurColors(blendColors=listOf(BlendColorEntry(color=surface.copy(alpha=tint)))))
                else Modifier.background(surface.copy(alpha=tint),shape)
            Column(Modifier.align(Alignment.BottomCenter).padding(18.dp).fillMaxWidth()
                .then(panel)
                .border(if(enabled)1.dp else .5.dp,Brush.linearGradient(listOf(Color.White.copy(alpha=if(enabled).15f+reflection/200f*.65f else .1f),Color.White.copy(alpha=.06f))),shape)
                .padding(horizontal=22.dp,vertical=24.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                Text("Soft Glass",fontSize=22.sp,color=material)
                Text("Minh họa trực tiếp trong Manager",fontSize=13.sp,color=material.copy(alpha=.65f))
            }
        }
        Text("Minh họa UI · Chưa phải renderer HyperOS",fontSize=12.sp,color=material.copy(alpha=.55f),modifier=Modifier.padding(16.dp))
    }
}
