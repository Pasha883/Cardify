package com.example.cardify;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SavedCardsFragment extends Fragment {

    private RecyclerView recyclerView;
    private VizitkaAdapter adapter;
    private List<Vizitka> vizitkaList;

    private DatabaseReference vizitRef;

    //ИЗМЕНИТЬ АЛГОРИТМ ПРИЁМА ВИЗИТОК

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_cards, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_saved);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        vizitkaList = new ArrayList<>();
        adapter = new VizitkaAdapter(vizitkaList, getContext());
        recyclerView.setAdapter(adapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        vizitRef = database.getReference("vizitcards");

        loadVizitki();

        return view;
    }

    private void loadVizitki() {
        String userId = "userID001";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("savedVizitcards");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vizitkaList.clear();
                for (DataSnapshot cardSnap : snapshot.getChildren()) {
                    String cardId = cardSnap.getKey();
                    FirebaseDatabase.getInstance().getReference("vizitcards")
                            .child(cardId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot cardSnapshot) {
                                    Vizitka card = cardSnapshot.getValue(Vizitka.class);
                                    if (card != null) {
                                        vizitkaList.add(card);
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
