package kr.ac.kumoh.s20150088.imagepdf

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_ocr_result.*

class ocrResultActivity : AppCompatActivity() {
    //이미지에 대한 ocr 결과 액티비티
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr_result)
        var data:String
        data = intent.getStringExtra("ocr")
        ocr_eb.setText(data)
        btnFinish.setOnClickListener {
            var intent: Intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent)
        }
    }
}