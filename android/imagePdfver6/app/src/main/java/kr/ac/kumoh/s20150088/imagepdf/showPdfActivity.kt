package kr.ac.kumoh.s20150088.imagepdf

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.document.PdfDocumentLoader
import com.pspdfkit.utils.Size
import kotlinx.android.synthetic.main.activity_show_pdf.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.util.HashMap

class showPdfActivity : AppCompatActivity() {
    companion object{
        private val OPEN_DOCUMENT_REQUEST_CODE = 0x33
        lateinit var documentUri : Uri
        lateinit var document : PdfDocument
        var pageIndex = 0

        private val SERVER_URL: String = "http://bustercallapi.r-e.kr/"
        lateinit var mQueue: RequestQueue
        var mResult: JSONObject? = null
        var ocr_result_ary = ArrayList<String>()
        var ocr_img_ary = ArrayList<Bitmap>()
    }
    private var imageData: ByteArray? = null
    lateinit var myView:MyView
    var filename:String? = null
    var rotatedBitmap: Bitmap? = null
    lateinit var etFilename: EditText
    var userInputFilename = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_pdf)
        MyView.rect.clear()
        ocr_result_ary.clear()
        ocr_img_ary.clear()
        pageIndex = 0
        launchSystemFilePicker()
        myView = MyView(this)
        inputFilename()
        previousButton.setOnClickListener {
            if(pageIndex > 0) {
                pageIndex -= 1
                showPdf()
                leftAdd.isEnabled = false
                leftSub.isEnabled = false
                rightAdd.isEnabled = false
                rightSub.isEnabled = false
                pdfuploadbtn.isEnabled = false
            }
            else
            {
                pageIndex = 0
                showPdf()
            }
        }
        nextButton.setOnClickListener {
            if (pageIndex < document.pageCount-1) {
                pageIndex += 1
                MyView.rect.clear()
                showPdf()
                leftAdd.isEnabled = false
                leftSub.isEnabled = false
                rightAdd.isEnabled = false
                rightSub.isEnabled = false
                pdfuploadbtn.isEnabled = false
            }
            else {
                pageIndex = document.pageCount-1
                MyView.rect.clear()
                showPdf()
            }
        }
        btnSend.setOnClickListener {
            uploadImage()
        }
        leftAdd.setOnClickListener {
            MyView.rect[MyView.containIndex].left = MyView.rect[MyView.containIndex].left - 5
            my_view_pdf.invalidate()
        }
        leftSub.setOnClickListener {
            MyView.rect[MyView.containIndex].left = MyView.rect[MyView.containIndex].left + 5
            my_view_pdf.invalidate()
        }
        rightSub.setOnClickListener {
            MyView.rect[MyView.containIndex].right = MyView.rect[MyView.containIndex].right - 5
            my_view_pdf.invalidate()
        }
        rightAdd.setOnClickListener {
            MyView.rect[MyView.containIndex].right = MyView.rect[MyView.containIndex].right + 5
            my_view_pdf.invalidate()
        }
        pdfuploadbtn.setOnClickListener {
            var boxSt:String = "["
            for(i in 0 until MyView.rect.size){
                if(i != MyView.rect.size-1){
                    boxSt = boxSt + "[[" +MyView.rect[i].left + ", " + MyView.rect[i].top + "]" +
                            ", [" + MyView.rect[i].right + ", " + MyView.rect[i].bottom + "]], "
                }
                else if(i == MyView.rect.size-1){
                    boxSt = boxSt + "[[" +MyView.rect[i].left + ", " + MyView.rect[i].top + "]" +
                            ", [" + MyView.rect[i].right + ", " + MyView.rect[i].bottom + "]]]"
                }
            }
            Toast.makeText(this, "ocr 결과 저장완료!!", Toast.LENGTH_SHORT).show()
            pdfuploadbtn.isEnabled = false;
            uploadBox(boxSt)
        }
        finishBtn.setOnClickListener {

            val resultIntent:Intent = Intent(this,pdfocrResultActivity::class.java)
            startActivity(resultIntent)
        }
    }

    // 1) 저장소 접근
    private fun launchSystemFilePicker() {
        val openIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        openIntent.addCategory(Intent.CATEGORY_OPENABLE)
        openIntent.type = "application/*"
        startActivityForResult(openIntent, OPEN_DOCUMENT_REQUEST_CODE)
    }
    // 2) 파일 이름 다이얼로그
    fun inputFilename(){
        var dialog = AlertDialog.Builder(this)
        etFilename = EditText(this)
        dialog.setTitle("작업 파일 이름")
        dialog.setView(etFilename)
        fun btnPositive() {
            userInputFilename = etFilename.text.toString()
        }
        var dialog_listener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when(which){
                    DialogInterface.BUTTON_POSITIVE ->
                        btnPositive()
                }
            }
        }
        dialog.setPositiveButton("YES",dialog_listener)
        dialog.show()
    }

    // 3) 저장소에서 다시 돌아왔을 때 절대경로로부터 PdfDocument 객체에 담기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            if(uri != null) {
                document = PdfDocumentLoader.openDocument(this, uri)
                showPdf()
            }
        }
    }

    // 4) 저장소로 부터 가져온 PDF 파일 비트맵 렌더링 후 뷰에 적용
    private fun showPdf() {

        val pageSize : Size = document.getPageSize(pageIndex)
        val width = 1024
        val height = (pageSize.height * (width / pageSize.width)).toInt()

        val pageBitmap : Bitmap = document.renderPageToBitmap(this, pageIndex, width, height)
        //이미지 전송을 위한 bitmap 바이트 배열 변환
        imageData = bitmapToByteArray(pageBitmap)
        rotatedBitmap = pageBitmap
        MyView.bmp = pageBitmap
        my_view_pdf.invalidate()
    }
    // 5) POST 통신으로 이미지를 서버로 전송하기 위한 비트맵 바이트어레이로 변환 메소드
    fun bitmapToByteArray(bitmap: Bitmap) : ByteArray{
        var stream: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        var byteArray:ByteArray = stream.toByteArray()
        return byteArray
    }


    // 6)저장소로부터 얻어온 PDF 이미지를 서버통신으로 전송하는 메소드
    // Response : 박스 좌표
    private fun uploadImage(){
        imageData?: return
        val url = SERVER_URL+"getImage"
        val request = object : VolleyFileUploadRequest(
            Request.Method.POST,
            url,
            Response.Listener {
                mResult = JSONObject(String(it.data))
                Log.i("response",mResult.toString())
                drawbox()
            },
            Response.ErrorListener {
                println("오류 : $it")
            }

        ) {
            override fun getByteData(): MutableMap<String, FileDataPart> {
                var params = HashMap<String, FileDataPart>()
                params["image"] = FileDataPart(userInputFilename+"_resized.jpg", imageData!!, "jpg")
                return params
            }
        }
        mQueue = Volley.newRequestQueue(this)
        mQueue.add(request)
    }

    // 7) 박스 좌표를 파싱, 응답으로 온 URL을 이미지로 변경하여 View에 적용하고 박스 그리기
    private fun drawbox() {
        val items = mResult?.getString("box")//json 파일의 list 배열을 가지고 와서 items에 넣어라
        filename = mResult?.getString("filename")

        Log.i("JSON 데이터", items.toString() )
        val ocrText = items.toString()
        val resText = ocrText.replace("\"","").replace("\n","").replace(",","").replace("[","").replace("]","")
        val res_img = mResult?.getString("url")
        var image_task: URLtoBitmapTask = URLtoBitmapTask()
        image_task = URLtoBitmapTask().apply {
            url = URL(res_img)
        }
        val task_bmp = image_task.execute().get()
        MyView.bmp = task_bmp
        ocr_img_ary.add(task_bmp) // 비트맵 추가
        MyView.rect.clear()
        if(!resText.equals("")){
            var s = resText.split(" ")
            var i =0
            while (i < s.size){ //getImage의 응답으로 온 박스 좌표를 rect_array에 rect class형식으로 저장
                var left = s[i].toInt()
                var top = s[i+1].toInt()
                var right = s[i+2].toInt()
                var bottom = s[i+3].toInt()
                MyView.rect.add(Rect(left, top, right, bottom))
                i = i+4
            }

            //업로드 이후 박스크기 조정 버튼 활성화
            leftAdd.isEnabled = true
            leftSub.isEnabled = true
            rightAdd.isEnabled = true
            rightSub.isEnabled = true
            finishBtn.isEnabled = true
            pdfuploadbtn.isEnabled = true
            //btnSendBox.isEnabled = true
            my_view_pdf.invalidate()
        }
        else{
            Toast.makeText(this,"형광펜 영역이 없습니다.",Toast.LENGTH_LONG).show()
        }
    }
    // 8) 사용자가 박스 수정 후 수정된 박스 좌표에 대한 OCR 서버 통신 요청 메소드
    // Response : 수정된 박스 영역에 대한 OCR 결과
    fun uploadBox(boxSt:String){
        mResult = null
        val url = SERVER_URL +"ocr"
        val params = HashMap<String, String?>()
        params.put("filename",filename)
        params.put("box",boxSt)
        params.put("uuid",LoginActivity.idByANDROID_ID)
        var jsonObj = JSONObject(params as Map<*, *>)
        var request: JsonObjectRequest = JsonObjectRequest(Request.Method.POST,url,jsonObj,
            Response.Listener {response->
                mResult = response
                ResponseOCRParsing()
            },
            Response.ErrorListener {error->
                Log.i("fail!!",error.toString())
            })
        Volley.newRequestQueue(this).add(request)
    }

    // 9) 수정된 박스영역에 대한 ocr 결과 파싱
    private fun ResponseOCRParsing() {
        val items = mResult?.getString("output")
        val ParsingRes = items.toString().replace("\"","").replace(",","").replace("[","").replace("]","")
        ocr_result_ary.add(ParsingRes)
    }

}
