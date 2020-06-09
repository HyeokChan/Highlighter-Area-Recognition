package kr.ac.kumoh.s20150088.imagepdf

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.btnLogin
import org.json.JSONObject
import java.net.CookieHandler
import java.net.CookieManager

class LoginActivity : AppCompatActivity() {

    companion object{
        const val QUEUE_TAG="VolleyRequest"
        const val SERVER_URL="http://bustercallapi.r-e.kr/"
        var me_email:String = ""
        var idByANDROID_ID = ""
    }
    lateinit var mQueue: RequestQueue
    var mResult: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        CookieHandler.setDefault(CookieManager())
        mQueue = Volley.newRequestQueue(this)

        btnLogin.setOnClickListener {
            if(!etEmailLogin.text.toString().equals("") && !etPasswordLogin.text.equals(""))
            {
                Login(etEmailLogin.text.toString(), etPasswordLogin.text.toString())
            }
            else{
                Toast.makeText(this,"로그인 양식을 다시 확인해주세요.",Toast.LENGTH_LONG).show()
            }
        }

        btnRegisterLogin.setOnClickListener {
            var rgIntent = Intent(this,RegisterActivity::class.java)
            startActivity(rgIntent)
        }
    }

    fun Login(email:String, password:String){
        val url = SERVER_URL+"login"
        val params = HashMap<String, String>()
        params.put("email",email)
        params.put("password",password)
        var jsonObj = JSONObject(params as Map<*, *>)
        var request: JsonObjectRequest = JsonObjectRequest(Request.Method.POST,url,jsonObj,
            Response.Listener { response->
                Log.i("success!!",response.toString())
                mResult = response
                if(response.getString("result").equals("success")){
                    // 로그인한 email
                    me_email = email
                    // 로그인한 uuid
                    idByANDROID_ID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID)
                    val intent = Intent(this,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this,"email, password를 다시 확인해주세요.",Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {error->
                Log.i("fail!!",error.toString())
            })
        request.tag = QUEUE_TAG
        mQueue?.add(request)
    }
}
