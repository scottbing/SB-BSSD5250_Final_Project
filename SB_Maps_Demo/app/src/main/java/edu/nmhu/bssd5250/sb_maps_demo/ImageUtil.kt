package edu.nmhu.bssd5250.sb_maps_demo

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import java.io.ByteArrayOutputStream

object ImageUtil {
    //
    //
    //
    // Taken from https://stackoverflow.com/questions/33139754/android-6-0-marshmallow-cannot-write-to-sd-card
    // used to convert photo image format YUV_420_888 to JPEG
    //
    //
    //
    //
    fun imageToByteArray(image: Image): ByteArray? {
        var data: ByteArray? = null
        if (image.format == ImageFormat.JPEG) {
            val planes = image.planes
            val buffer = planes[0].buffer
            data = ByteArray(buffer.capacity())
            buffer[data]
            return data
        } else if (image.format == ImageFormat.YUV_420_888) {
            data = NV21toJPEG(
                YUV_420_888toNV21(image),
                image.width, image.height
            )
        }
        return data
    }

    private fun YUV_420_888toNV21(image: Image): ByteArray {
        val nv21: ByteArray
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        nv21 = ByteArray(ySize + uSize + vSize)

        //U and V are swapped
        yBuffer[nv21, 0, ySize]
        vBuffer[nv21, ySize, vSize]
        uBuffer[nv21, ySize + vSize, uSize]
        return nv21
    }

    private fun NV21toJPEG(nv21: ByteArray, width: Int, height: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        yuv.compressToJpeg(Rect(0, 0, width, height), 100, out)
        return out.toByteArray()
    }
}