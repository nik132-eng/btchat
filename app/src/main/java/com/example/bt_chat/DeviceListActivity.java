package com.example.bt_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private ListView ListPairDevices , ListAvailableDevices;
    private ArrayAdapter<String> AdapterPairDevices , AdapterAvailableDevice;
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private ProgressBar progressScanDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        context = this;
        init();
    }

    private void init(){
        ListPairDevices = findViewById(R.id.List_pair_device);
        ListAvailableDevices = findViewById(R.id.List_pair_device);

        progressScanDevices = findViewById(R.id.progress_scan_devices);

        AdapterAvailableDevice = new ArrayAdapter<String>(context,R.layout.device_list_item);
        AdapterPairDevices = new ArrayAdapter<String>(context,R.layout.device_list_item);

        ListPairDevices.setAdapter(AdapterPairDevices);
        ListAvailableDevices.setAdapter(AdapterAvailableDevice);

        ListAvailableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() -17);

                Intent intent = new Intent();
                intent.putExtra("deviceaddess",address);
                setResult(RESULT_OK,intent);
                finish();

            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

        if (pairedDevice != null && pairedDevice.size()>0){
            for (BluetoothDevice device : pairedDevice){
                AdapterPairDevices.add(device.getName() + "\n" + device.getAddress());
            }
        }

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(Bluetoothlistner,intentFilter);
        IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(Bluetoothlistner,intentFilter1);
    }

    private BroadcastReceiver Bluetoothlistner = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDING){
                    AdapterAvailableDevice.add(device.getName() + "\n" + device.getAddress());
                }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    progressScanDevices.setVisibility(View.GONE);
                    if (AdapterAvailableDevice.getCount()== 0){
                        Toast.makeText(context, "No new device Found", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "Click on the Device to start Chat ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_scan_devices:
                scandevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void scandevices(){
        progressScanDevices.setVisibility(View.VISIBLE);
        AdapterAvailableDevice.clear();
        Toast.makeText(context, "Scan stared", Toast.LENGTH_SHORT).show();

        if (bluetoothAdapter.isDiscovering() ){
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }
}