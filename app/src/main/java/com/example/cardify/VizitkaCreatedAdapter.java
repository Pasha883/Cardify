package com.example.cardify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.widget.PopupMenu;

import java.util.List;

public class VizitkaCreatedAdapter extends RecyclerView.Adapter<VizitkaCreatedAdapter.ViewHolder> {

    private List<VizitkaCreated> vizitkaList;
    private Context context;

    public VizitkaCreatedAdapter(List<VizitkaCreated> vizitkaList, Context context) {
        this.vizitkaList = vizitkaList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vizitka_edit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VizitkaCreated card = vizitkaList.get(position);
        holder.companyName.setText(card.companyName);
        holder.companySpec.setText(card.companySpec);
        holder.usersCount.setText(String.valueOf(card.users));

        holder.moreButton.setOnClickListener(v -> {
            FragmentActivity activity = (FragmentActivity) context;
            Fragment fragment = EditCardFragment.newInstance(card);
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        holder.btnShare.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.btnShare);
            // Применяем стиль
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu_edit, popupMenu.getMenu());

            popupMenu.setForceShowIcon(true);
            // Добавляем пункты меню
            popupMenu.getMenu().add(0, 0, 0, "Поделиться через QR").setIcon(R.drawable.ic_qr_scan);
            popupMenu.getMenu().add(0, 1, 1, "Поделиться через NFC").setIcon(R.drawable.ic_nfc);


            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getTitle().toString()) {
                    case "Поделиться через QR":
                        Toast.makeText(context, "Поделиться через QR", Toast.LENGTH_SHORT).show();
                        FragmentActivity activity = (FragmentActivity) context;
                        QRCodeDialogFragment dialog = QRCodeDialogFragment.newInstance(
                                card.id, card.companyName);
                        dialog.show(activity.getSupportFragmentManager(), "qr_dialog");
                        return true;
                    case "Поделиться через NFC":
                        Toast.makeText(context, "Поделиться через NFC", Toast.LENGTH_SHORT).show();
                        FragmentActivity activity2 = (FragmentActivity) context;
                        NfcSenderDialogFragment dialog2 = NfcSenderDialogFragment.newInstance(card.id);
                        dialog2.show(activity2.getSupportFragmentManager(), "nfc_sender_dialog");

                        // TODO: Реализация передачи через NFC
                        return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return vizitkaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView companyName, companySpec, usersCount;
        ImageView iconUsers;
        Button moreButton;
        ImageButton btnShare;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            companyName = itemView.findViewById(R.id.text_company_name);
            companySpec = itemView.findViewById(R.id.text_company_spec);
            usersCount = itemView.findViewById(R.id.text_users_count);
            iconUsers = itemView.findViewById(R.id.icon_users);
            moreButton = itemView.findViewById(R.id.btn_more);
            btnShare = itemView.findViewById(R.id.btn_share);
        }
    }
}
