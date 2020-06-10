package kr.ac.kumoh.s20150088.imagepdf

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_pdfocr_result.*
import kr.ac.kumoh.s20150088.imagepdf.showPdfActivity.Companion.ocr_img_ary
import kr.ac.kumoh.s20150088.imagepdf.showPdfActivity.Companion.ocr_result_ary
import java.io.Serializable
import java.net.URL

class pdfocrResultActivity : AppCompatActivity() {

    //PDF에 대한 ocr 결과 액티비티
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfocr_result)
        var mAdapter = documentListAdapter(ocr_result_ary)
        pdf_res_rc.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
            itemAnimator = DefaultItemAnimator()
        }
        btnpdfFinish.setOnClickListener {
            var intent: Intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent)
        }

    }
    //PDF OCR 결과 리싸이클러뷰를 위한 어댑터
    class documentListAdapter(val data:ArrayList<String>) : RecyclerView.Adapter<documentListAdapter.ViewHolder>(){
        inner class ViewHolder : RecyclerView.ViewHolder, View.OnClickListener{
            val tvTitle : TextView
            val thumbnail : ImageView
            constructor(root: View) : super(root){
                tvTitle = root.findViewById(R.id.pdftvTitle)
                thumbnail = root.findViewById(R.id.pdfthumbnail)
            }
            override fun onClick(p0: View?) {
            }
        }
        override fun getItemCount(): Int {
            return data.size
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): documentListAdapter.ViewHolder {
            val root = LayoutInflater.from(parent.context).inflate(R.layout.custom_recyclerview_pdf,parent,false)
            return ViewHolder(root)
        }
        override fun onBindViewHolder(holder: documentListAdapter.ViewHolder, position: Int) {
            holder.tvTitle.text = data[position]
            holder.thumbnail.setImageBitmap(ocr_img_ary[position])
        }
    }
}
