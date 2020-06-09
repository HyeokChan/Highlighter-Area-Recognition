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
        //서버 통신
        private val SERVER_URL: String = "http://bustercallapi.r-e.kr/"
        lateinit var mQueue: RequestQueue
        var mResult: JSONObject? = null
        var ocr_result_ary = ArrayList<String>()
        var ocr_img_ary = ArrayList<Bitmap>()
    }
    private var imageData: ByteArray? = null
    lateinit var myView:MyView
    var filename:String? = null
    var view_height : Int = 0
    var file_index : Int = 0
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
            uploadBox(boxSt)
        }
        finishBtn.setOnClickListener {

            val resultIntent:Intent = Intent(this,pdfocrResultActivity::class.java)
//            resultIntent.putExtra("ocr",ocr_result_ary)
//            resultIntent.putExtra("img_url", ocr_img_ary)
            startActivity(resultIntent)
        }
    }

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
                ResponseParsing()
            },
            Response.ErrorListener {error->
                Log.i("fail!!",error.toString())
            })
        Volley.newRequestQueue(this).add(request)
    }

    private fun ResponseParsing() {
        val items = mResult?.getString("output")
        val ParsingRes = items.toString().replace("\"","").replace(",","").replace("[","").replace("]","")
        Log.i("업로드 박스",ParsingRes)
        ocr_result_ary.add(ParsingRes)
    }
    private fun launchSystemFilePicker() {
        val openIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        openIntent.addCategory(Intent.CATEGORY_OPENABLE)
        openIntent.type = "application/*"
        startActivityForResult(openIntent, OPEN_DOCUMENT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            //documentUri = Uri.parse(uri.toString())
            if(uri != null) {
                //prepareAndShowDocument(uri);
                document = PdfDocumentLoader.openDocument(this, uri)
                showPdf()
            }
        }

    }

    private fun showPdf() {
        // Page size is in PDF points (not pixels).
        val pageSize : Size = document.getPageSize(pageIndex)
        // We define a target width for the resulting bitmap and use it to calculate the final height.
        val width = 1024
        val height = (pageSize.height * (width / pageSize.width)).toInt()

        // This will render the first page uniformly into a bitmap with a width of 2,048 pixels.
        val pageBitmap : Bitmap = document.renderPageToBitmap(this, pageIndex, width, height)
        //이미지 전송을 위한 bitmap 바이트 배열 변환
        imageData = bitmapToByteArray(pageBitmap)
        rotatedBitmap = pageBitmap
        MyView.bmp = pageBitmap
        my_view_pdf.invalidate()
    }

    fun bitmapToByteArray(bitmap: Bitmap) : ByteArray{
        var stream: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        var byteArray:ByteArray = stream.toByteArray()
        return byteArray
    }

    private fun uploadImage(){
        imageData?: return
        val url = SERVER_URL+"getImage"
        val request = object : VolleyFileUploadRequest(
            Request.Method.POST,
            url,
            Response.Listener {
                mResult = JSONObject(String(it.data))
                Log.i("response",mResult.toString())
                drawList()
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
    private fun drawList() {
        val items = mResult?.getString("box")//json 파일의 list 배열을 가지고 와서 items에 넣어라
        filename = mResult?.getString("filename")
        //Log.i("filename 데이터", filename.toString())
        Log.i("JSON 데이터", items.toString() )
        val ocrText = items.toString()
        val resText = ocrText.replace("\"","").replace("\n","").replace(",","").replace("[","").replace("]","")
        //ResultText.setText(resText)
        val res_img = mResult?.getString("url")
        var image_task: URLtoBitmapTask = URLtoBitmapTask()
        image_task = URLtoBitmapTask().apply {
            url = URL(res_img)
        }

        val task_bmp = image_task.execute().get()
        MyView.bmp = task_bmp
        ocr_img_ary.add(task_bmp)
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

/*    override fun onWindowFocusChanged(hasFocus: Boolean) {
        view_height = my_view_pdf.height
        view_width = my_view_pdf.width
        var rotate_height = rotatedBitmap!!.height
        var ratio_h = view_height.toFloat() / rotate_height.toFloat()
        var widthTest = rotatedBitmap!!.width * ratio_h

        var rotate_width = rotatedBitmap!!.width
        var ratio_w = view_width.toFloat() / rotate_width.toFloat()
        var heightTest = rotatedBitmap!!.height*ratio_w
        var resized:Bitmap
        if(heightTest.toInt() > view_height){
            resized = Bitmap.createScaledBitmap(rotatedBitmap!!, widthTest.toInt(), view_height, false)
        }
        else{
            resized = Bitmap.createScaledBitmap(rotatedBitmap!!, view_width, heightTest.toInt(), false)
        }
        imageData = bitmapToByteArray(resized!!)

        MyView.rect.clear()
        MyView.bmp = resized
        my_view_pdf.invalidate()
    }*/
}