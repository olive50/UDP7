package com.olivetic.udp7

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.StrictMode
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.UnknownHostException


class MainActivity : AppCompatActivity() {

    //declared variables
    private var clientThread: ClientThread? = null
    private var thread: Thread? = null
    private var tvName:TextView? = null
    private var tvPrice:TextView? = null
    private var tvStock:TextView? = null
    var code:String="123"


    override fun onCreate(savedInstanceState: Bundle?) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
       // window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setContentView(R.layout.activity_main)

        //Create a thread so that the received data does not run within the main user interface
        clientThread = ClientThread()
        thread = Thread(clientThread)
        thread!!.start()

        // create a value that is linked to a button called (id) MyButton in the layout
        val  productCode = findViewById<EditText>(R.id.tv_barcode)
        //val buttonScan = findViewById<Button>(R.id.scanBtn)
        tvName = findViewById(R.id.tvName)
        //tvName!!.text = ""
        tvPrice = findViewById(R.id.tvPrice)
       // tvPrice!!.text = ""
        tvStock = findViewById(R.id.tvStock)
        //tvTemp!!.text = ""
        //lateinit var productCode: EditText


        //Create a listener that will respond if MyButton is clicked
        /*buttonScan.setOnClickListener{
            //send a UDP package as a test
            code=productCode.getText().toString()
            // your code to perform when the user clicks on the button

            Toast.makeText(this@MainActivity, "Code to Search : $code", Toast.LENGTH_SHORT).show()
            sendUDP(code)
            //sendUDP("123")
        }
*/
        productCode.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val code = v.text.toString()
                sendUDP(code)
                Toast.makeText(this@MainActivity, code, Toast.LENGTH_SHORT).show()
                return@OnEditorActionListener true
            }
            false
        })

        productCode.setOnKeyListener {v,keyCode, event->
            if(keyCode== KeyEvent.KEYCODE_ENTER
                && event.action == KeyEvent.ACTION_UP){

                val code =productCode.text.toString()
                sendUDP(code)
                Toast.makeText(this@MainActivity, " search for : $code", Toast.LENGTH_SHORT).show()
                return@setOnKeyListener true

            }
            false
        }

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }


    }

    override fun onStart() {
        super.onStart()
        sendUDP("testcode")
        registerBarcodeScannerBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterBarcodeScannerBroadcastReceiver()
    }



    //************************************ Some test code to send a UDP package
    fun sendUDP(messageStr: String) {
        //tvName?.text = ""
        //tvPrice?.text = ""
        //tvTemp?.text = ""
        // Hack Prevent crash (sending should be done using a separate thread)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)  //Just for testing relax the rules...
        try {
            //Open a port to send a UDP package
            val socket = DatagramSocket()
            //socket.broadcast = true
            val sendData = messageStr.toByteArray()
            val sendPacket = DatagramPacket(sendData, sendData.size,
                                            InetAddress.getByName(SERVER_IP),
                                             SERVERPORT)
            socket.send(sendPacket)
            Toast.makeText(this@MainActivity,
                "Code Sent:"+messageStr, Toast.LENGTH_SHORT).show()
            println("Packet Sent")
        } catch (e: IOException) {
            println(">>>>>>>>>>>>> IOException  "+e.message)
        }
    }

    //************************************* Some test code for receiving a UDP package
    internal inner class ClientThread : Runnable {
        private var socket: DatagramSocket? = null
        private val recvBuf = ByteArray(1500)
        private val packet = DatagramPacket(recvBuf, recvBuf.size)
        // **********************************************************************************************
        // * Open the network socket connection and start receiving a Byte Array                        *
        // **********************************************************************************************
        override fun run() {
            try {
                //Keep a socket open to listen to all the UDP trafic that is destined for this port
                socket = DatagramSocket(CLIENTPORT)
                while (true) {
                    //Receive a packet
                    socket!!.receive(packet)

                    //Packet received
                    println("Packet received from: " + packet.address.hostAddress)
                    val data = String(packet.data).trim { it <= ' ' }
                    println("Packet received; data: $data")
                    //Change the text on the main activity view
                    runOnUiThread {
                       // if (tvName?.text == "") {
                            //tvName?.text = data.substring(0,4).trim()
                            if(data.contains("1401L") or
                                data.contains("1201L")){
                                tvName?.text = data.substring(5,data.length).trim()
                                return@runOnUiThread
                            }
                        if(data.contains("1901L") or
                            data.contains("1601L")  ){
                            /*if(data.contains("Article inconnu")){
                                tvPrice?.setGravity(2)
                            }*/
                            tvPrice?.text = data.substring(5,data.length).trim()
                            return@runOnUiThread
                        }
                        if(data.contains("1:01L")){
                        tvStock?.text = data.substring(5,data.length).trim()
                        return@runOnUiThread

                        }else{
                            Toast.makeText(this@MainActivity,
                                "Info : "+data, Toast.LENGTH_SHORT).show()
                                return@runOnUiThread
                            }

                        //}
                        /*if (tvPrice?.text == "") {
                            tvPrice?.text = data
                            return@runOnUiThread
                        }
                        if (tvTemp?.text == "") {
                            tvTemp?.text = data
                            return@runOnUiThread
                        }*/
                        /*Timer().schedule(2000) {
                            tvName?.text = ""
                            tvPrice?.text = ""
                            tvTemp?.text = ""
                        }*/
                    }}
            }
            catch (e1: IOException) {
                println(">>>>>>>>>>>>> IOException  "+e1.message)
                socket?.close()
            }
            catch (e2: UnknownHostException) {
                println(">>>>>>>>>>>>> UnknownHostException  "+e2.message)
                socket?.close()
            }
            finally{
                socket?.close()
            }
        }
    }

    companion object {
        val CLIENTPORT = 9001
        val SERVERPORT = 9000
        //val SERVER_IP = "10.10.41.157"
        val SERVER_IP = "192.168.0.191"
    }

    // scanner

    private fun registerBarcodeScannerBroadcastReceiver() {
        // 配置条码扫描服务为广播模式，没有自动换行，打开读码提示灯
        val intent = Intent("ACTION_BAR_SCANCFG")
        intent.putExtra("EXTRA_SCAN_MODE", 3)
        intent.putExtra("EXTRA_SCAN_AUTOENT", 0)
        intent.putExtra("EXTRA_SCAN_NOTY_LED", 1)
        sendBroadcast(intent)
        registerReceiver(
            barcodeScannerBroadcastReceiver,
            IntentFilter("nlscan.action.SCANNER_RESULT")
        )
    }

    private fun unregisterBarcodeScannerBroadcastReceiver() {
        unregisterReceiver(barcodeScannerBroadcastReceiver)
    }

    private val barcodeScannerBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val scanResult_1 = intent.getStringExtra("SCAN_BARCODE1")
            //final String scanResult_2=intent.getStringExtra("SCAN_BARCODE2");
            val scanStatus = intent.getStringExtra("SCAN_STATE")
            if (null == scanResult_1 || null == scanStatus || scanResult_1.isEmpty() || scanStatus.isEmpty()) {
                return
            }
            if ("ok" == scanStatus) {
                sendUDP(scanResult_1)
                //tvBarcode!!.text = scanResult_1
               // val codeId = intent.getIntExtra("SCAN_BARCODE_TYPE", -1)
                //tvCodeId!!.text = "" + codeId
            }
        }
    }

}