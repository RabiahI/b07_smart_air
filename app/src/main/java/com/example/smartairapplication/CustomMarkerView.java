package com.example.smartairapplication;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.List;

public class CustomMarkerView extends MarkerView {

    private TextView tvDate, tvRescues, tvZone;
    private List<DailyOverview> overviews;

    public CustomMarkerView(Context context, int layoutResource, List<DailyOverview> overviews) {
        super(context, layoutResource);
        tvDate = findViewById(R.id.tvDate);
        tvRescues = findViewById(R.id.tvRescues);
        tvZone = findViewById(R.id.tvZone);
        this.overviews = overviews;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int index = (int) e.getX();
        if (index >= 0 && index < overviews.size()) {
            DailyOverview overview = overviews.get(index);
            tvDate.setText("Date: " + overview.date);
            tvRescues.setText("Rescues: " + overview.rescueCount);
            tvZone.setText("Zone: " + overview.zone);
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}