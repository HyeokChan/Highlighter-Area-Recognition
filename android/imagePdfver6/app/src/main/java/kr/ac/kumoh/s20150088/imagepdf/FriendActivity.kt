package kr.ac.kumoh.s20150088.imagepdf

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_friend.*
import kotlinx.android.synthetic.main.activity_main.*

class FriendActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)
        supportFragmentManager.beginTransaction().replace(R.id.fgView, friendListFragment()).commit()
        btnFreindList.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.fgView, friendListFragment()).commit()
        }
        btnRequestRecv.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.fgView, requestRecvFragment()).commit()
        }
    }
}
