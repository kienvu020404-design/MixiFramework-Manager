package com.mixi.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.blur.*
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal val LocalMenuBackdrop=staticCompositionLocalOf<LayerBackdrop?>{null}

/** Original Miuix popup positioning, motion and item semantics with an undimmed glass surface. */
@Composable internal fun GlassDropdownPreference(
    items:List<String>,selectedIndex:Int,title:String,onSelectedIndexChange:(Int)->Unit,
){
    var expanded by remember{mutableStateOf(false)}
    val haptics=LocalHapticFeedback.current
    val backdrop=LocalMenuBackdrop.current
    val scheme=MiuixTheme.colorScheme
    val shape=RoundedCornerShape(28.dp)
    val itemColors=DropdownDefaults.dropdownColors(containerColor=Color.Transparent,selectedContainerColor=Color.Transparent)
    BasicComponent(title=title,onClick={if(items.isNotEmpty()){haptics.performHapticFeedback(HapticFeedbackType.ContextClick);expanded=true}},enabled=items.isNotEmpty(),endActions={
        Text(items.getOrNull(selectedIndex).orEmpty(),fontSize=14.sp,textAlign=TextAlign.End,color=scheme.onSurfaceVariantActions,modifier=Modifier.weight(1f,false).padding(end=10.dp))
        DropdownArrowEndAction(scheme.onSurfaceVariantActions)
            OverlayListPopup(show=expanded,alignment=PopupPositionProvider.Align.End,enableWindowDim=false,
                onDismissRequest={expanded=false}){
                val material=if(backdrop!=null)Modifier.textureBlur(backdrop=backdrop,shape=shape,blurRadius=25f,noiseCoefficient=0f,
                    colors=BlurDefaults.blurColors(blendColors=listOf(BlendColorEntry(color=scheme.surfaceContainer.copy(alpha=.72f)))))
                    else Modifier.background(scheme.surfaceContainer,shape)
                Box(Modifier.widthIn(min=220.dp,max=288.dp).clip(shape).then(material)
                    .border(.5.dp,Brush.linearGradient(listOf(scheme.onSurface.copy(alpha=.14f),Color.Transparent,scheme.onSurface.copy(alpha=.06f))),shape)){
                    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())){
                        items.forEachIndexed{index,label->
                            Box(Modifier.fillMaxWidth().padding(horizontal=6.dp).clip(RoundedCornerShape(22.dp)),propagateMinConstraints=true){
                            DropdownImpl(item=DropdownItem(text=label,selected=index==selectedIndex,onClick={}),
                                optionSize=items.size,isSelected=index==selectedIndex,index=index,dropdownColors=itemColors,
                                onSelectedIndexChange={haptics.performHapticFeedback(HapticFeedbackType.Confirm);onSelectedIndexChange(index);expanded=false})
                            }
                        }
                    }
                }
            }
    })
}
