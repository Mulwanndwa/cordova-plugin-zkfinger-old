package cordova.plugin.zkteco.scan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import android.content.Context;

import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.core.utils.ToolUtils;

import com.zkteco.android.biometric.module.fingerprintreader.FingerprintCaptureListener;
import com.zkteco.android.biometric.module.fingerprintreader.FingerprintSensor;
import com.zkteco.android.biometric.module.fingerprintreader.FingprintFactory;
import com.zkteco.android.biometric.module.fingerprintreader.ZKFingerService;
import com.zkteco.android.biometric.module.fingerprintreader.exception.FingerprintException;

import com.zkteco.zkfinger.FingerprintService;

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



public class zkFinger extends CordovaPlugin 
{

    private static final int VID = 0x1b55;    //zkteco device VID always 6997
    private static final int PID = 0x0124;    //fvs100 PID always 512

    private FingerprintSensor fingerVeinSensor = null;
    private boolean bstart = false;
    private boolean bIsRegister = false;
    private int enrollCount = 3;
    private int enrollIndex = 0;
    private byte[][] regFPTemparray = new byte[3][2048];
    private String[] regFVTemplates = new String[3];
    private int regID = 0;

    private void startFingerVeinSensor(CallbackContext callbackContext)
    {
        try
        {

            //@ Capture the application context
            Context context = cordova.getActivity().getApplicationContext();

            // Start fingerprint sensor
            Map fingerprintParams = new HashMap();
            //set vid
            fingerprintParams.put(ParameterHelper.PARAM_KEY_VID, VID);
            //set pid
            fingerprintParams.put(ParameterHelper.PARAM_KEY_PID, PID);
            fingerVeinSensor = FingprintFactory.createFingerprintSensor(context, TransportType.USB, fingerprintParams);

        }
        catch(Exception e)
        {
            callbackContext.error(e.getMessage());
        }
    }

    /*
    @Override
    protected void onDestroy() 
    {
        super.onDestroy();
        // Destroy fingerprint sensor when it's not used
        FingerVeinFactory.destroy(fingerVeinSensor);
    }
    */

    public int[] json2int (JSONArray arr)
    {

        // Create an int array to accomodate the numbers.
        int[] respArr = new int[arr.length()];

        // Extract numbers from JSON array.
        for (int i = 0; i < arr.length(); ++i) {
            respArr[i] = arr.optInt(i);
        }

        return  respArr;
    }


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException 
    {

        try
        {

            //@ establish a connection to the device
            startFingerVeinSensor(callbackContext);


            if (action.equals("scan")) 
            {        
                fingerVeinSensor.open(0);   
                //this.captureBio(callbackContext);
                return true;
            }
            else if(action.equals("write"))
            {

                String fileName = args.getString(0);
                int[]  content  =  json2int( args.getJSONArray(1) );
                int    length   = args.getInt(2);


                ByteBuffer byteBuffer = ByteBuffer.allocate(content.length * 4);
                IntBuffer intBuffer = byteBuffer.asIntBuffer();
                intBuffer.put(content);

                byte[] array = byteBuffer.array();

                this.writeFile(fileName,array,length,callbackContext);
                return true;

            }
            else if(action.equals("saveTemplate"))
            {

                String file     = args.getString(0);
                String content  = args.getString(1);

                this.writeTemplateToFile(file, content, callbackContext);
                return true;
            }
            
            return false;


        
        }
        catch (Exception e)
        {
                        
            callbackContext.error(e.getMessage());
            return false;
        }

    }

    private void writeTemplateToFile(String file, String content, CallbackContext callbackContext) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true)));
            out.write(content);
            out.write("\r\n");
            callbackContext.success(content);
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeFile(String fileName,byte[] content, int length, CallbackContext callbackContext){
        try{
            File file = new File(fileName);
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(content, 0, length);
            stream.close();
            callbackContext.success(content.toString());
        }
        catch(Exception e){
            callbackContext.error(e.getMessage());
        }
    }




    //@ OnBnBegin ==> captureBio
//     public void captureBio(CallbackContext cbContext)
//     {
//         try 
//         {

//             final CallbackContext callbackContext = cbContext;

//             //@ if already started, desist from continuing
//             if (bstart) callbackContext.error("Fingerprint capture already started!");

//             //@ Start finger capture
//             fingerVeinSensor.open(0);

//             final FingerprintCaptureListener listener = new FingerprintCaptureListener() 
//             {

//                 //@ Handle a successful fingerprint capture
//                 //@Override
//                 public void captureOK(final byte[] fpImage, final byte[] veinImage) 
//                 {

//                     Runnable runnable = (new Runnable() 
//                     {

//                         @Override
//                         public void run() 
//                         {
//                             if(null != fpImage)
//                             {
//                                 // Bitmap bitmapFp = ToolUtils.renderCroppedGreyScaleBitmap(fpImage, fingerVeinSensor.getFpImgWidth(), fingerVeinSensor.getFpImgHeight());
//                                 // imageView.setImageBitmap(bitmapFp);
//                                 callbackContext.success(fpImage.toString());
//                             }
//                             if (null != veinImage)
//                             {
//                                 // Bitmap bitmapVein = ToolUtils.renderCroppedGreyScaleBitmap(veinImage, fingerVeinSensor.getVeinImgWidth(), fingerVeinSensor.getVeinImgHeight());
//                                 // imageView2.setImageBitmap(bitmapVein);
//                                 callbackContext.success(veinImage.toString());
//                             }
//                         }
//                     });
//                     cordova.getActivity().runOnUiThread(runnable);
                    
//                 }

//                 //@ Handle an unsuccessful fingerprint capture
//                 @Override
//                 public void captureError(FingerprintException e) {
//                     final FingerprintException exp = e;
//                     Runnable runnable = (new Runnable() {
//                         @Override
//                         public void run() {
//                             // LogHelper.d("captureError  errno=" + exp.getErrorCode() +
//                             //         ",Internal error code: " + exp.getInternalErrorCode() + ",message=" + exp.getMessage());
//                             callbackContext.error("captureError  errno=" + exp.getErrorCode() +
//                             ",Internal error code: " + exp.getInternalErrorCode() + ",message=" + exp.getMessage());
//                         }
//                     });
//                     cordova.getActivity().runOnUiThread(runnable);
//                 }

//                 //@ Handle a finger vein extraction error
//                 @Override
//                 public void extractError(final int err)
//                 {
//                     Runnable runnable = (new Runnable() {
//                         @Override
//                         public void run() {
//                             // textView.setText("extract fail, errorcode:" + err);
//                             callbackContext.error("extract fail, errorcode:" + err);
//                         }
//                     });
//                     cordova.getActivity().runOnUiThread(runnable);
//                 }

//                 //@ Handle a successful palm vein extraction
//                 //@Override
//                 public void extractOK(final byte[] fpTemplate, final String fvTemplate)
//                 {
//                     Runnable runnable = (new Runnable() {
//                         @Override
//                         public void run() {
//                             if (bIsRegister)
//                             {

//                                 regFVTemplates[enrollIndex] = fvTemplate;

//                                 System.arraycopy(fpTemplate, 0, regFPTemparray[enrollIndex], 0, fpTemplate.length);
//                                 if (enrollIndex > 1)
//                                 {

//                                     if (FingerVeinService.matchFinger(regFPTemparray[enrollIndex-1], regFPTemparray[enrollIndex]) <= 0 ||
//                                             FingerVeinService.matchFingerVein(regFVTemplates[enrollIndex-1], regFVTemplates[enrollIndex]) <= 0)
//                                     {
//                                         enrollIndex = 0;                                        
//                                         // textView.setText("Please press the same finger while registering");
//                                         // return;
//                                         // callbackContext.

//                                         alert(
//                                             "Please press the same finger while registering",
//                                             "Finger Capture Error", 
//                                             "OK", 
//                                             callbackContext
//                                         );
                                        
//                                     }
//                                 }
//                                 enrollIndex++;
//                                 if (enrollIndex == enrollCount)
//                                 {
//                                     byte[] regFPTemp = new byte[2048];
//                                     int ret = 0;
//                                        if (0 < (ret = FingerprintService.merge(regFPTemparray[0], regFPTemparray[1], regFPTemparray[2], regFPTemp)))
//                                     {
//                                         String strID = "test"+regID++;
//                                         if (0 == (ret = FingerVeinService.addRegTemplate(strID, regFPTemp, regFVTemplates)))
//                                         {
//                                             // textView.setText("enroll succ");
//                                             callbackContext.success(regFPTemp.toString());
//                                         }
//                                         else
//                                         {
//                                             callbackContext.error("enroll failed, addRegTemplate ret=" + ret);
//                                             // textView.setText("enroll failed, addRegTemplate ret=" + ret);
//                                         }
//                                     }
//                                     else
//                                     {
//                                         // textView.setText("enroll failed, merge ret=" + ret);
//                                         callbackContext.error("enroll failed, merge ret=" + ret);
//                                     }
//                                     bIsRegister = false;
//                                 }
//                                 else
//                                 {
//                                     // textView.setText("Please press your finger(" + (enrollCount-enrollIndex) + ").");
//                                     alert(
//                                         "Please press your finger(" + (enrollCount-enrollIndex) + ").", 
//                                         "Notice", 
//                                         "OK", 
//                                         callbackContext
//                                     );
//                                 }
//                             }
//                             else
//                             {
//                                 //writeTemplateToFile("/storage/emulated/0/zkfv.txt", fvTemplate);
//                                 byte[] idsfp = new byte[1024];
//                                 String strLog = "";
//                                 if (FingerVeinService.identifyFinger(fpTemplate, idsfp, 1) >0 ){
//                                     String strRes[] = new String(idsfp).split("\t");
//                                     strLog += "Identify Fingerprint Succ, id=" + strRes[0] + ",score=" + strRes[1];
//                                 }
//                                 else
//                                 {
//                                     strLog += "Identify Fingerprint fail";
//                                 }
//                                 byte[] idsfv = new byte[1024];
//                                 int ret = 0;
//                                 if ((ret = FingerVeinService.identifyFingerVein(fvTemplate, idsfv, 1)) >= 0)
//                                 {
//                                     String strRes[] = new String(idsfv).split("\t");
//                                     strLog += "\nIdentify Fingervein Succ, id=" + strRes[0] + ",score=" + strRes[1];
//                                 }
//                                 else
//                                 {
//                                     strLog += "\n Identify Fingervein Fail";
//                                 }
//                                 // textView.setText(strLog);
//                                 callbackContext.success(strLog);
//                             }
//                             //textView.setText("提取模板成功");
//                         }
//                     });
//                     cordova.getActivity().runOnUiThread(runnable);
//                 }
            
//             };

//             fingerVeinSensor.setFingerVeinCaptureListener(0, listener);

//             fingerVeinSensor.startCapture(0);

//             bstart = true;

//             alert(
//                 "Now Capturing biometrics", 
//                 "NOTICE", 
//                 "Continue", 
//                 callbackContext
//             );
//             // textView.setText("start capture succ");

//         }
//         catch (FingerprintException e)
//         {
// //            textView.setText("begin capture fail.errorcode:"+ e.getErrorCode() + "err message:" + e.getMessage() + "inner code:" + e.getInternalErrorCode());
//             cbContext.error("begin capture fail.errorcode:"+ e.getErrorCode() + "err message:" + e.getMessage() + "inner code:" + e.getInternalErrorCode());
//         }

//     }



    /**
     * Builds and shows a native Android alert with given Strings
     * @param message           The message the alert should display
     * @param title             The title of the alert
     * @param buttonLabel       The label of the button
     * @param callbackContext   The callback context
     */
    public synchronized void alert(final String message, final String title, final String buttonLabel, final CallbackContext callbackContext) {
        
//        final CordovaInterface cordova = this.cordova;

        Runnable runnable = new Runnable() 
        {
            public void run() {

                Builder dlg = new AlertDialog.Builder(cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT); //createDialog(cordova); // new AlertDialog.Builder(cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

                dlg.setMessage(message);

                dlg.setTitle(title);

                dlg.setCancelable(true);

                dlg.setPositiveButton(buttonLabel,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                // callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 0));
                            }
                        });
                dlg.setOnCancelListener(new AlertDialog.OnCancelListener() {
                    public void onCancel(DialogInterface dialog)
                    {
                        dialog.dismiss();
                        // callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 0));
                    }
                });

//                changeTextDirection(dlg);
            };
        };
        cordova.getActivity().runOnUiThread(runnable);
    }



}
