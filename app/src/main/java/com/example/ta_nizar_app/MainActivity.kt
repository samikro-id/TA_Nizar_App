package com.example.ta_nizar_app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ta_nizar_app.R

class MainActivity : AppCompatActivity() {
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val REQUEST_ENABLE_BT = 1

    private lateinit var logo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logo = findViewById(R.id.logo)

        animFade()

        Log.i(javaClass.simpleName, "Android ${Build.VERSION.SDK_INT}")

        checkBluetooth()
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
            goToNext()
        }
    }

    private val resultEnableBt = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result->
        if(result.resultCode == Activity.RESULT_OK){
            goToNext()
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

    private fun animFade(){
        val fadeIn: Animation = AlphaAnimation(0.0f, 1.0f)
        fadeIn.interpolator = DecelerateInterpolator()

        val animation = AnimationSet(false)

        fadeIn.duration = 3000
        animation.addAnimation(fadeIn)
        logo.animation = animation
    }

    private fun goToNext(){
        runOnUiThread{
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, BluetoothActivity::class.java)
                startActivity(intent)
                finish()
            }, 2500)
        }
    }

    fun appExit(){
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 2500)
    }
}