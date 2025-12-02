package com.example.smartairapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ProviderReportActivity extends AppCompatActivity {

    private EditText inputFromDate, inputToDate;
    private Switch toggleSymptoms, toggleMedicine, togglePEF, toggleTriage;
    private Button btnGeneratePreview, btnExportPdf;
    private LinearLayout previewContainer;

    private String parentId, childId;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
    private static final DateTimeFormatter SYMPTOM_TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault());

    private final ZoneId zoneId = ZoneId.systemDefault();
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_provider_report);

        parentId = getIntent().getStringExtra("parentId");
        childId = getIntent().getStringExtra("childId");

        inputFromDate = findViewById(R.id.inputFromDate);
        inputToDate = findViewById(R.id.inputToDate);
        toggleSymptoms = findViewById(R.id.toggleSymptoms);
        toggleMedicine = findViewById(R.id.toggleMedicine);
        togglePEF = findViewById(R.id.togglePEF);
        toggleTriage = findViewById(R.id.toggleTriage);
        btnGeneratePreview = findViewById(R.id.btnGeneratePreview);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        previewContainer = findViewById(R.id.previewContainer);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        inputFromDate.setOnClickListener(v -> showDatePicker(inputFromDate));
        inputToDate.setOnClickListener(v -> showDatePicker(inputToDate));

        btnGeneratePreview.setOnClickListener(v -> generatePreview());


    }

    private void showDatePicker(EditText target) {
        LocalDate today = LocalDate.now();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    LocalDate chosen = LocalDate.of(year, month + 1, dayOfMonth);
                    target.setText(DATE_FMT.format(chosen));
                },
                today.getYear(),
                today.getMonthValue() - 1,
                today.getDayOfMonth()
        );
        dialog.show();
    }

    private void generatePreview(){
        String fromStr = inputFromDate.getText().toString().trim();
        String toStr = inputToDate.getText().toString().trim();

        if (fromStr.isEmpty() || toStr.isEmpty()) {
            Toast.makeText(this, "Please select both From and To dates", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate fromDate, toDate;
        try {
            fromDate = LocalDate.parse(fromStr, DATE_FMT);
            toDate = LocalDate.parse(toStr, DATE_FMT);
        } catch (DateTimeParseException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (toDate.isBefore(fromDate)) {
            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
            return;
        }

        long days = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        if (days < 90 || days > 185) { // 3–6 months
            Toast.makeText(this, "Window must be between 3 and 6 months", Toast.LENGTH_LONG).show();
            return;
        }

        previewContainer.removeAllViews();
        btnGeneratePreview.setEnabled(false);

        DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId);

        childRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Integer plannedScheduleI = snapshot.child("plannedSchedule").getValue(Integer.class);
                Integer personalBestI = snapshot.child("personalBest").getValue(Integer.class);

                int plannedSchedule = plannedScheduleI == null ? 0 : plannedScheduleI;
                int personalBest = personalBestI == null ? 0 : personalBestI;

                DataSnapshot logsSnap = snapshot.child("Logs");

                buildReportFromSnapshot(fromDate, toDate, plannedSchedule, personalBest, logsSnap);

                btnGeneratePreview.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnGeneratePreview.setEnabled(true);
                Toast.makeText(ProviderReportActivity.this,
                        "Failed to load data: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildReportFromSnapshot(LocalDate fromDate, LocalDate toDate,
                                         int plannedSchedule, int personalBest,
                                         DataSnapshot logsSnap) {

        // maps for metrics
        Map<LocalDate, Integer> controllerPerDay = new HashMap<>();
        Map<LocalDate, Integer> rescuePerDay = new HashMap<>();
        Map<LocalDate, SymptomCategory> symptomPerDay = new HashMap<>();
        Map<LocalDate, Zone> zonePerDay = new HashMap<>();

        // medicine logs (controller + rescue)
        DataSnapshot medSnap = logsSnap.child("medicineLogs");
        for (DataSnapshot child : medSnap.getChildren()) {
            Long ts = child.child("timestamp").getValue(Long.class);
            if (ts == null) continue;
            LocalDate day = Instant.ofEpochMilli(ts)
                    .atZone(zoneId)
                    .toLocalDate();
            if (day.isBefore(fromDate) || day.isAfter(toDate)) continue;

            String inhalerType = child.child("inhalerType").getValue(String.class);
            if (inhalerType == null) continue;

            if (inhalerType.equalsIgnoreCase("Controller")) {
                increment(controllerPerDay, day);
            } else if (inhalerType.equalsIgnoreCase("Rescue")) {
                increment(rescuePerDay, day);
            }
        }

        // symptom logs
        DataSnapshot symSnap = logsSnap.child("symptomLogs");
        for (DataSnapshot child : symSnap.getChildren()) {
            String tsStr = child.child("timestamp").getValue(String.class);
            if (tsStr == null || tsStr.isEmpty()) continue;

            LocalDate day;
            try {
                day = LocalDate.parse(tsStr, SYMPTOM_TS_FMT);
            } catch (Exception e) {
                // fallback: just take first 10 chars "yyyy-MM-dd"
                try {
                    day = LocalDate.parse(tsStr.substring(0, 10), DATE_FMT);
                } catch (Exception ex) {
                    continue;
                }
            }

            if (day.isBefore(fromDate) || day.isAfter(toDate)) continue;

            boolean nightWaking = Boolean.TRUE.equals(
                    child.child("nightWaking").getValue(Boolean.class));
            boolean activityLimits = Boolean.TRUE.equals(
                    child.child("activityLimits").getValue(Boolean.class));
            String cough = child.child("coughWheeze").getValue(String.class);
            if (cough == null) cough = "none";

            SymptomCategory cat = classifySymptomDay(nightWaking, activityLimits, cough);
            symptomPerDay.put(day, cat);
        }

        // PEF logs to zones
        if (personalBest > 0) {
            DataSnapshot pefSnap = logsSnap.child("pefLogs");
            // for each day, we take the latest PEF reading
            Map<LocalDate, Integer> latestPefPerDay = new HashMap<>();

            for (DataSnapshot child : pefSnap.getChildren()) {
                Long ts = child.child("timestamp").getValue(Long.class);
                Long valueL = child.child("pefValue").getValue(Long.class);
                if (ts == null || valueL == null) continue;

                LocalDate day = Instant.ofEpochMilli(ts)
                        .atZone(zoneId)
                        .toLocalDate();
                if (day.isBefore(fromDate) || day.isAfter(toDate)) continue;

                int v = valueL.intValue();
                // override – we only care about latest
                latestPefPerDay.put(day, v);
            }

            for (Map.Entry<LocalDate, Integer> e : latestPefPerDay.entrySet()) {
                Zone z = classifyZone(e.getValue(), personalBest);
                zonePerDay.put(e.getKey(), z);
            }
        }

        // compute aggregates & build cards

        List<LocalDate> allDays = new ArrayList<>();
        LocalDate cursor = fromDate;
        while (!cursor.isAfter(toDate)) {
            allDays.add(cursor);
            cursor = cursor.plusDays(1);
        }

        if (toggleMedicine.isChecked() && plannedSchedule > 0) {
            addControllerAdherenceCard(allDays, controllerPerDay, plannedSchedule);
        }

        if (toggleMedicine.isChecked()) {
            addRescueFrequencyCard(allDays, rescuePerDay);
        }

        if (toggleSymptoms.isChecked()) {
            addSymptomBurdenCard(allDays, symptomPerDay);
        }

        if (togglePEF.isChecked() && personalBest > 0) {
            addZoneDistributionCard(zonePerDay);
        }

        if (toggleTriage.isChecked()) {
            loadSevereTriageIncidents(severeLogs -> {
                addTriageSummaryCard(severeLogs);
            });
        }

        // after all cards generated - now show export button
        btnExportPdf.setVisibility(View.VISIBLE);
        btnExportPdf.setOnClickListener(v -> {

            // Collect charts as bitmaps
            List<Bitmap> chartBitmaps = new ArrayList<>();

            if (toggleMedicine.isChecked()) {
                View controllerCard = previewContainer.findViewById(R.id.chartControllerAdherence);
                if (controllerCard instanceof PieChart) {
                    chartBitmaps.add(((PieChart) controllerCard).getChartBitmap());
                }

                View rescueCard = previewContainer.findViewById(R.id.chartRescueFrequency);
                if (rescueCard instanceof LineChart) {
                    chartBitmaps.add(((LineChart) rescueCard).getChartBitmap());
                }
            }

            if (toggleSymptoms.isChecked()) {
                View symptomCard = previewContainer.findViewById(R.id.chartSymptomBurden);
                if (symptomCard instanceof PieChart) {
                    chartBitmaps.add(((PieChart) symptomCard).getChartBitmap());
                }
            }

            if (togglePEF.isChecked()) {
                View zoneCard = previewContainer.findViewById(R.id.progGreen);
                if (zoneCard != null) {
                    Bitmap bmp = createBitmapFromView(
                            previewContainer.findViewById(R.id.zoneDistributionCardContainer)
                    );
                    chartBitmaps.add(bmp);
                }
            }

            // Load severe triage logs again
            loadSevereTriageIncidents(severeLogs -> {
                generatePDFReport(
                        chartBitmaps,
                        severeLogs,
                        inputFromDate.getText().toString(),
                        inputToDate.getText().toString(),
                        "Child"
                );
            });
        });


    }
    private enum SymptomCategory {
        PROBLEM_FREE,
        NIGHT_WAKING,
        ACTIVITY_LIMITS,
        COUGH_WHEEZE
    }

    private enum Zone {
        GREEN, YELLOW, RED
    }

    private SymptomCategory classifySymptomDay(boolean nightWaking, boolean activityLimits,
                                               String coughWheeze) {
        if (!nightWaking && !activityLimits &&
                (coughWheeze == null || coughWheeze.equalsIgnoreCase("none"))) {
            return SymptomCategory.PROBLEM_FREE;
        }
        if (nightWaking) return SymptomCategory.NIGHT_WAKING;
        if (activityLimits) return SymptomCategory.ACTIVITY_LIMITS;
        return SymptomCategory.COUGH_WHEEZE;
    }

    private Zone classifyZone(int currentPef, int personalBest) {
        if (personalBest == 0) return Zone.GREEN;
        double pct = (double) currentPef / personalBest * 100.0;
        if (pct >= 80.0) return Zone.GREEN;
        if (pct >= 50.0) return Zone.YELLOW;
        return Zone.RED;
    }

    private <T> void increment(Map<LocalDate, Integer> map, LocalDate day) {
        int cur = map.containsKey(day) ? map.get(day) : 0;
        map.put(day, cur + 1);
    }
    private String formatLongTimestamp(long ts) {
        Date date = new Date(ts);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
        return sdf.format(date);
    }


    //cards / charts
    private void addTriageSummaryCard(List<TriageLog> severeLogs) {

        View card = getLayoutInflater().inflate(R.layout.card_report_triage_summary, previewContainer, false);

        TextView title = card.findViewById(R.id.txtTriageTitle);
        TextView subtitle = card.findViewById(R.id.txtTriageCount);
        LinearLayout listContainer = card.findViewById(R.id.triageListContainer);

        title.setText("Severe Triage Incidents");

        if (severeLogs.isEmpty()) {
            subtitle.setText("No severe incidents in this period.");
            previewContainer.addView(card);
            return;
        }

        subtitle.setText(severeLogs.size() + " severe incident(s) recorded");

        for (TriageLog log : severeLogs) {

            View item = getLayoutInflater().inflate(R.layout.item_triage_summary_row, null);

            TextView itemDate = item.findViewById(R.id.triageItemDate);
            TextView itemResult = item.findViewById(R.id.triageItemResult);
            TextView itemNotes = item.findViewById(R.id.triageItemNotes);

            // convert timestamp → readable date
            String dateStr = formatLongTimestamp(log.timeStampStarted);
            itemDate.setText(dateStr);

            itemResult.setText("Result: Severe");

            if (log.notes == null || log.notes.isEmpty())
                itemNotes.setText("(No notes)");
            else
                itemNotes.setText(log.notes);

            listContainer.addView(item);
        }

        previewContainer.addView(card);
    }

    public interface ReportDataCallback {
        void onComplete(List<TriageLog> severeLogs);
    }

    private void loadSevereTriageIncidents(ReportDataCallback callback) {

        DatabaseReference triageRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child("Parent")
                .child(parentId)
                .child("Children")
                .child(childId)
                .child("Logs")
                .child("triageLogs");

        triageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<TriageLog> severeIncidents = new ArrayList<>();

                for (DataSnapshot childSnap : snapshot.getChildren()) {
                    TriageLog log = childSnap.getValue(TriageLog.class);
                    if (log == null) continue;

                    // Only add logs where result == "Severe"
                    if ("Severe".equalsIgnoreCase(log.result)) {
                        severeIncidents.add(log);
                    }
                }

                callback.onComplete(severeIncidents);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onComplete(new ArrayList<>());
            }
        });
    }


    private void addControllerAdherenceCard(List<LocalDate> allDays,
                                            Map<LocalDate, Integer> controllerPerDay,
                                            int plannedSchedule) {

        int adherentDays = 0;
        for (LocalDate d : allDays) {
            int doses = controllerPerDay.containsKey(d) ? controllerPerDay.get(d) : 0;
            if (doses >= plannedSchedule) {
                adherentDays++;
            }
        }

        float percent = allDays.isEmpty()
                ? 0f
                : (adherentDays * 100f / allDays.size());

        View card = getLayoutInflater().inflate(
                R.layout.card_report_controller_adherence, previewContainer, false);

        PieChart chart = card.findViewById(R.id.chartControllerAdherence);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(percent, "On-plan days"));
        entries.add(new PieEntry(100f - percent, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(0xFF3DDC84); // green
        colors.add(0xFFE0E0E0); // gray remainder
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setUsePercentValues(true);
        chart.setDrawEntryLabels(false);
        chart.setHoleRadius(70f);
        chart.setTransparentCircleRadius(75f);
        chart.setCenterText(String.format(Locale.getDefault(), "%.0f%%", percent));
        chart.setCenterTextSize(20f);

        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        chart.invalidate();
        previewContainer.addView(card);
    }

    private void addRescueFrequencyCard(List<LocalDate> allDays,
                                        Map<LocalDate, Integer> rescuePerDay) {

        View card = getLayoutInflater().inflate(
                R.layout.card_report_rescue_frequency, previewContainer, false);

        LineChart chart = card.findViewById(R.id.chartRescueFrequency);

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < allDays.size(); i++) {
            LocalDate d = allDays.get(i);
            int count = rescuePerDay.containsKey(d) ? rescuePerDay.get(d) : 0;
            entries.add(new Entry(i, count));
            labels.add(d.getMonthValue() + "/" + d.getDayOfMonth());
        }

        LineDataSet dataSet = new LineDataSet(entries, "Rescues per day");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);

        LineData data = new LineData(dataSet);
        chart.setData(data);

        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });

        chart.getAxisRight().setEnabled(false);
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);

        chart.invalidate();
        previewContainer.addView(card);
    }

    private void addSymptomBurdenCard(List<LocalDate> allDays,
                                      Map<LocalDate, SymptomCategory> symptomPerDay) {

        int problemFree = 0;
        int night = 0;
        int activity = 0;
        int cough = 0;

        for (LocalDate d : allDays) {
            SymptomCategory cat = symptomPerDay.getOrDefault(d, SymptomCategory.PROBLEM_FREE);
            switch (cat) {
                case PROBLEM_FREE:
                    problemFree++;
                    break;
                case NIGHT_WAKING:
                    night++;
                    break;
                case ACTIVITY_LIMITS:
                    activity++;
                    break;
                case COUGH_WHEEZE:
                    cough++;
                    break;
            }
        }

        int total = problemFree + night + activity + cough;
        if (total == 0) total = 1; // avoid div by 0

        View card = getLayoutInflater().inflate(
                R.layout.card_report_symptom_burden, previewContainer, false);

        PieChart chart = card.findViewById(R.id.chartSymptomBurden);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(problemFree, "Problem-free"));
        entries.add(new PieEntry(night, "Night waking"));
        entries.add(new PieEntry(activity, "Activity limits"));
        entries.add(new PieEntry(cough, "Cough/wheeze"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(0xFF4CAF50); // green
        colors.add(0xFFFFC107); // yellow
        colors.add(0xFF03A9F4); // blue
        colors.add(0xFFFF7043); // orange
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(11f);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setUsePercentValues(true);
        chart.setDrawEntryLabels(false);

        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);

        Legend legend = chart.getLegend();
        legend.setTextSize(11f);

        chart.invalidate();
        previewContainer.addView(card);
    }

    private void addZoneDistributionCard(Map<LocalDate, Zone> zonePerDay) {

        int green = 0, yellow = 0, red = 0;

        for (Zone z : zonePerDay.values()) {
            switch (z) {
                case GREEN:
                    green++;
                    break;
                case YELLOW:
                    yellow++;
                    break;
                case RED:
                    red++;
                    break;
            }
        }

        int total = green + yellow + red;
        if (total == 0) total = 1;

        int greenPct = Math.round(green * 100f / total);
        int yellowPct = Math.round(yellow * 100f / total);
        int redPct = Math.round(red * 100f / total);

        View card = getLayoutInflater().inflate(
                R.layout.card_report_zone_distribution, previewContainer, false);

        ProgressBar progGreen = card.findViewById(R.id.progGreen);
        ProgressBar progYellow = card.findViewById(R.id.progYellow);
        ProgressBar progRed = card.findViewById(R.id.progRed);
        TextView txtGreen = card.findViewById(R.id.txtGreenPercent);
        TextView txtYellow = card.findViewById(R.id.txtYellowPercent);
        TextView txtRed = card.findViewById(R.id.txtRedPercent);

        progGreen.setProgress(greenPct);
        progYellow.setProgress(yellowPct);
        progRed.setProgress(redPct);

        txtGreen.setText(greenPct + "%");
        txtYellow.setText(yellowPct + "%");
        txtRed.setText(redPct + "%");

        previewContainer.addView(card);
    }


    private void generatePDFReport(
            List<Bitmap> chartBitmaps,
            List<TriageLog> severeIncidents,
            String fromDateStr,
            String toDateStr,
            String childName) {

        PdfDocument pdf = new PdfDocument();

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(16);

        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(22);
        titlePaint.setFakeBoldText(true);

        int pageWidth = 595;   // A4 width in points
        int pageHeight = 842;  // A4 height in points
        int y = 50;

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // header
        canvas.drawText("Provider Report for " + childName, 30, y, titlePaint);
        y += 30;
        canvas.drawText("Date Range: " + fromDateStr + "  →  " + toDateStr, 30, y, textPaint);
        y += 50;

        // charts
        for (Bitmap bmp : chartBitmaps) {
            if (bmp == null) continue;

            // scale image to fit PDF width
            float scale = (float) (pageWidth - 60) / bmp.getWidth();
            Bitmap scaled = Bitmap.createScaledBitmap(
                    bmp,
                    (int) ((float) bmp.getWidth() * scale),
                    (int) ((float) bmp.getHeight() * scale),
                    true
            );

            canvas.drawBitmap(scaled, 30, y, null);
            y += scaled.getHeight() + 30;

            // If we run out of space -> new page
            if (y > pageHeight - 200) {
                pdf.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
                page = pdf.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
        }

        // triage incidents
        canvas.drawText("Severe Triage Incidents", 30, y, titlePaint);
        y += 30;

        if (severeIncidents.isEmpty()) {
            canvas.drawText("No severe incidents recorded.", 30, y, textPaint);
            y += 20;
        } else {
            for (TriageLog log : severeIncidents) {

                String dateStr = formatLongTimestamp(log.timeStampStarted);

                canvas.drawText("• " + dateStr, 40, y, textPaint);
                y += 20;

                if (log.notes != null && !log.notes.isEmpty()) {
                    canvas.drawText("  Notes: " + log.notes, 40, y, textPaint);
                    y += 20;
                }

                if (log.redFlags != null) {
                    String rf = "";
                    if (log.redFlags.cantSpeak) rf += "Can't speak, ";
                    if (log.redFlags.blueLips) rf += "Blue lips, ";
                    if (log.redFlags.chestRestriction) rf += "Chest restriction";

                    if (!rf.isEmpty()) {
                        canvas.drawText("  Red flags: " + rf, 40, y, textPaint);
                        y += 20;
                    }
                }

                y += 10;

                if (y > pageHeight - 200) {
                    pdf.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
                    page = pdf.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }
            }
        }

        pdf.finishPage(page);

        // save PDF
        File pdfFile = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Provider_Report.pdf"
        );

        try {
            FileOutputStream fos = new FileOutputStream(pdfFile);
            pdf.writeTo(fos);
            fos.close();
            pdf.close();

            Toast.makeText(this, "PDF saved: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create PDF", Toast.LENGTH_SHORT).show();
        }
    }
    private Bitmap createBitmapFromView(View view) {
        view.measure(
                View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(view.getHeight(), View.MeasureSpec.EXACTLY)
        );
        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        view.draw(canvas);
        return bmp;
    }
}


