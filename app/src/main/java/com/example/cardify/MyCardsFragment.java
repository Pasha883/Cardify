package com.example.cardify;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyCardsFragment extends Fragment {
    private RecyclerView recyclerView;
    private Button btnCreateCard;
    private List<Vizitka> myCardsList = new ArrayList<>();
    private VizitkaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_cards, container, false);

        recyclerView = view.findViewById(R.id.recycler_my_cards);
        btnCreateCard = view.findViewById(R.id.btn_create_card);
        adapter = new VizitkaAdapter(myCardsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadMyCards();

        btnCreateCard.setOnClickListener(v -> {
            // Переключение на экран создания визитки
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddCardFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadMyCards() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("createdVizitcards");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myCardsList.clear();
                for (DataSnapshot cardSnap : snapshot.getChildren()) {
                    String cardId = cardSnap.getKey();
                    FirebaseDatabase.getInstance().getReference("vizitcards")
                            .child(cardId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot cardSnapshot) {
                                    Vizitka card = cardSnapshot.getValue(Vizitka.class);
                                    if (card != null) {
                                        myCardsList.add(card);
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
