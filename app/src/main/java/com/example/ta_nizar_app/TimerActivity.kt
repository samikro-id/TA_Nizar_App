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
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ta_nizar_app.home.HomeAdapter
import com.example.ta_nizar_app.home.HomeListener
import com.example.ta_nizar_app.home.HomeModel
import com.example.ta_nizar_app.timer.TimerAdapter
import com.example.ta_nizar_app.timer.TimerListener
import com.example.ta_nizar_app.timer.TimerModel
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class TimerActivity : AppCompatActivity() {
    val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var btSocket: BluetoothSocket? = null
    private var btDevice: BluetoothDevice? = null
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: android.bluetooth.BluetoothAdapter? = null

    private var _inStream: InputStream? = null
    private var _outStream: OutputStream? = null

    private val REQUEST_ENABLE_BT = 1
    private var address: String = ""

    private lateinit var rvTimer: RecyclerView
    private var mData = arrayListOf<TimerModel>()
    private lateinit var mAdapter: TimerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        rvTimer = findViewById(R.id.rvTimer)

        address = intent.getStringExtra("bt_address").toString()
        Log.d(javaClass.simpleName, "msg: ${address}")
        
        checkBluetooth()

        InitializeSocket(btDevice!!)

        mData.clear()
        val index = arrayListOf<Int>(1,2,3,4,5,6,7,8)
        index?.forEach { i ->
            val item = TimerModel()

            item.name = "L${i}"
            item.state = "OFF"
            item.run = false
            item.counter = 0

            mData.add(item)
        }

        mAdapter = TimerAdapter(this, mData, object : TimerListener {
            override fun onItemClick(position: Int) {
//                TODO("Not yet implemented")
                val timer = mData[position]
                Log.d(javaClass.simpleName, "Tm ${timer.name} ${position} ${timer.counter}")

                if(timer.counter > 0 && timer.run == false) {
                    mData[position].run = true
                    mAdapter.notifyDataSetChanged()

                    object : CountDownTimer(timer.counter.toLong() * 1000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val remain = millisUntilFinished / 1000
                            Log.d(javaClass.simpleName,"Count ${timer.name} ${remain}")

                            mData[position].counter = remain.toInt()
                            mAdapter.notifyDataSetChanged()
                        }

                        override fun onFinish() {
                            Log.d(javaClass.simpleName, "Timer ${timer.name} Done")

                            mData[position].run = false
                            mAdapter.notifyDataSetChanged()

                            val true_position = position

                            when(mData[position].state){
                                "ON" -> control("OFF", true_position + 1)
                                else -> control("ON", true_position + 1)
                            }
                        }
                    }.start()
                }
            }

            override fun onSwClick(position: Int, on: Boolean) {
//                TODO("Not yet implemented")
                when(on){
                    true -> mData[position].state = "ON"
                    else -> mData[position].state = "OFF"
                }
//                mAdapter.notifyDataSetChanged()
            }

            override fun onNpChanged(position: Int, newVal: Int) {
//                TODO("Not yet implemented")
                mData[position].counter = newVal
//                mAdapter.notifyDataSetChanged()
            }
        })

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        rvTimer.layoutManager = layoutManager
        rvTimer.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    fun back(){
        appExit()
    }

    fun control(command: String, index: Int){
        var cmd : String
        if(command == "OFF"){
            cmd = "${index}1\r\n"
        }
        else{
            cmd = "${index}0\r\n"
        }

        Log.d(javaClass.simpleName, cmd)
        write(cmd)

        mAdapter.notifyDataSetChanged()
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

    @Throws(Exception::class)
    fun write(s: String) {
        if(btSocket?.isConnected == true){
            _outStream?.write(s.toByteArray())
        }
        else{
            Toast.makeText(this, "Bluetooth Disconnected !!", Toast.LENGTH_SHORT).show()
            appExit()
        }
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
            appExit()
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
            btSocket?.close()
            finish()
        }, 2500)
    }
}