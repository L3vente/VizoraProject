package com.example.vizoraproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignedInReports extends AppCompatActivity {
    private static final String LOG_TAG = SignedInReports.class.getName();
    private FirebaseUser user;
    private StorageReference storageRef;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private ArrayList<ReportItem> mItemList;
    private ReportItemAdapter mAdapter;
    @BindView(R.id.wctext)
    TextView welcomeTextET;
    private boolean viewRow = true;
    private FirebaseAuth mAuth;
    private String userId;
    private FirebaseFirestore db;
    private int gridNumber = 1;
    private int itemLimit = 10;
    private BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();

            if (intentAction == null)
                return;

            switch (intentAction) {
                case Intent.ACTION_BATTERY_LOW:
                    itemLimit = 10;
                    queryData();
                    break;
                case Intent.ACTION_BATTERY_OKAY:
                    itemLimit = 20;
                    queryData();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in_reports);
        ButterKnife.bind(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (user == null) {
            finishAfterTransition();
        }
        userId = mAuth.getCurrentUser().getUid();
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();
        mAdapter = new ReportItemAdapter(this, mItemList);
        mRecyclerView.setAdapter(mAdapter);
        storageRef = FirebaseStorage.getInstance().getReference();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        this.registerReceiver(powerReceiver, filter);
        queryData();
    }


    private void queryData() {
        db.collection("userReports")
                .whereEqualTo("userId", userId).orderBy("factoryNum", Query.Direction.DESCENDING).limit(itemLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    mItemList.clear();
                    for (QueryDocumentSnapshot documentSnapshot :
                            queryDocumentSnapshots) {
                        ReportItem item = documentSnapshot.toObject(ReportItem.class);
                        item.setReportId(documentSnapshot.getId());
                        mItemList.add(item);
                    }
                    mAdapter.notifyDataSetChanged();
                    db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                        String text;
                        String name;
                        name = documentSnapshot.get("lastname") + " " + documentSnapshot.get("firstname");
                        if (mItemList == null || mItemList.size() == 0) {
                            text = "Üdvözöllek " + name + ", eddig még nem volt jelentésed! Adj hozzá egyet a + menü megnyomásával! Keresés gyári szám alapján...";
                        } else if (mItemList.size() == 1) {
                            text = "Üdvözöllek " + name + "!" + " Alább látod az általad beküldött vízóra jelentésed. Keresés gyári szám alapján...";
                        } else {
                            text = "Üdvözöllek " + name + "!" + " Alább láthatod az általad beküldött vízóra jelentéseid. Keresés gyári szám alapján...";
                        }
                        welcomeTextET.setText(text);
                    }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
                }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out_button:
                new AlertDialog.Builder(this)
                        .setTitle("Kijelentkezés")
                        .setMessage("Biztosan ki akarsz jelentkezni?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("igen", (dialog, whichButton) -> {
                            if (whichButton != 0) {
                                FirebaseAuth.getInstance().signOut();
                                finishAfterTransition();
                            }

                        })
                        .setNegativeButton("nem", null).show();
                return true;
            case R.id.add_reportBTN:
                Intent intent = new Intent(this, AddReportActivity.class);
                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(this, welcomeTextET, "asd");
                startActivity(intent, options.toBundle());
                return true;
            case R.id.view_selector:
                viewRow = !viewRow;
                if (viewRow) {
                    mAdapter.setGridSize(1);
                    changeSpanCount(item, R.drawable.ic_view_grid, 1);
                } else {
                    mAdapter.setGridSize(2);
                    changeSpanCount(item, R.drawable.ic_view_row, 2);

                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private MenuItem keres;
    private SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.reports_menu, menu);
        keres = menu.findItem(R.id.search_bar);
        searchView = (SearchView) MenuItemCompat.getActionView(keres);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (mItemList != null && mItemList.size() != 0) {
                    mAdapter.getFilter().filter(s);
                }

                return false;
            }
        });

        return true;
    }


    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
    }

    public void editItem(ReportItem currentItem) {
        Intent intent = new Intent(this, EditReportActivity.class);
        ActivityOptions options = ActivityOptions
                .makeSceneTransitionAnimation(this, welcomeTextET, "asd");
        intent.putExtra("id", currentItem._getReportId());
        intent.putExtra("filename", currentItem.getImageFileName());
        startActivity(intent, options.toBundle());
    }

    public void deleteItem(ReportItem currentItem) {
        new AlertDialog.Builder(this)
                .setTitle("Törlés")
                .setMessage("Biztosan ki akarod törölni ezt a jelentést?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("igen", (dialog, whichButton) -> {
                    if (whichButton != 0) {
                        StorageReference deleteImage = storageRef.child("images/" + currentItem.getImageFileName());
                        deleteImage.delete().addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()).addOnSuccessListener(unused -> {
                            DocumentReference userRef = db.collection("userReports").document(currentItem._getReportId());
                            userRef.delete().addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()).addOnSuccessListener(unused1 -> {
                                Toast.makeText(this, "A jelentés törlése sikeres volt!", Toast.LENGTH_LONG).show();
                                queryData();
                            });
                        });

                    }

                })
                .setNegativeButton("nem", null).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryData();
    }
}