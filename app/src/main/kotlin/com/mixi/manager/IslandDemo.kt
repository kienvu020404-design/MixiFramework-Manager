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
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    DisposableEffect(drawable,lifecycle){
        val animated=drawable as? AnimatedImageDrawable
        fun update(){if(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))animated?.start() else animated?.stop()}
        animated?.repeatCount=AnimatedImageDrawable.REPEAT_INFINITE
        val observer=LifecycleEventObserver{_,_->update()}
        lifecycle.addObserver(observer);update()
        onDispose{lifecycle.removeObserver(observer);animated?.stop()}
    }
    Card{
        AndroidView(modifier=Modifier.fillMaxWidth().height(112.dp),
            factory={ImageView(it).apply{scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription="Demo Island ${if(colorOs)"ColorOS" else "HyperOS"}"}},
            update={it.setImageDrawable(drawable)})
    }
}
