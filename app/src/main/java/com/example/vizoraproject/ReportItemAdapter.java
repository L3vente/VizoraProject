package com.example.vizoraproject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class ReportItemAdapter extends RecyclerView.Adapter<ReportItemAdapter.ViewHolder> implements Filterable {
    private ArrayList<ReportItem> mReportItemsData;
    private ArrayList<ReportItem> mReportDataAll;
    private Context mContext;
    private int lastPostition = -1;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
   private StorageReference imageRef;
   private int mGridSize = 1;

    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();



    public ReportItemAdapter(Context context, ArrayList<ReportItem> itemsData) {
        ButterKnife.bind((Activity) context);
        this.mReportItemsData = itemsData;
        this.mReportDataAll = itemsData;
        this.mContext = context;
    }
    public void setGridSize(int gridSize) {
        mGridSize = gridSize;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_reports, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReportItemAdapter.ViewHolder holder, int position) {
        ReportItem currentItem = (ReportItem) mReportItemsData.get(position);
        holder.bindTo(currentItem,this.mGridSize);

        if (holder.getAdapterPosition() > lastPostition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            lastPostition = holder.getAdapterPosition();
        }

    }

    @Override
    public int getItemCount() {
        return mReportItemsData.size();
    }

    @Override
    public Filter getFilter() {
        return this.shoppingFilter;
    }
    private Filter shoppingFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<ReportItem> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();
            if(charSequence == null || charSequence.length() == 0){
                results.count = mReportDataAll.size();
                results.values = mReportDataAll;
            }else{
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for(ReportItem shoppingItem : mReportDataAll){
                    if(shoppingItem.getFactoryNum().contains(filterPattern)){
                        filteredList.add(shoppingItem);
                    }

                }
                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mReportItemsData = (ArrayList<ReportItem>) filterResults.values;
            notifyDataSetChanged();

        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mAddress;
        private TextView mFactoryNum;
        private TextView mDate;
        private TextView mPosition;
        private ImageView mImageView;
        private LinearLayout mButtons;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAddress = itemView.findViewById(R.id.address);
            mFactoryNum = itemView.findViewById(R.id.factoryNum);
            mDate = itemView.findViewById(R.id.date);
            mPosition = itemView.findViewById(R.id.position);
            mImageView = itemView.findViewById(R.id.itemImage);
            mButtons = itemView.findViewById(R.id.buttons);

        }

        public void bindTo(ReportItem currentItem,int girdsize) {
            mAddress.setText(currentItem.getAddress());
            mFactoryNum.setText(currentItem.getFactoryNum());
            mDate.setText(currentItem.getDate());
            mPosition.setText(((currentItem.getCurrentTimerPosition()))+" m3");
            if(girdsize == 1){
                mButtons.setOrientation(LinearLayout.HORIZONTAL);
            }else {
                mButtons.setOrientation(LinearLayout.VERTICAL);
            }

             imageRef = storageRef.child("images/" + currentItem.getImageFileName());

            imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mImageView.setImageBitmap(bitmap);
            });

            itemView.findViewById(R.id.edit).setOnClickListener(view -> ((SignedInReports)mContext).editItem(currentItem));
            itemView.findViewById(R.id.delete).setOnClickListener(view ->((SignedInReports)mContext).deleteItem(currentItem) );
        }
    }
}
