package mx.diegop.placesfragmentrecycleradapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class AddressFragment extends Fragment implements AddressAdapter.OnAddressSelectedListener {
    private static final String TAG = AddressFragment.class.getSimpleName();

    GoogleApiClient apiClient;

    EditText address;
    RecyclerView listAddresses;
    AddressAdapter addressAdapter;
    private LatLngBounds mexico = new LatLngBounds(new LatLng(19.235708, -99.344219), new LatLng(19.715924, -98.874655));

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .build();

        addressAdapter = new AddressAdapter(apiClient, mexico, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        address = (EditText) view.findViewById(R.id.address);
        listAddresses = (RecyclerView) view.findViewById(R.id.list_addresses);

        address.addTextChangedListener(textWatcher);

        listAddresses.setLayoutManager(new LinearLayoutManager(getActivity()));
        listAddresses.setAdapter(addressAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        apiClient.connect();
    }

    @Override
    public void onStop() {
        apiClient.disconnect();
        super.onStop();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            searchAddress(s.toString());
        }
    };

    private void searchAddress(String s) {
        addressAdapter.setAddressIn(s);
    }

    @Override
    public void onAddressSelected(AutocompletePrediction autocompletePrediction) {
        getAddressInfo(autocompletePrediction);
    }

    private void getAddressInfo(AutocompletePrediction autocompletePrediction) {
        final String placeId = autocompletePrediction.getPlaceId();
        final CharSequence primaryText = autocompletePrediction.getPrimaryText(null);

        Log.i(TAG, "Autocomplete item selected: " + primaryText);

        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(apiClient, placeId);
        placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

        Toast.makeText(getActivity(), "Clicked: " + primaryText, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }

            // Get the Place object from the buffer.
            final Place place = places.get(0);
            Log.i(TAG, "Lat: " + place.getLatLng().latitude + ", Lng: " + place.getLatLng().longitude);

            // Format details of the place for display and show it in a TextView.
//            mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
//                    place.getId(), place.getAddress(), place.getPhoneNumber(),
//                    place.getWebsiteUri()));

            // Display the third party attributions if set.
            final CharSequence thirdPartyAttribution = places.getAttributions();
//            Log.i(TAG, "" + Html.fromHtml(thirdPartyAttribution.toString()));
            Log.i(TAG, "Place details received: " + place.getName());
            places.release();
        }
    };
}
