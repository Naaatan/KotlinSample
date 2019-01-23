package jp.co.avancesys.kotlinsample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_capture_sub.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class CaptureSubActivity : AppCompatActivity() {

    companion object {
        private val TAG = "CaptureSubActivity"
        const val BUNDLE_CAPTURE = "Bundle_capture"
        const val PICT_NAME = "picture.jpg"
    }

    private var backgroundHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture_sub)

        title = "Capture"

        // TODO: 2019/01/11 intentに内包できるデータ容量をオーバーしたため、ファイル読込を採用
//        intent?.extras?.getParcelable<Bitmap>(BUNDLE_CAPTURE)?.let {
//            imageViewCapture.setImageBitmap(it)
//        }

//        intent?.extras?.getByteArray(BUNDLE_CAPTURE)?.let {
//            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
//            imageViewCapture.setImageBitmap(bitmap)
//        }

        readPicture()
    }

    /**
     * バックグランドのハンドラーを取得
     */
    private fun getBackgroundHandler(): Handler? {
        if (backgroundHandler == null) {
            val thread = HandlerThread("backgroundCapSub")
            thread.start()
            backgroundHandler = Handler(thread.looper)
        }

        return backgroundHandler
    }

    /**
     * キャプチャーファイル読み込み
     */
    private fun readPicture() {
        getBackgroundHandler()?.let { handler ->
            handler.post {
                val buffer = ByteArray(1024)

                try {
                    openFileInput(PICT_NAME).use { fis ->
                        ByteArrayOutputStream().use { baos ->
                            while (fis.read(buffer) > 0) {
                                baos.write(buffer)
                            }

                            val pictBytes = baos.toByteArray()
                            val bitmap = BitmapFactory.decodeByteArray(pictBytes, 0, pictBytes.size)
                            val fixBitmap = fixPictOrientation(bitmap)

                            Log.d(TAG, "readPicture: w= ${bitmap.width}px, h= ${bitmap.height}px")
                            Log.d(TAG, "readPicture: w(fix)= ${fixBitmap.width}px, h(fix)= ${fixBitmap.height}px")

                            runOnUiThread {
                                imageViewCapture.setImageBitmap(fixBitmap)
                            }
                        }
                    }
                } catch (e: IOException) {
                    Log.w(TAG, e)
                }
            }
        }
    }

    /**
     * プレビューの画像の向きを調整
     * (横 > 高さ である場合、表示の向きが撮影時の向きと異なるため)
     */
    private fun fixPictOrientation(bitmap: Bitmap): Bitmap {
        return if (bitmap.width > bitmap.height) {
            val matrix = Matrix().apply {
                postRotate(90f)
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }
}
