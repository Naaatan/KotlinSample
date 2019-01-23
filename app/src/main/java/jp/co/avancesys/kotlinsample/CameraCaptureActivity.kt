package jp.co.avancesys.kotlinsample

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.camerakit.CameraKitView
import kotlinx.android.synthetic.main.activity_camera_capture.*

class CameraCaptureActivity : AppCompatActivity(), CameraKitView.ErrorListener {

    companion object {
        private val TAG = CameraCaptureActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_capture_2)

        title = "Camera Capture"


        buttonCaptureCamera.setOnClickListener {
            capture()
        }

        cameraKitView.errorListener = this
    }

    override fun onStart() {
        super.onStart()
        cameraKitView.onStart()
    }

    override fun onResume() {
        super.onResume()
        cameraKitView.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraKitView.onPause()
    }

    override fun onStop() {
        super.onStop()
        cameraKitView.onStop()
    }

    private fun capture() {
        // TODO: 2019/01/11 cameraKitViewのバグでキャプチャのコールバックが呼び出されず、フリーズする
        cameraKitView.captureImage { _, bytes: ByteArray ->
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val intent = Intent(this, CaptureSubActivity::class.java).apply {
                putExtra(CaptureSubActivity.BUNDLE_CAPTURE, bitmap)
            }
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onError(cameraKit: CameraKitView?, e: CameraKitView.CameraException?) {
        Log.d(TAG, "onError: ${e.toString()}")
    }

}
