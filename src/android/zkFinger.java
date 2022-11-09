package cordova.plugin.zkteco.scan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import android.content.Context;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// import com.zkteco.android.biometric.core.device.ParameterHelper;
// import com.zkteco.android.biometric.core.device.TransportType;
// import com.zkteco.android.biometric.core.utils.LogHelper;
// import com.zkteco.android.biometric.core.utils.ToolUtils;
// import com.zkteco.android.biometric.module.fingervein.FingerVeinCaptureListener;
// import com.zkteco.android.biometric.module.fingervein.FingerVeinFactory;
// import com.zkteco.android.biometric.module.fingervein.FingerVeinSensor;
// import com.zkteco.android.biometric.module.fingervein.FingerVeinService;
// import com.zkteco.android.biometric.module.fingervein.exception.FingerVeinException;
// import com.zkteco.zkfinger.FingerprintService;

// import com.example.zkfinger10demo.ZKUSBManager.ZKUSBManager;
// import com.example.zkfinger10demo.ZKUSBManager.ZKUSBManagerListener;
// import com.example.zkfinger10demo.util.PermissionUtils;

import com.zkteco.android.biometric.FingerprintExceptionListener;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.core.utils.ToolUtils;
import com.zkteco.android.biometric.module.fingerprintreader.FingerprintCaptureListener;
import com.zkteco.android.biometric.module.fingerprintreader.FingerprintSensor;
import com.zkteco.android.biometric.module.fingerprintreader.FingprintFactory;
import com.zkteco.android.biometric.module.fingerprintreader.ZKFingerService;
import com.zkteco.android.biometric.module.fingerprintreader.exception.FingerprintException;

import java.util.ArrayList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import android.app.Activity;
// public FindLocation(Activity activity) {
//     this.activity = activity;
// }
public class zkFinger extends CordovaPlugin 
{

   private static final int VID = 0x1b55;    //zkteco device VID always 6997
    private static final int PID = 0x0124;    //fvs100 PID always 512

    //private FingerVeinSensor fingerVeinSensor = null;
    private FingerprintSensor fingerprintSensor = null;
    private boolean bstart = false;
    private boolean bIsRegister = false;
    private int enrollCount = 3;
    private int enrollIndex = 0;
    private byte[][] regFPTemparray = new byte[3][2048];
    private String[] regFVTemplates = new String[3];
    private int regID = 0;

    private void startFingerprintSensor() {
        // Define output log level
        LogHelper.setLevel(Log.WARN);
        // Start fingerprint sensor
        Map fingerprintParams = new HashMap();
        //set vid
        fingerprintParams.put(ParameterHelper.PARAM_KEY_VID, VID);
        //set pid
        fingerprintParams.put(ParameterHelper.PARAM_KEY_PID, PID);
        fingerprintSensor = FingprintFactory.createFingerprintSensor(this, TransportType.USB, fingerprintParams);
    }

    public void OnBnBegin(View view) throws FingerprintException{
        try {
            if(bstart) return;
            fingerprintSensor.open(0);
            finalFingerprintCaptureListener listener = new FingerprintCaptureListener() {
                @Override
                public void captureOK(final byte[] fpImage) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(null != fpImage){
                                ToolUtils.outputHexString(fpImage);
                                LogHelper.i("width=" + fingerprintSensor.getImageWidth() + "\nHeight=" + fingerprintSensor.getImageHeight());
                                Bitmap bitmapFp = ToolUtils.renderCroppedGreyScaleBitmap(fpImage, fingerprintSensor.getImageWidth(), fingerprintSensor.getImageHeight());
                                //saveBitmap(bitmapFp);
                                imageView.setImageBitmap(bitmapFp);
                            }
                        //textView.setText("FakeStatus:" + fingerprintSensor.getFakeStatus());
                        }
                    });
                }
            };
        }
        catch(Exception e){
            callbackContext.error(e.getMessage());
        }
    }
}
