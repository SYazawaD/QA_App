package com.yuzawa.shouta.techacademy.qa_app

import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.support.design.widget.Snackbar
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ListView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class FavoriteActivity : AppCompatActivity() {
    //データベースの読み書きに使用
    private lateinit var mDataBaseReference_f: DatabaseReference
    // Firebaseへのアクセスに必要なDatabaseReferenceクラスと、ListView、QuestionクラスのArrayList、QuestionsListAdapterを定義
    private lateinit var mListView_f: ListView
    private lateinit var mQuestionArrayList_f: ArrayList<Question>
    private lateinit var mAdapter_f: QuestionsListAdapter
    private var mGenreRef_f: DatabaseReference? = null
    private var mGenre_f = 1

    private val mEventListener_f = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val question_f = dataSnapshot.key as String
            val map = dataSnapshot.value as Map<String, String>
            val genre = map["genre"] ?: ""

//テスト実行用
//            Log.d("favorite", question_f)
//            Log.d("favorite", genre)

            val mGenreRef = mDataBaseReference_f.child(ContentsPATH).child(genre.toString()).child(question_f)
            mGenreRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val map = snapshot.value as Map<String, String>
                    val title = map["title"] ?: ""
                    val body = map["body"] ?: ""
                    val name = map["name"] ?: ""
                    val uid = map["uid"] ?: ""
                    val imageString = map["image"] ?: ""
                    val bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }
                    val answerArrayList = ArrayList<Answer>()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            answerArrayList.add(answer)
                        }
                    }
                    Log.d("favorite_title", title)
                    Log.d("favorite_body", body)
                    val question = Question(
                        title, body, name, uid, dataSnapshot.key ?: "",
                        genre.toInt(), bytes, answerArrayList
                    )
                    mQuestionArrayList_f.add(question)
                    mAdapter_f.notifyDataSetChanged()
                }

                override fun onCancelled(firebaseError: DatabaseError) {
                }

            }) //42行目の"("の終わり
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
        }

        override fun onChildRemoved(p0: DataSnapshot) {
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }

        override fun onCancelled(p0: DatabaseError) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.favorite_main)

    }

    override fun onResume() {
        super.onResume()
        val extras: Bundle = intent.extras
        val user = extras.get("user") as String

        // ListViewの準備
        mListView_f = findViewById(R.id.listView_f)
        mQuestionArrayList_f = ArrayList<Question>()
        mAdapter_f = QuestionsListAdapter(this)

        mAdapter_f.setQuestionArrayList(mQuestionArrayList_f)
        mListView_f.adapter = mAdapter_f
        mAdapter_f.notifyDataSetChanged()

        // Firebase初期化
        mDataBaseReference_f = FirebaseDatabase.getInstance().reference
        val genreRef_f = mDataBaseReference_f.child(FavoritePATH).child(user)
        genreRef_f.addChildEventListener(mEventListener_f)

        mListView_f.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList_f[position])
            startActivity(intent)
        }
    }
}