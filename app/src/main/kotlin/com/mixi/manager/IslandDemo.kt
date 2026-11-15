// SPDX-License-Identifier: AGPL-3.0-only
// Copyright (C) 2026 Lewewe (HyperMusicCover-Enhanced)
// Adapted from original IslandDemo.kt; original Remotion WebP assets are reused unchanged.
package com.mixi.manager

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card

@Composable internal fun IslandDemo(colorOs:Boolean){
    val resources=LocalContext.current.resources
    val id=if(colorOs)R.raw.island_demo_coloros else R.raw.island_demo_hyperos
    val drawable by produceState<Drawable?>(null,id){
        value=withContext(Dispatchers.Default){ImageDecoder.decodeDrawable(ImageDecoder.createSource(resources,id))}
    }
    var visible by remember{mutableStateOf(true)}
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    DisposableEffect(drawable,lifecycle,visible){
        val animated=drawable as? AnimatedImageDrawable
        fun update(){if(visible&&lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))animated?.start() else animated?.stop()}
        animated?.repeatCount=AnimatedImageDrawable.REPEAT_INFINITE
        val observer=LifecycleEventObserver{_,_->update()}
        lifecycle.addObserver(observer);update()
        onDispose{lifecycle.removeObserver(observer);animated?.stop()}
    }
    Card{
        AndroidView(modifier=Modifier.fillMaxWidth().height(112.dp).onGloballyPositioned{val b=it.boundsInWindow();visible=b.height>0f&&b.bottom>0f&&b.top<it.findRootCoordinates().size.height},
            factory={ImageView(it).apply{scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription="Demo Island ${if(colorOs)"ColorOS" else "HyperOS"}"}},
            onReset=null,onRelease={(it.drawable as? AnimatedImageDrawable)?.stop();it.setImageDrawable(null)},
            update={if(it.drawable !== drawable)it.setImageDrawable(drawable)})
    }
}
