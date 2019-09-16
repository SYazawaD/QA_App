package com.yuzawa.shouta.techacademy.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_question_detail.*
import java.util.*

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            val map = dataSnapshot.value as Map<String, String>
            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
        }
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
        }
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
        }
        override fun onCancelled(databaseError: DatabaseError) {
        }
    }
    private val mEventListener2 = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            if (dataSnapshot.value != null){
              favorite.text = "お気に入り登録済"
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
        }
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
        }
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
        }
        override fun onCancelled(databaseError: DatabaseError) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        //val mFavoriteArrayList = ArrayList<Favorite>()
        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question
        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        //お気に入りボタンの表示
        val user2 = FirebaseAuth.getInstance().currentUser
        if (user2 != null) {
            // 何もしない
            val btn3 = this.findViewById<View>(R.id.favorite) as Button
            btn3.setVisibility(View.VISIBLE);
        } else {
            // お気に入り画面を表示しない
            val btn3 = this.findViewById<View>(R.id.nav_favorite) as Button
            btn3.setVisibility(View.INVISIBLE);
        }

        favorite.setOnClickListener {
            val dataBaseReference2 = FirebaseDatabase.getInstance().reference
            val genreRef2 = dataBaseReference2.child(FavoritePATH).child(FirebaseAuth.getInstance().currentUser!!.uid).child(mQuestion.questionUid)

            val data2 = HashMap<String, String>()
            if (favorite.text =="お気に入り未登録" ){
/*                // UID
                //                data2["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

                // タイトルと本文を取得する
  //              val title = mQuestion.title
  //              val body = mQuestion.body

                // Preferenceから名前を取る
                //val sp = PreferenceManager.getDefaultSharedPreferences(this)
                //val name = sp.getString(NameKEY, "")

  //              data2["title"] = title
  //              data2["body"] = body
  //              data2["name"] = name*/

              data2["genre"] = mQuestion.genre.toString()
                genreRef2.setValue(data2) //pushは新規フォルダを作成してしまうためNG
            }else{
                genreRef2.removeValue()
                favorite.text ="お気に入り未登録"
                //お気に入り解除の処理
            }
        }

        //お気に入りボタンの表示--ここまで
        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // --- ここまで ---}
            }
        }
        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
        mFavoriteRef = dataBaseReference.child(FavoritePATH).child(FirebaseAuth.getInstance().currentUser!!.uid).child(mQuestion.questionUid)
        mFavoriteRef.addChildEventListener(mEventListener2)
    }
}