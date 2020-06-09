package kr.ac.kumoh.s20150088.imagepdf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_document.*


class DocumentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)
        var imageStTemp = MainActivity.imageStArray[intent.getIntExtra("position",-1)].replace("\n","")
        var imageSt = String(Base64.decode(imageStTemp,0))
        ivDoc.setImageBitmap(StringToBitmap(imageSt))
        var docSt = MainActivity.docConArray[intent.getIntExtra("position",-1)].replace("[","").replace("]","")
        tvDoc.text = docSt
    }
    fun StringToBitmap(encodedString: String?): Bitmap? {
        return try {
            val encodeByte =
                Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            null
        }
    }
}
