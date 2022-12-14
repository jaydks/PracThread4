package com.example.practhread4

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.practhread4.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //배경음악
    private var mp: MediaPlayer? = null

    var started = false // 타이머 실행여부
    var score = 0 // 점수
    var serving = "" // 서빙할 메뉴 저장

    // 테이블, 손님, 주문 이미지 담을 배열
    private var img_array_table = arrayListOf<ImageView>()
    private var img_array_customer = arrayListOf<ImageView>()
    private var img_array_order = arrayListOf<ImageView>()
    private var img_array_menu = arrayListOf<ImageView>()

    //변수들의 상태 표시할 태그
    val TAG_ON1 = "menu1" // menu1을 원하는 손님이 있다
    val TAG_ON2 = "menu2"
    val TAG_ON3 = "menu3"
    val TAG_OFF = "off" // 손님 없음
    val TAG_ING = "ing" // 손님 음식 먹는 중
    val TAG_MENU1 = "menu1"
    val TAG_MENU2 = "menu2"
    val TAG_MENU3 = "menu3"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mp = MediaPlayer.create(this, R.raw.music_main)
        mp?.isLooping = true
        mp?.start()

        //시간, 점수 초기화
        binding.time.text = "30초"
        binding.score.text = "0원"

        //테이블 배열, 초기화
        img_array_table = arrayListOf(
            binding.table1,
            binding.table2,
            binding.table3,
            binding.table4
        )
        for (i in img_array_table.indices) { // indices : index의 복수형
            img_array_table[i].setImageResource(R.drawable.table1)
            img_array_table[i].tag = TAG_OFF // 각 테이블 초기엔 손님없음 태그
        }

        //손님 배열, 초기화
        img_array_customer = arrayListOf(
            binding.customer1,
            binding.customer2,
            binding.customer3,
            binding.customer4
        )
        for (i in img_array_customer.indices) {
            img_array_customer[i].visibility = View.GONE // 처음엔 안 보이게
        }

        //말풍선(주문) 배열, 초기화
        img_array_order = arrayListOf(
            binding.table1Order,
            binding.table2Order,
            binding.table3Order,
            binding.table4Order
        )
        for (i in img_array_order.indices) {
            img_array_order[i].visibility = View.GONE // 처음엔 안 보이게
        }

        //메뉴 배열, 초기화
        img_array_menu = arrayListOf(
            binding.menu1,
            binding.menu2,
            binding.menu3
        )
        img_array_menu[0].setImageResource(R.drawable.pudding)
        img_array_menu[1].setImageResource(R.drawable.strawberrycake)
        img_array_menu[2].setImageResource(R.drawable.chicken)
        for (i in img_array_menu.indices) {
            img_array_menu[i].tag = TAG_OFF // 초기엔 해당 메뉴 원하는 손님 없음
        }

        //시작버튼 클릭
        binding.start.setOnClickListener {
            binding.start.visibility = View.INVISIBLE // 시작 누르면 시작버튼 사라짐
            binding.score.visibility = View.VISIBLE // 시작 누르면 점수텍스트 나타남

            //타이머 쓰레드 시작
            Thread(start()).start()

            //각 테이블 쓰레드 시작
            for (i in img_array_table.indices) {
                Thread(set_table(i)).start()
            }
        }


        // 메뉴 클릭 이벤트
        // menu1(푸딩) 클릭
        img_array_menu[0].setOnClickListener {
            // 해당 매뉴 주문이 들어왔을 때만 동작 (-> menu1 주문하고 TAG_ON1으로 설정했었음)
            if (img_array_table[0].tag == TAG_ON1 || img_array_table[1].tag == TAG_ON1 || img_array_table[2].tag == TAG_ON1 || img_array_table[3].tag == TAG_ON1) {
                binding.menu1Click.visibility = View.VISIBLE // 선택 메뉴 -> 큰 메뉴 보이게
                binding.menu2Click.visibility =
                    View.INVISIBLE // 현재 선택한 메뉴 제외 큰 메뉴는 작게 (어차피 framelayout으로 만들었기 때문에 큰 이미지를 띄울 때 굳이 작은 메뉴를 invisible할 필요 엇음)
                binding.menu3Click.visibility = View.INVISIBLE
                serving = "menu1"
                it.tag = TAG_MENU1
            } else { // 메뉴 클릭했지만 해당 메뉴를 원하는 손님이 없음. 잘못된 클릭 -> 감점
                Toast.makeText(applicationContext, "주문 없음(푸딩)", Toast.LENGTH_SHORT).show()
                if (score <= 0) {
                    score = 0
                    binding.score.text = "${score*100}원"
                } else binding.score.text = "${(score--)*100}원"
            }
        }
        // menu2(딸기 케이크) 클릭
        img_array_menu[1].setOnClickListener {
            // 해당 매뉴 주문이 들어왔을 때만 동작 (-> menu2 주문하고 TAG_ON2으로 설정했었음)
            if (img_array_table[0].tag == TAG_ON2 || img_array_table[1].tag == TAG_ON2 || img_array_table[2].tag == TAG_ON2 || img_array_table[3].tag == TAG_ON2) {
                binding.menu2Click.visibility = View.VISIBLE
                binding.menu1Click.visibility = View.INVISIBLE
                binding.menu3Click.visibility = View.INVISIBLE
                serving = "menu2"
                it.tag = TAG_MENU2
            } else { // 메뉴 클릭했지만 해당 메뉴를 원하는 손님이 없음. 잘못된 클릭 -> 감점
                Toast.makeText(applicationContext, "주문 없음(케이크)", Toast.LENGTH_SHORT).show()
                if (score <= 0) {
                    score = 0
                    binding.score.text = "${score*100}원"
                } else binding.score.text = "${(score--)*100}원"
            }
        }
        // menu3(치킨) 클릭
        img_array_menu[2].setOnClickListener {
            // 해당 매뉴 주문이 들어왔을 때만 동작 (-> menu3 주문하고 TAG_ON1으로 설정했었음)
            if (img_array_table[0].tag == TAG_ON3 || img_array_table[1].tag == TAG_ON3 || img_array_table[2].tag == TAG_ON3 || img_array_table[3].tag == TAG_ON3) {
                binding.menu3Click.visibility = View.VISIBLE
                binding.menu1Click.visibility = View.INVISIBLE
                binding.menu2Click.visibility = View.INVISIBLE
                serving = "menu3"
                it.tag = TAG_MENU3
            } else { // 메뉴 클릭했지만 해당 메뉴를 원하는 손님이 없음. 잘못된 클릭 -> 감점
                Toast.makeText(applicationContext, "주문 없음(치킨)", Toast.LENGTH_SHORT).show()
                if (score <= 0) {
                    score = 0
                    binding.score.text = "${score*100}원"
                } else binding.score.text = "${(score--)*100}원"
            }
        }


        // 테이블과 메뉴 매칭하기
        for (i in img_array_table.indices) {
            // 테이블 클릭 이벤트
            img_array_table[i].setOnClickListener {
                if (it.tag.toString() === serving && serving === "menu1") { //테이블 태그와 서빙 메뉴가 같고, 서빙 메뉴가 메뉴1
                    img_array_order[i].setImageResource(R.drawable.order_love) // 좋다는 말풍선
                    binding.menu1Click.visibility = View.INVISIBLE // 확대 메뉴 가리기
                    img_array_table[i].tag = TAG_ING // 서빙 받으면 먹는 중 태그로 변경
                    score += 3
                    binding.score.text = "${score*100}원"
                    img_array_table[i].setImageResource(R.drawable.table_menu1) // 테이블 위에 음식이 올라간 사진으로 변경
                } else if (it.tag.toString() === serving && serving === "menu2") {
                    img_array_order[i].setImageResource(R.drawable.order_love)
                    binding.menu2Click.visibility = View.INVISIBLE // 확대 메뉴 가리기
                    img_array_table[i].tag = TAG_ING
                    score += 3
                    binding.score.text = "${score*100}원"
                    img_array_table[i].setImageResource(R.drawable.table_menu2)
                } else if (it.tag.toString() === serving && serving === "menu3") {
                    img_array_order[i].setImageResource(R.drawable.order_love)
                    binding.menu3Click.visibility = View.INVISIBLE // 확대 메뉴 가리기
                    img_array_table[i].tag = TAG_ING
                    score += 3
                    binding.score.text = "${score*100}원"
                    img_array_table[i].setImageResource(R.drawable.table_menu3)
                } else {
                    Toast.makeText(applicationContext, "서빙 실패", Toast.LENGTH_SHORT).show()
                    img_array_order[i].setImageResource(R.drawable.order_wrong)
                    binding.menu1Click.visibility = View.INVISIBLE // 서빙 실패시에도 클릭해서 확대된 메뉴 가리기
                    binding.menu2Click.visibility = View.INVISIBLE
                    binding.menu3Click.visibility = View.INVISIBLE

                    if (score <= 0) {
                        score = 0
                        binding.score.text = "${score*100}원"
                    } else {
                        binding.score.text = "${(score--)*100}원"
                    }
                }
            }
        }
    }

    //시간 쓰레드 핸들러
    val timehandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            binding.time.text = "${msg.arg1}초"
        }
    }

    // 손님 왔을 때 핸들러
    val onHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            //랜덤 손님
            val ran_customer = Random().nextInt(4)
            img_array_customer[msg.arg1].setImageResource(R.drawable.customer1 + ran_customer) //?
            img_array_customer[msg.arg1].visibility = View.VISIBLE // 손님 이미지 보이기
            //랜덤 주문
            val ran = Random().nextInt(3)
            if (img_array_table[msg.arg1].tag == TAG_OFF) { // 테이블에 주문 없는 상태여야 주문을 만들 수 있음
                when (ran) {
                    0 -> { //랜덤이 0이면 menu1(푸딩) 주문(저장)
                        img_array_order[msg.arg1].setImageResource(R.drawable.order_menu1_1)
                        img_array_table[msg.arg1].tag = TAG_ON1 // menu1 원하는 손님 있음 태그 저장
                        img_array_order[msg.arg1].visibility = View.VISIBLE // 말풍선(주문) 보이기
                    }
                    1 -> { //랜덤이 0이면 menu2(케이크) 주문(저장)
                        img_array_order[msg.arg1].setImageResource(R.drawable.order_menu2_1)
                        img_array_table[msg.arg1].tag = TAG_ON2 // menu2 원하는 손님 있음 태그 저장
                        img_array_order[msg.arg1].visibility = View.VISIBLE
                    }
                    2 -> {//랜덤이 0이면 menu3(치킨) 주문(저장)
                        img_array_order[msg.arg1].setImageResource(R.drawable.order_menu3_1)
                        img_array_table[msg.arg1].tag = TAG_ON3 // menu3 원하는 손님 있음 태그 저장
                        img_array_order[msg.arg1].visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    // 서빙이 주문과 일치할 경우 핸들러
    var ingHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            println(msg.arg1.toString() + "번 테이블 먹는 중")
            // val ran_ing = Random().nextInt(2)
            //if (ran_ing == 0) { //좋다는 인사 말풍선
            img_array_order[msg.arg1].setImageResource(R.drawable.order_love2) // bye인사
        }
    }

    // 손님 떠날 경우 핸들러
    var offHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            println("손님 감")
            img_array_table[msg.arg1].setImageResource(R.drawable.table1)
            img_array_table[msg.arg1].tag = TAG_OFF // 손님 없음 태그
            img_array_order[msg.arg1].visibility = View.GONE // 끝난 주문 말풍선 없애기
            img_array_customer[msg.arg1].visibility = View.GONE // 밥 먹은 손님 없애기
        }
    }

    // 주문한지 4초 지났는데 음식 안 온 경우 핸들러
    var waitingHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (score <= 0) {
                score = 0
                binding.score.text = "${score*100}원"
            } else binding.score.text = "${(score--)*100}원" // 1점 감점
            img_array_order[msg.arg1].setImageResource(R.drawable.order_hurry)
        }
    }


    //시간 쓰레드 클래스
    inner class start : Runnable {
        val MAXTIME = 30
        override fun run() {
            started = true
            for (i in MAXTIME downTo 0) {
                if (!started) break
                val msg = Message()
                msg.arg1 = i
                timehandler.sendMessage(msg)
                if (i == 0) started = false

                try {
                    Thread.sleep(1000) // 1초마다
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 결과 액티비티로 이동, 점수 넘기기
            val intent = Intent(this@MainActivity, ResultActivity::class.java)
            intent.putExtra("score", score*100)
            startActivity(intent)
            finish()
        }
    }

    //각 테이블 쓰레드 클래스
    inner class set_table(index: Int) : Runnable {
        var index = index

        override fun run() {
            while (true) {
                try {
                    //테이블 빈 시간 랜덤
                    val offtime = Random().nextInt(3000) + 1000 // 1초~4초 랜덤
                    Thread.sleep(offtime.toLong())

                    //테이블 번호 handler로 보냄
                    val msg1 = Message()
                    msg1.arg1 = index
                    onHandler.sendMessage(msg1)

                    //주문 후 4초 이내에 메뉴를 제공해야 함
                    Thread.sleep(4000) // 4초 (아하 3초 지나고 나서 아래 코드를 진행해라구나 그니까 3초 지나고 음식을 먹고 있으면 ~고, 그게 아니면 음식이 아직 안 온거고)
                    if (img_array_table[index].tag === TAG_ING) { // 손님 음식 먹는 중 (-> 주문이 제대로 진행됨)
                        val msg2 = Message()
                        msg2.arg1 = index //
                        val msg3 = Message()
                        msg3.arg1 = index
                        ingHandler.sendMessage(msg2)
                        Thread.sleep(400) // 0.4초
                        // 손님 떠남
                        offHandler.sendMessage(msg3)
                    } else { // 4초 기다렸는데 주문힌 음식이 안 옴 (여전히 태그가 TAG_ON인 상태)
                        val msg4 = Message()
                        msg4.arg1 = index
                        waitingHandler.sendMessage(msg4) //2초 더 기다림. 메뉴는 볼 수 없고 대신 hurry 말풍선을 보인다
                        Thread.sleep(2000)
                        if (score <= 0) {
                            score = 0
                            binding.score.text = "${score*100}원"
                        } else binding.score.text = "${(score--)*100}원"
                        val msg5 = Message() //?
                        msg5.arg1 = index
                        offHandler.sendMessage(msg5) //손님 떠남
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mp?.pause()
    }

    override fun onResume() {
        super.onResume()
        mp?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mp?.stop()
    }

}
