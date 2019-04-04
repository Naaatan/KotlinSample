package jp.co.avancesys.kotlinsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import jp.co.avancesys.kotlinsample.DrawView.LineView
import jp.co.avancesys.kotlinsample.imageView.MyImageView
import kotlinx.android.synthetic.main.activity_draw_line.*

class DrawLineActivity : AppCompatActivity(), View.OnTouchListener {

    companion object {
        private val TAG = DrawLineActivity::class.java.simpleName
    }

    private val mLineList: ArrayList<LineView> = arrayListOf()
    private var mTrackingLine: LineView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw_line)

        title = "Draw Line"

        pointer1.setOnTouchListener(this)
        pointer2.setOnTouchListener(this)

        buttonDrawLine.setOnClickListener {
            drawPointerLine(false)
        }

        buttonClearLines.setOnClickListener {
            clearLines()
        }

        switchTrackingLine.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            mTrackingLine = if (b) {
                drawPointerLine(true)
            } else {
                layoutPointerParent.removeView(mTrackingLine)
                null
            }
        }
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (view != null) {
                        // x, y位置取得
                        val newDx = event.rawX.toInt()
                        val newDy = event.rawY.toInt()

                        // クリックイベントを記載しないと警告が出る
                        view.performClick()
                        when (view.id) {
                            pointer1.id, pointer2.id -> pointerMove(view, newDx, newDy)
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    // レイアウトパラメータを更新する
                    if (view != null) {
                        when (view.id) {
                            pointer1.id, pointer2.id -> {
                                val marginLayoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
                                marginLayoutParams.setMargins(view.left, view.top, 0, 0)
                                // マージン更新
                                view.layoutParams = marginLayoutParams
                            }
                        }
                    }
                }
            }
        }

        return true
    }

    /**
     * ポインターをタッチダウンでドラッグしたときのイベント
     */
    private fun pointerMove(view: View, newDx: Int, newDy: Int) {

        // オブジェクトの位置を決定するローカル関数
        fun move(pointer: MyImageView, data: MyImageView.CoordinateData, newDx: Int, newDy: Int) {
            data.dx = pointer.left + (newDx - data.preDx)
            data.dy = pointer.top + (newDy - data.preDy)
            val imgW = data.dx + pointer.width
            val imgH = data.dy + pointer.height

            // 画像の位置を設定
            pointer.layout(data.dx, data.dy, imgW, imgH)
            //Log.d(TAG, "pointMove: dx=${data.dx}, dy=${data.dy}")

            // タッチした位置を古い位置とする
            data.preDx = newDx
            data.preDy = newDy

            // Viewのタグにデータクラスを格納
            pointer.tag = data
        }

        // ポインターオブジェクトの座標データを取得するローカル関数
        fun getCoordinateData(pointer: MyImageView): MyImageView.CoordinateData {
            return if ((pointer.tag) is MyImageView.CoordinateData) {
                (pointer.tag) as MyImageView.CoordinateData
            } else {
                MyImageView.CoordinateData(dx = pointer.left, dy = pointer.top)
            }
        }

        // 座標移動に関するデータクラスの初期化
        val data = view.tag?.let {
            if (it is MyImageView.CoordinateData) it else MyImageView.CoordinateData(newDx, newDy)
        } ?: MyImageView.CoordinateData(newDx, newDy)

        when (view.id) {
            pointer1.id -> {
                move(pointer1, data, newDx, newDy)

                if (switchTrackingLine.isChecked) {
                    val p1Coordinate = getCoordinateData(pointer1)
                    val p2Coordinate = getCoordinateData(pointer2)
                    trackingPointerLine(p1Coordinate, p2Coordinate)
                }
            }
            pointer2.id -> {
                move(pointer2, data, newDx, newDy)

                if (switchTrackingLine.isChecked) {
                    val p1Coordinate = getCoordinateData(pointer1)
                    val p2Coordinate = getCoordinateData(pointer2)
                    trackingPointerLine(p1Coordinate, p2Coordinate)
                }
            }
        }
    }

    /**
     * ポインター間に線を引く
     */
    private fun drawPointerLine(isTracking: Boolean): LineView {
        val centerP1X = pointer1.left + (pointer1.width / 2f)
        val centerP1Y = pointer1.top + (pointer1.height / 2f)
        val centerP2X = pointer2.left + (pointer2.width / 2f)
        val centerP2Y = pointer2.top + (pointer2.height / 2f)
        val pts = floatArrayOf(centerP1X, centerP1Y, centerP2X, centerP2Y)

        val lineView = LineView(this, pts, getColor(R.color.colorPrimary)).apply {
            isDashLine = isTracking
            if (isTracking) {
                lineColor = getColor(R.color.colorAccent)
            }
        }

        lineView.getLineLength()
        layoutPointerParent.addView(lineView)
        mLineList.add(lineView)

        return lineView
    }

    /**
     * ポインターの移動に合わせて線を追従させる
     */
    private fun trackingPointerLine(
        fromCoordinate: MyImageView.CoordinateData,
        toCoordinate: MyImageView.CoordinateData
    ) {
        // 2つのポインターの大きさは同じとする
        val widthCenter = pointer1.width / 2f
        val heightCenter = pointer1.height / 2f
        val fromX = fromCoordinate.dx + widthCenter
        val fromY = fromCoordinate.dy + heightCenter
        val toX = toCoordinate.dx + widthCenter
        val toY = toCoordinate.dy + heightCenter
        val pts = floatArrayOf(fromX, fromY, toX, toY)

        mTrackingLine?.let { it.drawLine(pts) }
    }

    /**
     * 線をクリア
     */
    private fun clearLines() {
        mTrackingLine?.let { lineView ->
            mLineList.minus(lineView).map { layoutPointerParent.removeView(it) }
        } ?: mLineList.map { layoutPointerParent.removeView(it) }

        mLineList.clear()
    }
}
