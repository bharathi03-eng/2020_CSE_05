package pragma.embd.androidbasedassitanceseniorcitizens;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ConnectBluetoothScreenActivity extends Activity implements BluetoothSPPConnectionListener {
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    private boolean connected=false;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    protected static final int RESULT_SPEECH = 11;

    private BluetoothSPPConnection mBluetoothSPPConnection;
    private BluetoothAdapter mBluetoothAdapter = null;
    String readMessage="";

    TextToSpeech speak;

    TextView txtstatus, txtmsg, tv_temp, tv_smoke, tv_obstacle, tv_light;
    Button btn_connect;

   private static final int MY_PERMISSIONS_REQUEST_NETWORK_PROVIDER =1 ;

    DatabaseHelper helper;
    SQLiteDatabase database;
    Cursor cursor;

  //  private static final String fields[] = {"_ID", "rfid", "productname", "direction"};

    private CameraManager mCameraManager;
    private String mCameraId;

    String str_temp;
    String str_smoke;
    String str_obstacles;
    String str_torch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connectbluetoothscreen);

        txtstatus=(TextView)findViewById(R.id.txtstatus);
        txtmsg=(TextView)findViewById(R.id.bluetoothmsg);
        tv_temp=(TextView)findViewById(R.id.tv_temp);
        tv_smoke=(TextView)findViewById(R.id.tv_smoke);
        tv_obstacle=(TextView)findViewById(R.id.tv_obstacle);
        tv_light=(TextView)findViewById(R.id.tv_light);
        btn_connect=(Button)findViewById(R.id.btn_connect);
      //  img=(ImageView)findViewById(R.id.img);

        helper = new DatabaseHelper(this);
        requestForPermissions();



        boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);

        if (!isFlashAvailable) {
            showNoFlashError();
        }

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

       mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mBluetoothSPPConnection = new BluetoothSPPConnection(this,mHandler); // Registers the

        btn_connect.setOnClickListener(new  View.OnClickListener()
        {

            public void onClick(View v)
            {
                if (!connected)
                {
                    Intent serverIntent = new Intent(v.getContext(), DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                }
                else
                {
                    mBluetoothSPPConnection.close();
                }
            }
        });


    }

    public void showNoFlashError() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .create();
        alert.setTitle("Oops!");
        alert.setMessage("Flash not available in this device...");
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void switchFlashLight(boolean status) {
        try {
            mCameraManager.setTorchMode(mCameraId, status);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    void requestForPermissions(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getApplicationContext(), "in first if",
                    Toast.LENGTH_LONG).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

                Toast.makeText(getApplicationContext(), "in second if",
                        Toast.LENGTH_LONG).show();
            } else {

                // permission is already granted
                Toast.makeText(getApplicationContext(), "in else",
                        Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_NETWORK_PROVIDER);
            }


        } else {
           /* mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
                    0, mlocListener);*/

        }

    }


    public String actualdata="";
    private final Handler mHandler = new Handler() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {

                case MESSAGE_STATE_CHANGE:
                    break;

                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    // mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;

                case MESSAGE_READ:

                    byte[] readBuf = (byte[]) msg.obj;

                    readMessage = new String(readBuf, 0, msg.arg1);

                    if (readMessage.equals(" ")) {
                        readMessage = "";
                    }
                    if (readMessage.contains("@")) {
                        actualdata = actualdata + readMessage;
                        actualdata = actualdata.replace("@", "");
                        actualdata = actualdata.replace("#", "");

                        //T00S000O0L0@
                        //0123456789
                        txtmsg.setText("Message Received: " + actualdata);
                        if (actualdata.contains("T") && actualdata.contains("S") &&
                                actualdata.contains("O") && actualdata.contains("L")) {

                            str_temp = actualdata.substring(1, 3);
                            tv_temp.setText("Temperature is: " + str_temp +
                                    "degree celsius");
                            voiceOutput(tv_temp.getText().toString().trim());

                            str_smoke = actualdata.substring(4, 7);
                            tv_smoke.setText("Smoke is: " + str_smoke);
                            voiceOutput(tv_smoke.getText().toString().trim());


                            if(actualdata.substring(8, 9).equals("1")){
                                str_obstacles = "Obstacle Detected";
                                tv_obstacle.setText(str_obstacles);
                                voiceOutput(str_obstacles);
                            }
                            else if(actualdata.substring(8, 9).equals("0")){
                                str_obstacles = "No Obstacle Detected";
                                tv_obstacle.setText(str_obstacles);
                                voiceOutput(str_obstacles);
                            }

                            if(actualdata.substring(10).equals("1")){
                                str_torch = "Switch On Light";
                                tv_light.setText(str_torch);
                                switchFlashLight(true);
                                voiceOutput(str_torch);
                            }
                            else if(actualdata.substring(10).equals("0")){
                                str_torch = "No need to Switch On Light";
                                tv_light.setText(str_torch);
                                switchFlashLight(false);
                                voiceOutput(str_torch);
                            }

                            Time today = new Time(Time.getCurrentTimezone());
                            today.setToNow();
                            int int_month = today.month + 1;

                            String str_date = "" + today.monthDay + "/" + int_month + "/" + today.year;
                            String str_time = today.format("%k:%M:%S");
                            addDataToDatabase(str_date + " " + str_time, str_time);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "No correct Data received", Toast.LENGTH_LONG).show();
                        }

                        actualdata = "";
                        readMessage = "";

                    }
                    else {
                        actualdata = actualdata + readMessage;
                        readMessage = "";
                    }



                    break;
                case MESSAGE_DEVICE_NAME:
                    break;
                case MESSAGE_TOAST:

                    break;
            }
        }
    };

    void addDataToDatabase(String dates, String times){

        database = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        try{
            long i = 0;
            values.put("datetime", dates + " " + times);
            values.put("temperature", str_temp);
            values.put("smoke", str_smoke);
            values.put("obstacles", str_obstacles);
            values.put("torch", str_torch);
            i = database.insert(DatabaseHelper.DataDetails_info_TABLE_NAME, null, values);

            if(i>0){
                Toast.makeText(getApplicationContext(), "Data Inserted Successfully" , Toast.LENGTH_SHORT).show();
            }

        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Inserting Data failed: " + e.getMessage() , Toast.LENGTH_SHORT).show();

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //	Toast.makeText(getApplicationContext(),"requestCode" + RESULT_SPEECH, Toast.LENGTH_LONG).show();
        switch (requestCode)
        {

            case RESULT_SPEECH:
                //Toast.makeText(getApplicationContext(), "IntoSpeach Result", Toast.LENGTH_LONG).show();
                if (resultCode == RESULT_OK && null != data) {

                }
                break;
            case REQUEST_CONNECT_DEVICE:

                if (resultCode == Activity.RESULT_OK)
                {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mBluetoothSPPConnection.open(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                }
                break;
        }
    }

    public void bluetoothWrite(int bytes, byte[] buffer)
    {
        // TODO Auto-generated method stub
        byte[] command = new byte[9];
        command[0]='F';
        command[1]='A';
        mBluetoothSPPConnection.write(command);
    }



    public void onConnecting()
    {
        // TODO Auto-generated method stub
        Toast.makeText(getApplicationContext(), "connecting here", Toast.LENGTH_LONG).show();
        TextView connectionView = (TextView) findViewById(R.id.txtstatus);
        connectionView.setText("Connecting...");
    }


    public void onConnected() {
        // TODO Auto-generated method stub
        connected = true;

        // Change the text in the connectionInfo TextView

        txtstatus.setText("Connected to "+mBluetoothSPPConnection.getDeviceName());

        // Change the text in the connect button.
        Button bt = (Button) findViewById(R.id.btn_connect);
        bt.setText("Disconnect");

        // Send the 's' character so that the communication can start.
        byte[] command = new byte[1];
        command[0]='s';
        mBluetoothSPPConnection.write(command);
    }

    public void onConnectionFailed() {
        // TODO Auto-generated method stub
        connected = false;

        // Change the text in the connectionInfo TextView
        TextView connectionView = (TextView) findViewById(R.id.txtstatus);
        connectionView.setText("Connection failed!");

        // Change the text in the connect button.
        Button bt = (Button) findViewById(R.id.btn_connect);
        bt.setText("Connect");
    }

    public void onConnectionLost() {
        // TODO Auto-generated method stub
        connected = false;

        // Change the text in the connectionInfo TextView
        TextView connectionView = (TextView) findViewById(R.id.txtstatus);
        connectionView.setText("Not Connected!");

        // Change the text in the connect button.
        Button bt = (Button) findViewById(R.id.btn_connect);
        bt.setText("Connect");
    }

    public void bluetoothread(int bytes, byte[] buffer) {
        // TODO Auto-generated method stub
				/* byte[] readBuf = (byte[]) msg.obj;
				 String readMessage = new String(readBuf, 0, msg.arg1);
				 mBluetoothSPPConnection.read(command);*/

    }


    public void voiceOutput(final String text_speak){
        //Toast.makeText(getApplicationContext(),"In Method voice" , Toast.LENGTH_SHORT).show();
        speak = new TextToSpeech(this,
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            speak.setLanguage(Locale.US);
                        }
                        readMessage ="";

                        for(int k=0;k<=2;k++){
                            speak.speak(text_speak, TextToSpeech.QUEUE_ADD, null);
                        }


                    }
                });

    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
    }

}
