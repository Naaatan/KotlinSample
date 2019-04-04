package jp.co.avancesys.kotlinsample

import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.util.Log
import jp.co.avancesys.kotlinsample.DrawView.LineView
import kotlinx.android.synthetic.main.activity_opencv.*
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

/**
 * 参考(http://null-product.hatenablog.com/entry/2017/05/07/214743)
 */
class OpencvActivity : AppCompatActivity() {

    private var backgroundHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opencv)

        // 以下3行はOpenCV初期化のため必須
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onCreate: Error openCV")
        }

        buttonThreshold.setOnClickListener {
            readThresholdPict()
        }

        buttonDrawLine.setOnClickListener {
            drawLine()
        }

    }

    /**
     * バックグランドのハンドラーを取得
     */
    fun getBackgroundHandler(): Handler? {
        if (backgroundHandler == null) {
            val thread = HandlerThread("backgroundOpenCV")
            thread.start()
            backgroundHandler = Handler(thread.looper)
        }

        return backgroundHandler
    }

    /**
     * ARGB888変換
     */
    private fun convert2ARGB_8888(bitmap: Bitmap, config: Bitmap.Config): Bitmap {
        val convertBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, config)
        val canvas = Canvas(convertBitmap)
        val paint = Paint().apply { color = Color.BLACK }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return convertBitmap
    }

    /**
     * 2値化した画像を表示
     */
    private fun readThresholdPict() {
        getBackgroundHandler()?.let { handler ->
            handler.post {
                val bitmap = convert2ARGB_8888(
                    BitmapFactory.decodeResource(resources, R.drawable.opencv),
                    Bitmap.Config.ARGB_8888
                )

                val mat = Mat()
                Utils.bitmapToMat(bitmap, mat, true)

                val thresholdMat = getThreshold(mat)
                val bitmapThreshold =
                    Bitmap.createBitmap(thresholdMat.cols(), thresholdMat.rows(), Bitmap.Config.ARGB_8888)
                        .apply { Utils.matToBitmap(thresholdMat, this) }

                runOnUiThread {
                    imageViewOpencv.setImageBitmap(bitmapThreshold)
                }
            }
        }
    }

    /**
     * 認識したオブジェクトに線を引く
     */
    private fun drawLine() {

        getBackgroundHandler()?.let { handler ->
            handler.post {
                val bitmap = convert2ARGB_8888(
                    BitmapFactory.decodeResource(resources, R.drawable.opencv),
                    Bitmap.Config.ARGB_8888
                )

                val mat = Mat()
                Utils.bitmapToMat(bitmap, mat, true)

                val thresholdMat = getThreshold(mat)

                val scaleX = imageViewOpencv.width.toFloat() / bitmap.width.toFloat()

                val scaleY = imageViewOpencv.height.toFloat() / bitmap.height.toFloat()

                val marginX = imageViewOpencv.left.toFloat()

                val marginY = imageViewOpencv.top.toFloat()

                val contour2points = contour2point(getContour(thresholdMat))
                contour2points.map { points ->
                    for (i in 0..(points.size - 1)) {
                        val pts = if (i == (points.size - 1)) {
                            floatArrayOf(
                                points[i].x.toFloat() * scaleX + marginX,
                                points[i].y.toFloat() * scaleY + marginY,
                                points[0].x.toFloat() * scaleX + marginX,
                                points[0].y.toFloat() * scaleY + marginY
                            )
                        } else {
                            floatArrayOf(
                                points[i].x.toFloat() * scaleX + marginX,
                                points[i].y.toFloat() * scaleY + marginY,
                                points[i + 1].x.toFloat() * scaleX + marginX,
                                points[i + 1].y.toFloat() * scaleY + marginY
                            )
                        }

                        val lineView = LineView(this, pts, getColor(R.color.colorPrimary))

                        runOnUiThread {
                            layoutOpencvParent.addView(lineView)
                        }
                    }
                }

            }
        }

    }

    /**
     * 画像の2値化
     * (単純な2値化ではノイズが載ってしまうため、色空間をチャネル（RGBなど）に分離し、それぞれのチャネルに対して二値化を行う)
     */
    private fun getThreshold(mat: Mat): Mat {
        /*
         * 1-1 RGB空間チャンネルの取得
         *      画像をRGB空間の３チャネルに分離します。
         */
        val mat_rgb = mat.clone()
        val channels_rgb = ArrayList<Mat>()
        Core.split(mat_rgb, channels_rgb)

        /*
         * 1-2 RGB空間 -> グレースケール変換 -> 2値化
         *      RGB空間の３チャネルと、元の画像のグレースケールの差分を算出し、
         *      それぞれのチャンネル値を強調します。
         *      そして、その結果を2値化画像に変換します。
         */
        Imgproc.cvtColor(mat_rgb, mat_rgb, Imgproc.COLOR_RGB2GRAY)
        Core.subtract(channels_rgb[0], mat_rgb, channels_rgb[0])
        Core.subtract(channels_rgb[1], mat_rgb, channels_rgb[1])
        Core.subtract(channels_rgb[2], mat_rgb, channels_rgb[2])
        Imgproc.threshold(channels_rgb[0], channels_rgb[0], 0.0, 255.0, Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU)
        Imgproc.threshold(channels_rgb[1], channels_rgb[1], 0.0, 255.0, Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU)
        Imgproc.threshold(channels_rgb[2], channels_rgb[2], 0.0, 255.0, Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU)

        /*
         * 1-3 RGBの輪郭を取得
         *      RGB空間の３チャネルの二値化画像から四角形の座標を抽出します。
         */
        val contour_rgb0 = getContour(channels_rgb[0])
        val contour_rgb1 = getContour(channels_rgb[1])
        val contour_rgb2 = getContour(channels_rgb[2])


        /* 2-1：YUV空間チャネルの取得 */
        val mat_yuv = mat.clone()
        Imgproc.cvtColor(mat_yuv, mat_yuv, Imgproc.COLOR_BGR2YUV)
        val channels_yuv = ArrayList<Mat>()
        Core.split(mat_yuv, channels_yuv)

        /* 2-2：YUV空間 → グレースケール変換 → 二値化 */
        Imgproc.cvtColor(mat_yuv, mat_yuv, Imgproc.COLOR_RGB2GRAY)
        Core.subtract(channels_yuv[0], mat_yuv, channels_yuv[0])
        Core.subtract(channels_yuv[1], mat_yuv, channels_yuv[1])
        Core.subtract(channels_yuv[2], mat_yuv, channels_yuv[2])
        Imgproc.threshold(
            channels_yuv[0],
            channels_yuv[0],
            0.0,
            255.0,
            Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU
        )
        Imgproc.threshold(
            channels_yuv[1],
            channels_yuv[1],
            0.0,
            255.0,
            Imgproc.THRESH_BINARY_INV or Imgproc.THRESH_OTSU
        )
        Imgproc.threshold(
            channels_yuv[2],
            channels_yuv[2],
            0.0,
            255.0,
            Imgproc.THRESH_BINARY_INV or Imgproc.THRESH_OTSU
        )

        /* 2-3：YUVの輪郭を取得 */
        val contour_yuv0 = getContour(channels_yuv[0])
        val contour_yuv1 = getContour(channels_yuv[1])
        val contour_yuv2 = getContour(channels_yuv[2])


        /* 3：マスク画像に輪郭を合成 */
        val mat_mask = Mat(mat.size(), CvType.CV_8UC4, Scalar.all(255.0))
        val color = Scalar(0.0, 0.0, 0.0)
        Imgproc.drawContours(mat_mask, contour_rgb0, -1, color, -1)
        Imgproc.drawContours(mat_mask, contour_rgb1, -1, color, -1)
        Imgproc.drawContours(mat_mask, contour_rgb2, -1, color, -1)
        Imgproc.drawContours(mat_mask, contour_yuv0, -1, color, -1)
        Imgproc.drawContours(mat_mask, contour_yuv1, -1, color, -1)
        Imgproc.drawContours(mat_mask, contour_yuv2, -1, color, -1)

        Imgproc.cvtColor(mat_mask, mat_mask, Imgproc.COLOR_RGB2GRAY)
        Imgproc.threshold(mat_mask, mat_mask, 0.0, 255.0, Imgproc.THRESH_BINARY_INV or Imgproc.THRESH_OTSU)

        return mat_mask
    }

    /**
     * 四角形の輪郭を抽出する
     */
    private fun getContour(mat: Mat): List<MatOfPoint> {
        val contour = java.util.ArrayList<MatOfPoint>()

        /* 二値画像中の輪郭を検出 */
        val tmp_contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat.zeros(Size(5.0, 5.0), CvType.CV_8UC1)
        Imgproc.findContours(mat, tmp_contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1)
        for (i in tmp_contours.indices) {
            if (Imgproc.contourArea(tmp_contours[i]) < mat.size().area() / (100 * 1)) {
                /* サイズが小さいエリアは無視 */
                continue
            }

            val ptmat2 = MatOfPoint2f(*tmp_contours[i].toArray())
            val approx = MatOfPoint2f()
            val approxf1 = MatOfPoint()

            /* 輪郭線の周囲長を取得 */
            val arclen = Imgproc.arcLength(ptmat2, true)
            /* 直線近似 */
            Imgproc.approxPolyDP(ptmat2, approx, 0.01 * arclen, true)
            approx.convertTo(approxf1, CvType.CV_32S)
            if (approxf1.size().area() != 4.0) {
                /* 四角形以外は無視 */
                continue
            }

            /* 輪郭情報を登録 */
            contour.add(approxf1)
        }

        return contour
    }

    /**
     * Pointリストへの変換
     */
    private fun contour2point(contour: List<MatOfPoint>): List<List<Point>> {
        val points = java.util.ArrayList<List<Point>>()
        for (i in contour.indices) {
            points.add(contour[i].toList())
        }
        return points
    }


    companion object {
        private const val TAG = "OpencvActivity"
    }
}
