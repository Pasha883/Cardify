package com.example.cardify;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MyDevicesAdapter extends RecyclerView.Adapter<MyDevicesAdapter.DeviceViewHolder> {

    private List<BluetoothDeviceModel> devices;
    private Context context;

    public MyDevicesAdapter(List<BluetoothDeviceModel> devices, Context context) {
        this.devices = devices;
        this.context = context;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDeviceModel device = devices.get(position);
        holder.deviceName.setText(device.getDeviceName());

        holder.memoryUsage.setText(String.format("Занято %.2f Мб из %.2f Мб памяти", device.getUsedMemoryMb(), device.getTotalMemoryMb()));
        holder.dataSaved.setText(String.format("Сохранено %d визитки", device.getSavedCardsCount()));

        if (device.isConnected()) {
            holder.connectState.setText("Подключено");
            holder.iconState.setImageResource(R.drawable.ic_online); // иконка подключения

            int battery = device.getBatteryLevel();
            holder.batteryCharge.setText(battery + "%");
            holder.iconBattery.setImageResource(R.drawable.ic_battery_online);
        } else {
            holder.connectState.setText("Не подключено");
            holder.iconState.setImageResource(R.drawable.ic_offline); // иконка отключения

            holder.batteryCharge.setText("??");
            holder.iconBattery.setImageResource(R.drawable.ic_battery_none);
        }



        /*if (battery > 75) {
            holder.iconBattery.setImageResource(R.drawable.ic_battery_full);
        } else if (battery > 50) {
            holder.iconBattery.setImageResource(R.drawable.ic_battery_half);
        } else if (battery > 20) {
            holder.iconBattery.setImageResource(R.drawable.ic_battery_low);
        } else {
            holder.iconBattery.setImageResource(R.drawable.ic_battery_none);
        }*/
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName, memoryUsage, dataSaved, connectState, batteryCharge;
        ImageView iconMemory, iconSaved, iconState, iconBattery;
        MaterialButton btnMore;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.text_device_name);
            memoryUsage = itemView.findViewById(R.id.text_memory_usage);
            dataSaved = itemView.findViewById(R.id.text_data_saved);
            connectState = itemView.findViewById(R.id.text_connect_state);
            batteryCharge = itemView.findViewById(R.id.text_battery_charge);
            iconMemory = itemView.findViewById(R.id.icon_memory);
            iconSaved = itemView.findViewById(R.id.icon_saved);
            iconState = itemView.findViewById(R.id.icon_state);
            iconBattery = itemView.findViewById(R.id.icon_battery);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }
}

