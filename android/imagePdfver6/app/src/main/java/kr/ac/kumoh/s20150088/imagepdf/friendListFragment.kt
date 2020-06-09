package kr.ac.kumoh.s20150088.imagepdf

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_friendlist.view.*
import org.json.JSONObject
import java.net.CookieHandler
import java.net.CookieManager

class friendListFragment:Fragment() {

    companion object{
        const val SERVER_URL="http://bustercallapi.r-e.kr/"
    }
    lateinit var mQueue: RequestQueue
    var mResult: JSONObject? = null
    var mArray = ArrayList<String>()
    var mAdapter = friendListAdapter()
    lateinit var etFriendEmail:EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_friendlist,container,false)

        CookieHandler.setDefault(CookieManager())
        mQueue = Volley.newRequestQueue(context)

        view.rvFreindList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
            itemAnimator = DefaultItemAnimator()
        }

        requestFreindList()

        view.btnFab.setOnClickListener{
            var dialog = AlertDialog.Builder(context)
            etFriendEmail = EditText(context)
            dialog.setTitle("추가할 친구 이메일")
            dialog.setView(etFriendEmail)
            fun btnPositive() {
                positive()
            }
            fun btnNegative(){
            }
            var dialog_listener = object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    when(which){
                        DialogInterface.BUTTON_POSITIVE ->
                            btnPositive()
                        DialogInterface.BUTTON_NEGATIVE ->
                            btnNegative()
                    }
                }
            }
            dialog.setPositiveButton("YES",dialog_listener)
            dialog.setNegativeButton("NO",dialog_listener)
            dialog.show()
        }

        return  view
    }

    fun positive(){
        val url = SERVER_URL+"friend_request"
        val params = HashMap<String, String>()
        params.put("uuid",LoginActivity.idByANDROID_ID)
        params.put("me_email",LoginActivity.me_email)
        params.put("another_email",etFriendEmail.text.toString())
        params.put("type","send")
        var jsonObj = JSONObject(params as Map<*, *>)
        var request: JsonObjectRequest = JsonObjectRequest(Request.Method.POST,url,jsonObj,
            Response.Listener { response->
                if(response.getString("result").equals("success")){
                    mResult = response
                }
            },
            Response.ErrorListener {error->
                Log.i("fail!!",error.toString())
            })
        Volley.newRequestQueue(context).add(request)
    }

    fun requestFreindList(){
        val url = SERVER_URL+"friend_list"
        val params = HashMap<String, String>()
        params.put("uuid",LoginActivity.idByANDROID_ID)
        params.put("list_type","total")

        var jsonObj = JSONObject(params as Map<*, *>)
        var request: JsonObjectRequest = JsonObjectRequest(Request.Method.POST,url,jsonObj,
            Response.Listener { response->
                if(response.getString("result").equals("success")){
                    Log.i("success!!",response.toString())
                    mResult = response
                    drawList()
                }
            },
            Response.ErrorListener {error->
                Log.i("fail!!",error.toString())
            })
        Volley.newRequestQueue(context).add(request)
    }

    fun drawList(){
        mArray.clear()
        if(!mResult?.getString("friendList").equals("not exist")){
            val items = mResult?.getJSONArray("friendList")
            for(i in 0 until items!!.length()){
                mArray.add(items.getJSONObject(i).getString("friend_email"))
            }
        }
        else{
            mArray.add(" - ")
        }
        mAdapter.notifyDataSetChanged()
    }

    inner class friendListAdapter() : RecyclerView.Adapter<friendListAdapter.ViewHolder>(){
        inner class ViewHolder : RecyclerView.ViewHolder, View.OnClickListener{
            val tvEmail : TextView
            constructor(root: View) : super(root){
                root.setOnClickListener(this)
                tvEmail = root.findViewById(R.id.tvEmail)
            }
            override fun onClick(p0: View?) {
            }
        }
        override fun getItemCount(): Int {
            return mArray.size
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): friendListAdapter.ViewHolder {
            val root = LayoutInflater.from(context).inflate(R.layout.custom_recyclerview_fr,parent,false)
            return ViewHolder(root)
        }
        override fun onBindViewHolder(holder: friendListAdapter.ViewHolder, position: Int) {
            holder.tvEmail.text = mArray[position]
        }
    }

}