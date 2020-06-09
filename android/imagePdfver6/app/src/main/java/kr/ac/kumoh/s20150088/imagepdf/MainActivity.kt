package kr.ac.kumoh.s20150088.imagepdf

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayout
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_show_image.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    companion object{
        const val SERVER_URL="http://bustercallapi.r-e.kr/"
        var docConArray = ArrayList<String>()
        var imageStArray = ArrayList<String>()
    }
    lateinit var mQueue: RequestQueue
    var mResult: JSONObject? = null
    var mArray = ArrayList<String>()
    var mAdapter = documentListAdapter()
    //로딩중
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //tedPermission()


        btnGallery.setOnClickListener {
            val intent:Intent = Intent(this,showImageActivity::class.java)
            intent.putExtra("key","gallery")
            startActivity(intent)
        }
        btnCamera.setOnClickListener {
            val intent:Intent = Intent(this,showImageActivity::class.java)
            intent.putExtra("key","camera")
            startActivity(intent)
        }
        btnPdf.setOnClickListener {
            val intent:Intent = Intent(this,showPdfActivity::class.java)
            startActivity(intent)
        }
        btnFriend.setOnClickListener {
            val intent = Intent(this,FriendActivity::class.java)
            startActivity(intent)
        }
        /*btnLogin.setOnClickListener {
            val intent:Intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }*/

        rvDocumentList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
            itemAnimator = DefaultItemAnimator()
        }
        loading()
        requestDocument()

    }

    fun requestDocument(){
        val url = SERVER_URL+"content_list"
        val params = HashMap<String, String>()
        params.put("uuid",LoginActivity.idByANDROID_ID)

        var jsonObj = JSONObject(params as Map<*, *>)
        var request: JsonObjectRequest = JsonObjectRequest(Request.Method.POST,url,jsonObj,
            Response.Listener { response->
                Log.i("success!!do",response.toString())
                if(response.getString("result").equals("success")){
                    mResult = response
                    drawList()
                }
            },
            Response.ErrorListener {error->
                Log.i("fail!!",error.toString())
                loadingEnd()

            })
        Volley.newRequestQueue(this).add(request)

    }

    fun drawList(){
        mArray.clear()
        docConArray.clear()
        imageStArray.clear()
        /*if(!mResult?.getString("friendList").equals("not exist")){
            val items = mResult?.getJSONArray("friendList")
            for(i in 0 until items!!.length()){
                mArray.add(items.getJSONObject(i).getString("friend_email"))
            }
        }
        else{
            mArray.add(" - ")
        }*/

        val items = mResult?.getJSONArray("my_content")
        for(i in 0 until items!!.length()){
            mArray.add(items.getJSONObject(i).getString("doc_name"))
            docConArray.add(items.getJSONObject(i).getString("doc_content"))
            imageStArray.add(items.getJSONObject(i).getString("image"))
        }
        loadingEnd()
        mAdapter.notifyDataSetChanged()
    }

    inner class documentListAdapter() : RecyclerView.Adapter<documentListAdapter.ViewHolder>(){
        inner class ViewHolder : RecyclerView.ViewHolder, View.OnClickListener{
            val tvTitle : TextView
            val thumbnail : ImageView
            constructor(root: View) : super(root){
                root.setOnClickListener(this)
                tvTitle = root.findViewById(R.id.tvTitle)
                thumbnail = root.findViewById(R.id.thumbnail)
            }
            override fun onClick(p0: View?) {
                val intent:Intent = Intent(applicationContext,DocumentActivity::class.java)
                intent.putExtra("position",adapterPosition)
                startActivity(intent)
            }
        }
        override fun getItemCount(): Int {
            return mArray.size
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): documentListAdapter.ViewHolder {
            val root = LayoutInflater.from(applicationContext).inflate(R.layout.custom_recyclerview_do,parent,false)
            return ViewHolder(root)
        }
        override fun onBindViewHolder(holder: documentListAdapter.ViewHolder, position: Int) {
            holder.tvTitle.text = mArray[position]
            var imageStTemp = imageStArray[position].replace("\n","")
            var imageSt = String(Base64.decode(imageStTemp,0))
            holder.thumbnail.setImageBitmap(StringToBitmap(imageSt))
        }
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
    fun loading() {
        //로딩
        android.os.Handler().postDelayed(
            {
                progressDialog = ProgressDialog(this)
                progressDialog.isIndeterminate = true
                progressDialog.setMessage("잠시만 기다려 주세요")
                progressDialog.show()
            }, 0
        )
    }
    fun loadingEnd() {
        android.os.Handler().postDelayed(
            { progressDialog.dismiss() }, 0
        )
    }

    private fun tedPermission() {
        var permissionListener : PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                // 권한 허가시 실행 할 내용
            }
            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                // 권한 거부시 실행  할 내용
            }
        }
        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setRationaleMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
            .setDeniedMessage("사진 및 파일을 저장하기 위하여 접근 권한이 필요합니다.")
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .check()
    }
}

