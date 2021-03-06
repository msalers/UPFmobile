package mobile.android.upf.data.model.RecyclerViewAdapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import mobile.android.upf.R;
import mobile.android.upf.data.model.Notification;
import mobile.android.upf.data.model.Order;
import mobile.android.upf.ui.client.client_homepage.ClientOrdersFragment;

public class RecyclerViewAdapter_client_view_order extends RecyclerView.Adapter<RecyclerViewAdapter_client_view_order.MyViewHolder> {

    private Context mContext;
    private Fragment mFragment;
    private List<Order> mData;
    private ViewGroup parent;

    private DatabaseReference mDatabase;

    public RecyclerViewAdapter_client_view_order(Context mContext, List<Order> mData, Fragment mFragment) {
        this.mContext = mContext;
        this.mData = mData;
        this.mFragment = mFragment;
    }

    public RecyclerViewAdapter_client_view_order(Context mContext, List<Order> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        this.parent = parent;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        view = mInflater.inflate(R.layout.cardview_item_order_client, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tv_order_summary.setText(mData.get(position).getDishes_summary());
        holder.tv_order_date.setText(mData.get(position).getDate());
        holder.tv_order_time.setText(mData.get(position).getTime());

        if (mData.get(position).getState() == 1){
            holder.tv_delete_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog myQuittingDialogBox = new AlertDialog.Builder(mContext)
                            .setTitle(R.string.confirm_delete)
                            .setMessage(R.string.confirm_order_delete)
                            .setIcon(R.drawable.ic_baseline_delete_24_black)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Id del ristorante con l'ordine
                                    String toDeleteRestId = mData.get(position).getRestaurant_id();
                                    //Id dell'utente con l'ordine
                                    String toDeleteUserId = mData.get(position).getUser_id();
                                    //Id dell'ordine
                                    String toDeleteId = mData.get(position).getId();
                                    Log.d("Order to delete id: ", toDeleteId);

                                    if(mData.get(position).getState()!=1) {
                                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                mDatabase.child("Restaurants").child(toDeleteRestId)
                                                        .child("Orders").child(toDeleteId).removeValue();
                                                mDatabase.child("Users").child(toDeleteUserId)
                                                        .child("Orders").child(toDeleteId).removeValue();
                                                mDatabase.child("Orders").child(toDeleteId).removeValue();

                                                ((ClientOrdersFragment) mFragment).updateRecycler();

                                                Toast.makeText(mContext, R.string.order_deleted, Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                                Log.e("firebase", "Error while removing data from db");
                                            }
                                        });
                                    } else {
                                        Toast.makeText(mContext, R.string.order_cannot_delete, Toast.LENGTH_SHORT).show();
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                }
                            })
                            .create();
                    myQuittingDialogBox.show();
                    myQuittingDialogBox.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
                    myQuittingDialogBox.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                }
            });

            holder.tv_delete_btn.setVisibility(View.VISIBLE);
            holder.tv_order_text.setText(R.string.order_waiting);
            holder.tv_order_text.setTextColor(mContext.getResources().getColor(R.color.orange_500));
            holder.tv_order_text.setVisibility(View.VISIBLE);

        } else if(mData.get(position).getState() == 2) { // Ordine accettato dal ristorante
            holder.tv_order_text.setText(R.string.order_accepted);
            holder.tv_order_text.setTextColor(mContext.getResources().getColor(R.color.dark_green));
            holder.tv_order_text.setVisibility(View.VISIBLE);
        } else if(mData.get(position).getState() == -1) { // Ordine rifiutato dal ristorante
            holder.tv_order_text.setText(R.string.order_rejected);
            holder.tv_order_text.setTextColor(mContext.getResources().getColor(R.color.red));
            holder.tv_order_text.setVisibility(View.VISIBLE);
        } else if(mData.get(position).getState() == 3) { // Ordine pronto ed in attesa
            holder.tv_order_text.setText(R.string.order_ready_wait);
            holder.tv_order_text.setTextColor(mContext.getResources().getColor(R.color.cyan));
            holder.tv_order_text.setVisibility(View.VISIBLE);
        } else if(mData.get(position).getState() == 4) { // Ordine in mano al fattorino
            holder.tv_order_text.setText(R.string.order_delivered);
            holder.tv_order_text.setTextColor(mContext.getResources().getColor(R.color.green_done));
            holder.tv_order_text.setVisibility(View.VISIBLE);

            holder.tv_delivered_btn.setVisibility(View.VISIBLE);

            holder.tv_delivered_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { // ORDINE IMPOSTATO CON STATO 5 - CONSEGNATO
                    mDatabase.child("Orders").child(mData.get(position).getId()).child("state").setValue(5);

                    String msg = mContext.getString(R.string.msg_notification_order_received_by_client);

                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    String date = sdf.format(cal.getTime());
                    SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm");
                    String time = sdf_time.format(cal.getTime());

                    Notification notification = new Notification(mData.get(position).getDelivery_id(),date, time, "1", msg);
                    mDatabase.child("Notifications").child(mData.get(position).getUser_id()).child(String.valueOf(notification.getId())).setValue(notification);

                    Toast.makeText(mContext, R.string.confirm_delivered, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_order_summary, tv_order_text, tv_order_date, tv_order_time;
        Button tv_delete_btn, tv_delivered_btn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_order_summary = (TextView) itemView.findViewById(R.id.client_order_dishes_summary);
            tv_order_text = (TextView) itemView.findViewById(R.id.client_order_progress);
            tv_order_date = (TextView) itemView.findViewById(R.id.client_order_date);
            tv_order_time = (TextView) itemView.findViewById(R.id.client_order_time);

            tv_delete_btn = (Button) itemView.findViewById(R.id.client_delete_order_btn);
            tv_delivered_btn = (Button) itemView.findViewById(R.id.client_order_delivered_btn);
        }
    }
}