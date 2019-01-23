package jp.co.avancesys.kotlinsample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
import com.google.android.cameraview.CameraView
import kotlinx.android.synthetic.main.activity_camera_capture_2.*
import permissions.dispatcher.*
import java.io.IOException

@RuntimePermissions
class CameraCaptureActivity2 : AppCompatActivity(),
    View.OnTouchListener,
    GestureDetector.OnGestureListener,
    ScaleGestureDetector.OnScaleGestureListener {

    companion object {
        private val TAG = CameraCaptureActivity2::class.java.simpleName
    }

    private var backgroundHandler: Handler? = null
    private var gesture: GestureDetector? = null
    private var scaleGesture: ScaleGestureDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_capture_2)

        title = "Camera Capture2"

        if (savedInstanceState == null) {
            cameraStartWithPermissionCheck()
        }

        gesture = GestureDetector(this, this).apply { setIsLongpressEnabled(false) }
        scaleGesture = ScaleGestureDetector(this, this)
        layoutContentParent.setOnTouchListener(this)

        buttonCaptureCamera.setOnClickListener {
            capture()
        }
        buttonCaptureCamera.visibility = View.GONE

        // CameraViewのコールバック設定
        cameraView.addCallback(MyCameraCallback(this))
    }


    override fun onResume() {
        super.onResume()
        cameraStart()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }


    @NeedsPermission(Manifest.permission.CAMERA)
    fun cameraStart() {
        cameraView.start()
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun showDenied() {
        // 権限を許可されなかったとき
        when {
            PermissionUtils.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "カメラを使用する権限を取得できませんでした", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    fun showRational(request: PermissionRequest) {
        // 再度、権限を要求
        AlertDialog.Builder(this)
            .setMessage("カメラ使用の権限を取得する必要があります")
            .setPositiveButton("許可") { _, _ -> request.proceed() }
            .setNegativeButton("今はしない", null)
            .create()
            .show()
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun showNeverAsk() {
        // 今後表示しないを選択
        AlertDialog.Builder(this)
            .setTitle("カメラを利用できません")
            .setMessage("設定 > 許可から権限を許可して下さい")
            .setPositiveButton("設定画面") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("今はしない", null)
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    /**
     * バックグランドのハンドラーを取得
     */
    fun getBackgroundHandler(): Handler? {
        if (backgroundHandler == null) {
            val thread = HandlerThread("backgroundCap2")
            thread.start()
            backgroundHandler = Handler(thread.looper)
        }

        return backgroundHandler
    }

    /**
     * キャプチャー
     */
    private fun capture() {
        cameraView.takePicture()
    }

    /**
     * カメラコールバッククラス
     */
    private class MyCameraCallback(private val camCapActivity: CameraCaptureActivity2) : CameraView.Callback() {

        override fun onPictureTaken(cameraView: CameraView?, data: ByteArray?) {
            Log.d(TAG, "onPictureTaken: dataSize = ${data?.size}")

            camCapActivity.getBackgroundHandler()?.let { handler ->
                handler.post {
                    data?.let {
                        save(it)

                        val intent = Intent(camCapActivity, CaptureSubActivity::class.java)
                        camCapActivity.runOnUiThread {
                            camCapActivity.startActivity(intent)
                        }
                    }
                }
            }
        }

        /**
         * ローカルフォルダに保存
         */
        private fun save(data: ByteArray) {
            try {
                camCapActivity.openFileOutput(CaptureSubActivity.PICT_NAME, Context.MODE_PRIVATE).use {
                    it.write(data)
                }
            } catch (e: IOException) {
                Log.w(TAG, e)
            }
        }
    }

    /**
     * View.OnTouchListener
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        // TODO: 2019/01/14 1本指でタッチされた場合はGestureDetector。2本指でタッチされた場合はScaleDetectorを使用する。
        event?.let { e ->
            return if (e.pointerCount == 1) {
                gesture?.onTouchEvent(e) ?: true
            } else {
                scaleGesture?.onTouchEvent(e) ?: true
            }
        } ?: return true
    }

    /**
     * GestureDetector.OnGestureListener
     *
     * 指が画面に押下されて，少しだけ画面にとどまっていると呼び出される
     * 指が離れたときに呼び出されるのではない．
     * 移動させずに指を離すとonSingleTapUpが呼び出される．
     * 少しもとどまらずに指を移動させると呼び出されない．
     */
    override fun onShowPress(event: MotionEvent?) {
    }

    /**
     * GestureDetector.OnGestureListener
     *
     * 指が画面に押下されて，画面から離れると呼び出される．
     * UPのイベントを拾って呼び出してるイメージ．
     * ただし，指が少しでも移動してしまう，もしくは長押したときは呼び出されない．
     */
    override fun onSingleTapUp(event: MotionEvent?): Boolean {
        capture()
        return true
    }

    /**
     * GestureDetector.OnGestureListener
     *
     * 指が画面に押下されたら呼び出される．
     * とにかく押下されたら呼び出される．
     * 要するにDOWNのイベントを拾ってる．
     * 基本的に返り値はtrueにしておく
     * 返さない場合はonSingleTapUpが呼ばれなかったりonLongPressが呼ばれたりとにかく挙動がおかしくなる．
     */
    override fun onDown(event: MotionEvent?): Boolean {
        return true
    }

    /**
     * GestureDetector.OnGestureListener
     *
     * 指で画面上をサッとなぞると呼び出される．
     * スクロールで加速度をつけたいときとかに使う．
     * onScrollが呼び出されて指が離れたときに加速度がついていると判断されると呼び出される
     * 第1，第2引数はonScrollと一緒，第3，第4引数は速度が入っている．
     * 1秒間に移動するピクセル数．プラスマイナスはonScrollと一緒．
     */
    override fun onFling(event: MotionEvent?, eventCurrent: MotionEvent?, p2: Float, p3: Float): Boolean {
        return false
    }

    /**
     * GestureDetector.OnGestureListener
     *
     * 指が画面に押下されて，画面上を移動すると呼び出される．
     * MOVEイベントを拾っているイメージ．
     * 指が離れたときに呼び出されるメソッドが無いので，スクロールの開始と終了を管理したい場合は適していない
     * そもそも設計としてonScrollが呼ばれたら1回の処理で完結させる方が良いと思う．
     * MotionEventが2つとfloatが2つ渡される．
     * 第1引数のMotionEventがScrollの起点となる場所，第2引数が現在の場所．
     * 第3引数と第4引数が移動距離を表すfloat値．
     * onScrollを呼び出した前回の場所からどれだけ動いたかが格納されている
     * :起点から現在までの距離では無い（計算すりゃ分かるし）
     * 方向はX軸・Y軸の正方向に指を動かすとマイナスになる．
     * 画面をスクロールさせる場合は，表示している座標にそのまま足せば良い．
     */
    override fun onScroll(eventStart: MotionEvent?, eventCurrent: MotionEvent?, p2: Float, p3: Float): Boolean {
        return false
    }

    /**
     * GestureDetector.OnGestureListener
     *
     * 指が画面に押下されてじーっとしていると呼び出される．
     * 指が離れた時に呼び出されるのではなく，一定時間が経過すると呼び出される
     * 少しでも移動してしまうと呼び出されない．
     * 移動してからじーっと止まっていても呼び出されない．
     * setIsLongpressEnabledメソッドを使って無効化できる．
     * 使わないなら無効化しておく方がいい．
     */
    override fun onLongPress(event: MotionEvent?) {
    }

    /**
     * ScaleGestureDetector.OnScaleGestureListener
     *
     * 2本指でピンチイン・アウト処理が始まったら呼び出される．
     * 2本指タッチでは呼び出されない．回転操作でも呼び出されない．純粋にピンチイン・ピンチアウトで呼び出される．
     * 他の処理を中断させたりするときに使用．
     */
    override fun onScaleBegin(event: ScaleGestureDetector?): Boolean {
        return false
    }

    /**
     * ScaleGestureDetector.OnScaleGestureListener
     */
    override fun onScaleEnd(event: ScaleGestureDetector?) {
    }

    /**
     * ScaleGestureDetector.OnScaleGestureListener
     *
     * onScaleBeginからonScaleEndの間中ずっと呼び出される
     * 指が動いたかどうかは関係無い．定期的にずーっと呼び出される．
     * 引数のScaleGestureDetectorを使用してどこを中心にどの程度ピンチイン・ピンチアウトされたかを取得できる．
     * 倍率はgetScaleFactor，中心（焦点）はgetFocusX, getFocusYを使う．
     * 倍率はonScaleが前回呼ばれてから何倍になっったかの数値が入っている
     * つまり2本指で2倍に拡大したあとじーっとしていると，最初に2が入っているがその後はずっと1．
     */
    override fun onScale(event: ScaleGestureDetector?): Boolean {
        // TODO: 2019/01/14 ズーム、ズームアウト(CameraViewにサポートされていなかった)
        return false
    }
}
