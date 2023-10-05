package com.example.realtimelocationtrackergoogle6;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

public class AutocompletePredictionsAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = AutocompletePredictionsAdapter.class.getSimpleName();

    private Context context;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionsList;

    AutocompletePredictionsAdapter(Context context, PlacesClient placesClient) {
        this.context = context;
        this.placesClient = placesClient;
        predictionsList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return predictionsList.size();
    }

    @Override
    public AutocompletePrediction getItem(int position) {
        return predictionsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AutocompletePrediction prediction = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(prediction.getFullText(null));
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                final FilterResults filterResults = new FilterResults();
                if (charSequence != null) {
                    // Fetch predictions from Places API
                    FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                            .setQuery(charSequence.toString())
                            .build();

                    placesClient.findAutocompletePredictions(request)
                            .addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                                @Override
                                public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                                    if (task.isSuccessful()) {
                                        List<AutocompletePrediction> predictions = task.getResult().getAutocompletePredictions();
                                        if (predictions != null) {
                                            predictionsList.clear();
                                            predictionsList.addAll(predictions);
                                            filterResults.values = predictionsList;
                                            filterResults.count = predictionsList.size();
                                            notifyDataSetChanged();
                                        }
                                    } else {
                                        Log.e(TAG, "Autocomplete prediction fetching failed: " + task.getException());
                                    }
                                }
                            });
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults != null && filterResults.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                // Converts a result value to display in the AutocompletePrediction TextView
                if (resultValue instanceof AutocompletePrediction) {
                    return ((AutocompletePrediction) resultValue).getFullText(null);
                } else {
                    return super.convertResultToString(resultValue);
                }
            }
        };
    }
}

