package mobile.android.upf.ui.admin.admin_profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AdminProfileViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AdminProfileViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}