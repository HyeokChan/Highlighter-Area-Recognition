package kr.ac.kumoh.s20150088.imagepdf

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*

class MyView : View {

    //사각형의 좌표값
    companion object{
        lateinit var bmp: Bitmap
        var rect = ArrayList<Rect>()
        var containIndex = 0 //선택된 박스
    }
    //처음 터치했을때 x,y 좌표 시작값
    var x_start = 0f
    var y_start = 0f

    private val paint = Paint()
    private val containPaint = Paint()//선택된 박스에 대한 페인트객체 구별

    constructor(context: Context?):this(context,null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
        paint.setARGB(255,0,0,255)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1F

        containPaint.setARGB(255,255,0,0)
        containPaint.style = Paint.Style.STROKE
        containPaint.strokeWidth = 1F

        bmp = BitmapFactory.decodeResource(resources,R.drawable.pushbutton)
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(bmp,0F,0F,paint)
        for (i in 0 until rect.size){
            if(i == containIndex){
                canvas?.drawRect(rect[i],containPaint)
            }
            else{
                canvas?.drawRect(rect[i],paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var x_ = 0f
        var y_ = 0f
        when(event?.action) {
            MotionEvent.ACTION_DOWN->{
                x_start = event!!.x
                y_start = event!!.y
                for(i in 0 until rect.size){
                    if(rect[i].contains(x_start.toInt(), y_start.toInt()) == true){
                        containIndex = i
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE->{
                x_ = event!!.x
                y_ = event!!.y
                for(i in 0 until rect.size)
                {
                    if(rect[i].contains(x_.toInt(), y_.toInt()) == true){
                        if(x_start > (rect[i].left+rect[i].right)/2){
                            if(x_start<x_){
                                rect[i].right = rect[i].right+10
                                invalidate()
                            }
                            else if(x_start>x_){
                                rect[i].right = rect[i].right-10
                                invalidate()
                            }
                        }
                        else if(x_start < (rect[i].left+rect[i].right)/2){
                            if(x_start<x_){
                                rect[i].left = rect[i].left+10
                                invalidate()
                            }
                            else if(x_start>x_){
                                rect[i].left = rect[i].left-10
                                invalidate()
                            }
                        }
                    }
                }
            }
        }
        return true
    }
}