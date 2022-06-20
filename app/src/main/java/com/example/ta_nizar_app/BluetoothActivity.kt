package com.example.ta_nizar_app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ta_nizar_app.bluetooth.BluetoothAdapter
import com.example.ta_nizar_app.bluetooth.BluetoothListener
import com.example.ta_nizar_app.bluetooth.BluetoothModel

class BluetoothActivity : AppCompatActivity() {
    private var mData = arrayListOf<BluetoothModel>()
    private lateinit var mAdapter: BluetoothAdapter

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: android.bluetooth.BluetoothAdapter? = null
    private val REQUEST_ENABLE_BT = 1

    private lateinit var rvDevice: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_connect)
        rvDevice = findViewById(R.id.rvDevice)

        mData.clear()

        mAdapter = BluetoothAdapter(this, mData, object: BluetoothListener{
            override fun onItemClick(position: Int) {
//                TODO("Not yet implemented")

                val device = mData[position]
                Log.d(javaClass.simpleName, "Dev ${device.name} ${device.address}" )

                goToNext(device)
            }
        })

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        rvDevice.layoutManager = layoutManager
        rvDevice.adapter = mAdapter

        checkBluetooth()
    }

    private fun goToNext(btName: BluetoothModel){
        runOnUiThread{
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("bt_address", btName.address)
                startActivity(intent)
                finish()
            }, 2500)
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
            val enableBtIntent = Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
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

        mData.clear()

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
        pairedDevices?.forEach { device ->
            Log.i(javaClass.simpleName, "${device.name} : ${device.address}")
            val dev = BluetoothModel()
            dev.name = device.name
            dev.address = device.address

            mData.add(dev)
        }

        mAdapter.notifyDataSetChanged()
    }

    private val resultEnableBt = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result->
        if(result.resultCode == Activity.RESULT_OK){
            checkBluetoothDevices()
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