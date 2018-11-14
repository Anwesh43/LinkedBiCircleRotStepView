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
import android.util.Log

val nodes : Int = 5
val parts : Int = 4
val STROKE_FACTOR : Int = 60
val SIZE_FACTOR : Int = 3
val scaleFactor : Double = 0.5
val scGap : Float = 0.05f
val DELAY : Long = 30
val DEG_MAX : Float = 90f

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.getInverse(), Math.max(0f, this - i * n.getInverse())) * n

fun Int.getInverse() : Float = 1f / this

fun Float.getScaleFactor() : Float = Math.floor(this / scaleFactor).toFloat()

fun Int.getMirrorMultiplier(k : Float) : Float = this.getInverse() * (1 - k) + k

fun Float.updateScale(dir : Float, a : Int) : Float = scGap * dir * a.getMirrorMultiplier(getScaleFactor())

fun Int.getYFactor() : Int = this / 2

fun Int.getXFactor()  : Int = this % 2

fun Int.getRevFactor()  : Int = (this.getYFactor() + this.getXFactor()).getXFactor()

fun Canvas.drawBCRSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / 3
    val r : Float = size / 2
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = Math.min(w, h) / STROKE_FACTOR
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#0D47A1")
    save()
    translate(gap * (i + 1), h / 2)
    rotate(DEG_MAX * sc2)
    for(j in 0..(parts-1)) {
        val sc : Float = sc1.divideScale(j, parts)
        val degFactor : Int = j.getYFactor()
        val rFactor : Int = j.getRevFactor()
        val deg : Float = 180f + 180f * degFactor
        save()
        translate(-r + 2 * r * rFactor, 0f)
        drawArc(RectF(-r, -r, r, r), deg - 180f * sc, 180f * sc, false, paint)
        restore()
    }
    restore()
}

class BiCircleRotStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            val k : Float = scale.updateScale(dir, parts)
            scale += k
            Log.d("scale updated by", "$k")
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(DELAY)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BCRSNode(var i : Int, val state : State = State()) {

        private var next : BCRSNode? = null

        private var prev : BCRSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BCRSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBCRSNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BCRSNode {
            var curr : BCRSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BiCircleRotStep(var i : Int) {
        private var curr : BCRSNode = BCRSNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BiCircleRotStepView) {

        private val animator : Animator = Animator(view)

        private val bcrs : BiCircleRotStep = BiCircleRotStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            bcrs.draw(canvas, paint)
            animator.animate {
                bcrs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bcrs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : BiCircleRotStepView {
            val view : BiCircleRotStepView = BiCircleRotStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}