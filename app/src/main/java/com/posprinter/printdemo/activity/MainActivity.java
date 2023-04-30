package com.posprinter.printdemo.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;


import com.google.android.material.snackbar.Snackbar;
import com.posprinter.printdemo.R;
import com.posprinter.printdemo.utils.Conts;
import com.posprinter.printdemo.utils.DeviceReceiver;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.DataForSendToPrinterPos80;
import net.posprinter.utils.PosPrinterDev;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static String DISCONNECT="com.posconsend.net.disconnetct";

    //IMyBinder interface，All methods that can be invoked to connect and send data are encapsulated within this interface
    public static IMyBinder binder;

    //bindService connection
    ServiceConnection conn= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //Bind successfully
            binder= (IMyBinder) iBinder;
            Log.e("binder","connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("disbinder","disconnected");
        }
    };

    public static boolean ISCONNECT;
    Button BTCon,//connection button
            BTDisconnect,//disconnect button
            BTpos,
            BT76,
            BTtsc,
            BtposPrinter,
            BtSb;// start posprint button
    Spinner conPort;//spinner connetion port
    EditText showET;// show edittext
   // CoordinatorLayout container;

    private View dialogView;
    BluetoothAdapter bluetoothAdapter;

    private ArrayAdapter<String> adapter1
            ,adapter2
            ,adapter3;//usb adapter
    private ListView lv1,lv2,lv_usb;
    private ArrayList<String> deviceList_bonded=new ArrayList<String>();//bonded list
    private ArrayList<String> deviceList_found=new ArrayList<String>();//found list
    private Button btn_scan; //scan button
    private LinearLayout LLlayout;
    AlertDialog dialog;
    String mac;
    int pos ;

    private DeviceReceiver myDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        //bind service，get ImyBinder object
        Intent intent=new Intent(this,PosprinterService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
        //init view
        initView();

        //setlistener
        setlistener();
    }

    private void initView(){

        BTCon= (Button) findViewById(R.id.buttonConnect);
        BTDisconnect= (Button) findViewById(R.id.buttonDisconnect);

        BTpos= (Button) findViewById(R.id.buttonpos);
        BT76= (Button) findViewById(R.id.button76);
        BTtsc= (Button) findViewById(R.id.buttonTsc);

        BtposPrinter= (Button) findViewById(R.id.buttonPosPrinter);

        BtSb= (Button) findViewById(R.id.buttonSB);
        conPort= (Spinner) findViewById(R.id.connectport);
        showET= (EditText) findViewById(R.id.showET);
       // container= (CoordinatorLayout) findViewById(R.id.container);
    }




    private void setlistener(){
        BTCon.setOnClickListener(this);
        BTDisconnect.setOnClickListener(this);

        BTpos.setOnClickListener(this);
        BT76.setOnClickListener(this);
        BTtsc.setOnClickListener(this);

        BtSb.setOnClickListener(this);
        conPort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pos=i;
                switch (i){
                    case 0:
                        //wifi connect
                        showET.setText("");
                        showET.setEnabled(true);
                        BtSb.setVisibility(View.GONE);
                        showET.setHint(getString(R.string.hint));
                        break;
                    case 1:
                        //bluetooth connect
                        showET.setText("");
                        BtSb.setVisibility(View.VISIBLE);
                        showET.setHint(getString(R.string.bleselect));
                        showET.setEnabled(false);
                        break;
                    case 2:
                        //usb connect
                        showET.setText("");
                        BtSb.setVisibility(View.VISIBLE);
                        showET.setHint(getString(R.string.usbselect));
                        showET.setEnabled(false);
                        break;
                    default:break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public void onClick(View view) {

        int id=view.getId();
        //connect button
        if (id== R.id.buttonConnect){
            switch (pos){
                case 0:
                    // net connection
                    connetNet();
                    break;
                case 1:
                    //bluetooth connection
                    connetBle();
                    break;
                case 2:
                    //USB connection
                    connetUSB();
                    break;
            }
        }
        //device button
        if (id== R.id.buttonSB){
            switch (pos){
                case 0:
                    BTCon.setText(getString(R.string.connect));
                    break;
                case 1:
                    setBluetooth();
                    BTCon.setText(getString(R.string.connect));
                    break;
                case 2:
                    setUSB();
                    BTCon.setText(getString(R.string.connect));
                    break;
            }

        }
        //disconnect
        if (id== R.id.buttonDisconnect){
            if (ISCONNECT){
                binder.disconnectCurrentPort(new UiExecute() {
                    @Override
                    public void onsucess() {
                      //  showSnackbar(getString(R.string.toast_discon_success));
                        msg(getString(R.string.toast_discon_success) );
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_discon_success), Toast.LENGTH_LONG).show();
                        showET.setText("");
                        BTCon.setText(getString(R.string.connect));
                    }

                    @Override
                    public void onfailed() {
                      //  showSnackbar(getString(R.string.toast_discon_faile));
                        msg(getString(R.string.toast_discon_faile) );

                    }
                });
            }else {
               // showSnackbar(getString(R.string.toast_present_con));
                msg(getString(R.string.toast_present_con) );
            }
        }
        //start to pos printer
        if (id== R.id.buttonpos){
            if (ISCONNECT){
                Intent intent=new Intent(this,PosActivity.class);
                intent.putExtra("isconnect",ISCONNECT);
                startActivity(intent);
            }else {
                //showSnackbar(getString(R.string.connect_first));
                msg(getString(R.string.connect_first) );
            }

        }
        //start to 76 printer
        if (id== R.id.button76){
            if (ISCONNECT){
                Intent intent=new Intent(this,Z76Activity.class);
                intent.putExtra("isconnect",ISCONNECT);
                startActivity(intent);
            }else {
               // showSnackbar(getString(R.string.connect_first));
                msg(getString(R.string.connect_first) );
            }
        }
        //start to barcode(TSC) printer
        if (id== R.id.buttonTsc){
            if (ISCONNECT){
                Intent intent=new Intent(this,TscActivity.class);
                intent.putExtra("isconnect",ISCONNECT);
                startActivity(intent);
            }else {
               // showSnackbar(getString(R.string.connect_first));
                msg(getString(R.string.connect_first) );
            }
        }


    }

public void msg(String msg ){
    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
}


    /*
    net connection
     */
    private void connetNet(){

        String ipAddress=showET.getText().toString();
        if (ipAddress.equals(null)||ipAddress.equals("")){

          //  showSnackbar(getString(R.string.none_ipaddress));
            msg(getString(R.string.none_ipaddress) );
        }else {
            //ipAddress :ip address; portal:9100
            binder.connectNetPort(ipAddress,9100, new UiExecute() {
                @Override
                public void onsucess() {

                    ISCONNECT=true;

                    Toast.makeText(getApplicationContext(), getString(R.string.con_success), Toast.LENGTH_LONG).show();
                   // showSnackbar(getString(R.string.con_success));
                    msg(getString(R.string.con_success) );
                    //in this ,you could call acceptdatafromprinter(),when disconnect ,will execute onfailed();
                    binder.acceptdatafromprinter(new UiExecute() {
                        @Override
                        public void onsucess() {

                        }

                        @Override
                        public void onfailed() {
                            ISCONNECT=false;
                          //  showSnackbar(getString(R.string.con_failed));
                            msg(getString(R.string.con_failed) );
                            Intent intent=new Intent();
                            intent.setAction(DISCONNECT);
                            sendBroadcast(intent);

                        }
                    });
                }

                @Override
                public void onfailed() {
                    //Execution of the connection in the UI thread after the failure of the connection
                    ISCONNECT=false;
                  //  showSnackbar(getString(R.string.con_failed));
                    msg(getString(R.string.con_failed) );
                   BTCon.setText(getString(R.string.con_failed));


                }
            });

        }

    }

    /*
   USB connection
    */
    String usbAdrresss;
    private void connetUSB() {
        usbAdrresss=showET.getText().toString();
        if (usbAdrresss.equals(null)||usbAdrresss.equals("")){
           // showSnackbar(getString(R.string.usbselect));
            msg(getString(R.string.usbselect) );
        }else {
            binder.connectUsbPort(getApplicationContext(), usbAdrresss, new UiExecute() {
                @Override
                public void onsucess() {
                    ISCONNECT=true;
                  //  showSnackbar(getString(R.string.con_success));
                    msg(getString(R.string.con_success) );
                    BTCon.setText(getString(R.string.con_success));
                    setPortType(PosPrinterDev.PortType.USB);
                }

                @Override
                public void onfailed() {
                    ISCONNECT=false;
                  //  showSnackbar(getString(R.string.con_failed));
                    msg(getString(R.string.con_failed) );
                    BTCon.setText(getString(R.string.con_failed));


                }
            });
        }
    }
    /*
    bluetooth connecttion
     */
    private void connetBle(){
        String bleAdrress=showET.getText().toString();
        if (bleAdrress.equals(null)||bleAdrress.equals("")){
           // showSnackbar(getString(R.string.bleselect));
            msg(getString(R.string.bleselect) );
        }else {
            binder.connectBtPort(bleAdrress, new UiExecute() {
                @Override
                public void onsucess() {
                    ISCONNECT=true;
                  //  showSnackbar(getString(R.string.con_success));
                    msg(getString(R.string.con_success) );
                    BTCon.setText(getString(R.string.con_success));

                    binder.write(DataForSendToPrinterPos80.openOrCloseAutoReturnPrintState(0x1f), new UiExecute() {
                        @Override
                        public void onsucess() {
                                binder.acceptdatafromprinter(new UiExecute() {
                                    @Override
                                    public void onsucess() {

                                    }

                                    @Override
                                    public void onfailed() {
                                        ISCONNECT=false;
                                      //  showSnackbar(getString(R.string.con_has_discon));
                                        msg(getString(R.string.con_has_discon) );
                                    }
                                });
                        }

                        @Override
                        public void onfailed() {

                        }
                    });


                }

                @Override
                public void onfailed() {

                    ISCONNECT=false;
                  //  showSnackbar(getString(R.string.con_failed));
                    msg(getString(R.string.con_failed) );
                }
            });
        }


    }

    /*
     select bluetooth device
     */

    @SuppressLint("MissingPermission")
    public void setBluetooth(){
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()){
            //open bluetooth
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, Conts.ENABLE_BLUETOOTH);
        }else {

            showblueboothlist();

        }
    }

    @SuppressLint("MissingPermission")
    private void showblueboothlist() {
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
        LayoutInflater inflater=LayoutInflater.from(this);
        dialogView=inflater.inflate(R.layout.printer_list, null);
        adapter1=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceList_bonded);
        lv1=(ListView) dialogView.findViewById(R.id.listView1);
        btn_scan=(Button) dialogView.findViewById(R.id.btn_scan);
        LLlayout=(LinearLayout) dialogView.findViewById(R.id.ll1);
        lv2=(ListView) dialogView.findViewById(R.id.listView2);
        adapter2=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceList_found);
        lv1.setAdapter(adapter1);
        lv2.setAdapter(adapter2);
        dialog=new AlertDialog.Builder(this).setTitle("BLE").setView(dialogView).create();
        dialog.show();

        myDevice=new DeviceReceiver(deviceList_found,adapter2,lv2);

        //register the receiver
        IntentFilter filterStart=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filterEnd=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(myDevice, filterStart);
        registerReceiver(myDevice, filterEnd);

        setDlistener();
        findAvalibleDevice();
    }
    private void setDlistener() {
        // TODO Auto-generated method stub
        btn_scan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                LLlayout.setVisibility(View.VISIBLE);
                //btn_scan.setVisibility(View.GONE);
            }
        });
        //boned device connect
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                try {
                    if(bluetoothAdapter!=null&&bluetoothAdapter.isDiscovering()){
                        bluetoothAdapter.cancelDiscovery();

                    }

                    String msg=deviceList_bonded.get(arg2);
                    mac=msg.substring(msg.length()-17);
                    String name=msg.substring(0, msg.length()-18);
                    //lv1.setSelection(arg2);
                    dialog.cancel();
                    showET.setText(mac);
                    //Log.i("TAG", "mac="+mac);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        //found device and connect device
        lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                try {
                    if(bluetoothAdapter!=null&&bluetoothAdapter.isDiscovering()){
                        bluetoothAdapter.cancelDiscovery();

                    }
                    String msg=deviceList_found.get(arg2);
                    mac=msg.substring(msg.length()-17);
                    String name=msg.substring(0, msg.length()-18);
                    //lv2.setSelection(arg2);
                    dialog.cancel();
                    showET.setText(mac);
                    Log.i("TAG", "mac="+mac);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    /*
    find avaliable device
     */
    @SuppressLint("MissingPermission")
    private void findAvalibleDevice() {
        // TODO Auto-generated method stub

        Set<BluetoothDevice> device=bluetoothAdapter.getBondedDevices();

        deviceList_bonded.clear();
        if(bluetoothAdapter!=null&&bluetoothAdapter.isDiscovering()){
            adapter1.notifyDataSetChanged();
        }
        if(device.size()>0){
            //already
            for(Iterator<BluetoothDevice> it = device.iterator(); it.hasNext();){
                BluetoothDevice btd=it.next();
                deviceList_bonded.add(btd.getName()+'\n'+btd.getAddress());
                adapter1.notifyDataSetChanged();
            }
        }else{
            deviceList_bonded.add("No can be matched to use bluetooth");
            adapter1.notifyDataSetChanged();
        }

    }

    View dialogView3;
    private TextView tv_usb;
    private List<String> usbList,usblist;

   /*
   uSB connection
    */
    private void setUSB(){
        LayoutInflater inflater=LayoutInflater.from(this);
        dialogView3=inflater.inflate(R.layout.usb_link,null);
        tv_usb= (TextView) dialogView3.findViewById(R.id.textView1);
        lv_usb= (ListView) dialogView3.findViewById(R.id.listView1);


        usbList= PosPrinterDev.GetUsbPathNames(this);
        if (usbList==null){
            usbList=new ArrayList<>();
        }
        usblist=usbList;
        tv_usb.setText(getString(R.string.usb_pre_con)+usbList.size());
        adapter3=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,usbList);
        lv_usb.setAdapter(adapter3);


        AlertDialog dialog=new AlertDialog.Builder(this)
                .setView(dialogView3).create();
        dialog.show();

        setUsbLisener(dialog);

    }
    String usbDev="";
    public void setUsbLisener(final AlertDialog dialog) {

        lv_usb.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                usbDev=usbList.get(i);
                showET.setText(usbDev);
                dialog.cancel();
                Log.e("usbDev: ",usbDev);
            }
        });



    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        binder.disconnectCurrentPort(new UiExecute() {
            @Override
            public void onsucess() {

            }

            @Override
            public void onfailed() {

            }
        });
        unbindService(conn);
    }

    public static PosPrinterDev.PortType portType;//connect type
    private void setPortType(PosPrinterDev.PortType portType){
        this.portType=portType;

    }

}
