package com.example.smartairapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ParentHomeActivity extends AppCompatActivity {
    private PopupWindow alertsPopup;
    private CardView childButton, manageProviderButton;
    private CardView manageInventoryButton;
    private CardView providerReportButton;
    private BottomNavigationView bottomNav;
    private Spinner spinnerChildren;
    private List<String> childNames = new ArrayList<>();
    private List<String> childIds = new ArrayList<>();
    private String selectedChildId;
    private String parentId;

    private AlertsAdapter alertsAdapter;
    private List<Alert> alertList;
    private DatabaseReference alertsRef;
    private List<Alert> allAlertsMasterList = new ArrayList<>();
    private CardView zoneButton;
    private TextView zoneTitle, zoneMessage, pefValue;
    private ImageView btnNotifications, btnProfile;
    private TextView tvLastRescue, tvWeeklyRescueCount;

    private LineChart zoneChart, rescueChart;
    private int chartDays = 7;
    private boolean isZoneChartVisible = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        if (OnboardingActivity.isFirstLogin()) {
            OnboardingDialogFragment dialog = new OnboardingDialogFragment();

            Bundle args = new Bundle();
            args.putString("role", getIntent().getStringExtra("role"));
            args.putString("uid", getIntent().getStringExtra("uid"));
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "onboarding_dialog");
        }

        spinnerChildren = findViewById(R.id.spinnerChildren);
        parentId = getIntent().getStringExtra("parentId");
        if (parentId == null) {
            parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        childButton = findViewById(R.id.manageChildrenButton);
        manageProviderButton = findViewById(R.id.manageSharingButton);
        bottomNav = findViewById(R.id.bottomNav);
        manageInventoryButton = findViewById(R.id.manageInventoryButton);
        providerReportButton = findViewById(R.id.providerReportButton);
        

        zoneButton = findViewById(R.id.zone_button);
        zoneTitle = findViewById(R.id.zone_title);
        zoneMessage = findViewById(R.id.zone_message);
        pefValue = findViewById(R.id.pef_value);
        zoneButton.setOnClickListener(v -> showSetPersonalBestDialog());
        btnNotifications = findViewById(R.id.btnNotifications);
        btnProfile = findViewById(R.id.btnProfile);

        tvLastRescue = findViewById(R.id.tvLastRescue);
        tvWeeklyRescueCount = findViewById(R.id.tvWeeklyRescueCount);
        zoneChart = findViewById(R.id.zoneChart);
        rescueChart = findViewById(R.id.rescueChart);

        loadChildrenIntoSpinner();

        alertList = new ArrayList<>();
        alertsAdapter = new AlertsAdapter(this, alertList);

        btnNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAlertsPopup(v);
            }
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), ParentSettingsActivity.class));
        });

        childButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ChildManagementActivity.class);
            startActivity(intent);
        });

        manageProviderButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ManageAccessActivity.class);
            startActivity(intent);
        });

        manageInventoryButton.setOnClickListener(view -> {
            Intent intent = new Intent(ParentHomeActivity.this, ParentManageInventory.class);
            intent.putExtra("childId", selectedChildId);
            intent.putExtra("parentId", parentId);
            startActivity(intent);
        });

        providerReportButton.setOnClickListener(v -> {
            Intent intent = new Intent(ParentHomeActivity.this, ProviderReportActivity.class);
            intent.putExtra("childId", selectedChildId);
            intent.putExtra("parentId", parentId);
            startActivity(intent);
        });

        alertsRecyclerView = findViewById(R.id.alertsRecyclerView);
        alertsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alertList = new ArrayList<>();
        alertsAdapter = new AlertsAdapter(this, alertList);
        alertsRecyclerView.setAdapter(alertsAdapter);

        alertsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Alerts");

        alertsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allAlertsMasterList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Alert alert = snapshot.getValue(Alert.class);
                    if (alert != null) {
                        allAlertsMasterList.add(0, alert);
                    }
                }
                filterAlertsForSelectedChild();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ParentHomeActivity.this, "Failed to load alerts.", Toast.LENGTH_SHORT).show();
            }
        });

        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_alerts) {
                startActivity(new Intent(getApplicationContext(), ParentAlertsActivity.class));
                return false;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(getApplicationContext(), ParentHistoryActivity.class));
                return false;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(getApplicationContext(), ParentSettingsActivity.class));
                return false;
            } else if (itemId == R.id.nav_home) {
                return true;
            }
            return false;
        });

        TextView toggleDaysButton = findViewById(R.id.toggleDaysButton);
        toggleDaysButton.setOnClickListener(v -> {
            if (chartDays == 7) {
                chartDays = 30;
            } else {
                chartDays = 7;
            }
            updateOverviewForChild(selectedChildId);
        });

        TextView toggleChartButton = findViewById(R.id.toggleChartButton);
        toggleChartButton.setOnClickListener(v -> {
            isZoneChartVisible = !isZoneChartVisible;
            updateChartVisibility();
            updateOverviewForChild(selectedChildId);
        });
    }

    private void updateChartVisibility() {
        if (isZoneChartVisible) {
            zoneChart.setVisibility(View.VISIBLE);
            rescueChart.setVisibility(View.GONE);
        } else {
            zoneChart.setVisibility(View.GONE);
            rescueChart.setVisibility(View.VISIBLE);
        }
    }

    private void loadChildrenIntoSpinner() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId).child("Children");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childNames.clear();
                childIds.clear();

                for (DataSnapshot childSnap : snapshot.getChildren()){
                    String id = childSnap.getKey();
                    String name = childSnap.child("name").getValue(String.class);

                    childIds.add(id);
                    childNames.add(name != null ? name : "No Child");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ParentHomeActivity.this,
                        R.layout.spinner_item,
                        childNames
                );
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerChildren.setAdapter(adapter);

                loadSavedSelection();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadSavedSelection() {
        SharedPreferences prefs = getSharedPreferences("SmartAirPrefs", MODE_PRIVATE);
        String savedId = prefs.getString("selectedChildId", null);

        if (savedId != null) {
            int index = childIds.indexOf(savedId);
            if (index != -1) {
                spinnerChildren.setSelection(index);
                selectedChildId = savedId;
            }
        }
        filterAlertsForSelectedChild();

        if (selectedChildId != null) {
            updatePefDisplayForChild(selectedChildId);
            updateOverviewForChild(selectedChildId);
        }

        setupSpinnerListener();
    }

    private void setupSpinnerListener() {
        spinnerChildren.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedChildId = childIds.get(position);
                SharedPreferences prefs = getSharedPreferences("SmartAirPrefs", MODE_PRIVATE);
                prefs.edit().putString("selectedChildId", selectedChildId).apply();

                Toast.makeText(ParentHomeActivity.this,
                        "Switched to: " + childNames.get(position),
                        Toast.LENGTH_SHORT).show();

                filterAlertsForSelectedChild();
                updatePefDisplayForChild(selectedChildId);
                updateOverviewForChild(selectedChildId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterAlertsForSelectedChild() {
        if (selectedChildId == null && !childIds.isEmpty()) {
            selectedChildId = childIds.get(0);
        }

        alertList.clear();
        if (selectedChildId != null) {
            for (Alert alert : allAlertsMasterList) {
                if (alertList.size() >= 4) {
                    break;
                }
                if (selectedChildId.equals(alert.getChildId())) {
                    alertList.add(alert);
                }
            }
        }
        alertsAdapter.notifyDataSetChanged();
    }
    private void updatePefDisplayForChild(String childId) {
        if (childId == null) return;
        DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId).child("Children").child(childId);

        childRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer personalBest = snapshot.child("personalBest").getValue(Integer.class);
                    Integer latestPef = snapshot.child("latestPef").getValue(Integer.class);

                    if (personalBest == null) personalBest = 0;
                    if (latestPef == null) latestPef = 0;

                    updateZone(latestPef, personalBest);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParentHomeActivity.this, "Failed to load PEF data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOverviewForChild(String childId) {
        if (childId == null) return;

        DatabaseReference medLogsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId).child("Children").child(childId)
                .child("Logs").child("medicineLogs");

        medLogsRef.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot medSnap : snapshot.getChildren()) {
                        MedicineLog lastLog = medSnap.getValue(MedicineLog.class);
                        if (lastLog != null) {
                            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(lastLog.getTimestamp(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                            tvLastRescue.setText("Last Rescue: " + relativeTime);
                        }
                    }
                } else {
                    tvLastRescue.setText("Last Rescue: No recent data");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvLastRescue.setText("Last Rescue: Error");
            }
        });


        DatabaseReference overviewRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId).child("Children").child(childId)
                .child("DailyOverviews");

        overviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int weeklyCount = 0;
                List<DailyOverview> overviews = new ArrayList<>();
                for (DataSnapshot daySnap : snapshot.getChildren()) {
                    DailyOverview overview = daySnap.getValue(DailyOverview.class);
                    if (overview != null) {
                        overviews.add(overview);
                    }
                }

                for (int i = 0; i < 7 && i < overviews.size(); i++) {
                    weeklyCount += overviews.get(i).rescueCount;
                }
                tvWeeklyRescueCount.setText("Weekly Rescue Count: " + weeklyCount);

                setupZoneChart(overviews);
                setupRescueChart(overviews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvWeeklyRescueCount.setText("Weekly Rescue Count: Error");
            }
        });
    }

    private void setupRescueChart(List<DailyOverview> overviews) {
        if (overviews == null || overviews.isEmpty()) {
            rescueChart.clear();
            rescueChart.invalidate();
            return;
        }

        int pointsToUse = Math.min(chartDays, overviews.size());

        List<Entry> rescueEntries = new ArrayList<>();
        final List<String> xAxisLabels = new ArrayList<>();
        final List<DailyOverview> visibleOverviews = new ArrayList<>();

        int xIndex = 0;
        for (int i = pointsToUse - 1; i >= 0; i--, xIndex++) {
            DailyOverview overview = overviews.get(i);
            visibleOverviews.add(overview);
            rescueEntries.add(new Entry(xIndex, overview.rescueCount));

            String[] parts = overview.date.split("-");
            if (parts.length == 3) {
                xAxisLabels.add(parts[1] + "/" + parts[2]);
            } else {
                xAxisLabels.add(overview.date);
            }
        }

        LineDataSet rescueDataSet = new LineDataSet(rescueEntries, "Rescues");
        rescueDataSet.setColor(Color.WHITE);
        rescueDataSet.setLineWidth(2.5f);
        rescueDataSet.setCircleColor(Color.WHITE);
        rescueDataSet.setCircleHoleColor(Color.WHITE);
        rescueDataSet.setCircleRadius(4f);
        rescueDataSet.setCircleHoleRadius(2f);
        rescueDataSet.setDrawValues(false);
        rescueDataSet.setMode(LineDataSet.Mode.LINEAR);
        rescueDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        rescueDataSet.setHighLightColor(Color.WHITE);
        rescueDataSet.setHighlightLineWidth(2f);

        LineData lineData = new LineData(rescueDataSet);
        rescueChart.setData(lineData);
        rescueChart.getDescription().setEnabled(false);
        rescueChart.setBackgroundColor(Color.TRANSPARENT);
        rescueChart.setDrawGridBackground(true);
        rescueChart.setGridBackgroundColor(Color.parseColor("#8EC9FF"));

        rescueChart.animateY(800);
        rescueChart.setPinchZoom(true);
        rescueChart.setDoubleTapToZoomEnabled(false);

        rescueChart.getLegend().setEnabled(false);

        XAxis xAxis = rescueChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(11f);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        xAxis.setLabelCount(Math.min(4, xAxisLabels.size()), false);

        YAxis leftAxis = rescueChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisLineColor(Color.WHITE);
        leftAxis.setAxisLineWidth(1f);
        leftAxis.enableGridDashedLine(8f, 8f, 0f);
        leftAxis.setGridColor(Color.WHITE);
        leftAxis.setGridLineWidth(1f);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);

        rescueChart.getAxisRight().setEnabled(false);

        CustomMarkerView rescueMarker =
                new CustomMarkerView(this, R.layout.custom_marker_view, visibleOverviews, "RESCUE");
        rescueMarker.setChartView(rescueChart);
        rescueChart.setMarker(rescueMarker);

        rescueChart.invalidate();
    }

    private void setupZoneChart(List<DailyOverview> overviews) {
        if (overviews == null || overviews.isEmpty()) {
            zoneChart.clear();
            zoneChart.invalidate();
            return;
        }

        int pointsToUse = Math.min(chartDays, overviews.size());

        List<Entry> zoneEntries = new ArrayList<>();
        final List<String> xAxisLabels = new ArrayList<>();
        final List<DailyOverview> visibleOverviews = new ArrayList<>();

        int xIndex = 0;

        for (int i = pointsToUse - 1; i >= 0; i--, xIndex++) {
            DailyOverview overview = overviews.get(i);
            visibleOverviews.add(overview);

            int zoneValue;
            switch (overview.zone) {
                case "Green":  zoneValue = 3; break;
                case "Yellow": zoneValue = 2; break;
                case "Red":
                default:       zoneValue = 1; break;
            }

            zoneEntries.add(new Entry(xIndex, zoneValue));

            String[] parts = overview.date.split("-");
            if (parts.length == 3) {
                xAxisLabels.add(parts[1] + "/" + parts[2]);
            } else {
                xAxisLabels.add(overview.date);
            }
        }

        LineDataSet zoneDataSet = new LineDataSet(zoneEntries, "Zone");
        zoneDataSet.setColor(Color.WHITE);
        zoneDataSet.setLineWidth(2.5f);
        zoneDataSet.setCircleColor(Color.WHITE);
        zoneDataSet.setCircleHoleColor(Color.WHITE);
        zoneDataSet.setCircleRadius(4f);
        zoneDataSet.setCircleHoleRadius(2f);
        zoneDataSet.setDrawValues(false);
        zoneDataSet.setMode(LineDataSet.Mode.LINEAR);
        zoneDataSet.setDrawFilled(true);
        zoneDataSet.setFillColor(Color.WHITE);
        zoneDataSet.setFillAlpha(80);
        zoneDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        zoneDataSet.setHighLightColor(Color.WHITE);
        zoneDataSet.setHighlightLineWidth(2f);


        LineData lineData = new LineData(zoneDataSet);
        zoneChart.setData(lineData);
        zoneChart.getDescription().setEnabled(false);
        zoneChart.setBackgroundColor(Color.TRANSPARENT);
        zoneChart.setDrawGridBackground(true);
        zoneChart.setGridBackgroundColor(Color.parseColor("#8EC9FF"));

        zoneChart.animateY(800);
        zoneChart.setPinchZoom(true);
        zoneChart.setDoubleTapToZoomEnabled(false);


        Legend legend = zoneChart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = zoneChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(11f);
        xAxis.setLabelRotationAngle(-35f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        xAxis.setLabelCount(Math.min(4, xAxisLabels.size()), false);


        YAxis leftAxis = zoneChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisLineColor(Color.WHITE);
        leftAxis.setAxisLineWidth(1f);
        leftAxis.enableGridDashedLine(8f, 8f, 0f);
        leftAxis.setGridColor(Color.WHITE);
        leftAxis.setGridLineWidth(1f);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(4f);
        leftAxis.setLabelCount(5, true);

        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int v = Math.round(value);
                if (v == 1) return "🔴";
                if (v == 2) return "🟡";
                if (v == 3) return "🟢";
                return "";
            }
        });

        zoneChart.getAxisRight().setEnabled(false);


        CustomMarkerView marker =
                new CustomMarkerView(this, R.layout.custom_marker_view, visibleOverviews, "ZONE");
        marker.setChartView(zoneChart);
        zoneChart.setMarker(marker);

        TextView zoneChartTitle = findViewById(R.id.zoneChartTitle);
        if (isZoneChartVisible) {
            zoneChartTitle.setText("Daily Zone (last " + chartDays + " days)");
        } else {
            zoneChartTitle.setText("Daily Rescues (last " + chartDays + " days)");
        }

        zoneChart.invalidate();

    }

    private void updateZone(int currentPef, int personalBest) {
        if (personalBest == 0) {
            zoneTitle.setText(R.string.today_s_zone_not_set);
            zoneMessage.setText(R.string.please_set_your_personal_best_pef);
            zoneButton.setCardBackgroundColor(Color.parseColor("#9E9E9E")); // Gray
            pefValue.setText("PEF: Not Set");
            return;
        }

        double percentage = ((double) currentPef / personalBest) * 100;

        if (percentage >= 80) {
            zoneTitle.setText(R.string.today_s_zone_green);
            zoneMessage.setText(""); 
            zoneButton.setCardBackgroundColor(Color.parseColor("#90C4A5"));
        } else if (percentage >= 50) {
            zoneTitle.setText(R.string.today_s_zone_yellow);
            zoneMessage.setText("Caution: Child may need their reliever inhaler.");
            zoneButton.setCardBackgroundColor(Color.parseColor("#FFC107")); // Yellow
        } else {
            zoneTitle.setText(R.string.today_s_zone_red);
            zoneMessage.setText("Danger: Child may need reliever and medical attention.");
            zoneButton.setCardBackgroundColor(Color.parseColor("#F44336")); // Red
        }
        
        pefValue.setText("PEF: " + currentPef + " (PB: " + personalBest + ")");
    }

    private void showSetPersonalBestDialog() {
        if (selectedChildId == null) {
            Toast.makeText(this, "Please select a child first.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Personal Best");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter new Personal Best PEF value");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String pbString = input.getText().toString();
            if (!pbString.isEmpty()) {
                int newPersonalBest = Integer.parseInt(pbString);
                DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("Users")
                        .child("Parent").child(parentId).child("Children").child(selectedChildId);
                childRef.child("personalBest").setValue(newPersonalBest)
                        .addOnSuccessListener(aVoid -> Toast.makeText(ParentHomeActivity.this, "Personal Best updated.", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(ParentHomeActivity.this, "Failed to update Personal Best.", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void toggleAlertsPopup(View anchor) {
        if (alertsPopup != null && alertsPopup.isShowing()) {
            alertsPopup.dismiss();
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.layout_alerts_hub, null);

        RecyclerView alertsRecyclerView = popupView.findViewById(R.id.alertsRecyclerView);
        alertsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alertsRecyclerView.setAdapter(alertsAdapter);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = (int) (displayMetrics.widthPixels * 0.8);

        alertsPopup = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        alertsPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertsPopup.setOutsideTouchable(true);

        int xOff = -anchor.getWidth();
        int yOff = 8;

        alertsPopup.showAsDropDown(anchor, xOff, yOff);
    }

}