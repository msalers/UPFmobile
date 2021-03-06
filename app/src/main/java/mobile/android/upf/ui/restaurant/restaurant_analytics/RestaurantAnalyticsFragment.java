package mobile.android.upf.ui.restaurant.restaurant_analytics;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Random;

import mobile.android.upf.R;
import mobile.android.upf.data.model.Dish;

public class RestaurantAnalyticsFragment extends Fragment {

    private RestaurantsAnalyticsViewModel restaurantAnalyticsViewModel;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    private ArrayList<Dish> lstDishes;
    private ArrayList<BarEntry> lstEntries;
    private ArrayList<String> lstLabels, labelArrayLegend;
    private ArrayList<Integer> lstColors,lstColorsLegend;
    private BarDataSet barDataSet;
    private BarChart barChart;
    private BarData barData;
    private String userId;

    private LinearLayout legend;


    @SuppressLint("WrongViewCast")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        restaurantAnalyticsViewModel =
                new ViewModelProvider(this).get(RestaurantsAnalyticsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_restaurant_analytics, container, false);

        mAuth = FirebaseAuth.getInstance();

        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        currentUser = mAuth.getCurrentUser();

        userId = currentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        barChart = root.findViewById(R.id.restaurant_histogram);
        lstDishes = new ArrayList<>();
        lstEntries = new ArrayList<>();
        lstLabels = new ArrayList<>();
        lstColors = new ArrayList<>();
        labelArrayLegend = new ArrayList<>();
        barData = new BarData();

        lstColorsLegend = new ArrayList<>();

        legend = (LinearLayout) root.findViewById(R.id.legend);

        mDatabase.child("Restaurants").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    Iterable<DataSnapshot> restaurants_database = task.getResult().getChildren();
                    lstColorsLegend.clear();
                    labelArrayLegend.clear();
                    lstColors.clear();
                    lstLabels.clear();

                    for (DataSnapshot restaurant : restaurants_database) {

                        if (String.valueOf(restaurant.child("restaurateur_id").getValue()).equals(userId)
                                && Integer.parseInt(String.valueOf(restaurant.child("status").getValue())) == 1) {

                            lstDishes.clear();

                            Random rnd = new Random();
                            int color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                            lstColorsLegend.add(color);
                            labelArrayLegend.add(String.valueOf(restaurant.child("name").getValue()));

                            mDatabase.child("Dishes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                    if (!task.isSuccessful()) {
                                        Log.e("firebase", "Error getting data", task.getException());
                                    } else {
                                        Iterable<DataSnapshot> dishes_database = task.getResult().getChildren();

                                        for (DataSnapshot dish : dishes_database) {
                                            if (String.valueOf(dish.child("restaurant_id").getValue()).equals(
                                                    String.valueOf(restaurant.child("id").getValue()))) {

                                                lstDishes.add(new Dish(
                                                        String.valueOf(dish.getKey()),
                                                        String.valueOf(dish.child("name").getValue()),
                                                        String.valueOf(dish.child("description").getValue()),
                                                        String.valueOf(dish.child("restaurant_id").getValue()),
                                                        Double.parseDouble(String.valueOf(dish.child("price").getValue())),
                                                        Integer.parseInt(String.valueOf(dish.child("number").getValue()))));
                                            }

                                        }

                                        lstEntries.clear();
                                        lstLabels.clear();
                                        int Dishcounter = 0;
                                        for (Dish dish : lstDishes) {
                                            lstEntries.add(new BarEntry(Dishcounter, dish.getNumber()));
                                            lstLabels.add(dish.getName());
                                            Dishcounter++;
                                            lstColors.add(color);
                                        }

                                        if (lstEntries.size() != 0) {
                                            barDataSet = new BarDataSet(lstEntries, String.valueOf(restaurant.child("name").getValue()));

                                            barDataSet.setColors(lstColors);
                                            barDataSet.setValueTextColor(Color.DKGRAY);
                                            barDataSet.setValueTextSize(16f);
                                            barData.addDataSet(barDataSet);
                                        }
                                        barChart.setData(barData);
                                        Log.d("DATI", String.valueOf(barData.getDataSetCount()));
//                                        if (barData.getDataSetCount() > 1) {
//                                            float groupSpace = 0.1f;
//                                            float barSpace = 0.3f;
//                                            barChart.groupBars(1, groupSpace, barSpace);
//                                        }
                                        barChart.setFitBars(true);
                                        barChart.setDrawValueAboveBar(true);
                                        barChart.setDrawGridBackground(true);
                                        barChart.getDescription().setText("");

                                        XAxis xAxis = barChart.getXAxis();
                                        xAxis.setValueFormatter(new IndexAxisValueFormatter(lstLabels));
                                        xAxis.setLabelRotationAngle(-90);
                                        //xAxis.setCenterAxisLabels(true);
                                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                        barChart.animateY(2000);

                                        Legend legend = barChart.getLegend();
                                        legend.setEnabled(false);

//                                        ArrayList<LegendEntry> legendEntries = new ArrayList<>();
//
//                                        int i = 0;
//                                        for (String restaurant: labelArrayLegend){
//                                            LegendEntry legendEntryA = new LegendEntry();
//                                            legendEntryA.label = restaurant;
//                                            legendEntryA.formColor = lstColorsLegend.get(i);
//                                            legendEntries.add(legendEntryA);
//                                            i++;
//                                        }

//                                        legend.setCustom(legendEntries);
                                    }

                                }
                            });
                        }



                    }
                    // Fine ciclo for ristoranti

                }
                int i = 0;
                for (String restaurant: labelArrayLegend){
                    TextView label = new TextView(getContext());
                    String name = getString(R.string.square)+" "+restaurant;
                    label.setText(name);
                    label.setTextColor(lstColorsLegend.get(i));
                    i++;
                    legend.addView(label);
                }

            }

        });



        return root;
    }
}
