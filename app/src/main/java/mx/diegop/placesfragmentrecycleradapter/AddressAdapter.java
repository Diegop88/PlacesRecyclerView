package mx.diegop.placesfragmentrecycleradapter;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
    private static final String TAG = AddressAdapter.class.getSimpleName();

    private GoogleApiClient apiClient;
    private LatLngBounds latLngBounds;
    private OnAddressSelectedListener listener;
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    private AutocompletePredictionBuffer predictions;

    AddressAdapter(GoogleApiClient apiClient, LatLngBounds latLngBounds, OnAddressSelectedListener listener) {
        this.apiClient = apiClient;
        this.latLngBounds = latLngBounds;
        this.listener = listener;

        predictions = new AutocompletePredictionBuffer(null);
    }

    void setAddressIn(String s){
        if(apiClient.isConnected()) {
            PendingResult<AutocompletePredictionBuffer> result = Places.GeoDataApi.getAutocompletePredictions(apiClient, s, latLngBounds, null);
            result.setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                @Override
                public void onResult(@NonNull AutocompletePredictionBuffer autocompletePredictions) {
                    setPredictions(autocompletePredictions);
                }
            });
        }
    }

    private void setPredictions(AutocompletePredictionBuffer predictions) {
        Log.d(TAG, "Done" + predictions.getCount());
        this.predictions = predictions;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(predictions.get(position));
    }

    @Override
    public int getItemCount() {
        return predictions.getCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView address;
        TextView address2;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onAddressSelected(predictions.get(getAdapterPosition()));
                }
            });

            address = (TextView) itemView.findViewById(R.id.item_address);
            address2 = (TextView) itemView.findViewById(R.id.item_address_two);
        }

        void bind(AutocompletePrediction autocompletePrediction) {
            address.setText(autocompletePrediction.getPrimaryText(STYLE_BOLD));
            address2.setText(autocompletePrediction.getSecondaryText(STYLE_BOLD));
        }
    }

    public interface OnAddressSelectedListener {
        void onAddressSelected(AutocompletePrediction autocompletePrediction);
    }
}
