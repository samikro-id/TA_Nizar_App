package com.example.ta_nizar_app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ta_nizar_app.bluetooth.BluetoothListener
import com.example.ta_nizar_app.bluetooth.BluetoothModel
import com.example.ta_nizar_app.home.HomeAdapter
import com.example.ta_nizar_app.home.HomeListener
import com.example.ta_nizar_app.home.HomeModel
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class HomeActivity : AppCompatActivity() {
    val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var btSocket: BluetoothSocket? = null
    private var btDevice: BluetoothDevice? = null
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: android.bluetooth.BluetoothAdapter? = null

    private var _inStream: InputStream? = null
    private var _outStream: OutputStream? = null

    private val REQUEST_ENABLE_BT = 1
    private var address: String = ""

    private lateinit var rvButton: RecyclerView
    private var mData = arrayListOf<HomeModel>()
    private lateinit var mAdapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_home)
        rvButton = findViewById(R.id.rvButton)

        address = intent.getStringExtra("bt_address").toString()
        Log.d(javaClass.simpleName, "msg: ${address}")

        checkBluetooth()

        InitializeSocket(btDevice!!)

        mData.clear()
        val index = arrayListOf<Int>(0,1,2,3,4,5,6,7,8)
        index?.forEach { i ->
            val item = HomeModel()
            if(i == 0){
                item.name = "Semua"
            }
            else{
                item.name = "Lampu ${i}"
            }

            item.id = "${i}"
            item.state = "OFF"

            mData.add(item)
        }


        mAdapter = HomeAdapter(this, mData, object : HomeListener {
                override fun onItemClick(position: Int) {
//                TODO("Not yet implemented")

                    val device = mData[position]
                    Log.d(javaClass.simpleName, "Bt ${device.name} ${position}")

                    control(device.state.toString(), position)
                }
            })

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        rvButton.layoutManager = layoutManager
        rvButton.adapter = mAdapter

    }

    fun control(command: String, index: Int){
        var cmd : String
        if(command == "OFF"){
            cmd = "${index}1\r\n"

            if(index == 0){
                mData.forEach { data ->
                    data.state = "ON"
                }
            }else{
                mData[index].state = "ON"
            }
        }
        else{
            cmd = "${index}0\r\n"

            if(index == 0){
                mData.forEach { data ->
                    data.state = "OFF"
                }
            }else{
                mData[index].state = "OFF"
            }
        }

        Log.d(javaClass.simpleName, cmd)
        write(cmd)

        mAdapter.notifyDataSetChanged()
    }

    fun parseSpeech(string:String){

    }

    fun getSpeechInput(view: View){
        val mSpeechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mSpeechIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
        mSpeechIntent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Silahkan ucapkan perintah Anda..."
        )
        startActivityForResult(mSpeechIntent, 10)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            10 -> if(resultCode == RESULT_OK && data != null){
                    val spokenText: String? =  data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { text -> text?.get(0) }
                    Toast.makeText(this, "you said ${spokenText}", Toast.LENGTH_SHORT).show()


                }
        }

    }
    fun InitializeSocket(device: BluetoothDevice){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val resultBluetoothPermission = checkBluetoothPermission(Manifest.permission.BLUETOOTH_CONNECT)
            Log.d(javaClass.simpleName, "bt check2 ${resultBluetoothPermission}")
            if(resultBluetoothPermission > 0){
                return
            }
        }

        try {
            btSocket = device.createRfcommSocketToServiceRecord(myUUID)

            Log.d(javaClass.simpleName, "success")
        } catch (e: Exception) {
            //Error

            Toast.makeText(applicationContext, "Error !!!", Toast.LENGTH_SHORT).show()
            appExit()
        }

        try {
            btSocket?.connect()
        } catch (connEx: Exception) {
            try {
                btSocket?.close()
            } catch (closeException: Exception) {
                //Error

                Toast.makeText(applicationContext, "Error !!!", Toast.LENGTH_SHORT).show()

                appExit()
            }
        }

        if (btSocket != null && btSocket?.isConnected == true) {
            //Socket is connected, now we can obtain our IO streams
            Log.d(javaClass.simpleName,"connected");

            try {
                _inStream = btSocket?.getInputStream()
                _outStream = btSocket?.getOutputStream()
            } catch (e: Exception) {
                //Error

                Toast.makeText(applicationContext, "Error !!!", Toast.LENGTH_SHORT).show()
                appExit()
            }
        }
    }

    fun clickme(view: View){
        Log.i(javaClass.simpleName, "send")

        var data: String
        data = "00\r\n"
        write(data)
    }

    @Throws(Exception::class)
    fun write(s: String) {
        _outStream?.write(s.toByteArray())
    }

    private fun checkBluetooth(){
        if(bluetoothManager == null){
            bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if(bluetoothManager == null){
                Log.e(javaClass.simpleName, "unable to init BluetoothManager");
                return
            }
        }

        bluetoothAdapter = bluetoothManager!!.adapter
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.d(javaClass.simpleName,"Device doesn't support bluetooth")
            Toast.makeText(applicationContext, "Your Device doesn't support bluetooth !!!", Toast.LENGTH_SHORT).show()
            finish();
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val resultBluetoothPermission = checkBluetoothPermission(Manifest.permission.BLUETOOTH_ADMIN)
                Log.d(javaClass.simpleName, "bt check1 ${resultBluetoothPermission}")
                if(resultBluetoothPermission > 0){

                } else {
                    Log.i(javaClass.simpleName, "Bluetooth Granted")
                    checkBluetoothState()
                }
            } else {
                checkBluetoothState()
            }
        }
    }

    private fun checkBluetoothPermission(
        permission:String
    ) : Int {
        val permissionCheck =  ContextCompat.checkSelfPermission(this, permission)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i(javaClass.simpleName, "Bluetooth Permission not granted");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showExplanation(
                    "Permission Needed",
                    "Rationale",
                    arrayOf(permission),
                    REQUEST_ENABLE_BT
                )
                return 2
            } else {
                requestPermissionMulti(arrayOf(permission), REQUEST_ENABLE_BT)
                return 1
            }
        }
        else{
            return 0
        }
    }

    private fun checkBluetoothState(){
        if (!bluetoothAdapter!!.isEnabled) {
            Log.i(javaClass.simpleName, "Bluetooth is off")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultEnableBt.launch(enableBtIntent)
        } else {
            checkBluetoothDevices()
        }
    }

    private fun checkBluetoothDevices(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val resultBluetoothPermission = checkBluetoothPermission(Manifest.permission.BLUETOOTH_CONNECT)
            Log.d(javaClass.simpleName, "bt check2 ${resultBluetoothPermission}")
            if(resultBluetoothPermission > 0){
                return
            }
        }

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
        pairedDevices?.forEach { device ->
            if(device.address == address){
                btDevice = device
            }
        }
    }

    private val resultEnableBt = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result->
        if(result.resultCode == Activity.RESULT_OK){

        }
        else{
            Toast.makeText(this, "Deny Enable BT", Toast.LENGTH_SHORT).show()
            appExit();
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.BLUETOOTH) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun showExplanation(
        title: String,
        message: String,
        permission: Array<String>,
        permissionRequestCode: Int
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok,
                DialogInterface.OnClickListener { dialog, id ->
                    requestPermissionMulti(
                        permission, permissionRequestCode
                    )
                })
        builder.create().show()
    }

    private fun requestPermissionMulti(
        permissionArray: Array<String>,
        permissionRequestCode: Int
    ) {
        ActivityCompat.requestPermissions(this, permissionArray, permissionRequestCode)
    }

    fun appExit(){
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 2500)
    }
}