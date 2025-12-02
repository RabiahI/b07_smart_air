package com.example.smartairapplication.parent_home.overview;

import com.example.smartairapplication.models.DailyOverview;
import com.example.smartairapplication.models.Child;
import com.example.smartairapplication.models.MedicineLog;
import com.example.smartairapplication.models.PefLog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class OverviewCalculator {

    public static void updateDailyOverview(String parentId, String childId) {
        DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent").child(parentId)
                .child("Children").child(childId);

        childRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Child child = snapshot.getValue(Child.class);
                if (child == null || child.getPersonalBest() == 0) {
                    return;
                }

                List<DailyOverview> dailyOverviews = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Calendar cal = Calendar.getInstance();
                long thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);

                for (int i = 0; i < 30; i++) {
                    cal.setTimeInMillis(System.currentTimeMillis() - ((long) i * 24 * 60 * 60 * 1000));
                    String date = sdf.format(cal.getTime());
                    dailyOverviews.add(new DailyOverview(date, "Green", 0));
                }

                DataSnapshot pefLogsSnapshot = snapshot.child("Logs").child("pefLogs");
                for (DataSnapshot pefSnap : pefLogsSnapshot.getChildren()) {
                    PefLog log = pefSnap.getValue(PefLog.class);
                    if (log != null && log.getTimestamp() >= thirtyDaysAgo) {
                        cal.setTimeInMillis(log.getTimestamp());
                        String date = sdf.format(cal.getTime());
                        DailyOverview day = findOverviewForDate(dailyOverviews, date);

                        if (day != null) {
                            double percentage = ((double) log.getPefValue() / child.getPersonalBest()) * 100;
                            String zone = "Green";
                            if (percentage < 50) {
                                zone = "Red";
                            } else if (percentage < 80) {
                                zone = "Yellow";
                            }
                            if (zone.equals("Red") || (zone.equals("Yellow") && day.zone.equals("Green"))) {
                                day.zone = zone;
                            }
                        }
                    }
                }

                DataSnapshot medLogsSnapshot = snapshot.child("Logs").child("medicineLogs");
                for (DataSnapshot medSnap : medLogsSnapshot.getChildren()) {
                    MedicineLog log = medSnap.getValue(MedicineLog.class);
                    if (log != null && "Rescue".equals(log.getInhalerType()) && log.getTimestamp() >= thirtyDaysAgo) {
                        cal.setTimeInMillis(log.getTimestamp());
                        String date = sdf.format(cal.getTime());
                        DailyOverview day = findOverviewForDate(dailyOverviews, date);
                        if (day != null) {
                            day.rescueCount++;
                        }
                    }
                }

                snapshot.getRef().child("DailyOverviews").setValue(dailyOverviews);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error if needed.
            }
        });
    }

    private static DailyOverview findOverviewForDate(List<DailyOverview> overviews, String date) {
        for (DailyOverview overview : overviews) {
            if (overview.date.equals(date)) {
                return overview;
            }
        }
        return null;
    }
}