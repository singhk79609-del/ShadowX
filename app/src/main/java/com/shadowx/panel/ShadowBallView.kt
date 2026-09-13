package com.shadowx.panel

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.View
import kotlin.math.min

class ShadowBallView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val logoBitmap: Bitmap

    private var rotation = 0f

    init {

        val drawable =
            context.resources.getDrawable(
                R.drawable.shadow_x_logo,
                context.theme
            )

        logoBitmap =
            (drawable as BitmapDrawable).bitmap

        ringPaint.style = Paint.Style.STROKE
        ringPaint.strokeWidth = 5f
        ringPaint.isAntiAlias = true

        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        startRotation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size =
            min(width, height).toFloat()

        val centerX =
            width / 2f

        val centerY =
            height / 2f

        val radius =
            size * 0.43f

        /*
         * Rotating neon ring
         */

        canvas.save()

        canvas.rotate(
            rotation,
            centerX,
            centerY
        )

        val gradient =
            SweepGradient(
                centerX,
                centerY,
                intArrayOf(
                    Color.RED,
                    Color.MAGENTA,
                    Color.RED,
                    Color.YELLOW,
                    Color.RED
                ),
                null
            )

        ringPaint.shader = gradient

        ringPaint.setShadowLayer(
            14f,
            0f,
            0f,
            Color.RED
        )

        canvas.drawCircle(
            centerX,
            centerY,
            radius,
            ringPaint
        )

        canvas.restore()


        /*
         * Logo
         */

        val logoSize =
            size * 0.76f

        val left =
            centerX - logoSize / 2f

        val top =
            centerY - logoSize / 2f

        val destination =
            RectF(
                left,
                top,
                left + logoSize,
                top + logoSize
            )

        paint.alpha = 255

        canvas.drawBitmap(
            logoBitmap,
            null,
            destination,
            paint
        )
    }


    private fun startRotation() {

        post(object : Runnable {

            override fun run() {

                rotation += 4f

                if (rotation >= 360f) {
                    rotation -= 360f
                }

                invalidate()

                postDelayed(
                    this,
                    30
                )
            }
        })
    }
}
