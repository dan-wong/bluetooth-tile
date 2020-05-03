package com.daniel.tileapp.tile;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.daniel.tileapp.R;

import java.util.List;

public class BluetoothTileRecyclerViewAdapter extends RecyclerView.Adapter<BluetoothTileRecyclerViewAdapter.BluetoothTileViewHolder> {
    private final List<BluetoothDevice> devices;
    private final BluetoothTileListener listener;

    public BluetoothTileRecyclerViewAdapter(List<BluetoothDevice> devices, BluetoothTileListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BluetoothTileViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.device_card, viewGroup, false);
        return new BluetoothTileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothTileViewHolder holder, int i) {
        final BluetoothDevice device = devices.get(i);
        holder.deviceNameTextView.setText(device.getName());
        holder.macTextView.setText(device.getAddress());
        holder.cardView.setOnClickListener(view -> listener.tileSelected(device));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public interface BluetoothTileListener {
        void tileSelected(BluetoothDevice device);
    }

    public class BluetoothTileViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView deviceNameTextView;
        public TextView macTextView;

        public BluetoothTileViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            deviceNameTextView = itemView.findViewById(R.id.deviceNameTextView);
            macTextView = itemView.findViewById(R.id.macTextView);
        }
    }
}
