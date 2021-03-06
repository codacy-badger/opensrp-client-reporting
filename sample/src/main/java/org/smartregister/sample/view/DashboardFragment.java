package org.smartregister.sample.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.smartregister.reporting.contract.ReportContract;
import org.smartregister.reporting.listener.PieChartSelectListener;
import org.smartregister.reporting.domain.IndicatorTally;
import org.smartregister.reporting.view.NumericDisplayFactory;
import org.smartregister.reporting.domain.NumericIndicatorVisualization;
import org.smartregister.reporting.view.PieChartFactory;
import org.smartregister.reporting.domain.PieChartIndicatorVisualization;
import org.smartregister.reporting.domain.PieChartSlice;
import org.smartregister.sample.R;
import org.smartregister.sample.presenter.SamplePresenter;
import org.smartregister.sample.repository.SampleRepository;
import org.smartregister.sample.utils.ChartUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment implements ReportContract.View, LoaderManager.LoaderCallbacks<List<Map<String, IndicatorTally>>> {

    private ViewGroup visualizationsViewGroup;
    private View pieChartView;
    private View numericIndicatorView;
    private static ReportContract.Presenter presenter;
    private List<Map<String, IndicatorTally>> indicatorTallies;

    public DashboardFragment() {
        // Required empty public constructor
    }

    public static DashboardFragment newInstance() {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // fetch Indicator data
        presenter = new SamplePresenter(this);
        presenter.scheduleRecurringTallyJob();
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        visualizationsViewGroup = getView().findViewById(R.id.dashboard_content);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void buildVisualisations() {

        if (indicatorTallies == null || indicatorTallies.isEmpty()) {
            return;
        }
        // Aggregate values for display
        Map<String, IndicatorTally> numericIndicatorValue = new HashMap<>();
        Map<String, IndicatorTally> pieChartYesValue = new HashMap<>();
        Map<String, IndicatorTally> pieChartNoValue = new HashMap<>();

        for (Map<String, IndicatorTally> indicatorTallyMap : indicatorTallies) {
            if (indicatorTallyMap.containsKey(ChartUtil.numericIndicatorKey)) {
                updateTotalTally(indicatorTallyMap, numericIndicatorValue, ChartUtil.numericIndicatorKey);
            }
            if (indicatorTallyMap.containsKey(ChartUtil.pieChartYesIndicatorKey)) {
                updateTotalTally(indicatorTallyMap, pieChartYesValue, ChartUtil.pieChartYesIndicatorKey);
            }

            if (indicatorTallyMap.containsKey(ChartUtil.pieChartNoIndicatorKey)) {
                updateTotalTally(indicatorTallyMap, pieChartNoValue, ChartUtil.pieChartNoIndicatorKey);
            }
        }

        // Generate numeric indicator visualization
        NumericIndicatorVisualization numericIndicatorData = new NumericIndicatorVisualization(getResources().getString(R.string.total_under_5_count), numericIndicatorValue.get(ChartUtil.numericIndicatorKey).getCount());

        NumericDisplayFactory numericIndicatorFactory = new NumericDisplayFactory();
        numericIndicatorView = numericIndicatorFactory.getIndicatorView(numericIndicatorData, getContext());

        // Generate pie chart

        // Define pie chart chartSlices
        List<PieChartSlice> chartSlices = new ArrayList<>();

        PieChartSlice yesSlice = new PieChartSlice(pieChartYesValue.get(ChartUtil.pieChartYesIndicatorKey).getCount(), ChartUtil.YES_GREEN_SLICE_COLOR);
        PieChartSlice noSlice = new PieChartSlice(pieChartNoValue.get(ChartUtil.pieChartNoIndicatorKey).getCount(), ChartUtil.NO_RED_SLICE_COLOR);
        chartSlices.add(yesSlice);
        chartSlices.add(noSlice);

        // Build the chart
        PieChartIndicatorVisualization pieChartIndicatorVisualization = new PieChartIndicatorVisualization.PieChartIndicatorVisualizationBuilder()
                .indicatorLabel(getResources().getString(R.string.num_of_lieterate_children_0_60_label))
                .chartHasLabels(true)
                .chartHasLabelsOutside(true)
                .chartHasCenterCircle(false)
                .chartSlices(chartSlices)
                .chartListener(new ChartListener()).build();

        PieChartFactory pieChartFactory = new PieChartFactory();
        pieChartView = pieChartFactory.getIndicatorView(pieChartIndicatorVisualization, getContext());

        visualizationsViewGroup.addView(numericIndicatorView);
        visualizationsViewGroup.addView(pieChartView);

    }

    private void updateTotalTally(Map<String, IndicatorTally> indicatorTallyMap, Map<String, IndicatorTally> currentIndicatorValueMap, String indicatorKey) {
        int count, currentValue;
        count = indicatorTallyMap.get(indicatorKey).getCount();
        if (currentIndicatorValueMap.get(indicatorKey) == null) {
            currentIndicatorValueMap.put(indicatorKey, new IndicatorTally(null, count, indicatorKey, null));
            return;
        }
        currentValue = currentIndicatorValueMap.get(indicatorKey).getCount();
        currentIndicatorValueMap.get(indicatorKey).setCount(count + currentValue);
    }

    @Override
    public void refreshUI() {
        buildVisualisations();
    }

    @NonNull
    @Override
    public Loader<List<Map<String, IndicatorTally>>> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new ReportIndicatorsLoader(getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Map<String, IndicatorTally>>> loader, List<Map<String, IndicatorTally>> indicatorTallies) {
        this.indicatorTallies = indicatorTallies;
        refreshUI();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Map<String, IndicatorTally>>> loader) {
    }

    private class ChartListener implements PieChartSelectListener {

        @Override
        public void handleOnSelectEvent(PieChartSlice sliceValue) {
            Toast.makeText(getContext(), ChartUtil.getPieSelectionValue(sliceValue, getContext()), Toast.LENGTH_SHORT).show();
        }
    }

    private static class ReportIndicatorsLoader extends AsyncTaskLoader<List<Map<String, IndicatorTally>>> {

        public ReportIndicatorsLoader(Context context) {
            super(context);
        }

        @Nullable
        @Override
        public List<Map<String, IndicatorTally>> loadInBackground() {
            return presenter.fetchIndicatorsDailytallies();
        }
    }
}
