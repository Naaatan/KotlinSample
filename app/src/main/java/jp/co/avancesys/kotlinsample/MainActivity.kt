package jp.co.avancesys.kotlinsample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import jp.co.avancesys.kotlinsample.recycleView.RecycleViewHolder
import jp.co.avancesys.kotlinsample.recycleView.RecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), RecycleViewHolder.ItemClickListener {

    /**
     * 定数
     */
    companion object {
        private val TAG = "MainActivity"
        private const val ACTIVITY_APP_LIST = "AppList"
        private const val ACTIVITY_DRAW_LINE = "DrawLine"
        private const val ACTIVITY_CAMERA_CAPTURE = "CameraCapture"
        private const val ACTIVITY_CAMERA_CAPTURE2 = "CameraCapture2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val menu = arrayListOf(
            ACTIVITY_APP_LIST,
            ACTIVITY_DRAW_LINE,
            ACTIVITY_CAMERA_CAPTURE,
            ACTIVITY_CAMERA_CAPTURE2)

        recyclerViewMenuList.adapter = RecyclerAdapter(this, this, menu)
        recyclerViewMenuList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun onItemClick(view: View, position: Int, name: String) {
        when (name) {
            ACTIVITY_APP_LIST -> startActivity(Intent(this, AppListActivity::class.java))
            ACTIVITY_DRAW_LINE -> startActivity(Intent(this, DrawLineActivity::class.java))
            ACTIVITY_CAMERA_CAPTURE -> startActivity(Intent(this, CameraCaptureActivity::class.java))
            ACTIVITY_CAMERA_CAPTURE2 -> startActivity(Intent(this, CameraCaptureActivity2::class.java))

        }
    }
}
