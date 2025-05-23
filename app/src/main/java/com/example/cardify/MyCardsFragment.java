package com.example.cardify;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyCardsFragment extends Fragment {
    private RecyclerView recyclerView;
    private Button btnCreateCard;
    private List<VizitkaCreated> myCardsList = new ArrayList<>();
    private VizitkaCreatedAdapter adapter;
    private LinearLayout emptyStateLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_cards, container, false);


        recyclerView = view.findViewById(R.id.recycler_my_cards);
        btnCreateCard = view.findViewById(R.id.btn_create_card);
        adapter = new VizitkaCreatedAdapter(myCardsList, getContext());
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadMyCards();

        btnCreateCard.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddCardFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadMyCards() {
        final boolean[] isEmpty = {true};
        String userId = "";
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("createdVizitcards");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myCardsList.clear();
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    adapter.notifyDataSetChanged();
                    showEmptyState(true);
                    return;
                }

                for (DataSnapshot cardSnap : snapshot.getChildren()) {
                    String cardId = cardSnap.getKey();
                    FirebaseDatabase.getInstance().getReference("vizitcards")
                            .child(cardId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot cardSnapshot) {
                                    VizitkaCreated card = cardSnapshot.getValue(VizitkaCreated.class);
                                    if (card != null) {
                                        // Попробовать получить поле users
                                        Long usersCount = cardSnapshot.child("users").getValue(Long.class);
                                        if (usersCount != null) {
                                            card.users = usersCount.intValue();
                                        } else {
                                            card.users = 0;
                                        }
                                        myCardsList.add(card);
                                        isEmpty[0] = false;
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

    private void showEmptyState(boolean isEmpty) {
        //emptyText.setVisibility(isEmpty ? View.VISIBLE : View.INVISIBLE);
        //plusIcon.setVisibility(isEmpty ? View.VISIBLE : View.INVISIBLE);
        emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty) {
            emptyStateLayout.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AddCardFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
        Log.d("EmptyState", "isEmpty: " + isEmpty);
    }
}
