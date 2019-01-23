package jp.co.avancesys.kotlinsample.DrawView

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View

class LineView(context: Context?) : View(context) {

    companion object {
        private const val TAG = "LineView"
    }

    constructor(context: Context?, pts: FloatArray, lineColor: Int) : this(context) {
        this.pts = pts
        this.lineColor = lineColor
    }

    private var paint: Paint = Paint()
    private var path: Path = Path()
    private var pts: FloatArray = floatArrayOf(0f, 0f, 0f, 0f)
    var lineColor: Int = Color.BLUE
    var isDashLine: Boolean = false

    override fun onDraw(canvas: Canvas?) {
        // Paintの設定
        if (isDashLine) {
            setDashPaint()
        } else {
            setNormalPaint()
        }

        path.apply {
            rewind()
            moveTo(pts[0], pts[1])
            lineTo(pts[2], pts[3])
        }

        // 線を書く
        canvas?.let {
            //it.drawLines(pts, paint)
            it.drawPath(path, paint)
        }
    }

    /**
     * ペイントオブジェクト生成
     */
    private fun setNormalPaint() {
        paint.apply {
            isAntiAlias = true
            color = lineColor
            strokeWidth = 10f
            style = Paint.Style.STROKE
            pathEffect = CornerPathEffect(10f)
        }
    }

    /**
     * 点線オブジェクト生成
     */
    private fun setDashPaint() {
        paint.apply {
            isAntiAlias = true
            color = lineColor
            strokeWidth = 10f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(10.0f, 10.0f), 0f) // 5px描いたら5px描かないを繰り返す
        }
    }

    /**
     * 線の描画を更新
     */
    fun drawLine(pts: FloatArray) {
        this.pts = pts
        invalidate()
    }

    /**
     * 線の長さを取得
     */
    fun getLineLength() : Double{
        val xS = pts[0]
        val yS = pts[1]
        val xE = pts[2]
        val yE = pts[3]
        val width = Math.abs(xE - xS)
        val height = Math.abs(yE - yS)
        val length = Math.sqrt(Math.pow(width.toDouble(), 2.0) + Math.pow(height.toDouble(), 2.0))
        Log.d(TAG, "getLineLength: w=$width, h=$height, l=$length")

        return length
    }
}