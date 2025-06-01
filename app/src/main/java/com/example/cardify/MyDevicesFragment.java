package com.example.cardify;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MyDevicesFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private MyDevicesAdapter adapter;
    private TextView tvMyDevicesTitle;
    private List<BluetoothDeviceModel> deviceList = new ArrayList<>();

    public MyDevicesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_devices, container, false);
        recyclerView = view.findViewById(R.id.recycler_my_devices);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        tvMyDevicesTitle = view.findViewById(R.id.tv_my_devices_title);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyDevicesAdapter(deviceList, getContext());
        recyclerView.setAdapter(adapter);

        BluetoothDeviceHandler.loadData(requireContext());
        loadSavedDevices();

        return view;
    }

    private void loadSavedDevices() {
        Set<String> savedDevices = BluetoothDeviceHandler.getConnectedDevices();

        if (savedDevices == null || savedDevices.isEmpty()) {
            updateView();
            return;
        }

        for (String deviceAddr : savedDevices) {
            BluetoothDeviceModel device = BluetoothDeviceHandler.getDevice(deviceAddr);

            if (device != null){
                if(BluetoothDeviceHandler.isCurrentlyConnected(device.getMacAddress())){
                    device.setConnected(true);
                } else {
                    device.setConnected(false);
                }
                BluetoothDeviceHandler.addOrUpdateDevice(requireContext(), device.getMacAddress(), device);
            }

            //boolean isConnected = BluetoothDeviceHandler.isAlreadyConnected(deviceName);

            // Здесь можно расширить данными, если где-то хранишь память и заряд
            deviceList.add(device);

            Log.d("MyDevicesFragment", "Device Name: " + device.getDeviceName());
        }

        updateView();
    }

    private void updateView() {
        if (deviceList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            tvMyDevicesTitle.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    public void updateDeviceList(){
        deviceList.clear();
        loadSavedDevices();
    }
}
