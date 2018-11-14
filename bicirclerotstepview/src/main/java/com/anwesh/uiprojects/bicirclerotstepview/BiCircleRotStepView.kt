package com.anwesh.uiprojects.bicirclerotstepview

/**
 * Created by anweshmishra on 14/11/18.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val parts : Int = 4
val STROKE_FACTOR : Int = 60
val SIZE_FACTOR : Int = 3
val scaleFactor : Double = 0.5
val scGap : Float = 0.05f

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.getInverse(), Math.max(0f, this - i * n.getInverse())) * n

fun Int.getInverse() : Float = 1f / this

fun Float.getScaleFactor() : Float = Math.floor(this / scaleFactor).toFloat()

fun Int.getMirrorMultiplier(k : Float) : Float = this * (1 - k) + k

fun Float.updateScale(dir : Float, a : Int) : Float = scGap * dir * a.getMirrorMultiplier(getScaleFactor())

class BiCircleRotStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}