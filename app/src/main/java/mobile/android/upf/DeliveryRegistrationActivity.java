package mobile.android.upf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import mobile.android.upf.data.model.User;

import static android.widget.Toast.LENGTH_LONG;

public class DeliveryRegistrationActivity extends AppCompatActivity {

    private EditText editTextName, editTextSurname, editTextEmail, editTextPhone,
            editTextPassword, editTextConfirmPassword;

    private final String imageUrl = "https://firebasestorage.googleapis.com/v0/b/ultimatepizzafrisbee.appspot.com/o/splash.jpg?alt=media&token=6a2ea30e-8806-4e0c-8e7a-61dc8c0cd594";

    //private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_registration);

        mAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setTitle(R.string.register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextName = (EditText) findViewById(R.id.delivery_name);
        editTextSurname = (EditText) findViewById(R.id.delivery_surname);
        editTextPassword = (EditText) findViewById(R.id.delivery_password);
        editTextConfirmPassword = (EditText) findViewById(R.id.delivery_passwordConfirm);
        editTextPhone = (EditText) findViewById(R.id.delivery_phone);
        editTextEmail = (EditText) findViewById(R.id.delivery_emailAddress);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_delivery);

        Button delivery_registration_btn = (Button) findViewById(R.id.delivery_register_btn);
        delivery_registration_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.delivery_register_btn) {
                    registerDeliveryUser();
                }
            }
        });
    }

    public void registerDeliveryUser() {
        String name = editTextName.getText().toString().trim();
        String surname = editTextSurname.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String address = "not available";
        String city = "not available";
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();


        if (name.isEmpty()) {
            editTextName.setError(getString(R.string.empty_name));
            editTextName.requestFocus();
            return;
        }
        if (surname.isEmpty()) {
            editTextSurname.setError(getString(R.string.empty_surname));
            editTextSurname.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            editTextEmail.setError(getString(R.string.empty_email));
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError(getString(R.string.invalid_email));
            editTextEmail.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            editTextPhone.setError(getString(R.string.empty_phone));
            editTextPhone.requestFocus();
            return;
        }
        if (!Patterns.PHONE.matcher(phone).matches()) {
            editTextPhone.setError(getString(R.string.invalid_phone_number));
            editTextPhone.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError(getString(R.string.empty_password));
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError(getString(R.string.short_password));
            editTextPassword.requestFocus();
            return;
        }
        if (confirmPassword.isEmpty()) {
            editTextConfirmPassword.setError(getString(R.string.empty_confirm_password));
            editTextConfirmPassword.requestFocus();
            return;
        }
        if (!confirmPassword.equals(password)) {
            editTextConfirmPassword.setError(getString(R.string.password_mismatch));
            editTextConfirmPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(DeliveryRegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            User user = new User(name, surname, password, city, address, phone, email, imageUrl, 2, 0);
                            //aggiungo l'utente al db
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(DeliveryRegistrationActivity.this, getString(R.string.delivery_registration_success), LENGTH_LONG).show();

                                        final Intent delivery_homepage = new Intent(DeliveryRegistrationActivity.this, DeliveryHomepageActivity.class);
                                        startActivity(delivery_homepage);
                                        DeliveryRegistrationActivity.this.finishAffinity();
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(DeliveryRegistrationActivity.this, getString(R.string.delivery_registration_db_failed), LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(DeliveryRegistrationActivity.this, getString(R.string.delivery_registration_auth_failed), LENGTH_LONG).show();
                        }
                    }
                });
    }
}