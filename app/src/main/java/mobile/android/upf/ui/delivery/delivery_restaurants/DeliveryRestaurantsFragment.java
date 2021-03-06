package mobile.android.upf.ui.delivery.delivery_restaurants;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import mobile.android.upf.AddSubscriptionActivity;
import mobile.android.upf.R;
import mobile.android.upf.data.model.RecyclerViewAdapter.RecyclerViewAdapter_client_restaurant;
import mobile.android.upf.data.model.RecyclerViewAdapter.RecyclerViewAdapter_delivery_restaurant;
import mobile.android.upf.data.model.Restaurant;

public class DeliveryRestaurantsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    private Context context;
    private MaterialCardView cardview;
    private ViewGroup.LayoutParams layoutparams;
    private LinearLayout linearLayout;

    private RecyclerView myrv;
    private RecyclerViewAdapter_delivery_restaurant myAdapter;

    private List<Restaurant> lstRest;
    private String userId;

    private SwipeRefreshLayout swipeRefreshLayout;

    private DeliveryRestourantsViewModel deliveryRestourantsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        deliveryRestourantsViewModel =
                new ViewModelProvider(this).get(DeliveryRestourantsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_restaurants_delivery, container, false);
        setHasOptionsMenu(true);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        userId = currentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //        Floating button per l'aggiunta di nuovi ristoranti
        ExtendedFloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddSubscriptionActivity.class);
                startActivityForResult(intent, 1);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        lstRest = new ArrayList<>();

        mDatabase.child("Restaurants").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    Iterable<DataSnapshot> restaurants_database = task.getResult().getChildren();

                    for (DataSnapshot restaurant : restaurants_database) {

                            for (DataSnapshot subscriber: restaurant.child("Subscribers").getChildren()){
                                if (subscriber.getKey().equals(currentUser.getUid())){
                                    lstRest.add(new Restaurant(
                                            String.valueOf(restaurant.getKey()),
                                            String.valueOf(restaurant.child("name").getValue()),
                                            String.valueOf(restaurant.child("description").getValue()),
                                            String.valueOf(restaurant.child("email").getValue()),
                                            String.valueOf(restaurant.child("city").getValue()),
                                            String.valueOf(restaurant.child("address").getValue()),
                                            String.valueOf(restaurant.child("phone").getValue()),
                                            String.valueOf(restaurant.child("restaurateur_id").getValue()),
                                            String.valueOf(restaurant.child("admin_id").getValue()),
                                            String.valueOf(restaurant.child("imageUrl").getValue()),
                                            Integer.parseInt(String.valueOf(restaurant.child("status").getValue()))));
                                }
                            }

                    }

                    myrv = (RecyclerView) root.findViewById(R.id.recyclerview_delivery_restaurants);
                    myAdapter = new RecyclerViewAdapter_delivery_restaurant(getActivity(), DeliveryRestaurantsFragment.this, lstRest);

                    myrv.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                    myrv.setAdapter(myAdapter);

                }

            }

        });


        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_restaurant_delivery);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateRecycler();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateRecycler();
    }

    public void updateRecycler(){
        lstRest = new ArrayList<>();

        mDatabase.child("Restaurants").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    Iterable<DataSnapshot> restaurants_database = task.getResult().getChildren();

                    for (DataSnapshot restaurant : restaurants_database) {
//                        ID del ristorante


                        for (DataSnapshot subscriber: restaurant.child("Subscribers").getChildren()){
                            if (subscriber.getKey().equals(currentUser.getUid())){
                                lstRest.add(new Restaurant(
                                        String.valueOf(restaurant.getKey()),
                                        String.valueOf(restaurant.child("name").getValue()),
                                        String.valueOf(restaurant.child("description").getValue()),
                                        String.valueOf(restaurant.child("email").getValue()),
                                        String.valueOf(restaurant.child("city").getValue()),
                                        String.valueOf(restaurant.child("address").getValue()),
                                        String.valueOf(restaurant.child("phone").getValue()),
                                        String.valueOf(restaurant.child("restaurateur_id").getValue()),
                                        String.valueOf(restaurant.child("admin_id").getValue()),
                                        String.valueOf(restaurant.child("imageUrl").getValue()),
                                        Integer.parseInt(String.valueOf(restaurant.child("status").getValue()))));
                            }
                        }


                    }

                    myAdapter = new RecyclerViewAdapter_delivery_restaurant(getActivity(), DeliveryRestaurantsFragment.this, lstRest);

                    myrv.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                    myrv.setAdapter(myAdapter);

                }


            }

        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.actionSearch).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.length() > 0) {
                    filter(String.valueOf(s));
                } else {
                    myAdapter = new RecyclerViewAdapter_delivery_restaurant(getActivity(), lstRest);

                    myrv.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                    myrv.setAdapter(myAdapter);
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() > 0) {
                    filter(String.valueOf(s));
                } else {
                    myAdapter = new RecyclerViewAdapter_delivery_restaurant(getActivity(), lstRest);

                    myrv.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                    myrv.setAdapter(myAdapter);
                }
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);


    }

    private void filter(String text) {
        ArrayList<Restaurant> filteredList = new ArrayList<>();

        for (Restaurant item : lstRest) {
//            Log.d("RICERCATO", text);
//            Log.d("RICERCA", item.getName());
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(getActivity(), "No data found...", Toast.LENGTH_SHORT).show();
        }
        myAdapter = new RecyclerViewAdapter_delivery_restaurant(getActivity(), filteredList);

        myrv.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        myrv.setAdapter(myAdapter);
    }
}