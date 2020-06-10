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
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.net.CookieHandler
import java.net.CookieManager
import java.util.HashMap

class RegisterActivity : AppCompatActivity() {

    companion object{
        const val QUEUE_TAG="VolleyRequest"
        const val SERVER_URL="http://bustercallapi.r-e.kr/"
    }
    lateinit var mQueue: RequestQueue
    var mResult: JSONObject? = null
    //회원가입 액티비티
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        CookieHandler.setDefault(CookieManager())
        mQueue = Volley.newRequestQueue(this)

        btnRegisterLogin.setOnClickListener {
            var idByANDROID_ID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            Log.i("uuid",idByANDROID_ID)
            if(!etEmailLogin.text.toString().equals("") && !etPasswordLogin.text.toString().equals("") && !etPasswordCf.text.toString().equals("")
                && etPasswordLogin.text.toString().equals(etPasswordCf.text.toString()))
            {
                Signup(etEmailLogin.text.toString(), etPasswordLogin.text.toString(), idByANDROID_ID)
            }
            else{
                Toast.makeText(this,"회원가입 양식을 다시 확인해주세요.",Toast.LENGTH_LONG).show()
            }
        }
    }

    fun Signup(email:String, password:String, android_id:String){
        val url = SERVER_URL+"register"
        val params = HashMap<String, String>()
        params.put("email",email)
        params.put("password",password)
        params.put("uuid",android_id)
        var jsonObj = JSONObject(params as Map<*, *>)
        var request: JsonObjectRequest = JsonObjectRequest(Request.Method.POST,url,jsonObj,
            Response.Listener {response->
                if(response.getString("result").equals("success")){
                    Log.i("success!!",response.toString())
                    Toast.makeText(this,"회원가입이 완료되었습니다.",Toast.LENGTH_LONG).show()
                    mResult = response
                    var intent = Intent(this,LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            },
            Response.ErrorListener {error->
                Log.i("fail!!",error.toString())
                Toast.makeText(this,"회원가입 양식을 다시 확인해주세요.",Toast.LENGTH_LONG).show()
            })
        Volley.newRequestQueue(this).add(request)
    }
}
