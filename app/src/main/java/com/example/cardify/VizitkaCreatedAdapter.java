package com.example.cardify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.moreButton.setOnClickListener(v -> {
            // TODO: перейти на экран подробностей
            Toast.makeText(context, "Открыть " + card.companyName, Toast.LENGTH_SHORT).show();
        });
        holder.moreButton.setOnClickListener(v -> {
            FragmentActivity activity = (FragmentActivity) context;
            Fragment fragment = EditCardFragment.newInstance("param1", "param2");
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return vizitkaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView companyName, companySpec;
        Button moreButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            companyName = itemView.findViewById(R.id.text_company_name);
            companySpec = itemView.findViewById(R.id.text_company_spec);
            moreButton = itemView.findViewById(R.id.btn_more);
        }
    }
}

