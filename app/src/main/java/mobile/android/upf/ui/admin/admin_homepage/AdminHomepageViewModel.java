package mobile.android.upf.ui.admin.admin_homepage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AdminHomepageViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AdminHomepageViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}