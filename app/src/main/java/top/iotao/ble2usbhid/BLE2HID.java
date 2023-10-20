package top.iotao.ble2usbhid;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;


public  class BLE2HID implements Runnable{
    private final static int ST_INIT = 0;
    private final static int ST_SCANNING = 1;
    private final static int ST_NOTFOUND=2;
    private final static int ST_FOUNDDEVICE = 3;
    private final static int ST_CONNECTING = 4;
    private final static int ST_CONNECTED = 5;

    private final static String  BLEHID_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private final static String BLEHID_CHAR_UUID = "0000ffe3-0000-1000-8000-00805f9b34fb";


    private BleDevice bleHidDev =null;

    private BluetoothGattCharacteristic hidChar;

    private Context context;


    private   IBLEHIDHandler mBLEHIDHandler=null;

    private int bleSta = ST_INIT;

    private int reConnectTimes=0;


    Queue<String> queue = new ConcurrentLinkedQueue<String>();


    public BLE2HID(Context c,IBLEHIDHandler handler)
    {
        this.context = c;
        this.mBLEHIDHandler  = handler;
    }

    //将数据放入队列
    public  void sendDataQueue(String data)
    {

        if(data==null || data.length() ==0)return;


        queue.offer(data);


    }



    private void startScan() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                ///  .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
                // .setDeviceName(true, "ble2usbhid")         // 只扫描指定广播名的设备，可选
                //  .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                // .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);

        bleSta = ST_SCANNING;
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                //由于蓝牙没有打开，上一次扫描没有结束等原因，会造成扫描开启失败


            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                //扫描过程中所有被扫描到的结果回调。由于扫描及过滤的过程是在工作线程中的，此方法也处于工作线程中 同一个设备会在不同的时间，携带自身不同的状态（比如信号强度等），出现在这个回调方法中，出现次数取决于周围的设备量及外围设备的广播间隔。


            }

            @Override
            public void onScanning(BleDevice bleDevice) {

                //扫描过程中的所有过滤后的结果回调。与onLeScan区别之处在于：它会回到主线程；同一个设备只会出现一次
                if(bleDevice.getName()==null)return;

                if(bleDevice.getName().contains("ble2usbhid") || bleDevice.getName().contains("BLE2USBHID") || bleDevice.getName().contains("BLEHID")) //可以根据蓝牙名称或MAC判断是否为目标设备
                {


                    bleHidDev = bleDevice;
                    bleSta =ST_FOUNDDEVICE;
                    BleManager.getInstance().cancelScan();
                    mBLEHIDHandler.onMessage("发现目标设备"+bleDevice.getName()+" RSSI:"+bleDevice.getRssi() +" mac:"+bleDevice.getMac());


                }else
                {
                    mBLEHIDHandler.onMessage("找到设备"+bleDevice.getName()+" "+bleDevice.getMac());
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {

                if(bleSta ==ST_SCANNING) {
                    bleSta = ST_NOTFOUND;
                    mBLEHIDHandler.onMessage("搜索结束未找到目标设备，稍后重新查找");

                }
            }
        });
    }

    private  void connectBleHid()
    {
        if(this.bleHidDev==null)return;


        bleSta = ST_CONNECTING;
        BleManager.getInstance().connect(this.bleHidDev, new BleGattCallback() {
            @Override
            public void onStartConnect() {

                mBLEHIDHandler.onMessage("连接...");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException e) {
                //  bleHidDev =null; //要尝试重连
                mBLEHIDHandler.onMessage("连接失败"+reConnectTimes);

                if(++reConnectTimes>5){
                    bleSta = ST_INIT;

                }

                bleSta = ST_FOUNDDEVICE;

            }


            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {


                bleHidDev = bleDevice;
                bleSta = ST_CONNECTED;
                mBLEHIDHandler.onMessage("连接成功");

                //BleManager.getInstance().setSplitWriteNum(18); //每次发送最大长度

                List<BluetoothGattService> serviceList = gatt.getServices();
                for (BluetoothGattService service : serviceList) {
                    UUID uuid_service = service.getUuid();
                    if(!uuid_service.toString().equals(BLEHID_SERVICE_UUID))
                    {
                        continue;
                    }

                    // mBLEHIDHandler.onMessage("service:"+uuid_service.toString());

                    List<BluetoothGattCharacteristic> characteristicList= service.getCharacteristics();
                    for(BluetoothGattCharacteristic characteristic : characteristicList) {
                        UUID uuid_chara = characteristic.getUuid();

                        if(uuid_chara.toString().equals(BLEHID_CHAR_UUID))
                        {
                            hidChar = characteristic;
                            break;
                        }
                        //mBLEHIDHandler.onMessage("  char:"+uuid_chara.toString());
                    }
                }




            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                bleHidDev =null;
                bleSta = ST_INIT;
                mBLEHIDHandler.onMessage("连接断开");

                //断开和重连之间最好间隔一段时间，否则可能会出现长时间连接不上的情况
            }
        });





    }




    private  int sendTask() throws InterruptedException {

        if(this.bleHidDev ==null)return 1;

        if(queue.size()==0)
        {

            Thread.sleep(50);

            return 0;
        }

        String str=queue.poll();

        byte[] data;

        if(str.toUpperCase().startsWith("2C")) //二进制指令转成BYTE数组
        {
            data =  CconverUtils.hexStr2Bytes(str);
        }else
        {
            data =  str.getBytes();
        }



        final Object object = new Object();


        BleManager.getInstance().write(this.bleHidDev, BLEHID_SERVICE_UUID, BLEHID_CHAR_UUID, data, new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                String str =new String(justWrite);
                mBLEHIDHandler.onMessage("发送成功:" + str);

                synchronized (object){
                    object.notifyAll();
                }


            }

            @Override
            public void onWriteFailure(BleException exception) {
                // 发送数据到设备失败
                mBLEHIDHandler.onMessage("发送失败:" + exception.getDescription());

                synchronized (object){
                    object.notifyAll();
                }


            }
        });

        synchronized (object){
            object.wait(500);
        }



        return 1;


    }



    @Override
    public void run() {

        long lastprint=0;
        while (true) {

            switch (bleSta)
            {
                case ST_INIT:
                    mBLEHIDHandler.onMessage("ST_INIT" );
                    reConnectTimes=0;
                    BleManager.getInstance().disconnectAllDevice(); //断开所有连接
                    startScan();
                    break;
                case ST_SCANNING:
                    if(System.currentTimeMillis()-lastprint>1000) {
                        mBLEHIDHandler.onMessage("ST_SCANNING");
                        lastprint = System.currentTimeMillis();
                    }


                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case ST_NOTFOUND:
                    mBLEHIDHandler.onMessage("ST_NOTFOUND" );
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    bleSta = ST_INIT;
                    break;
                case ST_FOUNDDEVICE:
                    mBLEHIDHandler.onMessage("ST_FOUNDDEVICE" );
                    connectBleHid();
                    break;
                case ST_CONNECTING:
                    if(System.currentTimeMillis()-lastprint>1000) {
                        mBLEHIDHandler.onMessage("ST_CONNECTING");
                        lastprint = System.currentTimeMillis();
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case ST_CONNECTED:

                    if(System.currentTimeMillis()-lastprint>2000) {
                        mBLEHIDHandler.onMessage("ST_CONNECTED");
                        lastprint = System.currentTimeMillis();
                    }

                    try {
                        sendTask() ;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    break;

                default:
                    bleSta = ST_INIT;
                    break;
            }
        }
    }




};