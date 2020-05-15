package com.hua.klinetext

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.TypedValue
import kotlin.math.max
import kotlin.math.min

class KDrawable(var kData: KData) : Drawable(){

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 1.dp() }
    private val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = Rect()

    var topPoint  = PointF()
    var bottomPoint = PointF()


    override fun draw(canvas: Canvas) {
        //画线
        val sX =bounds.centerX().toFloat()
        val sY = computeOffset(kData.max).toFloat()
        val eX = bounds.centerX().toFloat()
        val eY = computeOffset(kData.min).toFloat()
        canvas.drawLine(
            sX, sY,
            eX, eY,
            paint)
        // 画色块
        rectPaint.color = when{
            kData.open > kData.close -> 0xff35DF7E.toInt()
            kData.open < kData.close -> 0xffDF3536.toInt()
            else -> Color.DKGRAY
        }
        rect.left = bounds.left
        rect.right = bounds.right
        rect.top = computeOffset(max(kData.open,kData.close)).toInt()
        rect.bottom = computeOffset(min(kData.open,kData.close)).toInt()
        canvas.drawRect(rect,rectPaint)

        //记录坐标
        topPoint.set(sX,sY)
        bottomPoint.set(eX,eY)
    }

    fun computeOffset(value:Double):Double{
        return (kData.rangeMax - value)/(kData.rangeMax - kData.rangeMin) * bounds.height() + bounds.top
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun getOpacity() = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    private fun Number.dp() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),
        Resources.getSystem().displayMetrics)

}