package kr.ac.kumoh.s20150088.imagepdf

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_show_image.*
import kotlinx.android.synthetic.main.activity_show_image.btnSend
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class showImageActivity : AppCompatActivity(){
    companion object{
        private val PICK_FROM_ALBUM:Int = 1
        private val PICK_FROM_CAMERA:Int = 2
        private var tempFile: File? = null
        private val SERVER_URL: String = "http://bustercallapi.r-e.kr/"
        var mResult: JSONObject? = null
        lateinit var mQueue: RequestQueue
    }
    private var imageData: ByteArray? = null
    lateinit var myView:MyView
    var filename:String? = null
    var view_height : Int = 0
    var view_width : Int = 0
    var rotatedBitmap: Bitmap? = null
    var userInputFilename = ""
    lateinit var etFilename: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_image)
        var data:String
        data = intent.getStringExtra("key")
        if(data == "gallery"){
            goToGallery()
        }
        else if(data == "camera"){
            takePhoto()
        }
        btnSend.setOnClickListener {
            uploadImage()
        }
        mQueue = Volley.newRequestQueue(applicationContext)
        myView = MyView(this)

        inputFilename()

        leftAdd.setOnClickListener {
            MyView.rect[MyView.containIndex].left = MyView.rect[MyView.containIndex].left - 5
            my_view_img.invalidate()
        }
        leftSub.setOnClickListener {
            MyView.rect[MyView.containIndex].left = MyView.rect[MyView.containIndex].left + 5
            my_view_img.invalidate()
        }
        rightSub.setOnClickListener {
            MyView.rect[MyView.containIndex].right = MyView.rect[MyView.containIndex].right - 5
            my_view_img.invalidate()
        }
        rightAdd.setOnClickListener {
            MyView.rect[MyView.containIndex].right = MyView.rect[MyView.containIndex].right + 5
            my_view_img.invalidate()
        }
        btnSendBox.setOnClickListener {
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
            uploadBox(boxSt)
        }
    }

    // 1-1) 갤러리로 인텐트
    private fun goToGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, PICK_FROM_ALBUM)
    }
    // 1-2) 카메라로 인텐트
    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            tempFile = createImageFile()
        } catch (e: IOException) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            finish()
            e.printStackTrace()
        }
        if (tempFile != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val photoUri = FileProvider.getUriForFile(
                    this,
                    "kr.ac.kumoh.s20150088.imagePdf.provider", tempFile!!
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            }
            else {
                val photoUri = Uri.fromFile(tempFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            }
        }
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

    // 3) 갤러리 또는 카메라를 통해 받아온 이미지를 담을 파일 생성 후 반환
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // 이미지 파일 이름
        val timeStamp = SimpleDateFormat("HHmmss").format(Date())
        val imageFileName = "buster_" + timeStamp + "_"
        // 이미지가 저장될 폴더 이름
        val storageDir = File(Environment.getExternalStorageDirectory().toString() + "/buster/")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        // 빈 파일 생성
        var rtImage:File = File.createTempFile(imageFileName,".jpg", storageDir)
        return rtImage
    }

    // 4) 액티비티가 생성되기 전에 이미지의 크기를 구하기 위한 onWindowFocusChagned 오버라이드 함수
    // 갤러리에서 ShowImageActivity로 전환 될 때 자동으로 실행
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        view_height = my_view_img.height
        view_width = my_view_img.width
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
        my_view_img.invalidate()
    }

    // 5) POST 통신으로 이미지를 서버로 전송하기 위한 비트맵 바이트어레이로 변환 메소드
    fun bitmapToByteArray(bitmap: Bitmap) : ByteArray{
        var stream:ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        var byteArray:ByteArray = stream.toByteArray()
        return byteArray
    }

    // 6) 카메라 또는 사진첩에서 다시 돌아왔을 때 tempFile에 객체 담기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            var photoUri: Uri? = data?.data
            var cursor: Cursor? = null
            try {
                val proj = arrayOf(MediaStore.Images.Media.DATA)
                cursor = contentResolver.query(photoUri!!, proj, null, null, null)
                val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor?.moveToFirst()
                tempFile = File(cursor?.getString(column_index!!))
            }finally {
                cursor?.close()
            }
            setImage()
        }
        else if(requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK){
            setImage()
        }
        else{
            var intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent)
        }

    }
    // 7) createImageFile에서 생성한 빈파일에 가져온 이미지(tempFIle)를 담고 사진 비율에 따른 화면 회전후 뷰에 적용
    private fun setImage() {
        val options = BitmapFactory.Options()
        val originalBm = BitmapFactory.decodeFile(tempFile?.getAbsolutePath(), options)
        var ei: ExifInterface? = null
        try {
            ei = ExifInterface(tempFile?.getAbsolutePath())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val orientation = ei!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(originalBm, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(originalBm, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(originalBm, 270f)
            ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = originalBm
            else -> rotatedBitmap = originalBm
        }
        var rotate_width = rotatedBitmap!!.width.toFloat()
        var rotate_height = rotatedBitmap!!.height.toFloat()
        var viewheight = 1400
        if(rotate_height > viewheight){
            var percente = (rotate_height/100).toFloat()
            var scale  = viewheight/percente
            rotate_width *= (scale/100)
            rotate_height *= (scale/100)
        }
        my_view_img.invalidate()
    }



    // 8) 사진첩 또는 카메라로 부터 얻어온 이미지를 서버통신으로 전송하는 메소드
    // Response : 박스 좌표
    private fun uploadImage(){
        imageData?: return
        val url = SERVER_URL+"getImage"
        val request = object : VolleyFileUploadRequest(
            Request.Method.POST,
            url,
            Response.Listener {response->
                mResult = JSONObject(String(response.data))
                Log.i("response!!!!!",mResult.toString())
                drawBox()
            },
            Response.ErrorListener {response->
                println("오류 : $response")

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


    // 9) 박스 좌표를 파싱, 응답으로 온 URL을 이미지로 변경하여 View에 적용하고 박스 그리기
    private fun drawBox() {
        val items = mResult?.getString("box")
        filename = mResult?.getString("filename")
        Log.i("filename 데이터", filename.toString())
        Log.i("JSON 데이터", items.toString() )
        val ocrText = items.toString()
        val resText = ocrText.replace("\"","").replace("\n","").replace(",","").replace("[","").replace("]","")
        //ResultText.setText(resText)
        val res_img = mResult?.getString("url")
        var image_task: URLtoBitmapTask = URLtoBitmapTask()
        image_task = URLtoBitmapTask().apply {
            url = URL(res_img)
        }
        MyView.bmp = image_task.execute().get()
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
            btnSendBox.isEnabled = true
            my_view_img.invalidate()
        }
        else{
            Toast.makeText(this,"형광펜 영역이 없습니다.",Toast.LENGTH_LONG).show()
        }
    }
    // 10) 사용자가 박스 수정 후 수정된 박스 좌표에 대한 OCR 서버 통신 요청 메소드
    // Response : 수정된 박스 영역에 대한 OCR 결과
    fun uploadBox(boxSt:String){
        val url = SERVER_URL+"ocr"
        val params = HashMap<String, String?>()
        Log.i("filename", filename)
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

    // 11)수정된 박스영역에 대한 ocr 결과 파싱 후 ocr 결과 액티비티로 Intent
    private fun ResponseOCRParsing() {
        val items = mResult?.getString("output")
        val ParsingRes = items.toString().replace("\"","").replace(",","").replace("[","").replace("]","")
        Log.i("ocr 파싱 결과", ParsingRes)
        val resultIntent:Intent = Intent(this,ocrResultActivity::class.java)
        resultIntent.putExtra("ocr",ParsingRes)
        startActivity(resultIntent)
    }

    //화면 회전을 위한 메소드
    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

}