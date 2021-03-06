package mobile.android.upf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shuhart.stepview.StepView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mobile.android.upf.data.model.Dish;
import mobile.android.upf.data.model.Notification;
import mobile.android.upf.data.model.Order;

import static android.widget.Toast.LENGTH_LONG;

public class CartCheckout extends AppCompatActivity {

    private TextView order_dishes_summary, order_total_summary, order_payment_summary;
    private Button payment_btn;

    String expiration, possessor, last_digit, city, address;
    String restaurant_id;

    private FirebaseAuth mAuth;
    private ArrayList<Dish> dishes;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_checkout);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Back arrow
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        boolean card = intent.getExtras().getBoolean("card");
        city = intent.getExtras().getString("city");
        address = intent.getExtras().getString("address");

        dishes = new ArrayList<>();
        order_dishes_summary = (TextView) findViewById(R.id.order_dishes_summary);
        order_total_summary = (TextView) findViewById(R.id.order_total_summary);
        order_payment_summary = (TextView) findViewById(R.id.order_payment_summary);

        StepView stepView = findViewById(R.id.step_view);
        stepView.getState()
                // You should specify only stepsNumber or steps array of strings.
                // In case you specify both steps array is chosen.
                .steps(new ArrayList<String>() {{
                    add(getApplicationContext().getString(R.string.address_payment));
                    add(getApplicationContext().getString(R.string.summary));
                    add(getApplicationContext().getString(R.string.payment));
                }})

                .animationDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .stepLineWidth(1)
                .textSize(getResources().getDimensionPixelSize(R.dimen.text_size_small))
                .stepNumberTextSize(getResources().getDimensionPixelSize(R.dimen.text_size_small))
                .commit();
        stepView.go(2,false);


        final String[] summary_dishes = {""};
        mDatabase.child("Cart").child(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    Iterable<DataSnapshot> dishes_database = task.getResult().getChildren();

                    restaurant_id = "";

                    double total_order = 0;

                    for (DataSnapshot dish : dishes_database) {

                        dishes.add(new Dish(
                                        String.valueOf(dish.getKey()),
                                        String.valueOf(dish.child("name").getValue()),
                                        String.valueOf(dish.child("description").getValue()),
                                        String.valueOf(dish.child("restaurant_id").getValue()),
                                        Double.parseDouble(String.valueOf(dish.child("price").getValue())),
                                        Integer.parseInt(String.valueOf(dish.child("number").getValue()))
                                )
                        );
                        double total_price = Double.parseDouble(String.valueOf(dish.child("price").getValue())) * Integer.parseInt(String.valueOf(dish.child("number").getValue()));
                        summary_dishes[0] = summary_dishes[0] + String.valueOf(dish.child("name").getValue()) + " (x"+Integer.parseInt(String.valueOf(dish.child("number").getValue()))+") "+total_price+"\n";
                        total_order += total_price;
                        restaurant_id = String.valueOf(dish.child("restaurant_id").getValue());


                    }
                    order_total_summary.setText(String.valueOf(total_order));
                    order_dishes_summary.setText(summary_dishes[0]);
                }
            }
        });



        if (card){
            expiration = intent.getExtras().getString("expiration");
            possessor = intent.getExtras().getString("possessor");
            last_digit = intent.getExtras().getString("last_digits");
            order_payment_summary.setText(city + "\n" + address + "\nCard number: **** **** **** "+last_digit+"\n"+possessor+"\n"+expiration);
        }
        else {
            order_payment_summary.setText(city + "\n" + address + "\nCash at the delivery");
        }




        payment_btn = (Button) findViewById(R.id.payment_btn);

        dishes = new ArrayList<>();

        payment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartCheckout.this.finishAffinity();

                Calendar cal = Calendar.getInstance();

                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String date = sdf.format(cal.getTime());
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm");
                String time = sdf_time.format(cal.getTime());


                final String[] restaurateur_id = {""};
                final String[] restaurant_name = {""};
                mDatabase.child("Restaurants").child(restaurant_id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        restaurateur_id[0] = String.valueOf(task.getResult().child("restaurateur_id").getValue());
                        restaurant_name[0] = String.valueOf(task.getResult().child("name").getValue());
                        Order order = new Order(mAuth.getCurrentUser().getUid(), restaurant_id, restaurant_name[0], "", dishes, summary_dishes[0], order_total_summary.getText().toString(), order_payment_summary.getText().toString(), city, address, date, time, 1);


                        mDatabase.child("Orders").child(order.getId()).setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Calendar cal = Calendar.getInstance();
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                    String date = sdf.format(cal.getTime());
                                    SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm");
                                    String time = sdf_time.format(cal.getTime());

                                    String msg = "New order for " + restaurant_name[0] +".";
                                    Notification notification = new Notification(mAuth.getCurrentUser().getUid(),date, time, "1",msg);


                                    mDatabase.child("Notifications").child(restaurateur_id[0]).child(String.valueOf(notification.getId())).setValue(notification);


                                    Intent returnIntent = new Intent();
                                    setResult(RESULT_OK, returnIntent);
                                    finish();
                                } else {
                                    Toast.makeText(CartCheckout.this, getString(R.string.dish_add_db_failed), LENGTH_LONG).show();
                                }
                            }
                        });

                        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Orders").child(order.getId()).setValue(order.getId());

                        mDatabase.child("Restaurants").child(restaurant_id).child("Orders").child(order.getId()).setValue(order.getId());

                        mDatabase.child("Cart").child(mAuth.getCurrentUser().getUid()).removeValue();

                        Intent intent = new Intent(CartCheckout.this, ClientHomepageActivity.class);
                        startActivity(intent);
                    }
                });



            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}