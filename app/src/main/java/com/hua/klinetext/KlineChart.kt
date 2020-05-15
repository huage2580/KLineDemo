package com.hua.klinetext

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import org.json.JSONArray
import kotlin.math.max
import kotlin.math.min

/**
 * 简易 K 线 图
 * @description:
 * @author: asus-hua
 * @date: 2020/5/15 14:05
 */
class KlineChart : View{

    var kDatas = mutableListOf<KData>()
    var kDrawables = mutableListOf<KDrawable>()

    private var gestureDetector: GestureDetector
    private var offsetX = 0



    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xffF5F5F5.toInt()
    }
    val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        textSize = 12.dp()
    }
    val kLineWitdh = 10.dp().toInt()
    val kLinePadding = 20.dp().toInt()

    val bounds = Rect(0,0,0,0)

    init {
        fakeData()
        gestureDetector = GestureDetector(context, object :GestureDetector.SimpleOnGestureListener(){
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                offsetX -= distanceX.toInt()
                genData()
                postInvalidateOnAnimation()
                return true
            }

            override fun onDown(e: MotionEvent?) = true
        })
    }

    fun genData(){
        val limitCanOffset = measuredWidth - kLineWitdh * (kDatas.size - 1)
        offsetX = min(0,offsetX)
        offsetX = max(offsetX,limitCanOffset)

        //区间最大最小
        var rangeMin = Double.MAX_VALUE
        var rangeMax = Double.MIN_VALUE

        val canShow = (measuredWidth / kLineWitdh) + 1
//        println("计算位移 ${offsetX} ,${limitCanOffset}")
        val leftIndex = max(- offsetX / kLineWitdh,0)
        val rightIndex = min(kDatas.size -1,leftIndex + canShow)
        println("$canShow , $leftIndex | $rightIndex")
        val subList = kDatas.subList(leftIndex,rightIndex)
        subList.forEach {
            rangeMax = max(it.max,rangeMax)
            rangeMin = min(it.min,rangeMin)
        }
        kDrawables.clear()
        //生成drawable
        subList.forEach {
            it.rangeMax = rangeMax
            it.rangeMin = rangeMin
            kDrawables.add(KDrawable(kData = it))
        }
    }

    private fun fakeData() {
        val json = JSONArray(data_json)
        for(i in 0 until json.length()){
            val obj = json.getJSONObject(i)
            val open = obj.getDouble("Open")
            val close = obj.getDouble("Close")
            val max = obj.getDouble("High")
            val min = obj.getDouble("Low")
            kDatas.add(KData(open, close, max, min))
        }
    }

    override fun onDraw(canvas: Canvas) {
        //background
        canvas.drawRect(bounds,paint)
        canvas.save()
        canvas.translate((offsetX % kLineWitdh).toFloat(),0.0f)
        //lines?
        kDrawables.forEachIndexed{ index,kDrawable ->
            val left = index * kLineWitdh
//            val right = measuredWidth - index * kLineWitdh
            kDrawable.setBounds(left,kLinePadding,left + kLineWitdh,measuredHeight - kLinePadding)
//            kDrawable.setBounds(right - kLineWitdh,0,right,measuredHeight)
            kDrawable.draw(canvas)

            if (kDrawable.kData.max == kDrawable.kData.rangeMax){
                canvas.drawCircle(kDrawable.topPoint.x,kDrawable.topPoint.y,10.0f,pointPaint)
            }
            if (kDrawable.kData.min == kDrawable.kData.rangeMin){
                canvas.drawCircle(kDrawable.bottomPoint.x,kDrawable.bottomPoint.y,10.0f,pointPaint)
            }
        }
        canvas.restore()

        val rMax = kDrawables[0].kData.rangeMax
        val rMin = kDrawables[0].kData.rangeMin

        for (i in 0..4){
            val value =String.format("%.2f",(rMax -(rMax-rMin)* i/4 ))
            println(value)
            val y = i/4.0f * measuredHeight -kLinePadding
            print(y)
            canvas.drawText(value,0.0f, y,pointPaint)
        }



    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        bounds.right = measuredWidth
        bounds.bottom = measuredHeight
        genData()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private fun Number.dp() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),Resources.getSystem().displayMetrics)
}