package com.example.cardify;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class SavedCardsFragment extends Fragment {

    private RecyclerView recyclerView;
    private VizitkaAdapter adapter;
    private List<Vizitka> vizitkaList;
    private TextView emptyText;
    private ImageView plusIcon, filterButton;
    private LinearLayout emptyStateLayout, searchFilterLayout;
    EditText searchInput;

    private DatabaseReference vizitRef;
    private String userId = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_cards, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_saved);
        emptyText = view.findViewById(R.id.empty_text);
        plusIcon = view.findViewById(R.id.plus_icon);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        searchInput = view.findViewById(R.id.search_input);
        filterButton = view.findViewById(R.id.filter_button);
        searchFilterLayout = view.findViewById(R.id.search_filter_layout);


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
        final boolean[] issEmpty = {true};
        DatabaseReference savedRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("savedVizitcards");

        savedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vizitkaList.clear();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    adapter.notifyDataSetChanged();
                    showEmptyState(true);
                    return;
                }

                showEmptyState(false);

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
                                    issEmpty[0] = false;
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                savedRef.child(cardId).removeValue();
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
       //if(issEmpty[0]) showEmptyState(true);
       //else showEmptyState(false);
    }

    private void showEmptyState(boolean isEmpty) {
        //emptyText.setVisibility(isEmpty ? View.VISIBLE : View.INVISIBLE);
        //plusIcon.setVisibility(isEmpty ? View.VISIBLE : View.INVISIBLE);
        emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        searchFilterLayout.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (isEmpty) {

            emptyStateLayout.setOnClickListener(v -> {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    activity.goToSaveCardFragment();
                }
            });
        }
        Log.d("EmptyState", "isEmpty: " + isEmpty);
    }
}
