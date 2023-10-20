package top.iotao.ble2usbhid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.clj.fastble.BleManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    BLE2HID ble2hid=null;


    private final IBLEHIDHandler   mBLEHIDHandler = new IBLEHIDHandler() {
        @Override
        public void onMessage(String msg) {

            Log.d(TAG, msg);
            final  String s = msg+"\n";
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if(edtmsg!=null)
                    {
                        edtmsg.append(s);
                    }
                }
            });


        }

    };

    private Button btntest,btntest2,btntest3,btntest4,btnbin ,btnmoveto;
    private EditText edtmsg ;




    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {

                // startScan();
                //开启蓝牙线程
                if(ble2hid==null) {
                    ble2hid = new BLE2HID(this, mBLEHIDHandler);
                    Thread thread = new Thread(ble2hid);
                    thread.start();
                }
            }
        }
    }





    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
        }
    }



    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }


    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "请打开蓝牙", Toast.LENGTH_LONG).show();
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {

                    // startScan();


                    //开启蓝牙线程
                    if(ble2hid==null) {
                        ble2hid = new BLE2HID(this, mBLEHIDHandler);
                        Thread thread = new Thread(ble2hid);
                        thread.start();
                    }


                }
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btntest=this.findViewById(R.id.btntest);
        btntest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ble2hid==null)
                {
                    edtmsg.append("权限未通过或硬件开关未开启");
                    return;
                }


                edtmsg.setText("");

                ble2hid.sendDataQueue("K:123456789");
                ble2hid.sendDataQueue("K:Enter");
                ble2hid.sendDataQueue("K:abcdefghijklmnopqrstuvwxyz");
                ble2hid.sendDataQueue("K:Enter");


            }
        });


        btntest2=this.findViewById(R.id.btntest2);
        btntest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ble2hid==null)
                {
                    edtmsg.append("权限未通过或硬件开关未开启");
                    return;
                }
                edtmsg.setText("多媒体静音测试\n");
                ble2hid.sendDataQueue("C:MUTE");

            }
        });


        btntest3=this.findViewById(R.id.btntest3);
        btntest3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ble2hid==null)
                {
                    edtmsg.append("权限未通过或硬件开关未开启");
                    return;
                }
                edtmsg.setText("鼠标右键测试\n");
                ble2hid.sendDataQueue("M:RCLICK");

            }
        });

        final EditText edt= (EditText)this.findViewById(R.id.edtstrcmd);

        btntest4=this.findViewById(R.id.btntest4);
        btntest4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ble2hid==null)
                {
                    edtmsg.append("权限未通过或硬件开关未开启\n");
                    return;
                }


                String cmd = edt.getText().toString();

                if(cmd.contains(":")) {
                    edtmsg.setText("字符串指令测试\n");
                    ble2hid.sendDataQueue(cmd);
                }

            }
        });




        final EditText edtbin= (EditText)this.findViewById(R.id.edtbin);
        btnbin=this.findViewById(R.id.btnbin);
        btnbin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ble2hid==null)
                {
                    edtmsg.append("权限未通过或硬件开关未开启");
                    return;
                }


                String cmd = edtbin.getText().toString();

                //指令文档及通信协议：https://note.youdao.com/s/AZhi77Gc  使用HID007脚本软件可辅助生成报文
                if(cmd.length()>=24) {
                    edtmsg.setText("二进制指令测试\n");
                    ble2hid.sendDataQueue(cmd);
                }

            }
        });




        final EditText edtx= (EditText)this.findViewById(R.id.edtx);
        final EditText edty= (EditText)this.findViewById(R.id.edty);

        btnmoveto=this.findViewById(R.id.btnmoveto);
        btnmoveto.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if(ble2hid==null)
                {
                    edtmsg.append("权限未通过或硬件开关未开启");
                    return;
                }


                String strx = edtx.getText().toString();
                String stry= edty.getText().toString();


                //移动到100,100： M:MOVETO,320,533
                //100/1280=x/4095  x=320
                //100/768=y/4095  y=533

                if(strx.length()>0 && stry.length()>0) {
                    //屏幕分辨率，根据自己电脑修改
                    int screenW=1280;
                    int screenH=768;

                    float x = Integer.parseInt(strx);
                    x=x/screenW*4095;

                    float y = Integer.parseInt(stry);
                    y=y/screenH*4095;


                    int nx= (int) x;
                    int ny= (int) y;

                    String cmd= String.format("M:MOVETO,%d,%d",nx,ny);

                    edtmsg.setText("绝对坐标测试"+strx+","+stry+"\n");
                    edtmsg.append(cmd);

                    ble2hid.sendDataQueue(cmd);
                }

            }
        });




        edtmsg = this.findViewById(R.id.edtmsg);
        edtmsg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                edtmsg.setText("长按已清空\n");
                return false;
            }
        });

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(5, 3000)
                .setOperateTimeout(3000);

        boolean isSupportBle = BleManager.getInstance().isSupportBle();

        boolean isEnable = BleManager.getInstance().isBlueEnable();

        if(!isEnable){
            edtmsg.setText("蓝牙未打开");
            BleManager.getInstance().enableBluetooth(); //异步 执行完并不会马上开启完成
        }


        checkPermissions();




    }


}