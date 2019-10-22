package com.estos.deflectcall;

import android.os.Build;
import android.telecom.Connection;
import android.telecom.DisconnectCause;

import androidx.annotation.RequiresApi;

/**
 * Created by Sebastian Schmid on 2019-10-22.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class CallConnection extends Connection {

    public CallConnection() {
        setAudioModeIsVoip(true);
    }

    @Override
    public void onAbort() {
        super.onAbort();
        dropCall();
    }
    @Override
    public void onReject() {
        super.onReject();
        dropCall();
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        dropCall();
    }

    private void dropCall() {
        setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        destroy();
    }
}
