package mobile.android.upf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobile.android.upf.data.model.Dish;

import static android.widget.Toast.LENGTH_LONG;

public class AddDishRestaurantActivity extends AppCompatActivity {

    private Button add_dish_btn;
    private EditText dish_name, dish_description, dish_price;

    private static final String TAG_LOG = "AddDishActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private Dish dish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dish_restaurant);

        //        Back arrow
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

//        Get restaurant ID
        Intent intent = getIntent();
        String restaurant_id = intent.getExtras().getString("id");

        mAuth = FirebaseAuth.getInstance();
        String restaurateur_id = mAuth.getCurrentUser().getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        dish_name = (EditText) findViewById(R.id.dish_name);
        dish_description = (EditText) findViewById(R.id.dish_description);
        dish_price = (EditText) findViewById(R.id.dish_price);

        add_dish_btn = (Button) findViewById(R.id.add_dish_btn);
        add_dish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = dish_name.getText().toString().trim();
                String description = dish_description.getText().toString().trim();
                String price = dish_price.getText().toString().trim();

                if (name.isEmpty()) {
                    dish_name.setError(getString(R.string.empty_name));
                    dish_name.requestFocus();
                    return;
                }
                if (description.isEmpty()) {
                    dish_description.setError(getString(R.string.empty_name));
                    dish_description.requestFocus();
                    return;
                }
                if (price.isEmpty()) {
                    dish_price.setError(getString(R.string.empty_price));
                    dish_price.requestFocus();
                    return;
                }
                Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
                Matcher matcher = pattern.matcher(price);
                boolean matchFound = matcher.find();
                if(!matchFound) {
                    dish_price.setError(getString(R.string.invalid_price));
                    dish_price.requestFocus();
                    return;
                }
                if (Double.parseDouble(price) == 0) {
                    dish_price.setError(getString(R.string.price_zero));
                    dish_price.requestFocus();
                    return;
                }


                dish = new Dish(name, description, restaurant_id, Double.parseDouble(price), 0);

                mDatabase.child("Dishes").child(dish.getId()).setValue(dish).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent returnIntent = new Intent();
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        } else {
                            Toast.makeText(AddDishRestaurantActivity.this, getString(R.string.dish_add_db_failed), LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}