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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private String userId = "";

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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        loadVizitki();

        return view;
    }

    private void loadVizitki() {
        DatabaseReference savedRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("savedVizitcards");

        savedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vizitkaList.clear();
                if (!snapshot.exists()) {
                    adapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot cardSnap : snapshot.getChildren()) {
                    String cardId = cardSnap.getKey();
                    if (cardId == null) continue;

                    vizitRef.child(cardId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot cardSnapshot) {
                            if (cardSnapshot.exists()) {
                                Vizitka card = cardSnapshot.getValue(Vizitka.class);
                                if (card != null) {
                                    vizitkaList.add(card);
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                // Если визитка не найдена в базе, удалить её из savedVizitcards
                                savedRef.child(cardId).removeValue()
                                        .addOnSuccessListener(unused -> {
                                            // Можно показать короткий тост об удалении (по желанию)
                                            // Toast.makeText(getContext(), "Удалена несуществующая визитка", Toast.LENGTH_SHORT).show();
                                        });
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
