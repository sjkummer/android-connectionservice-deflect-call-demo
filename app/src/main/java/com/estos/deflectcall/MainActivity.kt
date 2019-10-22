package com.estos.deflectcall

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.widget.Button
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    private var CALL_PERMISSION_REQUEST = 99

    private var phoneAccount: PhoneAccount? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val accountButton = this.findViewById<Button>(R.id.buttonEnablePhoneAccount)
        accountButton.setOnClickListener {
            showPhoneAccount()
        }

        val buttonOpenCallScreen = this.findViewById<Button>(R.id.buttonOpenCallScreen)
        buttonOpenCallScreen.setOnClickListener {
            showCallScreen()
        }
    }

    override fun onResume() {
        super.onResume()

        registerConnectionService()
    }

    fun getConnectionServiceId(): String {
        return applicationContext.packageName + ".connectionService"
    }


    fun registerConnectionService() {
        val manager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val connectionServiceId = getConnectionServiceId()
        val componentName =
            ComponentName(applicationContext, PhoneConnectionService::class.java)
        val phoneAccountHandle = PhoneAccountHandle(componentName, connectionServiceId)
        phoneAccount = manager.getPhoneAccount(phoneAccountHandle)
        if (phoneAccount == null) { // no phone account from us registered yet
            val builder = PhoneAccount.builder(
                phoneAccountHandle,
                this.resources.getText(R.string.app_name)
            )
            val uri = Uri.parse("tel:987654321")
            builder.setSubscriptionAddress(uri)
            builder.setAddress(uri)
            builder.setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            phoneAccount = builder.build()
            manager.registerPhoneAccount(phoneAccount)
        }
    }

    fun showPhoneAccount() {
        val intent = Intent()
        intent.component = ComponentName(
            "com.android.server.telecom",
            "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    fun showCallScreen() {

        if (hasCallPermission() ) {

            val account = this.phoneAccount

            if (account != null) {
                val uri = Uri.fromParts("tel", "123456789", null)

                val compontentName =
                    ComponentName(getPackageName(), PhoneConnectionService::class.java.name)

                var handle = account.accountHandle

                val extras = Bundle()
                extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)

                val manager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                manager.placeCall(uri, extras)
            }

        } else {
            requestInCallPermission()
        }
    }

    fun hasCallPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_OWN_CALLS)
            == PackageManager.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED)
            return true
        }
        return false
    }

    fun requestInCallPermission() {

        // Here, thisActivity is the current activity
        if (!hasCallPermission()) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.MANAGE_OWN_CALLS, Manifest.permission.CALL_PHONE),
                CALL_PERMISSION_REQUEST)
        } else {
            // Permission has already been granted
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CALL_PERMISSION_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    showCallScreen()

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}
