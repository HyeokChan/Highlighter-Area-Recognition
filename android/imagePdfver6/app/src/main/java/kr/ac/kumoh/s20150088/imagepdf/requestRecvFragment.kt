package kr.ac.kumoh.s20150088.imagepdf

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import kotlinx.android.synthetic.main.fragment_requestrecv.view.*
import org.json.JSONObject
import java.net.CookieHandler
import java.net.CookieManager

class requestRecvFragment:Fragment() {

    companion object{
        const val SERVER_URL="http://bustercallapi.r-e.kr/"
    }
    var mArrayReceive = ArrayList<String>()
    var mArrayRequest = ArrayList<String>()
    private lateinit var mQueue: RequestQueue
    var mResult: JSONObject? = null
    var mAdapterReceive = receiveAdapter()
    var mAdapterRequest = requestAdapter()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_requestrecv,container,false)
        CookieHandler.setDefault(CookieManager())
        mQueue = Volley.newRequestQueue(context)

        view.rvRequestList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapterRequest
            itemAnimator = DefaultItemAnimator()
        }

        view.rvRecvList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapterReceive
            itemAnimator = DefaultItemAnimator()
        }

        requestRequest()
        requestRecv()

        return view
    }

    fun requestRequest(){
        val url = SERVER_URL+"friend_list"
        val params = HashMap<String, String>()
        params.put("uuid",LoginActivity.idByANDROID_ID)
        params.put("list_type","send")

        var jsonObj = JSONObject(params as Map<*, *>)
        var request: JsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,url,jsonObj,
            Response.Listener { response->
                if(response.getString("result").equals("success")){
                    Log.i("success!!rque",response.toString())
                    mResult = response
                    drawListRequest()
                }
            },
            Response.ErrorListener {error->
                Log.i("fail!!",error.toString())
            })
        Volley.newRequestQueue(context).add(request)
    }

    fun drawListRequest(){
        mArrayRequest.clear()
        if(!mResult?.getString("friendList").equals("not exist")){
            val items = mResult?.getJSONArray("friendList")
            for(i in 0 until items!!.length()){
                mArrayRequest.add(items.getJSONObject(i).getString("friend_email"))
            }
        }
        else{
            mArrayRequest.add(" - ")
        }
        mAdapterRequest.notifyDataSetChanged()
    }

    inner class requestAdapter() : RecyclerView.Adapter<requestAdapter.ViewHolder>(){
        inner class ViewHolder : RecyclerView.ViewHolder, View.OnClickListener{
            val tvEmail : TextView
            val btnOK : Button
            constructor(root: View) : super(root){
                root.setOnClickListener(this)
                tvEmail = root.findViewById(R.id.tvEmail)
                btnOK = root.findViewById(R.id.btnOK)
            }
            override fun onClick(p0: View?) {
            }
        }
        override fun getItemCount(): Int {
            return mArrayRequest.size
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): requestAdapter.ViewHolder {
            val root = LayoutInflater.from(context)
                .inflate(R.layout.custom_recyclerview_rr,parent,false)
            return ViewHolder(root)
        }
        override fun onBindViewHolder(holder: requestAdapter.ViewHolder, position: Int) {
            holder.tvEmail.text = mArrayRequest[position]
            holder.btnOK.text = "취소"
            holder.btnOK.setOnClickListener {
                val url = SERVER_URL+"friend_request"
                val params = HashMap<String, String>()
                params.put("uuid",LoginActivity.idByANDROID_ID)
                params.put("me_email",LoginActivity.me_email)
                params.put("another_email",holder.tvEmail.text.toString())
                params.put("type","send_cancel")
                var jsonObj = JSONObject(params as Map<*, *>)
                var request: JsonObjectRequest = JsonObjectRequest(
                    Request.Method.POST,url,jsonObj,
                    Response.Listener { response->
                        if(response.getString("result").equals("success")){
                            Log.i("success!!can",response.toString())
                            mResult = response
                        }
                    },
                    Response.ErrorListener {error->
                        Log.i("fail!!",error.toString())
                    })
                Volley.newRequestQueue(context).add(request)
                requestRequest()
            }
        }
    }

    fun requestRecv(){
        val url = SERVER_URL+"friend_list"
        val params = HashMap<String, String>()
        params.put("uuid",LoginActivity.idByANDROID_ID)
        params.put("list_type","recv")

        var jsonObj = JSONObject(params as Map<*, *>)
        var request: JsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,url,jsonObj,
            Response.Listener { response->
                if(response.getString("result").equals("success")){
                    Log.i("success!!",response.toString())
                    mResult = response
                    drawListReceive()
                }
            },
            Response.ErrorListener {error->
                Log.i("fail!!",error.toString())
            })
        Volley.newRequestQueue(context).add(request)
    }

    fun drawListReceive(){
        mArrayReceive.clear()
        if(!mResult?.getString("friendList").equals("not exist")){
            val items = mResult?.getJSONArray("friendList")
            for(i in 0 until items!!.length()){
                mArrayReceive.add(items.getJSONObject(i).getString("friend_email"))
            }
        }
        else{
            mArrayReceive.add(" - ")
        }
        mAdapterReceive.notifyDataSetChanged()
    }

    inner class receiveAdapter() : RecyclerView.Adapter<receiveAdapter.ViewHolder>(){
        inner class ViewHolder : RecyclerView.ViewHolder, View.OnClickListener{
            val tvEmail : TextView
            val btnOK : Button
            constructor(root: View) : super(root){
                root.setOnClickListener(this)
                tvEmail = root.findViewById(R.id.tvEmail)
                btnOK = root.findViewById(R.id.btnOK)
            }
            override fun onClick(p0: View?) {
            }
        }
        override fun getItemCount(): Int {
            return mArrayReceive.size
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): receiveAdapter.ViewHolder {
            val root = LayoutInflater.from(context)
                .inflate(R.layout.custom_recyclerview_rr,parent,false)
            return ViewHolder(root)
        }
        override fun onBindViewHolder(holder: receiveAdapter.ViewHolder, position: Int) {
            holder.tvEmail.text = mArrayReceive[position]
            holder.btnOK.setOnClickListener {
                val url = SERVER_URL+"friend_request"
                val params = HashMap<String, String>()
                params.put("uuid",LoginActivity.idByANDROID_ID)
                params.put("me_email",LoginActivity.me_email)
                params.put("another_email",holder.tvEmail.text.toString())
                params.put("type","recv")
                var jsonObj = JSONObject(params as Map<*, *>)
                var request: JsonObjectRequest = JsonObjectRequest(
                    Request.Method.POST,url,jsonObj,
                    Response.Listener { response->
                        if(response.getString("result").equals("success")){
                            Log.i("success!!",response.toString())
                            mResult = response
                        }
                    },
                    Response.ErrorListener {error->
                        Log.i("fail!!",error.toString())
                    })
                Volley.newRequestQueue(context).add(request)
                requestRecv()
            }
        }
    }
}