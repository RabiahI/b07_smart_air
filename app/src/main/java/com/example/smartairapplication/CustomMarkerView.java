package com.example.smartairapplication;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.List;

public class CustomMarkerView extends MarkerView {

    private TextView tvDate, tvRescues, tvZone;
    private List<DailyOverview> overviews;
    private String chartType;

    public CustomMarkerView(Context context, int layoutResource, List<DailyOverview> overviews, String chartType) {
        super(context, layoutResource);
        tvDate = findViewById(R.id.tvDate);
        tvRescues = findViewById(R.id.tvRescues);
        tvZone = findViewById(R.id.tvZone);
        this.overviews = overviews;
        this.chartType = chartType;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int index = (int) e.getX();
        if (index >= 0 && index < overviews.size()) {
            DailyOverview overview = overviews.get(index);
            tvDate.setText("Date: " + overview.date);

            if ("ZONE".equals(chartType)) {
                tvZone.setVisibility(View.VISIBLE);
                tvRescues.setVisibility(View.GONE);
                tvZone.setText("Zone: " + overview.zone);
            } else if ("RESCUE".equals(chartType)) {
                tvRescues.setVisibility(View.VISIBLE);
                tvZone.setVisibility(View.GONE);
                tvRescues.setText("Rescues: " + overview.rescueCount);
            }
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}