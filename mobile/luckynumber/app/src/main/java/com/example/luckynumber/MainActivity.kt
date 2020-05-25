package com.example.luckynumber

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import com.example.luckynumber.Common.Common
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.lang.Exception
import java.lang.StringBuilder
import com.example.luckynumber.Services.PlayLoseService
import com.example.luckynumber.Services.PlayWinService

class MainActivity : AppCompatActivity() {


    internal  var isDisconnect = false;
    internal  var isBet = false;
    internal  var canPlay = false;
    internal  var resultNumber = -1;
    lateinit var socket:Socket


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        try {
            socket = IO.socket("http://192.168.179.1:3000")
            socket.on(Socket.EVENT_CONNECT)
            {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Connected", Toast.LENGTH_SHORT).show()
                }

            }
            socket.connect()
        }
        catch (ex:Exception){
            Toast.makeText(this@MainActivity, ""+ex.message, Toast.LENGTH_SHORT).show()
        }

        btn_disconnect.setOnClickListener {
            if ( !isDisconnect){
                socket.disconnect();
                btn_disconnect.text = "CONNECT";
            }else{
                socket.connect();
                btn_disconnect.text = "DISCONNECT";
            }
            isDisconnect = !isDisconnect;
        }

        btn_submit.setOnClickListener {
            try{
                if ( canPlay){
                    if ( !isBet)
                    {
                        val bet_money_value = Integer.parseInt(edit_txt_moneyValue.text.toString())
                        if ( Common.score >= bet_money_value )
                        {
                            val json = JSONObject()
                            json.put("money", bet_money_value.toString())
                            json.put("betValue", Integer.parseInt(edit_txt_betValue.text.toString()))

                            socket.emit("client_Send_money", json)
                            Common.score -= bet_money_value;
                            txt_score.text = Common.score.toString()
                            isBet = true;




                        }
                        else
                        {
                            Toast.makeText(this@MainActivity, "You don't have enough mount", Toast.LENGTH_SHORT).show()
                        }


                    }else
                    {
                        Toast.makeText(this@MainActivity, "You already bet this turn", Toast.LENGTH_SHORT).show()
                    }

                }else
                {
                    Toast.makeText(this@MainActivity, "Please wait for next turn", Toast.LENGTH_SHORT).show()

                }


            }catch (ex:Exception){
                Toast.makeText(this@MainActivity, ""+ex.message, Toast.LENGTH_SHORT).show()

            }
        }


        //Register
        socket.on("restart" )
        {
            canPlay = true
            runOnUiThread {
                txt_result.visibility = View.GONE
            }
        }


        socket.on("broadcast"){
            args ->
            runOnUiThread {
                txt_Count.text = StringBuilder("Timer: ").append((args[0].toString()))
                txt_Status.text = ""
                txt_result.text = ""

            }
        }

        socket.on("result"){
            args -> resultNumber = Integer.parseInt(args[0].toString())
            runOnUiThread {
                txt_result.visibility = View.VISIBLE
                txt_result.text = StringBuilder("Result :").append(args[0].toString())

            }
        }

        socket.on("wait_before_restart"){
            args -> canPlay == false
            runOnUiThread {
                txt_Status.text ="Please wating for " + args[0] + " seconds"
                txt_Count.text = "Waiting....."
                isBet = false
            }

        }

        socket.on("money_send"){
            args ->    runOnUiThread {

            txt_money.text = args[0].toString()

            }
        }

        socket.on("reward"){

            args -> runOnUiThread {

                txt_result.text ="Result: $resultNumber You win "+args[0]+" USD";
                txt_result.setBackgroundResource(R.drawable.win_text_view)
                Common.score += Integer.parseInt(args[0].toString())
                txt_score.text = Common.score.toString();

            Intent(this, PlayWinService::class.java).also {
                startService(it)
            }
            }


//            Intent(this, PlayWinService::class.java).also {
//                startService(it)
//           }

        }

        socket.on("lose"){
                args -> runOnUiThread {
            txt_result.text ="Result: $resultNumber You lose " + args[0] + " USD";
                    txt_result.setBackgroundResource(R.drawable.lose_text_view)
//                    Common.score -= Integer.parseInt(args[0].toString())
//                    txt_score.text = Common.score.toString();

            Intent(this, PlayLoseService::class.java).also {
                startService(it)
            }
        }


        }

        socket.on(Socket.EVENT_DISCONNECT){
            runOnUiThread {
                txt_result.text ="DISCONNECT"
                txt_Count.text = "DISCONNECT"
                txt_money.text = "DISCONNECT"
            }
        }


    }
}
