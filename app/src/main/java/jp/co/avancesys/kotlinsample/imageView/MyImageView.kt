package jp.co.avancesys.kotlinsample.imageView

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet

class MyImageView: AppCompatImageView {

    /**
     * オブジェクト移動に関するデータクラス
     */
    data class CoordinateData(
        var preDx: Int = 0,
        var preDy: Int = 0,
        var dx: Int = 0,
        var dy: Int = 0
    )

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}